package com.jifalops.wsnlocalize;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.jifalops.wsnlocalize.data.helper.InfoFileHelper;
import com.jifalops.wsnlocalize.data.helper.RssiHelper;
import com.jifalops.wsnlocalize.data.helper.SamplesHelper;
import com.jifalops.wsnlocalize.request.AbsRequest;
import com.jifalops.wsnlocalize.request.MacRequest;
import com.jifalops.wsnlocalize.toolbox.ServiceThreadApplication;
import com.jifalops.wsnlocalize.toolbox.bluetooth.BtHelper;
import com.jifalops.wsnlocalize.toolbox.debug.DebugLog;
import com.jifalops.wsnlocalize.toolbox.util.Arrays;
import com.jifalops.wsnlocalize.toolbox.wifi.WifiHelper;

import java.io.File;
import java.io.FilenameFilter;
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

    public static final String NN_PSO = "pso";
    public static final String NN_DE = "de";
    public static final String NN_DEPSO = "depso";

    public static final String DATA_RSSI = "rssi";
    public static final String DATA_SAMPLES = "samples";
    public static final String DATA_ESTIMATORS = "estimators";


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

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(InfoFileHelper.ACTION_LOADED)) {
                    SamplesHelper.getInstance();
                    OldEstimatorsHelper.getInstance();
                }
            }
        }, new IntentFilter(InfoFileHelper.ACTION_LOADED));

        // Load data in files
        RssiHelper.getInstance();
        InfoFileHelper.getInstance();
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

    public static void broadcast(String intentAction) {
        LocalBroadcastManager.getInstance(instance).sendBroadcast(
                new Intent(intentAction));
    }

    /**
     *
     */
    public static class Files {
        private Files() {}

        public static File getDataDir() {
            return instance.getExternalFilesDir(null);
        }

        public static File getDataDir(String signalType) {
            File f = new File(instance.getExternalFilesDir(null), signalType + "-data");
            if (!f.exists()) {
                if (!f.mkdirs()) instance.log.e("Failed to make dirs for " + f.getName());
            }
            return f;
        }

        public static File getRssiFile(String signalType) {
            return new File(getDataDir(), signalType + "-rssi.csv");
        }

        public static File getInfoFile(String signalType) {
            return new File(getDataDir(signalType), "_info.csv");
        }

        public static File getSamplesFile(String signalType, int id) {
            return new File(getDataDir(signalType), id + "-samples.csv");
        }

        public static File getEstimatorsFile(String signalType, String nnType, int id, boolean timed) {
            return new File(getDataDir(signalType), id + "-" + nnType + "-estimators" +
                    (timed ? "-timed" : "-untimed") + ".csv");
        }

        public static File getEstimatesFile(String signalType, String nnType, int id,
                                            int numEstimators, boolean timed) {
            return new File(getDataDir(signalType), id + "-" + nnType + "-estimates-" +
                    numEstimators + (timed ? "-timed" : "-untimed") + ".csv");
        }

        public static int[] getEstimatesFilesNumEstimators(String signalType, final int id) {
            final List<Integer> numEstimators = new ArrayList<>();
            getDataDir(signalType).list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    String[] parts = filename.split("-");
                    if (parts.length == 5 &&
                        Integer.valueOf(parts[0]) == id &&
                            parts[2].equals("estimates")) {
                        numEstimators.add(Integer.valueOf(parts[3]));
                    }
                    return false;
                }
            });
            return Arrays.toPrimitive(numEstimators.toArray(new Integer[numEstimators.size()]));
        }
    }
}
