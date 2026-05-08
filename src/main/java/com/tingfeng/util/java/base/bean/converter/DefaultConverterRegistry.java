package com.tingfeng.util.java.base.bean.converter;

import com.tingfeng.util.java.base.bean.converter.defaults.DefaultConverters;
import com.tingfeng.util.java.base.common.collection.ReadWriteArrayList;
import com.tingfeng.util.java.base.common.constant.ClassUtils;
import com.tingfeng.util.java.base.lang.base.UnionKey;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认转换器注册中心实现
 * <p>
 * 特性：
 * <ul>
 *   <li>使用 ConcurrentHashMap 保证线程安全</li>
 *   <li>自动同时注册原始类型+包装类型</li>
 *   <li>ConditionConverter 多个，按 order 排序，convert 时检查 matches</li>
 *   <li>Converter 每个类型对一个，后注册的替换先注册的</li>
 * </ul>
 */
public class DefaultConverterRegistry implements ConverterRegistry {

    /**
     * 条件转换器：UnionKey -> ReadWriteArrayList<ConditionConverter>（按 order 升序排序）
     */
    private final Map<UnionKey, ReadWriteArrayList<ConditionConverter<?, ?>>> conditionConverters;

    /**
     * 普通转换器：UnionKey -> Converter（每个类型对只有一个）
     */
    private final Map<UnionKey, Converter<?, ?>> converters;

    public DefaultConverterRegistry() {
        this.conditionConverters = new ConcurrentHashMap<>();
        this.converters = new ConcurrentHashMap<>();
    }

    private static volatile DefaultConverterRegistry INSTANCE;

