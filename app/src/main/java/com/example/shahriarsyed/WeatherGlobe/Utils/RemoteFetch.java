package com.example.shahriarsyed.WeatherGlobe.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.example.shahriarsyed.WeatherGlobe.R;

public class RemoteFetch {

    private static final String TAG = "RemoteFetch";
    private static final String OPEN_WEATHER_MAP_API =
            //"http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric";
            "http://api.openweathermap.org/data/2.5/weather?lat=%1$.3f&lon=%2$.3f&units=metric";

    public static JSONObject getJSON(Context context,double latitude,double longitude){
        try {
            Log.d(TAG,"RemoteFetch:" + latitude + ", " + longitude  );
            URL url = new URL(String.format(OPEN_WEATHER_MAP_API, latitude, longitude));
            HttpURLConnection connection =
                    (HttpURLConnection)url.openConnection();

            connection.addRequestProperty("x-api-key",
                    context.getString(R.string.open_weather_maps_app_id));

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            // This value will be 404 if the request was not
            // successful
            if(data.getInt("cod") != 200){
                return null;
            }

            return data;
        }catch(Exception e){
            return null;
        }
    }
}
