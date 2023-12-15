package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.bean.UnionKey;
import com.tingfeng.util.java.base.common.inter.IEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author huitoukest
 */
public class EnumUtils {
    /**
     *  枚举的值的缓存
     *  Map[UnionKey[枚举类,方法类],[value,枚举类的实例]]
     */
    private static final  Map<UnionKey,Map<?,? extends  Enum>> ENUM_VALUE_CACHE_MAP = new java.util.HashMap<>();

    /**
     * 等同于枚举的valueOf方法
     * @param enumClass 枚举类
     * @param value 值
     * @param supplyValue  通过枚举查找key值
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return
     */
    public static <V,E extends Enum<?>> E getEnum(Class<E> enumClass, V value, Function<E,V> supplyValue){
        return getEnum(enumClass,value,supplyValue,true);
    }

    /**
     * 等同于枚举的valueOf方法
     * @param enumClass 枚举类
     * @param value 值
     * @param supplyValue  通过枚举查找key值
     * @param useCache 使用缓存,使用缓存时（第一次仍会全量缓存，之后则一直使用缓存）
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return
     */
    public static <V,E extends Enum<?>> E getEnum(Class<E> enumClass, V value, Function<E,V> supplyValue, boolean useCache) {
        E[] es = enumClass.getEnumConstants();
        if(!useCache){
            return Arrays.asList(es).stream().filter(it -> value.equals(supplyValue.apply(it))).findAny().orElse(null);
        }else {
            Map<?, E> cacheData = (Map<?, E>) ENUM_VALUE_CACHE_MAP.get(new UnionKey(enumClass, supplyValue));
            if(cacheData == null){
                synchronized (ENUM_VALUE_CACHE_MAP){
                    cacheData = (Map<?, E>) ENUM_VALUE_CACHE_MAP.get(enumClass);
                    if(cacheData == null){
                        cacheData = Arrays.asList(es).stream().collect(Collectors.toMap(it -> supplyValue.apply(it),it -> it));
                        ENUM_VALUE_CACHE_MAP.put(new UnionKey(enumClass, supplyValue),cacheData);
                    }
                }
            }
            return cacheData.get(value);
        }
    }

    /**
     *
     * @param enumClass 枚举类
     * @param value 值
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return
     */
    public static <V,E extends Enum<?> & IEnum<V>> E getEnumByValue(Class<E> enumClass, V value){
        return getEnumByValue(enumClass,value,true);
    }

    /**
     *
     * @param enumClass 枚举类
     * @param value 值
     * @param useCache 是否使用缓存,使用缓存时（第一次仍会全量缓存，之后则一直使用缓存）
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return
     */
    public static <V,E extends Enum<?> & IEnum<V>> E getEnumByValue(Class<E> enumClass, V value,boolean useCache) {
        return getEnum(enumClass,value,it -> it.getValue(),useCache);
    }
}