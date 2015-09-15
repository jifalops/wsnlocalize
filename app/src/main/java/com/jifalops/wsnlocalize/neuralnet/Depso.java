package com.jifalops.wsnlocalize.neuralnet;

/**
 *
 */
public class Depso extends ParticleSwarm {
    // Bounds for DE crossover and mutate
    double Fmax  = .9;
    double Fmin  = .5;
    double CRmin = .5;
    double CRmax = .9;

    double alpha = 0.1;      // entropy decay ("convergence speed") max=1
    double DEvPSO = 0.5;     // 0 = all DE, 1 = all PSO, .5 = default

    double entropy = 1;

    private double[][] samples;

    public Depso(double[][] population, double[][] velocities, MlpWeightMetrics weightMetrics, Callbacks cb) {
        super(population, velocities, weightMetrics, cb);
    }

    @Override
    protected void onGenerationStarting(int index) {
        entropy *= 1 - alpha;
    }

    @Override
    protected void trainSampleBySample(double[][] samples) {
        this.samples = samples;
        super.trainSampleBySample(samples);
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
                DifferentialEvolution.mutate(population, index, F), CR);
        double err = calcError(crossed, samples);
        errors[index] = calcError(population[index], samples);
        if (err < errors[index]) {
            population[index] = crossed;
            errors[index] = err;
        }
    }
}
