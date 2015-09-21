package com.jifalops.toolbox;

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

    /** Both points must have the same number of elements */
    public static double linearDistance(float[] p1, float[] p2) {
        double d = 0, tmp;
        for (int i = 0; i < p1.length; ++i) {
            tmp = p1[i] - p2[i];
            d += tmp * tmp;
        }
        return Math.sqrt(d);
    }

    /** Sums each element of the arrays, up to v1.length elements. */
    public static float[] vectorSum(float[] v1, float[] v2) {
        float[] sum = new float[v1.length];
        for (int i = 0; i < v1.length; ++i) {
            sum[i] = v1[i] + v2[i];
        }
        return sum;
    }

    /** Subtracts each element of the arrays, up to v1.length elements. */
    public static float[] vectorDiff(float[] v1, float[] v2) {
        float[] diff = new float[v1.length];
        for (int i = 0; i < v1.length; ++i) {
            diff[i] = v1[i] - v2[i];
        }
        return diff;
    }

    /** @return true if v1 <= v2 in every element. */
    public static boolean isLessThanOrEqual(float[] v1, float[] v2) {
        for (int i = 0; i < v1.length; ++i) {
            if (v1[i] > v2[i]) return false;
        }
        return true;
    }
}
