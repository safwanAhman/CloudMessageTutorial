package com.example.cloudmessagetutorial;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

public class Places {

    public Places(){}
    public Places(String name, LatLng latLng, double radius){
        this.name = name;
        this.latLng = latLng;
        this.radius = radius;
    }


    public Places(String name, LatLng latLng, double radius, String text){
        this.name = name;
        this.latLng = latLng;
        this.radius = radius;
        this.text = text;
    }

    private Map<String, LatLng> places = new HashMap<>();
    private String name;
    private LatLng latLng;
    private double radius;
    private String text;

    private Marker marker;
    private Circle circle;

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

    public String getText() {
        return text;
    }

    public void setMarker(Marker m){
        marker = m;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setCircle(Circle c){
        circle = c;
    }

    public Circle getCircle(){
        return circle;
    }

    public void deleteCircle(){
        circle.remove();
        circle = null;
    }

    public void deleteMarker(){
        marker.remove();
        marker = null;
    }
}
