package com.jifalops.wsnlocalize.neuralnet;

import java.util.ArrayList;

/**
 *
 */
public class WeightVector {
    public static final int INIT_ZEROS = 0;
    public static final int INIT_RANDOM_NEGATIVE_ONE_TO_ONE = 1;

    protected final double[] weights;

    public WeightVector(double[] weights) {
        this.weights = weights;
    }

    public WeightVector(int size, int initType) {
        weights = new double[size];
        initWeights(weights, initType);
    }

    protected void initWeights(double[] weights, int initType) {
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

    public int getSize() {
        return weights.length;
    }
    public double getWeight(int index) {
        return weights[index];
    }
    public double[] getWeights() {
        return weights;
    }
    public void setWeight(int index, double value) {
        weights[index] = value;
    }
}
