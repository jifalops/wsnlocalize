package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jifalops.wsnlocalize.data.Estimator;
import com.jifalops.wsnlocalize.data.RssiRecord;
import com.jifalops.wsnlocalize.data.WindowRecord;
import com.jifalops.wsnlocalize.signal.SignalController;
import com.jifalops.wsnlocalize.util.Arrays;
import com.jifalops.wsnlocalize.util.ServiceThreadApplication;
import com.jifalops.wsnlocalize.util.SimpleLog;
import com.jifalops.wsnlocalize.util.Stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 */
public class SampleCollectionActivity extends Activity {
    static final int REQUEST_BT_ENABLE = 1;
    static final int REQUEST_BT_DISCOVERABLE = 2;
    static final String CONTROLLER = SampleCollectionActivity.class.getName() + ".controller";

    TextView eventLogView, deviceLogView,
            btRssiCountView, btWindowCountView, btEstimatorCountView,
            btleRssiCountView, btleWindowCountView, btleEstimatorCountView,
            wifiRssiCountView, wifiWindowCountView, wifiEstimatorCountView,
            wifi5gRssiCountView, wifi5gWindowCountView, wifi5gEstimatorCountView,
            deviceIdView;
    EditText distanceView;
    Switch collectSwitch;
    CheckBox btCheckBox, btleCheckBox, wifiCheckBox;

    SharedPreferences prefs;
    int logLevel;

