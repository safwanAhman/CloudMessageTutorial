package com.example.cloudmessagetutorial;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/*
*
* Where all the notification function should be
* To have a degree of seperation from MapsActivity class
* avoid it looking like a spaghetti code.
*
*
* Author: Safwan Ahman
*
* */
public class SendNotification {


    private Context mContext;

    SendNotification(Context mContext){
        this.mContext = mContext;
    }

    //notification showing that service is currently active
    public Notification createNotification(){
        Notification.Builder builder = new Notification.Builder(mContext, "default")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Active Service")
                .setContentText("Tap to return to map")
                .setPriority(Notification.PRIORITY_HIGH);

        Intent resultIntent = new Intent(mContext, MapsActivity.class);
        Intent t = new Intent();  //for testing purposes
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext,0,t,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        return builder.build(); //xoxo gossip girl

    }

    //FCM messages are to be sent as a notification whenreceived under certain conditions
    //should implement TYPE of notification once geoquery wors properly
    public void sendNotification(String title, String format) {
        Notification.Builder builder = new Notification.Builder(mContext, "default")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(format)
                .setPriority(Notification.PRIORITY_HIGH);

        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "channel",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("CLOUD_MESSAGING_NOTIFICATION");
            mNotificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(mContext, MapsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults  |= Notification.DEFAULT_SOUND;

        mNotificationManager.notify(0,notification);
    }

}
