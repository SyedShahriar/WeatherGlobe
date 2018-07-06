package com.example.shahriarsyed.WeatherGlobe;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(isServiceOK()){
            init();
        }
    }

    private void init(){
        Button BtnMap = (Button) findViewById(R.id.btnMap);
        BtnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                startActivity(intent);
            }
        });

    }

    public boolean isServiceOK(){
        Log.d(TAG,"isServicesOK: checking Google Play Services version.");
        int available = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(MainActivity.this);
        if (available== ConnectionResult.SUCCESS){
            //everything is ok
            Log.d(TAG,"isServicesOK: Google Play Services is working.");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //the error is resolvable
            Log.d(TAG,"isServicesOK: An error occurred but it can be resolved");
            Dialog dialog = GoogleApiAvailability.getInstance().
                    getErrorDialog(MainActivity.this,available,ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else{
            Toast.makeText(this, "We are unable to make map requests.", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
