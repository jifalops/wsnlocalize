package com.jifalops.wsnlocalize.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class WindowScaler {
    public static final double RSSI_MIN = -110;
    public static final double RSSI_MAX = -5;
    public static final double ELAPSED_MAX = 120_000;

    // When scaled
    private static final double inputMin = -1, inputMax = 1, outputMin = 0, outputMax = 1;

    // The minimum values possible in each column
    private static final double[] min = new double[] {
            RSSI_MIN, // min rssi min
            RSSI_MIN, // min rssi max
            0,        // min rssi range
            RSSI_MIN, // min rssi mean
            RSSI_MIN, // min rssi median
            0,        // min rssi stdDev
            1,        // min elapsed min
            1,        // min elapsed max
            1,        // min elapsed range
            1,        // min elapsed mean
            1,        // min elapsed median
            1,        // min elapsed stdDev
    };

    // The maximum values possible in each column
    private static final double[] max = new double[] {
            RSSI_MAX,               // max rssi min
            RSSI_MAX,               // max rssi max
            RSSI_MAX - RSSI_MIN,    // max rssi range
            RSSI_MAX,               // max rssi mean
            RSSI_MAX,               // max rssi median
            RSSI_MAX - RSSI_MIN,    // max rssi stdDev
            ELAPSED_MAX,            // max elapsed min
            ELAPSED_MAX,            // max elapsed max
            ELAPSED_MAX,            // max elapsed range
            ELAPSED_MAX,            // max elapsed mean
            ELAPSED_MAX,            // max elapsed median
            ELAPSED_MAX,            // max elapsed stdDev
    };

    private static final int numInputs = min.length;

    public static double[][] scale(double[][] data) {
        int rows = data.length;
        int cols = data[0].length;
        double[][] scaled = new double[rows][cols];
        double unscaledDiff, scaledMin, scaledDiff,
                inDiff = inputMax - inputMin,
                outDiff = outputMax - outputMin;
        for (int i = 0; i < cols; i++) {
            unscaledDiff = max[i] - min[i];
            if (i < numInputs) {
                scaledMin = inputMin;
                scaledDiff = inDiff;
            } else {
                scaledMin = outputMin;
                scaledDiff = outDiff;
            }
            for (int j = 0; j < rows; j++) {
                scaled[j][i] = scaledMin + (data[j][i] - min[i]) * scaledDiff / unscaledDiff;
            }
        }
        return scaled;
    }

    public static double[] unscale(double[] outputs) {
        int len = outputs.length;
        double[] unscaled = new double[len];
        for (int i = 0; i < len; ++i) {
            unscaled[i] = (outputs[i] - outputMin) *
                    (max[numInputs + i] - min[numInputs + i]) /
                    (outputMax - outputMin) +
                    min[numInputs + i];
        }
        return unscaled;
    }

    public static double[][] randomize(double[][] data) {
        List<double[]> rand = Arrays.asList(data);
        Collections.shuffle(rand);
        return rand.toArray(new double[data.length][data[0].length]);
    }

    public static double[][] scaleAndRandomize(double[][] data) {
        return randomize(scale(data));
    }
}
