package com.jifalops.wsnlocalize.signal;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.data.DistanceEstimator;
import com.jifalops.wsnlocalize.data.WindowRecord;
import com.jifalops.wsnlocalize.file.EstimatorReaderWriter;
import com.jifalops.wsnlocalize.request.AbsRequest;
import com.jifalops.wsnlocalize.request.EstimatorRequest;
import com.jifalops.wsnlocalize.toolbox.neuralnet.Estimator;
import com.jifalops.wsnlocalize.toolbox.neuralnet.MlpWeightMetrics;
import com.jifalops.wsnlocalize.toolbox.neuralnet.TerminationConditions;
import com.jifalops.wsnlocalize.toolbox.neuralnet.Trainer;
import com.jifalops.wsnlocalize.toolbox.util.SimpleLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 */
public class SampleTrainer {
    public static final int LOG_IMPORTANT = 3;
    public static final int LOG_INFORMATIVE = 2;
    public static final int LOG_ALL = 1;

    SampleHelper sampleHelper;
    EstimatorHelper estimatorHelper, bestEstimatorHelper;

    boolean shouldUseBt, shouldUseBtle, shouldUseWifi, shouldUseWifi5g;

    Trainer bt, btle, wifi, wifi5g;
    final int popSize = 20;
    final MlpWeightMetrics metrics = new MlpWeightMetrics(WindowRecord.TRAINING_ARRAY_SIZE - 1, 1);
    final TerminationConditions termCond = new TerminationConditions();

    final SimpleLog log = new SimpleLog();

    private static SampleTrainer instance;
    public static SampleTrainer getInstance() {
        if (instance == null) instance = new SampleTrainer();
        return instance;
    }
    private SampleTrainer() {
        estimatorHelper = new EstimatorHelper(false, true, new EstimatorHelper.EstimatorsCallback() {
            @Override
            public void onEstimatorsLoaded() {
                addEvent(LOG_IMPORTANT, String.format(Locale.US,
                        "%d BT, %d BTLE, %d WIFI, and %d WIFI5G estimators loaded.",
                        estimatorHelper.getBt().size(), estimatorHelper.getBtle().size(),
                        estimatorHelper.getWifi().size(), estimatorHelper.getWifi5g().size()));
                for (TrainingListener l : listeners) l.onEstimatesLoaded();
            }
        });

        bestEstimatorHelper = new EstimatorHelper(true, true, new EstimatorHelper.EstimatorsCallback() {
            @Override
            public void onEstimatorsLoaded() {
                addEvent(LOG_IMPORTANT, String.format(Locale.US,
                        "%d BT, %d BTLE, %d WIFI, and %d WIFI5G best-estimators loaded.",
                        bestEstimatorHelper.getBt().size(), bestEstimatorHelper.getBtle().size(),
                        bestEstimatorHelper.getWifi().size(), bestEstimatorHelper.getWifi5g().size()));
//                for (TrainingListener l : listeners) l.onEstimatesLoaded();
            }
        });

        sampleHelper = new SampleHelper(new SampleHelper.SamplesCallback() {
            @Override
            public void onSamplesLoaded() {
                addEvent(LOG_IMPORTANT, String.format(Locale.US,
                        "%d BT, %d BTLE, %d WIFI, and %d WIFI5G samples loaded.",
                        sampleHelper.getBt().size(), sampleHelper.getBtle().size(),
                        sampleHelper.getWifi().size(), sampleHelper.getWifi5g().size()));
                bt = makeTrainer(sampleHelper.getBt());
                btle = makeTrainer(sampleHelper.getBtle());
                wifi = makeTrainer(sampleHelper.getWifi());
                wifi5g = makeTrainer(sampleHelper.getWifi5g());
                for (TrainingListener l : listeners) l.onSamplesLoaded();
            }

            Trainer makeTrainer(List<double[]> samples) {
                int len = samples.size();
                Trainer t = null;
                if (len > 0) {
                    t = new Trainer(samples.toArray(new double[len][]), metrics, popSize, termCond);
                }
                return t;
            }
        });
    }

    public SimpleLog getLog() {
        return log;
    }

    public void close() {
        instance = null;
    }

