package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jifalops.wsnlocalize.data.RssiHelper;
import com.jifalops.wsnlocalize.data.SampleList;
import com.jifalops.wsnlocalize.data.SampleListSourceInfo;
import com.jifalops.wsnlocalize.data.SampleWindow;
import com.jifalops.wsnlocalize.data.SamplesHelper;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 */
public class SampleCreatorActivity extends Activity {
    static final String TAG = SampleCreatorActivity.class.getSimpleName();

    private static final SampleWindow defaultBtWindow = new SampleWindow(3, 10_000, 5, 120_000);
    private static final SampleWindow defaultBtleWindow = new SampleWindow(15, 5_000, 20, 30_000);
    private static final SampleWindow defaultWifiWindow = new SampleWindow(5, 5_000, 20, 20_000);

    EditText btMinCount, btMinTime, btMaxCount, btMaxTime,
            btleMinCount, btleMinTime, btleMaxCount, btleMaxTime,
            wifiMinCount, wifiMinTime, wifiMaxCount, wifiMaxTime,
            wifi5gMinCount, wifi5gMinTime, wifi5gMaxCount, wifi5gMaxTime;
    GridLayout samplesLayout;

    SampleWindow btWindow, btleWindow, wifiWindow, wifi5gWindow;

    SharedPreferences prefs;

    RssiHelper rssiHelper;
    SamplesHelper samplesHelper;
    
