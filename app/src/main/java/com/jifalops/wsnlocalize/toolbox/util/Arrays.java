package com.jifalops.wsnlocalize.toolbox.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Arrays {
    private Arrays() {}

    public static double[] concat(double[] a, double[] b) {
        int aLen = a.length;
        int bLen = b.length;
        double[] c = new double[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public static double[][] concat(double[][] a, double[][] b) {
        int aLen = a.length;
        int bLen = b.length;
        double[][] c = new double[aLen+bLen][a[0].length];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public static int[] toPrimitive(Integer[] ints) {
        int len = ints.length;
        int[] a = new int[len];
        for (int i = 0; i < len; ++i) {
            a[i] = ints[i];
        }
        return a;
    }

    public static void shiftLeft(double[] a, int shifts) {
        System.arraycopy(a, shifts, a, 0, a.length-shifts);
    }

    public static void shiftRight(double[] a, int shifts) {
        System.arraycopy(a, 0, a, shifts, a.length-shifts);
    }

    /** trade rows and columns; a 90 degree shift */
    public static double[][] transpose(double[][] m) {
        int rows = m.length;
        int cols = m[0].length;
        double[][] transposed = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = m[i][j];
            }
        }
        return transposed;
    }

    public static double[] trim(double[] array, double min, double max) {
        List<Double> trimmed = new ArrayList<>();
        for (double d : array) {
            if (d >= min && d <= max) trimmed.add(d);
        }
        return Lists.toPrimitive(trimmed);
    }
    public static double[][] rowTrim(double[][] array, double min, double max) {
        int len = array.length;
        double[][] trimmed = new double[len][];
        for (int i = 0; i < len; ++i) {
            trimmed[i] = trim(array[i], min, max);
        }
        return trimmed;
    }
    public static double[][] colTrim(double[][] array, double min, double max) {
        return rowTrim(transpose(array), min, max);
    }
}
