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

    // Adding velocity and particle-best info for population.
    protected final double[][] v;
    protected double[][] pbest;
    protected double[] pbestError;

    ParticleSwarm(double[][] particles, double[][] velocities, MlpWeightMetrics metrics) {
        super(particles, metrics);
        v = velocities;
        pbest = new double[particles.length][particles[0].length];
        pbestError = new double[particles.length];
        for (int i = 0; i < pbestError.length; ++i) {
            pbestError[i] = Double.MAX_VALUE;
        }
    }

    private void updateVelocities() {
        for (int i = 0; i < population.length; i++) {
            for (int j = 0; j < population[0].length; j++) {
                v[i][j] = calcVelocity(population[i][j], v[i][j], pbest[i][j], getGlobalBest()[j]);
            }
        }
    }

    private double calcVelocity(double pos, double v, double pbest, double gbest) {
        return (v * w) +                                    // inertia
                (Math.random() * c1 * (pbest - pos)) +      // individual cognition
                (Math.random() * c2 * (gbest - pos));       // social learning
    }

    private void updatePositions() {
        for (int i = 0; i < population.length; i++) {
            for (int j = 0; j < population[0].length; j++) {
                population[i][j] += v[i][j];
            }
        }
    }

    private void updateParticles() {
        updateVelocities();
        updatePositions();
    }

    @Override
    public void trainSampleBySample(double[][] samples) {
        updateParticles();
        for (int i = 0; i < population.length; i++) {
            errors[i] = calcError(population[i], samples);
            if (errors[i] < pbestError[i]) {
                pbest[i] = population[i];
                pbestError[i] = errors[i];

                status.updateIfBest(population[i], errors[i]);
            }
        }
    }
}
