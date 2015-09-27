package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import com.jifalops.wsnlocalize.data.Rssi;
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
    static final String TAG = DistanceEstimationActivity.class.getSimpleName();

    static final int REQUEST_BT_ENABLE = 1;
    static final int REQUEST_BT_DISCOVERABLE = 2;

    static class Device {
        final String mac, name, signal;
        BestDistanceEstimator.Estimate
                nnEstimate, nnPrevious,
                f1Estimate, f1Previous,
                logEstimate, logPrevious;
        double actual = 0;
        Device(String mac, String name, String signal) {
            this.mac = mac;
            this.name = name;
            this.signal = signal;
            nnEstimate = new BestDistanceEstimator.Estimate(0,0);
            nnPrevious = new BestDistanceEstimator.Estimate(0,0);
            f1Estimate = new BestDistanceEstimator.Estimate(0,0);
            f1Previous = new BestDistanceEstimator.Estimate(0,0);
            logEstimate = new BestDistanceEstimator.Estimate(0,0);
            logPrevious = new BestDistanceEstimator.Estimate(0,0);
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
                holder.signal = (TextView) convertView.findViewById(R.id.signal);
                holder.mac = (TextView) convertView.findViewById(R.id.mac);
                holder.nnEstimate = (TextView) convertView.findViewById(R.id.nnEstimate);
                holder.nnChange = (TextView) convertView.findViewById(R.id.nnChange);
                holder.f1Estimate = (TextView) convertView.findViewById(R.id.f1Estimate);
                holder.f1Change = (TextView) convertView.findViewById(R.id.f1Change);
                holder.logEstimate = (TextView) convertView.findViewById(R.id.logEstimate);
                holder.logChange = (TextView) convertView.findViewById(R.id.logChange);
            }
            holder.name.setText(device.name);
            holder.signal.setText(device.signal);
            holder.mac.setText(device.mac);
            holder.nnEstimate.setText(formatEstimate(device.nnEstimate));
            holder.nnChange.setText(formatChange(device.nnEstimate, device.nnPrevious));
            holder.f1Estimate.setText(formatEstimate(device.f1Estimate));
            holder.f1Change.setText(formatChange(device.f1Estimate, device.f1Previous));
            holder.logEstimate.setText(formatEstimate(device.logEstimate));
            holder.logChange.setText(formatChange(device.logEstimate, device.logPrevious));
            convertView.setTag(holder);
            return convertView;
        }
    }

    String formatEstimate(BestDistanceEstimator.Estimate e) {
        return String.format(Locale.US, "%.1f (%.1f)", e.mean, e.median);
    }
    String formatChange(BestDistanceEstimator.Estimate e, BestDistanceEstimator.Estimate p) {
        return String.format(Locale.US, "%+.1f (%+.1f)", e.mean - p.mean, e.median - p.median);
    }

    private static class Holder {
        TextView name, signal, mac,
                nnEstimate, nnChange,
                f1Estimate, f1Change,
                logEstimate, logChange;
    }

    DeviceAdapter adapter;

    BtBeacon bt;
    BtLeBeacon btle;
    WifiScanner wifi;

    BestDistanceEstimator estimator, bestEstimator, toSendEstimator;

    List<Device> devices = new ArrayList<>();
    Map<Device, ResettingList<Rssi>> windowers = new HashMap<>();

    CheckBox btCheckbox, btleCheckbox, wifiCheckbox, wifi5gCheckbox;

    boolean useBest;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distanceestimates);

        adapter = new DeviceAdapter();
        ((ListView) findViewById(R.id.deviceList)).setAdapter(adapter);

        bt = BtBeacon.getInstance(this);
        btle = BtLeBeacon.getInstance(this);
        wifi = new WifiScanner(this);

        prefs = getSharedPreferences(TAG, MODE_PRIVATE);
        useBest = prefs.getBoolean("best", useBest);

        btCheckbox = (CheckBox) findViewById(R.id.btCheckBox);
        btleCheckbox = (CheckBox) findViewById(R.id.btleCheckBox);
        wifiCheckbox = (CheckBox) findViewById(R.id.wifiCheckBox);
        wifi5gCheckbox = (CheckBox) findViewById(R.id.wifi5gCheckBox);

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
        btleCheckbox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked)
                            btle.startBeaconing(DistanceEstimationActivity.this, REQUEST_BT_ENABLE);
                        else btle.stopBeaconing();
                    }
                });
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

        bestEstimator = new BestDistanceEstimator(true, true, new BestDistanceEstimator.OnReadyListener() {
            @Override
            public void onReady() {
                setupControls();
                if (useBest) {
                    if (!btCheckbox.isEnabled() && !btleCheckbox.isEnabled() &&
                            !wifiCheckbox.isEnabled() && !wifi5gCheckbox.isEnabled()) {
                        Toast.makeText(DistanceEstimationActivity.this,
                                "No estimators available; need to train first or load different estimators.",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        toSendEstimator = new BestDistanceEstimator(false, true, new BestDistanceEstimator.OnReadyListener() {
            @Override
            public void onReady() {
                setupControls();
                if (!useBest) {
                    if (!btCheckbox.isEnabled() && !btleCheckbox.isEnabled() &&
                            !wifiCheckbox.isEnabled() && !wifi5gCheckbox.isEnabled()) {
                        Toast.makeText(DistanceEstimationActivity.this,
                                "No estimators available; need to train first or load different estimators.",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    void setupControls() {
        estimator = useBest ? bestEstimator : toSendEstimator;
        btCheckbox.setEnabled(estimator.getBtSize() > 0);
        btleCheckbox.setEnabled(estimator.getBtleSize() > 0);
        wifiCheckbox.setEnabled(estimator.getWifiSize() > 0);
        wifi5gCheckbox.setEnabled(estimator.getWifi5gSize() > 0);
        btCheckbox.setChecked(false);
        btleCheckbox.setChecked(false);
        wifiCheckbox.setChecked(false);
        wifi5gCheckbox.setChecked(false);
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
        setupControls();
        bt.registerListener(btBeaconListener);
        btle.registerListener(btLeBeaconListener);
        wifi.registerListener(wifiScanListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.edit().putBoolean("best", useBest).apply();
        bt.unregisterListener(btBeaconListener);
        btle.unregisterListener(btLeBeaconListener);
        wifi.unregisterListener(wifiScanListener);
        for (ResettingList list: windowers.values()) {
            list.reset();
        }
        btCheckbox.setChecked(false);
        btleCheckbox.setChecked(false);
        wifiCheckbox.setChecked(false);
        wifi5gCheckbox.setChecked(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_distanceestimator, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.useBest).setChecked(useBest);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.useBest) {
            useBest = !useBest;
            setupControls();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                    new ResettingList.LimitsCallback<Rssi>() {
                @Override
                public void onLimitsReached(List<Rssi> list, long time) {
                    BestDistanceEstimator.Estimate nnEstimate = null;
                    WindowRecord w = new WindowRecord(list);
                    double[] sample = w.toSample();
                    if (App.SIGNAL_BT.equals(signal)) {
                        nnEstimate = estimator.estimateBt(sample);
                    } else if (App.SIGNAL_BTLE.equals(signal)) {
                        nnEstimate = estimator.estimateBtle(sample);
                    } else if (App.SIGNAL_WIFI.equals(signal)) {
                        nnEstimate = estimator.estimateWifi(sample);
                    } else if (App.SIGNAL_WIFI5G.equals(signal)) {
                        nnEstimate = estimator.estimateWifi5g(sample);
                    }
                    finalDevice.nnPrevious = finalDevice.nnEstimate;
                    finalDevice.nnEstimate = nnEstimate;
                    BestDistanceEstimator.Estimate f1Estimate = new BestDistanceEstimator.Estimate(
                            freeSpacePathLoss(w.rss.mean, list.get(0).freq),
                            freeSpacePathLoss(w.rss.median, list.get(0).freq));
                    finalDevice.f1Previous = finalDevice.f1Estimate;
                    finalDevice.f1Estimate = f1Estimate;
                    BestDistanceEstimator.Estimate logEstimate = new BestDistanceEstimator.Estimate(
                            estimateLog(finalDevice.signal, w.rss.mean),
                            estimateLog(finalDevice.signal, w.rss.median));
                    finalDevice.logPrevious = finalDevice.logEstimate;
                    finalDevice.logEstimate = logEstimate;
                    adapter.notifyDataSetChanged();
                }
            }));
        }

        windowers.get(device).add(new Rssi(mac, rssi, freq,
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


    double estimateLog(String signal, double rssi) {
        int divisor = 50;
        if (App.SIGNAL_BT.equals(signal) || App.SIGNAL_BTLE.equals(signal)) divisor = 100;
        return Math.pow(10, (-rssi - 1) / divisor);
//        10^((-x-1)/50)
    }

    double freeSpacePathLoss(double levelInDb, double freqInMHz)    {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }
}
