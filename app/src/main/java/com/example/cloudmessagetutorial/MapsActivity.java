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

import com.google.firebase.database.DatabaseReference;

import java.util.regex.Pattern;


/*
 *
 * Where Map is being shown and shows device location
 * Calls the GeoService Service class to make map workable
 *
 * Author: Safwan Ahman
 *
 * */

public class MapsActivity extends FragmentActivity implements  OnMapReadyCallback
    {
    private GoogleMap mMap;

    private static final int MY_PERMISSION_REQUEST_CODE = 2019;
    private static final int PLAY_SERVCE_RES_REQUEST = 1995;

    private GeoService geoService = new GeoService();
    private boolean serviceBound;

    private String msgTitle;
    private String msgBody;
    private String namePlace;

    private EditText editText;
    private boolean matches;
    private String split[];

    private Marker mCurrent;
    private DatabaseReference ref;
    private final String TAG = "VB LOG TEXT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("IntentKey"));

        geoService.setMap(googleMap);
        geoService.setContext(this);
        geoService.readDatabase();

        //Log.d("CHECK LOCATION SIZE: ", Integer.toString(geoService.getLocationNames().getSize()));

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
               geoService.sendNotification("SERVICE STOPPED", "SERVICE HAS STOPPED");

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
                msgTitle = "FCM Message";
            }

            if(msgBody == null){
                msgBody = "No messages";
            }

            //loop through all name place to see if it matches
            if(geoService.getIsInRadius() && namePlace.equals(geoService.getCurrentLocationName()) && geoService.isServiceRunning()) {
                geoService.sendNotification(msgTitle, msgBody);

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

            for(int i = 0; i < geoService.getUnsendMessagesList().size(); i = i + 2){
                Log.d("GEOUNSEND", Integer.toString(i));
                Log.d("GEOUNSEND", Integer.toString(i) + ". " + geoService.getUnsendMessagesList().get(i).getTitle());
                Log.d("GEOUNSEND", Integer.toString(i) + ". " + geoService.getUnsendMessagesList().get(i).getPlace());
                Log.d("GEOUNSEND", Integer.toString(i) + ". " + geoService.getUnsendMessagesList().get(i).getBody());
            }

            Log.d("GEOUNSEND", "---=======================================----=======================---===================---");

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
        onMapReady(mMap);
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

            //this pattern is a regex for lat and lng
            matches = Pattern.matches("^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?)([\\s*]?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$"
                    , editText.getText().toString());

            if(matches) {
                popup2();
            }else
                Toast.makeText(this, "Value entered is not a LatLng, please enter correct LatLng", Toast.LENGTH_LONG).show();


        }

        private void popup2() {
            //splitting the given latlng into lat and lng
            split = editText.getText().toString().split(",");

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            Context context = this;
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);

            final EditText nameBox = new EditText(context);
            nameBox.setHint("Name");
            layout.addView(nameBox);

            final EditText latBox = new EditText(context);
            latBox.setHint("Latitude");
            layout.addView(latBox);


            final EditText  lngBox = new EditText(context);
            lngBox.setHint("Longitude");
            layout.addView(lngBox);

            final EditText radiusBox = new EditText(context);
            radiusBox.setHint("Radius");
            layout.addView(radiusBox);

            final EditText descriptionBox = new EditText(context);
            descriptionBox.setHint("Description");
            layout.addView(descriptionBox);

            alertDialogBuilder.setView(layout);

            latBox.setText(split[0]);
            lngBox.setText(split[1]);

            final LatLng latLng = new LatLng(Double.parseDouble(split[0]), Double.parseDouble(split[1]));


            alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MapsActivity.this, "Added: " +  latBox.getText(), Toast.LENGTH_LONG).show();

                    geoService.addGeofence(nameBox.getText().toString(),
                            latLng,
                            Double.parseDouble(radiusBox.getText().toString()),
                            mMap,
                            descriptionBox.getText().toString());

                    geoService.addToDB(nameBox.getText().toString(),
                            latLng,
                            Double.parseDouble(radiusBox.getText().toString()),
                            descriptionBox.getText().toString());

                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();

            alertDialog.show();

        }


    }