    ServiceThreadApplication.LocalService service;
    SignalController controller;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssitraining);
        eventLogView = (TextView) findViewById(R.id.eventLog);
        deviceLogView = (TextView) findViewById(R.id.deviceLog);
        btRssiCountView = (TextView) findViewById(R.id.btRssiCount);
        btleRssiCountView = (TextView) findViewById(R.id.btleRssiCount);
        wifiRssiCountView = (TextView) findViewById(R.id.wifiRssiCount);
        wifi5gRssiCountView = (TextView) findViewById(R.id.wifi5gRssiCount);
        btWindowCountView = (TextView) findViewById(R.id.btWindowCount);
        btleWindowCountView = (TextView) findViewById(R.id.btleWindowCount);
        wifiWindowCountView = (TextView) findViewById(R.id.wifiWindowCount);
        wifi5gWindowCountView = (TextView) findViewById(R.id.wifi5gWindowCount);
        btEstimatorCountView = (TextView) findViewById(R.id.btEstimatorCount);
        btleEstimatorCountView = (TextView) findViewById(R.id.btleEstimatorCount);
        wifiEstimatorCountView = (TextView) findViewById(R.id.wifiEstimatorCount);
        wifi5gEstimatorCountView = (TextView) findViewById(R.id.wifi5gEstimatorCount);
        collectSwitch = (Switch) findViewById(R.id.collectSwitch);
        btCheckBox = (CheckBox) findViewById(R.id.btCheckBox);
        btleCheckBox = (CheckBox) findViewById(R.id.btleCheckBox);
        wifiCheckBox = (CheckBox) findViewById(R.id.wifiCheckBox);

        deviceIdView = (TextView) findViewById(R.id.deviceId);
        distanceView = (EditText) findViewById(R.id.distanceMeters);
        Button sendButton = (Button) findViewById(R.id.sendButton);

        autoScroll((ScrollView) findViewById(R.id.eventScrollView), eventLogView);
        autoScroll((ScrollView) findViewById(R.id.deviceScrollView), deviceLogView);
        deviceIdView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder b = new AlertDialog.Builder(SampleCollectionActivity.this);
                final EditText input = new EditText(SampleCollectionActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                input.setLayoutParams(lp);
//                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                b.setView(input);
                b.setTitle("Device IDs (comma separated)");
                b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deviceIdView.setText("");
                        List<Integer> deviceIds = new ArrayList<Integer>();
                        controller.resetKnownDistances();
                        try {
                            String[] ids = input.getText().toString().split(",");
                            int id;
                            for (String s : ids) {
                                id = Integer.valueOf(s);
                                SignalController.Device d = controller.getDevice(id - 1);
                                if (d != null) {
                                    d.isDistanceKnown = true;
                                    deviceIds.add(d.id);
                                }
                            }
                        } catch (Exception ignored) {
                        }
                        boolean first = true;
                        for (int id : deviceIds) {
                            if (first) {
                                deviceIdView.append(id + "");
                                first = false;
                            } else {
                                deviceIdView.append("," + id);
                            }

                        }
                        if (deviceIds.size() == 0) {
                            deviceIdView.setText("0,0,0");
                        }

                    }
                });
                b.show();
            }
        });
        distanceView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    controller.setDistance(Double.valueOf(s.toString()));
                } catch (NumberFormatException e) {
                    distanceView.setText(controller.getDistance() + "");
                }
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.send();
            }
        });

        prefs = getSharedPreferences("rssitraining", MODE_PRIVATE);
        controller = SignalController.getInstance(this);

        App.getInstance().bindLocalService(new Runnable() {
            @Override
            public void run() {
                service = App.getInstance().getService();
                if (service != null && service.isPersistent()) {
                    setupControls();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (service != null && !service.isPersistent()) {
            controller.setCollectEnabled(false);
            controller.close();
            controller = null;
        }
        App.getInstance().unbindLocalService(null);
    }

    void autoScroll(final ScrollView sv, final TextView tv) {
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
        if (requestCode == REQUEST_BT_ENABLE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Activity cannot work unless Bluetooth is enabled.",
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_BT_DISCOVERABLE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Activity cannot work unless device is discoverable.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_samplecollection, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        SubMenu sub = menu.getItem(0);
        menu.findItem(R.id.action_persist).setChecked(service != null && service.isPersistent());
        menu.findItem(R.id.logImportant).setChecked(
                logLevel == SignalController.LOG_IMPORTANT);
        menu.findItem(R.id.logInformative).setChecked(
                logLevel == SignalController.LOG_INFORMATIVE);
        menu.findItem(R.id.logAll).setChecked(
                logLevel == SignalController.LOG_ALL);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SubMenu sub = item.getSubMenu();
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_persist:
                if (service != null && service.isPersistent()) {
                    item.setChecked(false);
                    setPersistent(false);
                } else {
                    item.setChecked(true);
                    setPersistent(true);
                }
                return true;
            case R.id.sampleAll:
                // fall through
            case R.id.sampleWifi:
                // fall through
            case R.id.sampleWifi5g:
                // fall through
            case R.id.sampleBt:
                // fall through
            case R.id.sampleBtle:
                // fall through
                showSamples(item.getItemId());
                return true;
            case R.id.action_clearSend:
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setMessage("Clear send queue?");
                b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        controller.clearPendingSendLists();
                        updateSendCounts();
                    }
                }).show();
                return true;
            case R.id.action_clearTraining:
                AlertDialog.Builder b2 = new AlertDialog.Builder(this);
                b2.setMessage("Clear all training samples?");
                b2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        controller.clearTrainingSamples();
                    }
                }).show();
                return true;
            case R.id.logImportant:
                logLevel = SignalController.LOG_IMPORTANT;
                return true;
            case R.id.logInformative:
                logLevel = SignalController.LOG_INFORMATIVE;
                return true;
            case R.id.logAll:
                logLevel = SignalController.LOG_ALL;
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void showSamples(int id) {
        double[][] samples = null, wifi, wifi5g, bt, btle;
        wifi = controller.getWifi().getSamples();
        wifi5g = controller.getWifi5g().getSamples();
        bt = controller.getBt().getSamples();
        btle = controller.getBtle().getSamples();
        switch (id) {
            case R.id.sampleWifi:
                samples = wifi;
                break;
            case R.id.sampleWifi5g:
                samples = wifi5g;
                break;
            case R.id.sampleBt:
                samples = bt;
                break;
            case R.id.sampleBtle:
                samples = btle;
                break;
            default:
                if (wifi != null) samples = wifi;
                if (wifi5g != null) {
                    samples = samples == null ? wifi5g : Arrays.concat(samples, wifi5g);
                }
                if (bt != null) {
                    samples = samples == null ? bt : Arrays.concat(samples, bt);
                }
                if (btle != null) {
                    samples = samples == null ? btle : Arrays.concat(samples, btle);
                }

        }

        if (samples == null) {
            Toast.makeText(this, "No samples to show", Toast.LENGTH_SHORT).show();
            return;
        }

        final double[][] finalSamples = samples;

        View layout = getLayoutInflater().inflate(R.layout.samples_view, null);
        TextView summary = (TextView) layout.findViewById(R.id.overallSummary);
        ListView distSummariesView = (ListView) layout.findViewById(R.id.distanceSummary);
        ListView samplesView = (ListView) layout.findViewById(R.id.samples);

        summary.setText(new SamplesSummary(samples).toString());

        final List<SamplesSummary> distanceSummaries = makeDistanceSummaries(samples);
        distSummariesView.setAdapter(new ArrayAdapter<SamplesSummary>(this,
                R.layout.listitem_sample, distanceSummaries) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.listitem_sample, parent, false);
                }
                ((TextView) convertView).setText(distanceSummaries.get(position).toString());
                return convertView;
            }
        });


        samplesView.setAdapter(new ArrayAdapter<double[]>(this, R.layout.listitem_sample, finalSamples) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.listitem_sample, parent, false);
                }
                ((TextView) convertView).setText(makeSampleString(finalSamples[position]));
                return convertView;
            }
        });


        new AlertDialog.Builder(this).setView(layout).show();
    }

    String makeSampleString(double[] s) {
        return String.format(Locale.US,
                "%.1fm. med:%.1f avg:%.1f std:%.1f\n" +
                        "#rss:%d #dev:%d tavg:%.1f.\n" +
                        "tmed:%.1f tstd:%.1f ttot:%.1f rmin:%d rmax:%d rrng:%d tmin:%d tmax:%d trng:%d",
                s[15], s[5], s[4], s[6],
                (int) s[0], (int) s[14], s[11] / 1000,
                s[12] / 1000, s[13] / 1000, s[7] / 1000, (int) s[1], (int) s[2], (int) s[3],
                (int) s[8], (int) s[9], (int) s[10]);
    }

    static class SamplesSummary {
        final double dist, avgrssicount, avgdevices,
                rmean, rmedian, rstddev, millis, tmean, tmedian, tstddev;
        final int count, rmin, rmax, rrange, tmin, tmax, trange;
        SamplesSummary(double[][] samples) {
            count = samples.length;
            double[][] cols = Arrays.transpose(samples);
            avgrssicount = Stats.mean(cols[0]);
            rmin = (int) Stats.min(cols[1]);
            rmax = (int) Stats.max(cols[2]);
            rrange = rmax - rmin; // skip cols[3]
            rmean = Stats.mean(cols[4]);
            rmedian = Stats.mean(cols[5]);
            rstddev = Stats.mean(cols[6]);
            millis = Stats.mean(cols[7]);
            tmin = (int) Stats.min(cols[8]);
            tmax = (int) Stats.max(cols[9]);
            trange = tmax - tmin; // skip cols[10]
            tmean = Stats.mean(cols[11]);
            tmedian = Stats.mean(cols[12]);
            tstddev = Stats.mean(cols[13]);
            avgdevices = Stats.mean(cols[14]);
            dist = Stats.mean(cols[15]);
        }

        @Override
        public String toString() {
            return String.format(Locale.US,
                "%.1fm (%d). med:%.1f avg:%.1f std:%.1f\n" +
                "#rss:%.1f #dev:%.1f tavg:%.1f\n" +
                "tmed:%.1f tstd:%.1f ttot:%.1f rmin:%d rmax:%d rrng:%d tmin:%d tmax:%d trng:%d",
                dist, count, rmedian, rmean, rstddev,
                avgrssicount, avgdevices, tmean/1000,
                tmedian/1000, tstddev/1000, millis/1000, rmin, rmax, rrange, tmin, tmax, trange);
        }
    }

    List<SamplesSummary> makeDistanceSummaries(double[][] samples) {
        Map<Double, List<double[]>> distances = new HashMap<>();
        List<SamplesSummary> summaries;
        int rows = samples.length;
        List<double[]> list;
        double dist;
        for (double[] sample : samples) {
            dist = sample[WindowRecord.ACTUAL_DISTANCE_INDEX];
            list = distances.get(dist);
            if (list == null) {
                list = new ArrayList<>();
                distances.put(dist, list);
            }
            list.add(sample);
        }
        summaries = new ArrayList<>(distances.size());
        for (List<double[]> s : distances.values()) {
            summaries.add(new SamplesSummary(s.toArray(new double[s.size()][])));
        }
        return summaries;
    }

    void setPersistent(boolean persist) {
        if (service == null) return;
        Object obj = persist ? controller : null;
        service.setPersistent(persist, getClass());
        service.setCachedObject(getLocalClassName() + CONTROLLER, obj);
    }

    void updateSendCounts() {
        btRssiCountView.setText(controller.getBt().getRssiCount() + "");
        btWindowCountView.setText(controller.getBt().getWindowCount() + "");
        btEstimatorCountView.setText(controller.getBt().getEstimatorCount() + "");
        btleRssiCountView.setText(controller.getBtle().getRssiCount() + "");
        btleWindowCountView.setText(controller.getBtle().getWindowCount() + "");
        btleEstimatorCountView.setText(controller.getBtle().getEstimatorCount() + "");
        wifiRssiCountView.setText(controller.getWifi().getRssiCount() + "");
        wifiWindowCountView.setText(controller.getWifi().getWindowCount() + "");
        wifiEstimatorCountView.setText(controller.getWifi().getEstimatorCount() + "");
        wifi5gRssiCountView.setText(controller.getWifi5g().getRssiCount() + "");
        wifi5gWindowCountView.setText(controller.getWifi5g().getWindowCount() + "");
        wifi5gEstimatorCountView.setText(controller.getWifi5g().getEstimatorCount() + "");
    }

    void loadDevicesAndEvents() {
        deviceLogView.setText("Devices:\n");
        eventLogView.setText("Events:\n");
        for (SignalController.Device d : controller.getDevices()) {
            deviceLogView.append(d.toString() + "\n");
        }
        List<SimpleLog.LogItem> items =  controller.getLog().getByImportance(logLevel, true);
        for (SimpleLog.LogItem item : items) {
            eventLogView.append(item.msg + "\n");
        }
    }

    void setupControls() {
        controller.setShouldUseBt(prefs.getBoolean("btEnabled", true));
        controller.setShouldUseBtle(prefs.getBoolean("btleEnabled", true));
        controller.setShouldUseWifi(prefs.getBoolean("wifiEnabled", true));

        btCheckBox.setOnCheckedChangeListener(null);
        btCheckBox.setChecked(controller.getShouldUseBt());
        btCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                controller.setShouldUseBt(isChecked);
            }
        });
        btleCheckBox.setOnCheckedChangeListener(null);
        btleCheckBox.setChecked(controller.getShouldUseBtle());
        btleCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                controller.setShouldUseBtle(isChecked);
            }
        });
        wifiCheckBox.setOnCheckedChangeListener(null);
        wifiCheckBox.setChecked(controller.getShouldUseWifi());
        wifiCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                controller.setShouldUseWifi(isChecked);
            }
        });

        collectSwitch.setOnCheckedChangeListener(null);
        collectSwitch.setChecked(controller.getCollectEnabled());
        collectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                controller.setCollectEnabled(isChecked);
            }
        });

        distanceView.setText(controller.getDistance() + "");
        List<Integer> ids = new ArrayList<>();
        for (SignalController.Device d : controller.getDevices()) {
            if (d.isDistanceKnown) ids.add(d.id);
        }
        if (ids.size() > 0) deviceIdView.setText(TextUtils.join(",", ids));
    }

    @Override
    protected void onResume() {
        super.onResume();
        controller.registerListener(signalListener);
        logLevel = prefs.getInt("logLevel", SignalController.LOG_INFORMATIVE);
        updateSendCounts();
        loadDevicesAndEvents();
        setupControls();
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.edit()
                .putInt("logLevel", logLevel)
                .putBoolean("btEnabled", controller.getShouldUseBt())
                .putBoolean("btleEnabled", controller.getShouldUseBtle())
                .putBoolean("wifiEnabled", controller.getShouldUseWifi()).apply();
        controller.unregisterListener(signalListener);
    }

    private final SignalController.SignalListener signalListener = new SignalController.SignalListener() {
        @Override
        public void onMessageLogged(int level, final String msg) {
            if (level >= logLevel) {
                eventLogView.post(new Runnable() {
                    @Override
                    public void run() {
                        eventLogView.append(msg + "\n");
                    }
                });
            }
        }

        @Override
        public void onDeviceFound(SignalController.Device device) {
            deviceLogView.append(device.toString() + "\n");
        }

        @Override
        public void onRssiLoadedFromDisk(String signal, List<RssiRecord> records) {
            updateCountView(signal, Settings.DATA_RSSI);
        }

        @Override
        public void onWindowsLoadedFromDisk(String signal, List<WindowRecord> records) {
            updateCountView(signal, Settings.DATA_WINDOW);
        }

        @Override
        public void onEstimatorsLoadedFromDisk(String signal, List<Estimator> estimators) {
            updateCountView(signal, Settings.DATA_ESTIMATOR);
        }

        @Override
        public void onRecordAdded(String signal, SignalController.Device device, RssiRecord r) {
            updateCountView(signal, Settings.DATA_RSSI);
        }

        @Override
        public void onTrainingStarting(String signal, int samples) {

        }

        @Override
        public void onGenerationFinished(String signal, int gen, double best, double mean, double stdDev) {

        }

        @Override
        public void onTrainingComplete(String signal, Estimator estimator, double error, int samples, int generations) {
            updateCountView(signal, Settings.DATA_ESTIMATOR);
        }

        @Override
        public void onWindowReady(String signal, WindowRecord record) {
            updateCountView(signal, Settings.DATA_WINDOW);
        }

        @Override
        public void onSentSuccess(String signal, String dataType, int count) {

        }

        @Override
        public void onSentFailure(String signal, String dataType, int count, int respCode, String resp, String result) {

        }

        @Override
        public void onSentFailure(String signal, String dataType, int count, String volleyError) {

        }
    };

    void updateCountView(String signal, String data) {
        switch (signal) {
            case Settings.SIGNAL_BT:
                switch (data) {
                    case Settings.DATA_RSSI:
                        btRssiCountView.setText(controller.getBt().getRssiCount() + "");
                        break;
                    case Settings.DATA_WINDOW:
                        btWindowCountView.setText(controller.getBt().getWindowCount() + "");
                        break;
                    case Settings.DATA_ESTIMATOR:
                        btEstimatorCountView.setText(controller.getBt().getEstimatorCount()+"");
                        break;
                }
                break;
            case Settings.SIGNAL_BTLE:
                switch (data) {
                    case Settings.DATA_RSSI:
                        btleRssiCountView.setText(controller.getBtle().getRssiCount() + "");
                        break;
                    case Settings.DATA_WINDOW:
                        btleWindowCountView.setText(controller.getBtle().getWindowCount() + "");
                        break;
                    case Settings.DATA_ESTIMATOR:
                        btleEstimatorCountView.setText(controller.getBtle().getEstimatorCount()+"");
                        break;
                }
                break;
            case Settings.SIGNAL_WIFI:
                switch (data) {
                    case Settings.DATA_RSSI:
                        wifiRssiCountView.setText(controller.getWifi().getRssiCount() + "");
                        break;
                    case Settings.DATA_WINDOW:
                        wifiWindowCountView.setText(controller.getWifi().getWindowCount() + "");
                        break;
                    case Settings.DATA_ESTIMATOR:
                        wifiEstimatorCountView.setText(controller.getWifi().getEstimatorCount()+"");
                        break;
                }
                break;
            case Settings.SIGNAL_WIFI5G:
                switch (data) {
                    case Settings.DATA_RSSI:
                        wifi5gRssiCountView.setText(controller.getWifi5g().getRssiCount() + "");
                        break;
                    case Settings.DATA_WINDOW:
                        wifi5gWindowCountView.setText(controller.getWifi5g().getWindowCount() + "");
                        break;
                    case Settings.DATA_ESTIMATOR:
                        wifi5gEstimatorCountView.setText(controller.getWifi5g().getEstimatorCount()+"");
                        break;
                }
                break;
        }
    }
}
