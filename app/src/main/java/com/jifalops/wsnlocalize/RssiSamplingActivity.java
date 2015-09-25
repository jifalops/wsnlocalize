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

import com.jifalops.wsnlocalize.data.Rssi;
import com.jifalops.wsnlocalize.signal.RssiHelper;
import com.jifalops.wsnlocalize.signal.RssiSampler;
import com.jifalops.wsnlocalize.signal.SampleHelper;
import com.jifalops.wsnlocalize.signal.WindowHelper;
import com.jifalops.wsnlocalize.toolbox.ServiceThreadApplication;
import com.jifalops.wsnlocalize.toolbox.util.SimpleLog;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RssiSamplingActivity extends Activity {
    static final String TAG = RssiSamplingActivity.class.getSimpleName();
    static final int REQUEST_BT_ENABLE = 1;
    static final int REQUEST_BT_DISCOVERABLE = 2;
    static final String SAMPLER = RssiSamplingActivity.class.getName() + ".sampler";

    TextView eventLogView, deviceLogView,
            btRssiCountView, btWindowCountView,
            btleRssiCountView, btleWindowCountView,
            wifiRssiCountView, wifiWindowCountView,
            wifi5gRssiCountView, wifi5gWindowCountView,
            deviceIdView;
    EditText distanceView;
    Switch collectSwitch;
    CheckBox btCheckBox, btleCheckBox, wifiCheckBox, wifi5gCheckBox;

    SharedPreferences prefs;
    int logLevel = RssiSampler.LOG_INFORMATIVE;

    ServiceThreadApplication.LocalService service;
    RssiSampler rssiSampler;



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
                AlertDialog.Builder b = new AlertDialog.Builder(RssiSamplingActivity.this);
                final EditText input = new EditText(RssiSamplingActivity.this);
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
                        List<Integer> deviceIds = new ArrayList<>();
                        rssiSampler.resetKnownDistances();
                        try {
                            String[] ids = input.getText().toString().split(",");
                            int id;
                            for (String s : ids) {
                                id = Integer.valueOf(s);
                                RssiSampler.Device d = rssiSampler.getDevice(id - 1);
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
                    rssiSampler.setDistance(Double.valueOf(s.toString()));
                } catch (NumberFormatException e) {
                    distanceView.setText(rssiSampler.getDistance() + "");
                }
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rssiSampler.send();
            }
        });

        rssiSampler = RssiSampler.getInstance(this);

        prefs = getSharedPreferences(TAG, MODE_PRIVATE);
        logLevel = prefs.getInt("logLevel", logLevel);


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
            rssiSampler.setCollectEnabled(false);
            rssiSampler.close();
            rssiSampler = null;
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
                logLevel == RssiSampler.LOG_IMPORTANT);
        menu.findItem(R.id.logInformative).setChecked(
                logLevel == RssiSampler.LOG_INFORMATIVE);
        menu.findItem(R.id.logAll).setChecked(
                logLevel == RssiSampler.LOG_ALL);
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
                            rssiSampler.clearPendingSendLists();
                            updateSendCounts();
                        }
                    }).show();
                return true;
            case R.id.action_clearSamples:
                new AlertDialog.Builder(this)
                    .setMessage("Clear all training samples (not recommended)?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            rssiSampler.clearSamples();
                        }
                    }).show();
                return true;
            case R.id.action_trimSamples:
                new AlertDialog.Builder(this)
                        .setMessage("Trim samples that took longer than allowed?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int count = rssiSampler.trimLongSamples();
                                Toast.makeText(RssiSamplingActivity.this,
                                        "Trimmed " + count + " samples.", Toast.LENGTH_LONG).show();
                            }
                        }).show();
                return true;
            case R.id.logImportant:
                logLevel = RssiSampler.LOG_IMPORTANT;
                return true;
            case R.id.logInformative:
                logLevel = RssiSampler.LOG_INFORMATIVE;
                return true;
            case R.id.logAll:
                logLevel = RssiSampler.LOG_ALL;
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void setPersistent(boolean persist) {
        if (service == null) return;
        Object obj = persist ? rssiSampler : null;
        service.setPersistent(persist, getClass());
        service.setCachedObject(SAMPLER, obj);
    }

    void updateSendCounts() {
        updateCountView(App.SIGNAL_BT, App.DATA_RSSI);
        updateCountView(App.SIGNAL_BT, App.DATA_WINDOW);
        updateCountView(App.SIGNAL_BTLE, App.DATA_RSSI);
        updateCountView(App.SIGNAL_BTLE, App.DATA_WINDOW);
        updateCountView(App.SIGNAL_WIFI, App.DATA_RSSI);
        updateCountView(App.SIGNAL_WIFI, App.DATA_WINDOW);
        updateCountView(App.SIGNAL_WIFI5G, App.DATA_RSSI);
        updateCountView(App.SIGNAL_WIFI5G, App.DATA_WINDOW);
    }

    void loadDevicesAndEvents() {
        deviceLogView.setText("Devices:\n");
        eventLogView.setText("Events:\n");
        for (RssiSampler.Device d : rssiSampler.getDevices()) {
            deviceLogView.append(d.toString() + "\n");
        }
        List<SimpleLog.LogItem> items =  rssiSampler.getLog().getByImportance(logLevel, true);
        for (SimpleLog.LogItem item : items) {
            eventLogView.append(item.msg + "\n");
        }
    }

    void setupControls() {
        rssiSampler.setShouldUseBt(prefs.getBoolean("btEnabled", true));
        rssiSampler.setShouldUseBtle(prefs.getBoolean("btleEnabled", true));
        rssiSampler.setShouldUseWifi(prefs.getBoolean("wifiEnabled", true));
        rssiSampler.setShouldUseWifi5g(prefs.getBoolean("wifi5gEnabled", true));

        btCheckBox.setOnCheckedChangeListener(null);
        btCheckBox.setChecked(rssiSampler.getShouldUseBt());
        btCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rssiSampler.setShouldUseBt(isChecked);
            }
        });
        btleCheckBox.setOnCheckedChangeListener(null);
        btleCheckBox.setChecked(rssiSampler.getShouldUseBtle());
        btleCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rssiSampler.setShouldUseBtle(isChecked);
            }
        });
        wifiCheckBox.setOnCheckedChangeListener(null);
        wifiCheckBox.setChecked(rssiSampler.getShouldUseWifi());
        wifiCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rssiSampler.setShouldUseWifi(isChecked);
            }
        });
        wifi5gCheckBox.setOnCheckedChangeListener(null);
        wifi5gCheckBox.setChecked(rssiSampler.getShouldUseWifi5g());
        wifi5gCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rssiSampler.setShouldUseWifi5g(isChecked);
            }
        });

        collectSwitch.setOnCheckedChangeListener(null);
        collectSwitch.setChecked(rssiSampler.getCollectEnabled());
        collectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rssiSampler.setCollectEnabled(isChecked);
            }
        });

        distanceView.setText(rssiSampler.getDistance() + "");
        List<Integer> ids = new ArrayList<>();
        for (RssiSampler.Device d : rssiSampler.getDevices()) {
            if (d.isDistanceKnown) ids.add(d.id);
        }
        if (ids.size() > 0) deviceIdView.setText(TextUtils.join(",", ids));
    }

    @Override
    protected void onResume() {
        super.onResume();
        rssiSampler.registerListener(samplerListener);
        updateSendCounts();
        loadDevicesAndEvents();
        setupControls();
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.edit()
                .putInt("logLevel", logLevel)
                .putBoolean("btEnabled", rssiSampler.getShouldUseBt())
                .putBoolean("btleEnabled", rssiSampler.getShouldUseBtle())
                .putBoolean("wifiEnabled", rssiSampler.getShouldUseWifi())
                .putBoolean("wifi5gEnabled", rssiSampler.getShouldUseWifi5g()).apply();
        rssiSampler.unregisterListener(samplerListener);
    }

    private final RssiSampler.SamplerListener samplerListener = new RssiSampler.SamplerListener() {
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
        public void onDeviceFound(RssiSampler.Device device) {
            deviceLogView.append(device.toString() + "\n");
        }

        @Override
        public void onDataLoadedFromDisk(RssiHelper rssiHelper, WindowHelper windowHelper,
                                         SampleHelper sampleHelper) {
            updateSendCounts();
        }

        @Override
        public void onRecordAdded(String signal, RssiSampler.Device device, Rssi r) {
            updateCountView(signal, App.DATA_RSSI);
        }

        @Override
        public void onSampleAdded(String signal, double[] sample) {
            updateCountView(signal, App.DATA_RSSI);
            updateCountView(signal, App.DATA_WINDOW);
        }

        @Override
        public void onSentSuccess(String signal, String dataType, int count) {
            updateCountView(signal, dataType);
        }

        @Override
        public void onSentFailure(String signal, String dataType, int count, int respCode, String resp, String result) {
            updateCountView(signal, dataType);
        }

        @Override
        public void onSentFailure(String signal, String dataType, int count, String volleyError) {
            updateCountView(signal, dataType);
        }
    };

    void updateCountView(String signal, String data) {
        int count = rssiSampler.getCount(signal, data);
        TextView tv = null;
        switch (signal) {
            case App.SIGNAL_BT:
                switch (data) {
                    case App.DATA_RSSI:      tv = btRssiCountView; break;
                    case App.DATA_WINDOW:    tv = btWindowCountView; break;
                }
                break;
            case App.SIGNAL_BTLE:
                switch (data) {
                    case App.DATA_RSSI:      tv = btleRssiCountView; break;
                    case App.DATA_WINDOW:    tv = btleWindowCountView; break;
                }
                break;
            case App.SIGNAL_WIFI:
                switch (data) {
                    case App.DATA_RSSI:      tv = wifiRssiCountView; break;
                    case App.DATA_WINDOW:    tv = wifiWindowCountView; break;
                }
                break;
            case App.SIGNAL_WIFI5G:
                switch (data) {
                    case App.DATA_RSSI:      tv = wifi5gRssiCountView; break;
                    case App.DATA_WINDOW:    tv = wifi5gWindowCountView; break;
                }
                break;
        }
        if (tv != null) tv.setText(count+"");
    }
}
