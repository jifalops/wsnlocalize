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
    protected Scaler scaler;

    public NeuralNetwork(double[][] population, MlpWeightMetrics weightMetrics, Scaler scaler) {
        this.population = population;
        this.weightMetrics = weightMetrics;
        this.scaler = scaler;
        this.errors = new double[population.length];
    }

    public double[] getGlobalBest() {
        return status.getBest();
    }

    public double test(double[][] data) {
        if (status == null) return 0;
        return calcError(status.getBest(), data);
    }


    protected abstract void trainSampleBySample(double[][] samples);

    public double[] trainSampleBySample(double[][] samples, TerminationConditions conditions) {
        samples = scaler.scaleAndRandomize(samples);
        status = new TrainingStatus(weightMetrics, conditions);
        double stdDev;
        int generation = 0;
        do {
            trainSampleBySample(samples);
            stdDev = Stats.stdDev(errors);
            generation++;
        } while (!status.isComplete(generation, stdDev));
        return status.getBest();
    }


    /**
     * Calculate outputs using the given weights and inputs.
     * The entire sample can be used (inputs + outputs) as long as the inputs
     * occupy the lower indexes (only the inputs will be used).
     */
    protected double[] calcOutputs(double[] weights, double[] inputsOrSample) {
        double[] outputs = new double[weightMetrics.numOutputs];
        double[] gamma = new double[weightMetrics.numHidden];
        double[] z = new double[weightMetrics.numHidden];

        int start;
        // Weights for connections between input and hidden neurons.
        for (int i = 0; i < weightMetrics.numInputs; i++) {
            start = i * weightMetrics.numHidden;
            for (int j = 0; j < weightMetrics.numHidden; j++) {
                gamma[j] += weights[start + j] * inputsOrSample[i];
            }
        }

        for (int j = 0; j < weightMetrics.numHidden; j++) {
            // Weights for the biases of hidden neurons.
            gamma[j] += weights[weightMetrics.hiddenBiasesStart + j];

            // Sigmoid activation
            z[j] = 1 / (1 + Math.exp(-gamma[j]));

            // Weights for connections between hidden and output neurons.
            start = weightMetrics.hiddenToOutputStart + j * weightMetrics.numOutputs;
            for (int k = 0; k < weightMetrics.numOutputs; k++) {
                outputs[k] += weights[start + k] * z[j];
            }
        }

        // Weights for the biases of output neurons.
        for (int k = 0; k < weightMetrics.numOutputs; k++) {
            outputs[k] += weights[weightMetrics.outputBiasesStart + k];
        }

        return outputs;
    }

    /**
     * Calculate the RMS error for an individual using all samples in the data set.
     */
    protected double calcError(double[] weights, double[][] samples) {
        double error = 0;
        double[] outputs;
        for (int i = 0; i < samples.length; i++) {
            outputs = calcOutputs(weights, samples[i]);
            for (int j = 0; j < weightMetrics.numOutputs; j++) {
                error += Math.pow(outputs[j] - samples[i][weightMetrics.numInputs + j], 2) / 2;
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
