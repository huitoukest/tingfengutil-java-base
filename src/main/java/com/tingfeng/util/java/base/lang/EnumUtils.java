package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.lang.base.UnionKey;
import com.tingfeng.util.java.base.lang.base.IEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 枚举工具类
 * <p>
 * 提供枚举的常用操作方法，包括：
 * <ul>
 *   <li>通过值获取枚举实例</li>
 *   <li>获取枚举的所有值</li>
 *   <li>检查值是否在枚举中存在</li>
 *   <li>获取枚举的名称和序号</li>
 *   <li>根据名称获取枚举实例</li>
 * </ul>
 * </p>
 * <p>
 * 支持缓存功能，提高频繁查询的性能
 * </p>
 * @author huitoukest
 * @since 0.2.6
 */
public class EnumUtils {
    
    /**
     * 枚举的值的缓存
     * Map[UnionKey[枚举类,方法类],[value,枚举类的实例]]
     * 使用ConcurrentHashMap保证线程安全
     */
    private static final Map<UnionKey, Map<?, ? extends Enum>> ENUM_VALUE_CACHE_MAP = new ConcurrentHashMap<>();

    /**
     * 等同于枚举的valueOf方法，使用缓存
     * <p>
     * 通过自定义函数获取枚举值，支持缓存以提高性能
     * </p>
     * @param enumClass 枚举类
     * @param value 值
     * @param supplyValue 通过枚举查找key值的函数
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 匹配的枚举实例，如果未找到则返回null
     */
    public static <V, E extends Enum<?>> E getEnum(Class<E> enumClass, V value, Function<E, V> supplyValue) {
        return getEnum(enumClass, value, supplyValue, true);
    }

    /**
     * 等同于枚举的valueOf方法
     * <p>
     * 通过自定义函数获取枚举值，可选择是否使用缓存
     * </p>
     * @param enumClass 枚举类
     * @param value 值
     * @param supplyValue 通过枚举查找key值的函数
     * @param useCache 是否使用缓存，使用缓存时（第一次仍会全量缓存，之后则一直使用缓存）
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 匹配的枚举实例，如果未找到则返回null
     */
    public static <V, E extends Enum<?>> E getEnum(Class<E> enumClass, V value, Function<E, V> supplyValue, boolean useCache) {
        if (!useCache) {
            return Arrays.stream(enumClass.getEnumConstants())
                    .filter(it -> value.equals(supplyValue.apply(it)))
                    .findAny()
                    .orElse(null);
        } else {
            UnionKey key = new UnionKey(enumClass, supplyValue);
            Map<?, E> cacheData = (Map<?, E>) ENUM_VALUE_CACHE_MAP.get(key);
            if (cacheData == null) {
                synchronized (ENUM_VALUE_CACHE_MAP) {
                    cacheData = (Map<?, E>) ENUM_VALUE_CACHE_MAP.get(key);
                    if (cacheData == null) {
                        cacheData = Arrays.stream(enumClass.getEnumConstants())
                                .collect(Collectors.toMap(supplyValue, Function.identity()));
                        ENUM_VALUE_CACHE_MAP.put(key, cacheData);
                    }
                }
            }
            return cacheData.get(value);
        }
    }

    /**
     * 通过IEnum接口的getValue方法获取枚举实例，使用缓存
     * <p>
     * 适用于实现了IEnum接口的枚举类
     * </p>
     * @param enumClass 枚举类
     * @param value 值
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 匹配的枚举实例，如果未找到则返回null
     */
    public static <V, E extends Enum<?> & IEnum<V>> E getEnumByValue(Class<E> enumClass, V value) {
        return getEnumByValue(enumClass, value, true);
    }

    /**
     * 通过IEnum接口的getValue方法获取枚举实例
     * <p>
     * 适用于实现了IEnum接口的枚举类，可选择是否使用缓存
     * </p>
     * @param enumClass 枚举类
     * @param value 值
     * @param useCache 是否使用缓存，使用缓存时（第一次仍会全量缓存，之后则一直使用缓存）
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 匹配的枚举实例，如果未找到则返回null
     */
    public static <V, E extends Enum<?> & IEnum<V>> E getEnumByValue(Class<E> enumClass, V value, boolean useCache) {
        return getEnum(enumClass, value, e -> e.getValue(), useCache);
    }

