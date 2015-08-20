package com.jifalops.wsnlocalize.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * BtBeacon manages turning on Bluetooth, making the device discoverable, and discovering
 * other devices. Other classes can observe events by calling
 * {@link #registerListener(BtBeaconListener)}.
 */
public class BtBeacon {
    private static BtBeacon instance;
    public static BtBeacon getInstance(Context ctx) {
        if (instance == null) {
            instance = new BtBeacon(ctx);
        }
        return instance;
    }
    private BtBeacon(Context ctx) {
        this.ctx = ctx.getApplicationContext();
    }

    private Context ctx;
    private BluetoothAdapter btAdapter;
    private boolean registered;

    public BluetoothAdapter getAdapter() {
        return btAdapter;
    }

    public boolean isDiscoverable() {
        return btAdapter != null &&
                btAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;
    }

    /**
     * Start the beaconing process. The user will be prompted to enable discoverability.
     * Be careful not to ask them repeatedly. If they say no, exit gracefully.
     */
    public void startBeaconing() {
        startBeaconing(null, 0);
    }

    /**
     * Start the beaconing process. The user will be prompted to enable discoverability.
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

        if (btAdapter == null || !btAdapter.isEnabled() ||
                btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            requestBtDiscoverable(3600, a, reqCode);
            return;
        }

        ctx.registerReceiver(scanModeChangedReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
        ctx.registerReceiver(scanReceiver,
                new IntentFilter(BluetoothDevice.ACTION_FOUND));
        ctx.registerReceiver(discoveryStartedReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        ctx.registerReceiver(discoveryFinishedReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        registered = true;
        btAdapter.startDiscovery();
    }

    public void stopBeaconing() {
        stopBeaconing(null, 0);
    }
    public void stopBeaconing(Activity a, int reqCode) {
        if (btAdapter != null && btAdapter.isEnabled() &&
                btAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            btAdapter.cancelDiscovery();
            requestBtDiscoverable(1, a, reqCode);
            if (registered) {
                ctx.unregisterReceiver(scanModeChangedReceiver);
                ctx.unregisterReceiver(scanReceiver);
                ctx.unregisterReceiver(discoveryStartedReceiver);
                ctx.unregisterReceiver(discoveryFinishedReceiver);
                registered = false;
            }
        }
    }

    private void requestBtDiscoverable(int duration, Activity a, int reqCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
        if (a != null) {
            a.startActivityForResult(intent, reqCode);
        } else {
            ctx.startActivity(intent);
        }
    }

    private final BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device != null) {
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                for (BtBeaconListener l : listeners) {
                    l.onDeviceFound(device, rssi);
                }
            }
        }
    };

    private final BroadcastReceiver discoveryStartedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            for (BtBeaconListener l : listeners) {
                l.onDiscoveryStarting();
            }
        }
    };

    private final BroadcastReceiver discoveryFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            btAdapter.startDiscovery();
        }
    };

    private final BroadcastReceiver scanModeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int newMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1);
            int oldMode = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE, -1);
            if (oldMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE &&
                    newMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                for (BtBeaconListener l : listeners) {
                    l.onThisDeviceDiscoverableChanged(true);
                }
            } else if (oldMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE &&
                    newMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                for (BtBeaconListener l : listeners) {
                    l.onThisDeviceDiscoverableChanged(false);
                }
            }
        }
    };


    public interface BtBeaconListener {
        void onDeviceFound(BluetoothDevice device, short rssi);
        void onThisDeviceDiscoverableChanged(boolean discoverable);
        void onDiscoveryStarting();
    }
    private final List<BtBeaconListener> listeners = new ArrayList<>(1);
    public boolean registerListener(BtBeaconListener l) {
        return !listeners.contains(l) && listeners.add(l);
    }
    public boolean unregisterListener(BtBeaconListener l) {
        return listeners.remove(l);
    }
}
