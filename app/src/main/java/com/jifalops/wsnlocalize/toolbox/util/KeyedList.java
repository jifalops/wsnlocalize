package com.jifalops.wsnlocalize.toolbox.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
public class KeyedList<K, V> extends HashMap<K, List<V>> {
    public interface KeyLookup<K, V> {
        K getKey(V item);
    }

    public KeyedList(List<V> list, KeyLookup<K, V> keyLookup) {
        List<V> tmp;
        K key;
        for (V v : list) {
            key = keyLookup.getKey(v);
            tmp = get(key);
            if (tmp == null) {
                tmp = new ArrayList<>();
                put(key, tmp);
            }
            tmp.add(v);
        }
    }
}