    public void clearPendingSendLists() {
        estimatorHelper.btRW.truncate(null);
        estimatorHelper.btleRW.truncate(null);
        estimatorHelper.wifiRW.truncate(null);
        estimatorHelper.wifi5gRW.truncate(null);
    }

    public void clearEstimators() {
        bestEstimatorHelper.btRW.truncate(null);
        bestEstimatorHelper.btleRW.truncate();
        bestEstimatorHelper.wifiRW.truncate();
        bestEstimatorHelper.wifi5gRW.truncate();
    }

    public void send() {
        send(App.SIGNAL_BT, estimatorHelper.getBt(), estimatorHelper.btRW);
        send(App.SIGNAL_BTLE, estimatorHelper.getBtle(), estimatorHelper.btleRW);
        send(App.SIGNAL_WIFI, estimatorHelper.getWifi(), estimatorHelper.wifiRW);
        send(App.SIGNAL_WIFI5G, estimatorHelper.getWifi5g(), estimatorHelper.wifi5gRW);
    }

    private void send(final String signal, final List<DistanceEstimator> estimators,
                      final EstimatorReaderWriter estimatorRW) {
        final int estimatorsSize = estimators.size();

        if (estimatorsSize > 0) {
            final List<DistanceEstimator> toSend = new ArrayList<>(estimators);
            estimators.clear();
            App.sendRequest(new EstimatorRequest(signal, toSend,
                    new Response.Listener<AbsRequest.MyResponse>() {
                        @Override
                        public void onResponse(AbsRequest.MyResponse response) {
                            if (response.responseCode == 200) {
                                estimatorRW.truncate();
                                for (TrainingListener l : listeners)
                                    l.onSentSuccess(signal, App.DATA_RSSI, estimatorsSize);
                            } else {
                                estimators.addAll(toSend);
                                for (TrainingListener l : listeners)
                                    l.onSentFailure(signal, App.DATA_RSSI, estimatorsSize,
                                            response.responseCode, response.responseMessage,
                                            response.queryResult);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    estimators.addAll(toSend);
                    for (TrainingListener l : listeners)
                        l.onSentFailure(signal, App.DATA_RSSI, estimatorsSize,
                                volleyError.toString());
                }
            }));
        }
    }

    void trainBt() {
        if (bt == null || !shouldUseBt) return;
        bt.train(new Trainer.TrainerCallbacks() {
            @Override
            public void onTrainingComplete(Estimator estimator) {
                DistanceEstimator de = new DistanceEstimator(estimator, DistanceEstimator.BT_MAX);
                estimatorHelper.addBt(de);
                if (estimator.error < DistanceEstimator.GOOD_ERROR) {
                    bestEstimatorHelper.addBt(de);
                }
                addEvent(estimator.error < DistanceEstimator.GOOD_ERROR
                                ? LOG_IMPORTANT : LOG_INFORMATIVE,
                        String.format(Locale.US, "BT: err %.4f mean %.4f std %.4f gen %d",
                                estimator.error, estimator.mean, estimator.stddev, estimator.generations));
                if (shouldUseBt) trainBt();
            }
        });
    }

    void trainBtle() {
        if (btle == null || !shouldUseBtle) return;
        btle.train(new Trainer.TrainerCallbacks() {
            @Override
            public void onTrainingComplete(Estimator estimator) {
                DistanceEstimator de = new DistanceEstimator(estimator, DistanceEstimator.BT_MAX);
                estimatorHelper.addBtle(de);
                if (estimator.error < DistanceEstimator.GOOD_ERROR) {
                    bestEstimatorHelper.addBtle(de);
                }
                addEvent(estimator.error < DistanceEstimator.GOOD_ERROR
                                ? LOG_IMPORTANT : LOG_INFORMATIVE,
                        String.format(Locale.US, "BTLE: err %.4f mean %.4f std %.4f gen %d",
                                estimator.error, estimator.mean, estimator.stddev, estimator.generations));
                if (shouldUseBtle) trainBtle();
            }
        });
    }

    void trainWifi() {
        if (wifi == null || !shouldUseWifi) return;
        wifi.train(new Trainer.TrainerCallbacks() {
            @Override
            public void onTrainingComplete(Estimator estimator) {
                DistanceEstimator de = new DistanceEstimator(estimator, DistanceEstimator.WIFI_MAX);
                estimatorHelper.addWifi(de);
                if (estimator.error < DistanceEstimator.GOOD_ERROR) {
                    bestEstimatorHelper.addWifi(de);
                }
                addEvent(estimator.error < DistanceEstimator.GOOD_ERROR
                                ? LOG_IMPORTANT : LOG_INFORMATIVE,
                        String.format(Locale.US, "WIFI: err %.4f mean %.4f std %.4f gen %d",
                                estimator.error, estimator.mean, estimator.stddev, estimator.generations));
                if (shouldUseWifi) trainWifi();
            }
        });
    }

    void trainWifi5g() {
        if (wifi5g == null || !shouldUseWifi5g) return;
        wifi5g.train(new Trainer.TrainerCallbacks() {
            @Override
            public void onTrainingComplete(Estimator estimator) {
                DistanceEstimator de = new DistanceEstimator(estimator, DistanceEstimator.WIFI_MAX);
                estimatorHelper.addWifi5g(de);
                if (estimator.error < DistanceEstimator.GOOD_ERROR) {
                    bestEstimatorHelper.addWifi5g(de);
                }
                addEvent(estimator.error < DistanceEstimator.GOOD_ERROR
                                ? LOG_IMPORTANT : LOG_INFORMATIVE,
                        String.format(Locale.US, "WIFI5G: err %.4f mean %.4f std %.4f gen %d",
                                estimator.error, estimator.mean, estimator.stddev, estimator.generations));
                if (shouldUseWifi5g) trainWifi5g();
            }
        });
    }

    public void setShouldUseBt(boolean use) {
        if (shouldUseBt == use) return;
        shouldUseBt = use;
        if (use) trainBt();
    }
    public void setShouldUseBtle(boolean use) {
        if (shouldUseBtle == use) return;
        shouldUseBtle = use;
        if (use) trainBtle();
    }
    public void setShouldUseWifi(boolean use) {
        if (shouldUseWifi == use) return;
        shouldUseWifi = use;
        if (use) trainWifi();
    }

    public void setShouldUseWifi5g(boolean use) {
        if (shouldUseWifi5g == use) return;
        shouldUseWifi5g = use;
        if (use) trainWifi5g();
    }

    public boolean getShouldUseBt() { return shouldUseBt; }
    public boolean getShouldUseBtle() { return shouldUseBtle; }
    public boolean getShouldUseWifi() { return shouldUseWifi; }
    public boolean getShouldUseWifi5g() { return shouldUseWifi5g; }

    public boolean getHasBt() { return bt != null; }
    public boolean getHasBtle() { return btle != null; }
    public boolean getHasWifi() { return wifi != null; }
    public boolean getHasWifi5g() { return wifi5g != null; }

    void addEvent(int level, String msg) {
        log.add(level, msg);
        for (TrainingListener l : listeners) l.onMessageLogged(level, msg);
    }

    public int getCount(String signal) {
        switch (signal) {
            case App.SIGNAL_BT:     return estimatorHelper.getBt().size();
            case App.SIGNAL_BTLE:   return estimatorHelper.getBtle().size();
            case App.SIGNAL_WIFI:   return estimatorHelper.getWifi().size();
            case App.SIGNAL_WIFI5G: return estimatorHelper.getWifi5g().size();
        }
        return 0;
    }


    public interface TrainingListener {
        void onMessageLogged(int level, String msg);
        void onSamplesLoaded();
        void onEstimatesLoaded();
        void onSentSuccess(String signal, String dataType, int count);
        void onSentFailure(String signal, String dataType, int count, int respCode, String resp, String result);
        void onSentFailure(String signal, String dataType, int count, String volleyError);
    }
    private final List<TrainingListener> listeners = new ArrayList<>(1);
    public boolean registerListener(TrainingListener l) {
        return !listeners.contains(l) && listeners.add(l);
    }
    public boolean unregisterListener(TrainingListener l) {
        return listeners.remove(l);
    }
}
