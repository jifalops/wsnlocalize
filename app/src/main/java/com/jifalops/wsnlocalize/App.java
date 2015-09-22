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
import com.jifalops.wsnlocalize.request.AbsRequest;
import com.jifalops.wsnlocalize.request.MacRequest;
import com.jifalops.wsnlocalize.toolbox.ServiceThreadApplication;
import com.jifalops.wsnlocalize.toolbox.bluetooth.BtHelper;
import com.jifalops.wsnlocalize.toolbox.util.ResettingList;
import com.jifalops.wsnlocalize.toolbox.wifi.WifiHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * App is the central area for managing global application state.
 */
public class App extends ServiceThreadApplication {
    public static final String NSD_SERVICE_PREFIX = "wsnloco_";
    public static final String WIFI_BEACON_SSID_PREFIX = "wsnloco_";

    public static final String SIGNAL_BT = "bt";
    public static final String SIGNAL_BTLE = "btle";
    public static final String SIGNAL_WIFI = "wifi";
    public static final String SIGNAL_WIFI5G = "wifi5g";

    public static final String DATA_RSSI = "rssi";
    public static final String DATA_WINDOW = "windows";
    public static final String DATA_ESTIMATOR = "estimator";
    public static final String DATA_BEST_ESTIMATOR = "best-estimator";
    public static final String DATA_SAMPLES = "samples";

    public static final ResettingList.Trigger btWindowTrigger   = new ResettingList.Trigger(3, 10_000, 5, 120_000);
//    public static final ResettingList.Trigger btTrainTrigger    = new ResettingList.Trigger(2, 30_000, 10, 120_000);
    public static final ResettingList.Trigger btleWindowTrigger = new ResettingList.Trigger(15, 5_000, 20, 30_000);
//    public static final ResettingList.Trigger btleTrainTrigger  = new ResettingList.Trigger(3, 30_000, 10, 120_000);
    public static final ResettingList.Trigger wifiWindowTrigger = new ResettingList.Trigger(5, 5_000, 20, 20_000);
//    public static final ResettingList.Trigger wifiTrainTrigger  = new ResettingList.Trigger(3, 30_000, 10, 1200_000);

    private static App instance;
    public static App getInstance() {
        return instance;
    }

    private SharedPreferences prefs;
    private RequestQueue requestQueue;

    private String wifiMac, btMac;
    private boolean debug;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        requestQueue = Volley.newRequestQueue(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        debug = prefs.getBoolean("debug", false);
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
                    if (response.responseCode != 200 && response.responseCode != 31) {
                        Toast.makeText(App.this,
                                response.responseCode + ": " + response.responseMessage +
                                        ". Result: " + response.queryResult,
                                Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Toast.makeText(App.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                }
            }));
        }
    }

    public static void sendRequest(AbsRequest request) {
        instance.requestQueue.add(request);
    }

    public static String getWifiMac() {
        return instance.wifiMac;
    }
    public static String getBtMac() {
        return instance.btMac;
    }


    public static String getFileName(String signalType, String dataType) {
        String ext = ".csv";
        if (DATA_ESTIMATOR.equals(dataType) || DATA_BEST_ESTIMATOR.equals(dataType)) ext = ".json";
        return signalType + "-" + dataType + ext;
    }

    public static File getDataDir() {
        return instance.getExternalFilesDir(null);
    }

    public static File getFile(String signalType, String dataType) {
        return new File(getDataDir(), getFileName(signalType, dataType));
    }

    public static void showToast(CharSequence msg, boolean lengthLong) {
        Toast.makeText(instance, msg, lengthLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    public static boolean debug() { return instance.debug; }
    public static void setDebugMode(boolean debug) {
        instance.debug = debug;
        instance.prefs.edit().putBoolean("debug", debug).apply();
    }
}
