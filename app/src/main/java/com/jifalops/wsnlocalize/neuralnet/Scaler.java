package com.jifalops.wsnlocalize.neuralnet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class Scaler {
    public static class ScaledLimits {
        int inputMin = -1;
        int inputMax = 1;
        int outputMin = 0;
        int outputMax = 1;
    }

    /** The min and max value of each column before scaling. */
    public static class UnscaledLimits {
        final double[] min, max;
        public UnscaledLimits(double[] min, double[] max) {
            this.min = min;
            this.max = max;
        }
        public UnscaledLimits(double[][] data) {
            int rows = data.length;
            int cols = data[0].length;
            min = new double[cols];
            max = new double[cols];
            for (int col = 0; col < cols; ++col) {
                min[col] = Double.MAX_VALUE;
                max[col] = Double.MIN_VALUE;
                for (int row = 0; row < rows; ++row) {
                    if (data[row][col] < min[col]) min[col] = data[row][col];
                    if (data[row][col] > max[col]) max[col] = data[row][col];
                }
            }
        }
    }

    final ScaledLimits scaledLimits;
    final UnscaledLimits unscaledLimits;
    final int numInputs;

    public Scaler(UnscaledLimits unscaled, ScaledLimits scaled, int numInputs) {
        this.unscaledLimits = unscaled;
        this.scaledLimits = scaled;
        this.numInputs = numInputs;
    }

    public double[][] scale(double[][] data) {
        int rows = data.length;
        int cols = data[0].length;
        double[][] scaled = new double[rows][cols];
        double unscaledDiff, scaledMin, scaledDiff,
                inDiff = scaledLimits.inputMax - scaledLimits.inputMin,
                outDiff = scaledLimits.outputMax - scaledLimits.outputMin;
        for (int i = 0; i < cols; i++) {
            unscaledDiff = unscaledLimits.max[i] - unscaledLimits.min[i];
            if (i < numInputs) {
                scaledMin = scaledLimits.inputMin;
                scaledDiff = inDiff;
            } else {
                scaledMin = scaledLimits.outputMin;
                scaledDiff = outDiff;
            }
            for (int j = 0; j < rows; j++) {
                scaled[j][i] = scaledMin + (data[j][i] - unscaledLimits.min[i]) * scaledDiff / unscaledDiff;
            }
        }
        return scaled;
    }

    public double[] unscale(double[] outputs) {
        int len = outputs.length;
        double[] unscaled = new double[len];
        for (int i = 0; i < len; ++i) {
            unscaled[i] = (outputs[i] - scaledLimits.outputMin) *
                    (unscaledLimits.max[numInputs + i] - unscaledLimits.min[numInputs + i]) /
                    (scaledLimits.outputMax - scaledLimits.outputMin) +
                    unscaledLimits.min[numInputs + i];
        }
        return unscaled;
    }

    public double[][] randomize(double[][] data) {
        List<double[]> rand = Arrays.asList(data);
        Collections.shuffle(rand);
        return rand.toArray(new double[data.length][data[0].length]);
    }

    public double[][] scaleAndRandomize(double[][] data) {
        return randomize(scale(data));
    }
}
