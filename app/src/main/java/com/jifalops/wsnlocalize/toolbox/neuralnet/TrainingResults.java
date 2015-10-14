package com.jifalops.wsnlocalize.toolbox.neuralnet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class TrainingResults implements Comparable<TrainingResults> {
    public final double[] weights;
    public final double error, mean, stddev;
    public final int numGenerations;
    public final long elapsedTime;

    public TrainingResults(double[] weights, double error, double mean, double stddev,
                    int numGenerations, long elapsedTime) {
        this.weights = weights;
        this.error = error;
        this.mean = mean;
        this.stddev = stddev;
        this.numGenerations = numGenerations;
        this.elapsedTime = elapsedTime;
    }

    public TrainingResults(double[] info) {
        error = info[0];
        mean = info[1];
        stddev = info[2];
        numGenerations = (int) info[3];
        elapsedTime = (long) info[4];
        weights = Arrays.copyOfRange(info, 5, info.length);
    }

    public double[] toArray() {
        double[] a = new double[weights.length + 5];
        a[0] = error;
        a[1] = mean;
        a[2] = stddev;
        a[3] = numGenerations;
        a[4] = elapsedTime;
        System.arraycopy(weights, 0, a, 5, weights.length);
        return a;
    }

    public static List<TrainingResults> fromDoubleList(List<double[]> list) {
        List<TrainingResults> results = new ArrayList<>(list.size());
        for (double[] a : list) {
            results.add(new TrainingResults(a));
        }
        return results;
    }

    public TrainingResults(String csv) {
        String[] parts = csv.split(",");
        error = Double.valueOf(parts[0]);
        mean = Double.valueOf(parts[1]);
        stddev = Double.valueOf(parts[2]);
        numGenerations = Integer.valueOf(parts[3]);
        elapsedTime = Long.valueOf(parts[4]);
        int len = parts.length - 5;
        weights = new double[len];
        for (int i = 0; i < len; ++i) {
            weights[i] = Double.valueOf(parts[i+5]);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder((weights.length + 5) * 2 - 1);
        sb.append(error).append(',').append(mean).append(',')
                .append(stddev).append(',').append(numGenerations)
                .append(',').append(elapsedTime);
        for (int i = 0; i < weights.length; ++i) {
            sb.append(',').append(weights[i]);
        }
       return sb.toString();
    }

    @Override
    public int compareTo(TrainingResults another) {
//        if (samples < another.samples) return -1;
//        if (samples > another.samples) return 1;
        if (error < another.error) return -1;
        if (error > another.error) return 1;
        return 0;
    }
}
