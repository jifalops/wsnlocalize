package com.jifalops.wsnlocalize.toolbox.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Lists {
    private Lists() {}

    public static List<String> toString(List<?> objects) {
        List<String> strings = new ArrayList<>();
        for (Object obj : objects) {
            strings.add(obj.toString());
        }
        return strings;
    }

    public static int[] toPrimitive(Integer[] ints) {
        int len = ints.length;
        int[] a = new int[len];
        for (int i = 0; i < len; ++i) {
            a[i] = ints[i];
        }
        return a;
    }
}
