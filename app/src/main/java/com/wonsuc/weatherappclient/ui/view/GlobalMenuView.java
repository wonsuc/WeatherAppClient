package com.wonsuc.weatherappclient.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.wonsuc.weatherappclient.R;
import com.wonsuc.weatherappclient.ui.activity.BaseActivity;
import com.wonsuc.weatherappclient.ui.adapter.GlobalMenuAdapter;
import com.wonsuc.weatherappclient.ui.utils.CircleTransformation;


public class GlobalMenuView extends ListView implements View.OnClickListener, AdapterView.OnItemClickListener {

    private OnHeaderClickListener onHeaderClickListener;
    private OnMenuItemClickListener onMenuClickListener;
    private GlobalMenuAdapter globalMenuAdapter;
    private ImageView ivUserProfilePhoto;
    private TextView tvUserProfileName;
    private int avatarSize;
    private String profilePhoto;

    public GlobalMenuView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setChoiceMode(CHOICE_MODE_SINGLE);
        setDivider(getResources().getDrawable(android.R.color.transparent));
        setDividerHeight(0);
        setBackgroundColor(Color.WHITE);

        setupHeader();
        setupAdapter();
        setOnItemClickListener(this);
    }

    private void setupAdapter() {
        globalMenuAdapter = new GlobalMenuAdapter(getContext());
        setAdapter(globalMenuAdapter);
    }

    private void setupHeader() {
        this.avatarSize = getResources().getDimensionPixelSize(R.dimen.global_menu_avatar_size);
        this.profilePhoto = getResources().getString(R.string.user_profile_photo);

        setHeaderDividersEnabled(true);
        View vHeader = LayoutInflater.from(getContext()).inflate(R.layout.view_global_menu_header, null);
        ivUserProfilePhoto = (ImageView) vHeader.findViewById(R.id.ivUserProfilePhoto);
        Picasso.with(getContext())
                .load(R.drawable.img_global_menu_profile)
                .placeholder(R.drawable.img_circle_placeholder)
                .resize(avatarSize, avatarSize)
                .centerCrop()
                .transform(new CircleTransformation())
                .into(ivUserProfilePhoto);
        tvUserProfileName = (TextView) vHeader.findViewById(R.id.tvUserProfileName);
        tvUserProfileName.setText("Wonsuc Yoo");
        addHeaderView(vHeader);
        vHeader.setOnClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("onItemClick","Called.");
        if (onMenuClickListener != null) {
            onMenuClickListener.onGlobalMenuItemClick(parent, view, position, id);
        }
    }

    public interface OnMenuItemClickListener {
        public void onGlobalMenuItemClick(AdapterView<?> parent, View view, int position, long id);
    }

    public void setOnMenuClickListener(OnMenuItemClickListener onMenuClickListener) {
        this.onMenuClickListener = onMenuClickListener;
    }

    @Override
    public void onClick(View v) {
        if (onHeaderClickListener != null) {
            onHeaderClickListener.onGlobalMenuHeaderClick(v);
        }
    }

    public interface OnHeaderClickListener {
        public void onGlobalMenuHeaderClick(View v);
    }

    public void setOnHeaderClickListener(OnHeaderClickListener onHeaderClickListener) {
        this.onHeaderClickListener = onHeaderClickListener;
    }
}