    public static DefaultConverterRegistry getInstance() {
        if (INSTANCE == null) {
            synchronized (DefaultConverterRegistry.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DefaultConverterRegistry();
                }
            }
        }
        return INSTANCE;
    }


    @Override
    public <S, T> boolean unregister(Converter<S, T> converter) {
        if (converter == null) {
            return false;
        }
        Class<?> srcType = converter.getSourceType();
        Class<?> targetType = converter.getTargetType();
        if (srcType == null || targetType == null) {
            return false;
        }

        return unregisterOne(converter, srcType, targetType);
    }

    @Override
    public <S, T> List<Converter<S, T>> findAll(Class<S> source, Class<T> target) {
        if (source == null || target == null) {
            return Collections.emptyList();
        }
        return findConverters(source, target).getAllConverters();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <S, T> ConverterSearchResult<S, T> findConverters(Class<S> source, Class<T> target) {
        if (source == null || target == null) {
            return new ConverterSearchResult<>(Collections.emptyList(), null);
        }

        UnionKey key = new UnionKey(source, target);

        // 1. 获取所有 ConditionConverter
        List<ConditionConverter<?, ?>> conditionList = conditionConverters.get(key);

        // 2. 获取普通 Converter
        Converter<?, ?> converter = converters.get(key);

        return new ConverterSearchResult<>((List) conditionList, (Converter) converter);
    }

    @Override
    public <S, T> void register(Converter<S, T> converter) {
        if (converter == null) {
            return;
        }
        Class<?> srcType = converter.getSourceType();
        Class<?> targetType = converter.getTargetType();
        if (srcType == null || targetType == null) {
            return;
        }
        // 注册当前类型
        registerOne(converter, srcType, targetType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S,T> T convert(S source, Class<T> target) {
        if (source == null) {
            return null;
        }
        if (target == null) {
            throw new ConverterException("target type is null");
        }

        Class<S> sourceType = (Class<S>) source.getClass();
        if (sourceType.equals(target)) {
            return (T) source;
        }

        // 目标为基础类型：先找基础类型转换器，没有则找包装类型转换器
        if (ClassUtils.isPrimitive(target)) {
            T result = convertToPrimitive(source, sourceType, target);
            if (result != null) {
                return result;
            }
            throw new ConverterException(
                    String.format("No converter found from %s to %s", sourceType.getName(), target.getName()));
        }

        // 目标为包装类型 或 来源为基础类型：先找自身，找不到则找对应类型
        if (ClassUtils.isWrapper(target) || ClassUtils.isPrimitive(sourceType)) {
            T result = convertToWrapper(source, sourceType, target);
            if (result != null) {
                return result;
            }
            throw new ConverterException(
                    String.format("No converter found from %s to %s", sourceType.getName(), target.getName()));
        }

        // 来源为包装类型：优先自身，找不到且值不为null则尝试基础类型转换器
        T result = convertAuto(source, sourceType, target);
        if (result != null) {
            return result;
        }
        throw new ConverterException(
                String.format("No converter found from %s to %s", sourceType.getName(), target.getName()));
    }

    @Override
    public <S, T> T convert(S source, Class<T> target, T defaultValue) {
        if (source == null) {
            return defaultValue;
        }
        try {
            return convert(source, target);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // ==================== 转换辅助方法 ====================

    /**
     * 获取匹配的转换器
     * @param source 源对象实例（用于 matches 检查）
     * @param sourceType 源类型
     * @param target 目标类型
     * @return 匹配的 Converter，或 null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <S, T> Converter<S, T> getConverterByValue(S source, Class<S> sourceType, Class<T> target) {
        if (source == null || sourceType == null || target == null) {
            return null;
        }
        ConverterSearchResult<S, T> result = findConverters(sourceType, target);

        // 1. 遍历条件转换器，找 matches(source) 返回 true 的
        for (ConditionConverter<S, T> cc : result.getConditionConverters()) {
            if (cc.matches(source)) {
                return cc;
            }
        }

        // 2. 返回普通转换器
        return result.getConverter();
    }

    /**
     * 获取转换器（仅按类型）
     * @param sourceType 源类型
     * @param target 目标类型
     * @return 普通 Converter，或 null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <S, T> Converter<S, T> getConverter(Class<S> sourceType, Class<T> target) {
        if (sourceType == null || target == null) {
            return null;
        }
        ConverterSearchResult<S, T> result = findConverters(sourceType, target);
        // 只返回普通 Converter，条件转换器由 getConverterByValue 处理
        return result.getConverter();
    }

    /**
     * 从查找结果中查找转换器
     * <p>
     * 规则：遍历 ConditionConverter（按 order 顺序），检查 matches，找到则转换返回；
     * 否则使用普通 Converter，找到则转换返回。都不匹配返回 null。
     *
     * @param result 转换器查找结果
     * @param source 源对象
     * @return 转换器，或 null（无匹配）
     */
    private <S, T> T find(ConverterSearchResult<S, T> result, S source) {
        for (ConditionConverter<S, T> cc : result.getConditionConverters()) {
            if (cc.matches(source)) {
                return cc.convert(source);
            }
        }
        Converter<S, T> converter = result.getConverter();
        if (converter != null) {
            return converter.convert(source);
        }
        return null;
    }

    /**
     * 转换为基础类型
     * <p>
     * 规则：先找基础类型转换器，没有则找包装类型转换器，找不到返回 null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> T convertToPrimitive(Object source, Class<?> sourceType, Class<T> target) {
        // 1. 先尝试基础类型转换器
        Converter<Object, T> converter = getConverterByValue(source, (Class<Object>) sourceType, target);
        if (converter != null) {
            return converter.convert(source);
        }

        // 2. 找不到基础类型转换器，尝试包装类型转换器
        Class<?> wrapperTarget = ClassUtils.toWrapper(target);
        converter = getConverterByValue(source, (Class<Object>) sourceType, (Class) wrapperTarget);
        if (converter != null) {
            return converter.convert(source);
        }

        return null;
    }

    /**
     * 使用包装类型转换器转换
     * <p>
     * 规则：目标为包装类型 或 来源为基础类型时，先找自身，找不到则找对应类型，找不到返回 null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <S,T> T convertToWrapper(S source, Class<S> sourceType, Class<T> target) {
        // 1. 先找自身转换器
        Converter<S, T> converter = getConverterByValue(source,  sourceType, target);
        if (converter != null) {
            return converter.convert(source);
        }

        // 2. 找不到则找对应类型（wrapper↔primitive）
        Class<?> correspondingType = ClassUtils.isPrimitive(target)
                ? ClassUtils.toWrapper(target)
                : ClassUtils.toPrimitive(target);
        if (correspondingType != null) {
            converter = getConverterByValue(source, (Class<Object>) sourceType, (Class) correspondingType);
            if (converter != null) {
                return converter.convert(source);
            }
        }

        return null;
    }

    /**
     * 来源为包装类型时的转换
     * <p>
     * 规则：优先自身转换器，找不到且值不为null则尝试基础类型转换器，找不到返回 null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> T convertAuto(Object source, Class<?> sourceType, Class<T> target) {
        // 1. 优先使用包装类型自身转换器
        Converter<Object, T> converter = getConverterByValue(source, (Class<Object>) sourceType, target);
        if (converter != null) {
            return (T) converter.convert(source);
        }

        // 2. 找不到且值不为null，尝试基础类型转换器
        if (source != null) {
            Class<?> primitiveTarget = ClassUtils.toPrimitive(target);
            if (primitiveTarget != null) {
                converter = getConverterByValue(source, (Class<Object>) sourceType, (Class) primitiveTarget);
                if (converter != null) {
                    return (T) converter.convert(source);
                }
            }
        }

        return null;
    }

    // ==================== 私有方法 ====================

    /**
     * 注册单个转换器
     */
    private void registerOne(Converter<?, ?> converter, Class<?> srcType, Class<?> targetType) {
        UnionKey key = new UnionKey(srcType, targetType);

        if (converter instanceof ConditionConverter) {
            // ConditionConverter 加入列表（使用 ReadWriteArrayList）
            conditionConverters.computeIfAbsent(key, k -> new ReadWriteArrayList<>())
                    .add((ConditionConverter<?, ?>) converter);
            // 排序（按 order 升序）
            sortConditionConverters(key);
        } else {
            // 普通 Converter 直接替换
            converters.put(key, converter);
        }
    }

    /**
     * 注销单个转换器
     */
    private boolean unregisterOne(Converter<?, ?> converter, Class<?> srcType, Class<?> targetType) {
        UnionKey key = new UnionKey(srcType, targetType);

        boolean removed = false;
        if (converter instanceof ConditionConverter) {
            List<ConditionConverter<?, ?>> list = conditionConverters.get(key);
            if (list != null) {
                removed = list.remove(converter);
            }
        } else {
            Converter<?, ?> existing = converters.remove(key);
            removed = existing != null;
        }
        return removed;
    }

    /**
     * 对条件转换器列表排序（按 order 升序）
     */
    private void sortConditionConverters(UnionKey key) {
        ReadWriteArrayList<ConditionConverter<?, ?>> list = conditionConverters.get(key);
        if (list != null && list.size() > 1) {
            list.sort(Comparator.comparingInt(ConditionConverter::order));
        }
    }

    @Override
    public void clear() {
        conditionConverters.clear();
        converters.clear();
    }

    @Override
    public void resetConverter() {
        clear();
        DefaultConverters.registerDefaults(this);
    }
}
