package com.example.cloudmessagetutorial;

import com.google.android.gms.maps.model.LatLng;

public class Physics {

    public Physics(){}

    public double distance(LatLng obj1, LatLng obj2){
        double distance;

        distance = ((obj2.latitude-obj1.latitude)*(obj2.latitude-obj1.latitude))+ ((obj2.longitude-obj1.longitude)*(obj2.longitude-obj1.longitude));
        return distance*distance;

    }

    public boolean isInArea(LatLng obj1,LatLng obj2){

        return false;
    }

}
