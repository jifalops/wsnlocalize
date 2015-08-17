package com.jifalops.wsnlocalize.neuralnet.pso;

import com.jifalops.wsnlocalize.neuralnet.WeightVector;

/**
 *
 */
public class Particle extends WeightVector {
    private final double[] v;
    private double[] best;

    public Particle(int size, int initType) {
        super(size, initType);
        v = new double[size];
        initWeights(v, initType);
        best = weights.clone();
    }

    public double getVelocity(int index) {
        return v[index];
    }
    public double[] getVelocity() {
        return v;
    }
    public void setVelocity(int index, double value) {
        v[index] = value;
    }

    public double getBest(int index) {
        return best[index];
    }
    public double[] getBest() {
        return best;
    }
    public void setBest(double[] best) {
        this.best = best;
    }
}