    /**
     * 通过枚举名称获取枚举实例，使用缓存
     * <p>
     * 等同于Enum.valueOf()方法，但不会抛出IllegalArgumentException异常
     * </p>
     * @param enumClass 枚举类
     * @param name 枚举名称
     * @param <E> 枚举类型
     * @return 匹配的枚举实例，如果未找到则返回null
     */
    public static <E extends Enum<?>> E getEnumByName(Class<E> enumClass, String name) {
        return getEnumByName(enumClass, name, true);
    }

    /**
     * 通过枚举名称获取枚举实例
     * <p>
     * 等同于Enum.valueOf()方法，但不会抛出IllegalArgumentException异常
     * 可选择是否使用缓存
     * </p>
     * @param enumClass 枚举类
     * @param name 枚举名称
     * @param useCache 是否使用缓存
     * @param <E> 枚举类型
     * @return 匹配的枚举实例，如果未找到则返回null
     */
    public static <E extends Enum<?>> E getEnumByName(Class<E> enumClass, String name, boolean useCache) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return getEnum(enumClass, name, Enum::name, useCache);
    }

    /**
     * 通过枚举序号获取枚举实例
     * <p>
     * 根据枚举的ordinal值获取对应的枚举实例
     * </p>
     * @param enumClass 枚举类
     * @param ordinal 枚举序号
     * @param <E> 枚举类型
     * @return 匹配的枚举实例，如果序号越界则返回null
     */
    public static <E extends Enum<?>> E getEnumByOrdinal(Class<E> enumClass, int ordinal) {
        E[] constants = enumClass.getEnumConstants();
        if (ordinal < 0 || ordinal >= constants.length) {
            return null;
        }
        return constants[ordinal];
    }

    /**
     * 获取枚举的所有实例列表
     * <p>
     * 返回枚举类中定义的所有枚举常量
     * </p>
     * @param enumClass 枚举类
     * @param <E> 枚举类型
     * @return 包含所有枚举实例的不可变列表
     */
    public static <E extends Enum<?>> List<E> getEnumList(Class<E> enumClass) {
        E[] constants = enumClass.getEnumConstants();
        return constants == null ? Collections.emptyList() : Collections.unmodifiableList(Arrays.asList(constants));
    }

    /**
     * 检查指定的值是否在枚举中存在
     * <p>
     * 通过自定义函数判断值是否存在于枚举中，使用缓存
     * </p>
     * @param enumClass 枚举类
     * @param value 要检查的值
     * @param supplyValue 通过枚举获取值的函数
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 如果值存在返回true，否则返回false
     */
    public static <V, E extends Enum<?>> boolean contains(Class<E> enumClass, V value, Function<E, V> supplyValue) {
        return contains(enumClass, value, supplyValue, true);
    }

    /**
     * 检查指定的值是否在枚举中存在
     * <p>
     * 通过自定义函数判断值是否存在于枚举中，可选择是否使用缓存
     * </p>
     * @param enumClass 枚举类
     * @param value 要检查的值
     * @param supplyValue 通过枚举获取值的函数
     * @param useCache 是否使用缓存
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 如果值存在返回true，否则返回false
     */
    public static <V, E extends Enum<?>> boolean contains(Class<E> enumClass, V value, Function<E, V> supplyValue, boolean useCache) {
        return getEnum(enumClass, value, supplyValue, useCache) != null;
    }

    /**
     * 检查指定的值是否在实现了IEnum接口的枚举中存在
     * <p>
     * 通过IEnum接口的getValue方法判断值是否存在于枚举中，使用缓存
     * </p>
     * @param enumClass 枚举类
     * @param value 要检查的值
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 如果值存在返回true，否则返回false
     */
    public static <V, E extends Enum<?> & IEnum<V>> boolean containsByValue(Class<E> enumClass, V value) {
        return containsByValue(enumClass, value, true);
    }

    /**
     * 检查指定的值是否在实现了IEnum接口的枚举中存在
     * <p>
     * 通过IEnum接口的getValue方法判断值是否存在于枚举中，可选择是否使用缓存
     * </p>
     * @param enumClass 枚举类
     * @param value 要检查的值
     * @param useCache 是否使用缓存
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 如果值存在返回true，否则返回false
     */
    public static <V, E extends Enum<?> & IEnum<V>> boolean containsByValue(Class<E> enumClass, V value, boolean useCache) {
        return getEnumByValue(enumClass, value, useCache) != null;
    }

    /**
     * 检查指定的名称是否在枚举中存在
     * <p>
     * 通过枚举的name方法判断名称是否存在于枚举中，使用缓存
     * </p>
     * @param enumClass 枚举类
     * @param name 要检查的名称
     * @param <E> 枚举类型
     * @return 如果名称存在返回true，否则返回false
     */
    public static <E extends Enum<?>> boolean containsName(Class<E> enumClass, String name) {
        return containsName(enumClass, name, true);
    }

    /**
     * 检查指定的名称是否在枚举中存在
     * <p>
     * 通过枚举的name方法判断名称是否存在于枚举中，可选择是否使用缓存
     * </p>
     * @param enumClass 枚举类
     * @param name 要检查的名称
     * @param useCache 是否使用缓存
     * @param <E> 枚举类型
     * @return 如果名称存在返回true，否则返回false
     */
    public static <E extends Enum<?>> boolean containsName(Class<E> enumClass, String name, boolean useCache) {
        return getEnumByName(enumClass, name, useCache) != null;
    }

    /**
     * 获取枚举的所有值列表
     * <p>
     * 通过自定义函数获取枚举的所有值
     * </p>
     * @param enumClass 枚举类
     * @param supplyValue 通过枚举获取值的函数
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 包含所有枚举值的列表
     */
    public static <V, E extends Enum<?>> List<V> getValueList(Class<E> enumClass, Function<E, V> supplyValue) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(supplyValue)
                .collect(Collectors.toList());
    }

    /**
     * 获取实现了IEnum接口的枚举的所有值列表
     * <p>
     * 通过IEnum接口的getValue方法获取所有枚举值
     * </p>
     * @param enumClass 枚举类
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 包含所有枚举值的列表
     */
    public static <V, E extends Enum<?> & IEnum<V>> List<V> getValueListByValue(Class<E> enumClass) {
        return getValueList(enumClass, e -> e.getValue());
    }

    /**
     * 获取枚举的所有名称列表
     * <p>
     * 通过枚举的name方法获取所有枚举名称
     * </p>
     * @param enumClass 枚举类
     * @param <E> 枚举类型
     * @return 包含所有枚举名称的列表
     */
    public static <E extends Enum<?>> List<String> getNameList(Class<E> enumClass) {
        return getValueList(enumClass, Enum::name);
    }

    /**
     * 获取枚举的所有序号列表
     * <p>
     * 通过枚举的ordinal方法获取所有枚举序号
     * </p>
     * @param enumClass 枚举类
     * @param <E> 枚举类型
     * @return 包含所有枚举序号的列表
     */
    public static <E extends Enum<?>> List<Integer> getOrdinalList(Class<E> enumClass) {
        return getValueList(enumClass, Enum::ordinal);
    }

    /**
     * 清除指定枚举类的缓存
     * <p>
     * 清除指定枚举类和值函数对应的缓存数据
     * </p>
     * @param enumClass 枚举类
     * @param supplyValue 值函数
     * @param <V> 值类型
     * @param <E> 枚举类型
     */
    public static <V, E extends Enum<?>> void clearCache(Class<E> enumClass, Function<E, V> supplyValue) {
        UnionKey key = new UnionKey(enumClass, supplyValue);
        ENUM_VALUE_CACHE_MAP.remove(key);
    }

    /**
     * 清除指定枚举类的所有缓存
     * <p>
     * 清除指定枚举类的所有缓存数据
     * </p>
     * @param enumClass 枚举类
     * @param <E> 枚举类型
     */
    public static <E extends Enum<?>> void clearAllCache(Class<E> enumClass) {
        ENUM_VALUE_CACHE_MAP.keySet().removeIf(key -> {
            Object key1 = key.getKey(0);
            return enumClass.equals(key1);
        });
    }

    /**
     * 清除所有枚举缓存
     * <p>
     * 清除所有枚举类的缓存数据
     * </p>
     */
    public static void clearAllCache() {
        ENUM_VALUE_CACHE_MAP.clear();
    }

    /**
     * 通过lambda函数获取枚举实例，使用缓存
     * <p>
     * 支持格式：枚举A a = EnumUtils.getBy(枚举A.class, 枚举A::方法, 枚举值);
     * </p>
     * @param enumClass 枚举类
     * @param mapper 通过枚举获取值的函数
     * @param value 要匹配的值
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 匹配的枚举实例，如果未找到则返回null
     */
    public static <V, E extends Enum<?>> E getBy(Class<E> enumClass, Function<E, V> mapper, V value) {
        return getEnum(enumClass, value, mapper, true);
    }

    /**
     * 通过lambda函数获取枚举实例，如果不存在返回默认值
     * <p>
     * 支持格式：枚举A a = EnumUtils.getByOrDefault(枚举A.class, 枚举A::方法, 枚举值, 默认值);
     * </p>
     * @param enumClass 枚举类
     * @param mapper 通过枚举获取值的函数
     * @param value 要匹配的值
     * @param defaultValue 默认值
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 匹配的枚举实例，如果未找到则返回默认值
     */
    public static <V, E extends Enum<?>> E getByOrDefault(Class<E> enumClass, Function<E, V> mapper, V value, E defaultValue) {
        E result = getBy(enumClass, mapper, value);
        return result != null ? result : defaultValue;
    }

    /**
     * 通过lambda函数获取枚举实例，如果不存在抛出异常
     * <p>
     * 支持格式：枚举A a = EnumUtils.getByOrThrow(枚举A.class, 枚举A::方法, 枚举值);
     * </p>
     * @param enumClass 枚举类
     * @param mapper 通过枚举获取值的函数
     * @param value 要匹配的值
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 匹配的枚举实例
     * @throws IllegalArgumentException 如果未找到匹配的枚举实例
     */
    public static <V, E extends Enum<?>> E getByOrThrow(Class<E> enumClass, Function<E, V> mapper, V value) {
        E result = getBy(enumClass, mapper, value);
        if (result == null) {
            throw new IllegalArgumentException("No enum constant " + enumClass.getName() + " with value: " + value);
        }
        return result;
    }

    /**
     * 通过Predicate条件获取枚举实例
     * <p>
     * 支持格式：枚举A a = EnumUtils.getByPredicate(枚举A.class, e -> e.方法().equals(值));
     * </p>
     * @param enumClass 枚举类
     * @param predicate 判断条件
     * @param <E> 枚举类型
     * @return 匹配的枚举实例，如果未找到则返回null
     */
    public static <E extends Enum<?>> E getByPredicate(Class<E> enumClass, java.util.function.Predicate<E> predicate) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    /**
     * 通过Predicate条件获取枚举实例，如果不存在返回默认值
     * <p>
     * 支持格式：枚举A a = EnumUtils.getByPredicateOrDefault(枚举A.class, e -> e.方法().equals(值), 默认值);
     * </p>
     * @param enumClass 枚举类
     * @param predicate 判断条件
     * @param defaultValue 默认值
     * @param <E> 枚举类型
     * @return 匹配的枚举实例，如果未找到则返回默认值
     */
    public static <E extends Enum<?>> E getByPredicateOrDefault(Class<E> enumClass, java.util.function.Predicate<E> predicate, E defaultValue) {
        E result = getByPredicate(enumClass, predicate);
        return result != null ? result : defaultValue;
    }

    /**
     * 通过Predicate条件获取枚举实例，如果不存在抛出异常
     * <p>
     * 支持格式：枚举A a = EnumUtils.getByPredicateOrThrow(枚举A.class, e -> e.方法().equals(值));
     * </p>
     * @param enumClass 枚举类
     * @param predicate 判断条件
     * @param <E> 枚举类型
     * @return 匹配的枚举实例
     * @throws IllegalArgumentException 如果未找到匹配的枚举实例
     */
    public static <E extends Enum<?>> E getByPredicateOrThrow(Class<E> enumClass, java.util.function.Predicate<E> predicate) {
        E result = getByPredicate(enumClass, predicate);
        if (result == null) {
            throw new IllegalArgumentException("No enum constant " + enumClass.getName() + " matching the predicate");
        }
        return result;
    }

    /**
     * 通过lambda函数获取枚举实例列表
     * <p>
     * 支持格式：List<枚举A> list = EnumUtils.getByList(枚举A.class, 枚举A::方法, 枚举值);
     * </p>
     * @param enumClass 枚举类
     * @param mapper 通过枚举获取值的函数
     * @param value 要匹配的值
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 匹配的枚举实例列表
     */
    public static <V, E extends Enum<?>> List<E> getByList(Class<E> enumClass, Function<E, V> mapper, V value) {
        return getByList(enumClass, mapper, value, true);
    }

    /**
     * 通过lambda函数获取枚举实例列表
     * <p>
     * 支持格式：List<枚举A> list = EnumUtils.getByList(枚举A.class, 枚举A::方法, 枚举值);
     * </p>
     * @param enumClass 枚举类
     * @param mapper 通过枚举获取值的函数
     * @param value 要匹配的值
     * @param useCache 是否使用缓存
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 匹配的枚举实例列表
     */
    public static <V, E extends Enum<?>> List<E> getByList(Class<E> enumClass, Function<E, V> mapper, V value, boolean useCache) {
        if (!useCache) {
            return Arrays.stream(enumClass.getEnumConstants())
                    .filter(e -> value.equals(mapper.apply(e)))
                    .collect(Collectors.toList());
        } else {
            E result = getEnum(enumClass, value, mapper, true);
            return result != null ? Collections.singletonList(result) : Collections.emptyList();
        }
    }

    /**
     * 通过Predicate条件获取枚举实例列表
     * <p>
     * 支持格式：List<枚举A> list = EnumUtils.getByPredicateList(枚举A.class, e -> e.方法().equals(值));
     * </p>
     * @param enumClass 枚举类
     * @param predicate 判断条件
     * @param <E> 枚举类型
     * @return 匹配的枚举实例列表
     */
    public static <E extends Enum<?>> List<E> getByPredicateList(Class<E> enumClass, java.util.function.Predicate<E> predicate) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * 通过lambda函数判断枚举是否存在，使用缓存
     * <p>
     * 支持格式：boolean exists = EnumUtils.exists(枚举A.class, 枚举A::方法, 枚举值);
     * </p>
     * @param enumClass 枚举类
     * @param mapper 通过枚举获取值的函数
     * @param value 要匹配的值
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 如果存在返回true，否则返回false
     */
    public static <V, E extends Enum<?>> boolean exists(Class<E> enumClass, Function<E, V> mapper, V value) {
        return exists(enumClass, mapper, value, true);
    }

    /**
     * 通过lambda函数判断枚举是否存在
     * <p>
     * 支持格式：boolean exists = EnumUtils.exists(枚举A.class, 枚举A::方法, 枚举值);
     * </p>
     * @param enumClass 枚举类
     * @param mapper 通过枚举获取值的函数
     * @param value 要匹配的值
     * @param useCache 是否使用缓存
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 如果存在返回true，否则返回false
     */
    public static <V, E extends Enum<?>> boolean exists(Class<E> enumClass, Function<E, V> mapper, V value, boolean useCache) {
        return contains(enumClass, value, mapper, useCache);
    }

    /**
     * 通过Predicate条件判断枚举是否存在
     * <p>
     * 支持格式：boolean exists = EnumUtils.existsPredicate(枚举A.class, e -> e.方法().equals(值));
     * </p>
     * @param enumClass 枚举类
     * @param predicate 判断条件
     * @param <E> 枚举类型
     * @return 如果存在返回true，否则返回false
     */
    public static <E extends Enum<?>> boolean existsPredicate(Class<E> enumClass, java.util.function.Predicate<E> predicate) {
        return getByPredicate(enumClass, predicate) != null;
    }

    /**
     * 通过lambda函数获取枚举的第一个实例
     * <p>
     * 支持格式：枚举A a = EnumUtils.getFirst(枚举A.class);
     * </p>
     * @param enumClass 枚举类
     * @param <E> 枚举类型
     * @return 第一个枚举实例，如果枚举为空则返回null
     */
    public static <E extends Enum<?>> E getFirst(Class<E> enumClass) {
        E[] constants = enumClass.getEnumConstants();
        return (constants == null || constants.length == 0) ? null : constants[0];
    }

    /**
     * 通过lambda函数获取枚举的最后一个实例
     * <p>
     * 支持格式：枚举A a = EnumUtils.getLast(枚举A.class);
     * </p>
     * @param enumClass 枚举类
     * @param <E> 枚举类型
     * @return 最后一个枚举实例，如果枚举为空则返回null
     */
    public static <E extends Enum<?>> E getLast(Class<E> enumClass) {
        E[] constants = enumClass.getEnumConstants();
        return (constants == null || constants.length == 0) ? null : constants[constants.length - 1];
    }

    /**
     * 通过lambda函数获取枚举的数量
     * <p>
     * 支持格式：int count = EnumUtils.getCount(枚举A.class);
     * </p>
     * @param enumClass 枚举类
     * @param <E> 枚举类型
     * @return 枚举实例的数量
     */
    public static <E extends Enum<?>> int getCount(Class<E> enumClass) {
        E[] constants = enumClass.getEnumConstants();
        return constants == null ? 0 : constants.length;
    }

    /**
     * 通过lambda函数判断枚举是否为空
     * <p>
     * 支持格式：boolean isEmpty = EnumUtils.isEmpty(枚举A.class);
     * </p>
     * @param enumClass 枚举类
     * @param <E> 枚举类型
     * @return 如果枚举为空返回true，否则返回false
     */
    public static <E extends Enum<?>> boolean isEmpty(Class<E> enumClass) {
        return getCount(enumClass) == 0;
    }

    /**
     * 通过lambda函数获取枚举实例，使用缓存
     * <p>
     * 支持格式：枚举A a = EnumUtils.getBy(枚举A::方法, 枚举值);
     * </p>
     * @param mapper 通过枚举获取值的函数
     * @param value 要匹配的值
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 匹配的枚举实例，如果未找到则返回null
     */
    public static <V, E extends Enum<?>> E getBy(Function<E, V> mapper, V value) {
        Class<E> enumClass = LambdaUtils.getDeclaringClass(mapper);
        return getBy(enumClass, mapper, value);
    }

    /**
     * 通过lambda函数获取枚举实例，如果不存在返回默认值
     * <p>
     * 支持格式：枚举A a = EnumUtils.getByOrDefault(枚举A::方法, 枚举值, 默认值);
     * </p>
     * @param mapper 通过枚举获取值的函数
     * @param value 要匹配的值
     * @param defaultValue 默认值
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 匹配的枚举实例，如果未找到则返回默认值
     */
    public static <V, E extends Enum<?>> E getByOrDefault(Function<E, V> mapper, V value, E defaultValue) {
        Class<E> enumClass = LambdaUtils.getDeclaringClass(mapper);
        return getByOrDefault(enumClass, mapper, value, defaultValue);
    }

    /**
     * 通过lambda函数获取枚举实例，如果不存在抛出异常
     * <p>
     * 支持格式：枚举A a = EnumUtils.getByOrThrow(枚举A::方法, 枚举值);
     * </p>
     * @param mapper 通过枚举获取值的函数
     * @param value 要匹配的值
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 匹配的枚举实例
     * @throws IllegalArgumentException 如果未找到匹配的枚举实例
     */
    public static <V, E extends Enum<?>> E getByOrThrow(Function<E, V> mapper, V value) {
        Class<E> enumClass = LambdaUtils.getDeclaringClass(mapper);
        return getByOrThrow(enumClass, mapper, value);
    }

    /**
     * 通过lambda函数判断枚举值是否存在，使用缓存
     * <p>
     * 支持格式：boolean exists = EnumUtils.exists(枚举A::方法, 枚举值);
     * </p>
     * @param mapper 通过枚举获取值的函数
     * @param value 要匹配的值
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 如果存在返回true，否则返回false
     */
    public static <V, E extends Enum<?>> boolean exists(Function<E, V> mapper, V value) {
        Class<E> enumClass = LambdaUtils.getDeclaringClass(mapper);
        return exists(enumClass, mapper, value);
    }

    /**
     * 通过lambda函数获取枚举实例列表，使用缓存
     * <p>
     * 支持格式：List<枚举A> list = EnumUtils.getByList(枚举A::方法, 枚举值);
     * </p>
     * @param mapper 通过枚举获取值的函数
     * @param value 要匹配的值
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 匹配的枚举实例列表
     */
    public static <V, E extends Enum<?>> List<E> getByList(Function<E, V> mapper, V value) {
        Class<E> enumClass = LambdaUtils.getDeclaringClass(mapper);
        return getByList(enumClass, mapper, value);
    }

    /**
     * 判断是否为枚举类
     * @param clazz 类对象
     * @return 如果是枚举类返回true，否则返回false
     */
    public static boolean isEnum(Class<?> clazz) {
        return clazz != null && clazz.isEnum();
    }

    /**
     * 获取枚举的值数组
     * @param enumClass 枚举类
     * @param <E> 枚举类型
     * @return 枚举值数组
     */
    public static <E extends Enum<?>> E[] getValues(Class<E> enumClass) {
        if (enumClass == null || !enumClass.isEnum()) {
            return null;
        }
        return enumClass.getEnumConstants();
    }

    /**
     * 获取枚举的名称数组
     * @param enumClass 枚举类
     * @param <E> 枚举类型
     * @return 枚举名称数组
     */
    public static <E extends Enum<?>> String[] getNames(Class<E> enumClass) {
        E[] values = getValues(enumClass);
        if (values == null) {
            return null;
        }
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].name();
        }
        return names;
    }

    /**
     * 获取枚举的序号数组
     * @param enumClass 枚举类
     * @param <E> 枚举类型
     * @return 枚举序号数组
     */
    public static <E extends Enum<?>> int[] getOrdinals(Class<E> enumClass) {
        E[] values = getValues(enumClass);
        if (values == null) {
            return null;
        }
        int[] ordinals = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            ordinals[i] = values[i].ordinal();
        }
        return ordinals;
    }

    /**
     * 根据序号获取枚举实例
     * <p>
     * 等同于getEnumByOrdinal方法
     * </p>
     * @param enumClass 枚举类
     * @param ordinal 序号
     * @param <E> 枚举类型
     * @return 匹配的枚举实例，如果序号越界则返回null
     */
    public static <E extends Enum<?>> E getByOrdinal(Class<E> enumClass, int ordinal) {
        return getEnumByOrdinal(enumClass, ordinal);
    }

    /**
     * 根据名称获取枚举实例
     * <p>
     * 等同于getEnumByName方法
     * </p>
     * @param enumClass 枚举类
     * @param name 枚举名称
     * @param <E> 枚举类型
     * @return 匹配的枚举实例，如果未找到则返回null
     */
    public static <E extends Enum<?>> E getByName(Class<E> enumClass, String name) {
        return getEnumByName(enumClass, name);
    }

    /**
     * 获取枚举的所有值
     * <p>
     * 等同于getEnumList方法
     * </p>
     * @param enumClass 枚举类
     * @param <E> 枚举类型
     * @return 包含所有枚举实例的列表
     */
    public static <E extends Enum<?>> List<E> list(Class<E> enumClass) {
        return getEnumList(enumClass);
    }

    /**
     * 获取枚举的值列表
     * <p>
     * 等同于getValueList方法
     * </p>
     * @param enumClass 枚举类
     * @param supplyValue 通过枚举获取值的函数
     * @param <V> 值类型
     * @param <E> 枚举类型
     * @return 包含所有枚举值的列表
     */
    public static <V, E extends Enum<?>> List<V> valueList(Class<E> enumClass, Function<E, V> supplyValue) {
        return getValueList(enumClass, supplyValue);
    }

    /**
     * 获取枚举的名称列表
     * <p>
     * 等同于getNameList方法
     * </p>
     * @param enumClass 枚举类
     * @param <E> 枚举类型
     * @return 包含所有枚举名称的列表
     */
    public static <E extends Enum<?>> List<String> nameList(Class<E> enumClass) {
        return getNameList(enumClass);
    }

    /**
     * 获取枚举的序号列表
     * <p>
     * 等同于getOrdinalList方法
     * @param enumClass 枚举类
     * @param <E> 枚举类型
     * @return 包含所有枚举序号的列表
     */
    public static <E extends Enum<?>> List<Integer> ordinalList(Class<E> enumClass) {
        return getOrdinalList(enumClass);
    }
}