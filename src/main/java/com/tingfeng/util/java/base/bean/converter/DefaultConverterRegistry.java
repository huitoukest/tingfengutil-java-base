package com.tingfeng.util.java.base.bean.converter;

import com.tingfeng.util.java.base.bean.converter.defaults.DefaultConverters;
import com.tingfeng.util.java.base.common.collection.ReadWriteArrayList;
import com.tingfeng.util.java.base.lang.support.ClassUtils;
import com.tingfeng.util.java.base.lang.base.UnionKey;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认转换器注册中心实现
 * <p>
 * 特性：
 * <ul>
 *   <li>使用 ConcurrentHashMap 保证线程安全</li>
 *   <li>自动同时注册原始类型+包装类型</li>
 *   <li>ConditionConverter 多个，按 (registrationOrder, order, 源类名, 原始目标类名) 排序，convert 时检查 matches</li>
 *   <li>Converter 支持多个（因冒泡注册可能产生多个同类型对的副本），后注册的不覆盖，按排序规则确定优先级</li>
 *   <li>支持冒泡注册：注册时自动遍历父类链+接口链，为父类型产生冒泡副本</li>
 * </ul>
 * <p>
 * 冒泡注册（bubble）机制说明：
 * <ul>
 *   <li>注册时通过对 targetType 的父类链+接口链递归创建副本</li>
 *   <li>bubbleLevel 控制冒泡深度：BUBBLE_UNLIMITED=-1 表示无限制，冒泡到 Object 为止</li>
 *   <li>冒泡副本的优先级低于原始注册（registrationOrder 递增：原始为 0，冒泡一层 +1）</li>
 *   <li>排序时精确注册始终优先于冒泡副本</li>
 * </ul>
 * <p>
 * 冒泡副本类说明：
 * <ul>
 *   <li>BubbledConverter：普通 Converter 的冒泡副本，转换逻辑委托给原始 Converter</li>
 *   <li>BubbledConditionConverter：ConditionConverter 的冒泡副本，额外委托 matches() 方法</li>
 *   <li>两者均实现原始 Converter 的所有接口方法，仅覆盖 registrationOrder/bubbleLevel/targetType</li>
 * </ul>
 */
public class DefaultConverterRegistry implements ConverterRegistry {

    /**
     * 条件转换器：UnionKey -> ReadWriteArrayList&lt;ConditionConverter&gt;（按排序规则排序）
     */
    private final Map<UnionKey, ReadWriteArrayList<ConditionConverter<?, ?>>> conditionConverters;

    /**
     * 普通转换器：UnionKey -> ReadWriteArrayList&lt;Converter&gt;
     */
    private final Map<UnionKey, ReadWriteArrayList<Converter<?, ?>>> converters;

    /**
     * 追踪原始 Converter -> 其冒泡副本的 UnionKey 列表（用于 unregister）
     */
    private final Map<Converter<?, ?>, List<UnionKey>> originalToBubbledKeys;

    // ==================== 排序比较器 ====================

    /**
     * 获取 Converter 的原始目标类型名称用于排序。
     * 冒泡副本通过 delegate 获取冒泡前的原始目标类名，非冒泡转换器直接用 getTargetType()。
     */
    private static String getOriginalTargetTypeName(Converter<?, ?> c) {
        if (c instanceof BubbledConverter) {
            return ((BubbledConverter<?, ?>) c).delegate.getTargetType().getName();
        }
        return c.getTargetType().getName();
    }

    /**
     * Converter 排序比较器。
     * <p>
     * 排序链（全部升序）：
     * <ol>
     *   <li>registrationOrder — 精确注册(=0)优先，冒泡层级越高(=1,2...)越靠后</li>
     *   <li>order — 用户自定义优先级</li>
     *   <li>getSourceType().getName() — 源类名字母序</li>
     *   <li>原始目标类名 — 通过 delegate 获取冒泡前的目标类名，区分不同来源的冒泡副本</li>
     * </ol>
     * 使用显式 lambda 避免 Java 8 对链式 Comparator 的类型推断问题。
     */
    private static final Comparator<Converter<?, ?>> CONVERTER_ORDER_COMPARATOR = (c1, c2) -> {
        int cmp = Integer.compare(c1.registrationOrder(), c2.registrationOrder());
        if (cmp != 0) return cmp;
        cmp = Integer.compare(c1.order(), c2.order());
        if (cmp != 0) return cmp;
        cmp = c1.getSourceType().getName().compareTo(c2.getSourceType().getName());
        if (cmp != 0) return cmp;
        return getOriginalTargetTypeName(c1).compareTo(getOriginalTargetTypeName(c2));
    };

