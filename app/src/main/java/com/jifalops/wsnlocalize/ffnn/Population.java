package com.jifalops.wsnlocalize.ffnn;

/**
 *
 */
public class Population {

    final int size;
    final MlpWeightMetrics metrics;
    final Individual[] pop;
    int best = -1;

    Population(int popSize, SampleMetrics metrics) {
        this(popSize, metrics, Math.round((metrics.inputs + metrics.outputs) / 2));
    }
    Population(int popSize, SampleMetrics sm, int hiddenNeurons) {
        size = popSize;
        metrics = new MlpWeightMetrics(sm, hiddenNeurons);
        pop = new Individual[popSize];
        for (int i = 0; i < popSize; i++) {
            pop[i] = new Individual(metrics.weights, Individual.INIT_RANDOM_NEGATIVE_ONE_TO_ONE);
        }
    }
}
