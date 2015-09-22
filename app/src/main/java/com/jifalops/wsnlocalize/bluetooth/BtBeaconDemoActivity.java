package com.jifalops.wsnlocalize.bluetooth;

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

import com.jifalops.wsnlocalize.R;


public class BtBeaconDemoActivity extends Activity {
    private static final int REQUEST_BT_DISCOVERABLE = 1;
    private BtBeacon btBeacon;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btbeacon);
        final ScrollView scroll = (ScrollView) findViewById(R.id.scrollView);
        textView = (TextView) findViewById(R.id.text);
        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                scroll.post(new Runnable() {
                    @Override
                    public void run() {
                        scroll.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
        btBeacon = BtBeacon.getInstance(this);
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
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }


    private final BtBeacon.BtBeaconListener btBeaconListener = new BtBeacon.BtBeaconListener() {
        @Override
        public void onDeviceFound(BluetoothDevice device, short rssi) {
            textView.append("Received " + rssi + "dBm from " + device.getAddress() + " (" +
                    String.format("%1.1f", freeSpacePathLoss(rssi, 2400)) + "m).\n");
        }

        /** @return The distance in meters. */
        public  float freeSpacePathLoss(float levelInDb, float freqInMHz)    {
            double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
            return (float) Math.pow(10.0, exp);
        }

        @Override
        public void onThisDeviceDiscoverableChanged(boolean discoverable) {
            if (discoverable) {
                textView.append("Mac is now discoverable.\n");
            } else {
                textView.append("Mac is no longer discoverable.\n");
            }
        }

        @Override
        public void onDiscoveryStarting() {
            textView.append("Discovery is (re)starting...\n");
        }
    };
}
