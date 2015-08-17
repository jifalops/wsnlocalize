package com.jifalops.wsnlocalize.ffnn;

/**
 *
 */
public interface NeuralNetwork {
    double[] train(DataSet data);
    double test(DataSet data, double[] weights);
}
