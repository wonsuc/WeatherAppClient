package com.wonsuc.weatherappclient.model;

public class DailyForecast {
    public String date;
    public String time;

    public String WAV;
    public String POP;
    public String PTY;
    public String R06;
    public String S06;
    public String TMN;
    public String TMX;
    public String UUU;
    public String SKY;
    public String VVV;
    public String WSD;
    public String T3H;
    public String REH;
    public String VEC;

    @Override
    public String toString() {
        return "date: " + date + ", time: " + time + ", T3H: " + T3H + ", SKY: " + SKY;
    }
}
