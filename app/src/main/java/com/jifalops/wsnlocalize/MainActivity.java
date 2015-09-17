package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * MainActivity is the starting point when using the app. Its main purpose is to allow the user
 * to enter various areas of the app.
 */
public class MainActivity extends Activity {
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = (LinearLayout) findViewById(R.id.linearLayout);
        addToLayout("Wifi: " + App.getInstance().getWifiMac(),
                    "BT:   " + App.getInstance().getBtMac(), null);
        addToLayout("Demos", "Examples of app components", DemoActivity.class);
        addToLayout("RSSI Training", "Train neural network in real time", RssiTrainingActivity.class);
        addToLayout("RSSI Distance", "Estimate distances without training", DistanceEstimationActivity.class);
    }

    private void addToLayout(String text, final Class<?> clazz) {
        View v = getLayoutInflater().inflate(android.R.layout.simple_list_item_activated_1, layout, false);
        ((TextView) v.findViewById(android.R.id.text1)).setText(text);
        if (clazz != null) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, clazz));
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
                    startActivity(new Intent(MainActivity.this, clazz));
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