    // ==================== 构造器 & 单例 ====================

    public DefaultConverterRegistry() {
        this.conditionConverters = new ConcurrentHashMap<>();
        this.converters = new ConcurrentHashMap<>();
        this.originalToBubbledKeys = new ConcurrentHashMap<>();
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

    // ==================== register / unregister ====================

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

        int bubbleLevel = converter.bubbleLevel();

        // 记录本次注册影响的所有 UnionKey
        Set<UnionKey> affectedKeys = new HashSet<>();

        // 1. 注册原始 Converter
        registerOne(converter, srcType, targetType);
        affectedKeys.add(new UnionKey(srcType, targetType));

        // 2. 冒泡注册
        if (bubbleLevel != 0) {
            Set<UnionKey> bubbledKeys = new HashSet<>();
            bubbledKeys.add(new UnionKey(srcType, targetType)); // 原始已注册，跳过
            bubbleToHierarchy(converter, srcType, targetType,
                    bubbleLevel, 0, bubbledKeys, affectedKeys);
        }

        // 3. 对所有受影响的列表统一排序
        for (UnionKey key : affectedKeys) {
            sortConverterList(key);
        }
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

        boolean removed = unregisterOne(converter, srcType, targetType);

        // 如果移除的是原始 Converter，同时移除所有冒泡副本
        List<UnionKey> bubbledKeys = originalToBubbledKeys.remove(converter);
        if (bubbledKeys != null) {
            for (UnionKey key : bubbledKeys) {
                removeBubbledCopies(converter, key);
            }
        }

        return removed;
    }

