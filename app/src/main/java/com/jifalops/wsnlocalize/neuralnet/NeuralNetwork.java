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
    protected final MlpWeightMetrics weightMetrics;
    protected TrainingStatus status;

    private TrainingMetrics trainingMetrics;
    private ScaleMetrics scaleMetrics;

    public NeuralNetwork(double[][] population, MlpWeightMetrics weightMetrics) {
        this.population = population;
        this.weightMetrics = weightMetrics;
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

    public double[] trainSampleBySample(double[][] samples, ScaleMetrics metrics,
                                        TerminationConditions conditions) {
        scaleMetrics = metrics;

        trainingMetrics = new TrainingMetrics(samples);
        samples = randomize(scale(samples, weightMetrics, metrics));
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

    public static double[][] scale(double[][] data, SampleMetrics metrics, ScaleMetrics s) {
        int samples = data.length;
        int cols = data[0].length;
        double[][] scaled = new double[samples][cols];
        double min, max, diff,
                inDiff = s.inputMax - s.inputMin,
                outDiff = s.outputMax - s.outputMin;
        for (int i = 0; i < metrics.numInputs; i++) {
            min = Double.MAX_VALUE;
            max = Double.MIN_VALUE;
            for (int j = 0; j < samples; j++) {
                if (data[j][i] < min) min = data[j][i];
                if (data[j][i] > max) max = data[j][i];
            }
            diff = max - min;
            for (int j = 0; j < samples; j++) {
                scaled[j][i] = s.inputMin + (data[j][i] - min) * inDiff / diff;
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
                scaled[j][i] = s.outputMin + (data[j][i] - min) * outDiff / diff;
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
