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
    public static abstract class TrainingCallbacks implements RssiWindower.Callback, TrainingTrigger.Callback {
        public abstract void onTrainingComplete(double[] weights, double error, int samples);
    }

    private final RssiWindower windower;
    private final TrainingTrigger trigger;
    public final NeuralNetwork nnet;
    private final TerminationConditions termCond;

    private final HandlerThread thread;
    private final Handler handler, uiHandler = new Handler();

    private final TrainingCallbacks callbacks;

    public Trainer(int minRssiCountForWindow, int minRssiTimeMillisForWindow,
                   int minWindowCountForTraining, int minWindowTimeMillisForTraining,
                    TrainingCallbacks callbacks) {
        this.callbacks = callbacks;
        windower = new RssiWindower(minRssiCountForWindow, minRssiTimeMillisForWindow, windowerCB);
        trigger = new TrainingTrigger(minWindowCountForTraining, minWindowTimeMillisForTraining, trainingCB);
        MlpWeightMetrics metrics = new MlpWeightMetrics(WindowRecord.TRAINING_ARRAY_SIZE - 1, 1);
        nnet = new Depso(NeuralNetwork.initPop(20, metrics),
                NeuralNetwork.initPop(20, metrics), metrics);
        termCond = new TerminationConditions();
        thread = new HandlerThread(getClass().getName());
        handler = new Handler(thread.getLooper());
    }

    public void add(RssiRecord record) {
        windower.add(record);
    }


    private boolean post(Runnable r) {
        if (thread.getState() == Thread.State.NEW) {
            thread.start();
        }
        return handler.post(r);
    }

    private final RssiWindower.Callback windowerCB = new RssiWindower.Callback() {
        @Override
        public void onWindowRecordReady(WindowRecord record, List<RssiRecord> from) {
            callbacks.onWindowRecordReady(record, from);
            trigger.add(record);
        }
    };

    private final TrainingTrigger.Callback trainingCB = new TrainingTrigger.Callback() {
        @Override
        public double[][] onTimeToTrain(List<WindowRecord> records, final double[][] samples) {
            final double[][] toTrain = callbacks.onTimeToTrain(records, samples);
            post(new Runnable() {
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
            return toTrain;
        }
    };
}
