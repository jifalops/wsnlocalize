package com.jifalops.wsnlocalize.toolbox.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;

/**
 *
 */
public class BtHelper {
    private static BtHelper instance;
    public static BtHelper getInstance(Context ctx) {
        if (instance == null) instance = new BtHelper(ctx);
        return instance;
    }

    private Context ctx;

    private BtHelper(Context ctx) {
        this.ctx = ctx.getApplicationContext();
    }

    public BluetoothAdapter getAdapter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager btManager = (BluetoothManager) ctx.getSystemService(
                    Context.BLUETOOTH_SERVICE);
            return btManager.getAdapter();
        } else {
            return BluetoothAdapter.getDefaultAdapter();
        }
    }

    public void enableBluetooth() {
        enableBluetooth(null, 0);
    }
    public void enableBluetooth(Activity a, int reqCode) {
        BluetoothAdapter adapter = getAdapter();
        if (adapter == null || !adapter.enable()) {
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (a == null) {
                ctx.startActivity(i);
            } else {
                a.startActivityForResult(i, reqCode);
            }
        }
    }

    @Nullable
    public String getMacAddress() {
        BluetoothAdapter adapter = getAdapter();
        return adapter != null ? adapter.getAddress() : null;
    }
}
