package com.example.cloudmessagetutorial;

import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    private Map<String, LatLng> places = new HashMap<>();
    private List<Places> placesList = new ArrayList<>();
    private LatLng defaultLatLng;
    private double radius = 50; //50 is the default set radius
    private Marker myMarker;

    private String defaultPlace;

    public LocationNames(){}

    //add to the list of places
    //error check with same name
    //if have the same name then make it not work
    public void setPlaces(String name, LatLng latLng, double radius, Context context){


        if(placesList.size() >0){
            for(int count = 0; count < placesList.size(); count++){
                if(placesList.get(count).getName().equals(name)){
                    Toast.makeText(context, "Name of places taken", Toast.LENGTH_LONG).show();
                }
            }
        }else
            placesList.add(new Places(name, latLng,radius));


        //checks in the list of places already have the same name
        //if they do, do not add it into the list
        //else add to the list
        if(places.size() > 0){
            for(Map.Entry<String, LatLng> entry: places.entrySet()){
                if(name.equals(entry.getKey())){
                    Toast.makeText(context, "Name of places taken", Toast.LENGTH_LONG).show();
                }
            }
        }else
            places.put(name,latLng);
    }


    //REMINDER: radius is in meter
    public void setRadius(double r){
        radius = r;
    }


    //search list of the name and return the latlng
    //not sure if it works, have to check
    //next step is to connect to Firebase Database
    //should retrieve stuff from Firebase database as well as able to add to it too

    public LatLng getLatLng(String name){

        defaultLatLng = new LatLng(0,0);

        if(places.size() > 0){
            for(Map.Entry<String, LatLng> entry: places.entrySet()){
                if(name.equals(entry.getKey())){
                    defaultLatLng = entry.getValue();
                }
            }
        }
        return  defaultLatLng;
    }

    //search list of the latlng and return the name
    //not sure if it works, have to check
    public String getPlace(LatLng latLng){

        defaultPlace = "Nowehere";

        if(places.size() > 0){
            for(Map.Entry<String, LatLng> entry: places.entrySet()){
                if(latLng.equals(entry.getValue())){
                    defaultPlace = entry.getKey();
                }
            }
        }
        return  defaultPlace;
    }

    public void setMarker(GoogleMap googleMap, LatLng location, String name, String text){
        googleMap.addMarker(new MarkerOptions()
                .position(location)
                .title(name)
                .snippet(text)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        googleMap.addCircle(new CircleOptions()
                .center(location)
                .radius(radius)//is in meter
                .strokeColor(Color.BLUE)
                .fillColor(0x222000FF)
                .strokeWidth(5.0f));
    }

    public int getSize(){
        if(places == null){
            return 0;
        }else
            return places.size();
    }

    public Map<String, LatLng> getPlaces(){
        return places;
    }

    public double getRadius(){
        return radius;
    }
}
