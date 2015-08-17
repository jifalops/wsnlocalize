package com.jifalops.wsnlocalize.ffnn;

import java.util.Random;

/**
 *
 */
public class Pso extends NeuralNetwork {
    double w  = 0.7298;
    double c1 = 1.496;
    double c2 = 1.496;
    double VmaxMin = .2;
    double VmaxMax = .8;

    Pso(double[][] pop, MlpWeightMetrics metrics) {
        super(pop, metrics);
    }

    void updateVelocities() {

    }

    @Override
    public double[] train(double[][] data) {
        return new double[0];
    }
}
