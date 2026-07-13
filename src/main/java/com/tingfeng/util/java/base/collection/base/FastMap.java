package com.tingfeng.util.java.base.collection.base;

import java.util.HashMap;

/**
 * 快捷 Map 实现
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class FastMap<K,V> extends HashMap<K,V> {
    private static final long serialVersionUID = -9160627831473020010L;

    public FastMap<K,V> add(K key, V value) {
        super.put(key, value);
        return this;
    }

    public static <K,V> FastMap instance(K key, V value) {
        FastMap<K,V> fastMap = new FastMap<>();
        fastMap.add(key, value);
        return fastMap;
    }

    public static <K,V> FastMap instance() {
        FastMap<K,V> fastMap = new FastMap<>();
        return fastMap;
    }

}
