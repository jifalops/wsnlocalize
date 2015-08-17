package com.jifalops.wsnlocalize.ffnn;

/**
 *
 */
public abstract class NeuralNetwork {
    final double[][] pop;
    final MlpWeightMetrics metrics;
    int best = -1;

    public abstract double[] train(double[][] data);

    public NeuralNetwork(double[][] pop, MlpWeightMetrics metrics) {
        this.pop = pop;
        this.metrics = metrics;
    }


    public double test(double[][] data) {
        return Util.calculateError(pop[best], data, metrics);
    }
}
