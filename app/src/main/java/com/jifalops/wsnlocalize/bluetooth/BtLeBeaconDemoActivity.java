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
import android.os.Bundle;
import android.os.ParcelUuid;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jifalops.wsnlocalize.R;
import com.jifalops.wsnlocalize.util.Calc;

import java.util.List;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BtLeBeaconDemoActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 1;
    BtLeBeacon btLeBeacon;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Toast.makeText(this, "BT-LE Beaconing is only supported on Android 5.0 and above.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_btlebeacon);
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
        btLeBeacon = BtLeBeacon.getInstance(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "App cannot work with Bluetooth disabled.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFinishing()) {
            return;
        }
        btLeBeacon.registerListener(btLeListener);
        btLeBeacon.startBeaconing(this, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        btLeBeacon.stopBeaconing();
        btLeBeacon.unregisterListener(btLeListener);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private final BtLeBeacon.BtLeBeaconListener btLeListener = new BtLeBeacon.BtLeBeaconListener() {
        @Override
        public void onAdvertiseNotSupported() {
            textView.append("Advertisement not supported. Scanning for other devices...\n");
        }

        @Override
        public void onAdvertiseStartSuccess(AdvertiseSettings settingsInEffect) {
            textView.append("Started advertising at " + settingsInEffect.getTxPowerLevel() + "dBm.\n");
        }

        @Override
        public void onAdvertiseStartFailure(int errorCode) {
            switch (errorCode) {
                case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                    textView.append("Advertise failed: already started.\n");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                    textView.append("Advertise failed: data too large.\n");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    textView.append("Advertise failed: feature unsupported.\n");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                    textView.append("Advertise failed: internal error.\n");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    textView.append("Advertise failed: too many advertisers.\n");
                    break;
            }
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            printScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            textView.append("Received " + results.size() + " batch results:\n");
            for (ScanResult r : results) {
                printScanResult(r);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            switch (errorCode) {
                case ScanCallback.SCAN_FAILED_ALREADY_STARTED:
                    textView.append("Scan failed: already started.\n");
                    break;
                case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    textView.append("Scan failed: app registration failed.\n");
                    break;
                case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED:
                    textView.append("Scan failed: feature unsupported.\n");
                    break;
                case ScanCallback.SCAN_FAILED_INTERNAL_ERROR:
                    textView.append("Scan failed: internal error.\n");
                    break;
            }
        }

        private void printScanResult(ScanResult result) {
            String id = result.getDevice() != null ? result.getDevice().getAddress() : "unknown";
            int tx = result.getScanRecord() != null ? result.getScanRecord().getTxPowerLevel() : 0;
            textView.append("TX: " + tx + " RX: " + result.getRssi() + " from " + id + " (" +
                    String.format("%1.1f", Calc.freeSpacePathLoss(result.getRssi(), 2400)) + "m).\n");
        }
    };
}
