package com.wonsuc.weatherappclient.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;


public class CurrentWeatherAdapter extends ArrayAdapter {

    private final LayoutInflater inflater;

    public CurrentWeatherAdapter(Context context) {
        super(context, 0);
        this.inflater = LayoutInflater.from(context);
        //setupMenuItems();
    }
}
