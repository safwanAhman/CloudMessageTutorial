package com.example.cloudmessagetutorial;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
* This class is used to set location and latlng
* to be used in MapActivity class
*
*
* Should add database to store the location of place
*
*
* Should include RADIUS for the point of location
*
* */
public class LocationNames {

    private List<Places> placesList = new ArrayList<>();
    private Set<Places> placesSet = new HashSet<>();

    private LatLng defaultLatLng;
    private double radius = 50; //50 is the default set radius

    private int size = 0;

    private GoogleMap mMap;
    private String defaultPlace;

    private boolean duplicate = false;

    public LocationNames(){}

    //add to the list of places
    //error check with same name
    //if have the same name then make it not work
    public void setPlaces(String name, LatLng latLng, double radius, Context context, GoogleMap googleMap, String text ){

        //checks in the list of places already have the same name
        //if they do, do not add it into the list
        //else add to the list

        if(placesSet.add(new Places(name, latLng,radius, text))){
            addMarkerToMapTEST(name, latLng, radius, context, googleMap, text);
            placesList.add(new Places(name, latLng,radius, text));
            size++;
            Log.d("ADDED: ", name + " " + size);
        }


        /**
        if(placesList.size() >0 ){
            for(int count = 0; count < placesList.size(); count++){
                if(placesList.get(count).getName().equals(name)){
                   return;
                }
            }

            placesList.add(new Places(name, latLng,radius, text));
            addMarkerToMapTEST(name, latLng, radius, context, googleMap, text);
            Log.d("ADDED: ", name);

        }else if(placesList.size() <= 0){
            placesList.add(new Places(name, latLng,radius, text));
            addMarkerToMapTEST(name, latLng, radius, context, googleMap, text);
            Log.d("ADDED: ", name);

        }**/

        mMap = googleMap;

    }

    //search list of the name and return the latlng
    //not sure if it works, have to check
    //next step is to connect to Firebase Database
    //should retrieve stuff from Firebase database as well as able to add to it too

    public LatLng getLatLng(String name){

        defaultLatLng = new LatLng(0,0);

        if(placesList.size() >0){
            for(int count = 0; count < placesList.size(); count++){
                if(placesList.get(count).getName().equals(name)){
                    defaultLatLng = placesList.get(count).getLatLng();
                }
            }

        }
        return  defaultLatLng;
    }

    //search list of the latlng and return the name
    //not sure if it works, have to check
    public String getPlace(LatLng latLng){

        defaultPlace = "Nowehere";

        if(placesList.size() > 0){
            for(int count=0; count < placesList.size(); count++){
                if(placesList.get(count).getLatLng().equals(latLng)){
                    defaultPlace = placesList.get(count).getName();
                }
            }
        }
        return  defaultPlace;
    }


    public int getSize(){
        if(placesList == null){
            return 0;
        }else
            return placesList.size();
    }


    public double getRadius(String name){
        radius = 50;

        if(placesList.size() > 0){
            for(int count=0; count < placesList.size(); count++){
                if(placesList.get(count).getName().equals(name)){
                    radius = placesList.get(count).getRadius();
                }
            }
        }

        return radius;
    }

    public String getDescription(String name){
        String des = "";

        if(placesList.size() > 0){
            for(int count=0; count < placesList.size(); count++){
                if(placesList.get(count).getName().equals(name)){
                    des = placesList.get(count).getText();
                }
            }
        }

        return des;

    }

    public List<Places> getPlacesList(){
        return placesList;
    }

    public void clear() {

      placesList.clear();
      mMap.clear();


    }


    public void addMarkerToMapTEST(String name, LatLng latLng, double radius, Context context, GoogleMap googleMap, String text){

        MarkerOptions mm = new MarkerOptions()
                .position(latLng)
                .title(name)
                .snippet(text)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        CircleOptions cc = new CircleOptions()
                .center(latLng)
                .radius(radius)//is in meter
                .strokeColor(Color.BLUE)
                .fillColor(0x222000FF)
                .strokeWidth(5.0f);

        Marker marker = googleMap.addMarker(mm);
        Circle circle = googleMap.addCircle(cc);

        if(placesList.size() > 0){
            for(int count=0; count < placesList.size(); count++){
                if(placesList.get(count).getName().equals(name)){
                    placesList.get(count).setMarker(marker);
                    placesList.get(count).setCircle(circle);

                }
            }
        }

    }

    public void clearMarker(){

        if(mMap != null){
          mMap.clear();
        }
    }

    public void removeLocation(String name){
        if(placesList.size() > 0){
            for(int count=0; count < placesList.size(); count++){
                if(placesList.get(count).getName().equals(name)){
                    Log.d("NAME CIRCLE ", "RETURNING");

                    if(getMarker(name) != null) {
                        placesList.get(count).deleteMarker();

                    }

                    if(getCircle(name) != null){
                        placesList.get(count).deleteCircle();

                    }
                    placesList.remove(count);
                }
            }
        }
    }

    public Circle getCircle(String name){

        Circle c = null;

        if(placesList.size() > 0){
            for(int count=0; count < placesList.size(); count++){
                if(placesList.get(count).getName().equals(name)){
                    Log.d("CIRCLE ", "RETURNING");

                    c =  placesList.get(count).getCircle();
                }
            }
        }

        return c;
    }

    public Marker getMarker(String name){
        Marker m = null;

        if(placesList.size() > 0){
            for(int count=0; count < placesList.size(); count++){
                if(placesList.get(count).getName().equals(name)){

                    Log.d("MARKER AND CIRCLE ", "RETURNING");
                    m =  placesList.get(count).getMarker();
                }
            }
        }

        return m;
    }
}
