package com.jifalops.wsnlocalize.toolbox.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Lists {
    private Lists() {}

    public static List<String> toStrings(List<?> objects) {
        List<String> strings = new ArrayList<>();
        for (Object obj : objects) {
            strings.add(obj.toString());
        }
        return strings;
    }

    public static double[] toPrimitive(List<Double> list) {
        int len = list.size();
        double[] a = new double[len];
        for (int i = 0; i < len; ++i) {
            a[i] = list.get(i);
        }
        return a;
    }

//    public static int[] toPrimitive(List<Integer> list) {
//        int len = list.size();
//        int[] a = new int[len];
//        for (int i = 0; i < len; ++i) {
//            a[i] = list.get(i);
//        }
//        return a;
//    }

    public static double[][] toPrimitive2D(List<double[]> list) {
        int len = list.size();
        double[][] a = new double[len][];
        for (int i = 0; i < len; ++i) {
            a[i] = list.get(i);
        }
        return a;
    }
}
