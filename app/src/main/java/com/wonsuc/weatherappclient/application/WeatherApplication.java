package com.wonsuc.weatherappclient.application;

import android.app.Application;

import com.wonsuc.weatherappclient.ui.adapter.GlobalMenuAdapter;

public class WeatherApplication extends Application {

    public GlobalMenuAdapter.GlobalMenuItem selectedGlobalMenuItem;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
