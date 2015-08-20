package com.jifalops.wsnlocalize.neuralnet;

/**
 *
 */
public class ParticleSwarm extends NeuralNetwork {
    /** Inertia weight */
    double w  = 0.7298;
    /** Individual cognition constant */
    double c1 = 1.496;
    /** Social learning constant */
    double c2 = 1.496;

    // limit the max velocity
    double VmaxMin = .2;
    double VmaxMax = .8;

    // Adding velocity and particle-best info for population.
    protected final double[][] v;
    protected double[][] pbest;

    ParticleSwarm(double[][] positions, double[][] velocities, MlpWeightMetrics metrics) {
        super(positions, metrics);
        v = velocities;
        pbest = positions.clone();
    }

    private void updateVelocities() {
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[0].length; j++) {
                v[i][j] = calcVelocity(weights[i][j], v[i][j], pbest[i][j], gbest[j]);
            }
        }
    }

    private double calcVelocity(double pos, double v, double pbest, double gbest) {
        return (v * w) +                                    // inertia
                (Math.random() * c1 * (pbest - pos)) +      // individual cognition
                (Math.random() * c2 * (gbest - pos));       // social learning
    }

    private void updateWeights() {
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[0].length; j++) {
                weights[i][j] += v[i][j];
            }
        }
    }

    @Override
    public double[] train(double[][] data) {
        return new double[0];
    }
}
