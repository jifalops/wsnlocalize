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
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.jifalops.wsnlocalize.bluetooth.BtHelper;
import com.jifalops.wsnlocalize.request.AbsRequest;
import com.jifalops.wsnlocalize.request.MacRequest;
import com.jifalops.wsnlocalize.util.ServiceThreadApplication;
import com.jifalops.wsnlocalize.wifi.WifiHelper;

import java.util.ArrayList;
import java.util.List;

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
        tryDeviceRequest();
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
            List<MacRequest.Mac> macs = new ArrayList<>(2);
            macs.add(new MacRequest.Mac(wifiMac, "WiFi"));
            macs.add(new MacRequest.Mac(btMac, "Bluetooth"));
            sendRequest(new MacRequest(macs, new Response.Listener<AbsRequest.MyResponse>() {
                @Override
                public void onResponse(AbsRequest.MyResponse response) {
//                    if (response.responseCode != 200 && response.responseCode != 31) {
                        Toast.makeText(App.this,
                                response.responseCode + ": " + response.responseMessage +
                                        ". Result: " + response.queryResult,
                                Toast.LENGTH_LONG).show();
                    }
//                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Toast.makeText(App.this, volleyError.toString(), Toast.LENGTH_LONG).show();
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
