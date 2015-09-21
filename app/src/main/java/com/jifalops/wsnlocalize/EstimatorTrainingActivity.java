package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jifalops.toolbox.SimpleLog;
import com.jifalops.wsnlocalize.data.Estimator;
import com.jifalops.wsnlocalize.data.RssiRecord;
import com.jifalops.wsnlocalize.data.WindowRecord;
import com.jifalops.wsnlocalize.signal.SignalController;
import com.jifalops.wsnlocalize.util.ServiceThreadApplication;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class EstimatorTrainingActivity extends Activity {
//    static final String CONTROLLER = EstimatorTrainingActivity.class.getName() + ".controller";

    TextView eventLogView,
            btEstimatorCountView, btleEstimatorCountView,
            wifiEstimatorCountView, wifi5gEstimatorCountView;

    CheckBox btCheckBox, btleCheckBox, wifiCheckBox, wifi5gCheckBox;

    int logLevel;

    ServiceThreadApplication.LocalService service;
    SignalController controller;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_samplecollection);
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
        wifi5gCheckBox = (CheckBox) findViewById(R.id.wifi5gCheckBox);

        deviceIdView = (TextView) findViewById(R.id.deviceId);
        distanceView = (EditText) findViewById(R.id.distanceMeters);
        Button sendButton = (Button) findViewById(R.id.sendButton);

        autoScroll((ScrollView) findViewById(R.id.eventScrollView), eventLogView);
        autoScroll((ScrollView) findViewById(R.id.deviceScrollView), deviceLogView);
        deviceIdView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder b = new AlertDialog.Builder(EstimatorTrainingActivity.this);
                final EditText input = new EditText(EstimatorTrainingActivity.this);
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
            case R.id.action_clearSend:
                new AlertDialog.Builder(this)
                    .setMessage("Clear send queue?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            controller.clearPendingSendLists();
                            updateSendCounts();
                        }
                    }).show();
                return true;
            case R.id.action_clearEstimators:
                new AlertDialog.Builder(this)
                    .setMessage("Clear Estimators?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            controller.clearEstimators();
                        }
                    }).show();
                return true;
            case R.id.action_clearTraining:
                new AlertDialog.Builder(this)
                    .setMessage("Clear all training samples (not recommended)?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
        controller.setShouldUseWifi5g(prefs.getBoolean("wifi5gEnabled", true));

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
        wifi5gCheckBox.setOnCheckedChangeListener(null);
        wifi5gCheckBox.setChecked(controller.getShouldUseWifi5g());
        wifi5gCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                controller.setShouldUseWifi5g(isChecked);
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
                .putBoolean("wifiEnabled", controller.getShouldUseWifi())
                .putBoolean("wifi5gEnabled", controller.getShouldUseWifi5g()).apply();
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
            updateCountView(signal, App.DATA_RSSI);
        }

        @Override
        public void onWindowsLoadedFromDisk(String signal, List<WindowRecord> records) {
            updateCountView(signal, App.DATA_WINDOW);
        }

        @Override
        public void onEstimatorsLoadedFromDisk(String signal, List<Estimator> estimators) {
            updateCountView(signal, App.DATA_ESTIMATOR);
        }

        @Override
        public void onRecordAdded(String signal, SignalController.Device device, RssiRecord r) {
            updateCountView(signal, App.DATA_RSSI);
        }

        @Override
        public void onTrainingStarting(String signal, int samples) {

        }

        @Override
        public void onGenerationFinished(String signal, int gen, double best, double mean, double stdDev) {

        }

        @Override
        public void onTrainingComplete(String signal, Estimator estimator) {
            updateCountView(signal, App.DATA_ESTIMATOR);
        }

        @Override
        public void onWindowReady(String signal, WindowRecord record) {
            updateCountView(signal, App.DATA_WINDOW);
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
            case SIGNAL_BT:
                switch (data) {
                    case DATA_RSSI:
                        btRssiCountView.setText(controller.getBt().getRssiCount() + "");
                        break;
                    case DATA_WINDOW:
                        btWindowCountView.setText(controller.getBt().getWindowCount() + "");
                        break;
                    case DATA_ESTIMATOR:
                        btEstimatorCountView.setText(controller.getBt().getEstimatorCount()+"");
                        break;
                }
                break;
            case SIGNAL_BTLE:
                switch (data) {
                    case DATA_RSSI:
                        btleRssiCountView.setText(controller.getBtle().getRssiCount() + "");
                        break;
                    case DATA_WINDOW:
                        btleWindowCountView.setText(controller.getBtle().getWindowCount() + "");
                        break;
                    case DATA_ESTIMATOR:
                        btleEstimatorCountView.setText(controller.getBtle().getEstimatorCount()+"");
                        break;
                }
                break;
            case SIGNAL_WIFI:
                switch (data) {
                    case DATA_RSSI:
                        wifiRssiCountView.setText(controller.getWifi().getRssiCount() + "");
                        break;
                    case DATA_WINDOW:
                        wifiWindowCountView.setText(controller.getWifi().getWindowCount() + "");
                        break;
                    case DATA_ESTIMATOR:
                        wifiEstimatorCountView.setText(controller.getWifi().getEstimatorCount()+"");
                        break;
                }
                break;
            case SIGNAL_WIFI5G:
                switch (data) {
                    case DATA_RSSI:
                        wifi5gRssiCountView.setText(controller.getWifi5g().getRssiCount() + "");
                        break;
                    case DATA_WINDOW:
                        wifi5gWindowCountView.setText(controller.getWifi5g().getWindowCount() + "");
                        break;
                    case DATA_ESTIMATOR:
                        wifi5gEstimatorCountView.setText(controller.getWifi5g().getEstimatorCount()+"");
                        break;
                }
                break;
        }
    }
}
