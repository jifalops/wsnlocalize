package com.jifalops.wsnlocalize.toolbox.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import java.util.Arrays;

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
    public static double[] rowSums(double[][] array) {
        int len = array.length;
        double[] sums = new double[len];
        for (int i = 0; i < len; ++i) {
            sums[i] = sum(array[i]);
        }
        return sums;
    }
    public static double[] colSums(double[][] array) {
        return rowSums(Arrays.transpose(array));
    }

    public static double mean(double[] array) {
        return sum(array) / array.length;
    }
    public static double[] rowMeans(double[][] array) {
        int len = array.length;
        double[] means = new double[len];
        for (int i = 0; i < len; ++i) {
            means[i] = mean(array[i]);
        }
        return means;
    }
    public static double[] colMeans(double[][] array) {
        return rowMeans(Arrays.transpose(array));
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
    public static double[] rowVariances(double[][] array) {
        return rowVariances(array, false);
    }
    public static double[] rowVariances(double[][] array, boolean population) {
        int len = array.length;
        double[] vars = new double[len];
        for (int i = 0; i < len; ++i) {
            vars[i] = variance(array[i], population);
        }
        return vars;
    }
    public static double[] colVariances(double[][] array) {
        return colVariances(array, false);
    }
    public static double[] colVariances(double[][] array, boolean population) {
        return rowVariances(Arrays.transpose(array), population);
    }

    public static double stdDev(double[] array) {
        return stdDev(array, false);
    }
    public static double stdDev(double[] array, boolean population) {
        return Math.sqrt(variance(array, population));
    }
    public static double[] rowStdDev(double[][] array) {
        return rowStdDev(array, false);
    }
    public static double[] rowStdDev(double[][] array, boolean population) {
        int len = array.length;
        double[] stds = new double[len];
        for (int i = 0; i < len; ++i) {
            stds[i] = stdDev(array[i], population);
        }
        return stds;
    }
    public static double[] colStdDev(double[][] array) {
        return colStdDev(array, false);
    }
    public static double[] colStdDev(double[][] array, boolean population) {
        return rowStdDev(Arrays.transpose(array), population);
    }

    public static double median(double[] array) {
        double[] sorted = array.clone();
        java.util.Arrays.sort(sorted);
        int len = sorted.length;
        int mid = len / 2; // Upper-mid index if length is even.
        double median = sorted[mid];
        if (len % 2 == 0) {
            median = (median + sorted[mid - 1]) / 2;
        }
        return median;
    }
    public static double[] rowMedians(double[][] array) {
        int len = array.length;
        double[] meds = new double[len];
        for (int i = 0; i < len; ++i) {
            meds[i] = median(array[i]);
        }
        return meds;
    }
    public static double[] colMedians(double[][] array) {
        return rowMedians(Arrays.transpose(array));
    }

    public static double[] modes(double[] array) {
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
    public static double[][] rowModes(double[][] array) {
        int len = array.length;
        double[][] modes = new double[len][];
        for (int i = 0; i < len; ++i) {
            modes[i] = modes(array[i]);
        }
        return modes;
    }
    public static double[][] colModes(double[][] array) {
        return rowModes(Arrays.transpose(array));
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
    public static double[] rowMins(double[][] array) {
        int len = array.length;
        double[] mins = new double[len];
        for (int i = 0; i < len; ++i) {
            mins[i] = min(array[i]);
        }
        return mins;
    }
    public static double[] colMins(double[][] array) {
        return rowMins(Arrays.transpose(array));
    }

    public static double max(double[] array) {
        double max = -Double.MAX_VALUE;
        for (double n : array) {
            if (n > max) {
                max = n;
            }
        }
        return max;
    }
    public static double[] rowMaxes(double[][] array) {
        int len = array.length;
        double[] maxes = new double[len];
        for (int i = 0; i < len; ++i) {
            maxes[i] = max(array[i]);
        }
        return maxes;
    }
    public static double[] colMaxes(double[][] array) {
        return rowMaxes(Arrays.transpose(array));
    }
}
