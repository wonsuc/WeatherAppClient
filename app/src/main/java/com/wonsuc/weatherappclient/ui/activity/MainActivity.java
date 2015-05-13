package com.wonsuc.weatherappclient.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.wonsuc.weatherappclient.R;
import com.wonsuc.weatherappclient.common.HttpClient;

import org.json.JSONArray;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;
import hugo.weaving.DebugLog;

public class MainActivity extends BaseActivity {

    // http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService/ForecastGrib?ServiceKey=U%2F6locvWMaaYvTFLXqyMJvAW%2FeRXl8iFCpKULs%2BAFLh3DepTcLoeDlNpg1EY2rb%2FHVRpXNZMgoPTFsJZJpLjaA%3D%3D&region=5%EC%9B%94&base_date=20121212&base_time=0800&nx=130&ny=160&_type=json

    private final HttpClient httpClient = new HttpClient();

    private final String ServiceKey = "U%2F6locvWMaaYvTFLXqyMJvAW%2FeRXl8iFCpKULs%2BAFLh3DepTcLoeDlNpg1EY2rb%2FHVRpXNZMgoPTFsJZJpLjaA%3D%3D";
    //private final String BaseDate = "20150509";
    //private final String BaseTime = "1800";
    private String BaseDate = "";
    private String BaseTime = "";
    private final String NX = "62";
    private final String NY = "122";

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

        get();
    }

    private void get() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) - 30);
        BaseDate = DateFormat.format("yyyyMMdd", cal.getTime()).toString();
        BaseTime = DateFormat.format("HH00", cal.getTime()).toString();

        String TitleDate = DateFormat.format("yyyy년 MM월 dd일 H시", Calendar.getInstance().getTime()).toString();

        weatherCurrentListTitleTextView.setText(TitleDate + ", 정자동의 날씨 정보");

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
                    new Callable<Void>() {

                        @Override
                        public Void call() throws Exception {
                            return null;
                        }
                    },
                    new Callable<Void>() {

                        @Override
                        public Void call() throws Exception {
                            Response response = httpClient.response;
                            if(response.isSuccessful()) {
                                jsonParser(response);
                            }else{
                                System.out.println(response);
                            }
                            return null;
                        }
                    });
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        jsonAdapter.clear();
        get();
    }
}