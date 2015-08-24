package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jifalops.wsnlocalize.bluetooth.BtBeacon;
import com.jifalops.wsnlocalize.bluetooth.BtLeBeacon;
import com.jifalops.wsnlocalize.request.AbsRequest;
import com.jifalops.wsnlocalize.request.DeviceRequest;
import com.jifalops.wsnlocalize.request.RssiFilter;
import com.jifalops.wsnlocalize.request.RssiRequest;
import com.jifalops.wsnlocalize.wifi.WifiScanner;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *
 */
public class RssiActivity extends Activity {
    private static final int REQUEST_BT_ENABLE = 1;
    private static final int REQUEST_BT_DISCOVERABLE = 2;
    private static final int LOG_IMPORTANT = 1;
    private static final int LOG_INFORMATIVE = 2;
    private static final int LOG_ALL = 3;
    private static final String METHOD_BT = "BtBeacon";
    private static final String METHOD_BTLE = "BtLeBeacon";
    private static final String METHOD_WIFI = "WifiScan";


    private static class Device {
        final int id;
        String mac, desc;
        public Device(int id, String mac, String desc) {
            this.id = id;
            this.mac = mac;
            this.desc = desc;
        }
        @Override
        public String toString() {
            return id + ": " + desc + " " + mac;
        }
    }

    private TextView eventLogView, deviceLogView, collectedCountView, filteredCountView, toSendCountView;
    private Switch collectSwitch;

    private final List<Device> devices = new ArrayList<>();
    private final List<RssiRequest.RssiRecord> rssiRecords = new ArrayList<>();

    private int collectedCount, filteredCount, logLevel = LOG_IMPORTANT;
    private final List<Integer> deviceIds = new ArrayList<>();
    private float distance;
    private boolean collectEnabled;

    SharedPreferences prefs;

