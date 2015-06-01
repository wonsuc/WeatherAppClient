package com.wonsuc.weatherappclient.utils;

import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.wonsuc.weatherappclient.common.HttpClient;

import java.io.IOException;
import java.util.HashMap;

public class WeatherUtil {

    private final HttpClient httpClient = new HttpClient();
    private OnAddressProviderListener onAddressProviderListener;

    // https://apis.daum.net/local/geo/coord2addr?apikey=a302ba88cd67d2fc7ba5d225ad03e6e1&latitude=37.655592&longitude=127.043646&inputCoordSystem=WGS84&output=json
    public void getKoreanAddress(final Location location) {

        final HashMap<String, String> map = new HashMap<String, String>();

        String url = "https://apis.daum.net/local/geo/coord2addr";
        StringBuilder sb = new StringBuilder();
        String params = sb.append("apikey=").append("a302ba88cd67d2fc7ba5d225ad03e6e1")
                .append("&latitude=").append(location.getLatitude())
                .append("&longitude=").append(location.getLongitude())
                .append("&inputCoordSystem=").append("WGS84")
                .append("&output=").append("json").toString();

        try {
            httpClient.get(url, params,
                    new HttpClient.Fail<Void, Request, IOException>() {
                        @Override
                        public Void call(Request request, IOException e) throws Exception {
                            return null;
                        }
                    },
                    new HttpClient.Success<Void, Response>() {
                        @Override
                        public Void call(Response response) throws Exception {
                            if(response.isSuccessful()) {
                                //System.out.println(response.body().string());
                                JsonElement je = new JsonParser().parse(response.body().charStream());

                                // 국가
                                map.put("country", je.getAsJsonObject().get("name0").getAsString());
                                // 도
                                String province = null;
                                if(je.getAsJsonObject().has("name1")) province = je.getAsJsonObject().get("name1").getAsString();
                                map.put("province", province);
                                // 시/구
                                String district = null;
                                if(je.getAsJsonObject().has("name2")) district = je.getAsJsonObject().get("name2").getAsString();
                                map.put("district", district);
                                // 동
                                map.put("town", je.getAsJsonObject().get("name3").getAsString());

                                // typeRegId: 날씨 상태를 위한 regId
                                // tempRegId: 온도를 위한 regId

                                String typeRegId = "";
                                switch (province) {
                                    case "서울특별시":
                                    case "인천광역시":
                                    case "경기도":
                                        typeRegId = "11B00000";
                                        break;
                                    case "강원도":
                                        switch (district) {
                                            case "춘천시":
                                            case "화천군":
                                            case "양구군":
                                            case "인제군":
                                            case "철원군":
                                            case "홍천군":
                                            case "원주시":
                                            case "횡성군":
                                            case "영월군":
                                            case "평창군":
                                            case "정선군":
                                                typeRegId = "11D10000";
                                                break;
                                            case "강릉시":
                                            case "동해시":
                                            case "삼척시":
                                            case "고성군":
                                            case "속초시":
                                            case "양양군":
                                            case "태백시":
                                                typeRegId = "11D20000";
                                                break;
                                        }
                                        break;
                                    case "충청북도":
                                        typeRegId = "11C10000";
                                        break;
                                    case "대전광역시":
                                    case "세종특별자치시":
                                    case "충청남도":
                                        typeRegId = "11C20000";
                                        break;
                                    case "전라북도":
                                        typeRegId = "11F10000";
                                        break;
                                    case "광주광역시":
                                    case "전라남도":
                                        typeRegId = "11F20000";
                                        break;
                                    case "대구광역시":
                                    case "경상북도":
                                        typeRegId = "11H10000";
                                        break;
                                    case "부산광역시":
                                    case "울산광역시":
                                    case "경상남도":
                                        typeRegId = "11H20000";
                                        break;
                                    case "제주특별자치도":
                                        typeRegId = "11G0000";
                                        break;
                                    default:
                                        break;
                                }

                                map.put("typeRegId", typeRegId);
                                map.put("tempRegId", null);

                                System.out.println(map);

                                // 성공적으로 가져왔을시 이벤트 리스너 작동
                                if (onAddressProviderListener != null) {
                                    onAddressProviderListener.onAddressProvided(map, location);
                                }
                            } else {
                                System.out.println(response);
                            }
                            return null;
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // http://maps.googleapis.com/maps/api/geocode/json?latlng=37.359454,127.126094&sensor=true&language=ko
    public void getKoreanAddress_x(final Location location) {

        final HashMap<String, String> map = new HashMap<String, String>();

        String url = "http://maps.googleapis.com/maps/api/geocode/json";
        String latlng = location.getLatitude() + "," + location.getLongitude();
        StringBuilder sb = new StringBuilder();
        String params = sb.append("latlng=").append(latlng)
                .append("&sensor=").append("true")
                .append("&language=").append("ko").toString();

        try {
            httpClient.get(url, params,
                    new HttpClient.Fail<Void, Request, IOException>() {
                        @Override
                        public Void call(Request request, IOException e) throws Exception {
                            return null;
                        }
                    },
                    new HttpClient.Success<Void, Response>() {
                        @Override
                        public Void call(Response response) throws Exception {
                            if(response.isSuccessful()) {
                                //System.out.println(response.body().string());
                                JsonElement je = new JsonParser().parse(response.body().charStream());
                                JsonArray results = je.getAsJsonObject().getAsJsonArray("results");

                                JsonArray addresses = results.get(2).getAsJsonObject().get("address_components").getAsJsonArray();

                                map.put("country",addresses.get(4).getAsJsonObject().get("long_name").getAsString());
                                map.put("province",addresses.get(3).getAsJsonObject().get("long_name").getAsString());
                                map.put("city",addresses.get(2).getAsJsonObject().get("long_name").getAsString());
                                map.put("district",addresses.get(1).getAsJsonObject().get("long_name").getAsString());
                                map.put("town",addresses.get(0).getAsJsonObject().get("long_name").getAsString());

                                // typeRegId: 날씨 상태를 위한 regId
                                // tempRegId: 온도를 위한 regId

                                map.put("typeRegId", null);
                                map.put("tempRegId", null);



                                System.out.println(map);

                                // 성공적으로 가져왔을시 이벤트 리스너 작동
                                if (onAddressProviderListener != null) {
                                    onAddressProviderListener.onAddressProvided(map, location);
                                }
                            } else {
                                System.out.println(response);
                            }
                            return null;
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface OnAddressProviderListener {
        public void onAddressProvided(HashMap<String, String> map, Location location);
    }

    public void setOnAddressProviderListener(OnAddressProviderListener onAddressProviderListener) {
        this.onAddressProviderListener = onAddressProviderListener;
    }

    //public void setOnMenuClickListener(OnMenuItemClickListener onMenuClickListener) {
    //    this.onMenuClickListener = onMenuClickListener;
    //}

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
