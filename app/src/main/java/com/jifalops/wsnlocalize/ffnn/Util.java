package com.jifalops.wsnlocalize.ffnn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class Util {
    private Util() {}
    static final int INIT_ZEROS = 0;
    static final int INIT_RANDOM_NEGATIVE_ONE_TO_ONE = 1;

    static void initWeights(double[] weights, int initType) {
        switch (initType) {
            case INIT_ZEROS:
                for (int i = 0; i < weights.length; i++) {
                    weights[i] = 0;
                }
                break;
            case INIT_RANDOM_NEGATIVE_ONE_TO_ONE:
                for (int i = 0; i < weights.length; i++) {
                    weights[i] = (Math.random() - 0.5) * 2; // [-1, 1)
                }
                break;
        }
    }


    /**
     * Calculate outputs using the current weights and the given inputs.
     * The entire sample can be used (inputs + outputs) as long as the inputs
     * occupy the lower indexes (only the inputs will be used).
     */
    static double[] calculateOutputs(double[] weights, double[] sample, MlpWeightMetrics m) {
        double[] outputs = new double[m.outputs];
        double[] gamma = new double[m.hidden];
        double[] z = new double[m.hidden];


        // Weights for connections between input and hidden neurons.
        for (int i = 0; i < m.inputs; i++) {
            for (int j = 0; j < m.hidden; j++) {
                gamma[j] += weights[i * m.hidden + j] * sample[i];
            }
        }

        for (int j = 0; j < m.hidden; j++) {
            // Weights for the biases of hidden neurons.
            gamma[j] += weights[m.hiddenBiasesStart + j];

            // Sigmoid activation
            z[j] = 1 / (1 + Math.exp(-gamma[j]));

            // Weights for connections between hidden and output neurons.
            for (int k = 0; k < m.outputs; k++) {
                outputs[k] += weights[m.hiddenToOutputStart + j * m.outputs + k] * z[j];
            }
        }

        // Weights for the biases of output neurons.
        for (int k = 0; k < m.outputs; k++) {
            outputs[k] += weights[m.outputBiasesStart + k];
        }

        return outputs;
    }

    /**
     * Calculate the RMS error for this individual using all samples in the data set.
     */
    static double calculateError(double[] weights, double[][] data, MlpWeightMetrics m) {
        double error = 0;
        double[] outputs;
        for (int i = 0; i < data.length; i++) {
            outputs = calculateOutputs(weights, data[i], m);
            for (int j = 0; j < m.outputs; j++) {
                error += Math.pow(outputs[j] - data[i][m.inputs + j], 2) / 2;
            }
        }
        return error / data.length;
    }

    static double[][] scale(double[][] data, SampleMetrics metrics) {
        return scale(data, metrics, -1, 1, 0, 1);
    }
    static double[][] scale(double[][] data, SampleMetrics metrics, double inputMin,
                            double inputMax, double outputMin, double outputMax) {
        int samples = data.length;
        int cols = data[0].length;
        double[][] scaled = new double[samples][cols];
        double min, max, diff,
                inDiff = inputMax - inputMin,
                outDiff = outputMax - outputMin;
        for (int i = 0; i < metrics.inputs; i++) {
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

        for (int i = metrics.inputs; i < cols; i++) {
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


    static double[][] randomize(double[][] data) {
        List<double[]> rand = Arrays.asList(data);
        Collections.shuffle(rand);
        return rand.toArray(new double[data.length][data[0].length]);
    }
}
