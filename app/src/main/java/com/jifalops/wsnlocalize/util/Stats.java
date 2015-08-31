package com.jifalops.wsnlocalize.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Stats {
    private Stats() {}

    public static double sum(double[] array) {
        double sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum;
    }

    public static double mean(double[] array) {
        return sum(array) / array.length;
    }

    public static double variance(double[] array) {
        return variance(array, false);
    }
    public static double variance(double[] array, boolean population) {
        double mean = mean(array);
        double[] var = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            var[i] = Math.pow(array[i] - mean, 2);
        }
        if (population) {
            return mean(var);
        } else {
            return sum(var) / (var.length - 1);
        }
    }

    public static double stdDev(double[] array) {
        return stdDev(array, false);
    }
    public static double stdDev(double[] array, boolean population) {
        return Math.sqrt(variance(array, population));
    }

    public static double median(double[] array) {
        double[] sorted = array.clone();
        Arrays.sort(sorted);
        int len = sorted.length;
        int mid = len / 2; // Upper-mid index if length is even.
        double median = sorted[mid];
        if (len % 2 == 0) {
            median = (median + sorted[mid - 1]) / 2;
        }
        return median;
    }

    public static double[] mode(double[] array) {
        double[] modes;
        List<Double> modeList = new ArrayList<>();
        Map<Double, Integer> counts = new HashMap<>();
        int count, maxCount = 0;
        // Count how many times each number occurs.
        for (double n : array) {
            count = 1;
            if (counts.containsKey(n)) {
                count = counts.get(n) + 1;
            }
            counts.put(n, count);
            if (count > maxCount) {
                maxCount = count;
            }
        }
        // Find all numbers that occurred maxCount times.
        for (Map.Entry<Double, Integer> e : counts.entrySet()) {
            if (e.getValue() == maxCount) {
                modeList.add(e.getKey());
            }
        }
        // Unbox the List<Double> to a double[].
        int len = modeList.size();
        modes = new double[len];
        for (int i = 0; i < len; ++i) {
            modes[i] = modeList.get(i);
        }
        return modes;
    }

    public static double min(double[] array) {
        double min = Double.MAX_VALUE;
        for (double n : array) {
            if (n < min) {
                min = n;
            }
        }
        return min;
    }

    public static double max(double[] array) {
        double max = Double.MIN_VALUE;
        for (double n : array) {
            if (n > max) {
                max = n;
            }
        }
        return max;
    }
}
