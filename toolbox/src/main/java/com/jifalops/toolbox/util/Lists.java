package com.jifalops.toolbox.util;

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

}
