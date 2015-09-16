package com.jifalops.wsnlocalize.neuralnet;

/**
 *
 */
public class ParticleSwarm extends NeuralNetwork {
    /** Inertia weight */
    static final double w  = 0.7298;
    /** Individual cognition constant */
    static final double c1 = 1.496;
    /** Social learning constant */
    static final double c2 = 1.496;

    // Adding velocity and particle-best info for population.
    protected final double[][] v;
    protected double[][] pbest;
    protected double[] pbestError;

    ParticleSwarm(double[][] particles, double[][] velocities, MlpWeightMetrics metrics, Callbacks cb) {
        super(particles, metrics, cb);
        v = velocities;
        pbest = particles.clone();
        pbestError = new double[particles.length];
        for (int i = 0; i < pbestError.length; ++i) {
            pbestError[i] = 1_000_000;
        }
    }

    protected void updateVelocities() {
        for (int i = 0; i < population.length; i++) {
            for (int j = 0; j < population[0].length; j++) {
                v[i][j] = calcVelocity(population[i][j], v[i][j], pbest[i][j], getGlobalBest()[j]);
            }
        }
    }

    protected double calcVelocity(double pos, double v, double pbest, double gbest) {
        return (v * w) +                                    // inertia
                (Math.random() * c1 * (pbest - pos)) +      // individual cognition
                (Math.random() * c2 * (gbest - pos));       // social learning
    }

    protected void updatePositions() {
        for (int i = 0; i < population.length; i++) {
            for (int j = 0; j < population[0].length; j++) {
                population[i][j] += v[i][j];
            }
        }
    }

    protected void updateParticles() {
        updateVelocities();
        updatePositions();
    }

    @Override
    protected void onGenerationStarting(int index) {

    }

    @Override
    protected void trainSampleBySample(double[][] samples) {
        updateParticles();
        for (int i = 0; i < population.length; i++) {
            errors[i] = calcError(population[i], samples);
            if (errors[i] < pbestError[i]) {
                pbest[i] = population[i];
                pbestError[i] = errors[i];

                status.updateIfBest(population[i], i, errors[i]);
            }
        }
    }
}
