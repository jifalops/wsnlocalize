package com.jifalops.wsnlocalize.toolbox.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class WifiScanner {
    Context context;
    WifiManager manager;
    boolean enabled;
    int intervalMillis;

    public WifiScanner(Context ctx) {
        context = ctx;
        manager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
    }

    public void startScanning() { startScanning(100); }
    public void startScanning(int intervalMillis) {
        this.intervalMillis = intervalMillis;
        if (enabled) return;
        enabled = true;
        context.registerReceiver(scanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        manager.startScan();
    }

    public void stopScanning() {
        if (!enabled) return;
        enabled = false;
        context.unregisterReceiver(scanReceiver);
    }

    private final BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> results = manager.getScanResults();
            for (ScanListener l : listeners) {
                l.onScanResults(results);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    manager.startScan();
                }
            }, intervalMillis);
        }
    };


    public interface ScanListener {
        void onScanResults(List<ScanResult> scanResults);
    }
    private final List<ScanListener> listeners = new ArrayList<ScanListener>(1);
    public boolean registerListener(ScanListener l) {
        return !listeners.contains(l) && listeners.add(l);
    }
    public boolean unregisterListener(ScanListener l) {
        return listeners.remove(l);
    }
}