    SampleList btSamples, btleSamples, wifiSamples, wifi5gSamples;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_creator);

        samplesHelper = SamplesHelper.getInstance();
        rssiHelper = RssiHelper.getInstance();
        if (rssiHelper.isLoaded()) {
            ((TextView) findViewById(R.id.btRssi)).setText(rssiHelper.getBt().size() + "");
            ((TextView) findViewById(R.id.btleRssi)).setText(rssiHelper.getBtle().size() + "");
            ((TextView) findViewById(R.id.wifiRssi)).setText(rssiHelper.getWifi().size() + "");
            ((TextView) findViewById(R.id.wifi5gRssi)).setText(rssiHelper.getWifi5g().size() + "");
        } else {
            Toast.makeText(this, "RSSI not loaded. Wait and try again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        btMinCount = (EditText) findViewById(R.id.btMinCount);
        btMinTime = (EditText) findViewById(R.id.btMinTime);
        btMaxCount = (EditText) findViewById(R.id.btMaxCount);
        btMaxTime = (EditText) findViewById(R.id.btMaxTime);

        btleMinCount = (EditText) findViewById(R.id.btleMinCount);
        btleMinTime = (EditText) findViewById(R.id.btleMinTime);
        btleMaxCount = (EditText) findViewById(R.id.btleMaxCount);
        btleMaxTime = (EditText) findViewById(R.id.btleMaxTime);

        wifiMinCount = (EditText) findViewById(R.id.wifiMinCount);
        wifiMinTime = (EditText) findViewById(R.id.wifiMinTime);
        wifiMaxCount = (EditText) findViewById(R.id.wifiMaxCount);
        wifiMaxTime = (EditText) findViewById(R.id.wifiMaxTime);

        wifi5gMinCount = (EditText) findViewById(R.id.wifi5gMinCount);
        wifi5gMinTime = (EditText) findViewById(R.id.wifi5gMinTime);
        wifi5gMaxCount = (EditText) findViewById(R.id.wifi5gMaxCount);
        wifi5gMaxTime = (EditText) findViewById(R.id.wifi5gMaxTime);

        samplesLayout = (GridLayout)findViewById(R.id.samplesGrid);

        prefs = getSharedPreferences(TAG, MODE_PRIVATE);
        
        loadWindows();
        showWindows();
    }
    
    private void loadWindows() {
        btWindow = new SampleWindow(
                prefs.getInt("btMinCount", defaultBtWindow.minCount),
                prefs.getLong("btMinTime", defaultBtWindow.minTime),
                prefs.getInt("btMaxCount", defaultBtWindow.maxCount),
                prefs.getLong("btMaxTime", defaultBtWindow.maxTime));

        btleWindow = new SampleWindow(
                prefs.getInt("btleMinCount", defaultBtleWindow.minCount),
                prefs.getLong("btleMinTime", defaultBtleWindow.minTime),
                prefs.getInt("btleMaxCount", defaultBtleWindow.maxCount),
                prefs.getLong("btleMaxTime", defaultBtleWindow.maxTime));

        wifiWindow = new SampleWindow(
                prefs.getInt("wifiMinCount", defaultWifiWindow.minCount),
                prefs.getLong("wifiMinTime", defaultWifiWindow.minTime),
                prefs.getInt("wifiMaxCount", defaultWifiWindow.maxCount),
                prefs.getLong("wifiMaxTime", defaultWifiWindow.maxTime));

        wifi5gWindow = new SampleWindow(
                prefs.getInt("wifi5gMinCount", defaultWifiWindow.minCount),
                prefs.getLong("wifi5gMinTime", defaultWifiWindow.minTime),
                prefs.getInt("wifi5gMaxCount", defaultWifiWindow.maxCount),
                prefs.getLong("wifi5gMaxTime", defaultWifiWindow.maxTime));
    }

    private void updateWindows() {
        btWindow = new SampleWindow(
                Integer.valueOf(btMinCount.getText().toString()),
                Long.valueOf(btMinTime.getText().toString()),
                Integer.valueOf(btMaxCount.getText().toString()),
                Long.valueOf(btMaxTime.getText().toString()));

        btleWindow = new SampleWindow(
                Integer.valueOf(btleMinCount.getText().toString()),
                Long.valueOf(btleMinTime.getText().toString()),
                Integer.valueOf(btleMaxCount.getText().toString()),
                Long.valueOf(btleMaxTime.getText().toString()));

        wifiWindow = new SampleWindow(
                Integer.valueOf(wifiMinCount.getText().toString()),
                Long.valueOf(wifiMinTime.getText().toString()),
                Integer.valueOf(wifiMaxCount.getText().toString()),
                Long.valueOf(wifiMaxTime.getText().toString()));

        wifi5gWindow = new SampleWindow(
                Integer.valueOf(wifi5gMinCount.getText().toString()),
                Long.valueOf(wifi5gMinTime.getText().toString()),
                Integer.valueOf(wifi5gMaxCount.getText().toString()),
                Long.valueOf(wifi5gMaxTime.getText().toString()));
    }
    
    private void saveWindows() {
        prefs.edit()
                .putInt("btMinCount", btWindow.minCount)
                .putLong("btMinTime", btWindow.minTime)
                .putInt("btMaxCount", btWindow.maxCount)
                .putLong("btMaxTime", btWindow.maxTime)

                .putInt("btleMinCount", btleWindow.minCount)
                .putLong("btleMinTime", btleWindow.minTime)
                .putInt("btleMaxCount", btleWindow.maxCount)
                .putLong("btleMaxTime", btleWindow.maxTime)

                .putInt("wifiMinCount", wifiWindow.minCount)
                .putLong("wifiMinTime", wifiWindow.minTime)
                .putInt("wifiMaxCount", wifiWindow.maxCount)
                .putLong("wifiMaxTime", wifiWindow.maxTime)

                .putInt("wifi5gMinCount", wifi5gWindow.minCount)
                .putLong("wifi5gMinTime", wifi5gWindow.minTime)
                .putInt("wifi5gMaxCount", wifi5gWindow.maxCount)
                .putLong("wifi5gMaxTime", wifi5gWindow.maxTime).apply();
    }

    private void showWindows() {
        btMinCount.setText(btWindow.minCount+"");
        btMinTime.setText(btWindow.minTime+"");
        btMaxCount.setText(btWindow.maxCount+"");
        btMaxTime.setText(btWindow.maxTime+"");

        btleMinCount.setText(btleWindow.minCount+"");
        btleMinTime.setText(btleWindow.minTime+"");
        btleMaxCount.setText(btleWindow.maxCount+"");
        btleMaxTime.setText(btleWindow.maxTime+"");

        wifiMinCount.setText(wifiWindow.minCount+"");
        wifiMinTime.setText(wifiWindow.minTime+"");
        wifiMaxCount.setText(wifiWindow.maxCount + "");
        wifiMaxTime.setText(wifiWindow.maxTime+"");

        wifi5gMinCount.setText(wifi5gWindow.minCount+"");
        wifi5gMinTime.setText(wifi5gWindow.minTime+"");
        wifi5gMaxCount.setText(wifi5gWindow.maxCount+"");
        wifi5gMaxTime.setText(wifi5gWindow.maxTime+"");
    }

    public void makeSamples(View view) {
        updateWindows();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                btSamples = new SampleList(rssiHelper.getBt(), btWindow);
                btleSamples = new SampleList(rssiHelper.getBtle(), btleWindow);
                wifiSamples = new SampleList(rssiHelper.getWifi(), wifiWindow);
                wifi5gSamples = new SampleList(rssiHelper.getWifi5g(), wifi5gWindow);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                showSamples();
            }
        }.execute();

    }

    private void showSamples() {
        // Get all distances
        SampleList all = new SampleList();
        all.addAll(btSamples);
        all.addAll(btleSamples);
        all.addAll(wifiSamples);
        all.addAll(wifi5gSamples);
        TreeMap<Double, SampleList> map = new TreeMap<>(all.splitByDistance());

        Map<Double, SampleList> btMap = btSamples.splitByDistance();
        Map<Double, SampleList> btleMap = btleSamples.splitByDistance();
        Map<Double, SampleList> wifiMap = wifiSamples.splitByDistance();
        Map<Double, SampleList> wifi5gMap = wifi5gSamples.splitByDistance();
        
        TextView dist, bt, btle, wifi, wifi5g, total;
        LayoutInflater inf = getLayoutInflater();
        
        SampleList tmp;
        int count;
        samplesLayout.removeAllViews();
        for (Double d : map.keySet()) {
            dist = (TextView) inf.inflate(R.layout.gridtext, samplesLayout, false);
            bt = (TextView) inf.inflate(R.layout.gridtext, samplesLayout, false);
            btle = (TextView) inf.inflate(R.layout.gridtext, samplesLayout, false);
            wifi = (TextView) inf.inflate(R.layout.gridtext, samplesLayout, false);
            wifi5g = (TextView) inf.inflate(R.layout.gridtext, samplesLayout, false);
            total = (TextView) inf.inflate(R.layout.gridtext, samplesLayout, false);
            
            dist.setText(String.format(Locale.US, "%.1fm", d));
            
            tmp = btMap.get(d);
            count = tmp == null ? 0 : tmp.size();
            bt.setText(count+"");

            tmp = btleMap.get(d);
            count = tmp == null ? 0 : tmp.size();
            btle.setText(count+"");

            tmp = wifiMap.get(d);
            count = tmp == null ? 0 : tmp.size();
            wifi.setText(count+"");

            tmp = wifi5gMap.get(d);
            count = tmp == null ? 0 : tmp.size();
            wifi5g.setText(count+"");

            total.setText(map.get(d).size()+"");

            samplesLayout.addView(dist);
            samplesLayout.addView(bt);
            samplesLayout.addView(btle);
            samplesLayout.addView(wifi);
            samplesLayout.addView(wifi5g);
            samplesLayout.addView(total);
        }

        dist = (TextView) inf.inflate(R.layout.gridtext, samplesLayout, false);
        bt = (TextView) inf.inflate(R.layout.gridtext, samplesLayout, false);
        btle = (TextView) inf.inflate(R.layout.gridtext, samplesLayout, false);
        wifi = (TextView) inf.inflate(R.layout.gridtext, samplesLayout, false);
        wifi5g = (TextView) inf.inflate(R.layout.gridtext, samplesLayout, false);
        total = (TextView) inf.inflate(R.layout.gridtext, samplesLayout, false);

        dist.setText("Totals");
        bt.setText(btSamples.size()+"");
        btle.setText(btleSamples.size()+"");
        wifi.setText(wifiSamples.size()+"");
        wifi5g.setText(wifi5gSamples.size()+"");
        total.setText(all.size()+"");

        samplesLayout.addView(dist);
        samplesLayout.addView(bt);
        samplesLayout.addView(btle);
        samplesLayout.addView(wifi);
        samplesLayout.addView(wifi5g);
        samplesLayout.addView(total);
    }

    public void saveSamples(View view) {
        if (btSamples != null && btSamples.size() > 0) {
            samplesHelper.save(new SampleListSourceInfo(App.SIGNAL_BT,
                    rssiHelper.getBt().size(), btWindow, App.Files.EXT_SAMPLES), btSamples, null);
        }
        if (btleSamples != null && btleSamples.size() > 0) {
            samplesHelper.save(new SampleListSourceInfo(App.SIGNAL_BTLE,
                    rssiHelper.getBtle().size(), btleWindow, App.Files.EXT_SAMPLES), btleSamples, null);
        }
        if (wifiSamples != null && wifiSamples.size() > 0) {
            samplesHelper.save(new SampleListSourceInfo(App.SIGNAL_WIFI,
                    rssiHelper.getWifi().size(), wifiWindow, App.Files.EXT_SAMPLES), wifiSamples, null);
        }
        if (wifi5gSamples != null && wifi5gSamples.size() > 0) {
            samplesHelper.save(new SampleListSourceInfo(App.SIGNAL_WIFI5G,
                    rssiHelper.getWifi5g().size(), wifi5gWindow, App.Files.EXT_SAMPLES), wifi5gSamples, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveWindows();
    }
}