    // ==================== find / convert ====================

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
            return new ConverterSearchResult<>(Collections.emptyList(), Collections.emptyList());
        }

        UnionKey key = new UnionKey(source, target);

        // 1. 获取所有 ConditionConverter
        List<ConditionConverter<?, ?>> conditionList = conditionConverters.get(key);

        // 2. 获取所有普通 Converter（之前为单个，现为列表）
        List<Converter<?, ?>> converterList = converters.get(key);

        return new ConverterSearchResult<>(
                (List) (conditionList != null ? conditionList : Collections.emptyList()),
                (List) (converterList != null ? converterList : Collections.emptyList()));
    }

    /**
     * 查找源类型层次中可用的转换器（用于源类型多态查找）
     *
     * @param sourceType 源类型
     * @param target     目标类型
     * @return 可用的 Converter，或 null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <S, T> Converter<S, T> findConverterInSourceHierarchy(Class<S> sourceType, Class<T> target) {
        if (sourceType == null || target == null) {
            return null;
        }

        // 遍历源类型的父类链（包含 Object）
        Class<?> current = sourceType;
        while (current != null) {
            UnionKey key = new UnionKey(current, target);
            List<Converter<?, ?>> list = converters.get(key);
            if (list != null && !list.isEmpty()) {
                return (Converter<S, T>) list.get(0);
            }
            current = current.getSuperclass();
        }

        // 遍历源类型实现的接口链
        for (Class<?> iface : sourceType.getInterfaces()) {
            UnionKey key = new UnionKey(iface, target);
            List<Converter<?, ?>> list = converters.get(key);
            if (list != null && !list.isEmpty()) {
                return (Converter<S, T>) list.get(0);
            }
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S, T> T convert(S source, Class<T> target) {
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
     *
     * @param source     源对象实例（用于 matches 检查）
     * @param sourceType 源类型
     * @param target     目标类型
     * @return 匹配的 Converter，或 null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <S, T> Converter<S, T> getConverterByValue(S source, Class<S> sourceType, Class<T> target) {
        if (source == null || sourceType == null || target == null) {
            return null;
        }
        ConverterSearchResult<S, T> result = findConverters(sourceType, target);

        // 1. 遍历条件转换器（已排序），找 matches(source) 返回 true 的
        for (ConditionConverter<S, T> cc : result.getConditionConverters()) {
            if (cc.matches(source)) {
                return cc;
            }
        }

        // 2. 返回普通转换器列表中的第一个（已排序，第一个即最优）
        List<Converter<S, T>> converterList = result.getConverters();
        if (!converterList.isEmpty()) {
            return converterList.get(0);
        }

        return null;
    }

    /**
     * 获取转换器（仅按类型）
     *
     * @param sourceType 源类型
     * @param target     目标类型
     * @return 普通 Converter 列表中的第一个，或 null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <S, T> Converter<S, T> getConverter(Class<S> sourceType, Class<T> target) {
        if (sourceType == null || target == null) {
            return null;
        }
        ConverterSearchResult<S, T> result = findConverters(sourceType, target);
        // 只返回普通 Converter 列表中的第一个，条件转换器由 getConverterByValue 处理
        List<Converter<S, T>> converterList = result.getConverters();
        return converterList.isEmpty() ? null : converterList.get(0);
    }

    /**
     * 从查找结果中查找转换器并执行转换
     * <p>
     * 规则：遍历 ConditionConverter（按排序顺序），检查 matches，找到则转换返回；
     * 否则使用普通 Converter 列表中的第一个，找到则转换返回。都不匹配返回 null。
     *
     * @param result 转换器查找结果
     * @param source 源对象
     * @return 转换结果，或 null（无匹配）
     */
    private <S, T> T find(ConverterSearchResult<S, T> result, S source) {
        for (ConditionConverter<S, T> cc : result.getConditionConverters()) {
            if (cc.matches(source)) {
                return cc.convert(source);
            }
        }
        List<Converter<S, T>> converterList = result.getConverters();
        if (!converterList.isEmpty()) {
            return converterList.get(0).convert(source);
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
     * 规则：目标为包装类型 或 来源为基础类型时，先找自身，找不到则尝试源类型层次查找，找不到则找对应类型，找不到返回 null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <S, T> T convertToWrapper(S source, Class<S> sourceType, Class<T> target) {
        // 1. 先找自身转换器
        Converter<S, T> converter = getConverterByValue(source, sourceType, target);
        if (converter != null) {
            return converter.convert(source);
        }

        // 2. 尝试源类型层次查找（如 String -> Object 父类）
        converter = findConverterInSourceHierarchy(sourceType, target);
        if (converter != null) {
            return converter.convert(source);
        }

        // 3. 找不到则找对应类型（wrapper↔primitive）
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
     * 规则：优先自身转换器，找不到则尝试源类型层次查找，找不到且值不为null则尝试基础类型转换器，找不到返回 null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> T convertAuto(Object source, Class<?> sourceType, Class<T> target) {
        // 1. 优先使用包装类型自身转换器
        Converter<Object, T> converter = getConverterByValue(source, (Class<Object>) sourceType, target);
        if (converter != null) {
            return (T) converter.convert(source);
        }

        // 2. 尝试源类型层次查找（如 String -> Object 父类）
        converter = (Converter<Object, T>) findConverterInSourceHierarchy((Class<Object>) sourceType, target);
        if (converter != null) {
            return (T) converter.convert(source);
        }

        // 3. 找不到且值不为null，尝试基础类型转换器
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

    // ==================== 冒泡注册私有方法 ====================

    /**
     * 递归冒泡：遍历目标类型的父类链 + 接口链，为每个父类型创建冒泡副本
     *
     * @param converter       原始转换器
     * @param srcType         源类型
     * @param targetType      当前目标类型
     * @param bubbleLevel     当前剩余可冒泡层数
     * @param currentRegOrder 当前 registrationOrder
     * @param bubbledKeys     已产生的冒泡副本 key 集合（去重）
     * @param affectedKeys    本次注册影响的所有 UnionKey 集合（用于最后排序）
     */
    private void bubbleToHierarchy(
            Converter<?, ?> converter,
            Class<?> srcType,
            Class<?> targetType,
            int bubbleLevel,
            int currentRegOrder,
            Set<UnionKey> bubbledKeys,
            Set<UnionKey> affectedKeys) {

        if (bubbleLevel == 0) {
            return;
        }

        int nextBubbleLevel = (bubbleLevel == ConverterConstants.BUBBLE_UNLIMITED)
                ? ConverterConstants.BUBBLE_UNLIMITED : bubbleLevel - 1;
        int nextRegOrder = safeIncrementRegOrder(currentRegOrder);

        // 1️⃣ 处理父类链（先父类链，再接口链）
        Class<?> superClass = targetType.getSuperclass();
        if (superClass != null) {
            registerBubbledCopyIfAbsent(converter, srcType, superClass,
                    nextBubbleLevel, nextRegOrder, bubbledKeys, affectedKeys);
            bubbleToHierarchy(converter, srcType, superClass,
                    nextBubbleLevel, nextRegOrder, bubbledKeys, affectedKeys);
        }

        // 2️⃣ 处理接口链
        for (Class<?> iface : targetType.getInterfaces()) {
            registerBubbledCopyIfAbsent(converter, srcType, iface,
                    nextBubbleLevel, nextRegOrder, bubbledKeys, affectedKeys);
            bubbleToHierarchy(converter, srcType, iface,
                    nextBubbleLevel, nextRegOrder, bubbledKeys, affectedKeys);
        }
        // bubbleLevel = -1 时，Object 的 getSuperclass()=null, getInterfaces()=[] 自然终止
    }

    /**
     * 安全递增 registrationOrder（防溢出）
     */
    private static int safeIncrementRegOrder(int regOrder) {
        if (regOrder >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return regOrder + 1;
    }

    /**
     * 创建冒泡副本并注册（仅当该 UnionKey 尚未被本次冒泡产生时）
     */
    private void registerBubbledCopyIfAbsent(
            Converter<?, ?> original,
            Class<?> srcType,
            Class<?> newTargetType,
            int nextBubbleLevel,
            int nextRegOrder,
            Set<UnionKey> bubbledKeys,
            Set<UnionKey> affectedKeys) {

        UnionKey key = new UnionKey(srcType, newTargetType);
        if (bubbledKeys.contains(key)) {
            return; // 去重：不同路径可能到达同一父类型
        }
        bubbledKeys.add(key);
        affectedKeys.add(key);

        // 创建包裹 Converter（使用 raw 类型绕过通配符约束）
        @SuppressWarnings({"rawtypes", "unchecked"})
        Converter<?, ?> bubbled = createBubbledCopy((Converter) original,
                (Class) srcType, (Class) newTargetType, nextRegOrder, nextBubbleLevel);

        registerOne(bubbled, srcType, newTargetType);

        // 记录追踪信息，供 unregister 使用
        originalToBubbledKeys.computeIfAbsent(original, k -> new ArrayList<>()).add(key);
    }

    /**
     * 创建冒泡副本 Converter。
     * 所有转换逻辑委托给 original，仅覆盖 registrationOrder/bubbleLevel/targetType。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <S, T> Converter<S, T> createBubbledCopy(
            Converter<S, T> original,
            Class<S> srcType,
            Class<T> bubbledTargetType,
            int regOrder,
            int bl) {

        if (original instanceof ConditionConverter) {
            ConditionConverter<S, T> cc = (ConditionConverter<S, T>) original;
            return new BubbledConditionConverter<>(cc, srcType, bubbledTargetType, regOrder, bl);
        } else {
            return new BubbledConverter<>(original, srcType, bubbledTargetType, regOrder, bl);
        }
    }

    // ==================== 注册/注销私有方法 ====================

    /**
     * 注册单个转换器
     */
    private void registerOne(Converter<?, ?> converter, Class<?> srcType,
                             Class<?> targetType) {
        UnionKey key = new UnionKey(srcType, targetType);

        if (converter instanceof ConditionConverter) {
            // ConditionConverter 加入列表
            conditionConverters.computeIfAbsent(key, k -> new ReadWriteArrayList<>())
                    .add((ConditionConverter<?, ?>) converter);
        } else {
            // 普通 Converter 加入列表
            converters.computeIfAbsent(key, k -> new ReadWriteArrayList<>())
                    .add(converter);
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
                if (list.isEmpty()) {
                    conditionConverters.remove(key);
                }
            }
        } else {
            List<Converter<?, ?>> list = converters.get(key);
            if (list != null) {
                removed = list.remove(converter);
                if (list.isEmpty()) {
                    converters.remove(key);
                }
            }
        }
        return removed;
    }

    /**
     * 移除指定原始 Converter 在目标 UnionKey 下的所有冒泡副本。
     * <p>
     * 注意：使用索引逆序遍历 + {@code remove(int)} 而非 {@code removeIf()}，
     * 此处索引方式效率更高；{@link ReadWriteArrayList} 的惰性迭代器
     * 已支持 {@link Iterator#remove()} 正确修改底层列表。
     */
    private void removeBubbledCopies(Converter<?, ?> original, UnionKey key) {
        ReadWriteArrayList<Converter<?, ?>> cvList = converters.get(key);
        if (cvList != null) {
            for (int i = cvList.size() - 1; i >= 0; i--) {
                if (isBubbledCopyOf(cvList.get(i), original)) {
                    cvList.remove(i);
                }
            }
            if (cvList.isEmpty()) {
                converters.remove(key);
            }
        }

        ReadWriteArrayList<ConditionConverter<?, ?>> ccList = conditionConverters.get(key);
        if (ccList != null) {
            for (int i = ccList.size() - 1; i >= 0; i--) {
                if (isBubbledCopyOf(ccList.get(i), original)) {
                    ccList.remove(i);
                }
            }
            if (ccList.isEmpty()) {
                conditionConverters.remove(key);
            }
        }
    }

    /**
     * 判断 converter 是否是 original 的冒泡副本
     */
    private static boolean isBubbledCopyOf(Converter<?, ?> converter, Converter<?, ?> original) {
        if (converter instanceof BubbledConverter) {
            return ((BubbledConverter<?, ?>) converter).delegate == original;
        }
        return false;
    }

    // ==================== 排序 ====================

    /**
     * 对指定 key 的所有转换器列表进行排序
     */
    private void sortConverterList(UnionKey key) {
        // 排序 ConditionConverter 列表
        ReadWriteArrayList<ConditionConverter<?, ?>> ccList = conditionConverters.get(key);
        if (ccList != null && ccList.size() > 1) {
            ccList.sort((Comparator) CONVERTER_ORDER_COMPARATOR);
        }
        // 排序普通 Converter 列表
        ReadWriteArrayList<Converter<?, ?>> cvList = converters.get(key);
        if (cvList != null && cvList.size() > 1) {
            cvList.sort((Comparator) CONVERTER_ORDER_COMPARATOR);
        }
    }

    // ==================== clear / reset ====================

    @Override
    public void clear() {
        conditionConverters.clear();
        converters.clear();
        originalToBubbledKeys.clear();
    }

    @Override
    public void resetConverter() {
        clear();
        DefaultConverters.registerDefaults(this);
    }

    // ==================== 冒泡副本静态内部类 ====================

    /**
     * 普通转换器冒泡副本。
     * 所有转换逻辑委托给原始 {@link #delegate}，仅覆盖 registrationOrder/bubbleLevel/targetType。
     */
    static class BubbledConverter<S, T> implements Converter<S, T> {
        /** 原始转换器（包可见，供 Comparator 和外部逻辑访问） */
        final Converter<S, T> delegate;
        private final Class<S> sourceType;
        private final Class<T> targetType;
        private final int registrationOrder;
        private final int bubbleLevel;

        BubbledConverter(Converter<S, T> delegate, Class<S> sourceType,
                         Class<T> targetType, int registrationOrder, int bubbleLevel) {
            this.delegate = delegate;
            this.sourceType = sourceType;
            this.targetType = targetType;
            this.registrationOrder = registrationOrder;
            this.bubbleLevel = bubbleLevel;
        }

        @Override
        public T convert(S source) {
            return delegate.convert(source);
        }

        @Override
        public Class<S> getSourceType() {
            return sourceType;
        }

        @Override
        public Class<T> getTargetType() {
            return targetType;
        }

        @Override
        public int registrationOrder() {
            return registrationOrder;
        }

        @Override
        public int bubbleLevel() {
            return bubbleLevel;
        }

        @Override
        public int order() {
            return delegate.order();
        }

        /**
         * 获取原始转换器
         */
        public Converter<S, T> getDelegate() {
            return delegate;
        }
    }

    /**
     * ConditionConverter 冒泡副本。
     * <p>
     * 额外覆盖 matches() 方法，委托给原始 ConditionConverter。
     * 不持有独立的 delegate 字段（父类 {@link BubbledConverter#delegate} 已保存原始引用），
     * 通过强制类型转换访问其 {@link ConditionConverter#matches} 方法。
     * order() 无需重写：父类 {@link BubbledConverter#order()} 通过虚方法分派
     * 会自动调用原始 ConditionConverter 的覆盖版本。
     */
    static class BubbledConditionConverter<S, T> extends BubbledConverter<S, T>
            implements ConditionConverter<S, T> {

        BubbledConditionConverter(ConditionConverter<S, T> delegate, Class<S> sourceType,
                                  Class<T> targetType, int registrationOrder, int bubbleLevel) {
            super(delegate, sourceType, targetType, registrationOrder, bubbleLevel);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean matches(S source) {
            return ((ConditionConverter<S, T>) delegate).matches(source);
        }
    }
}
