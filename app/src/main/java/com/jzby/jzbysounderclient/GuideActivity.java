package com.jzby.jzbysounderclient;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Spinner;

import com.jzby.jzbysounderclient.adapter.WifiAdapter;
import com.jzby.jzbysounderclient.util.Tools;

import java.util.ArrayList;
import java.util.List;


public class GuideActivity extends BaseActivity implements View.OnClickListener {
    final static String TAG = GuideActivity.class.getSimpleName();

    WifiManager mWifiManager;

    WiFiBroadcastReceiver mWiFiBroadcastReceiver;

    Spinner mSpinner;

    List<String> mWifiList=null;

    int nowNetworkId = -1,mCurrentPosition=0;

    String mCurrentRssId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_guide);

        mWiFiBroadcastReceiver = new WiFiBroadcastReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        registerReceiver(mWiFiBroadcastReceiver,filter);

        mSpinner=(Spinner)findViewById(R.id.sp_wifi);

        findViewById(R.id.btn_sounder_connect).setOnClickListener(this);
        findViewById(R.id.btn_prev).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},10000);
        }

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
            mWifiManager.setWifiEnabled(true);
        }

        WifiInfo info = mWifiManager.getConnectionInfo();
        Log.i(TAG, "============" + info.toString());
        nowNetworkId = info.getNetworkId();

        mCurrentRssId = info.getSSID();

        mWifiList=new ArrayList<String>();

        mWifiManager.startScan();

        //mWifiManager.enableNetwork(info.getNetworkId(),true);

        //mWifiManager.disconnect()
    }


    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            Log.i(TAG, "====handleMessage=====" + message.what);
           // mWifiManager.enableNetwork(nowNetworkId, true);

            WifiAdapter adapter=new WifiAdapter(GuideActivity.this,mWifiList,R.layout.item_wifi);
            mSpinner.setAdapter(adapter);
            mSpinner.setSelection(mCurrentPosition);

            return false;
        }
    });

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        Log.i(TAG,"=====onRequestPermissionsResult()=====");

        switch (requestCode) {
            case 10000:

                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.i(TAG,"====PERMISSION_DENIED=======");
                        showTextToast(getString(R.string.tips_permission));
                    }
                }
                break;
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_sounder_connect:

                mWifiManager.disconnect();

                WifiConfiguration configuration = new WifiConfiguration();
                configuration.SSID = '"' + "iPhone 8" + '"';
                //configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                configuration.preSharedKey = '"' + "11300115" + '"';
                int netId = mWifiManager.addNetwork(configuration);
                Log.i(TAG, "====newNetworkId=====" + netId);
                mWifiManager.enableNetwork(netId, true);

                break;
            case R.id.btn_prev:
                findViewById(R.id.ll_step_1).setVisibility(View.VISIBLE);
                findViewById(R.id.ll_step_2).setVisibility(View.GONE);

                break;
            case R.id.btn_next:

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                        showTextToast(getString(R.string.tips_permission));
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 10000);
                        return;
                    }

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                        showTextToast(getString(R.string.tips_permission));
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 10000);
                        return;
                    }
                }

                findViewById(R.id.ll_step_1).setVisibility(View.GONE);
                findViewById(R.id.ll_step_2).setVisibility(View.VISIBLE);
                break;
        }
    }

    class WiFiBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "======onReceive()=======" + action);
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equalsIgnoreCase(action)) {

            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equalsIgnoreCase(action)) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.i(TAG, "==========" + info.getState() + "=========" + info.getDetailedState());
                if (NetworkInfo.State.CONNECTED == info.getState()) {

                    DhcpInfo dhcpInfo=mWifiManager.getDhcpInfo();
                    WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                    Log.i(TAG,wifiInfo.getSSID()+"======="+Tools.longToIP(wifiInfo.getIpAddress())+"======"+Tools.longToIP(dhcpInfo.gateway));
                }

            }
            else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action))
            {
                List<ScanResult> results = mWifiManager.getScanResults();
                Log.i(TAG, "=======SCAN_RESULTS_AVAILABLE_ACTION=======" + results.size());
                for (int i = 0; i < results.size(); i++) {
                    ScanResult result = results.get(i);
                    Log.i(TAG, result.BSSID + "=======" + result.SSID);
                    if(!TextUtils.isEmpty(result.SSID))
                    {
                        mWifiList.add(result.SSID);
                        if(TextUtils.isEmpty(mCurrentRssId) && ('"'+result.SSID+'"').equalsIgnoreCase(mCurrentRssId))
                        {
                            mCurrentPosition=i;
                        }
                    }
                }
                Log.i(TAG,"=========size====" + mWifiList.size()+"====position==="+mCurrentPosition);
                mHandler.sendEmptyMessage(10000);

            } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equalsIgnoreCase(action)) {

            }


        }
    }
}
