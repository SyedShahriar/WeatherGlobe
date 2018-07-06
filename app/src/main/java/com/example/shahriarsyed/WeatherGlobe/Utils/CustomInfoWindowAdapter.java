package com.example.shahriarsyed.WeatherGlobe.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shahriarsyed.WeatherGlobe.Models.InfoWindowData;
import com.example.shahriarsyed.WeatherGlobe.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
    private Context mContext;

    public CustomInfoWindowAdapter(Context mContext) {
        this.mContext = mContext;
        mWindow = LayoutInflater.from(mContext).inflate(R.layout.custom_info_window,null);
    }

    private void renderWindowText (Marker marker, View view){
        String title = marker.getTitle();
        TextView tvtitle = (TextView) view.findViewById(R.id.title);
        //String snippet = marker.getSnippet();
        //TextView tvsnippet = (TextView) view.findViewById(R.id.snippet);
        InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();
        TextView tvtemp = (TextView) view.findViewById(R.id.current_temperature_field);
        ImageView tvpic = (ImageView) view.findViewById(R.id.pic);
        TextView tvdescription = view.findViewById(R.id.current_description_field);
        TextView tvhumidity = view.findViewById(R.id.current_humidity_field);
        TextView tvpressure = view.findViewById(R.id.current_pressure_field);

        if (!title.equals("")){
            tvtitle.setText(title);
        }
//        if (!snippet.equals("")){
//            //tvsnippet.setText(snippet);
//        }
        tvtemp.setText(infoWindowData.getTemperature());

        tvpic.setImageResource(infoWindowData.getImage());
        tvdescription.setText(infoWindowData.getDescription());
        tvhumidity.setText(infoWindowData.getHumidity());
        tvpressure.setText(infoWindowData.getPressure());
    }

    @Override
    public View getInfoWindow(Marker marker) {
        renderWindowText(marker,mWindow);
        return mWindow;

    }

    @Override
    public View getInfoContents(Marker marker) {
        renderWindowText(marker,mWindow);
        return mWindow;
    }
}
