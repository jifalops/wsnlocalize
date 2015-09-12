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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jifalops.wsnlocalize.bluetooth.BtBeacon;
import com.jifalops.wsnlocalize.bluetooth.BtLeBeacon;
import com.jifalops.wsnlocalize.data.ResettingList;
import com.jifalops.wsnlocalize.data.RssiRecord;
import com.jifalops.wsnlocalize.data.WindowRecord;
import com.jifalops.wsnlocalize.file.TextReaderWriter;
import com.jifalops.wsnlocalize.signal.SignalStuff;
import com.jifalops.wsnlocalize.util.ServiceThreadApplication;
import com.jifalops.wsnlocalize.wifi.WifiScanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 */
public class RssiTrainingActivity extends Activity {
    static final int REQUEST_BT_ENABLE = 1;
    static final int REQUEST_BT_DISCOVERABLE = 2;
    static final int LOG_IMPORTANT = 1;
    static final int LOG_INFORMATIVE = 2;
    static final int LOG_ALL = 3;
    static final int LOG_DEFAULT = LOG_INFORMATIVE;

    final ResettingList.Limits
        btWindowTrigger = new ResettingList.Limits(5, 30_000, 10, 120_000),
        btTrainTrigger = new ResettingList.Limits(5, 300_000, 10, 600_000),

        btleWindowTrigger = new ResettingList.Limits(20, 10_000, 40, 30_000),
        btleTrainTrigger = new ResettingList.Limits(5, 120_000, 10, 300_000),

        wifiWindowTrigger = new ResettingList.Limits(8, 10_000, 16, 30_000),
        wifiTrainTrigger = new ResettingList.Limits(5, 120_000, 10, 300_000);


    static class Device {
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

    SignalStuff bt, btle, wifi;

    TextView eventLogView, deviceLogView,
            btRssiCountView, btWindowCountView,
            btleRssiCountView, btleWindowCountView,
            wifiRssiCountView, wifiWindowCountView;
    Switch collectSwitch;

    CheckBox btCheckBox, btleCheckBox, wifiCheckBox;

    final List<Device> devices = new ArrayList<>();
    final List<Integer> deviceIds = new ArrayList<>();
    int logLevel = LOG_DEFAULT;
    double distance;
    boolean collectEnabled;
    boolean isPersistent;

    SharedPreferences prefs;

    BtBeacon btBeacon;
    BtLeBeacon btLeBeacon;
    WifiScanner wifiScanner;

    ServiceThreadApplication.LocalService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssitraining);
        eventLogView = (TextView) findViewById(R.id.eventLog);
        deviceLogView = (TextView) findViewById(R.id.deviceLog);
        btRssiCountView = (TextView) findViewById(R.id.btRssiCount);
        btleRssiCountView = (TextView) findViewById(R.id.btleRssiCount);
        wifiRssiCountView = (TextView) findViewById(R.id.wifiRssiCount);
        btWindowCountView = (TextView) findViewById(R.id.btWindowCount);
        btleWindowCountView = (TextView) findViewById(R.id.btleWindowCount);
        wifiWindowCountView = (TextView) findViewById(R.id.wifiWindowCount);
        collectSwitch = (Switch) findViewById(R.id.collectSwitch);
        btCheckBox = (CheckBox) findViewById(R.id.btCheckBox);
        btleCheckBox = (CheckBox) findViewById(R.id.btleCheckBox);
        wifiCheckBox = (CheckBox) findViewById(R.id.wifiCheckBox);

        final TextView deviceIdView = (TextView) findViewById(R.id.deviceId);
        final EditText distanceView = (EditText) findViewById(R.id.distanceMeters);
        Button sendButton = (Button) findViewById(R.id.sendButton);

