package com.tingfeng.util.java.base.common.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 封装和Map相关的处理工具
 * @author huitoukest
 */
public class MapUtils {
    /**
     * 返回一个新的HashMap ; 用默认的kes 和 values  作为Map的键值对填充值.
     * @param keys
     * @param values
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K,V> HashMap<K,V> newHashMap(List<K> keys,List<V> values){
        int size = keys.size();
        assert size == values.size();
        HashMap<K,V> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(keys.get(i),values.get(i));
        }
        return map;
    }

    /**
     * 返回一个新的HashMap ; 用默认的kes 和 values  作为Map的键值对填充值.
     * @param keys
     * @param values
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K,V> HashMap<K,V> newHashMap(K[] keys,V[] values){
        return newHashMap(Arrays.asList(keys),Arrays.asList(values));
    }

    /**
     * 返回一个新的HashMap ; 用默认的kes 和 value  作为Map的键值对填充值.
     * @param keys
     * @param value
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K,V> HashMap<K,V> newHashMap(List<K> keys,V value){
        int size = keys.size();
        HashMap<K,V> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(keys.get(i),value);
        }
        return map;
    }

    /**
     * 返回一个新的HashMap ; 用默认的kes 和 value  作为Map的键值对填充值.
     * @param keys
     * @param value
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K,V> HashMap<K,V> newHashMap(K[] keys,V value){
        return newHashMap(Arrays.asList(keys),value);
    }

    /**
     * 返回一个新的HashMap ; 用默认的ke 和 value 作为Map的键值对填充值.
     * @param key
     * @param value
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K,V> HashMap<K,V> newHashMap(K key,V value){
        return newHashMap(Arrays.asList(key),value);
    }


}
