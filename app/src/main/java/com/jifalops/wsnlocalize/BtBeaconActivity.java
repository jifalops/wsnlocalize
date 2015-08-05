package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


public class BtBeaconActivity extends Activity {
    private static final int REQUEST_BT_DISCOVERABLE = 1;
    private BtBeacon btBeacon;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btbeacon);
        textView = (TextView) findViewById(R.id.text);
        autoScrollTextView(textView, (ScrollView) findViewById(R.id.scrollView));
        btBeacon = BtBeacon.getInstance(this);
    }

    private void autoScrollTextView(TextView tv, final ScrollView sv) {
        tv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sv.post(new Runnable() {
                    @Override
                    public void run() {
                        sv.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BT_DISCOVERABLE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Activity cannot work unless device is discoverable.",
                        Toast.LENGTH_LONG).show();
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
        btBeacon.registerListener(btBeaconListener);
        btBeacon.startBeaconing(this, REQUEST_BT_DISCOVERABLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        btBeacon.stopBeaconing(this, REQUEST_BT_DISCOVERABLE);
        btBeacon.unregisterListener(btBeaconListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private final BtBeacon.BtBeaconListener btBeaconListener = new BtBeacon.BtBeaconListener() {
        @Override
        public void onDeviceFound(BluetoothDevice device, short rssi) {
            textView.append("RX " + rssi + "dBm from " + device.getAddress() + ".\n");
        }

        @Override
        public void onThisDeviceDiscoverableChanged(boolean discoverable) {
            if (discoverable) {
                textView.append("Device is now discoverable.\n");
            } else {
                textView.append("Device is no longer discoverable.\n");
            }
        }

        @Override
        public void onDiscoveryRestarting() {
            textView.append("Discovery is (re)starting...\n");
        }
    };
}
