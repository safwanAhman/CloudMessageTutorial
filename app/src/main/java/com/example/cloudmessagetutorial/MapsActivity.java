package com.example.cloudmessagetutorial;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.regex.Pattern;


/*
 *
 * Where Map is being shown and shows device location
 * Calls the GeoService Service class to make map workable
 *
 * Author: Safwan Ahman
 *
 * */

public class MapsActivity extends FragmentActivity implements  OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener
{
    private GoogleMap mMap;

    private static final int MY_PERMISSION_REQUEST_CODE = 2019;
    private static final int PLAY_SERVCE_RES_REQUEST = 1995;

    private GeoService geoService; // = new GeoService();
    private boolean serviceBound;

    private String msgTitle;
    private String msgBody;
    private String namePlace;

    private EditText editText;
    private String split[];

    private Marker mCurrent;
    private final String TAG = "VB LOG TEXT";

    boolean added = false;
    private SendNotification sendNotification = new SendNotification(this);

    private String defaultparmeter = "";

    private UserInterface userInterface = new UserInterface();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geoService = new GeoService();
    }


    @Override
    public void onPause(){
        super.onPause();
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
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);

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
        LatLng ubd = new LatLng(4.972740, 114.893977);
        LatLng airport = new LatLng(4.943832, 114.931770);
        LatLng mcd = new LatLng(4.965173, 114.951696);


        geoService.addGeofence("MCD", mcd,  800, googleMap,"lets eat here");
        geoService.addGeofence("673 Jerudong", crossfit,70, googleMap, "I gym here");
        geoService.addGeofence("House", house, 70, googleMap,"my house");
        geoService.addGeofence("Giant", giant, 300,googleMap,"We are meeting at Jolibee");
        geoService.addGeofence("Pizza", pizza, 400,googleMap,"Lets eat Pizza");
        geoService.addGeofence("Haz", haz, 300,googleMap,"My second gym is here");
        geoService.addGeofence("KIA", kia, 400, googleMap,"Car broom broom");
        geoService.addGeofence("HuaHo Manggis", huahomanggis, 70, googleMap,"buy BB's watch pls");
        geoService.addGeofence("Relentless", relentless, 600,  googleMap, "dance dance dannce");
        geoService.addGeofence("Ubd", ubd, 800, googleMap, "University Brunei Darussalam");
        geoService.addGeofence("Airport", airport, 800,  googleMap, "Brunei International Airport");


        geoService.setMap(googleMap);
        geoService.setContext(this);
        geoService.readDatabase();

        Button button2 = (Button) findViewById(R.id.startbutton);
        button2.setEnabled(false);

        Button button1 = (Button) findViewById(R.id.stopbutton);
        button1.setEnabled(true);

        //only place where it should be
        geoService.startService(MapsActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults){
        switch(requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(checkPlayServices()) {
                        geoService.buildGoogleApiClient();
                        geoService.createLocationRequest();
                        geoService.displayLocation();
                        geoService.setLocationChangeListener(new GeoService.LocationChangeListener() {
                            @Override
                            public void onLocationChange(Location location) {
                                if(mCurrent != null){
                                    mCurrent.remove();
                                }
                                mCurrent = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                        .title("You"));
                                LatLng coord = new LatLng(location.getLatitude(), location.getLongitude());

                                CameraUpdate camLocation = CameraUpdateFactory.newLatLngZoom(coord, 15);
                                mMap.animateCamera(camLocation);
                            }
                        });
                    }
                }break;
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Starting and binding service");
        }
        Intent i = new Intent(this, GeoService.class);
        startService(i);
        bindService(i, mConnection, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("onSTOP", Boolean.toString(geoService.isServiceRunning()));

        if (serviceBound) {
            // If a timer is active, foreground the service, otherwise kill the service
            if (geoService.isServiceRunning()) {
               geoService.foreground();

            } else {
               Log.d("SERVICESTOPPED", "Service has stopped. Oof ");
               sendNotification.sendNotification("SERVICE STOPPED", "SERVICE HAS STOPPED");

                //so if service stops, start service again
                //need to double check on the function call on this one
               stopService(new Intent(this, GeoService.class));

                Log.d("SERVICESTOPPED", "SERVICE IS RESTARTING ");

            }
        }

        // Unbind the service
        unbindService(mConnection);
        serviceBound = false;
    }

    protected void onDestroy(){
        super.onDestroy();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            geoService.stopService();//true will remove notification
        }

    }

    /**
     * Callback for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Service bound");
            }
            GeoService.RunServiceBinder binder = (GeoService.RunServiceBinder) service;
            geoService = binder.getService();
            serviceBound = true;
            // Ensure the service is not in the foreground when bound
            geoService.background();
            setUpdateLocation();
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(MapsActivity.this);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Service disconnect");
            }
            serviceBound = false;

            Log.d("DISCONNECT", "Service disconnect");
        }
    };


    public boolean checkPlayServices(){
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();

        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS){
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result, PLAY_SERVCE_RES_REQUEST).show();
            }else{
                Toast.makeText(this, "This device is not supported",Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    private void setUpdateLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){

            //requst runtime permission
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSION_REQUEST_CODE);

        }else if(checkPlayServices()){
            geoService.buildGoogleApiClient();
            geoService.createLocationRequest();
            geoService.displayLocation();

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setAllGesturesEnabled(true);

            geoService.setLocationChangeListener(new GeoService.LocationChangeListener() {
                @Override
                public void onLocationChange(Location location) {
                    if(mCurrent != null){
                        mCurrent.remove();
                    }
                    mCurrent = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title("You"));
                    LatLng coord = new LatLng(location.getLatitude(), location.getLongitude());

                    Log.d("ARIANAGRANDE" , Double.toString(coord.latitude)  + " , " + Double.toString(coord.longitude));
                    Log.d("CURRENTLOCATION" , geoService.getCurrentLocationName());

                    CameraUpdate camLocation = CameraUpdateFactory.newLatLngZoom(coord, 15);
                    mMap.animateCamera(camLocation);
                }
              });
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            namePlace = intent.getStringExtra("place");
            msgTitle = intent.getStringExtra("title");
            msgBody = intent.getStringExtra("body");

            //Need to check if messages receive is null
            if(msgTitle == null){
                msgTitle = "No FCM";
            }

            if(msgBody == null){
                msgBody = "No messages";
            }

            //loop through all name place to see if it matches
            if(geoService.getIsInRadius() && namePlace.equals(geoService.getCurrentLocationName()) && geoService.isServiceRunning()) {
                sendNotification.sendNotification(msgTitle, msgBody);

            }else{
                //if there is a messsage that is being received but it is not in a radius
                //that it can receieved then it will be put in here

                //always adding it TWO times. Reason: Unknown tbfh kmaksmdaslmdsa fml
                //maybe always receiving messages twice. Just add the first one instead. (the odd number always taken)
                if(geoService.getUnsendMessagesList().size() <= 20) {
                    geoService.getUnsendMessagesList().add(new Messages(msgTitle, msgBody, namePlace));
                    Log.d("MSGSIZE: ", "unsendmsgsize is " + Integer.toString(geoService.getUnsendMessagesList().size()));
                }
            }


            Log.d(TAG, msgBody);
        }
    };

    public void sendStop(View view){
        geoService.stopService();

        if(geoService.getLocationNames().getSize() > 0){
            geoService.getLocationNames().clear();
            geoService.getLocationNames().clearMarker();

        }

        Button button1 = (Button) findViewById(R.id.stopbutton);
        button1.setEnabled(false);

        Button button2 = (Button) findViewById(R.id.startbutton);
        button2.setEnabled(true);

        stopService(new Intent(this, GeoService.class));


    }

    public void sendStart(View view){
        //onMapReady(mMap);
        Button button = (Button) findViewById(R.id.stopbutton);
        button.setEnabled(true);

        Button button2 = (Button) findViewById(R.id.startbutton);
        button2.setEnabled(false);

        Intent i = new Intent(this, GeoService.class);
        startService(i);
        bindService(i, mConnection, 0);
        geoService.startService(this);

    }

    public void testButton(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void press(View view){
        editText = (EditText) findViewById(R.id.latlng);

        if(regex(editText.getText().toString())) {
            added=false;
            popup2();
        }else
            Toast.makeText(this, "Value entered is not a LatLng, please enter correct LatLng", Toast.LENGTH_LONG).show();
    }

    private void popup2(){
        popup2(defaultparmeter);
    }

    private void popup2(final String name) {
        userInterface.inputpop(added,editText,this,name,geoService,mMap);
    }

    //get latlng of tapped area
    @Override
    public void onMapClick(LatLng point){
        EditText e = (EditText) findViewById(R.id.latlng);

        DecimalFormat df = new DecimalFormat("#.#####");
        df.setRoundingMode(RoundingMode.CEILING);
        e.setText(df.format(point.latitude) + ", " +df.format(point.longitude) );

        Toast.makeText(this, "TAPPED " , Toast.LENGTH_SHORT).show();
    }

    //to edit a  hold down marker (For delete or edit)
    @Override
    public void onMapLongClick(LatLng latLng) {

    }

        //get current location and put it in the text box
    public void pressCurrent(View view){

        EditText e = findViewById(R.id.latlng);
        Double lat = mCurrent.getPosition().latitude;
        Double lng = mCurrent.getPosition().longitude;

        DecimalFormat df = new DecimalFormat("#.#####");
        df.setRoundingMode(RoundingMode.CEILING);

        e.setText(df.format(lat) + ", " + df.format(lng));
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        DecimalFormat df = new DecimalFormat("#.#####");
        df.setRoundingMode(RoundingMode.CEILING);

        editText = (EditText) findViewById(R.id.latlng);
        editText.setText(df.format(marker.getPosition().latitude) + ", " +df.format(marker.getPosition().longitude) );

        final String n = geoService.getLocationNames().getPlace(marker.getPosition());
        double la = marker.getPosition().latitude;
        double ln = marker.getPosition().longitude;

        Toast.makeText(this, "TAPPED " , Toast.LENGTH_SHORT).show();

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapsActivity.this);
        LinearLayout linearLayout = userInterface.pop(this, n, la, ln, geoService.getLocationNames().getRadius(n), geoService.getLocationNames().getDescription(n) );
        alertDialogBuilder.setView(linearLayout);

        alertDialogBuilder.setCancelable(true).setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(regex(editText.getText().toString())) {
                    added = true;
                    popup2(n);  //once it goes here....it never goes back lmaooooo ;_;

                }else
                    Toast.makeText(MapsActivity.this, "Value entered is not a LatLng, please enter correct LatLng", Toast.LENGTH_LONG).show();

            }
        });

        alertDialogBuilder.setCancelable(true).setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               Toast.makeText(MapsActivity.this, "DELETED: "+ n , Toast.LENGTH_LONG).show();

                geoService.deleteFromDB(n);
                //not sure if needed
                marker.remove();   //it works but hard remove circle
                SystemClock.sleep(1000);

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        return true;
    }

    private boolean regex(String s){

        if(Pattern.matches("^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?)([\\s*]?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$", s)){
            return true;
        }else
            return false;
    }


}
