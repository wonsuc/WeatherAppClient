package com.wonsuc.weatherappclient.model;

public class LongTermForecast {
    public String date;

    public String type;
    public String lowTemp;
    public String highTemp;

    @Override
    public String toString() {
        return "date: " + date + ", type: " + type + ", lowTemp: " + lowTemp + ", highTemp: " + highTemp;
    }
}
