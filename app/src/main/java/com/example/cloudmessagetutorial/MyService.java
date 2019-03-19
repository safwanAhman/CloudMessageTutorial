package com.example.cloudmessagetutorial;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static android.content.ContentValues.TAG;

/*
*
* Handles incoming meesages from FCM
* Sends data from MyService to Maps Activity using Broadcast
*
* Author: Safwan Ahman
*
* */

public class MyService extends FirebaseMessagingService {
    public MyService() {

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...
        Intent intent = new Intent("IntentKey");
        Bundle bundle = new Bundle();

        super.onMessageReceived(remoteMessage);
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        /*
        // Check if message contains a data payload.
        if (remoteMessage.getNotification().getBody() != null) {

            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            bundle.putString("valueName", remoteMessage.getData().get("body"));
            intent.putExtra("body", remoteMessage.getData().get("body"));
            intent.putExtra("title", remoteMessage.getData().get("title"));
            intent.putExtra("place", remoteMessage.getData().get("place"));

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }*/


        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            bundle.putString("valueName", remoteMessage.getData().get("body"));
            intent.putExtra("body", remoteMessage.getData().get("body"));
            intent.putExtra("title", remoteMessage.getData().get("title"));
            intent.putExtra("place", remoteMessage.getData().get("place"));

            remoteMessage.getData().values();

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

    }

    private void scheduleJob() {
        // [START dispatch_job]
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
        // [END dispatch_job]

    }

     /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    public void sendRegistrationToServer(String token){
        // send token to web service ??
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        //MAKE SURE IT IS in THE CORRECT PATH
        DatabaseReference ref = database.getReference("server/saving-data/IDs");
        // then store your token ID
        ref.push().setValue(token);

    }

}
