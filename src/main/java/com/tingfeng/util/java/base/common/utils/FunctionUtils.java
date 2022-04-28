package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.bean.tuple.Tuple2;
import com.tingfeng.util.java.base.common.inter.returnfunction.Function2;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
