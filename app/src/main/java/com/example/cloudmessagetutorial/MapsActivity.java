package com.example.cloudmessagetutorial;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


import com.firebase.geofire.GeoFire;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.List;

/*
 *
 *
 * Where Map is being shown and shows device location
 * Uses all the other classes to send notification via Firebase
 * Uses physics class to determine location of message and uses Geoquery
 * to determine the location of device.
 *
 *
 * Author: Safwan Ahman
 *
 * */

public class MapsActivity extends FragmentActivity implements  OnMapReadyCallback
        , GoogleApiClient.ConnectionCallbacks
        ,GoogleApiClient.OnConnectionFailedListener
        , LocationListener
        , GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    private static final int MY_PERMISSION_REQUEST_CODE = 2019;
    private static final int PLAY_SERVCE_RES_REQUEST = 1995;

    private LatLng mDefaultLocation = new LatLng(4.965173, 114.951696);
    private LatLng  here = new LatLng(4.971249, 114.952393);

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private boolean isInRadius = false;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private String msgTitle;
    private String msgBody;
    private String currentPlace = "Valak is here";
    private String namePlace;
    private String mood = "";

    private Marker mCurrent;
    private DatabaseReference ref;

    private final String TAG = "VB LOG TEXT";
    private GeoFire geoFire;

    private List<Messages> unsendMessagesList = new ArrayList<>();

    private LocationNames locationNames;

    private List<GeoQuery> geoQueriesList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //initialized location names here
        locationNames = new LocationNames();

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, msg);
                        Toast.makeText(MapsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

        ref = FirebaseDatabase.getInstance().getReference("My Location");
        geoFire = new GeoFire(ref);
        setUpLocation();

    }


    @Override
    public void onResume(){
        super.onResume();

        Log.d("ONRESUME" , "ENTERING ON RESUME");

        SharedPreferences e = getSharedPreferences("SHARED", MODE_PRIVATE);

        int restoredText = e.getInt("size", 0);
        if(restoredText != 0){
            for(int i=0; i < e.getInt("size",0); i++ ){
                String title = "title" + Integer.toString(i);
                String body = "body" + Integer.toString(i);
                String place = "place" + Integer.toString(i);
                //String lat = "lat" + Integer.toString(i);
                //String lon  = "lon" + Integer.toString(i);

                String t = e.getString(title,  "(no title)");
                String b = e.getString(body, "(no body)");
                String p = e.getString(place, "(no place)");
                //String la = e.getString(lat, "(no latitude)");
                ///String lo = e.getString(lon, "(no longitude");  //need to put in after type is implemented

                unsendMessagesList.add(new Messages(t,b, p) ); //, new LatLng(Double.valueOf( la),Double.valueOf( lo)) );
                Log.d("ONRESUME: ", t);
                Log.d("ONRESUME: ", b);
                Log.d("ONRESUME: ", p);
                Log.d("ONRESUME SIZE: ", Integer.toString(unsendMessagesList.size()));
            }
        }

        e.edit().clear();
        e.edit().commit();

        //might not need to display location again and again
        checkUnsendMessages(mood);
    }

    @Override
    public void onPause(){
        super.onPause();
        //SharedPreferences settings = getSharedPreferences("TEST", 0)
        SharedPreferences.Editor editor = (SharedPreferences.Editor) getSharedPreferences("SHARED", MODE_PRIVATE).edit();

        for(int count = 0; count < unsendMessagesList.size(); count++){
            String title = "title" + Integer.toString(count);
            String body = "body" + Integer.toString(count);
            String place = "place" + Integer.toString(count);
          //  String lat = "lat" + Integer.toString(count);
          //  String lon  = "lon" + Integer.toString(count);
            //String type = "type" + Integer.toString(count);    //not being used now, for future purposes

            editor.putString(title, unsendMessagesList.get(count).getTitle() );
            editor.putString(body, unsendMessagesList.get(count).getBody() );
            editor.putString(place, unsendMessagesList.get(count).getPlace() );
            editor.putInt("size", unsendMessagesList.size());

       //     editor.putString(lat, Double.toString(unsendMessagesList.get(count).getLatLng().latitude) );
       //     editor.putString(lon,Double.toString(unsendMessagesList.get(count).getLatLng().longitude));
        }
        editor.clear();
        editor.commit();
        unsendMessagesList.clear();
        Log.d("onPause", "SUCCESSFULLY COMMITTED");
        checkUnsendMessages(mood);

    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        final Intent mIntent = getIntent();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("IntentKey"));


        LatLng crossfit = new LatLng(4.930940, 114.840726);
        LatLng house = new LatLng(4.899541, 114.849591);
        LatLng huahomanggis = new LatLng(4.947292, 114.960772);
        LatLng giant = new LatLng(4.952149, 114.907406);
        LatLng pizza = new LatLng(4.901201, 114.901684);
        LatLng haz = new LatLng(4.917353, 114.911082);
        LatLng kia = new LatLng(4.981852, 114.953762);
        LatLng relentless = new LatLng(4.907545, 114.924353);

        locationNames.setPlaces("MCD", mDefaultLocation,  700,this, googleMap,"lets eat here");
        locationNames.setPlaces("673 Jerudong", crossfit,70, this, googleMap, "I gym here");
        locationNames.setPlaces("House", house, 70,this, googleMap,"my house");
        locationNames.setPlaces("Giant", giant, 300,this,googleMap,"We are meeting at Jolibee");
        locationNames.setPlaces("Pizza", pizza, 400,this,googleMap,"Lets eat Pizza");
        locationNames.setPlaces("Haz", haz, 300,this,googleMap,"My second gym is here");
        locationNames.setPlaces("KIA", kia, 400,this, googleMap,"Car broom broom");
        locationNames.setPlaces("HuaHo Manggis", huahomanggis, 70,this, googleMap,"buy BB's watch pls");
        locationNames.setPlaces("Relentless", relentless, 600, this, googleMap, "dance dance dannce");

        Log.d("CHECK LOCATION SIZE: ", Integer.toString(locationNames.getSize()));

        //only place where it should be
        checkGeoQuery(googleMap);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults){
        switch(requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();

                    }
                }break;

        }
    }

    public boolean checkPlayServices(){
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVCE_RES_REQUEST).show();
            }else
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        return true;
    }

    private void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {

                    // Set the map's camera position to the current location of the device.
                    mLastLocation = (Location) task.getResult();//(Location) task.getResult();

                    final double latitude = mLastLocation.getLatitude();
                    final double longtitiude = mLastLocation.getLongitude();

                   //update to database
                    geoFire.setLocation("You", new GeoLocation(latitude, longtitiude),
                            new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {

                                    if(mCurrent != null)
                                        mCurrent.remove();   //remove old marker
                                    mCurrent = mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(latitude,longtitiude))
                                            .title("You"));
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(mLastLocation.getLatitude(),
                                                    mLastLocation.getLongitude()), 17));
                                }
                            });

                    LatLng currentLoc = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(currentLoc).title("ME"));

                    Log.d(TAG, String.format("Your Location was changed: %f / %f", latitude,longtitiude));

                } else {
                    Log.d(TAG, "Current location is null. Using defaults.");
                    Log.e(TAG, "Exception: %s", task.getException());
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 17));
                    mMap.addMarker(new MarkerOptions().position(mDefaultLocation).title("MCD LAMBAK"));
                    Log.d(TAG, "Can not get your location");

                }
            }
        });
    }

    private void setUpLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){

            //requst runtime permission
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSION_REQUEST_CODE);

        }else if(checkPlayServices()){
            buildGoogleApiClient();
            createLocationRequest();
            displayLocation();

        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void startLocationOnUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);

        LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    // Set the map's camera position to the current location of the device.
                    mLastLocation = (Location) task.getResult();//(Location) task.getResult();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(mLastLocation.getLatitude(),
                                    mLastLocation.getLongitude()), 17));
                    //    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 17));
                    LatLng currentLoc = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(currentLoc).title("ME"));

                } else {
                    Log.d(TAG, "Current location is null. Using defaults.");
                    Log.e(TAG, "Exception: %s", task.getException());
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 17));
                    mMap.addMarker(new MarkerOptions().position(mDefaultLocation).title("MCD LAMBAK"));
                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();

        // Log and toast
        //to check when it is triggered
        Log.d("LOCATIONCAHGNED", "LOCATION CHANGED TO" + Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude()));
        Toast.makeText(MapsActivity.this,
                "LOCATION CHANGED TO" + Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude()),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationOnUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private void sendNotification(String title, String format) {
        Notification.Builder builder = new Notification.Builder(this, "default")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(format)
                .setPriority(Notification.PRIORITY_HIGH);

        NotificationManager mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "channel",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("CLOUD_MESSAGING_NOTIFICATION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MapsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults  |= Notification.DEFAULT_SOUND;

        mNotificationManager.notify(0,notification);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            namePlace = intent.getStringExtra("place");
            msgTitle = intent.getStringExtra("title");
            msgBody = intent.getStringExtra("body");

            //for testing purposes
            Log.d("SENDMESSAGETO: ", mood);
            Log.d("CHECK ISINRADIUUS: ", Boolean.toString(isInRadius));

            //Need to check if messages receive is null
            if(msgTitle == null){
                msgTitle = "FCM Message";
            }

            if(msgBody == null){
                msgBody = "No messages";
            }

            //loop through all name place to see if it matches
            if(isInRadius && namePlace.equals(mood)) {
                sendNotification(msgTitle, msgBody);

            }else{
                //if there is a messsage that is being received but it is not in a radius
                //that it can receieved then it will be put in here

                //max size is 5 //currently NOT properly working...why
                if(unsendMessagesList.size() >= 5){
                    Log.d("MSGSIZE: ", "unsendmsgsize is " + Integer.toString(unsendMessagesList.size()));
                    unsendMessagesList.remove(0);   //removes first element in the list. Should be FIFO
                    unsendMessagesList.add(new Messages(msgTitle,msgBody,namePlace));

                }else {
                    unsendMessagesList.add(new Messages(msgTitle, msgBody, namePlace));
                }

                checkUnsendMessages(mood);
            }

            Log.d(TAG, msgBody);
        }
    };


    //might need to add more parameter to fit in the set marker
    private void checkGeoQuery(GoogleMap googleMap){

        //checks when the device in radius of the given in location
        //PROBLEM: only check against the last set of the locationName
        GeoQueryEventListener listener = new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                LatLng device = new LatLng(location.latitude,location.longitude);

                for(int count = 0; count < locationNames.getSize(); count++){
                    String name = locationNames.getPlacesList().get(count).getName();
                    LatLng checkAgainst = locationNames.getLatLng(locationNames.getPlacesList().get(count).getName());
                    double distance = Physics.haversine(device, checkAgainst );

                    if(Physics.isInArea(0.1,(locationNames.getRadius(name)/1000), distance)) {
                        isInRadius = true;
                        Log.d("1. places NAME is in area: ", name);
                        Log.d("2. places RADIUS: ", Double.toString((locationNames.getRadius(name) / 1000)));
                        Log.d("3. places DISTANCE: ", Double.toString(distance));

                        mood = name;
                    }
               }
               checkUnsendMessages(mood);

                Log.d("LOCATIONCAHGNED", "LOCATION CHANGED TO" + Double.toString(location.latitude) + ", " + Double.toString(location.longitude));
                Toast.makeText(MapsActivity.this,
                        "LOCATION CHANGED TO" + Double.toString(location.latitude) + ", " + Double.toString(location.longitude),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onKeyExited(String key) {

                // Log and toast
                //to check when it is triggered
                Log.d("LOCATIONCAHGNED EXITED", "LOCATION EXIT");// + Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude()));
                Toast.makeText(MapsActivity.this, "LOCATION EXIT", Toast.LENGTH_SHORT).show();
                isInRadius = false;
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

                isInRadius = false;
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e("ERROR", "" + error);
            }
        };

        //Add Geoquery
        //convert meter in kilometer
        //example 1000 meter = 1.0km
        //check against locationNames instead of hardcoding it by looping through all the given names and latlng
        Log.d("CHECK LOCATION SIZE: ", Integer.toString(locationNames.getSize()));

        if(locationNames.getSize() > 0 ) {
            for (int count = 0; count < locationNames.getSize(); count++) {

                currentPlace = locationNames.getPlacesList().get(count).getName();
                LatLng placeslatlng = locationNames.getLatLng(currentPlace);
                double r = locationNames.getRadius(currentPlace);

                geoQueriesList.add(geoFire.queryAtLocation(new GeoLocation(placeslatlng.latitude, placeslatlng.longitude), (r/1000)));
                geoQueriesList.get(count).addGeoQueryEventListener(listener);

            }
        }
    }

    //when messages are not in the radius of the location it is intended to be send to
    //it is put in the list of unsend messages
    public void checkUnsendMessages(String current){
        if(unsendMessagesList.size() > 0 ){
            for(int count = 0; count < unsendMessagesList.size(); count++){
                //for testing purposes
                //in theory should work, but it doeSNT
                Log.d("UNSEND MESSAGE SIZE: ", Integer.toString(unsendMessagesList.size()) );

                //for testing purposes
                Log.d("UNSENT EQUALS: " , Boolean.toString(unsendMessagesList.get(count).getPlace().equals(current)));

                Log.d("UNSENT CURRENT LOCATION: ", current);

                Log.d("UNSENT TITLE: ", unsendMessagesList.get(count).getTitle());
                Log.d("UNSENT BODY: ", unsendMessagesList.get(count).getBody());
                Log.d("UNSENT PLACE: ", unsendMessagesList.get(count).getPlace());
                Log.d("=========================================: ", "=========================================: ");
                Log.d("=========================================: ", "=========================================: ");
                Log.d("=========================================: ", "=========================================: ");
                Log.d("=========================================: ", "=========================================: ");

                if(isInRadius && unsendMessagesList.get(count).getPlace().equals(current)){
                    //send notification to device
                    sendNotification(unsendMessagesList.get(count).getTitle(), unsendMessagesList.get(count).getBody());
                    unsendMessagesList.remove(count);

                }
            }

        }
    }

}
