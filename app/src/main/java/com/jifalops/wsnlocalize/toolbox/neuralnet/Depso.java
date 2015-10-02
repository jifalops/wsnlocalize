package com.jifalops.wsnlocalize.toolbox.neuralnet;

/**
 *
 */
public class Depso extends ParticleSwarm {
    // Bounds for DE crossover and mutate
    double Fmin  = 0.5;
    double Fmax  = 1.0;
    double CRmin = 0.9;
    double CRmax = 1.0;

    double vMaxMin = 0.2;
    double vMaxMax = 0.8;

    double DEvPSO = 0.5;     // 0 = all DE, 1 = all PSO, .5 = default

    double entropy = 1.0;
    double alpha = 0.1;      // entropy decay ("convergence speed") max=1

    private double[][] samples;

    public Depso(SampleList samples, int popSize,
                 TerminationConditions termCond, TrainingCallbacks callbacks) {
        super(samples, popSize, termCond, callbacks);
    }

    @Override
    protected void onGenerationStarting(int index) {
        entropy *= 1 - alpha;
    }

    @Override
    protected void trainGeneration(double[][] samples) {
        this.samples = samples;
        super.trainGeneration(samples);
    }

    protected void updateVelocities() {
        double[][] tmp;
        double[] diff;
        double vmax = vMaxMin + (vMaxMax - vMaxMin) * entropy;
        for (int i = 0; i < population.length; i++) {
            tmp = DifferentialEvolution.getRandomIndividuals(population, 2, i);
            diff = DifferentialEvolution.diff(tmp[0], tmp[1]);
            for (int j = 0; j < population[0].length; j++) {
                v[i][j] = calcVelocity(population[i][j], v[i][j], diff[j], best[j]);
                if (v[i][j] > vmax) v[i][j] = vmax;
                else if (v[i][j] < -vmax) v[i][j] = -vmax;
            }
        }
    }

    protected double calcVelocity(double pos, double v, double pbest, double gbest) {
        return (v * w * entropy) +                                // inertia
                (entropy * c1 * pbest) +                          // individual cognition
                ((Math.random()/2 + 0.5) * c2 * (gbest - pos));   // social learning
    }

    @Override
    protected void updatePositions() {
        for (int i = 0; i < population.length; i++) {
            if (Math.random() < DEvPSO) {
                doDifferentialEvolution(i);
            } else {
                for (int j = 0; j < population[0].length; j++) {
                    population[i][j] += v[i][j];
                }
            }
        }
    }

    void doDifferentialEvolution(int index) {
        double F = Fmin + (Fmax - Fmin) * entropy;
        double CR = CRmin + (CRmax - CRmin) * entropy;

        double[] crossed = DifferentialEvolution.crossover(population[index],
                DifferentialEvolution.mutate(population, index, best, F), CR);
        double err = calcError(crossed, samples, metrics);
        errors[index] = calcError(population[index], samples, metrics);
        if (err < errors[index]) {
            population[index] = crossed;
            errors[index] = err;
        }
    }
}
