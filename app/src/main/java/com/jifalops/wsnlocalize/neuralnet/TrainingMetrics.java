package com.jifalops.wsnlocalize.neuralnet;

/**
 *
 */
public class TrainingMetrics {
    double[] min, max;
    TrainingMetrics(double[][] samples) {
        int rows = samples.length;
        int cols = samples[0].length;
        min = new double[cols];
        max = new double[cols];
        for (int col = 0; col < cols; ++col) {
            min[col] = Double.MAX_VALUE;
            max[col] = Double.MIN_VALUE;
            for (int row = 0; row < rows; ++row) {
                if (samples[row][col] < min[col]) min[col] = samples[row][col];
                if (samples[row][col] > max[col]) max[col] = samples[row][col];
            }
        }
    }
}
