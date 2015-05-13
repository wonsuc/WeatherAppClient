package com.wonsuc.weatherappclient.ui.utils;

import java.util.HashMap;

public class WeatherUtil {

    public HashMap<String, String> getKoreanAddress() {
        return null;
    }

    //
    // LCC DFS 좌표변환 ( code : "toXY"(위경도->좌표, v1:위도, v2:경도), "toLL"(좌표->위경도,v1:x,v2:y) )
    //
    public HashMap<String, Double> convXY(String code, double v1, double v2) {
        //
        // LCC DFS 좌표변환을 위한 기초 자료
        //
        double RE = 6371.00877; // 지구 반경(km)
        double GRID = 5.0; // 격자 간격(km)
        double SLAT1 = 30.0; // 투영 위도1(degree)
        double SLAT2 = 60.0; // 투영 위도2(degree)
        double OLON = 126.0; // 기준점 경도(degree)
        double OLAT = 38.0; // 기준점 위도(degree)
        int XO = 43; // 기준점 X좌표(GRID)
        int YO = 136; // 기1준점 Y좌표(GRID)

        double DEGRAD = Math.PI / 180.0;
        double RADDEG = 180.0 / Math.PI;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon  = OLON  * DEGRAD;
        double olat  = OLAT  * DEGRAD;

        double sn = Math.tan( Math.PI*0.25 + slat2*0.5 ) / Math.tan( Math.PI*0.25 + slat1*0.5 );
        sn = Math.log( Math.cos(slat1) / Math.cos(slat2) ) / Math.log(sn);
        double sf = Math.tan( Math.PI*0.25 + slat1*0.5 );
        sf = Math.pow(sf,sn) * Math.cos(slat1) / sn;
        double ro = Math.tan( Math.PI*0.25 + olat*0.5 );
        ro = re * sf / Math.pow(ro,sn);
        HashMap<String, Double> rs = new HashMap<String, Double>();

        if (code.equals("toXY")) {
            rs.put("lat", v1);
            rs.put("lng", v2);

            double ra = Math.tan( Math.PI*0.25 + (v1)*DEGRAD*0.5 );
            ra = re * sf / Math.pow(ra,sn);
            double theta = v2 * DEGRAD - olon;
            if (theta >  Math.PI) theta -= 2.0 * Math.PI;
            if (theta < -Math.PI) theta += 2.0 * Math.PI;
            theta *= sn;
            rs.put("x", Math.floor( ra*Math.sin(theta) + XO + 0.5 ));
            rs.put("y", Math.floor( ro - ra*Math.cos(theta) + YO + 0.5 ));
        } else {
            rs.put("x", v1);
            rs.put("y", v2);

            double xn = v1 - XO;
            double yn = ro - v2 + YO;
            double ra = Math.sqrt( xn*xn+yn*yn );
            if (sn < 0.0) ra *= -1;
            double alat = Math.pow( (re*sf/ra),(1.0/sn) );
            alat = 2.0*Math.atan(alat) - Math.PI*0.5;

            double theta;
            if (Math.abs(xn) <= 0.0) {
                theta = 0.0;
            } else {
                if (Math.abs(yn) <= 0.0) {
                    theta = Math.PI*0.5;
                    if( xn < 0.0 ) theta *= -1;
                } else {
                    theta = Math.atan2(xn,yn);
                }
            }
            double alon = theta/sn + olon;
            rs.put("lat", alat*RADDEG);
            rs.put("lng", alon*RADDEG);
        }
        return rs;
    }

}
