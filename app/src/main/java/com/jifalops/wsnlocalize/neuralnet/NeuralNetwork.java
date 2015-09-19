package com.jifalops.wsnlocalize.neuralnet;

import com.jifalops.wsnlocalize.util.Stats;

/**
 *
 */
public abstract class NeuralNetwork {
    protected final double[][] population;
    protected final double[] errors;
    protected final MlpWeightMetrics weightMetrics;
    protected TrainingStatus status;
    protected final Callbacks callbacks;

    public interface Callbacks {
        /** called on non-UI thread. */
        void onGenerationFinished(int gen, double best, double mean, double stdDev);
    }

    public NeuralNetwork(double[][] population, MlpWeightMetrics weightMetrics, Callbacks cb) {
        this.population = population;
        this.weightMetrics = weightMetrics;
        this.errors = new double[population.length];
        for (int i = 0; i < errors.length; ++i) {
            errors[i] = 1_000_000;
        }
        callbacks = cb;
    }

    public double[] getGlobalBest() {
        return status.getBest();
    }
    public double getGlobalBestError() { return status.getBestError(); }

    protected abstract void onGenerationStarting(int index);
    protected abstract void trainSampleBySample(double[][] samples);

    public TrainingResults trainSampleBySample(double[][] samples, TerminationConditions conditions) {
        Scaler scaler = new Scaler(samples, weightMetrics.numInputs);
        samples = scaler.scaleAndRandomize(samples);
        status = new TrainingStatus(weightMetrics, conditions);
        status.updateIfBest(population[0], 0, 1_000_000 - 1);
        double mean, stdDev;
        int generation = 0;
        do {
            onGenerationStarting(generation);
            trainSampleBySample(samples);
            mean = Stats.mean(errors);
            stdDev = Stats.stdDev(errors);
            generation++;
            callbacks.onGenerationFinished(generation, status.getBestError(), mean, stdDev);
        } while (!status.isComplete(generation, stdDev));
        return new TrainingResults(status.getBest(), weightMetrics, status.getBestError(),
                mean, stdDev, samples.length, generation, scaler);
    }


    /**
     * Calculate outputs using the given weights and inputs.
     * The entire sample can be used (inputs + outputs) as long as the inputs
     * occupy the lower indexes (only the inputs will be used).
     */
    public static double[] calcOutputs(double[] weights, double[] inputsOrSample, MlpWeightMetrics metrics) {
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



    public static double[][] initPop(int popSize, MlpWeightMetrics metrics) {
        double[][] pop = new double[popSize][metrics.numWeights];
        for (int i = 0; i < popSize; i++) {
            for (int j = 0; j < metrics.numWeights; j++) {
                pop[i][j] = (Math.random() - 0.5) * 2; // [-1, 1)
            }
        }
        return pop;
    }
}
