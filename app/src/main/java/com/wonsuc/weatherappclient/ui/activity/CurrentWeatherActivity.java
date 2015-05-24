package com.wonsuc.weatherappclient.ui.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
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
import com.wonsuc.weatherappclient.utils.WeatherUtil;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CurrentWeatherActivity extends BaseActivity implements WeatherUtil.OnAddressProviderListener {

    // http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService/ForecastGrib?ServiceKey=U%2F6locvWMaaYvTFLXqyMJvAW%2FeRXl8iFCpKULs%2BAFLh3DepTcLoeDlNpg1EY2rb%2FHVRpXNZMgoPTFsJZJpLjaA%3D%3D&region=5%EC%9B%94&base_date=20121212&base_time=0800&nx=130&ny=160&_type=json

    private static final String TAG = CurrentWeatherActivity.class.getSimpleName();

    private final HttpClient httpClient = new HttpClient();
    private WeatherUtil weatherUtil = new WeatherUtil();

    private final String ServiceKey = "U%2F6locvWMaaYvTFLXqyMJvAW%2FeRXl8iFCpKULs%2BAFLh3DepTcLoeDlNpg1EY2rb%2FHVRpXNZMgoPTFsJZJpLjaA%3D%3D";
    private String BaseDate = "";
    private String BaseTime = "";
    private String NX = "";
    private String NY = "";

    private String address;

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

    ArrayAdapter<JsonObject> jsonAdapter;

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

        if (savedInstanceState == null) {
        } else {
        }

        jsonAdapter = new ArrayAdapter<JsonObject>(this, 0) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                JsonObject item = getItem(position);

                String jLabel = null;
                String jValue = null;
                String jUnit = null;

                if (convertView == null)
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_weather_current_list_item, parent, false);

                if(item.has("category") && item.has("obsrValue")) {
                    jLabel = CategoryValue.valueOf(item.get("category").getAsString()).getText();
                    switch(item.get("category").getAsString()) {
                        case "SKY":
                            String[] SKY = {"", "맑음", "구름조금", "구름많음", "흐림"};
                            jValue = SKY[item.get("obsrValue").getAsInt()];
                            break;
                        case "PTY":
                            String[] PTY = {"없음", "비", "비/눈", "눈"};
                            jValue = PTY[item.get("obsrValue").getAsInt()];
                            break;
                        case "LGT":
                            String[] LGT = {"없음", "있음"};
                            jValue = LGT[item.get("obsrValue").getAsInt()];
                            break;
                        default:
                            jUnit = ObsrValue.valueOf(item.get("category").getAsString()).getText();
                            jValue = item.get("obsrValue").getAsString() + jUnit;
                    }
                }

                TextView itemLabel = ButterKnife.findById(convertView, R.id.itemLabel);
                TextView itemValue = ButterKnife.findById(convertView, R.id.itemValue);

                itemLabel.setText(jLabel);
                itemValue.setText(jValue);

                return convertView;
            }
        };
        weatherCurrentListView.setAdapter(jsonAdapter);

        // 이렇게 함으로써 비로서 WeatherUtil 객체에서 메인에서 입력된 메서드들을 호출할 수 있다.
        weatherUtil.setOnAddressProviderListener(this);
    }


    private void get(Location location, String address) {
        // 타이틀 영역의 날짜와 시간, 현재 위치 정보 표시
        String TitleDate = DateFormat.format("yyyy년 MM월 dd일 H시", Calendar.getInstance().getTime()).toString();
        weatherCurrentListTitleTextView.setText(TitleDate + ", " + address + "의 날씨 정보");

        // http 클라이언트
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) - 30);
        BaseDate = DateFormat.format("yyyyMMdd", cal.getTime()).toString();
        BaseTime = DateFormat.format("HH00", cal.getTime()).toString();

        HashMap<String, Double> locationMap = weatherUtil.convXY("toXY", location.getLatitude(), location.getLongitude());
        NX = String.valueOf(locationMap.get("x").intValue());
        NY = String.valueOf(locationMap.get("y").intValue());

        Log.v(TAG, "NX: " + NX + ", NY: " + NY);

        String url = "http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService/ForecastGrib";
        StringBuilder sb = new StringBuilder();
        String params = sb.append("ServiceKey=").append(ServiceKey)
                .append("&base_date=").append(BaseDate)
                .append("&base_time=").append(BaseTime)
                .append("&nx=").append(NX)
                .append("&ny=").append(NY)
                .append("&_type=").append("json").toString();

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

    private void jsonParser(Response response) throws IOException {
        JsonElement je = new JsonParser().parse(response.body().charStream());
        JsonObject result = je.getAsJsonObject().getAsJsonObject("response");
        JsonObject header = result.getAsJsonObject("header");
        JsonObject body = result.getAsJsonObject("body");
        JsonObject item = body.getAsJsonObject("items");
        JsonArray items = item.getAsJsonArray("item");

        for (int i = 0; i < items.size(); i++) {
            System.out.println(items.get(i).getAsJsonObject());
            jsonAdapter.add(items.get(i).getAsJsonObject());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        jsonAdapter.clear();
        if(mCurrentLocation != null && address != null) get(mCurrentLocation, address);
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
        get(location, this.address);
    }


}