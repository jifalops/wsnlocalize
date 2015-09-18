package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jifalops.wsnlocalize.bluetooth.BtBeaconDemoActivity;
import com.jifalops.wsnlocalize.bluetooth.BtLeBeaconDemoActivity;
import com.jifalops.wsnlocalize.nsd.NsdDemoActivity;
import com.jifalops.wsnlocalize.wifi.WifiScannerDemoActivity;

/**
 *
 */
public class DemoActivity extends Activity {
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        layout = (LinearLayout) findViewById(R.id.linearLayout);
        addToLayout("Wifi: " + App.getInstance().getWifiMac(),
                    "BT:   " + App.getInstance().getBtMac(), null);
        addToLayout("Bluetooth",  "Beacon & Scanner", BtBeaconDemoActivity.class);
        addToLayout("Bluetooth Low Energy", "Beacon & Scanner", BtLeBeaconDemoActivity.class);
        addToLayout("WiFi", "Scanner only", WifiScannerDemoActivity.class);
        addToLayout("Network Service Discovery (chat)", "Requires multiple devices", NsdDemoActivity.class);
    }

    private void addToLayout(String text, final Class<?> clazz) {
        View v = getLayoutInflater().inflate(android.R.layout.simple_list_item_activated_1, layout, false);
        ((TextView) v.findViewById(android.R.id.text1)).setText(text);
        if (clazz != null) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(DemoActivity.this, clazz));
                }
            });
        }
        layout.addView(v);
    }

    private void addToLayout(String line1, String line2, final Class<?> clazz) {
        View v = getLayoutInflater().inflate(android.R.layout.simple_list_item_activated_2, layout, false);
        ((TextView) v.findViewById(android.R.id.text1)).setText(line1);
        ((TextView) v.findViewById(android.R.id.text2)).setText(line2);
        if (clazz != null) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(DemoActivity.this, clazz));
                }
            });
        }
        layout.addView(v);
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
}
