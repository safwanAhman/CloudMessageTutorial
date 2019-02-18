package com.example.cloudmessagetutorial;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

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
    private List<Map> listPlaces = new ArrayList<>();
    private LatLng defaultLatLng;
    private double radius;

    private String defaultPlace;


    public LocationNames(){}

    //add to the list of places
    //error check with same name
    //if have the same name then make it not work
    public void setPlaces(String name, LatLng latLng, Context context){

        //checks in the list of places already have the same name
        //if they do, do not add it into the list
        //else add to the list
        if(listPlaces.size() > 0){
            for(int count = 0; count < listPlaces.size(); count++){
                if(listPlaces.get(count).containsKey(name)){
                    Toast.makeText(context, "Name of places taken", Toast.LENGTH_LONG).show();
                }else{
                    places.put(name,latLng);
                    listPlaces.add(places);
                }
            }

        }

    }

    //search array list of the name and return the latlng
    //not sure if it works, have to check
    public LatLng getLatLng(String name){

        defaultLatLng = new LatLng(0,0);

        if(listPlaces.size() > 0){
            for(int count=0; count <listPlaces.size(); count++){
                if (listPlaces.get(count).containsKey(name))
                    defaultLatLng = places.get(name);
            }
        }else
            return defaultLatLng;

        return  defaultLatLng;
    }

    /**public String getPlace(LatLng latLng){

        defaultPlace = "Nowehere";

        if(listPlaces.size() > 0){
            for(int count = 0; count< listPlaces.size(); count++){
                if(listPlaces.get(count).containsValue(latLng))
                    defaultPlace = listPlaces.get(count).;

            }

        }

    }**/

}
