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
import com.jifalops.wsnlocalize.data.EstimatorsHelper;
import com.jifalops.wsnlocalize.data.RssiHelper;
import com.jifalops.wsnlocalize.data.SamplesHelper;
import com.jifalops.wsnlocalize.request.AbsRequest;
import com.jifalops.wsnlocalize.request.MacRequest;
import com.jifalops.wsnlocalize.toolbox.ServiceThreadApplication;
import com.jifalops.wsnlocalize.toolbox.bluetooth.BtHelper;
import com.jifalops.wsnlocalize.toolbox.debug.DebugLog;
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
    public static final String DATA_SAMPLES = "samples";
    public static final String DATA_ESTIMATOR = "estimator";


    private static App instance;
    public static App getInstance() {
        return instance;
    }

    private SharedPreferences prefs;
    private RequestQueue requestQueue;

    private String wifiMac, btMac;

    private DebugLog log;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        requestQueue = Volley.newRequestQueue(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

//        initWifiMac();
//        initBtMac();
//        tryDeviceRequest();
        log = new DebugLog(this);

        // Load data in files
        RssiHelper.getInstance();
        SamplesHelper.getInstance();
        EstimatorsHelper.getInstance();
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




    public static DebugLog log() { return instance.log; }

    /**
     *
     */
    public static class Files {
        private Files() {}
        public static final String DIR_SAMPLES = "samples";
        public static final String DIR_ESTIMATORS = "estimators";
        public static final String EXT_RSSI = "csv";
        public static final String EXT_SAMPLES = "csv";
        public static final String EXT_ESTIMATORS = "json";

        public static File getDataDir() {
            return instance.getExternalFilesDir(null);
        }

        public static File getRssiFile(String signalType) {
            return new File(getDataDir(), signalType + "-rssi.csv");
        }

        public static File getSamplesDir() {
            File f = new File(getDataDir(), DIR_SAMPLES);
            if (!f.exists()) {
                if (!f.mkdirs()) instance.log.e("failed to make samples dir.");
            }
            return f;
        }

        public static File getEstimatorsDir() {
            File f = new File(getDataDir(), DIR_ESTIMATORS);
            if (!f.exists()) {
                if (!f.mkdirs()) instance.log.e("failed to make estimators dir.");
            }
            return f;
        }
    }
}
