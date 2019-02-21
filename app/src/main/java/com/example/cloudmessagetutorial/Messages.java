package com.example.cloudmessagetutorial;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.MessagingAnalytics;

public class Messages {

    private String title,body,place;
    private int type;
    private LatLng latLng;
    private final int TYPE_ACTIVITY = 1, TYPE_DISPLAY = 2, TYPE_WEBSITE = 3, TYPE_GENERAL = 4;


    public Messages(){}

    public Messages(String title, String body, String place, LatLng latLng, int type){
        this.title = title;
        this.body = body;
        this.place = place;
        this.latLng = latLng;
        this.type = type;
    }

    //for testing purpose
    //before implementing type of messages
    public Messages(String title, String body, String place){
        this.title = title;
        this.body = body;
        this.place = place;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public int getType() {
        return type;
    }

    public String getBody() {
        return body;
    }

    public String getPlace() {
        return place;
    }

    public String getTitle() {
        return title;
    }
}
