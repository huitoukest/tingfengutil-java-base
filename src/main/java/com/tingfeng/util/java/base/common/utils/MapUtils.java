package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.bean.tuple.Tuple2;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    /**
     * Map 的KV反转
     * @param originMap
     * @param mergeFunction 值相同时的处理策略
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K,V> Map<V,K> reverse(Map<K,V> originMap, BinaryOperator<K> mergeFunction){
        return originMap.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getValue(), entry -> entry.getKey(), mergeFunction));
    }

    /**
     * Map的KV反转, 其中V是一个Collection类型.
     * @param originMap
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K,V> Map<V, List<K>> reverses(Map<K,Collection<V>> originMap){
        return originMap.entrySet()
                .stream()
                .filter(it -> null != it.getValue())
                .flatMap(it -> it.getValue().stream().map(vs -> new Tuple2(it.getKey(),vs)))
                .collect(Collectors.groupingBy(it -> (V)it.get_2(),Collectors.mapping(it -> (K)it.get_1(),Collectors.toList())));
    }

    /**
     * 计算Map KV值 , 支持类型变化; 默认使用Object的KV类型计算,最后强转类
     * @param originMap
     * @param kMapperF if null , not use , keep origin type ;
     * @param vMapperF if null , not use , keep origin type ;
     * @param <K_R> 返回的K类型
     * @param <V_R> 返回的V类型
     * @param <K_O> 输入的原始的K类型
     * @param <V_O> 输入的原始的V类型
     * @return
     */
    public static <K_R,V_R,K_O,V_O> HashMap<K_R,V_R> compute(Map<K_O,V_O> originMap, Function<K_O,K_R> kMapperF,Function<V_O,V_R> vMapperF){
        HashMap<Object, Object> map = new HashMap(originMap.size());
        originMap.forEach((k,v) -> {
            map.put(Optional.ofNullable(kMapperF).map(it -> (Object) it.apply(k)).orElse(k)
                    ,Optional.ofNullable(vMapperF).map(it -> (Object)it.apply(v)).orElse(v));
        });
        return (HashMap<K_R, V_R>) map;
    }

    /**
     * 获取值
     * @param map
     * @param k map 的key
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K,V> V getValue(Map<K,V> map, K k){
        return map.get(k);
    }

    /**
     * 获取值
     * @param map
     * @param k map 的key
     * @param value 默认值
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K,V> V getOrDefault(Map<K,V> map, K k, V value){
        return map.getOrDefault(k,value);
    }

    /**
     * 获取值
     * @param map
     * @param k
     * @param supplier 默认值生产者
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K,V> V getOrDefault(Map<K,V> map, K k, Supplier<V> supplier){
        return map.getOrDefault(k,supplier.get());
    }

}
