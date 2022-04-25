package com.tingfeng.util.java.base.common.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author huitoukest
 * 一些常用的函数，以及相关方法；
 * 通常返回值为一个函数
 */
public class FunctionUtils {

    /**
     * eg: list.stream().filter(distinctByKey(it -> getKey(it));
     * @param keyExtractor 将目标值转为一个判断重复的key
     * @param <T>
     * @return 一个Predicate函数，此函数根据输入值进行去重,当输入的值有重复的情况时，返回false，否则返回true
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }


}
