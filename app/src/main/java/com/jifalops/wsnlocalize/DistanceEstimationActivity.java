package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.jifalops.wsnlocalize.bluetooth.BtBeacon;
import com.jifalops.wsnlocalize.bluetooth.BtLeBeacon;
import com.jifalops.wsnlocalize.data.DataFileInfo;
import com.jifalops.wsnlocalize.data.Rssi;
import com.jifalops.wsnlocalize.data.RssiSample;
import com.jifalops.wsnlocalize.data.helper.InfoFileHelper;
import com.jifalops.wsnlocalize.toolbox.util.ResettingList;
import com.jifalops.wsnlocalize.toolbox.wifi.WifiScanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class DistanceEstimationActivity extends Activity {
    static final String TAG = DistanceEstimationActivity.class.getSimpleName();

    static final int REQUEST_BT_ENABLE = 1;
    static final int REQUEST_BT_DISCOVERABLE = 2;

    DeviceEstimatorAdapter adapter;

    BtBeacon bt;
    BtLeBeacon btle;
    WifiScanner wifi;


    List<DeviceEstimatorAdapter.Device> devices = new ArrayList<>();
    Map<DeviceEstimatorAdapter.Device, List<ResettingList<Rssi>>> windowers = new HashMap<>();

    CheckBox btCheckbox, btleCheckbox, wifiCheckbox, wifi5gCheckbox;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distanceestimates);

        adapter = new DeviceEstimatorAdapter(this);
        ((RecyclerView) findViewById(R.id.recycler)).setAdapter(adapter);

        bt = BtBeacon.getInstance(this);
        btle = BtLeBeacon.getInstance(this);
        wifi = new WifiScanner(this);

        prefs = getSharedPreferences(TAG, MODE_PRIVATE);

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

        /*bestEstimator = new BestDistanceEstimator(true, true, new BestDistanceEstimator.OnReadyListener() {
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
        });*/
    }

    void setupControls() {
//        estimator = useBest ? bestEstimator : toSendEstimator;
//        btCheckbox.setEnabled(estimator.getBtSize() > 0);
//        btleCheckbox.setEnabled(estimator.getBtleSize() > 0);
//        wifiCheckbox.setEnabled(estimator.getWifiSize() > 0);
//        wifi5gCheckbox.setEnabled(estimator.getWifi5gSize() > 0);
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
        bt.unregisterListener(btBeaconListener);
        btle.unregisterListener(btLeBeaconListener);
        wifi.unregisterListener(wifiScanListener);
        for (List<ResettingList<Rssi>> list: windowers.values()) {
            for (ResettingList r : list) {
                r.reset();
            }
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
//        menu.findItem(R.id.useBest).setChecked(useBest);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.useBest) {
//            useBest = !useBest;
            setupControls();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void reportSignal(String mac, String name, final String signal, int rssi, int freq) {
        DeviceEstimatorAdapter.Device device = null;
        List<ResettingList<Rssi>> windows = null;
        for (DeviceEstimatorAdapter.Device d : windowers.keySet()) {
            if (d.mac.equals(mac)) {
                device = d;
                windows = windowers.get(device);
                break;
            }
        }
        if (device == null) {
            device = new DeviceEstimatorAdapter.Device(mac, name, signal);
            devices.add(device);
            InfoFileHelper helper = InfoFileHelper.getInstance();
            List<DataFileInfo> infos = helper.get(signal);
            windows = new ArrayList<>(infos.size());
            for (final DataFileInfo info : infos) {
                final DeviceEstimatorAdapter.Device finalDevice = device;
                windows.add(new ResettingList<>(info.window, new ResettingList.LimitsCallback<Rssi>() {
                    @Override
                    public void onLimitsReached(List<Rssi> list, long time) {
                        finalDevice.estimate(new RssiSample(list), info.window);
                    }
                }));
            }
            windowers.put(device, windows);
        }

        for (ResettingList<Rssi> list : windows) {
            list.add(new Rssi(mac, rssi, freq,
                    System.currentTimeMillis(), 0));
        }
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
