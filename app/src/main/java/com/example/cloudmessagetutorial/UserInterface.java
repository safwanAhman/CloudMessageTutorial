package com.example.cloudmessagetutorial;

import android.content.Context;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class UserInterface {

private boolean tmpadd = false;

    public LinearLayout pop(Context context, String names, double lats, double lngs, double radiuss, String dess){

        final android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(context);
      //  Context context = this;
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText name = new EditText(context);
        name.setEnabled(false);
        layout.addView(name);

        final EditText lat = new EditText(context);
        lat.setEnabled(false);
        layout.addView(lat);

        final EditText lng = new EditText(context);
        lng.setEnabled(false);
        layout.addView(lng);

        final EditText radius = new EditText(context);
        radius.setEnabled(false);
        layout.addView(radius);

        final EditText description = new EditText(context);
        description.setEnabled(false);
        layout.addView(description);

        name.setText("Name: " + names);
        lat.setText("Latitude: "+ Double.toString(lats) );
        lng.setText("Longitude: "+ Double.toString(lngs) );
        radius.setText("Radius: " + Double.toString(radiuss));
        description.setText("Description: " + dess);

        return layout;

    }

    public boolean inputpop(final boolean added, EditText editText, final Context context, final String name, final GeoService geoService, final GoogleMap mMap){
        //splitting the given latlng into lat and lng
       String[] split = editText.getText().toString().split(",");

       tmpadd = added;
        final android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(context);


        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText nameBox = new EditText(context);

        if(name.equals("")) {
            nameBox.setHint("Name");
            layout.addView(nameBox);
        }else{
            //  nameBox.setEnabled(false);
            nameBox.setHint(name);
            layout.addView(nameBox);
        }

        final EditText latBox = new EditText(context);
        latBox.setEnabled(false);
        latBox.setHint("Latitude");
        layout.addView(latBox);

        final EditText  lngBox = new EditText(context);
        lngBox.setEnabled(false);
        lngBox.setHint("Longitude");
        layout.addView(lngBox);

        final EditText radiusBox = new EditText(context);
        radiusBox.setHint("Radius");
        layout.addView(radiusBox);

        final EditText descriptionBox = new EditText(context);
        descriptionBox.setHint("Description");
        layout.addView(descriptionBox);



        alertDialogBuilder.setView(layout);

        latBox.setText(split[0]);
        lngBox.setText(split[1]);

        final LatLng latLng = new LatLng(Double.parseDouble(split[0]), Double.parseDouble(split[1]));

        alertDialogBuilder.setCancelable(true).setPositiveButton("Add Geofence", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(MapsActivity.this, "Added: " +  nameBox.getText().toString(), Toast.LENGTH_LONG).show();
                if(TextUtils.isEmpty(nameBox.getText().toString()) ||
                        TextUtils.isEmpty(descriptionBox.getText().toString()) ||
                        TextUtils.isEmpty(radiusBox.getText().toString()) ){

                    Toast.makeText(context, "Please enter all field!", Toast.LENGTH_SHORT).show();
                }else{
                    geoService.addGeofence(nameBox.getText().toString(),
                            latLng,
                            Double.parseDouble(radiusBox.getText().toString()),
                            mMap,
                            descriptionBox.getText().toString());

                    geoService.addToDB(nameBox.getText().toString(),
                            latLng,
                            Double.parseDouble(radiusBox.getText().toString()),
                            descriptionBox.getText().toString());

                    if(tmpadd){
                        Toast.makeText(context, "SHOULD DELETE:" + name, Toast.LENGTH_LONG).show();
                        geoService.deleteFromDB(name);
                        SystemClock.sleep(1000);   //not sure if necessary
                        tmpadd = false;

                    }

                }
            }
        });

        alertDialogBuilder.setCancelable(true).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

        Toast.makeText(context, "end of function", Toast.LENGTH_SHORT).show();

        return tmpadd;

    }
}
