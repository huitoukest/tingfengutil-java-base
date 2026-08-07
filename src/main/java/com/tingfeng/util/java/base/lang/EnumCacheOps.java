package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.lang.base.UnionKey;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 枚举缓存操作。
 * 包级私有，不对外暴露。通过 {@link EnumUtils} 对外提供统一 API。
 * 缓存结构：Map[UnionKey[枚举类,值函数],[value,枚举实例列表]]，使用 ConcurrentHashMap 保证线程安全。
 * 值函数返回的 value 允许重复，同一 value 对应的全部枚举实例以列表形式保留。
 */
final class EnumCacheOps {

    /**
     * 枚举的值的缓存
     * Map[UnionKey[枚举类,值函数],[value,枚举类的实例列表]]
     */
    private static final Map<UnionKey, Map<Object, List<? extends Enum<?>>>> ENUM_VALUE_CACHE_MAP = new ConcurrentHashMap<>();

    private EnumCacheOps() {
    }

    /**
     * 从缓存获取指定枚举值对应的实例列表，缓存不存在时构建
     * 列表元素顺序为枚举声明顺序，未命中时返回 null
     * @param enumClass 枚举类
     * @param value 值
     * @param supplyValue 通过枚举查找key值的函数
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 匹配的枚举实例列表，未命中返回 null
     */
    static <V, E extends Enum<?>> List<E> getCacheList(Class<E> enumClass, V value, Function<E, V> supplyValue) {
        UnionKey key = new UnionKey(enumClass, supplyValue);
        Map<Object, List<? extends Enum<?>>> cacheData = ENUM_VALUE_CACHE_MAP.get(key);
        if (cacheData == null) {
            synchronized (ENUM_VALUE_CACHE_MAP) {
                cacheData = ENUM_VALUE_CACHE_MAP.get(key);
                if (cacheData == null) {
                    cacheData = buildCache(enumClass, supplyValue);
                    ENUM_VALUE_CACHE_MAP.put(key, cacheData);
                }
            }
        }
        return (List<E>) cacheData.get(value);
    }

    /**
     * 构建指定枚举类与值函数的完整缓存
     * 使用 groupingBy 聚合，value 重复时全部枚举实例保留在列表中
     * @param enumClass 枚举类
     * @param supplyValue 通过枚举查找key值的函数
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return value 到枚举实例列表的映射
     */
    @SuppressWarnings("unchecked")
    private static <V, E extends Enum<?>> Map<Object, List<? extends Enum<?>>> buildCache(Class<E> enumClass, Function<E, V> supplyValue) {
        return (Map<Object, List<? extends Enum<?>>>) (Map<?, ?>) Arrays.stream(enumClass.getEnumConstants())
                .collect(Collectors.groupingBy(supplyValue, Collectors.toList()));
    }

    /**
     * 清除指定枚举类与值函数对应的缓存
     * @param enumClass 枚举类
     * @param supplyValue 值函数
     * @param <V> 值类型
     * @param <E> 枚举类型
     */
    static <V, E extends Enum<?>> void clearCache(Class<E> enumClass, Function<E, V> supplyValue) {
        UnionKey key = new UnionKey(enumClass, supplyValue);
        ENUM_VALUE_CACHE_MAP.remove(key);
    }

    /**
     * 清除指定枚举类的所有缓存
     * @param enumClass 枚举类
     * @param <E> 枚举类型
     */
    static <E extends Enum<?>> void clearAllCache(Class<E> enumClass) {
        ENUM_VALUE_CACHE_MAP.keySet().removeIf(key -> enumClass.equals(key.getKey(0)));
    }

    /**
     * 清除所有枚举缓存
     */
    static void clearAllCache() {
        ENUM_VALUE_CACHE_MAP.clear();
    }
}
