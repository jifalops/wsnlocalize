package com.jifalops.wsnlocalize.signal;

import com.jifalops.wsnlocalize.data.DistanceEstimator;
import com.jifalops.wsnlocalize.data.WindowRecord;
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
    EstimatorHelper estimatorHelper;

    boolean estimatorsLoaded,
            shouldUseBt, shouldUseBtle, shouldUseWifi, shouldUseWifi5g;

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
        estimatorHelper = new EstimatorHelper(new EstimatorHelper.EstimatorsCallback() {
            @Override
            public void onEstimatorsLoaded() {
                addEvent(LOG_IMPORTANT, String.format(Locale.US,
                        "%d BT, %d BTLE, %d WIFI, and %d WIFI5G estimators loaded.",
                        estimatorHelper.getBt().size(), estimatorHelper.getBtle().size(),
                        estimatorHelper.getWifi().size(), estimatorHelper.getWifi5g().size()));
                for (TrainingListener l : listeners) l.onEstimatesLoaded();
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
        //TODO
//        bt.clearPendingSendLists();
//        btle.clearPendingSendLists();
//        wifi.clearPendingSendLists();
//        wifi5g.clearPendingSendLists();
    }

    public void clearTrainingSamples() {
        //TODO not available for now (would be bad accident)
//        bt.clearSamples();
//        btle.clearSamples();
//        wifi.clearSamples();
//        wifi5g.clearSamples();
    }

    void trainBt() {
        if (bt == null || !shouldUseBt) return;
        bt.train(new Trainer.TrainerCallbacks() {
            @Override
            public void onTrainingComplete(Estimator estimator) {
                estimatorHelper.addBt(new DistanceEstimator(estimator, DistanceEstimator.BT_MAX));
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
                estimatorHelper.addBtle(new DistanceEstimator(estimator, DistanceEstimator.BT_MAX));
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
                estimatorHelper.addWifi(new DistanceEstimator(estimator, DistanceEstimator.WIFI_MAX));
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
                estimatorHelper.addWifi5g(new DistanceEstimator(estimator, DistanceEstimator.WIFI_MAX));
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

    public void clearEstimators() {// TODO
    }

    public interface TrainingListener {
        void onMessageLogged(int level, String msg);
        void onSamplesLoaded();
        void onEstimatesLoaded();
    }
    private final List<TrainingListener> listeners = new ArrayList<>(1);
    public boolean registerListener(TrainingListener l) {
        return !listeners.contains(l) && listeners.add(l);
    }
    public boolean unregisterListener(TrainingListener l) {
        return listeners.remove(l);
    }
}
