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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jifalops.wsnlocalize.data.DataFileInfo;
import com.jifalops.wsnlocalize.data.RssiSampleList;
import com.jifalops.wsnlocalize.data.helper.EstimatorsHelper;
import com.jifalops.wsnlocalize.data.helper.InfoFileHelper;
import com.jifalops.wsnlocalize.data.helper.SamplesHelper;
import com.jifalops.wsnlocalize.toolbox.ServiceThreadApplication;
import com.jifalops.wsnlocalize.toolbox.neuralnet.Depso;
import com.jifalops.wsnlocalize.toolbox.neuralnet.DifferentialEvolution;
import com.jifalops.wsnlocalize.toolbox.neuralnet.NeuralNetwork;
import com.jifalops.wsnlocalize.toolbox.neuralnet.ParticleSwarm;
import com.jifalops.wsnlocalize.toolbox.neuralnet.TerminationConditions;
import com.jifalops.wsnlocalize.toolbox.neuralnet.TrainingResults;
import com.jifalops.wsnlocalize.toolbox.util.SimpleLog;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class EstimatorTrainingActivity extends Activity {
    static final String TAG = EstimatorTrainingActivity.class.getSimpleName();
    static final String CONTROLLER = EstimatorTrainingActivity.class.getName() + ".controller";
    static final String EXTRA_DATAFILEINFO_INDEXES = "indexes";

    static final int LOG_ALL = 1;
    static final int LOG_INFORMATIVE = 2;
    static final int LOG_IMPORTANT = 3;

    TextView eventLog;
    EditText errorLimitView;
    Button trainingButton;
    int logLevel = LOG_INFORMATIVE;
    ServiceThreadApplication.LocalService service;
    SharedPreferences prefs;
    List<TrainingUnit> trainers;
    double errorLimit;
    boolean training;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estimatortraining);
        eventLog = (TextView) findViewById(R.id.eventLog);
        errorLimitView = (EditText) findViewById(R.id.errorLimit);
        trainingButton = (Button) findViewById(R.id.trainButton);

        autoScroll((ScrollView) findViewById(R.id.eventScrollView), eventLog);

        prefs = getSharedPreferences(TAG, MODE_PRIVATE);
        logLevel = prefs.getInt("logLevel", logLevel);

        int[] dataInfos = getIntent().getExtras().getIntArray(EXTRA_DATAFILEINFO_INDEXES);
        List<DataFileInfo> infos = InfoFileHelper.getInstance().getAll();
        trainers = new ArrayList<>(dataInfos.length);

        for (int i : dataInfos) {
            trainers.add(new TrainingUnit(InfoFileHelper.getInstance().getSignal(i), infos.get(i)));
        }

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
            enableTraining(false);
        }
        App.getInstance().unbindLocalService(null);
    }

    void enableTraining(boolean enable) {
        if (training == enable) return;
        training = enable;
        if (enable) {
            trainingButton.setText("Stop Training");
            for (TrainingUnit tu : trainers) {
                tu.train();
            }
        } else {
            trainingButton.setText("Start Training");
        }
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
                logLevel == RssiCollector.LOG_IMPORTANT);
        menu.findItem(R.id.logInformative).setChecked(
                logLevel == RssiCollector.LOG_INFORMATIVE);
        menu.findItem(R.id.logAll).setChecked(
                logLevel == RssiCollector.LOG_ALL);
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
            case R.id.logImportant:
                logLevel = RssiCollector.LOG_IMPORTANT;
                return true;
            case R.id.logInformative:
                logLevel = RssiCollector.LOG_INFORMATIVE;
                return true;
            case R.id.logAll:
                logLevel = RssiCollector.LOG_ALL;
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
        updateCountView(App.SIGNAL_BT);
        updateCountView(App.SIGNAL_BTLE);
        updateCountView(App.SIGNAL_WIFI);
        updateCountView(App.SIGNAL_WIFI5G);
    }

    void updateCountView(String signal) {
        int count = sampleTrainer.getCount(signal);
        TextView tv = null;
        switch (signal) {
            case App.SIGNAL_BT:     tv = btEstimatorCountView; break;
            case App.SIGNAL_BTLE:   tv = btleEstimatorCountView; break;
            case App.SIGNAL_WIFI:   tv = wifiEstimatorCountView; break;
            case App.SIGNAL_WIFI5G: tv = wifi5gEstimatorCountView; break;
        }
        if (tv != null) tv.setText(count + "");
    }

    void loadEvents() {
        eventLogView.setText("Events:\n");
        List<SimpleLog.LogItem> items =  sampleTrainer.getLog().getByImportance(logLevel, true);
        for (SimpleLog.LogItem item : items) {
            eventLogView.append(item.msg + "\n");
        }
    }

    void setupControls() {
        btCheckBox.setEnabled(sampleTrainer.getHasBt());
        btleCheckBox.setEnabled(sampleTrainer.getHasBtle());
        wifiCheckBox.setEnabled(sampleTrainer.getHasWifi());
        wifi5gCheckBox.setEnabled(sampleTrainer.getHasWifi5g());

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

        updateSendCounts();
        loadEvents();
        setupControls();
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.edit()
                .putInt("logLevel", logLevel).apply();;
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
            setupControls();
        }

        @Override
        public void onEstimatesLoaded() {
            updateSendCounts();
        }

        @Override
        public void onSentSuccess(String signal, String dataType, int count) {
            updateCountView(signal);
        }

        @Override
        public void onSentFailure(String signal, String dataType, int count, int respCode, String resp, String result) {
            updateCountView(signal);
        }

        @Override
        public void onSentFailure(String signal, String dataType, int count, String volleyError) {
            updateCountView(signal);
        }
    };

    class TrainingUnit {
        final int popSize = 20;
        final String signal;
        final DataFileInfo info;
        final ParticleSwarm psoTimed, psoUntimed;
        final DifferentialEvolution deTimed, deUntimed;
        final Depso depsoTimed, depsoUntimed;

        TrainingUnit(final String signal, final DataFileInfo info) {
            this.signal = signal;
            this.info = info;
            RssiSampleList samplesTimed = SamplesHelper.getInstance().getSamples(signal, info);
            RssiSampleList.Untimed samplesUntimed = samplesTimed.toUntimed();
            TerminationConditions conds = new TerminationConditions();

            psoTimed = new ParticleSwarm(samplesTimed, popSize, conds, new NeuralNetwork.TrainingCallbacks() {
                @Override
                public void onGenerationFinished(int gen, double best, double mean, double stdDev) {

                }

                @Override
                public void onTrainingComplete(TrainingResults results) {
                    if (results.error < errorLimit) {
                        EstimatorsHelper.getInstance().add(results, info, signal, App.NN_PSO, true, null);
                    }
                    if (training) {
                        psoTimed.train();
                    }
                }
            });

            psoUntimed = new ParticleSwarm(samplesUntimed, popSize, conds, new NeuralNetwork.TrainingCallbacks() {
                @Override
                public void onGenerationFinished(int gen, double best, double mean, double stdDev) {

                }

                @Override
                public void onTrainingComplete(TrainingResults results) {
                    if (results.error < errorLimit) {
                        EstimatorsHelper.getInstance().add(results, info, signal, App.NN_PSO, false, null);
                    }
                    if (training) {
                        psoUntimed.train();
                    }
                }
            });

            deTimed = new DifferentialEvolution(samplesTimed, popSize, conds, new NeuralNetwork.TrainingCallbacks() {
                @Override
                public void onGenerationFinished(int gen, double best, double mean, double stdDev) {

                }

                @Override
                public void onTrainingComplete(TrainingResults results) {
                    if (results.error < errorLimit) {
                        EstimatorsHelper.getInstance().add(results, info, signal, App.NN_DE, true, null);
                    }
                    if (training) {
                        deTimed.train();
                    }
                }
            });

            deUntimed = new DifferentialEvolution(samplesUntimed, popSize, conds, new NeuralNetwork.TrainingCallbacks() {
                @Override
                public void onGenerationFinished(int gen, double best, double mean, double stdDev) {

                }

                @Override
                public void onTrainingComplete(TrainingResults results) {
                    if (results.error < errorLimit) {
                        EstimatorsHelper.getInstance().add(results, info, signal, App.NN_DE, false, null);
                    }
                    if (training) {
                        deUntimed.train();
                    }
                }
            });

            depsoTimed = new Depso(samplesTimed, popSize, conds, new NeuralNetwork.TrainingCallbacks() {
                @Override
                public void onGenerationFinished(int gen, double best, double mean, double stdDev) {

                }

                @Override
                public void onTrainingComplete(TrainingResults results) {
                    if (results.error < errorLimit) {
                        EstimatorsHelper.getInstance().add(results, info, signal, App.NN_DEPSO, true, null);
                    }
                    if (training) {
                        depsoTimed.train();
                    }
                }
            });

            depsoUntimed = new Depso(samplesUntimed, popSize, conds, new NeuralNetwork.TrainingCallbacks() {
                @Override
                public void onGenerationFinished(int gen, double best, double mean, double stdDev) {

                }

                @Override
                public void onTrainingComplete(TrainingResults results) {
                    if (results.error < errorLimit) {
                        EstimatorsHelper.getInstance().add(results, info, signal, App.NN_DEPSO, false, null);
                    }
                    if (training) {
                        depsoUntimed.train();
                    }
                }
            });
        }

        void train() {
            psoTimed.train();
            psoUntimed.train();
            deTimed.train();
            deUntimed.train();
            depsoTimed.train();
            depsoUntimed.train();
        }
    }
}
