package com.example.shahriarsyed.WeatherGlobe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shahriarsyed.WeatherGlobe.Models.InfoWindowData;
import com.example.shahriarsyed.WeatherGlobe.Models.PlaceInfo;
import com.example.shahriarsyed.WeatherGlobe.Utils.CustomInfoWindowAdapter;
import com.example.shahriarsyed.WeatherGlobe.Utils.PlaceAutocompleteAdapter;
import com.example.shahriarsyed.WeatherGlobe.Utils.RemoteFetch;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    protected GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;

    private GoogleMap mMap;

    private FusedLocationProviderClient mFusedLocationClient;

    private static final String TAG = "MapsActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40,-168),
            new LatLng(71,136));
    private static final int PLACE_PICKER_REQUEST = 1;

    private Boolean mLocationsPermissionsGranted = false;

    private AutoCompleteTextView msearchText;
    private ImageView mGps,mInfo,mPlacePicker;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private PlaceInfo mPlace;
    //private double mTemperature;
    private Marker mMarker;

    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        msearchText = (AutoCompleteTextView) findViewById(R.id.input_search);
        mGps = findViewById(R.id.ic_gps);
        mInfo = findViewById(R.id.place_info);
        mPlacePicker = findViewById(R.id.place_picker);
        getLocationPermission();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: Map is ready.");
        mMap = googleMap;
        if (mLocationsPermissionsGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            init();

        }

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: Called");
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationsPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationsPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: Permissions Failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: Permissions Granted.");

                    mLocationsPermissionsGranted = true;
                    initMap();
                    //init map
                }
        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: Getting the current location of this device.");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationsPermissionsGranted) {
                Task location = mFusedLocationClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: We have found the location. ");
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM,
                                    "Me");
                        } else {
                            Log.d(TAG, "onComplete: Location not found.");
                            Toast.makeText(MapsActivity.this, "Unable to get current location.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: Security Exception: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latlng, float zoom, String title) {
        Log.d(TAG, "moveCamera: Moving the camera to lat:" + latlng.latitude + ", long: "
                + latlng.longitude
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));
        updateWeatherData(latlng);

        //if (title!="Me"){
        MarkerOptions options = new MarkerOptions().position(latlng).title(title);
        mMarker = mMap.addMarker(options);
        //}

        HideSoftKeyBoard();

    }

    private void moveCamera(LatLng latlng, float zoom, PlaceInfo placeInfo) {
        Log.d(TAG, "moveCamera: Moving the camera to lat:" + latlng.latitude + ", long: "
                + latlng.longitude
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));

        mMap.clear();

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));

        updateWeatherData(latlng);

        if (placeInfo!=null){
            try{
                String snippet = "Address: " + placeInfo.getAddress() + "\n" +
                        "Phone Number: " + placeInfo.getPhoneNumber() + "\n" +
//                        "Website " + placeInfo.getWebsiteUri() + "\n" +
                        "Price Rating: " + placeInfo.getRating() + "\n";
                MarkerOptions options = new MarkerOptions().
                        position(latlng).
                        title(placeInfo.getName()).
                        snippet(snippet);
                mMarker = mMap.addMarker(options);
            }
            catch(NullPointerException e){
                Log.e(TAG,"moveCamera: NullPointerException" + e.getMessage());
            }
        }
        else{
            mMap.addMarker(new MarkerOptions().position(latlng));
        }

        HideSoftKeyBoard();

    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: Getting location permissions.");
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};
        if (ContextCompat.
                checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.
                    checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationsPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void initMap() {
        Log.d(TAG, "initMap: Initializing Map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

    }

    private void init(){
        Log.d(TAG,"init: Initializing app...");

        mGeoDataClient = Places.getGeoDataClient(this,null);

        msearchText.setOnItemClickListener(mAutocompleteClickListenener);

        mPlaceDetectionClient = Places.getPlaceDetectionClient(this,null);

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this,mGeoDataClient,
                LAT_LNG_BOUNDS,null);

        msearchText.setAdapter(mPlaceAutocompleteAdapter);

        msearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i== EditorInfo.IME_ACTION_SEARCH
                        || i == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){
                    geoLocate();
                }
                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick: GPS Icon was clicked.");
                getDeviceLocation();
            }
        });

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick: clicked Place info");
                try{
                    if(mMarker.isInfoWindowShown()){
                        mMarker.hideInfoWindow();
                    }
                    else{
                        //Log.d(TAG,"onClick: place info : " + mPlace.toString());
                        mMarker.showInfoWindow();
                    }
                }
                catch(NullPointerException e){
                    Log.e(TAG,"onClick: NullPointerException" + e.getMessage());
                }
            }
        });

        mPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(MapsActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    Log.e(TAG,"onClick: GooglePlayServicesRepairableException " + e.getMessage() );
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.e(TAG,"onClick: GooglePlayServicesNotAvailableException " + e.getMessage() );
                }
            }
        });

        HideSoftKeyBoard();


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this,data);
                Task<PlaceBufferResponse> placeResult = mGeoDataClient.getPlaceById(place.getId());
                placeResult.addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                        if(task.isSuccessful()){
                            PlaceBufferResponse places = task.getResult();
                            Place myPlace = places.get(0);
                            try{
                                mPlace = new PlaceInfo();
                                mPlace.setName(myPlace.getName().toString());
                                mPlace.setAddress(myPlace.getAddress().toString());
                                //mPlace.setAttributions(myPlace.getAttributions().toString());
                                mPlace.setId(myPlace.getId());
                                mPlace.setLatLng(myPlace.getLatLng());
                                mPlace.setPhoneNumber(myPlace.getPhoneNumber().toString());
                                mPlace.setRating(myPlace.getRating());
                                //mPlace.setWebsiteUri(myPlace.getWebsiteUri());
                                Log.d(TAG,"onResult: myplace:" + mPlace.toString());
                            }
                            catch(NullPointerException e){
                                Log.e(TAG,"onResult: NullPointerException:" + e.getMessage());
                            }
                            moveCamera(new LatLng(myPlace.getViewport().getCenter().latitude,
                                            myPlace.getViewport().getCenter().longitude),
                                    DEFAULT_ZOOM,mPlace);
                            places.release();
                        }else {
                            Log.e(TAG, "Place not found.");
                        }
                    }
                });
            }
        }
    }

    private void geoLocate(){
        Log.d(TAG,"geoLocate: geoLocating");
        String searchString = msearchText.getText().toString();
        Geocoder geocoder = new Geocoder(MapsActivity.this);

        List <Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString,1);
        }
        catch(IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
        }

        if(list.size()>0){
            Address address = list.get(0);
            Log.d(TAG,"geoLocate: " + address.toString());
            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM, address.getAddressLine(0));
            //Toast.makeText(this,address.toString(),Toast.LENGTH_SHORT);
        }
    }

    private void HideSoftKeyBoard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void updateWeatherData(final LatLng latLng){
        handler = new Handler();
        new Thread(){
            public void run(){
                final JSONObject json = RemoteFetch.getJSON(MapsActivity.this,latLng.latitude,latLng.longitude);
                if(json == null){
                    handler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(MapsActivity.this,
                                    MapsActivity.this.getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable(){
                        public void run(){
                            renderWeather(json);
                        }
                    });
                }
            }
        }.start();
    }

    private void renderWeather(JSONObject json){
        InfoWindowData info = new InfoWindowData();
        try {
            JSONObject main = json.getJSONObject("main");
            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            info.setTemperature((String.format("%.2f", main.getDouble("temp"))+ " ℃"));
            info.setDescription(details.getString("description").toUpperCase(Locale.US));
            info.setHumidity("Humidity: " + main.getString("humidity") + "%");
            info.setPressure("Pressure: " + main.getString("pressure") + " hPa");

            int actualId = details.getInt("id");
            long sunrise = json.getJSONObject("sys").getLong("sunrise") * 1000;
            long sunset = json.getJSONObject("sys").getLong("sunset") * 1000;

            int id = actualId / 100;
            String icon = "";
            if(actualId == 800){
                long currentTime = new Date().getTime();
                if(currentTime>=sunrise && currentTime<sunset) {
                    icon = MapsActivity.this.getString(R.string.weather_sunny);
                } else {
                    icon = MapsActivity.this.getString(R.string.weather_clear_night);
                }
            } else {
                switch(id) {
                    case 2 : icon = MapsActivity.this.getString(R.string.weather_thunder);
                        break;
                    case 3 : icon = MapsActivity.this.getString(R.string.weather_drizzle);
                        break;
                    case 7 : icon = MapsActivity.this.getString(R.string.weather_foggy);
                        break;
                    case 8 : icon = MapsActivity.this.getString(R.string.weather_cloudy);
                        break;
                    case 6 : icon = MapsActivity.this.getString(R.string.weather_snowy);
                        break;
                    case 5 : icon = MapsActivity.this.getString(R.string.weather_rainy);
                        break;
                }
            }
            Log.d(TAG,"Icon: The icon is set to " + icon);

            int imageId = MapsActivity.this.getResources().getIdentifier(icon.toLowerCase(),
                    "mipmap", MapsActivity.this.getPackageName());

            info.setImage(imageId);
            mMarker.setTag(info);
        }catch(Exception e){
            Log.e(TAG, "One or more fields not found in the JSON data");
        }
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListenener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            HideSoftKeyBoard();
            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();
            Task<PlaceBufferResponse> placeResult = mGeoDataClient.getPlaceById(placeId);
            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                    if(task.isSuccessful()){
                        PlaceBufferResponse places = task.getResult();
                        Place myPlace = places.get(0);
                        try{
                            mPlace = new PlaceInfo();
                            mPlace.setName(myPlace.getName().toString());
                            mPlace.setAddress(myPlace.getAddress().toString());
                            //mPlace.setAttributions(myPlace.getAttributions().toString());
                            mPlace.setId(myPlace.getId());
                            mPlace.setLatLng(myPlace.getLatLng());
                            mPlace.setPhoneNumber(myPlace.getPhoneNumber().toString());
                            mPlace.setRating(myPlace.getRating());
                            //mPlace.setWebsiteUri(myPlace.getWebsiteUri());
                            Log.d(TAG,"onResult: myplace:" + mPlace.toString());
                        }
                        catch(NullPointerException e){
                            Log.e(TAG,"onResult: NullPointerException:" + e.getMessage());
                        }
                        moveCamera(new LatLng(myPlace.getViewport().getCenter().latitude,
                                        myPlace.getViewport().getCenter().longitude),
                                DEFAULT_ZOOM,mPlace);
                        places.release();
                    }else {
                        Log.e(TAG, "Place not found.");
                    }
                }
            });
        }
    };

}
