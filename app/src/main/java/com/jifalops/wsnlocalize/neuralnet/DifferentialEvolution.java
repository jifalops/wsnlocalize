package com.jifalops.wsnlocalize.neuralnet;

import java.util.Arrays;
import java.util.Random;

/**
 *
 */
public class DifferentialEvolution extends NeuralNetwork {
    static final double F = 0.5;
    static final double CR = 0.9;

    static Random random = new Random();

    public DifferentialEvolution(double[][] population, MlpWeightMetrics weightMetrics) {
        super(population, weightMetrics);
    }

    @Override
    protected void onGenerationStarting(int index) {

    }

    @Override
    protected void trainSampleBySample(double[][] samples) {
        for (int i = 0; i < population.length; i++) {
            double[] crossed = crossover(population[i], mutate(population, i, F), CR);
            double err = calcError(crossed, samples);
            errors[i] = calcError(population[i], samples);
            if (err < errors[i]) {
                population[i] = crossed;
                errors[i] = err;
            }
            status.updateIfBest(population[i], errors[i]);
        }
    }

    /** TODO check this against literature. */
    static double[] mutate(double[][] population, int index, double F) {
        double[] mutated = new double[population[0].length];
        double[][] rands = getRandomIndividuals(population, 4, index);
        for (int i = 0, len = mutated.length; i < len; ++i) {
            mutated[i] = population[index][i] + F *
                    (rands[0][i] - rands[1][i] + rands[2][i] - rands[3][i]);
        }
        return mutated;
    }

    static double[] crossover(double[] original, double[] mutated, double CR) {
        int len = original.length;
        double[] crossed = new double[len];
        int index = random.nextInt(len);
        for (int i = 0; i < len; ++i) {
            if (Math.random() < CR || i == index) { // at least one will change
                crossed[i] = mutated[i];
            } else {
                crossed[i] = original[i];
            }
        }
        return crossed;
    }

    static double[] diff(double[] a1, double[] a2) {
        int len = a1.length;
        double[] diff = new double[len];
        for (int i = 0; i < len; ++i) {
            diff[i] = a1[i] - a2[i];
        }
        return diff;
    }

    static double[][] getRandomIndividuals(double[][] population, int count, int indexToAvoid) {
        double[][] individuals = new double[count][population[0].length];
        int[] indexes = getRandomIndexes(population, count, indexToAvoid);
        while (count > 0) {
            individuals[count - 1] = population[indexes[count - 1]];
            --count;
        }
        return individuals;
    }

    /** Returns [count] unique integers from 0 to the population size. */
    static int[] getRandomIndexes(double[][] population, int count, int indexToAvoid) {
        int[] indexes = new int[count];
        Arrays.fill(indexes, -1);
        int index;
        while (count > 0) {
            index = random.nextInt(population.length);
            if (index != indexToAvoid && !contains(indexes, index)) {
                indexes[count - 1] = index;
                --count;
            }
        }
        return indexes;
    }

    static boolean contains(int[] a, int index) {
        for (int i : a) {
            if (i == index) return true;
        }
        return false;
    }
}
