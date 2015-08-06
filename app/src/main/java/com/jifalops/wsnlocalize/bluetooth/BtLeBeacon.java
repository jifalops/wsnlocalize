package com.jifalops.wsnlocalize.bluetooth;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BtLeBeacon {
    private static final String TAG = BtLeBeacon.class.getSimpleName();
    /** Randomly generated online */
    private static final String SERVICE_UUID = "4a07c69e-3c6c-11e5-a151-feff819cdc9f";

    private static BtLeBeacon instance;
    public static BtLeBeacon getInstance(Context ctx) {
        if (instance == null) {
            instance = new BtLeBeacon(ctx);
        }
        return instance;
    }
    private BtLeBeacon(Context ctx) {
        this.ctx = ctx.getApplicationContext();
    }

    private Context ctx;
    BluetoothAdapter btAdapter;

    /**
     * Start the beaconing process. The user will be prompted to enable Bluetooth if necessary.
     * Be careful not to ask them repeatedly. If they say no, exit gracefully.
     */
    public void startBeaconing() {
        startBeaconing(null, 0);
    }

    /**
     * Start the beaconing process. The user will be prompted to enable Bluetooth if necessary.
     * The specified Activity will get the result of the users decision in
     * {@link Activity#onActivityResult(int, int, Intent)} (called just before onResume()).
     */
    public void startBeaconing(Activity a, int reqCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager btManager = (BluetoothManager) ctx.getSystemService(
                    Context.BLUETOOTH_SERVICE);
            btAdapter = btManager.getAdapter();
        } else {
            btAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        if (btAdapter == null || !btAdapter.isEnabled()) {
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (a == null) {
                ctx.startActivity(i);
            } else {
                a.startActivityForResult(i, reqCode);
            }
            return;
        }

        // Choose advertise settings for long range / high powered advertising.
        AdvertiseSettings.Builder settings = new AdvertiseSettings.Builder();
        settings.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        settings.setConnectable(false); // We are not handling connections.
        settings.setTimeout(0); // No time limit;
        settings.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH); // Long range.

        AdvertiseData.Builder data = new AdvertiseData.Builder();
        data.addServiceUuid(new ParcelUuid(UUID.fromString(SERVICE_UUID)));
        data.setIncludeDeviceName(false);
        data.setIncludeTxPowerLevel(true);

        if (btAdapter.getBluetoothLeAdvertiser() != null) {
            btAdapter.getBluetoothLeAdvertiser().startAdvertising(settings.build(), data.build(), adCallback);
        } else {
            Log.w(TAG, "Advertisement not supported.");
            for (BtLeBeaconListener l : listeners) {
                l.onAdvertiseNotSupported();
            }
        }
        btAdapter.getBluetoothLeScanner().startScan(scanCallback);
    }

    public void stopBeaconing() {
        if (btAdapter != null && btAdapter.isEnabled()) {
            if (btAdapter.getBluetoothLeAdvertiser() != null) {
                btAdapter.getBluetoothLeAdvertiser().stopAdvertising(adCallback);
            }
            btAdapter.getBluetoothLeScanner().stopScan(scanCallback);
            btAdapter.getBluetoothLeScanner().flushPendingScanResults(scanCallback);
        }
    }

    private final AdvertiseCallback adCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(TAG, "Started advertising at " + settingsInEffect.getTxPowerLevel() + "dBm.");
            for (BtLeBeaconListener l : listeners) {
                l.onAdvertiseStartSuccess(settingsInEffect);
            }
        }

        @Override
        public void onStartFailure(int errorCode) {
            switch (errorCode) {
                case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                    Log.i(TAG, "Advertise failed: already started.");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                    Log.i(TAG, "Advertise failed: data too large.");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    Log.i(TAG, "Advertise failed: feature unsupported.");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                    Log.i(TAG, "Advertise failed: internal error.");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    Log.i(TAG, "Advertise failed: too many advertisers.");
                    break;
            }
            for (BtLeBeaconListener l : listeners) {
                l.onAdvertiseStartFailure(errorCode);
            }
        }
    };

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            logScanResult(result);
            for (BtLeBeaconListener l : listeners) {
                l.onScanResult(callbackType, result);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.i(TAG, "Received " + results.size() + " batch results:");
            for (ScanResult r : results) {
                logScanResult(r);
            }
            for (BtLeBeaconListener l : listeners) {
                l.onBatchScanResults(results);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            switch (errorCode) {
                case ScanCallback.SCAN_FAILED_ALREADY_STARTED:
                    Log.i(TAG, "Scan failed: already started.");
                    break;
                case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    Log.i(TAG, "Scan failed: app registration failed.");
                    break;
                case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED:
                    Log.i(TAG, "Scan failed: feature unsupported.");
                    break;
                case ScanCallback.SCAN_FAILED_INTERNAL_ERROR:
                    Log.i(TAG, "Scan failed: internal error.");
                    break;
            }
            for (BtLeBeaconListener l : listeners) {
                l.onScanFailed(errorCode);
            }
        }

        private void logScanResult(ScanResult result) {
            String id = result.getDevice() != null ? result.getDevice().getAddress() : "unknown";
            int tx = result.getScanRecord() != null ? result.getScanRecord().getTxPowerLevel() : 0;
            Log.i(TAG, "TX: " + tx + " RX: " + result.getRssi() + " from " + id);
        }
    };

    public interface BtLeBeaconListener {
        void onAdvertiseNotSupported();
        /** See {@link AdvertiseCallback#onStartSuccess(AdvertiseSettings)} */
        void onAdvertiseStartSuccess(AdvertiseSettings settingsInEffect);
        /** See {@link AdvertiseCallback#onStartFailure(int)} */
        void onAdvertiseStartFailure(int errorCode);
        /** See {@link ScanCallback#onScanResult(int, ScanResult)} */
        void onScanResult(int callbackType, ScanResult result);
        /** See {@link ScanCallback#onBatchScanResults(List)} */
        void onBatchScanResults(List<ScanResult> results);
        /** See {@link ScanCallback#onScanFailed(int)} */
        void onScanFailed(int errorCode);
    }
    private final List<BtLeBeaconListener> listeners = new ArrayList<>(1);
    public boolean registerListener(BtLeBeaconListener l) {
        return !listeners.contains(l) && listeners.add(l);
    }
    public boolean unregisterListener(BtLeBeaconListener l) {
        return listeners.remove(l);
    }
}
