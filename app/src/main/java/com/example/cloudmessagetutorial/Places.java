package com.example.cloudmessagetutorial;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

public class Places {

    public Places(){}
    public Places(String name, LatLng latLng, double radius){
        this.name = name;
        this.latLng = latLng;
        this.radius = radius;
    }

    private Map<String, LatLng> places = new HashMap<>();
    private String name;
    private LatLng latLng;
    private double radius;


    public void setName(String s){
        name = s;
    }

    public String getName(){
        return name;
    }

    public void setLatLng(LatLng l){
        latLng = l;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setRadius(double r){
        radius = r;
    }

    public double getRadius() {
        return radius;
    }
}
