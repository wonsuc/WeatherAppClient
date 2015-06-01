package com.wonsuc.weatherappclient.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.wonsuc.weatherappclient.R;
import com.wonsuc.weatherappclient.application.WeatherApplication;
import com.wonsuc.weatherappclient.ui.adapter.GlobalMenuAdapter;
import com.wonsuc.weatherappclient.ui.utils.DrawerLayoutInstaller;
import com.wonsuc.weatherappclient.ui.utils.GlobalUiUtils;
import com.wonsuc.weatherappclient.ui.view.GlobalMenuView;

import java.text.DateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class BaseActivity extends ActionBarActivity
        implements GlobalMenuView.OnHeaderClickListener, GlobalMenuView.OnMenuItemClickListener,
        LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = BaseActivity.class.getSimpleName();

    @Optional
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    private MenuItem inboxMenuItem;
    private DrawerLayout drawerLayout;
    private GlobalMenuAdapter globalMenuAdapter;

    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.inject(this);
        setupToolbar();
        if (shouldInstallDrawer()) {
            setupDrawer();
        }

        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    protected void setupToolbar() {
        if (toolbar != null) {
            toolbar.setTitleTextColor(0xFFFFFFFF);
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_menu_white);
            //getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    protected boolean shouldInstallDrawer() {
        return true;
    }

    private void setupDrawer() {
        globalMenuAdapter = new GlobalMenuAdapter(this);

        GlobalMenuView menuView = new GlobalMenuView(this);
        menuView.setOnHeaderClickListener(this);
        menuView.setOnMenuClickListener(this);

        drawerLayout = DrawerLayoutInstaller.from(this)
                .drawerRoot(R.layout.drawer_root)
                .drawerLeftView(menuView)
                .drawerLeftWidth(GlobalUiUtils.dpToPx(300))
                .withNavigationIconToggler(getToolbar())
                .build();
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public void onGlobalMenuHeaderClick(View v) {
        drawerLayout.closeDrawer(Gravity.START);
    }

    @Override
    public void onGlobalMenuItemClick(AdapterView<?> parent, View view, int position, long id) {
        drawerLayout.closeDrawer(Gravity.START);

        //globalMenuAdapter.selectedItem = 50;
        //Log.d(TAG, "globalMenuAdapter.selectedItem: " + globalMenuAdapter.selectedItem);

        GlobalMenuAdapter.GlobalMenuItem menuItem = (GlobalMenuAdapter.GlobalMenuItem) parent.getItemAtPosition(position);

        WeatherApplication weatherApplication = (WeatherApplication) this.getApplicationContext();

        if(menuItem.equals(weatherApplication.selectedGlobalMenuItem)) {
            //Log.d(TAG, "이미 선택된 메뉴를 터치했습니다.");
            //return;
        };

        Log.d(TAG, "menuItem: " + menuItem);
        Log.d(TAG, "weatherApplication.selectedGlobalMenuItem: " + weatherApplication.selectedGlobalMenuItem);

        weatherApplication.selectedGlobalMenuItem = menuItem;

        String label = menuItem.label;
        //Toast.makeText(getContext(), label, Toast.LENGTH_SHORT).show();

        if(label.equals("오늘 날씨")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(BaseActivity.this, DailyWeatherActivity.class);
                    BaseActivity.this.startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }, 200);
        } else if(label.equals("현재 날씨")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(BaseActivity.this, CurrentWeatherActivity.class);
                    BaseActivity.this.startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }, 200);
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;

        //mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        //updateUI();
        //mGoogleApiClient.disconnect();

        Log.v(TAG, location.getLatitude() + ", " + location.getLongitude());

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void updateUI() {
        Log.d(TAG, "UI update initiated .............");
        if (null != mCurrentLocation) {
            String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lng = String.valueOf(mCurrentLocation.getLongitude());
            // At Time: mLastUpdateTime
            // Latitude: lat
            // Longitude: lng
            // Accuracy:  mCurrentLocation.getAccuracy()
            // Provider: mCurrentLocation.getProvider()
        } else {
            Log.d(TAG, "location is null ...............");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    // 현재 Activity가 Foreground로 복귀할 때마다 Location을 Update 한다.
    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onBackPressed() {
        backButtonHandler();
    }

    public void backButtonHandler() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        // 다이얼로그의 타이틀
        alertDialog.setTitle("어플리케이션 종료");
        // 다이얼로그의 메세지
        alertDialog.setMessage("어플리케이션을 정말로 종료하시겠습니까?");
        // 다이얼로그에 아이콘을 설정하고 싶을 때는 다음을 추가한다.
        //alertDialog.setIcon(R.drawable.dialog_icon);
        // "네"를 터치했을 때.
        alertDialog.setPositiveButton("네",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        // "아니오"를 터치했을 때.
        alertDialog.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NO event
                        dialog.cancel();
                    }
                });
        // 다이얼로그를 보여준다.
        alertDialog.show();
    }
}
