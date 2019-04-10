package com.example.cloudmessagetutorial;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.cloudmessagetutorial.MyService;

import java.util.regex.Pattern;


//a test class for all the UI function
//should be deleted after mapactivity is done
public class MainActivity extends AppCompatActivity {

    EditText editText;
    boolean matches;
    String split[];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
  }

    public void press(View view){
        editText = (EditText) findViewById(R.id.latlng);

        //this pattern is a regex for lat and ln
        matches = Pattern.matches("^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?)([\\s*]?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$"
                , editText.getText().toString());

        if(matches) {
            popup2();
        }else
            Toast.makeText(MainActivity.this, "Is not a LatLng, please enter correct LatLng", Toast.LENGTH_LONG).show();


    }

    public void popup2(){

        //splitting the given latlng into lat and lng
        split = editText.getText().toString().split(",");

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        Context context = this;
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText nameBox = new EditText(context);
        nameBox.setHint("Name");
        layout.addView(nameBox);

        final EditText latBox = new EditText(context);
        latBox.setHint("Latitude");
        layout.addView(latBox);


        final EditText  lngBox = new EditText(context);
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


        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "Added: " +  latBox.getText(), Toast.LENGTH_LONG).show();

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

    }

    public void test2(View view){

        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);


    }

}
