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
import com.jifalops.wsnlocalize.data.RssiRecord;
import com.jifalops.wsnlocalize.data.WindowRecord;
import com.jifalops.wsnlocalize.signal.BestDistanceEstimator;
import com.jifalops.wsnlocalize.toolbox.util.ResettingList;
import com.jifalops.wsnlocalize.toolbox.wifi.WifiScanner;

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
        BestDistanceEstimator.Estimate estimate, previous;
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
            holder.estimate.setText(String.format(Locale.US, "%.1fm (%.1fm)",
                    device.estimate.mean, device.estimate.median));
            holder.change.setText(String.format(Locale.US, "%+.1fm (%+.1fm)",
                    device.estimate.mean - device.previous.mean,
                    device.estimate.median - device.previous.median));
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

    BestDistanceEstimator estimator;

    List<Device> devices = new ArrayList<>();
    Map<Device, ResettingList<RssiRecord>> windowers = new HashMap<>();

    CheckBox btCheckbox, btleCheckbox, wifiCheckbox, wifi5gCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distanceestimates);

        adapter = new DeviceAdapter();
        ((ListView) findViewById(R.id.deviceList)).setAdapter(adapter);

        bt = BtBeacon.getInstance(this);
        btle = BtLeBeacon.getInstance(this);
        wifi = new WifiScanner(this);



        btCheckbox = (CheckBox) findViewById(R.id.btCheckBox);
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
        btleCheckbox = (CheckBox) findViewById(R.id.btleCheckBox);
        btleCheckbox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked)
                            btle.startBeaconing(DistanceEstimationActivity.this, REQUEST_BT_ENABLE);
                        else btle.stopBeaconing();
                    }
                });
        wifiCheckbox = (CheckBox) findViewById(R.id.wifiCheckBox);
        wifiCheckbox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!wifi5gCheckbox.isChecked()) {
                            if (isChecked) wifi.startScanning(100);
                            else wifi.stopScanning();
                        }
                    }
                });
        wifi5gCheckbox = (CheckBox) findViewById(R.id.wifi5gCheckBox);
        wifi5gCheckbox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!wifiCheckbox.isChecked()) {
                            if (isChecked) wifi.startScanning(100);
                            else wifi.stopScanning();
                        }
                    }
                });

        estimator = new BestDistanceEstimator(new BestDistanceEstimator.OnReadyListener() {
            @Override
            public void onReady() {
                if (estimator.getBtSize() > 0) {
                    btCheckbox.setEnabled(true);
                }
                if (estimator.getBtleSize() > 0) {
                    btleCheckbox.setEnabled(true);
                }
                if (estimator.getWifiSize() > 0) {
                    wifiCheckbox.setEnabled(true);
                }
                if (estimator.getWifi5gSize() > 0) {
                    wifi5gCheckbox.setEnabled(true);
                }
                if (!btCheckbox.isEnabled() && !btleCheckbox.isEnabled() &&
                        !wifiCheckbox.isEnabled() && !wifi5gCheckbox.isEnabled()) {
                    Toast.makeText(DistanceEstimationActivity.this,
                            "No estimators available; need to train first.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bt = null;
        btle = null;
        wifi = null;
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
            ResettingList.Trigger trigger;
            if (App.SIGNAL_BT.equals(signal)) trigger = App.btWindowTrigger;
            else if (App.SIGNAL_BTLE.equals(signal)) trigger = App.btleWindowTrigger;
            else trigger = App.wifiWindowTrigger;
            windowers.put(device, new ResettingList<>(trigger,
                    new ResettingList.LimitsCallback<RssiRecord>() {
                @Override
                public void onLimitsReached(List<RssiRecord> list, long time) {
                    BestDistanceEstimator.Estimate estimate = null;
                    double[] sample = new WindowRecord(list).toSample();
                    if (App.SIGNAL_BT.equals(signal)) {
                        estimate = estimator.estimateBt(sample);
                    } else if (App.SIGNAL_BTLE.equals(signal)) {
                        estimate = estimator.estimateBtle(sample);
                    } else if (App.SIGNAL_WIFI.equals(signal)) {
                        estimate = estimator.estimateWifi(sample);
                    } else if (App.SIGNAL_WIFI5G.equals(signal)) {
                        estimate = estimator.estimateWifi5g(sample);
                    }
                    finalDevice.previous = finalDevice.estimate;
                    finalDevice.estimate = estimate;
                    adapter.notifyDataSetChanged();
                }
            }));
        }

        windowers.get(device).add(new RssiRecord(mac, rssi, freq,
                System.currentTimeMillis(), 0));

    }

    final BtBeacon.BtBeaconListener btBeaconListener = new BtBeacon.BtBeaconListener() {
        @Override
        public void onDeviceFound(BluetoothDevice device, short rssi) {
            reportSignal(device.getAddress(), device.getName(), App.SIGNAL_BT, rssi, 2400);
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
                reportSignal(device.getAddress(), device.getName(), App.SIGNAL_BTLE,
                        result.getRssi(), 2400);
            }
        }
    };

    final WifiScanner.ScanListener wifiScanListener = new WifiScanner.ScanListener() {
        @Override
        public void onScanResults(List<android.net.wifi.ScanResult> scanResults) {
            for (android.net.wifi.ScanResult r : scanResults) {
                if (r.frequency < 4000 && wifiCheckbox.isChecked()) {
                    reportSignal(r.BSSID, r.SSID, App.SIGNAL_WIFI, r.level, r.frequency);
                } else if (r.frequency > 4000 && wifi5gCheckbox.isChecked()) {
                    reportSignal(r.BSSID, r.SSID, App.SIGNAL_WIFI5G, r.level, r.frequency);
                }
            }
        }
    };
}
