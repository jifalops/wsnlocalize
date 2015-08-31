package com.jifalops.wsnlocalize.neuralnet;

import com.jifalops.wsnlocalize.util.Stats;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public abstract class NeuralNetwork {
    protected final double[][] population;
    protected final double[] errors;
    protected final MlpWeightMetrics metrics;
    protected TrainingStatus status;

    public NeuralNetwork(double[][] population, MlpWeightMetrics metrics) {
        this.population = population;
        this.metrics = metrics;
        this.errors = new double[population.length];
    }

    public double test(double[][] data) {
        if (status == null) return 0;
        return calcError(status.getBest(), data);
    }

    public double[] getGlobalBest() {
        return status.getBest();
    }

    /**
     * Calculate outputs using the current weights and the given inputs.
     * The entire sample can be used (inputs + outputs) as long as the inputs
     * occupy the lower indexes (only the inputs will be used).
     */
    protected double[] calcOutputs(double[] weights, double[] sample) {
        double[] outputs = new double[metrics.numOutputs];
        double[] gamma = new double[metrics.numHidden];
        double[] z = new double[metrics.numHidden];

        int start;
        // Weights for connections between input and hidden neurons.
        for (int i = 0; i < metrics.numInputs; i++) {
            start = i * metrics.numHidden;
            for (int j = 0; j < metrics.numHidden; j++) {
                gamma[j] += weights[start + j] * sample[i];
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
    protected double calcError(double[] weights, double[][] samples) {
        double error = 0;
        double[] outputs;
        for (int i = 0; i < samples.length; i++) {
            outputs = calcOutputs(weights, samples[i]);
            for (int j = 0; j < metrics.numOutputs; j++) {
                error += Math.pow(outputs[j] - samples[i][metrics.numInputs + j], 2) / 2;
            }
        }
        return error / samples.length;
    }


    protected abstract void trainSampleBySample(double[][] samples);

    public double[] trainSampleBySample(double[][] samples, TerminationConditions conditions) {
        status = new TrainingStatus(metrics, conditions);
        double stdDev;
        int generation = 0;
        do {
            trainSampleBySample(samples);
            stdDev = Stats.stdDev(errors);
            generation++;
        } while (!status.isComplete(generation, stdDev));
        return status.getBest();
    }





    public static double[][] scale(double[][] data, SampleMetrics metrics) {
        return scale(data, metrics, -1, 1, 0, 1);
    }
    public static double[][] scale(double[][] data, SampleMetrics metrics, double inputMin,
                            double inputMax, double outputMin, double outputMax) {
        int samples = data.length;
        int cols = data[0].length;
        double[][] scaled = new double[samples][cols];
        double min, max, diff,
                inDiff = inputMax - inputMin,
                outDiff = outputMax - outputMin;
        for (int i = 0; i < metrics.numInputs; i++) {
            min = Double.MAX_VALUE;
            max = Double.MIN_VALUE;
            for (int j = 0; j < samples; j++) {
                if (data[j][i] < min) min = data[j][i];
                if (data[j][i] > max) max = data[j][i];
            }
            diff = max - min;
            for (int j = 0; j < samples; j++) {
                scaled[j][i] = inputMin + (data[j][i] - min) * inDiff / diff;
            }
        }

        for (int i = metrics.numInputs; i < cols; i++) {
            min = Double.MAX_VALUE;
            max = Double.MIN_VALUE;
            for (int j = 0; j < samples; j++) {
                if (data[j][i] < min) min = data[j][i];
                if (data[j][i] > max) max = data[j][i];
            }
            diff = max - min;
            for (int j = 0; j < samples; j++) {
                scaled[j][i] = outputMin + (data[j][i] - min) * outDiff / diff;
            }
        }

        return scaled;
    }

    public static double[][] unscale(double[][] data, SampleMetrics metrics, double inputMin,
                                   double inputMax, double outputMin, double outputMax) {

    }


    public static double[][] randomize(double[][] data) {
        List<double[]> rand = Arrays.asList(data);
        Collections.shuffle(rand);
        return rand.toArray(new double[data.length][data[0].length]);
    }
}
