package com.example.cloudmessagetutorial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ServiceReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, GeoService.class));
        Toast.makeText(context, "service receiver at work", Toast.LENGTH_SHORT).show();
    }
}
