package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jifalops.toolbox.app.ServiceThreadApplication;
import com.jifalops.toolbox.util.SimpleLog;
import com.jifalops.wsnlocalize.signal.RssiSampler;
import com.jifalops.wsnlocalize.signal.SampleTrainer;

import java.util.List;

/**
 *
 */
public class EstimatorTrainingActivity extends Activity {
    static final String CONTROLLER = EstimatorTrainingActivity.class.getName() + ".controller";

    TextView eventLogView,
            btEstimatorCountView, btleEstimatorCountView,
            wifiEstimatorCountView, wifi5gEstimatorCountView;

    CheckBox btCheckBox, btleCheckBox, wifiCheckBox, wifi5gCheckBox;

    int logLevel;

    ServiceThreadApplication.LocalService service;
    SampleTrainer sampleTrainer;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estimatortraining);
        eventLogView = (TextView) findViewById(R.id.eventLog);
        btEstimatorCountView = (TextView) findViewById(R.id.btEstimatorCount);
        btleEstimatorCountView = (TextView) findViewById(R.id.btleEstimatorCount);
        wifiEstimatorCountView = (TextView) findViewById(R.id.wifiEstimatorCount);
        wifi5gEstimatorCountView = (TextView) findViewById(R.id.wifi5gEstimatorCount);
        btCheckBox = (CheckBox) findViewById(R.id.btCheckBox);
        btleCheckBox = (CheckBox) findViewById(R.id.btleCheckBox);
        wifiCheckBox = (CheckBox) findViewById(R.id.wifiCheckBox);
        wifi5gCheckBox = (CheckBox) findViewById(R.id.wifi5gCheckBox);

        autoScroll((ScrollView) findViewById(R.id.eventScrollView), eventLogView);

        prefs = getSharedPreferences("sampleTraining", MODE_PRIVATE);

        sampleTrainer = SampleTrainer.getInstance();

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
            sampleTrainer.close();
            sampleTrainer = null;
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
                            sampleTrainer.clearPendingSendLists();
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
                            sampleTrainer.clearEstimators();
                        }
                    }).show();
                return true;
            case R.id.action_clearTraining:
                new AlertDialog.Builder(this)
                    .setMessage("Clear all training samples (not recommended)?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sampleTrainer.clearTrainingSamples();
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
        Object obj = persist ? sampleTrainer : null;
        service.setPersistent(persist, getClass());
        service.setCachedObject(getLocalClassName() + CONTROLLER, obj);
    }

    void updateSendCounts() {
//        btRssiCountView.setText(controller.getBt().getRssiCount() + "");
//        btWindowCountView.setText(controller.getBt().getWindowCount() + "");
//        btEstimatorCountView.setText(controller.getBt().getEstimatorCount() + "");
//        btleRssiCountView.setText(controller.getBtle().getRssiCount() + "");
//        btleWindowCountView.setText(controller.getBtle().getWindowCount() + "");
//        btleEstimatorCountView.setText(controller.getBtle().getEstimatorCount() + "");
//        wifiRssiCountView.setText(controller.getWifi().getRssiCount() + "");
//        wifiWindowCountView.setText(controller.getWifi().getWindowCount() + "");
//        wifiEstimatorCountView.setText(controller.getWifi().getEstimatorCount() + "");
//        wifi5gRssiCountView.setText(controller.getWifi5g().getRssiCount() + "");
//        wifi5gWindowCountView.setText(controller.getWifi5g().getWindowCount() + "");
//        wifi5gEstimatorCountView.setText(controller.getWifi5g().getEstimatorCount() + "");
    }

    void loadDevicesAndEvents() {
        eventLogView.setText("Events:\n");
        List<SimpleLog.LogItem> items =  sampleTrainer.getLog().getByImportance(logLevel, true);
        for (SimpleLog.LogItem item : items) {
            eventLogView.append(item.msg + "\n");
        }
    }

    void setupControls() {
        sampleTrainer.setShouldUseBt(prefs.getBoolean("btEnabled", true));
        sampleTrainer.setShouldUseBtle(prefs.getBoolean("btleEnabled", true));
        sampleTrainer.setShouldUseWifi(prefs.getBoolean("wifiEnabled", true));
        sampleTrainer.setShouldUseWifi5g(prefs.getBoolean("wifi5gEnabled", true));

        btCheckBox.setOnCheckedChangeListener(null);
        btCheckBox.setChecked(sampleTrainer.getShouldUseBt());
        btCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sampleTrainer.setShouldUseBt(isChecked);
            }
        });
        btleCheckBox.setOnCheckedChangeListener(null);
        btleCheckBox.setChecked(sampleTrainer.getShouldUseBtle());
        btleCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sampleTrainer.setShouldUseBtle(isChecked);
            }
        });
        wifiCheckBox.setOnCheckedChangeListener(null);
        wifiCheckBox.setChecked(sampleTrainer.getShouldUseWifi());
        wifiCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sampleTrainer.setShouldUseWifi(isChecked);
            }
        });
        wifi5gCheckBox.setOnCheckedChangeListener(null);
        wifi5gCheckBox.setChecked(sampleTrainer.getShouldUseWifi5g());
        wifi5gCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sampleTrainer.setShouldUseWifi5g(isChecked);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        sampleTrainer.registerListener(trainingListener);
        logLevel = prefs.getInt("logLevel", RssiSampler.LOG_INFORMATIVE);
        updateSendCounts();
        loadDevicesAndEvents();
        setupControls();
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.edit()
                .putInt("logLevel", logLevel)
                .putBoolean("btEnabled", sampleTrainer.getShouldUseBt())
                .putBoolean("btleEnabled", sampleTrainer.getShouldUseBtle())
                .putBoolean("wifiEnabled", sampleTrainer.getShouldUseWifi())
                .putBoolean("wifi5gEnabled", sampleTrainer.getShouldUseWifi5g()).apply();
        sampleTrainer.unregisterListener(trainingListener);
    }

    private final SampleTrainer.TrainingListener trainingListener = new SampleTrainer.TrainingListener() {
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
        public void onSamplesLoaded() {

        }

        @Override
        public void onEstimatesLoaded() {

        }
    };
}
