package com.jifalops.wsnlocalize;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 */
public class EstimatorTrainingActivity extends Activity {
    static final String TAG = EstimatorTrainingActivity.class.getSimpleName();
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
    float errorLimit = 0.01f;
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
        errorLimit = prefs.getFloat("errorLimit", errorLimit);

        int[] dataInfos = getIntent().getExtras().getIntArray(EXTRA_DATAFILEINFO_INDEXES);
        addEvent(LOG_IMPORTANT, dataInfos.length + " DataInfoFiles loaded.");
        List<DataFileInfo> infos = InfoFileHelper.getInstance().getAll();
        trainers = new ArrayList<>(dataInfos.length);

        for (int i : dataInfos) {
            trainers.add(new TrainingUnit(
                    InfoFileHelper.getInstance().getSignal(i), infos.get(i)));
        }
        
        trainingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableTraining(!training);
            }
        });

        errorLimitView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    errorLimit = Float.valueOf(s.toString());
                } catch (Exception e) {
                    errorLimitView.setText(errorLimit+"");
                }
            }
        });

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

    void addEvent(int level, String msg) {
        if (logLevel <= level) {
            eventLog.append(msg + "\n");
        }
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
            addEvent(LOG_INFORMATIVE, "Starting training on " + trainers.size() * 6 + " neural networks.");
            for (TrainingUnit tu : trainers) {
                tu.train();
            }
            setPersistent(true);
        } else {
            trainingButton.setText("Start Training");
            setPersistent(false);
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
        getMenuInflater().inflate(R.menu.menu_estimatortraining, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        SubMenu sub = menu.getItem(0);
        menu.findItem(R.id.logImportant).setChecked(
                logLevel == LOG_IMPORTANT);
        menu.findItem(R.id.logInformative).setChecked(
                logLevel == LOG_INFORMATIVE);
        menu.findItem(R.id.logAll).setChecked(
                logLevel == LOG_ALL);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logImportant:
                logLevel = LOG_IMPORTANT;
                return true;
            case R.id.logInformative:
                logLevel = LOG_INFORMATIVE;
                return true;
            case R.id.logAll:
                logLevel = LOG_ALL;
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void setPersistent(boolean persist) {
        if (service == null) return;
        service.setPersistent(persist, getClass());
    }

    void setupControls() {
        trainingButton.setText(training ? "Stop Training" : "Start Training");
        errorLimitView.setText(errorLimit+"");
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupControls();

    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.edit()
                .putInt("logLevel", logLevel)
                .putFloat("errorLimit", errorLimit).apply();
    }

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
                    showResults(psoTimed, results);
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
                    showResults(psoUntimed, results);
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
                    showResults(deTimed, results);
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
                    showResults(deUntimed, results);
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
                    showResults(depsoTimed, results);
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
                    showResults(depsoUntimed, results);
                    if (results.error < errorLimit) {
                        EstimatorsHelper.getInstance().add(results, info, signal, App.NN_DEPSO, false, null);
                    }
                    if (training) {
                        depsoUntimed.train();
                    }
                }
            });
        }

        void showResults(NeuralNetwork nn, TrainingResults r) {
            String status = r.error < errorLimit ? " accepted. " : " rejected. ";
            int level = r.error < errorLimit ? LOG_IMPORTANT : LOG_ALL;
            String nnet;
            if (nn == psoTimed) nnet = "PSO timed";
            else if (nn == psoUntimed) nnet = "PSO untimed";
            else if (nn == deTimed) nnet = "DE timed";
            else if (nn == deUntimed) nnet = "DE untimed";
            else if (nn == depsoTimed) nnet = "DEPSO timed";
            else nnet = "DEPSO untimed";
            addEvent(level, signal + " " + nnet + status + String.format(Locale.US,
                    "Err: %.3f Gen: %d Avg: %.3f Std: %.4f Time: %ds",
                    r.error, r.numGenerations, r.mean, r.stddev, r.elapsedTime/1000));
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
