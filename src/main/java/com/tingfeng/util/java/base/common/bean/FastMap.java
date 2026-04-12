package com.tingfeng.util.java.base.common.bean;

import java.util.HashMap;

/**
 * 一个快捷的Map
 * @param <K>
 * @param <V>
 */
public class FastMap<K,V> extends HashMap<K,V> {
    private static final long serialVersionUID = -9160627831473020010L;

    public FastMap<K,V> add(K key, V value) {
        super.put(key, value);
        return this;
    }

    public static <K,V> FastMap instance(K key, V value) {
        FastMap<K,V> fastMap = new FastMap<>();
        return fastMap;
    }

    public static <K,V> FastMap instance() {
        FastMap<K,V> fastMap = new FastMap<>();
        return fastMap;
    }

}
