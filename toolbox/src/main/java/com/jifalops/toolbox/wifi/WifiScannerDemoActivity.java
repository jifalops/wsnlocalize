package com.jifalops.toolbox.wifi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jifalops.toolbox.R;

import java.util.List;

public class WifiScannerDemoActivity extends Activity {
    private WifiScanner scanner;
    private TextView textView;
    private boolean enabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifiscanner);
        final ScrollView scroll = (ScrollView) findViewById(R.id.scrollView);
        textView = (TextView) findViewById(R.id.text);
        textView.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                scroll.post(new Runnable() {
                    @Override
                    public void run() {
                        scroll.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
        scanner = new WifiScanner(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (scanner.manager.isWifiEnabled()) {
            startScanning();
        } else {
            textView.append("Turning on WiFi...\n");
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (scanner.manager.isWifiEnabled()) {
                        unregisterReceiver(this);
                        startScanning();
                    }
                }
            }, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
            scanner.manager.setWifiEnabled(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScanning();
    }

    private void startScanning() {
        if (enabled) return;
        enabled = true;
        scanner.registerListener(scanListener);
        scanner.startScanning();
    }

    private void stopScanning() {
        if (!enabled) return;
        enabled = false;
        scanner.stopScanning();
        scanner.unregisterListener(scanListener);
    }

    private final WifiScanner.ScanListener scanListener = new WifiScanner.ScanListener() {
        @Override
        public void onScanResults(List<ScanResult> scanResults) {
            textView.append("Found " + scanResults.size() + " devices:\n");
            for (ScanResult sr : scanResults) {
                textView.append(sr.level + "dBm " + sr.frequency + "MHz " +
                        sr.SSID + " " + sr.BSSID + "\n");
            }
        }
    };
}
