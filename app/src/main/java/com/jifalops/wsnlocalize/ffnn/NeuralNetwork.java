package com.jifalops.wsnlocalize.ffnn;

/**
 *
 */
public abstract class NeuralNetwork {
    Population pop;

    public abstract double[] train(DataSet data);


    public NeuralNetwork(Population pop) {
        this.pop = pop;
    }


    public double test(DataSet data) {
        return pop.pop[pop.best].calculateError(data, pop.metrics);
    }
}
