package com.jifalops.wsnlocalize.toolbox.neuralnet;

import android.os.AsyncTask;

import com.jifalops.wsnlocalize.toolbox.util.Arrays;
import com.jifalops.wsnlocalize.toolbox.util.Stats;

import java.util.List;

/**
 *
 */
public abstract class NeuralNetwork {

    public interface TrainingCallbacks {
        /** called on neural network thread. */
        void onGenerationFinished(int gen, double best, double mean, double stdDev);
        /** called on neural network thread. */
        void onTrainingComplete(TrainingResults results);
    }

    protected final SampleList samples;
    protected final int popSize;
    protected final MlpWeightMetrics metrics;
    protected final TerminationConditions termCond;
    protected final TrainingCallbacks callbacks;

    protected double[][] population;
    protected double[] errors;
    protected double[] best;
    protected int bestIndex;
    private double[] bestError;

    public NeuralNetwork(SampleList samples, int popSize,
                         TerminationConditions termCond, TrainingCallbacks callbacks) {
        this.samples = samples;
        this.popSize = popSize;
        this.termCond = termCond;
        this.callbacks = callbacks;
        metrics = new MlpWeightMetrics(samples.getNumInputs(), samples.getNumOutputs());
    }

    protected abstract void prepareToTrain();
    protected abstract void onGenerationStarting(int index);
    protected abstract void trainGeneration(double[][] toTrain);

    public void train() {
        new AsyncTask<Void, Double, TrainingResults>() {
            @Override
            protected TrainingResults doInBackground(Void... params) {
                double mean, stdDev;
                int generation = 0;

                population = initPop(popSize, metrics);
                double[][] toTrain = samples.getScaler().scaleAndRandomize(samples.toDoubleArray());

                errors = new double[popSize];
                for (int i = 0; i < errors.length; ++i) {
                    errors[i] = 1_000_000;
                }

                best = population[0];
                bestError = new double[termCond.minImprovementGenerations];
                for (int i = 0; i < bestError.length; i++) {
                    bestError[i] = 1_000_000;
                }

                prepareToTrain();

                do {
                    onGenerationStarting(generation);
                    trainGeneration(toTrain);
                    mean = Stats.mean(errors);
                    stdDev = Stats.stdDev(errors);
                    ++generation;
                    publishProgress((double)generation, bestError[0], mean, stdDev);
                } while (!isComplete(generation, stdDev));
                return new TrainingResults(best, bestError[0], mean, stdDev, generation);
            }

            @Override
            protected void onProgressUpdate(Double... values) {
                callbacks.onGenerationFinished((int)(double)values[0], values[1], values[2], values[3]);
            }

            @Override
            protected void onPostExecute(TrainingResults trainingResults) {
                callbacks.onTrainingComplete(trainingResults);
            }
        }.execute();
    }

    protected boolean updateIfBest(int index) {
        if (errors[index] < bestError[0]) {
            best = population[index];
            bestIndex = index;
            Arrays.shiftRight(bestError, 1);
            bestError[0] = errors[index];
            return true;
        } else {
            return false;
        }
    }

    protected boolean isComplete(int generation, double errorStdDev) {
        return generation >= termCond.maxGenerations ||
                bestError[0] <= termCond.minError ||
                errorStdDev <= termCond.minStdDev ||
                bestError[bestError.length - 1] - bestError[0] < termCond.minImprovement ;
    }


    /**
     * Calculate outputs using the given weights and inputs.
     * The entire sample can be used (inputs + outputs) as long as the inputs
     * occupy the lower indexes (only the inputs will be used).
     */
    protected static double[] calcOutputs(double[] weights, double[] inputsOrSample, MlpWeightMetrics metrics) {
        double[] outputs = new double[metrics.numOutputs];
        double[] gamma = new double[metrics.numHidden];
        double[] z = new double[metrics.numHidden];

        int start;
        // Weights for connections between input and hidden neurons.
        for (int i = 0; i < metrics.numInputs; i++) {
            start = i * metrics.numHidden;
            for (int j = 0; j < metrics.numHidden; j++) {
                gamma[j] += weights[start + j] * inputsOrSample[i];
            }
        }

        for (int j = 0; j < metrics.numHidden; j++) {
            // Weights for the biases of hidden neurons.
            gamma[j] += weights[metrics.hiddenBiasesStart + j];

            // Sigmoid activation
            z[j] = 1 / (1 + Math.exp(-gamma[j]));

            // Weights for connections between hidden and output neurons.
            start = metrics.hiddenToOutputStart + j * metrics.numOutputs;
            for (int k = 0; k < metrics.numOutputs; k++) {
                outputs[k] += weights[start + k] * z[j];
            }
        }

        // Weights for the biases of output neurons.
        for (int k = 0; k < metrics.numOutputs; k++) {
            outputs[k] += weights[metrics.outputBiasesStart + k];
        }

        return outputs;
    }

    /**
     * Calculate the RMS error for an individual using all samples in the data set.
     */
    protected static double calcError(double[] weights, double[][] samples, MlpWeightMetrics metrics) {
        double error = 0;
        double[] outputs;
        for (int i = 0; i < samples.length; i++) {
            outputs = calcOutputs(weights, samples[i], metrics);
            for (int j = 0; j < metrics.numOutputs; j++) {
                error += Math.pow(outputs[j] - samples[i][metrics.numInputs + j], 2) / 2;
            }
        }
        return error / samples.length;
    }



    protected static double[][] initPop(int popSize, MlpWeightMetrics metrics) {
        double[][] pop = new double[popSize][metrics.numWeights];
        for (int i = 0; i < popSize; i++) {
            for (int j = 0; j < metrics.numWeights; j++) {
                pop[i][j] = (Math.random() - 0.5) * 2; // [-1, 1)
            }
        }
        return pop;
    }


    public static double[] estimate(double[] inputsOrSample, Scaler scaler,
                                    double[] weights, MlpWeightMetrics metrics) {
        double[] scaled = scaler.scale(new double[][]{inputsOrSample})[0];
        double[] outputs = calcOutputs(weights, scaled, metrics);
        return scaler.unscale(outputs);
    }

    public static double[][] estimate(double[] inputsOrSample, Scaler scaler,
                                    List<TrainingResults> results, MlpWeightMetrics metrics) {
        double[] scaled = scaler.scale(new double[][]{inputsOrSample})[0];
        int rows = results.size();
        double[][] estimates = new double[rows][];
        for (int i = 0; i < rows; ++i) {
            estimates[i] = scaler.unscale(calcOutputs(results.get(i).weights, scaled, metrics));
        }
        return estimates;
    }
}
