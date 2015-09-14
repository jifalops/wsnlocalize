package com.jifalops.wsnlocalize.data;

import android.os.Handler;
import android.os.HandlerThread;

import com.jifalops.wsnlocalize.neuralnet.Depso;
import com.jifalops.wsnlocalize.neuralnet.MlpWeightMetrics;
import com.jifalops.wsnlocalize.neuralnet.NeuralNetwork;
import com.jifalops.wsnlocalize.neuralnet.TerminationConditions;

import java.util.List;

/**
 *
 */
public class Trainer {
    public interface TrainerCallbacks {
        void onWindowRecordReady(WindowRecord record, List<RssiRecord> from);
        double[][] onTimeToTrain(List<WindowRecord> records, final double[][] samples);
        void onTrainingComplete(double[] weights, double error, int samples);
    }

    private final ResettingList<RssiRecord> windower;
    private final ResettingList<WindowRecord> trigger;
    public final NeuralNetwork nnet; // TODO make non-public
    private final TerminationConditions termCond;

    private final HandlerThread thread;
    private final Handler handler, uiHandler = new Handler();

    private final TrainerCallbacks callbacks;

    public Trainer(ResettingList.Limits rssiWindowLimits,
                   ResettingList.Limits windowTrainingLimits, TrainerCallbacks callbacks) {
        this.callbacks = callbacks;
        windower = new ResettingList<>(rssiWindowLimits, windowerCB);
        trigger = new ResettingList<>(windowTrainingLimits, trainingCB);
        MlpWeightMetrics metrics = new MlpWeightMetrics(WindowRecord.TRAINING_ARRAY_SIZE - 1, 1);
        nnet = new Depso(NeuralNetwork.initPop(20, metrics),
                NeuralNetwork.initPop(20, metrics), metrics);
        termCond = new TerminationConditions();
        thread = new HandlerThread(getClass().getName());
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    public void add(RssiRecord record) {
        windower.add(record);
    }

    private final ResettingList.LimitsCallback<RssiRecord> windowerCB =
            new ResettingList.LimitsCallback<RssiRecord>() {
        @Override
        public void onLimitsReached(List<RssiRecord> list, long time) {
            WindowRecord w = new WindowRecord(list);
            callbacks.onWindowRecordReady(w, list);
            trigger.add(w);
        }
    };

    private final ResettingList.LimitsCallback<WindowRecord> trainingCB =
            new ResettingList.LimitsCallback<WindowRecord>() {
        @Override
        public void onLimitsReached(List<WindowRecord> list, long time) {
            final double[][] samples = WindowRecord.toSamples(list);
            final double[][] toTrain = callbacks.onTimeToTrain(list, samples);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    final double[] weights = nnet.trainSampleBySample(toTrain, termCond);
                    final double error = nnet.getGlobalBestError();
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callbacks.onTrainingComplete(weights, error, toTrain.length);
                        }
                    });
                }
            });
        }
    };

    public void resetCurrentWindow() {
        windower.reset();
    }

    public void resetAllWindows() {
        windower.reset();
        trigger.reset();
    }

    public void close() {
        thread.quitSafely();
    }
}
