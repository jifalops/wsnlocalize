package com.jifalops.wsnlocalize.wifi;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jifalops.wsnlocalize.R;

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
        scanner = WifiScanner.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (scanner.manager.isWifiEnabled()) {
            startScanning();
        } else {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            scanner.startScanning();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        int count = 0;
        @Override
        public void onScanResults(List<ScanResult> scanResults) {
            textView.append("Found " + scanResults.size() + " devices:\n");
            for (ScanResult sr : scanResults) {
                textView.append(sr.level + "dBm " + sr.frequency + "MHz " +
                        sr.SSID + " " + sr.BSSID + "\n");
            }
            count++;
            if (count == 10) {
                try {
                    SoftApManager.getInstance(WifiScannerDemoActivity.this).setEnabled(true);
                } catch (NoSuchMethodException e) {
                    Toast.makeText(WifiScannerDemoActivity.this,
                            "Hot-spot not available.", Toast.LENGTH_LONG).show();
                }
            }
        }
    };
}
