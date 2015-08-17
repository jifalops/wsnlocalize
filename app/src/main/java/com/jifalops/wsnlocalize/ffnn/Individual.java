package com.jifalops.wsnlocalize.ffnn;

import java.util.Random;

/**
 *
 */
public class Individual {
    static final int INIT_ZEROS = 0;
    static final int INIT_RANDOM_NEGATIVE_ONE_TO_ONE = 1;

    /** position */
    final double[] x;
    /** velocity */
    final double[] v;

    double[] best;

    /** Create a new individual with the specified number of weights */
    Individual(int size, int initType) {
        x = new double[size];
        v = new double[size];

        switch (initType) {
            case INIT_RANDOM_NEGATIVE_ONE_TO_ONE:
                for (int i = 0; i < size; i++) {
                    x[i] = (Math.random() - 0.5) * 2; // [-1, 1)
                    v[i] = (Math.random() - 0.5) * 2; // [-1, 1)
                }
                break;
        }
    }
}
