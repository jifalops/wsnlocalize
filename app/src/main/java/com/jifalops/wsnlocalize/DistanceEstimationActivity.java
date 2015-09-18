package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jifalops.wsnlocalize.bluetooth.BtBeacon;
import com.jifalops.wsnlocalize.bluetooth.BtLeBeacon;
import com.jifalops.wsnlocalize.data.Estimator;
import com.jifalops.wsnlocalize.data.RssiRecord;
import com.jifalops.wsnlocalize.data.WindowRecord;
import com.jifalops.wsnlocalize.file.EstimatorReaderWriter;
import com.jifalops.wsnlocalize.util.ResettingList;
import com.jifalops.wsnlocalize.wifi.WifiScanner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 */
public class DistanceEstimationActivity extends Activity {
    static final int REQUEST_BT_ENABLE = 1;
    static final int REQUEST_BT_DISCOVERABLE = 2;

    static class Device {
        final String mac, name, signal;
        double estimate, previous;
        Device(String mac, String name, String signal) {
            this.mac = mac;
            this.name = name;
            this.signal = signal;
        }
    }

    class DeviceAdapter extends ArrayAdapter<Device> {
        public DeviceAdapter() {
            super(DistanceEstimationActivity.this, R.layout.listitem_deviceestimate, devices);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Device device = devices.get(position);
            Holder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listitem_deviceestimate, parent, false);
            }
            holder = (Holder) convertView.getTag();
            if (holder == null) {
                holder = new Holder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.desc = (TextView) convertView.findViewById(R.id.details);
                holder.estimate = (TextView) convertView.findViewById(R.id.estimate);
                holder.change = (TextView) convertView.findViewById(R.id.change);
            }
            holder.name.setText(device.name);
            holder.desc.setText(device.signal + ", " + device.mac);
            holder.estimate.setText(String.format(Locale.US, "%.1fm", device.estimate));
            holder.change.setText(String.format(Locale.US, "%+.1fm", device.estimate - device.previous));
            convertView.setTag(holder);
            return convertView;
        }
    }

    static class Holder {
        TextView name, desc, estimate, change;
    }

    DeviceAdapter adapter;

    BtBeacon bt;
    BtLeBeacon btle;
    WifiScanner wifi;

    List<Device> devices = new ArrayList<>();
    Map<Device, ResettingList<RssiRecord>> windowers = new HashMap<>();

    Estimator btEstimator, btleEstimator, wifiEstimator, wifi5gEstimator;

    boolean btUnavailable, btleUnavailable, wifiUnavailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distanceestimates);

        adapter = new DeviceAdapter();
        ((ListView) findViewById(R.id.deviceList)).setAdapter(adapter);

        bt = BtBeacon.getInstance(this);
        btle = BtLeBeacon.getInstance(this);
        wifi = WifiScanner.getInstance(this);


        final CheckBox btCheckbox = (CheckBox) findViewById(R.id.btCheckBox);
        btCheckbox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked)
                            bt.startBeaconing(DistanceEstimationActivity.this, REQUEST_BT_DISCOVERABLE);
                        else
                            bt.stopBeaconing(DistanceEstimationActivity.this, REQUEST_BT_DISCOVERABLE);
                    }
                });
        final CheckBox btleCheckbox = (CheckBox) findViewById(R.id.btleCheckBox);
        btleCheckbox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked)
                            btle.startBeaconing(DistanceEstimationActivity.this, REQUEST_BT_ENABLE);
                        else btle.stopBeaconing();
                    }
                });
        final CheckBox wifiCheckbox = (CheckBox) findViewById(R.id.wifiCheckBox);
        wifiCheckbox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) wifi.startScanning(100);
                        else wifi.stopScanning();
                    }
                });

        File dir = Settings.getDataDir(this);

        new EstimatorReaderWriter(new File(dir, Settings.getFileName(Settings.SIGNAL_BT,
                Settings.DATA_ESTIMATOR)), new EstimatorReaderWriter.EstimatorCallbacks() {
            @Override
            public void onEstimatorRecordsRead(EstimatorReaderWriter rw, List<Estimator> records) {
                if (records.size() > 0) {
                    btEstimator = records.get(records.size() - 1);
                    btCheckbox.setEnabled(true);
                } else {
                    btUnavailable = true;
                    checkIfFailed();
                }
            }

            @Override
            public void onEstimatorRecordsWritten(EstimatorReaderWriter rw, int recordsWritten) {

            }
        }).readRecords();

        new EstimatorReaderWriter(new File(dir, Settings.getFileName(Settings.SIGNAL_BTLE,
                Settings.DATA_ESTIMATOR)), new EstimatorReaderWriter.EstimatorCallbacks() {
            @Override
            public void onEstimatorRecordsRead(EstimatorReaderWriter rw, List<Estimator> records) {
                if (records.size() > 0) {
                    btleEstimator = records.get(records.size() - 1);
                    btleCheckbox.setEnabled(true);
                } else {
                    btleUnavailable = true;
                    checkIfFailed();
                }
            }

            @Override
            public void onEstimatorRecordsWritten(EstimatorReaderWriter rw, int recordsWritten) {

            }
        }).readRecords();

        new EstimatorReaderWriter(new File(dir, Settings.getFileName(Settings.SIGNAL_WIFI,
                Settings.DATA_ESTIMATOR)), new EstimatorReaderWriter.EstimatorCallbacks() {
            @Override
            public void onEstimatorRecordsRead(EstimatorReaderWriter rw, List<Estimator> records) {
                if (records.size() > 0) {
                    wifiEstimator = records.get(records.size() - 1);
                    wifiCheckbox.setEnabled(true);
                } else {
                    wifiUnavailable = true;
                    checkIfFailed();
                }
            }

            @Override
            public void onEstimatorRecordsWritten(EstimatorReaderWriter rw, int recordsWritten) {

            }
        }).readRecords();

        new EstimatorReaderWriter(new File(dir, Settings.getFileName(Settings.SIGNAL_WIFI5G,
                Settings.DATA_ESTIMATOR)), new EstimatorReaderWriter.EstimatorCallbacks() {
            @Override
            public void onEstimatorRecordsRead(EstimatorReaderWriter rw, List<Estimator> records) {
                if (records.size() > 0) {
                    wifi5gEstimator = records.get(records.size() - 1);
                }
            }

            @Override
            public void onEstimatorRecordsWritten(EstimatorReaderWriter rw, int recordsWritten) {

            }
        }).readRecords();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bt = null;
        btle = null;
        wifi = null;
    }

    void checkIfFailed() {
        if (btUnavailable && btleUnavailable && wifiUnavailable) {
            Toast.makeText(this, "No estimators available; need to train first.", Toast.LENGTH_LONG).show();
            finish();
        }
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
    protected void onResume() {
        super.onResume();
        bt.registerListener(btBeaconListener);
        btle.registerListener(btLeBeaconListener);
        wifi.registerListener(wifiScanListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bt.unregisterListener(btBeaconListener);
        btle.unregisterListener(btLeBeaconListener);
        wifi.unregisterListener(wifiScanListener);
        for (ResettingList list: windowers.values()) {
            list.reset();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    void reportSignal(String mac, String name, final String signal, int rssi, int freq) {
        Device device = null;
        for (Device d : windowers.keySet()) {
            if (d.mac.equals(mac)) {
                device = d;
                break;
            }
        }
        if (device == null) {
            device = new Device(mac, name, signal);
            devices.add(device);
            adapter.notifyDataSetChanged();
            final Device finalDevice = device;
            ResettingList.Limits limits;
            if (Settings.SIGNAL_BT.equals(signal)) limits = Settings.btWindowTrigger;
            else if (Settings.SIGNAL_BTLE.equals(signal)) limits = Settings.btleWindowTrigger;
            else limits = Settings.wifiWindowTrigger;
            windowers.put(device, new ResettingList<>(limits,
                    new ResettingList.LimitsCallback<RssiRecord>() {
                @Override
                public void onLimitsReached(List<RssiRecord> list, long time) {
                    Estimator estimator = null;
                    if (Settings.SIGNAL_BT.equals(signal)) {
                        estimator = btEstimator;
                    } else if (Settings.SIGNAL_BTLE.equals(signal)) {
                        estimator = btleEstimator;
                    } else if (Settings.SIGNAL_WIFI.equals(signal)) {
                        estimator = wifiEstimator;
                    } else if (Settings.SIGNAL_WIFI5G.equals(signal)) {
                        estimator = wifi5gEstimator;
                    }

                    if (estimator != null) {
                        double estimate = estimator.estimate(new WindowRecord(list));
                        finalDevice.previous = finalDevice.estimate;
                        finalDevice.estimate = estimate;
                        adapter.notifyDataSetChanged();
                    }
                }
            }));
        }

        windowers.get(device).add(new RssiRecord(mac, rssi, freq,
                System.currentTimeMillis(), 0));

    }

    final BtBeacon.BtBeaconListener btBeaconListener = new BtBeacon.BtBeaconListener() {
        @Override
        public void onDeviceFound(BluetoothDevice device, short rssi) {
            reportSignal(device.getAddress(), device.getName(), Settings.SIGNAL_BT, rssi, 2400);
        }

        @Override
        public void onThisDeviceDiscoverableChanged(boolean discoverable) {

        }

        @Override
        public void onDiscoveryStarting() {

        }
    };

    final BtLeBeacon.BtLeBeaconListener btLeBeaconListener = new BtLeBeacon.BtLeBeaconListener() {
        @Override
        public void onAdvertiseNotSupported() {

        }

        @Override
        public void onAdvertiseStartSuccess(AdvertiseSettings settingsInEffect) {

        }

        @Override
        public void onAdvertiseStartFailure(int errorCode, String errorMsg) {

        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            handleScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                handleScanResult(sr);
            }
        }

        @Override
        public void onScanFailed(int errorCode, String errorMsg) {

        }

        void handleScanResult(ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device != null) {
                reportSignal(device.getAddress(), device.getName(), Settings.SIGNAL_BTLE,
                        result.getRssi(), 2400);
            }
        }
    };

    final WifiScanner.ScanListener wifiScanListener = new WifiScanner.ScanListener() {
        @Override
        public void onScanResults(List<android.net.wifi.ScanResult> scanResults) {
            String signal;
            for (android.net.wifi.ScanResult r : scanResults) {
                signal = r.frequency < 4000 ? Settings.SIGNAL_WIFI : Settings.SIGNAL_WIFI5G;
                reportSignal(r.BSSID, r.SSID, signal, r.level, r.frequency);
            }
        }
    };
}
