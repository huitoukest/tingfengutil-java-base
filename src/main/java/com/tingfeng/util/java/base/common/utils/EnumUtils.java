package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.inter.IEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class EnumUtils {
    /**
     *  实现了IEnum接口的枚举的值的工具
     */
    private static final Map<Class<? extends Enum>,Map<?,? extends  Enum>> ENUM_VALUE_CACHE_MAP = new java.util.HashMap<>();
    /**
     * 普通的枚举的values方法
     */
    private static final Map<Class<? extends Enum>,Map<?,? extends  Enum>> ENUM__CACHE_MAP = new java.util.HashMap<>();

    /**
     * 等同于枚举的valueOf方法
     * @param e
     * @param item
     * @param useCache
     * @param <E>
     * @return
     */
    /*public static <E extends Enum<?>> E valuesOf(Enum e,String item,boolean useCache) {
       if(useCache){

       }else{
           e.
       }
    }*/

    /**
     * <p>
     * 值映射为枚举
     * </p>
     *
     * @param enumClass 枚举类
     * @param value     枚举值
     * @param <E>       对应枚举
     * @param useCache     是否使用缓存
     * @return
     */
    public static <T,E extends Enum<?> & IEnum<T>> E getEnumByValue(Class<E> enumClass, T value,boolean useCache) {
        E[] es = enumClass.getEnumConstants();
        if(!useCache){
            for (E e : es) {
                if ((value instanceof String && e.getValue().equals(value))
                        || e.getValue() == value) {
                    return e;
                }
            }
        }else {
            Map<?,E> cacheData = (Map<?, E>) ENUM_VALUE_CACHE_MAP.get(enumClass);
            if(cacheData == null){
                synchronized (ENUM_VALUE_CACHE_MAP){
                    cacheData = (Map<?, E>) ENUM_VALUE_CACHE_MAP.get(enumClass);
                    if(cacheData == null){
                        cacheData = Arrays.asList(es).stream().collect(Collectors.toMap(it -> it.getValue(),it -> it));
                        ENUM_VALUE_CACHE_MAP.put(enumClass,cacheData);
                    }else{
                        return cacheData.get(value);
                    }
                }
            }
        }
        return null;
    }
}