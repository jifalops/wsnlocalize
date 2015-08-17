package com.jifalops.wsnlocalize.ffnn;

/**
 *
 */
public class Population {
    static final int ACTIVATION_SIGMOID = 1;

    final int size;
    final MlpWeightMetrics metrics;
    private final Individual[] pop;
    private int best = -1;

    Population(int popSize, SampleMetrics metrics) {
        this(popSize, metrics, Math.round((metrics.inputs + metrics.outputs) / 2));
    }
    Population(int popSize, SampleMetrics sm, int hiddenNeurons) {
        size = popSize;
        metrics = new MlpWeightMetrics(sm, hiddenNeurons);
        pop = new Individual[popSize];
        for (int i = 0; i < popSize; i++) {
            pop[i] = new Individual(metrics.weights, Individual.INIT_RANDOM_NEGATIVE_ONE_TO_ONE);
        }
    }

    double[] estimateOutputs(double[] inputs, int individualIndex, int activationType) {
        switch (activationType) {
            case ACTIVATION_SIGMOID:
                return estimateOutputsUsingSigmoid(inputs, individualIndex);
        }
        return null;
    }

    double[] estimateOutputsUsingSigmoid(double[] inputs, int individualIndex) {
        double[] outputs = new double[metrics.outputs];
        double[] gamma = new double[metrics.hidden];
        double[] z = new double[metrics.hidden];


        // Weights for connections between input and hidden neurons.
        for (int i = 0; i < metrics.inputs; i++) {
            for (int j = 0; j < metrics.hidden; j++) {
                gamma[j] += pop[individualIndex].x[i * metrics.hidden + j] * inputs[i];
            }
        }

        for (int j = 0; j < metrics.hidden; j++) {
            // Weights for the biases of hidden neurons.
            gamma[j] += pop[individualIndex].x[metrics.hiddenBiasesStart + j];

            // Sigmoid activation
            z[j] = 1 / (1 + Math.exp(-gamma[j]));

            // Weights for connections between hidden and output neurons.
            for (int k = 0; k < metrics.outputs; k++) {
                outputs[k] += pop[individualIndex].
                        x[metrics.hiddenToOutputStart + j * metrics.outputs + k] * z[j];
            }
        }

        // Weights for the biases of output neurons.
        for (int k = 0; k < metrics.outputs; k++) {
            outputs[k] += pop[individualIndex].x[metrics.outputBiasesStart + k];
        }

        return outputs;
    }
}