        autoScroll((ScrollView) findViewById(R.id.eventScrollView), eventLogView);
        autoScroll((ScrollView) findViewById(R.id.deviceScrollView), deviceLogView);
        deviceIdView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder b = new AlertDialog.Builder(RssiTrainingActivity.this);
                final EditText input = new EditText(RssiTrainingActivity.this);
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
                        } catch (Exception ignored) {
                        }
                        boolean first = true;
                        for (int id : deviceIds) {
                            if (first) {
                                deviceIdView.append(id + "");
                                first = false;
                            } else {
                                deviceIdView.append("," + id);
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
                    distance = Double.valueOf(s.toString());
                } catch (NumberFormatException e) {
                    distanceView.setText(distance + "");
                }
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bt.send();
                btle.send();
                wifi.send();
            }
        });

        prefs = getSharedPreferences("rssitraining", MODE_PRIVATE);
        btBeacon = BtBeacon.getInstance(this);
        btLeBeacon = BtLeBeacon.getInstance(this);
        wifiScanner = WifiScanner.getInstance(this);

        final File dir = getExternalFilesDir(null);
        bt = new SignalStuff("bt", dir, btWindowTrigger, btTrainTrigger, signalCallbacks);
        btle = new SignalStuff("btle", dir, btleWindowTrigger, btleTrainTrigger, signalCallbacks);
        wifi = new SignalStuff("wifi", dir, wifiWindowTrigger, wifiTrainTrigger, signalCallbacks);

        App.getInstance().bindLocalService(new Runnable() {
            @Override
            public void run() {
                service = App.getInstance().getService();
                if (service != null) {
//                    service.setCachedObject("bt", bt);
//                    service.setCachedObject("btle", btle);
//                    service.setCachedObject("wifi", wifi);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.getInstance().unbindLocalService(null);
        bt.close();
        btle.close();
        wifi.close();
    }

    void autoScroll(final ScrollView sv, final TextView tv) {
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
        getMenuInflater().inflate(R.menu.menu_rssitraining, menu);
        SubMenu sub = menu.addSubMenu("Log Level");
        sub.add(1, LOG_IMPORTANT, 1, "Important").setCheckable(true);
        sub.add(1, LOG_INFORMATIVE, 2, "Informative").setCheckable(true);
        sub.add(1, LOG_ALL, 3, "All").setCheckable(true);
        sub.setGroupCheckable(1, true, true);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_persist).setChecked(isPersistent);
        menu.findItem(LOG_IMPORTANT).setChecked(logLevel == LOG_IMPORTANT);
        menu.findItem(LOG_INFORMATIVE).setChecked(logLevel == LOG_INFORMATIVE);
        menu.findItem(LOG_ALL).setChecked(logLevel == LOG_ALL);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_persist:
                if (isPersistent) {
                    item.setChecked(false);
                    setPersistent(false);
                } else {
                    item.setChecked(true);
                    setPersistent(true);
                }
                return true;
            case R.id.action_clear:
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setMessage("Truncate data files?");
                b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bt.truncate();
                        btle.truncate();
                        wifi.truncate();
                    }
                });
                return true;
            case LOG_IMPORTANT:
                logLevel = LOG_IMPORTANT;
                item.setChecked(true);
                return true;
            case LOG_INFORMATIVE:
                logLevel = LOG_INFORMATIVE;
                item.setChecked(true);
                return true;
            case LOG_ALL:
                logLevel = LOG_ALL;
                item.setChecked(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void setPersistent(boolean persist) {
        if (isPersistent != persist) {
            isPersistent = persist;
            if (service != null) {
                service.setPersistent(persist);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        btRssiCountView.setText(bt.getRssiCount() + "");
        btWindowCountView.setText(bt.getWindowCount() + "");
        btleRssiCountView.setText(btle.getRssiCount() + "");
        btleWindowCountView.setText(btle.getWindowCount() + "");
        wifiRssiCountView.setText(wifi.getRssiCount() + "");
        wifiWindowCountView.setText(wifi.getWindowCount() + "");

        logLevel = prefs.getInt("logLevel", LOG_DEFAULT);
        bt.enabled = prefs.getBoolean("btEnabled", true);
        btle.enabled = prefs.getBoolean("btleEnabled", true);
        wifi.enabled = prefs.getBoolean("wifiEnabled", true);

        btCheckBox.setOnCheckedChangeListener(null);
        btCheckBox.setChecked(bt.enabled);
        btCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bt.enabled = isChecked;
            }
        });
        btleCheckBox.setOnCheckedChangeListener(null);
        btleCheckBox.setChecked(btle.enabled);
        btleCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                btle.enabled = isChecked;
            }
        });
        wifiCheckBox.setOnCheckedChangeListener(null);
        wifiCheckBox.setChecked(wifi.enabled);
        wifiCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                wifi.enabled = isChecked;
            }
        });

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
        prefs.edit()
                .putInt("logLevel", logLevel)
                .putBoolean("btEnabled", bt.enabled)
                .putBoolean("btleEnabled", btle.enabled)
                .putBoolean("wifiEnabled", wifi.enabled).apply();
        collectSwitch.setChecked(false);
    }

    void addRecord(Device device, String signal, int rssi, int freq) {
        if (collectEnabled) {
            if (deviceIds.contains(device.id) && rssi < 0 && distance > 0) {
                addEvent("Device " + device.id + ": " + rssi + " dBm (" + freq + " MHz) at " +
                        distance + "m (" + signal + ").", LOG_INFORMATIVE);
//                String time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US).format(new Date());
                RssiRecord record = new RssiRecord(rssi, freq, System.currentTimeMillis(), distance);
                if (signal.equals(RssiRecord.SIGNAL_BT)) {
                    bt.add(record);
                    btRssiCountView.setText(bt.getRssiCount()+"");
                } else if (signal.equals(RssiRecord.SIGNAL_BTLE)) {
                    btle.add(record);
                    btleRssiCountView.setText(btle.getRssiCount() + "");
                } else if (signal.equals(RssiRecord.SIGNAL_WIFI)) {
                    wifi.add(record);
                    wifiRssiCountView.setText(wifi.getRssiCount() + "");
                }
            } else {
                addEvent("Ignoring " + rssi + " dBm (" + freq + " MHz) for device " +
                        device.id + " (" + signal + ").", LOG_ALL);
            }
        }
    }

    Device getDevice(String mac, String desc) {
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

    void addEvent(String event, int level) {
        if (level <= logLevel) {
            eventLogView.append(event + "\n");
        }
    }

    public void startCollection() {
        collectEnabled = true;
        if (bt.enabled) {

        }
        if (btle.enabled) {
            btLeBeacon.registerListener(btLeBeaconListener);
            btLeBeacon.startBeaconing(this, REQUEST_BT_ENABLE);
        }
        if (wifi.enabled) {
            wifiScanner.registerListener(wifiScanListener);
            wifiScanner.startScanning(100);
        }
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

    private void setBtEnabled(boolean enabled) {
        bt.enabled = enabled;
        if (enabled && collectEnabled) {
            btBeacon.registerListener(btBeaconListener);
            btBeacon.startBeaconing(this, REQUEST_BT_DISCOVERABLE);
        } else
    }

    final BtBeacon.BtBeaconListener btBeaconListener = new BtBeacon.BtBeaconListener() {
        @Override
        public void onDeviceFound(BluetoothDevice device, short rssi) {
            addRecord(getDevice(device.getAddress(), device.getName() + " (BT)"),
                    RssiRecord.SIGNAL_BT, rssi, 2400);
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

    final BtLeBeacon.BtLeBeaconListener btLeBeaconListener = new BtLeBeacon.BtLeBeaconListener() {
        @Override
        public void onAdvertiseNotSupported() {
            addEvent("BTLE advertisement not supported on this device.", LOG_IMPORTANT);
        }

        @Override
        public void onAdvertiseStartSuccess(AdvertiseSettings settingsInEffect) {
            addEvent("BTLE advertising started at " +
                    settingsInEffect.getTxPowerLevel() + " dBm.", LOG_IMPORTANT);
        }

        @Override
        public void onAdvertiseStartFailure(int errorCode, String errorMsg) {
            addEvent("BTLE advertisements failed to start (" + errorCode + "): " + errorMsg, LOG_IMPORTANT);
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            handleScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            addEvent("Received " + results.size() + " batch scan results (BTLE).", LOG_ALL);
            for (ScanResult sr : results) {
                handleScanResult(sr);
            }
        }

        @Override
        public void onScanFailed(int errorCode, String errorMsg) {
            addEvent("BT-LE scan failed (" + errorCode + "): " + errorMsg, LOG_IMPORTANT);
        }

        void handleScanResult(ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device != null) {
                addRecord(getDevice(device.getAddress(), device.getName() + " (BTLE)"),
                        RssiRecord.SIGNAL_BTLE, result.getRssi(), 2400);
            } else {
                addEvent("BTLE received " + result.getRssi() + " dBm from null device.", LOG_INFORMATIVE);
            }
        }
    };

    final WifiScanner.ScanListener wifiScanListener = new WifiScanner.ScanListener() {
        @Override
        public void onScanResults(List<android.net.wifi.ScanResult> scanResults) {
            addEvent("WiFi found " + scanResults.size() + " results.", LOG_ALL);
            for (android.net.wifi.ScanResult r : scanResults) {
                addRecord(getDevice(r.BSSID, r.SSID + " (WiFi " + r.frequency + "MHz)"),
                        RssiRecord.SIGNAL_WIFI, r.level, r.frequency);
            }
        }
    };

    final SignalStuff.SignalCallbacks signalCallbacks = new SignalStuff.SignalCallbacks() {
        @Override
        public void onDataFileRead(TextReaderWriter rw) {
            if (rw == bt.rssiRW) btRssiCountView.setText(bt.getRssiCount() + "");
            else if (rw == bt.windowRW) btWindowCountView.setText(bt.getWindowCount() + "");
            else if (rw == btle.rssiRW) btleRssiCountView.setText(btle.getRssiCount()+"");
            else if (rw == btle.windowRW) btleWindowCountView.setText(btle.getWindowCount()+"");
            else if (rw == wifi.rssiRW) wifiRssiCountView.setText(wifi.getRssiCount()+"");
            else if (rw == wifi.windowRW) wifiWindowCountView.setText(wifi.getWindowCount()+"");
        }

        @Override
        public void onDataFileWrite(TextReaderWriter rw) {

        }

        @Override
        public void onTrainingStarting(SignalStuff s, int samples) {
            addEvent("Training " + s.getSignalType() + " with " + samples + " samples.",
                    LOG_INFORMATIVE);
        }

        @Override
        public void onTrainingComplete(SignalStuff s, double[] weights, double error, int samples) {
            addEvent("Trained " + s.getSignalType() + " with " + samples + " samples, error = "
                    + String.format(Locale.US, "%.3f", error), LOG_IMPORTANT);
        }

        @Override
        public void onWindowReady(SignalStuff s, WindowRecord record) {
            String msg = s.getSignalType() + " window: " + record.rss.count + " in " +
                    formatMillis(record.elapsed.millis);
            if (record.estimated != 0) {
                double error = (record.estimated - record.distance) / record.distance;
                msg += String.format(Locale.US, ", est: %.1fm %.1f%%",
                        record.estimated, error * 100);
            }
            addEvent(msg, LOG_IMPORTANT);
            if (s == bt) btWindowCountView.setText(bt.getWindowCount()+"");
            else if (s == btle) btleWindowCountView.setText(btle.getWindowCount()+"");
            else if (s == wifi) wifiWindowCountView.setText(wifi.getWindowCount()+"");
        }

        @Override
        public void onSentSuccess(SignalStuff s, boolean wasRssi, int count) {
            String type = wasRssi ? " rssi " : " window ";
            addEvent(s.getSignalType() + " sent " + count + type + "records successfully.",
                    LOG_IMPORTANT);
        }

        @Override
        public void onSentFailure(SignalStuff s, boolean wasRssi, int count,
                                  int respCode, String resp, String result) {
            String type = wasRssi ? " rssi " : " window ";
            addEvent(s.getSignalType() + " failed to send " + count + type + "records. " +
                    respCode + ": " + resp + ". Result: " + result,
                    LOG_IMPORTANT);
        }

        @Override
        public void onSentFailure(SignalStuff s, boolean wasRssi, int count, String volleyError) {
            String type = wasRssi ? " rssi " : " window ";
            addEvent(s.getSignalType() + " failed to send " + count + type + "records. " +
                            volleyError,
                    LOG_IMPORTANT);
        }
    };

    String formatMillis(long millis) {
        return String.format(Locale.US, "%.1fs", ((double)millis)/1000);
    }

}
