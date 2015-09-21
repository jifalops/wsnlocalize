package com.jifalops.toolbox.util;

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

    public static double[] toPrimitive(List<Double> list) {
        int len = list.size();
        double[] a = new double[len];
        for (int i = 0; i < len; ++i) {
            a[i] = list.get(i);
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
}
