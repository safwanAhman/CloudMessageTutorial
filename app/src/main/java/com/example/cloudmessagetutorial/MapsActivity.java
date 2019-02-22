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

        checkGeoQuery(mMap);

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

        locationNames.setPlaces("MCD", mDefaultLocation, 700,this, googleMap,"lets eat here");
        locationNames.setPlaces("673 Jerudong", crossfit,70, this, googleMap, "I gym here");
        locationNames.setPlaces("House", house, 70,this, googleMap,"my house");
        locationNames.setPlaces("Giant", giant, 300,this,googleMap,"We are meeting at Jolibee");
        locationNames.setPlaces("Pizza", pizza, 400,this,googleMap,"Lets eat Pizza");
        locationNames.setPlaces("Haz", haz, 300,this,googleMap,"My second gym is here");
        locationNames.setPlaces("KIA", kia, 400,this, googleMap,"Car broom broom");
        locationNames.setPlaces("HuaHo Manggis", huahomanggis, 70,this, googleMap,"buy BB's watch pls");


        Log.d("CHECK LOCATION SIZE: ", Integer.toString(locationNames.getSize()));

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
                                    //add marker
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

    public boolean getIsInRadius(){
        return  isInRadius;

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
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

            Log.d("UNSENT CURRENTPLACE: ", currentPlace);


            //Need to check if messages receive is null
            if(msgTitle == null){
                msgTitle = "FCM Message";
            }

            if(msgBody == null){
                msgBody = "No messages";
            }

            //loop through all name place to see if it matches
            if(isInRadius && namePlace.equals(currentPlace)) {
                sendNotification(msgTitle, msgBody);
                unsendMessagesList.add(new Messages(msgTitle,msgBody,namePlace));

                Log.d("SIZE OF UNSEND MESSAGES", Integer.toString(unsendMessagesList.size()));

            }else{
                //if there is a messsage that is being received but it is not in a radius
                //that it can receieved then it will be put in here
                unsendMessagesList.add(new Messages(msgTitle,msgBody,namePlace));
            }


            //just to check if messages is being added to the list or not
            if(unsendMessagesList.size() > 0 ) {
                for (int count = 0; count < unsendMessagesList.size(); count++) {
                    if (unsendMessagesList.get(count).getPlace().equals(currentPlace)) {

                        Log.d("UNSENT EQUALS: ", Boolean.toString(unsendMessagesList.get(count).getPlace().equals(namePlace)));

                        Log.d("UNSENT NAMEPLACE: ", namePlace);
                        Log.d("UNSENT CURRENTPLACE: ", currentPlace);

                        //send notification to device
                        Log.d("UNSENT TITLE: ", unsendMessagesList.get(count).getTitle());
                        Log.d("UNSENT BODY: ", unsendMessagesList.get(count).getBody());
                        Log.d("UNSENT PLACE: ", unsendMessagesList.get(count).getPlace());

                        sendNotification(unsendMessagesList.get(count).getTitle(), unsendMessagesList.get(count).getBody());

                    }
                }
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
                isInRadius = true;
                Log.d("places : ", currentPlace);

                //check for any unsend messages when it enters an area
                checkUnsendMessages(currentPlace);
            }

            @Override
            public void onKeyExited(String key) {

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

        GeoQuery geoQuery = null;// =  geoFire.queryAtLocation(new GeoLocation(mDefaultLocation.latitude,mDefaultLocation.longitude), (10));

        if(locationNames.getSize() > 0 ) {
            for (int count = 0; count < locationNames.getSize(); count++) {

                currentPlace = locationNames.getPlacesList().get(count).getName();
                LatLng placeslatlng = locationNames.getLatLng(currentPlace);
                double r = locationNames.getRadius(currentPlace);

                Log.d("GEOQUERY CHECK LOCATION: ", currentPlace);
                Log.d("GEOQUERY CHECK LAT: ", Double.toString(placeslatlng.latitude));
                Log.d("GEOQUERY CHECK LNG: ", Double.toString(placeslatlng.longitude));

                //show all entry of the map data set
                //geoQuery = geoFire.queryAtLocation(new GeoLocation(placeslatlng.latitude, placeslatlng.longitude), (r/1000));
//                geoQuery.addGeoQueryEventListener(listener);

                geoQueriesList.add(geoFire.queryAtLocation(new GeoLocation(placeslatlng.latitude, placeslatlng.longitude), (r/1000)));
                geoQueriesList.get(count).addGeoQueryEventListener(listener);

               // Log.d("GEOBOOL CHECK: ",  Double.toString(geoQuery.getRadius()));

            }
        }

    }

    //when messages are not in the radius of the location it is intended to be send to
    //it is put in the list of unsend messages
    public void checkUnsendMessages(String current){
        if(unsendMessagesList.size() > 0 ){
            for(int count = 0; count < unsendMessagesList.size(); count++){
                if(isInRadius && unsendMessagesList.get(count).getPlace().equals(current)){

                    Log.d("UNSENT EQUALS: " , Boolean.toString(unsendMessagesList.get(count).getPlace().equals(current)));

                    Log.d("UNSENT NAMEPLACE: ", current);

                    Log.d("UNSENT TITLE: ", unsendMessagesList.get(count).getTitle());
                    Log.d("UNSENT BODY: ", unsendMessagesList.get(count).getBody());
                    Log.d("UNSENT PLACE: ", unsendMessagesList.get(count).getPlace());

                    //send notification to device
                    sendNotification(unsendMessagesList.get(count).getTitle(), unsendMessagesList.get(count).getBody());
                    unsendMessagesList.remove(count);

                }
            }

        }
    }

}
