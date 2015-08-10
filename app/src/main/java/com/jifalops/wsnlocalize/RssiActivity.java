package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jifalops.wsnlocalize.bluetooth.BtBeacon;
import com.jifalops.wsnlocalize.bluetooth.BtLeBeacon;
import com.jifalops.wsnlocalize.request.AbsRequest;
import com.jifalops.wsnlocalize.request.RssiRequest;
import com.jifalops.wsnlocalize.wifi.WifiScanner;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RssiActivity extends Activity {
    private static final int REQUEST_BT_ENABLE = 1;
    private static final int REQUEST_BT_DISCOVERABLE = 2;

    private static class Device {
        final int id;
        String wifiMac, btMac, name;
        public Device(int id, String name, String wifiMac, String btMac) {
            this.id = id;
            this.wifiMac = wifiMac;
            this.btMac = btMac;
            this.name = name;
        }
        @Override
        public String toString() {
            return id + ": " + name + " " + wifiMac + " " + btMac;
        }
    }

    private TextView eventLogView, deviceLogView, collectedCountView, toSendCountView;
    private Switch collectSwitch;

    private final List<Device> devices = new ArrayList<>();
    private final List<RssiRequest.RssiRecord> rssiRecords = new ArrayList<>();

    private int deviceId, collectedCount;
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
        toSendCountView = (TextView) findViewById(R.id.toSendCount);
        collectSwitch = (Switch) findViewById(R.id.collectSwitch);

        final EditText deviceIdView = (EditText) findViewById(R.id.deviceId);
        final EditText distanceView = (EditText) findViewById(R.id.distanceMeters);
        Button sendButton = (Button) findViewById(R.id.sendButton);

        ((TextView) findViewById(R.id.wifiMacView)).setText(App.getInstance().getWifiMac());
        ((TextView) findViewById(R.id.btMacView)).setText(App.getInstance().getBtMac());

        autoScroll((ScrollView) findViewById(R.id.eventScrollView), eventLogView);
        autoScroll((ScrollView) findViewById(R.id.deviceScrollView), deviceLogView);
        deviceIdView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int id = Integer.valueOf(s.toString());
                    Device d = devices.get(id - 1);
                    if (d != null) deviceId = id;
                } catch (Exception e) {
                    if (deviceId != 0) {
                        deviceId = 0;
                        deviceIdView.setText("0");
                    }
                }
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
        collectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                collectEnabled = isChecked;
                if (isChecked) {
                    startCollection();
                } else {
                    stopCollection();
                }
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<RssiRequest.RssiRecord> records = new ArrayList<>(rssiRecords);
                rssiRecords.clear();
                App.getInstance().sendRequest(new RssiRequest(records,
                        new Response.Listener<AbsRequest.MyResponse>() {
                            @Override
                            public void onResponse(AbsRequest.MyResponse response) {
                                if (response.responseCode == 200) {

                                } else {
                                    Toast.makeText(RssiActivity.this,
                                            response.responseCode + ": " + response.responseMessage +
                                                    " Result: " + response.queryResult,
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(RssiActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
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
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        collectedCount = prefs.getInt("collected", 0);
        deserializeRssiRecords(prefs.getString("toSend", ""));
        updateCountViews();
        addEvent("Device is " + (btBeacon.isDiscoverable() ? "" : "not ") +
                "discoverable (may be inaccurate).");
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.edit().putInt("collected", collectedCount)
                .putString("toSend", serializeRssiRecords(rssiRecords))
                .apply();
        rssiRecords.clear();
        collectSwitch.setChecked(false);
    }

    private void addRecord(String name, String wifiMac, String btMac,
                           String method, float rssi, float freq) {
        Device d = getDevice(name, wifiMac, btMac);
        if (collectEnabled) {
            if (d.id == deviceId) {
                RssiRequest.RssiRecord record = new RssiRequest.RssiRecord(
                        App.getInstance().getWifiMac(), App.getInstance().getBtMac(),
                        d.wifiMac, d.btMac, d.name, method, rssi, freq, distance);
                rssiRecords.add(record);
                collectedCount++;
                updateCountViews();
                addEvent("Device " + d.id + ": " + rssi + " dBm (" + freq + " MHz) at " +
                        distance + "m (" + method + ").");
            } else {
                addEvent("Ignoring " + rssi + " dBm (" + freq + " MHz) for device " +
                        d.id + " (" + method + ").");
            }
        }
    }

    private Device getDevice(String name, String wifiMac, String btMac) {
        Device device = null;
        for (Device d : devices) {
            if ((!TextUtils.isEmpty(d.btMac) && d.btMac.equals(btMac)) ||
                    (!TextUtils.isEmpty(d.wifiMac) && d.wifiMac.equals(wifiMac))) {
                device = d;
                break;
            }
        }
        if (device == null) {
            device = new Device(devices.size()+1, name, wifiMac, btMac);
            devices.add(device);
            deviceLogView.append(device.toString() + "\n");
            addEvent("Found new device, " + device.id);
        } else {
            if (TextUtils.isEmpty(device.name) && !TextUtils.isEmpty(name)) device.name = name;
            if (TextUtils.isEmpty(device.wifiMac) && !TextUtils.isEmpty(wifiMac)) device.wifiMac = wifiMac;
            if (TextUtils.isEmpty(device.btMac) && !TextUtils.isEmpty(btMac)) device.btMac = btMac;
            // fill in any previous records
            for (RssiRequest.RssiRecord r : rssiRecords) {
                if ((!TextUtils.isEmpty(device.btMac) && device.btMac.equals(r.remoteBtMac)) ||
                        (!TextUtils.isEmpty(device.wifiMac) && device.wifiMac.equals(r.remoteWifiMac))) {
                    if (TextUtils.isEmpty(r.remoteName) &&
                            !TextUtils.isEmpty(device.name)) r.remoteName = device.name;
                    if (TextUtils.isEmpty(r.remoteWifiMac) &&
                            !TextUtils.isEmpty(device.wifiMac)) r.remoteWifiMac = device.wifiMac;
                    if (TextUtils.isEmpty(r.remoteBtMac) &&
                            !TextUtils.isEmpty(device.btMac)) r.remoteBtMac = device.btMac;
                }
            }
        }
        return device;
    }

    private void addEvent(String event) {
        eventLogView.append(event + "\n");
    }

    private void updateCountViews() {
        collectedCountView.setText(collectedCount+"");
        toSendCountView.setText(rssiRecords.size() + "");
    }

    public void startCollection() {
        btBeacon.registerListener(btBeaconListener);
        btBeacon.startBeaconing(this, REQUEST_BT_DISCOVERABLE);
        btLeBeacon.registerListener(btLeBeaconListener);
        btLeBeacon.startBeaconing(this, REQUEST_BT_ENABLE);
        wifiScanner.registerListener(wifiScanListener);
        wifiScanner.startScanning();
    }

    public void stopCollection() {
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
        addEvent("Loaded " + list.size() + " records from memory.");
    }

    private final BtBeacon.BtBeaconListener btBeaconListener = new BtBeacon.BtBeaconListener() {
        @Override
        public void onDeviceFound(BluetoothDevice device, short rssi) {
            addRecord(device.getName(), "", device.getAddress(), "BtBeacon", rssi, 2400);
        }

        @Override
        public void onThisDeviceDiscoverableChanged(boolean discoverable) {
            addEvent("BT Discoverability changed to " + discoverable);
        }

        @Override
        public void onDiscoveryStarting() {
            addEvent("Scanning for BT devices...");
        }
    };

    private final BtLeBeacon.BtLeBeaconListener btLeBeaconListener = new BtLeBeacon.BtLeBeaconListener() {
        @Override
        public void onAdvertiseNotSupported() {
            addEvent("BT-LE advertisement not supported on this device.");
        }

        @Override
        public void onAdvertiseStartSuccess(AdvertiseSettings settingsInEffect) {
            addEvent("BT-LE advertising started at " +
                    settingsInEffect.getTxPowerLevel() + " dBm.");
        }

        @Override
        public void onAdvertiseStartFailure(int errorCode, String errorMsg) {
            addEvent("BT-LE advertisements failed to start (" + errorCode + "): " + errorMsg);
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            handleScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            addEvent("Received " + results.size() + " batch scan results (BtLeBeacon).");
            for (ScanResult sr : results) {
                handleScanResult(sr);
            }
        }

        @Override
        public void onScanFailed(int errorCode, String errorMsg) {
            addEvent("BT-LE scan failed (" + errorCode + "): " + errorMsg);
        }

        private void handleScanResult(ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device != null) {
                addRecord(device.getName(), "", device.getAddress(),
                        "BtLeBeacon", result.getRssi(), 2400);
            } else {
                addEvent("BT-LE received " + result.getRssi() + " dBm from null device.");
            }
        }
    };

    private final WifiScanner.ScanListener wifiScanListener = new WifiScanner.ScanListener() {
        @Override
        public void onScanResults(List<android.net.wifi.ScanResult> scanResults) {
            addEvent("WifiScanner found " + scanResults.size() + " results.");
            for (android.net.wifi.ScanResult r : scanResults) {
                addRecord(r.SSID, r.BSSID, "", "WifiScan", r.level, r.frequency);
            }
        }
    };
}
