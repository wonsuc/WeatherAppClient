package com.wonsuc.weatherappclient.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wonsuc.weatherappclient.R;
import com.wonsuc.weatherappclient.model.DailyForecast;
import com.wonsuc.weatherappclient.model.LongTermForecast;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class LongTermWeatherAdapter extends ArrayAdapter<LongTermForecast> {

    private final Context context;
    private final LayoutInflater inflater;
    private final List<LongTermForecast> dataItems = new ArrayList<>();

    public LongTermWeatherAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void add(LongTermForecast object) {
        super.add(object);
        dataItems.add(object);
    }

    @Override
    public LongTermForecast getItem(int position) {
        return dataItems.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LongTermForecastViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.view_weather_daily_list_item, parent, false);
            holder = new LongTermForecastViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (LongTermForecastViewHolder) convertView.getTag();
        }

        LongTermForecast item = getItem(position);

        //for (Map.Entry<String, DailyForecast> entry : item.entrySet()) {
            holder.itemLabel.setText(item.date);
            holder.itemValue.setText(item.type);
        //}
        return convertView;
    }

    public static class LongTermForecastViewHolder {
        @InjectView(R.id.itemLabel)
        TextView itemLabel;
        @InjectView(R.id.itemValue)
        TextView itemValue;

        public LongTermForecastViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
