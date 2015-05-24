package com.wonsuc.weatherappclient.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wonsuc.weatherappclient.R;
import com.wonsuc.weatherappclient.model.DailyForecast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class DailyWeatherAdapter extends ArrayAdapter<DailyForecast> {

    private final Context context;
    private final LayoutInflater inflater;
    private final List<DailyForecast> dataItems = new ArrayList<>();

    public DailyWeatherAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void add(DailyForecast object) {
        super.add(object);
        dataItems.add(object);
    }

    @Override
    public DailyForecast getItem(int position) {
        return dataItems.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DailyForecastViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.view_weather_daily_list_item, parent, false);
            holder = new DailyForecastViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (DailyForecastViewHolder) convertView.getTag();
        }

        DailyForecast item = getItem(position);

        //for (Map.Entry<String, DailyForecast> entry : item.entrySet()) {
            holder.itemLabel.setText(item.date + " " + item.time);
            holder.itemValue.setText(item.T3H);
        //}
        return convertView;
    }

    public static class DailyForecastViewHolder {
        @InjectView(R.id.itemLabel)
        TextView itemLabel;
        @InjectView(R.id.itemValue)
        TextView itemValue;

        public DailyForecastViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
