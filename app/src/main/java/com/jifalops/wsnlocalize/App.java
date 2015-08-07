package com.jifalops.wsnlocalize;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.jifalops.wsnlocalize.bluetooth.BtHelper;
import com.jifalops.wsnlocalize.request.AbsRequest;
import com.jifalops.wsnlocalize.request.DeviceRequest;
import com.jifalops.wsnlocalize.util.ServiceThreadApplication;
import com.jifalops.wsnlocalize.wifi.WifiHelper;

import org.json.JSONObject;

/**
 * App is the central area for managing global application state.
 */
public class App extends ServiceThreadApplication {
    public static final String NSD_SERVICE_PREFIX = "wsnloco_";
    public static final String WIFI_BEACON_SSID_PREFIX = "wsnloco_";

    private static App instance;
    public static App getInstance() {
        return instance;
    }

    private SharedPreferences prefs;
    private RequestQueue requestQueue;

    private String wifiMac, btMac;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        requestQueue = Volley.newRequestQueue(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        initWifiMac();
        initBtMac();
    }

    private void initWifiMac() {
        final WifiHelper wifi = WifiHelper.getInstance(this);
        wifiMac = wifi.getMacAddress();
        if (TextUtils.isEmpty(wifiMac)) {
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                    if (state == WifiManager.WIFI_STATE_ENABLED) {
                        wifiMac = wifi.getMacAddress();
                        unregisterReceiver(this);
                        tryDeviceRequest();
                    }
                }
            }, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
            wifi.setWifiEnabled(true);
        }
    }

    private void initBtMac() {
        final BtHelper bt = BtHelper.getInstance(this);
        btMac = bt.getMacAddress();
        if (TextUtils.isEmpty(btMac)) {
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    if (state == BluetoothAdapter.STATE_ON) {
                        btMac = bt.getMacAddress();
                        unregisterReceiver(this);
                        tryDeviceRequest();
                    }
                }
            }, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            bt.enableBluetooth();
        }
    }

    private void tryDeviceRequest() {
        if (!TextUtils.isEmpty(wifiMac) && !TextUtils.isEmpty(btMac)) {
            sendRequest(new DeviceRequest(wifiMac, btMac, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    //
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    //
                }
            }));
        }
    }

    public void sendRequest(AbsRequest request) {
        requestQueue.add(request);
    }

    public String getWifiMac() {
        return wifiMac;
    }

    public String getBtMac() {
        return btMac;
    }
}
