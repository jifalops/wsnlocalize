package com.jifalops.wsnlocalize.neuralnet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class Util {
    private Util() {}

    static double[][] scale(double[][] data, SampleMetrics metrics) {
        return scale(data, metrics, -1, 1, 0, 1);
    }
    static double[][] scale(double[][] data, SampleMetrics metrics, double inputMin,
                            double inputMax, double outputMin, double outputMax) {
        int samples = data.length;
        int cols = data[0].length;
        double[][] scaled = new double[samples][cols];
        double min, max, diff,
                inDiff = inputMax - inputMin,
                outDiff = outputMax - outputMin;
        for (int i = 0; i < metrics.inputs; i++) {
            min = Double.MAX_VALUE;
            max = Double.MIN_VALUE;
            for (int j = 0; j < samples; j++) {
                if (data[j][i] < min) min = data[j][i];
                if (data[j][i] > max) max = data[j][i];
            }
            diff = max - min;
            for (int j = 0; j < samples; j++) {
                scaled[j][i] = inputMin + (data[j][i] - min) * inDiff / diff;
            }
        }

        for (int i = metrics.inputs; i < cols; i++) {
            min = Double.MAX_VALUE;
            max = Double.MIN_VALUE;
            for (int j = 0; j < samples; j++) {
                if (data[j][i] < min) min = data[j][i];
                if (data[j][i] > max) max = data[j][i];
            }
            diff = max - min;
            for (int j = 0; j < samples; j++) {
                scaled[j][i] = outputMin + (data[j][i] - min) * outDiff / diff;
            }
        }

        return scaled;
    }


    static double[][] randomize(double[][] data) {
        List<double[]> rand = Arrays.asList(data);
        Collections.shuffle(rand);
        return rand.toArray(new double[data.length][data[0].length]);
    }
}
