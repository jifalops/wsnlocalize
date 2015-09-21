package com.jifalops.toolbox.neuralnet;

import android.os.AsyncTask;

/**
 *
 */
public class Trainer {
    public interface TrainerCallbacks {
        void onTrainingComplete(Estimator estimator);
    }

    final TerminationConditions termCond;
    final MlpWeightMetrics metrics;
    final double[][] samples;
    final int popSize;

    public Trainer(double[][] samples, MlpWeightMetrics metrics, int popSize,
                   TerminationConditions conditions) {
        this.samples = samples;
        this.metrics = metrics;
        this.popSize = popSize;
        termCond = conditions;
    }

    public void train(final TrainerCallbacks callbacks) {
        new AsyncTask<Void, Void, Estimator>() {
            @Override
            protected Estimator doInBackground(Void... params) {
                NeuralNetwork nnet = new Depso(NeuralNetwork.initPop(popSize, metrics),
                        NeuralNetwork.initPop(popSize, metrics), metrics, new NeuralNetwork.Callbacks() {
                    @Override
                    public void onGenerationFinished(int gen, double best, double mean, double stdDev) {

                    }
                });
                return nnet.trainSampleBySample(samples, termCond);
            }

            @Override
            protected void onPostExecute(Estimator estimator) {
                callbacks.onTrainingComplete(estimator);
            }
        }.execute();
    }
}
