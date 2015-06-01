package com.wonsuc.weatherappclient.ui.activity;

import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;
import com.wonsuc.weatherappclient.R;
import com.wonsuc.weatherappclient.common.HttpClient;
import com.wonsuc.weatherappclient.model.DailyForecast;
import com.wonsuc.weatherappclient.model.LongTermForecast;
import com.wonsuc.weatherappclient.ui.adapter.DailyWeatherAdapter;
import com.wonsuc.weatherappclient.ui.adapter.LongTermWeatherAdapter;
import com.wonsuc.weatherappclient.utils.WeatherUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DailyWeatherActivity extends BaseActivity implements WeatherUtil.OnAddressProviderListener {

    private static final String TAG = DailyWeatherActivity.class.getSimpleName();

    private final HttpClient httpClient = new HttpClient();
    private WeatherUtil weatherUtil = new WeatherUtil();

    private final String ServiceKey = "U%2F6locvWMaaYvTFLXqyMJvAW%2FeRXl8iFCpKULs%2BAFLh3DepTcLoeDlNpg1EY2rb%2FHVRpXNZMgoPTFsJZJpLjaA%3D%3D";
    private String BaseDate = "", BaseTime = "", BaseFullTime = "", NX = "", NY = "";

    private String address = "", typeRegId = "", tempRegId = "";

    public static enum CategoryValue {

        T1H("기온"),
        RN1("1시간 강수량"),
        SKY("하늘상태"),
        UUU("동서바람성분"),
        VVV("남북바람성분"),
        REH("습도"),
        PTY("강수형태"),
        LGT("낙뢰"),
        VEC("풍향"),
        WSD("풍속");

        private String text;

        CategoryValue(String text) {
            this.text = text;
        }

        public String getText() {
            return this.text;
        }
    }

    public static enum ObsrValue {

        T1H("℃"),
        RN1("mm"),
        SKY("SKY"),
        UUU("m/s"),
        VVV("m/s"),
        REH("%"),
        PTY("PTY"),
        LGT("LGT"),
        VEC(""),
        WSD("m/s");

        private String text;

        ObsrValue(String text) {
            this.text = text;
        }

        public String getText() {
            return this.text;
        }
    }


    @InjectView(R.id.weather_current_list_title)
    TextView weatherCurrentListTitleTextView;

    @InjectView(R.id.weather_current_list)
    ListView weatherCurrentListView;

    LongTermWeatherAdapter longTermWeatherAdapter;
    DailyWeatherAdapter dailyWeatherAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Picasso로 간단히 이미지를 다운로드 받고 bg에 draw 한다.
        Picasso
        .with(this)
        .load("http://cdn.eyeem.com/thumb/h/800/6df34d42fa813b926f24cf9d32d49eea779cc014-1405682234")
        .placeholder(new ColorDrawable(0xffaaaaaa))
                .into((ImageView) findViewById(R.id.bg));

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // 어댑터 설정 부분
        //dailyWeatherAdapter = new DailyWeatherAdapter(this,0);
        //weatherCurrentListView.setAdapter(dailyWeatherAdapter);

        longTermWeatherAdapter = new LongTermWeatherAdapter(this,0);
        weatherCurrentListView.setAdapter(longTermWeatherAdapter);

        // 이렇게 함으로써 비로서 WeatherUtil 객체에서 메인에서 입력된 메서드들을 호출할 수 있다.
        weatherUtil.setOnAddressProviderListener(this);
    }

    private void setTitleView(String address) {
        // 타이틀 영역의 날짜와 시간, 현재 위치 정보 표시
        String TitleDate = DateFormat.format("yyyy년 MM월 dd일 H시", Calendar.getInstance().getTime()).toString();
        weatherCurrentListTitleTextView.setText(TitleDate + ", " + address + "의 날씨 정보");
    }

    // http://newsky2.kma.go.kr/service/MiddleFrcstInfoService/getMiddleLandWeather?ServiceKey=U%2F6locvWMaaYvTFLXqyMJvAW%2FeRXl8iFCpKULs%2BAFLh3DepTcLoeDlNpg1EY2rb%2FHVRpXNZMgoPTFsJZJpLjaA%3D%3D&regId=11B00000&tmFc=201310171800&numOfRows=1&pageNo=1
    private void getLongTermType(Location location, String typeRegId) {
        // http 클라이언트
        Date time = getLongTermProperTime();
        BaseFullTime = DateFormat.format("yyyyMMddHHmm", time).toString();

        String url = "http://newsky2.kma.go.kr/service/MiddleFrcstInfoService/getMiddleLandWeather";
        StringBuilder sb = new StringBuilder();
        String params = sb.append("ServiceKey=").append(ServiceKey)
                .append("&regId=").append(typeRegId)
                .append("&tmFc=").append(BaseFullTime)
                .append("&_type=").append("json").toString();

        System.out.println(url + "?" + params);

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
                                setToLongTermWeatherAdapter(dailyTypeParser(response), null);
                            }else{
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

    // http://newsky2.kma.go.kr/service/getMiddleFrcstInfoService/MiddleTemperature?ServiceKey=TEST_SERVICEKEY&regId=11D20501&tmFc=201404080600&pageNo=1&numOfRows=10
    private void getLongTermTemp(Location location, String tempRegId) {
        // http 클라이언트
        Date time = getLongTermProperTime();
        BaseFullTime = DateFormat.format("yyyyMMddHHmm", time).toString();

        String url = "http://newsky2.kma.go.kr/service/getMiddleFrcstInfoService/MiddleTemperature";
        StringBuilder sb = new StringBuilder();
        String params = sb.append("ServiceKey=").append(ServiceKey)
                .append("&regId=").append("11D20501")
                .append("&tmFc=").append(BaseFullTime)
                .append("&_type=").append("json").toString();

        System.out.println(url + "?" + params);

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
                                dailyTempParser(response);
                            }else{
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

    private void getShortTerm(Location location) {
        // 타이틀 영역의 날짜와 시간, 현재 위치 정보 표시
        String TitleDate = DateFormat.format("yyyy년 MM월 dd일 H시", Calendar.getInstance().getTime()).toString();
        weatherCurrentListTitleTextView.setText(TitleDate + ", " + address + "의 날씨 정보");

        // http 클라이언트
        Date time = getShortTermProperTime();
        BaseDate = DateFormat.format("yyyyMMdd", time).toString();
        BaseTime = DateFormat.format("HHmm", time).toString();

        HashMap<String, Double> locationMap = weatherUtil.convXY("toXY", location.getLatitude(), location.getLongitude());
        NX = String.valueOf(locationMap.get("x").intValue());
        NY = String.valueOf(locationMap.get("y").intValue());

        Log.v(TAG, "NX: " + NX + ", NY: " + NY);

        String url = "http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService/ForecastSpaceData";
        StringBuilder sb = new StringBuilder();
        String params = sb.append("ServiceKey=").append(ServiceKey)
                .append("&base_date=").append(BaseDate)
                .append("&base_time=").append(BaseTime)
                .append("&nx=").append(NX)
                .append("&ny=").append(NY)
                .append("&numOfRows=").append("300")
                .append("&_type=").append("json").toString();

        System.out.println(url + "?" + params);

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
                            jsonParser(response);
                        }else{
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

    /*
        단기 예보의 경우(3시간 간격): 23, 2, 5, 8, 11, 14, 17, 20
     */
    private Date getShortTermProperTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) - 20);
        cal.set(Calendar.MINUTE, 0);
        Date time = cal.getTime();

        Calendar testCal = Calendar.getInstance();
        testCal.set(Calendar.MINUTE, 0);

        HashMap<String, Date> timeMap = new HashMap<>();

        for (int i = 2; i <= 23; i += 3) {
            testCal.set(Calendar.HOUR, i);
            timeMap.put(String.valueOf(i), testCal.getTime());
        }

        if (time.before(timeMap.get("2"))) {
            cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
            cal.set(Calendar.HOUR, 23);
            return cal.getTime();
        } else if (time.after(timeMap.get("2")) && time.before(timeMap.get("5")) || time.equals(timeMap.get("2"))) {
            cal.set(Calendar.HOUR, 2);
            return cal.getTime();
        } else if (time.after(timeMap.get("5")) && time.before(timeMap.get("8")) || time.equals(timeMap.get("5"))) {
            cal.set(Calendar.HOUR, 5);
            return cal.getTime();
        } else if (time.after(timeMap.get("8")) && time.before(timeMap.get("11")) || time.equals(timeMap.get("8"))) {
            cal.set(Calendar.HOUR, 8);
            return cal.getTime();
        } else if (time.after(timeMap.get("11")) && time.before(timeMap.get("14")) || time.equals(timeMap.get("11"))) {
            cal.set(Calendar.HOUR, 11);
            return cal.getTime();
        } else if (time.after(timeMap.get("14")) && time.before(timeMap.get("17")) || time.equals(timeMap.get("14"))) {
            cal.set(Calendar.HOUR, 14);
            return cal.getTime();
        } else if (time.after(timeMap.get("17")) && time.before(timeMap.get("20")) || time.equals(timeMap.get("17"))) {
            cal.set(Calendar.HOUR, 17);
            return cal.getTime();
        } else if (time.after(timeMap.get("20")) && time.before(timeMap.get("23")) || time.equals(timeMap.get("20"))) {
            cal.set(Calendar.HOUR, 20);
            return cal.getTime();
        } else if (time.after(timeMap.get("23")) || time.equals(timeMap.get("23"))) {
            cal.set(Calendar.HOUR, 23);
            return cal.getTime();
        } else {
            return null;
        }
    }

    /*
        중기 예보의 경우(12시간 간격): 6, 18
     */
    private Date getLongTermProperTime() {
        Calendar cal = Calendar.getInstance();
        // 3일 이후의 날씨부터 제공하기 때문에 오늘 날씨를 알고싶으면 3일 전(오후 6시)을 기준으로 요청한다.
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 3);
        cal.set(Calendar.HOUR, 18);
        cal.set(Calendar.MINUTE, 0);
        Date time = cal.getTime();

        return time;
        /*Calendar testCal = Calendar.getInstance();
        testCal.set(Calendar.MINUTE, 0);

        HashMap<String, Date> timeMap = new HashMap<>();

        for (int i = 2; i <= 23; i += 3) {
            testCal.set(Calendar.HOUR, i);
            timeMap.put(String.valueOf(i), testCal.getTime());
        }

        // 문제가 존재하는데 새벽 6시 이전에 자료를 요청하게 되면 전날 18시 기준의 데이터가 로드된다.
        // 중기 예보는 3일 이후부터 예보를 제공하기 때문에 데이터 상의 3일 이후가 실제로는 2일 이후의 날씨가 된다.

        testCal.set(Calendar.HOUR, 6);
        timeMap.put("6", testCal.getTime());
        testCal.set(Calendar.HOUR, 18);
        timeMap.put("18", testCal.getTime());

        if (time.before(timeMap.get("6"))) {
            cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
            cal.set(Calendar.HOUR, 18);
            return cal.getTime();
        } else if (time.after(timeMap.get("6")) && time.before(timeMap.get("18")) || time.equals(timeMap.get("6"))) {
            cal.set(Calendar.HOUR, 6);
            return cal.getTime();
        } else if (time.after(timeMap.get("18")) || time.equals(timeMap.get("18"))) {
            cal.set(Calendar.HOUR, 18);
            return cal.getTime();
        } else {
            return null;
        }*/
    }

    /*{
        "response": {
        "header": {
            "resultCode": "0000",
                    "resultMsg": "OK"
        },
        "body": {
            "items": {
                "item": {
                    "regId": "11B00000",
                    "wf3Am": "구름많음",
                    "wf3Pm": "구름많음",
                    "wf4Am": "구름많음",
                    "wf4Pm": "구름조금",
                    "wf5Am": "구름조금",
                    "wf5Pm": "구름많음",
                    "wf6Am": "구름많음",
                    "wf6Pm": "구름많음",
                    "wf7Am": "구름많음",
                    "wf7Pm": "구름많음",
                    "wf8": "구름많음",
                    "wf9": “구름많음”,
                    "wf10": "구름많음"
                }
            },
            "numOfRows": 10,
            "pageNo": 1,
            "totalCount": 1
        }
    }
    }*/
    private ArrayList<String> dailyTypeParser(Response response) throws IOException {
        //System.out.println(response.body().string());

        JsonElement je = new JsonParser().parse(response.body().charStream());
        JsonObject result = je.getAsJsonObject().getAsJsonObject("response");
        JsonObject header = result.getAsJsonObject("header");
        JsonObject body = result.getAsJsonObject("body");
        JsonObject items = body.getAsJsonObject("items");
        JsonObject item = items.getAsJsonObject("item");

        ArrayList<String> typeAL = new ArrayList<>();

        typeAL.add(item.get("wf3Pm").getAsString());
        typeAL.add(item.get("wf4Pm").getAsString());
        typeAL.add(item.get("wf5Pm").getAsString());
        typeAL.add(item.get("wf6Pm").getAsString());
        typeAL.add(item.get("wf7Pm").getAsString());
        typeAL.add(item.get("wf8").getAsString());
        typeAL.add(item.get("wf9").getAsString());
        typeAL.add(item.get("wf10").getAsString());

        return typeAL;
    }

    private ArrayList<HashMap<String, String>> dailyTempParser(Response response) throws IOException {
        //System.out.println(response.body().string());

        JsonElement je = new JsonParser().parse(response.body().charStream());
        JsonObject result = je.getAsJsonObject().getAsJsonObject("response");
        JsonObject header = result.getAsJsonObject("header");
        JsonObject body = result.getAsJsonObject("body");
        JsonObject items = body.getAsJsonObject("items");
        JsonObject item = items.getAsJsonObject("item");

        ArrayList<HashMap<String, String>> tempAL = new ArrayList<>();

        /*typeAL.add(item.get("wf3Pm").getAsString());
        typeAL.add(item.get("wf4Pm").getAsString());
        typeAL.add(item.get("wf5Pm").getAsString());
        typeAL.add(item.get("wf6Pm").getAsString());
        typeAL.add(item.get("wf7Pm").getAsString());
        typeAL.add(item.get("wf8").getAsString());
        typeAL.add(item.get("wf9").getAsString());
        typeAL.add(item.get("wf10").getAsString());*/

        return tempAL;
    }

    private void setToLongTermWeatherAdapter(ArrayList<String> dailyType, ArrayList<String> tempType) {
        for (int i = 0; i < dailyType.size(); i++) {
            LongTermForecast longTermForecast = new LongTermForecast();

            Calendar cal = Calendar.getInstance();
            // 3일 이후의 날씨부터 제공하기 때문에 오늘 날씨를 알고싶으면 3일 전(오후 6시)을 기준으로 요청한다.
            cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + i);
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            Date time = cal.getTime();

            String date = DateFormat.format("M월 d일", cal.getTime()).toString();

            longTermForecast.date = date;
            longTermForecast.type = dailyType.get(i);
            longTermWeatherAdapter.add(longTermForecast);
        }
    }

    private void jsonParser(Response response) throws IOException {
        JsonElement je = new JsonParser().parse(response.body().charStream());
        JsonObject result = je.getAsJsonObject().getAsJsonObject("response");
        JsonObject header = result.getAsJsonObject("header");
        JsonObject body = result.getAsJsonObject("body");
        JsonObject items = body.getAsJsonObject("items");
        JsonArray item = items.getAsJsonArray("item");

        LinkedHashMap<String, LinkedHashMap<String, DailyForecast>> dailyForecastMap = new LinkedHashMap();
        for (int i = 0; i < item.size(); i++) {
            System.out.println(item.get(i).getAsJsonObject());

            String date = item.get(i).getAsJsonObject().get("fcstDate").getAsString();
            String time = item.get(i).getAsJsonObject().get("fcstTime").getAsString();

            if(date.equals(" ")) continue;

            String T3H = "", SKY = "";
            String category = item.get(i).getAsJsonObject().get("category").getAsString();
            if (category.equals("T3H")) {
                T3H = item.get(i).getAsJsonObject().get("fcstValue").getAsString();
            } else if (category.equals("SKY")) {
                SKY = item.get(i).getAsJsonObject().get("fcstValue").getAsString();
            }

            //if(!T3H.isEmpty() && !SKY.isEmpty()) Log.d(TAG, date + " " + time + " - " + "T3H: " + T3H + " / " + "SKY: " + SKY);

            LinkedHashMap<String, DailyForecast> hourlyForecastMap;
            if (!dailyForecastMap.containsKey(date)) {
                //Log.d(TAG, "dailyForecastMap이 " + date + "에 대한 key를 가지고 있지 않기 때문에 새로운 hourlyForecastMap 객체를 생성합니다.");
                hourlyForecastMap = new LinkedHashMap<>();
            } else {
                //Log.d(TAG, "dailyForecastMap이 " + date + "에 대한 key를 가지고 있기 때문에 저장된 hourlyForecastMap 객체를 가져옵니다.");
                hourlyForecastMap = dailyForecastMap.get(date);
            }


            DailyForecast dailyForecast;
            if (!hourlyForecastMap.containsKey(time)) {
                dailyForecast = new DailyForecast();
                dailyForecast.date = date;
                dailyForecast.time = time;
            } else {
                dailyForecast = hourlyForecastMap.get(time);
            }

            if (!T3H.isEmpty()) {
                dailyForecast.T3H = T3H;
                Log.d(TAG, date + " " + time + " - " + "T3H: " + dailyForecast.T3H);
            } else if (!SKY.isEmpty()) {
                dailyForecast.SKY = SKY;
                Log.d(TAG, date + " " + time + " - " + "SKY: " + dailyForecast.SKY);
            }

            hourlyForecastMap.put(time, dailyForecast);
            dailyForecastMap.put(date, hourlyForecastMap);

            //String key = dailyForecastMap.keySet().toArray()[0].toString();
            //Log.d(TAG, "key: " + key);
            //dailyWeatherAdapter.add(dailyForecastMap.get(key));
        }

        for (Map.Entry<String, LinkedHashMap<String, DailyForecast>> entry : dailyForecastMap.entrySet()) {

            LinkedHashMap<String, DailyForecast> hourlyForecastMap = entry.getValue();

            for (Map.Entry<String, DailyForecast> hourlyForecastMapEntry : hourlyForecastMap.entrySet()) {
                dailyWeatherAdapter.add(hourlyForecastMapEntry.getValue());
            }
        }

    }



    @Override
    public void onResume() {
        super.onResume();
        longTermWeatherAdapter.clear();
        //dailyWeatherAdapter.clear();
        if(mCurrentLocation != null && address != null && typeRegId != null) {
            setTitleView(address);
            //getShortTerm(mCurrentLocation);
            getLongTermType(mCurrentLocation, this.typeRegId);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);

        // 넘겨받은 좌표값을 기준으로 한국 주소를 추출하고 주소가 추출되면 아래의 onAddressProvided 이벤트 리스너가 작동된다.
        weatherUtil.getKoreanAddress(location);
    }

    @Override
    public void onAddressProvided(HashMap<String, String> map, Location location) {
        Log.d(TAG, "onAddressProvided fired");

        // 멤버변수에 넣는 이유는 onResume 일 때 재사용하기 위함이다.
        this.address = map.get("town");
        this.typeRegId = map.get("typeRegId");
        this.tempRegId = map.get("tempRegId");

        setTitleView(this.address);
        //getShortTerm(location);
        getLongTermType(location, this.typeRegId);
        //get(location, this.address);
    }


}