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

import com.jifalops.wsnlocalize.bluetooth.BtBeacon;
import com.jifalops.wsnlocalize.bluetooth.BtLeBeacon;
import com.jifalops.wsnlocalize.data.RssiRecord;
import com.jifalops.wsnlocalize.data.Trainer;
import com.jifalops.wsnlocalize.data.WindowRecord;
import com.jifalops.wsnlocalize.file.NumberReaderWriter;
import com.jifalops.wsnlocalize.file.RssiReaderWriter;
import com.jifalops.wsnlocalize.file.WindowReaderWriter;
import com.jifalops.wsnlocalize.wifi.WifiScanner;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
            btRssiCountView, btWindowCountView, btWeightCountView,
            btleRssiCountView, btleWindowCountView, btleWeightCountView,
            wifiRssiCountView, wifiWindowCountView, wifiWeightCountView;
    Switch collectSwitch;

    final List<Device> devices = new ArrayList<>();

    int logLevel = LOG_IMPORTANT;
    final List<Integer> deviceIds = new ArrayList<>();
    float distance;
    boolean collectEnabled;

    SharedPreferences prefs;

    BtBeacon btBeacon;
    BtLeBeacon btLeBeacon;
    WifiScanner wifiScanner;

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
        btWeightCountView = (TextView) findViewById(R.id.btWeightCount);
        btleWeightCountView = (TextView) findViewById(R.id.btleWeightCount);
        wifiWeightCountView = (TextView) findViewById(R.id.wifiWeightCount);
        collectSwitch = (Switch) findViewById(R.id.collectSwitch);

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
                    distance = Float.valueOf(s.toString());
                } catch (NumberFormatException e) {
                    distanceView.setText(distance + "");
                }
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        prefs = getSharedPreferences("rssitraining", MODE_PRIVATE);
        btBeacon = BtBeacon.getInstance(this);
        btLeBeacon = BtLeBeacon.getInstance(this);
        wifiScanner = WifiScanner.getInstance(this);

        bt = new SignalStuff(new Trainer(10, 10_000, 10, 120_000, new Trainer.TrainingCallbacks() {
            @Override
            public void onTimeToTrain(List<WindowRecord> records, double[][] samples) {

            }

            @Override
            public void onWindowRecordReady(WindowRecord record, List<RssiRecord> from) {

            }

            @Override
            public void onTrainingComplete(double[] weights, double error) {

            }
        }), new Recorder(new RssiReaderWriter(new File(getExternalFilesDir(null), "bt-rssi-hist.csv"),
                new RssiReaderWriter.RssiCallbacks() {
                    @Override
                    public void onRssiRecordsRead(RssiReaderWriter rw, List<RssiRecord> records) {

                    }

                    @Override
                    public void onRssiRecordsWritten(RssiReaderWriter rw, int recordsWritten) {

                    }
                }), new WindowReaderWriter(new File(getExternalFilesDir(null), "bt-window-hist.csv"),
                new WindowReaderWriter.WindowCallbacks() {
                    @Override
                    public void onWindowRecordsRead(WindowReaderWriter rw, List<WindowRecord> records) {

                    }

                    @Override
                    public void onWindowRecordsWritten(WindowReaderWriter rw, int recordsWritten) {

                    }
                }), new NumberReaderWriter(new File(getExternalFilesDir(null), "bt-weights-hist.csv"),
                new NumberReaderWriter.NumberCallbacks() {
                    @Override
                    public void onNumbersRead(NumberReaderWriter rw, List<double[]> records) {

                    }

                    @Override
                    public void onNumbersWritten(NumberReaderWriter rw, int recordsWritten) {

                    }
                })), new Recorder(new RssiReaderWriter(new File(getExternalFilesDir(null), "bt-rssi-send.csv"),
                new RssiReaderWriter.RssiCallbacks() {
                    @Override
                    public void onRssiRecordsRead(RssiReaderWriter rw, List<RssiRecord> records) {

                    }

                    @Override
                    public void onRssiRecordsWritten(RssiReaderWriter rw, int recordsWritten) {

                    }
                }), new WindowReaderWriter(new File(getExternalFilesDir(null), "bt-window-send.csv"),
                new WindowReaderWriter.WindowCallbacks() {
                    @Override
                    public void onWindowRecordsRead(WindowReaderWriter rw, List<WindowRecord> records) {

                    }

                    @Override
                    public void onWindowRecordsWritten(WindowReaderWriter rw, int recordsWritten) {

                    }
                }), new NumberReaderWriter(new File(getExternalFilesDir(null), "bt-weights-send.csv"),
                new NumberReaderWriter.NumberCallbacks() {
                    @Override
                    public void onNumbersRead(NumberReaderWriter rw, List<double[]> records) {

                    }

                    @Override
                    public void onNumbersWritten(NumberReaderWriter rw, int recordsWritten) {

                    }
                }));
        btle = new SignalStuff(new Trainer(10, 10_000, 10, 120_000, new Trainer.TrainingCallbacks() {
            @Override
            public void onTimeToTrain(List<WindowRecord> records, double[][] samples) {

            }

            @Override
            public void onWindowRecordReady(WindowRecord record, List<RssiRecord> from) {

            }

            @Override
            public void onTrainingComplete(double[] weights, double error) {

            }
        }), new Recorder(new RssiReaderWriter(new File(getExternalFilesDir(null), "btle-rssi-hist.csv"),
                new RssiReaderWriter.RssiCallbacks() {
                    @Override
                    public void onRssiRecordsRead(RssiReaderWriter rw, List<RssiRecord> records) {

                    }

                    @Override
                    public void onRssiRecordsWritten(RssiReaderWriter rw, int recordsWritten) {

                    }
                }), new WindowReaderWriter(new File(getExternalFilesDir(null), "btle-window-hist.csv"),
                new WindowReaderWriter.WindowCallbacks() {
                    @Override
                    public void onWindowRecordsRead(WindowReaderWriter rw, List<WindowRecord> records) {

                    }

                    @Override
                    public void onWindowRecordsWritten(WindowReaderWriter rw, int recordsWritten) {

                    }
                }), new NumberReaderWriter(new File(getExternalFilesDir(null), "btle-weights-hist.csv"),
                new NumberReaderWriter.NumberCallbacks() {
                    @Override
                    public void onNumbersRead(NumberReaderWriter rw, List<double[]> records) {

                    }

                    @Override
                    public void onNumbersWritten(NumberReaderWriter rw, int recordsWritten) {

                    }
                })), new Recorder(new RssiReaderWriter(new File(getExternalFilesDir(null), "btle-rssi-send.csv"),
                new RssiReaderWriter.RssiCallbacks() {
                    @Override
                    public void onRssiRecordsRead(RssiReaderWriter rw, List<RssiRecord> records) {

                    }

                    @Override
                    public void onRssiRecordsWritten(RssiReaderWriter rw, int recordsWritten) {

                    }
                }), new WindowReaderWriter(new File(getExternalFilesDir(null), "btle-window-send.csv"),
                new WindowReaderWriter.WindowCallbacks() {
                    @Override
                    public void onWindowRecordsRead(WindowReaderWriter rw, List<WindowRecord> records) {

                    }

                    @Override
                    public void onWindowRecordsWritten(WindowReaderWriter rw, int recordsWritten) {

                    }
                }), new NumberReaderWriter(new File(getExternalFilesDir(null), "btle-weights-send.csv"),
                new NumberReaderWriter.NumberCallbacks() {
                    @Override
                    public void onNumbersRead(NumberReaderWriter rw, List<double[]> records) {

                    }

                    @Override
                    public void onNumbersWritten(NumberReaderWriter rw, int recordsWritten) {

                    }
                })));
        wifi = new SignalStuff(new Trainer(10, 10_000, 100, 120_000, new Trainer.TrainingCallbacks() {
            @Override
            public void onTimeToTrain(List<WindowRecord> records, double[][] samples) {

            }

            @Override
            public void onWindowRecordReady(WindowRecord record, List<RssiRecord> from) {

            }

            @Override
            public void onTrainingComplete(double[] weights, double error) {

            }
        }), new Recorder(new RssiReaderWriter(new File(getExternalFilesDir(null), "wifi-rssi-hist.csv"),
                new RssiReaderWriter.RssiCallbacks() {
                    @Override
                    public void onRssiRecordsRead(RssiReaderWriter rw, List<RssiRecord> records) {

                    }

                    @Override
                    public void onRssiRecordsWritten(RssiReaderWriter rw, int recordsWritten) {

                    }
                }), new WindowReaderWriter(new File(getExternalFilesDir(null), "wifi-window-hist.csv"),
                new WindowReaderWriter.WindowCallbacks() {
                    @Override
                    public void onWindowRecordsRead(WindowReaderWriter rw, List<WindowRecord> records) {

                    }

                    @Override
                    public void onWindowRecordsWritten(WindowReaderWriter rw, int recordsWritten) {

                    }
                }), new NumberReaderWriter(new File(getExternalFilesDir(null), "wifi-weights-hist.csv"),
                new NumberReaderWriter.NumberCallbacks() {
                    @Override
                    public void onNumbersRead(NumberReaderWriter rw, List<double[]> records) {

                    }

                    @Override
                    public void onNumbersWritten(NumberReaderWriter rw, int recordsWritten) {

                    }
                })), new Recorder(new RssiReaderWriter(new File(getExternalFilesDir(null), "wifi-rssi-send.csv"),
                new RssiReaderWriter.RssiCallbacks() {
                    @Override
                    public void onRssiRecordsRead(RssiReaderWriter rw, List<RssiRecord> records) {

                    }

                    @Override
                    public void onRssiRecordsWritten(RssiReaderWriter rw, int recordsWritten) {

                    }
                }), new WindowReaderWriter(new File(getExternalFilesDir(null), "wifi-window-send.csv"),
                new WindowReaderWriter.WindowCallbacks() {
                    @Override
                    public void onWindowRecordsRead(WindowReaderWriter rw, List<WindowRecord> records) {

                    }

                    @Override
                    public void onWindowRecordsWritten(WindowReaderWriter rw, int recordsWritten) {

                    }
                }), new NumberReaderWriter(new File(getExternalFilesDir(null), "wifi-weights-send.csv"),
                new NumberReaderWriter.NumberCallbacks() {
                    @Override
                    public void onNumbersRead(NumberReaderWriter rw, List<double[]> records) {

                    }

                    @Override
                    public void onNumbersWritten(NumberReaderWriter rw, int recordsWritten) {

                    }
                })));
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

    void addRecord(String localMac, String remoteMac, String remoteDesc,
                           String method, int rssi, int freq) {
        Device d = getDevice(remoteMac, remoteDesc);
        if (collectEnabled) {
            if (deviceIds.contains(d.id) && rssi != 0 && distance != 0) {
                String time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US).format(new Date());
                RssiRecordOld record = new RssiRecordOld(
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

    final BtBeacon.BtBeaconListener btBeaconListener = new BtBeacon.BtBeaconListener() {
        @Override
        public void onDeviceFound(BluetoothDevice device, short rssi) {
            addRecord(App.getInstance().getBtMac(), device.getAddress(),
                    device.getName() + " (BT)",
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

        void handleScanResult(ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device != null) {
                addRecord(App.getInstance().getBtMac(), device.getAddress(),
                        device.getName() + " (BTLE)",
                        RssiRecord.SIGNAL_BTLE, result.getRssi(), 2400);
            } else {
                addEvent("BT-LE received " + result.getRssi() + " dBm from null device.", LOG_INFORMATIVE);
            }
        }
    };

    final WifiScanner.ScanListener wifiScanListener = new WifiScanner.ScanListener() {
        @Override
        public void onScanResults(List<android.net.wifi.ScanResult> scanResults) {
            addEvent("WifiScan found " + scanResults.size() + " results.", LOG_ALL);
            for (android.net.wifi.ScanResult r : scanResults) {
                addRecord(App.getInstance().getWifiMac(), r.BSSID, r.SSID  +
                                " (WiFi " + r.frequency + "MHz)",
                        RssiRecord.SIGNAL_WIFI, r.level, r.frequency);
            }
        }
    };

    final RssiFilter.FilterCallback filterCallback =
            new RssiFilter.FilterCallback() {
        long lastTime = 0;
        @Override
        public void onRecordReady(RssiRecordOld record, int recordsFiltered, long elapsedMillis) {
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

    String formatMillis(long millis) {
        return String.format(Locale.US, "%.1fs", ((double)millis)/1000);
    }

    final RssiFilter btFilter = new RssiFilter(10000, 5, filterCallback);
    final RssiFilter btLeFilter = new RssiFilter(10000, 20, filterCallback);
    final RssiFilter wifiFilter = new RssiFilter(10000, 10, filterCallback);
}
