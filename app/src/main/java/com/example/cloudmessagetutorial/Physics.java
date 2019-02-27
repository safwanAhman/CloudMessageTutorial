package com.example.cloudmessagetutorial;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;


//Currently not needed
public class Physics {

    public Physics(){}

    //distance -> d
    //d = (x2-x1)^2 + (y2-y1)^2 + (z2-z1)^2
    public static double distance(LatLng obj1, LatLng obj2){
        double distance;
        double xx = (obj2.latitude-obj1.latitude)*(obj2.latitude-obj1.latitude);
        double yy = (obj2.longitude-obj1.longitude)*(obj2.longitude-obj1.longitude);


        distance = (xx)+ (yy);
        Log.d("CHECK DISTANCE", Double.toString(distance*distance));
        return distance*distance;

    }


    public static double haversine(LatLng obj1, LatLng obj2){
        double distance;
        double R = 6371;
        double dLat = deg(obj2.latitude - obj1.latitude);
        double dLon = deg(obj2.longitude - obj1.longitude);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg(obj1.latitude)) * Math.cos(deg(obj2.latitude)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2);


       double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
       distance = R * c;
       //Log.d("DISTANCE LATLNG: ", Double.toString(distance));
       return distance;


    }

    private static double deg(double deg){
        return deg* (Math.PI/180);
    }


    //collison checking
    // distance -> d, radius -> r
    // d^2 < (r1 + r2)
    public static boolean isInArea(LatLng obj1,LatLng obj2){

        return false;
    }

    //collison checking
    // distance -> d, radius -> r
    // d^2 < (r1 + r2)
    public static boolean isInArea(double r1,double r2, double distance){

        double test1 = ((r1)+(r2));

      //  Log.d("CHECK RADIUS", Double.toString(test1));


        if((distance) <= ((r1)+(r2))){
            return true;
        }

        return false;
    }

}
