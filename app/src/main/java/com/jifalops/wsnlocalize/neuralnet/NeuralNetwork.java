package com.jifalops.wsnlocalize.neuralnet;

/**
 *
 */
public abstract class NeuralNetwork {
//    protected WeightVector[] pop;
    protected final double[][] weights;
    protected final MlpWeightMetrics metrics;
    protected double[] gbest; // Global best

    public abstract double[] train(double[][] data);

    public NeuralNetwork(double[][] weights, MlpWeightMetrics metrics) {
        this.weights = weights;
        this.metrics = metrics;
        gbest = weights[0];
    }

    public double test(double[][] data) {
        return calculateError(gbest, data);
    }

    public double[] getGlobalBest() {
        return gbest;
    }

    /**
     * Calculate outputs using the current weights and the given inputs.
     * The entire sample can be used (inputs + outputs) as long as the inputs
     * occupy the lower indexes (only the inputs will be used).
     */
    protected double[] calculateOutputs(double[] weights, double[] sample) {
        double[] outputs = new double[metrics.outputs];
        double[] gamma = new double[metrics.hidden];
        double[] z = new double[metrics.hidden];

        int start;
        // Weights for connections between input and hidden neurons.
        for (int i = 0; i < metrics.inputs; i++) {
            start = i * metrics.hidden;
            for (int j = 0; j < metrics.hidden; j++) {
                gamma[j] += weights[start + j] * sample[i];
            }
        }

        for (int j = 0; j < metrics.hidden; j++) {
            // Weights for the biases of hidden neurons.
            gamma[j] += weights[metrics.hiddenBiasesStart + j];

            // Sigmoid activation
            z[j] = 1 / (1 + Math.exp(-gamma[j]));

            // Weights for connections between hidden and output neurons.
            start = metrics.hiddenToOutputStart + j * metrics.outputs;
            for (int k = 0; k < metrics.outputs; k++) {
                outputs[k] += weights[start + k] * z[j];
            }
        }

        // Weights for the biases of output neurons.
        for (int k = 0; k < metrics.outputs; k++) {
            outputs[k] += weights[metrics.outputBiasesStart + k];
        }

        return outputs;
    }

    /**
     * Calculate the RMS error for this individual using all samples in the data set.
     */
    protected double calculateError(double[] weights, double[][] data) {
        double error = 0;
        double[] outputs;
        for (int i = 0; i < data.length; i++) {
            outputs = calculateOutputs(weights, data[i]);
            for (int j = 0; j < metrics.outputs; j++) {
                error += Math.pow(outputs[j] - data[i][metrics.inputs + j], 2) / 2;
            }
        }
        return error / data.length;
    }
}
