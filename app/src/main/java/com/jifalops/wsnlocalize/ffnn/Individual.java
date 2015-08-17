package com.jifalops.wsnlocalize.ffnn;

import java.util.Random;

/**
 *
 */
public class Individual {
    static final int INIT_ZEROS = 0;
    static final int INIT_RANDOM_NEGATIVE_ONE_TO_ONE = 1;

    /** position */
    final double[] x;
    /** velocity */
    final double[] v;

    /** Create a new individual with the specified number of weights */
    Individual(int size, int initType) {
        x = new double[size];
        v = new double[size];

        switch (initType) {
            case INIT_RANDOM_NEGATIVE_ONE_TO_ONE:
                for (int i = 0; i < size; i++) {
                    x[i] = (Math.random() - 0.5) * 2; // [-1, 1)
                    v[i] = (Math.random() - 0.5) * 2; // [-1, 1)
                }
                break;
        }
    }

    /**
     * Calculate outputs using the current weights and the given inputs.
     * The entire sample can be used (inputs + outputs) as long as the inputs
     * occupy the lower indexes (only the inputs will be used).
     */
    double[] calculateOutputs(double[] sample, MlpWeightMetrics m) {
        double[] outputs = new double[m.outputs];
        double[] gamma = new double[m.hidden];
        double[] z = new double[m.hidden];


        // Weights for connections between input and hidden neurons.
        for (int i = 0; i < m.inputs; i++) {
            for (int j = 0; j < m.hidden; j++) {
                gamma[j] += x[i * m.hidden + j] * sample[i];
            }
        }

        for (int j = 0; j < m.hidden; j++) {
            // Weights for the biases of hidden neurons.
            gamma[j] += x[m.hiddenBiasesStart + j];

            // Sigmoid activation
            z[j] = 1 / (1 + Math.exp(-gamma[j]));

            // Weights for connections between hidden and output neurons.
            for (int k = 0; k < m.outputs; k++) {
                outputs[k] += x[m.hiddenToOutputStart + j * m.outputs + k] * z[j];
            }
        }

        // Weights for the biases of output neurons.
        for (int k = 0; k < m.outputs; k++) {
            outputs[k] += x[m.outputBiasesStart + k];
        }

        return outputs;
    }

    /**
     * Calculate the RMS error for this individual using all samples in the data set.
     */
    double calculateError(DataSet data, MlpWeightMetrics m) {
        double error = 0;
        double[] outputs;
        for (int i = 0; i < data.data.length; i++) {
            outputs = calculateOutputs(data.data[i], m);
            for (int j = 0; j < m.outputs; j++) {
                error += Math.pow(outputs[j] - data.data[i][m.inputs + j], 2) / 2;
            }
        }
        return error / data.data.length;
    }
}
