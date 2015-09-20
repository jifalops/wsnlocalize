package com.jifalops.wsnlocalize.signal;

import android.os.Handler;
import android.os.HandlerThread;

import com.jifalops.wsnlocalize.data.WindowRecord;
import com.jifalops.wsnlocalize.neuralnet.Depso;
import com.jifalops.wsnlocalize.neuralnet.MlpWeightMetrics;
import com.jifalops.wsnlocalize.neuralnet.NeuralNetwork;
import com.jifalops.wsnlocalize.neuralnet.TerminationConditions;
import com.jifalops.wsnlocalize.neuralnet.TrainingResults;

/**
 *
 */
public class Trainer2 {
    public interface TrainerCallbacks {
        void onTrainingComplete(TrainingResults results);
    }

    private NeuralNetwork nnet;
    private final TerminationConditions termCond;

    private final HandlerThread thread;
    private final Handler handler, uiHandler = new Handler();

    private final TrainerCallbacks callbacks;
    final MlpWeightMetrics metrics;

    final double[][] samples;

    public Trainer2(double[][] samples, TrainerCallbacks callbacks) {
        this.samples = samples;
        this.callbacks = callbacks;
        metrics = new MlpWeightMetrics(WindowRecord.TRAINING_ARRAY_SIZE - 1, 1);
        termCond = new TerminationConditions();
        thread = new HandlerThread(getClass().getName());
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    public void train() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                nnet = new Depso(NeuralNetwork.initPop(20, metrics),
                        NeuralNetwork.initPop(20, metrics), metrics, new NeuralNetwork.Callbacks() {
                    @Override
                    public void onGenerationFinished(int gen, double best, double mean, double stdDev) {

                    }
                });

                final TrainingResults results = nnet.trainSampleBySample(samples, termCond);

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callbacks.onTrainingComplete(results);
                    }
                });
            }
        });
    }

    public void close() {
        thread.quitSafely();
    }
}
