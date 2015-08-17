package com.jifalops.wsnlocalize.neuralnet;

/**
 *
 */
public abstract class NeuralNetwork {
    protected WeightVector[] pop;
    protected final MlpWeightMetrics metrics;
    protected int bestIndex = -1;

    public abstract double[] train(double[][] data);

    public NeuralNetwork(WeightVector[] pop, MlpWeightMetrics metrics) {
        this.pop = pop;
        this.metrics = metrics;
    }

    public double test(double[][] data) {
        return Util.calculateError(pop[bestIndex].getWeights(), data, metrics);
    }
}
