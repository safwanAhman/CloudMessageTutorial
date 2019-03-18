package com.example.cloudmessagetutorial;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;


/*
*
*
* Where the background service for the MapActivity mainly works
* Here is where the notifications are being sent
* Also where GeoQuery is being handled and also updates the current location to the firebase database
*
*
* Author: Safwan Ahman
*
* */
public class GeoService extends Service implements GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final int NOTIFICATION_ID = 1;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationChangeListener mLocationChangeListener;

    private boolean isInRadius = false;

    private static int UPDATE_INTERVAL = 2000;
    private static int FASTEST_INTERVAL = 1000;
    private static int DISPLACEMENT = 10;

    int count=0;   //used for testing, to see how many times the service is being run

    private String currentLocationName = "";

    private DatabaseReference ref;

    private final String TAG = "GEO_SERVICE_TAG: ";
    private GeoFire geoFire;

    private List<Messages> unsendMessagesList = new ArrayList<>();

    private List<GeoQuery> geoQueriesList;// = new ArrayList<>();

    private List<LocationNames> locationTest = new ArrayList<>();


    private boolean serviceRunning = false;

    private final IBinder serviceBinder = new RunServiceBinder();

    public boolean isServiceRunning() {
        return serviceRunning;
    }

    public class RunServiceBinder extends Binder {
        GeoService getService(){
            return GeoService.this;
        }
    }

    public GeoService() {
    }

    @Override
    public void onCreate() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Creating service");
        }

        //Intialise Firebase so it can be updated to the database in firebase
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
                        //
                        //Toast.makeText(MapsActivity.class, msg, Toast.LENGTH_SHORT).show();
                    }
                });


        //subscribe to a topic, in this case it is Virtual Brunei
         FirebaseMessaging.getInstance().subscribeToTopic("VirtualBrunei");

         //give update on live location.
        ref = FirebaseDatabase.getInstance().getReference("MyLocation");

        /**
        //testing for inserting location name
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference l = databaseReference.child("location/");
        DatabaseReference ll = l.child("Index");
        final DatabaseReference name = ll.child("Location Name");
        final DatabaseReference lat = ll.child("Location Lat");
        final DatabaseReference lng = ll.child("Location Lng");

        lat.setValue(4.965173);
        lng.setValue(114.951696);

        name.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name.setValue("House");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        lat.setValue(4.899541);
        lng.setValue(114.849591);
            **/
         Log.d(TAG, "Subscribed to Virtual Brunei topic");
         geoFire = new GeoFire(ref);
         geoQueriesList = new ArrayList<>();
         serviceRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");

        if(Log.isLoggable(TAG, Log.VERBOSE)){
            Log.v(TAG, "Binding Service");
        }

        return serviceBinder;
    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        displayLocation();

        if(mLastLocation != location) {

            Log.d("LOCATIONCAHGNED", "LOCATION CHANGED TO" + Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude()));
            Toast.makeText(GeoService.this,
                    "MOVING: " + Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude()),
                    Toast.LENGTH_SHORT).show();

        }


       // GeoLocation l = new GeoLocation(location.getLatitude(), location.getLongitude());

        //new function hopefully updates to trigger the geoqueries
        for(int i = 0; i < geoQueriesList.size(); i++){
            geoQueriesList.get(i).setCenter(geoQueriesList.get(i).getCenter());
          //   geoQueriesList.get(i).setCenter(l);

        }

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
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //using start_sticky so it can restart where it left off
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(Log.isLoggable(TAG, Log.VERBOSE)){
            Log.v(TAG, "Starting service");
        }
        return Service.START_STICKY;
    }

    public void stopService(){
        if(serviceRunning){
            serviceRunning = false;

            if(geoQueriesList != null){
                for(int i = 0; i < geoQueriesList.size(); i++){
                    geoQueriesList.get(i).removeAllListeners();
                }
            }
        }else{
            Log.e(TAG, "STOP TIMER FOR A TIMER THAT IS NOT RUNNING");
        }
    }


    public void foreground(){
        startForeground(NOTIFICATION_ID,createNotification());
    }

    public void background(){
        stopForeground(true);
    }

    //used in mapsactivity classes
    //used to handle geoquery and also handles location change
    //also starts the timer
    public void startService(final LocationNames locationNames,final Context context){

        if (!serviceRunning) {
            serviceRunning = true;

        } else {
            Log.e(TAG, "startService request for an already running Service");

        }

        //handling events when device enters a location
        GeoQueryEventListener listener = new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                isInRadius = true;

                LatLng device = new LatLng(location.latitude, location.longitude);

                for(int i= 0; i < locationNames.getSize(); i++){
                    String me = locationNames.getPlacesList().get(i).getName();
                    LatLng checkAgainst = locationNames.getLatLng(locationNames.getPlacesList().get(i).getName());
                    double distance = Physics.haversine(device, checkAgainst);

                    //assuming the device's radius is 1km
                    if(Physics.isInArea(0.1, (locationNames.getRadius(me)/1000), distance)){
                        Log.d("1. places NAME is in area: ", me);
                        Log.d("2. places RADIUS: ", Double.toString((locationNames.getRadius(me) / 1000)));
                        Log.d("3. places DISTANCE: ", Double.toString(distance));

                        currentLocationName = me;
                        checkUnsendMessages(currentLocationName);

                    }

                }

                checkUnsendMessages(currentLocationName);

              //  Log.d("LOCATIONENETER", "LOCATION CHANGED TO" + Double.toString(location.latitude) + ", " + Double.toString(location.longitude));
                Toast.makeText(context , "LOCATION ENTERED: " + currentLocationName, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onKeyExited(String key) {
                // Log and toast
                //to check when it is triggered
                Toast.makeText(context, "LOCATION EXIT", Toast.LENGTH_SHORT).show();
                sendNotification("EXITED", "YOU HAVE EXITED A LOCATION");
                isInRadius = false;
                currentLocationName = "";

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

                Toast.makeText(context ,
                        "DEVICE IS MOVING LOCATION: " + Double.toString(location.latitude) + ", " + Double.toString(location.longitude),
                        Toast.LENGTH_SHORT).show();

                LatLng device = new LatLng(location.latitude, location.longitude);

                for(int i= 0; i < locationNames.getSize(); i++){
                    String me = locationNames.getPlacesList().get(i).getName();
                    LatLng checkAgainst = locationNames.getLatLng(locationNames.getPlacesList().get(i).getName());
                    double distance = Physics.haversine(device, checkAgainst);

                    //assuming the device's radius is 1km
                    if(Physics.isInArea(0.1, (locationNames.getRadius(me)/1000), distance)){
                        Log.d("1. places NAME is in area: ", me);
                        Log.d("2. places RADIUS: ", Double.toString((locationNames.getRadius(me) / 1000)));
                        Log.d("3. places DISTANCE: ", Double.toString(distance));

                        Toast.makeText(context, "LOCATION HAS MOVED TO" + currentLocationName, Toast.LENGTH_SHORT).show();

                        //radius is only true here when it is in the area
                        isInRadius = true;
                        currentLocationName = me;
                        checkUnsendMessages(currentLocationName);

                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.d("ERROR", "" + error);
            }
        };



        if(!serviceRunning){
            serviceRunning = true;
        }else{
            Log.e(TAG , "startService request for an already running Service");
        }

        if(geoQueriesList !=null){
            for(int i=0; i < geoQueriesList.size(); i++){
                geoQueriesList.get(i).removeAllListeners();
            }
        }

        if(locationNames != null ){
            for(int i = 0; i < locationNames.getSize(); i++){
                String current = locationNames.getPlacesList().get(i).getName();
                LatLng latLng = locationNames.getLatLng(current);
                double r = locationNames.getRadius(current);

                //need to check for the geoquerieslist.size < locationnames.getsize
                if(geoQueriesList != null && geoQueriesList.size() < locationNames.getSize()) {
                    geoQueriesList.add(geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), (r / 1000)));

                    geoQueriesList.get(i).addGeoQueryEventListener(listener);

                    /**
                    Log.d("GEOQUERIES: ", Integer.toString(geoQueriesList.size()));
                     Toast.makeText(GeoService.this,
                             "GEOQUERY LOCATON AT " + i  + ":  "+ Double.toString(geoQueriesList.get(i).getCenter().latitude) + ", " + Double.toString(geoQueriesList.get(i).getCenter().longitude),
                             Toast.LENGTH_SHORT).show();**/
                }
            }
        }

        count++;
        Log.d("RUNNING: ", "SERVICE IS RUNNING THIS MANY TIMES " + Integer.toString(count) );

        checkUnsendMessages(currentLocationName);
    }

    //notification showing that service is currently active
    private Notification createNotification(){
        Notification.Builder builder = new Notification.Builder(this, "default")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Active Service")
                .setContentText("Tap to return to map")
                .setPriority(Notification.PRIORITY_HIGH);

        Intent resultIntent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        return builder.build(); //xoxo gossip girl

    }

    //FCM messages are to be sent as a notification whenreceived under certain conditions
    //should implement TYPE of notification once geoquery wors properly
    public void sendNotification(String title, String format) {
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

        Intent intent = new Intent(this, MapsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults  |= Notification.DEFAULT_SOUND;

        mNotificationManager.notify(0,notification);
    }

    //when messages are not in the radius of the location it is intended to be send to
    //it is put in the list of unsend messages
    //handles unsent messages
    public void checkUnsendMessages(String current){
        if(unsendMessagesList.size() > 0 ){
            for(int count = 0; count < unsendMessagesList.size(); count++){
                //for testing purposes
                //in theory should work, but it doeSNT
                Log.d("UNSEND MESSAGE SIZE: ", Integer.toString(unsendMessagesList.size()) );

                //for testing purposes
                Log.d("UNSENT EQUALS: " , Boolean.toString(unsendMessagesList.get(count).getPlace().equals(current)));

                Log.d("UNSENT CURRENT LOCATION: ", current);

                Log.d("UNSENT COUNT: ", Integer.toString(count) );


                Log.d("UNSENT TITLE: ", unsendMessagesList.get(count).getTitle());
                Log.d("UNSENT BODY: ", unsendMessagesList.get(count).getBody());
                Log.d("UNSENT PLACE: ", unsendMessagesList.get(count).getPlace());
                Log.d("=========================================: ", "=========================================: ");
                Log.d("=========================================: ", "=========================================: ");
                Log.d("=========================================: ", "=========================================: ");
                Log.d("=========================================: ", "=========================================: ");

                if(isInRadius && unsendMessagesList.get(count).getPlace().equals(current)){
                    sendNotification(unsendMessagesList.get(count).getTitle(), unsendMessagesList.get(count).getBody());
                    //send notification to device
                    unsendMessagesList.remove(count);

                }
            }

        }
    }

    public List<Messages> getUnsendMessagesList(){
        return unsendMessagesList;
    }

    private void startLocationOnUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback(){
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    interface LocationChangeListener {
        void onLocationChange(Location location);
    }

    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    public void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    public void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mLastLocation = location;

                geoFire.setLocation("You", new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if(mLocationChangeListener != null){

                            Log.d("SUCSESSFULLY WRITTEN IN FIREBASE" ,  Double.toString(mLastLocation.getLatitude()) + " , " + Double.toString(mLastLocation.getLongitude()));

                            mLocationChangeListener.onLocationChange(mLastLocation);
                        }
                    }
                });

                if(mLastLocation != null) {

                    final double latitude = mLastLocation.getLatitude();
                    final double longitude = mLastLocation.getLongitude();

                    Log.d(TAG, String.format("Your last location was changed: %f / %f", latitude, longitude));
                }else
                    Log.d(TAG, "No last location");

            }
        });
    }

    public void setLocationChangeListener(LocationChangeListener mLocationChangeListener){
        this.mLocationChangeListener = mLocationChangeListener;
    }

    public boolean getIsInRadius(){
        Log.d("CHECK ISINRADIUUS SERVICE: ", Boolean.toString(isInRadius));

        return isInRadius;
    }

    public String getCurrentLocationName(){
        return currentLocationName;
    }

    private void addToDB(String id, String name){

    }
}
