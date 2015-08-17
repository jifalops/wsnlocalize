package com.jifalops.wsnlocalize.neuralnet.pso;

import com.jifalops.wsnlocalize.neuralnet.MlpWeightMetrics;
import com.jifalops.wsnlocalize.neuralnet.NeuralNetwork;
import com.jifalops.wsnlocalize.neuralnet.WeightVector;

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

    ParticleSwarm(Particle[] pop, MlpWeightMetrics metrics) {
        super(pop, metrics);
    }

    private void updateVelocities() {
        Particle[] p = (Particle[]) pop;
        WeightVector gbest = pop[bestIndex];
        for (int i = 0; i < pop.length; i++) {
            for (int j = 0; j < pop[0].getSize(); j++) {
                p[i].setVelocity(j, calcVelocity(
                        p[i].getWeight(j), p[i].getVelocity(j),
                        p[i].getBest(j), gbest.getWeight(j)));
            }
        }
    }

    private double calcVelocity(double pos, double v, double pbest, double gbest) {
        return (v * w) +                                    // inertia
                (Math.random() * c1 * (pbest - pos)) +      // individual cognition
                (Math.random() * c2 * (gbest - pos));       // social learning
    }

    private void updateWeights() {
        Particle[] p = (Particle[]) pop;
        for (int i = 0; i < pop.length; i++) {
            p[i].setWeight(i, p[i].getWeight(i) + p[i].getVelocity(i));
        }
    }

    @Override
    public double[] train(double[][] data) {
        return new double[0];
    }
}
