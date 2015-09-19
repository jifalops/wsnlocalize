package com.jifalops.wsnlocalize.signal;

import android.os.Handler;
import android.os.HandlerThread;

import com.jifalops.wsnlocalize.data.RssiRecord;
import com.jifalops.wsnlocalize.data.WindowRecord;
import com.jifalops.wsnlocalize.neuralnet.Depso;
import com.jifalops.wsnlocalize.neuralnet.MlpWeightMetrics;
import com.jifalops.wsnlocalize.neuralnet.NeuralNetwork;
import com.jifalops.wsnlocalize.neuralnet.TerminationConditions;
import com.jifalops.wsnlocalize.neuralnet.TrainingResults;
import com.jifalops.wsnlocalize.util.ResettingList;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class Trainer {
    public interface TrainerCallbacks {
        void onWindowRecordReady(WindowRecord record, List<RssiRecord> from);
        double[][] onTimeToTrain(List<WindowRecord> records, final double[][] samples);
        void onGenerationFinished(int gen, double best, double mean, double stdDev);
        void onTrainingComplete(TrainingResults results);
    }

    private final ResettingList<RssiRecord> windower;
    private final ResettingList<WindowRecord> trigger;
    private   NeuralNetwork nnet;
    private final TerminationConditions termCond;

    private final HandlerThread thread;
    private final Handler handler, uiHandler = new Handler();

    private final TrainerCallbacks callbacks;
    final MlpWeightMetrics metrics;

    public Trainer(ResettingList.Limits rssiWindowLimits,
                   ResettingList.Limits windowTrainingLimits, final TrainerCallbacks callbacks) {
        this.callbacks = callbacks;
        windower = new ResettingList<>(rssiWindowLimits, windowerCB);
        trigger = new ResettingList<>(windowTrainingLimits, trainingCB);
        metrics = new MlpWeightMetrics(WindowRecord.TRAINING_ARRAY_SIZE - 1, 1);
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
        public void onLimitsReached(final List<WindowRecord> list, long time) {
            final double[][] samples = WindowRecord.toSamples(list);
            final double[][] toTrain = callbacks.onTimeToTrain(list, samples);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    final AtomicInteger generations = new AtomicInteger();
//                    final Scaler scaler = new Scaler(toTrain, toTrain[0].length-1);

                    nnet = new Depso(NeuralNetwork.initPop(20, metrics),
                            NeuralNetwork.initPop(20, metrics), metrics, new NeuralNetwork.Callbacks() {
                        @Override
                        public void onGenerationFinished(int gen, double best, double mean, double stdDev) {
                            generations.set(gen);
                            callbacks.onGenerationFinished(gen, best, mean, stdDev);
                        }
                    });

                    final TrainingResults results = nnet.trainSampleBySample(toTrain, termCond);
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callbacks.onTrainingComplete(results);
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