    private BtBeacon btBeacon;
    private BtLeBeacon btLeBeacon;
    private WifiScanner wifiScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssi);
        eventLogView = (TextView) findViewById(R.id.eventLog);
        deviceLogView = (TextView) findViewById(R.id.deviceLog);
        collectedCountView = (TextView) findViewById(R.id.collectedCount);
        filteredCountView = (TextView) findViewById(R.id.filteredCount);
        toSendCountView = (TextView) findViewById(R.id.toSendCount);
        collectSwitch = (Switch) findViewById(R.id.collectSwitch);

        final TextView deviceIdView = (TextView) findViewById(R.id.deviceId);
        final EditText distanceView = (EditText) findViewById(R.id.distanceMeters);
        Button sendButton = (Button) findViewById(R.id.sendButton);

        ((TextView) findViewById(R.id.wifiMacView)).setText(App.getInstance().getWifiMac());
        ((TextView) findViewById(R.id.btMacView)).setText(App.getInstance().getBtMac());

        autoScroll((ScrollView) findViewById(R.id.eventScrollView), eventLogView);
        autoScroll((ScrollView) findViewById(R.id.deviceScrollView), deviceLogView);
        deviceIdView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder b = new AlertDialog.Builder(RssiActivity.this);
                final EditText input = new EditText(RssiActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                input.setLayoutParams(lp);
//                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                b.setView(input);
                b.setTitle("Device IDs (comma separated)");
                b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deviceIdView.setText("");
                        deviceIds.clear();
                        try {
                            String[] ids = input.getText().toString().split(",");
                            int id;
                            for (String s : ids) {
                                id = Integer.valueOf(s);
                                Device d = devices.get(id - 1);
                                if (d != null) deviceIds.add(id);
                            }
                        } catch (Exception ignored) {}
                        boolean first = true;
                        for (int id : deviceIds) {
                            if (first) {
                                deviceIdView.append(id+"");
                                first = false;
                            } else {
                                deviceIdView.append(","+id);
                            }

                        }
                        if (deviceIds.size() == 0) {
                            deviceIdView.setText("0,0,0");
                        }
                    }
                });
                b.show();
            }
        });
        distanceView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    distance = Float.valueOf(s.toString());
                } catch (NumberFormatException e) {
                    distanceView.setText(distance + "");
                }
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<RssiRequest.RssiRecord> records = new ArrayList<>(rssiRecords);
                rssiRecords.clear();
                toSendCountView.setText("0");
                App.getInstance().sendRequest(new RssiRequest(records,
                        new Response.Listener<AbsRequest.MyResponse>() {
                            @Override
                            public void onResponse(AbsRequest.MyResponse response) {
                                if (response.responseCode == 200) {
                                    Toast.makeText(RssiActivity.this,
                                            "Sent " + records.size() + " records successfully",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(RssiActivity.this,
                                            response.responseCode + ": " + response.responseMessage +
                                                    " Result: " + response.queryResult,
                                            Toast.LENGTH_LONG).show();
                                    rssiRecords.addAll(records);
                                    toSendCountView.setText(rssiRecords.size() + "");
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(RssiActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                        rssiRecords.addAll(records);
                        toSendCountView.setText(rssiRecords.size()+"");
                    }
                }));
            }
        });

        prefs = getSharedPreferences("rssi", MODE_PRIVATE);
        btBeacon = BtBeacon.getInstance(this);
        btLeBeacon = BtLeBeacon.getInstance(this);
        wifiScanner = WifiScanner.getInstance(this);
    }

    private void autoScroll(final ScrollView sv, final TextView tv) {
        tv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sv.post(new Runnable() {
                    @Override
                    public void run() {
                        sv.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BT_ENABLE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Activity cannot work unless Bluetooth is enabled.",
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_BT_DISCOVERABLE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Activity cannot work unless device is discoverable.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SubMenu sub = menu.addSubMenu("Log Level");
        sub.add(1, LOG_IMPORTANT, 1, "Important").setCheckable(true).setChecked(true);
        sub.add(1, LOG_INFORMATIVE, 2, "Informative").setCheckable(true);
        sub.add(1, LOG_ALL, 3, "All").setCheckable(true);
        sub.setGroupCheckable(1, true, true);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case LOG_IMPORTANT:
                logLevel = LOG_IMPORTANT;
                return true;
            case LOG_INFORMATIVE:
                logLevel = LOG_INFORMATIVE;
                return true;
            case LOG_ALL:
                logLevel = LOG_ALL;
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        collectedCount = prefs.getInt("collected", 0);
        filteredCount = prefs.getInt("filtered", 0);
        deserializeRssiRecords(prefs.getString("toSend", ""));

        collectedCountView.setText(collectedCount+"");
        filteredCountView.setText(filteredCount+"");
        toSendCountView.setText(rssiRecords.size()+"");

        collectSwitch.setOnCheckedChangeListener(null);
        collectSwitch.setChecked(collectEnabled);
        collectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startCollection();
                } else {
                    stopCollection();
                }
            }
        });

        addEvent("Device is " + (btBeacon.isDiscoverable() ? "" : "not ") +
                "discoverable (may be inaccurate).", LOG_INFORMATIVE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.edit().putInt("collected", collectedCount)
                .putInt("filtered", filteredCount)
                .putString("toSend", serializeRssiRecords(rssiRecords))
                .apply();
        rssiRecords.clear();
        collectSwitch.setChecked(false);
    }

    private void addRecord(String localMac, String remoteMac, String remoteDesc,
                           String method, int rssi, int freq) {
        Device d = getDevice(remoteMac, remoteDesc);
        if (collectEnabled) {
            if (deviceIds.contains(d.id) && rssi != 0 && distance != 0) {
                String time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US).format(new Date());
                RssiRequest.RssiRecord record = new RssiRequest.RssiRecord(
                        localMac, remoteMac, remoteDesc, method, rssi, freq, distance, time);
                if (method.equals(METHOD_BT)) {
                    btFilter.add(record);
                } else if (method.equals(METHOD_BTLE)) {
                    btLeFilter.add(record);
                } else if (method.equals(METHOD_WIFI)) {
                    wifiFilter.add(record);
                }
                collectedCount++;
                collectedCountView.setText(collectedCount+"");
                addEvent("Device " + d.id + ": " + rssi + " dBm (" + freq + " MHz) at " +
                        distance + "m (" + method + ").", LOG_INFORMATIVE);
            } else {
                addEvent("Ignoring " + rssi + " dBm (" + freq + " MHz) for device " +
                        d.id + " (" + method + ").", LOG_ALL);
            }
        }
    }

    private Device getDevice(String mac, String desc) {
        Device device = null;
        for (Device d : devices) {
            if (d.mac.equals(mac)) {
                device = d;
                break;
            }
        }
        if (device == null) {
            device = new Device(devices.size()+1, mac, desc);
            devices.add(device);
            deviceLogView.append(device.toString() + "\n");
            addEvent("Found new device, " + device.id, LOG_INFORMATIVE);
        }
        return device;
    }

    private void addEvent(String event, int level) {
        if (level <= logLevel) {
            eventLogView.append(event + "\n");
        }
    }

    public void startCollection() {
        collectEnabled = true;
        btBeacon.registerListener(btBeaconListener);
        btBeacon.startBeaconing(this, REQUEST_BT_DISCOVERABLE);
        btLeBeacon.registerListener(btLeBeaconListener);
        btLeBeacon.startBeaconing(this, REQUEST_BT_ENABLE);
        wifiScanner.registerListener(wifiScanListener);
        wifiScanner.startScanning(1000);
    }

    public void stopCollection() {
        collectEnabled = false;
        btBeacon.stopBeaconing(this, REQUEST_BT_DISCOVERABLE);
        btBeacon.unregisterListener(btBeaconListener);
        btLeBeacon.stopBeaconing();
        btLeBeacon.unregisterListener(btLeBeaconListener);
        wifiScanner.stopScanning();
        wifiScanner.unregisterListener(wifiScanListener);
    }

    private String serializeRssiRecords(List<RssiRequest.RssiRecord> records) {
        JSONArray array = new JSONArray();
        for (RssiRequest.RssiRecord r : records) {
            array.put(r.toString());
        }
        return array.toString();
    }

    private void deserializeRssiRecords(String records) {
        List<RssiRequest.RssiRecord> list = new ArrayList<>();
        JSONArray array;
        try {
            array = new JSONArray(records);
            for (int i = 0, len = array.length(); i < len; i++) {
                list.add(new RssiRequest.RssiRecord(array.getString(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        rssiRecords.addAll(list);
        addEvent("Loaded " + list.size() + " records from memory.", LOG_INFORMATIVE);
    }

    private final BtBeacon.BtBeaconListener btBeaconListener = new BtBeacon.BtBeaconListener() {
        @Override
        public void onDeviceFound(BluetoothDevice device, short rssi) {
            addRecord(App.getInstance().getBtMac(), device.getAddress(),
                    device.getName() + " (Bluetooth)",
                    METHOD_BT, rssi, 2400);
        }

        @Override
        public void onThisDeviceDiscoverableChanged(boolean discoverable) {
            addEvent("BT Discoverability changed to " + discoverable, LOG_INFORMATIVE);
        }

        @Override
        public void onDiscoveryStarting() {
            addEvent("Scanning for BT devices...", LOG_ALL);
        }
    };

    private final BtLeBeacon.BtLeBeaconListener btLeBeaconListener = new BtLeBeacon.BtLeBeaconListener() {
        @Override
        public void onAdvertiseNotSupported() {
            addEvent("BT-LE advertisement not supported on this device.", LOG_IMPORTANT);
        }

        @Override
        public void onAdvertiseStartSuccess(AdvertiseSettings settingsInEffect) {
            addEvent("BT-LE advertising started at " +
                    settingsInEffect.getTxPowerLevel() + " dBm.", LOG_IMPORTANT);
        }

        @Override
        public void onAdvertiseStartFailure(int errorCode, String errorMsg) {
            addEvent("BT-LE advertisements failed to start (" + errorCode + "): " + errorMsg, LOG_IMPORTANT);
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            handleScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            addEvent("Received " + results.size() + " batch scan results (BtLeBeacon).", LOG_ALL);
            for (ScanResult sr : results) {
                handleScanResult(sr);
            }
        }

        @Override
        public void onScanFailed(int errorCode, String errorMsg) {
            addEvent("BT-LE scan failed (" + errorCode + "): " + errorMsg, LOG_IMPORTANT);
        }

        private void handleScanResult(ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device != null) {
                addRecord(App.getInstance().getBtMac(), device.getAddress(),
                        device.getName() + " (Bluetooth LE)",
                        METHOD_BTLE, result.getRssi(), 2400);
            } else {
                addEvent("BT-LE received " + result.getRssi() + " dBm from null device.", LOG_INFORMATIVE);
            }
        }
    };

    private final WifiScanner.ScanListener wifiScanListener = new WifiScanner.ScanListener() {
        @Override
        public void onScanResults(List<android.net.wifi.ScanResult> scanResults) {
            addEvent("WifiScan found " + scanResults.size() + " results.", LOG_ALL);
            for (android.net.wifi.ScanResult r : scanResults) {
                addRecord(App.getInstance().getWifiMac(), r.BSSID, r.SSID  +
                                " (WiFi " + r.frequency + "MHz)",
                        METHOD_WIFI, r.level, r.frequency);
            }
        }
    };

    private final RssiFilter.FilterCallback filterCallback =
            new RssiFilter.FilterCallback() {
        long lastTime = 0;
        @Override
        public void onRecordReady(RssiRequest.RssiRecord record, int recordsFiltered, long elapsedMillis) {
            if (lastTime == 0) lastTime = System.nanoTime();
            String elapsed = formatMillis((System.nanoTime() - lastTime) / 1_000_000);
            lastTime = System.nanoTime();
            rssiRecords.add(record);
            toSendCountView.setText(rssiRecords.size() + "");
            filteredCount += recordsFiltered;
            filteredCountView.setText(filteredCount+"");

            Device d = getDevice(record.remoteMac, record.remoteDesc);
            addEvent("#" + d.id + " queued " + record.rssi + " dBm " + record.rssiMethod
                    + ", filtered " + recordsFiltered + " in " + formatMillis(elapsedMillis)
                    + " (" + elapsed + ")", LOG_IMPORTANT);
        }
    };

    private String formatMillis(long millis) {
        return String.format(Locale.US, "%.1fs", ((double)millis)/1000);
    }

    private final RssiFilter btFilter = new RssiFilter(10000, 5, filterCallback);
    private final RssiFilter btLeFilter = new RssiFilter(10000, 20, filterCallback);
    private final RssiFilter wifiFilter = new RssiFilter(10000, 10, filterCallback);
}
