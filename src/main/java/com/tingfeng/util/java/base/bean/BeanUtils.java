package com.tingfeng.util.java.base.bean;

import com.tingfeng.util.java.base.bean.base.CommonType;
import com.tingfeng.util.java.base.bean.copier.BeanCopier;
import com.tingfeng.util.java.base.bean.copier.BeanDesc;
import com.tingfeng.util.java.base.bean.copier.CopyOptions;
import com.tingfeng.util.java.base.bean.copier.MapValueProvider;
import com.tingfeng.util.java.base.bean.copier.PropertyResult;
import com.tingfeng.util.java.base.bean.copier.ValueProvider;
import com.tingfeng.util.java.base.lang.exception.BaseException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean工具门面类，提供统一的属性拷贝与对象转换入口。
 * <p>
 * 核心方法：
 * <ul>
 *   <li>{@link #copyProperties(Object, Object)} - 对象间属性拷贝</li>
 *   <li>{@link #toBean(Object, Class)} - 对象转换为目标类型实例</li>
 *   <li>{@link #toBean(ValueProvider, Class)} - 从值提供者创建Bean实例</li>
 * </ul>
 *
 * @author huitoukest
 */
@Slf4j
public final class BeanUtils {

    /**
     * 私有构造器，禁止外部实例化
     */
    private BeanUtils() {
    }

    // ========== copyProperties ==========

    /**
     * 将source对象的属性拷贝到target对象（浅拷贝）。
     * <p>
     * source或target为null时直接返回，不抛异常。
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyProperties(Object source, Object target) {
        BeanCopier.copy(source, target, null);
    }

    /**
     * 将source对象的属性拷贝到target对象，支持配置选项（浅拷贝）。
     * <p>
     * source或target为null时直接返回，不抛异常。
     *
     * @param source  源对象
     * @param target  目标对象
     * @param options 拷贝选项（可为null，使用默认选项）
     */
    public static void copyProperties(Object source, Object target, CopyOptions options) {
        BeanCopier.copy(source, target, options);
    }

    // ========== toBean ==========

    /**
     * 将source对象转换为targetClass类型的实例。
     * <p>
     * 要求targetClass有无参构造器。
     * <p>
     * 如果source为null，直接返回null，不抛异常。
     *
     * @param source      源对象
     * @param targetClass 目标类型
     * @param <T>         目标类型泛型
     * @return 目标类型实例，source为null时返回null
     * @throws BaseException 如果目标类无法实例化
     */
    public static <T> T toBean(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanCopier.copy(source, target, null);
            return target;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new BaseException(e);
        }
    }

    /**
     * 将source对象转换为targetClass类型的实例，支持配置选项。
     * <p>
     * 要求targetClass有无参构造器。
     * <p>
     * 如果source为null，直接返回null，不抛异常。
     *
     * @param source      源对象
     * @param targetClass 目标类型
     * @param options     拷贝选项（可为null，使用默认选项）
     * @param <T>         目标类型泛型
     * @return 目标类型实例，source为null时返回null
     * @throws BaseException 如果目标类无法实例化
     */
    public static <T> T toBean(Object source, Class<T> targetClass, CopyOptions options) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanCopier.copy(source, target, options);
            return target;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new BaseException(e);
        }
    }

    /**
     * 将source对象转换为targetClass类型的实例，使用指定的构造器和参数。
     * <p>
     * 如果source为null，直接返回null，不抛异常。
     *
     * @param source      源对象
     * @param constructor 目标类型的构造器（非null）
     * @param args        构造器参数
     * @param <T>         目标类型泛型
     * @return 目标类型实例，source为null时返回null
     * @throws BaseException 如果目标类无法实例化
     */
    public static <T> T toBean(Object source, Constructor<T> constructor, Object... args) {
        return toBean(source, constructor, null, args);
    }

    /**
     * 将source对象转换为targetClass类型的实例，使用指定的构造器和参数，支持配置选项。
     * <p>
     * 如果source为null，直接返回null，不抛异常。
     *
     * @param source      源对象
     * @param constructor 目标类型的构造器（非null）
     * @param options     拷贝选项（可为null，使用默认选项）
     * @param args        构造器参数
     * @param <T>         目标类型泛型
     * @return 目标类型实例，source为null时返回null
     * @throws BaseException 如果目标类无法实例化
     */
    public static <T> T toBean(Object source, Constructor<T> constructor, CopyOptions options, Object... args) {
        if (source == null) {
            return null;
        }
        try {
            T target = constructor.newInstance(args);
            BeanCopier.copy(source, target, options);
            return target;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new BaseException(e);
        }
    }

    // ========== toBean from ValueProvider ==========

    /**
     * 从ValueProvider创建targetClass类型的实例。
     * <p>
     * 要求targetClass有无参构造器。
     * <p>
     * 如果provider或targetClass为null，直接返回null，不抛异常。
     *
     * @param provider    值提供者
     * @param targetClass 目标类型
     * @param <T>         目标类型泛型
     * @return 目标类型实例，provider或targetClass为null时返回null
     * @throws BaseException 如果目标类无法实例化
     */
    public static <T> T toBean(ValueProvider<?> provider, Class<T> targetClass) {
        if (provider == null || targetClass == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanCopier.copyFromProvider(provider, target, null);
            return target;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new BaseException(e);
        }
    }

    /**
     * 从ValueProvider创建targetClass类型的实例，支持配置选项。
     * <p>
     * 要求targetClass有无参构造器。
     * <p>
     * 如果provider或targetClass为null，直接返回null，不抛异常。
     *
     * @param provider    值提供者
     * @param targetClass 目标类型
     * @param options     拷贝选项（可为null，使用默认选项）
     * @param <T>         目标类型泛型
     * @return 目标类型实例，provider或targetClass为null时返回null
     * @throws BaseException 如果目标类无法实例化
     */
    public static <T> T toBean(ValueProvider<?> provider, Class<T> targetClass, CopyOptions options) {
        if (provider == null || targetClass == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanCopier.copyFromProvider(provider, target, options);
            return target;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new BaseException(e);
        }
    }

    // ========== toBean from Map ==========

    /**
     * 从 Map 创建 Bean 实例（便捷方法，内部包装 MapValueProvider）。
     * <p>
     * 等价于 {@code toBean(new MapValueProvider(map), targetClass)}。
     *
     * @param map         Map 数据源
     * @param targetClass 目标类型
     * @param <T>        目标类型泛型
     * @return 目标类型实例，map 或 targetClass 为 null 时返回 null
     * @throws BaseException 如果目标类无法实例化
     */
    public static <T> T toBean(Map<String, ?> map, Class<T> targetClass) {
        if (map == null || targetClass == null) {
            return null;
        }
        return toBean(new MapValueProvider(map), targetClass);
    }

    /**
     * 从 Map 创建 Bean 实例，支持配置选项。
     *
     * @param map         Map 数据源
     * @param targetClass 目标类型
     * @param options     拷贝选项
     * @param <T>        目标类型泛型
     * @return 目标类型实例，map 或 targetClass 为 null 时返回 null
     * @throws BaseException 如果目标类无法实例化
     */
    public static <T> T toBean(Map<String, ?> map, Class<T> targetClass, CopyOptions options) {
        if (map == null || targetClass == null) {
            return null;
        }
        return toBean(new MapValueProvider(map), targetClass, options);
    }

    // ========== toMap ==========

    /**
     * 将bean对象转换为Map（包含所有非null属性）。
     * <p>
     * 如果bean为null，返回空Map。
     * <p>
     * 等同于 {@code toMap(bean, (String[]) null)}。
     *
     * @param bean 源对象
     * @return 属性名-属性值的Map
     */
    public static Map<String, Object> toMap(Object bean) {
        return BeanCopier.toMap(bean, null);
    }

    /**
     * 将bean对象转换为Map，可忽略指定属性。
     * <p>
     * 如果bean为null，返回空Map。
     *
     * @param bean              源对象
     * @param ignoreProperties  要忽略的属性名（可变参数，可为null或空数组）
     * @return 属性名-属性值的Map
     * @deprecated 从 V5 开始废弃。推荐使用 {@link #toMap(Object)} 配合
     *             {@link com.tingfeng.util.java.base.bean.copier.CopyOptions#setIgnoreProperties(Collection)} 的方式：
     *             <pre>{@code
     *             CopyOptions options = CopyOptions.create()
     *                 .setIgnoreProperties(Arrays.asList("field1", "field2"));
     *             Map<String, Object> map = BeanCopier.toMap(bean, options);
     *             }</pre>
     *             或直接使用 {@link #toMap(Object)} 后手动过滤。
     */
    @Deprecated
    public static Map<String, Object> toMap(Object bean, String... ignoreProperties) {
        CopyOptions options = null;
        if (ignoreProperties != null && ignoreProperties.length > 0) {
            options = CopyOptions.create().setIgnoreProperties(Arrays.asList(ignoreProperties));
        }
        return BeanCopier.toMap(bean, options);
    }

    // ========== toList ==========

    /**
     * 批量拷贝：将List中的元素转换为目标类型的List。
     * <p>
     * 要求targetClass有无参构造器。
     * <p>
     * 如果sources为null或空列表，返回空列表，不抛异常。
     *
     * @param sources     源列表
     * @param targetClass 目标类型
     * @param <S>        源类型泛型
     * @param <T>        目标类型泛型
     * @return 目标类型的List，sources为null或空时返回空List
     * @throws BaseException 如果目标类无法实例化
     */
    public static <S, T> List<T> toList(List<S> sources, Class<T> targetClass) {
        if (sources == null || sources.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<T> result = new ArrayList<>(sources.size());
        for (S source : sources) {
            result.add(toBean(source, targetClass));
        }
        return result;
    }

    /**
     * 批量拷贝：将List中的元素转换为目标类型的List，支持配置选项。
     * <p>
     * 要求targetClass有无参构造器。
     * <p>
     * 如果sources为null或空列表，返回空列表，不抛异常。
     *
     * @param sources     源列表
     * @param targetClass 目标类型
     * @param options     拷贝选项（可为null，使用默认选项）
     * @param <S>        源类型泛型
     * @param <T>        目标类型泛型
     * @return 目标类型的List，sources为null或空时返回空List
     * @throws BaseException 如果目标类无法实例化
     */
    public static <S, T> List<T> toList(List<S> sources, Class<T> targetClass, CopyOptions options) {
        if (sources == null || sources.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<T> result = new ArrayList<>(sources.size());
        for (S source : sources) {
            result.add(toBean(source, targetClass, options));
        }
        return result;
    }

    /**
     * 批量拷贝：将 List 中的元素转换为目标类型，支持泛型类型推断。
     * <p>
     * 通过 TypeReference 保留泛型信息，自动提取目标类型并执行批量拷贝。
     * <p>
     * 注意：TypeReference 的泛型参数 T 是目标元素类型，而非容器类型。
     * 例如 {@code new TypeReference<User>() {}} 表示目标类型为 User。
     *
     * @param sources  源列表
     * @param typeRef 泛型类型引用，如 {@code new TypeReference<User>() {}}
     * @param <T>    目标元素类型
     * @return 目标类型的 List，sources 为 null 或空时返回空 List
     * @throws BaseException 如果目标类无法实例化
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> toList(List<?> sources, TypeReference<T> typeRef) {
        if (sources == null || sources.isEmpty()) {
            return new ArrayList<>(0);
        }
        Type type = typeRef.getType();
        Class<?> elementClass = resolveElementClass(type);
        if (elementClass == null) {
            throw new BaseException("Cannot resolve element class from TypeReference");
        }
        List<T> result = new ArrayList<>(sources.size());
        for (Object source : sources) {
            if (source == null) {
                continue;
            }
            result.add((T) toBean(source, elementClass));
        }
        return result;
    }

    /**
     * 从 TypeReference 中解析元素类型。
     * <p>
     * T 直接是目标元素类型，例如 User。
     *
     * @param type TypeReference.getType() 返回的 Type
     * @return 元素类型
     */
    private static Class<?> resolveElementClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        // 对于简单泛型情况，如 TypeReference<User>，type 是 Class
        // 对于容器泛型情况，如 TypeReference<List<User>>，type 是 ParameterizedType
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type rawType = pt.getRawType();
            if (rawType instanceof Class) {
                Class<?> rawClass = (Class<?>) rawType;
                // 如果是 List/Collection 类型，提取泛型参数作为元素类型
                if (List.class.isAssignableFrom(rawClass) || Collection.class.isAssignableFrom(rawClass)) {
                    Type[] typeArgs = pt.getActualTypeArguments();
                    if (typeArgs != null && typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                        return (Class<?>) typeArgs[0];
                    }
                }
                return rawClass;
            }
        }
        return null;
    }

    // ========== isEmpty ==========

    /**
     * 判断bean是否为空（所有属性都为null或无属性）。
     * <p>
     * bean为null时返回true。
     * <p>
     * 排除"class"属性后，任一属性值非null则返回false，全部为null或无属性则返回true。
     *
     * @param bean 待检查的对象
     * @return 是否为空
     */
    public static boolean isEmpty(Object bean) {
        if (bean == null) {
            return true;
        }
        for (String name : BeanCopier.getOrCreateBeanDesc(bean.getClass()).getPropertyNames()) {
            if ("class".equals(name)) {
                continue;
            }
            PropertyResult<Object> result = BeanCopier.getOrCreateBeanDesc(bean.getClass()).getPropertyValue(bean, name);
            if (result.exists() && result.getValue() != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断bean是否非空。
     * <p>
     * 等同于 {@code !isEmpty(bean)}。
     *
     * @param bean 待检查的对象
     * @return 是否非空
     */
    public static boolean isNotEmpty(Object bean) {
        return !isEmpty(bean);
    }

    // ========== deepCopy ==========

    /**
     * 深拷贝对象，返回完全独立的副本。
     * <p>
     * 支持以下类型：
     * <ul>
     *   <li>基本类型及包装类型 — 直接返回</li>
     *   <li>String、BigDecimal 等不可变类型 — 直接返回引用</li>
     *   <li>数组 — 深拷贝元素</li>
     *   <li>Collection — 深拷贝元素</li>
     *   <li>Map — 深拷贝 key 和 value</li>
     *   <li>普通 Java Bean — 递归反射拷贝属性</li>
     * </ul>
     * <p>
     * 循环引用检测：通过 IdentityHashMap 记录已拷贝对象，
     * 遇到重复引用时直接返回已创建的副本。
     *
     * @param source 源对象
     * @param <T>    对象类型
     * @return 完全独立的副本，source 为 null 时返回 null
     * @throws BaseException 如果拷贝过程中发生反射异常
     */
    public static <T> T deepCopy(T source) {
        return deepCopy(source, Integer.MAX_VALUE, new IdentityHashMap<>());
    }

    /**
     * 深拷贝对象，支持深度控制。
     * <p>
     * maxDepth 控制递归拷贝的深度：
     * <ul>
     *   <li>maxDepth = 0：只创建外层容器/实例，元素不递归（浅拷贝行为）</li>
     *   <li>maxDepth = 1：拷贝一层嵌套</li>
     *   <li>maxDepth = 2：拷贝两层嵌套</li>
     *   <li>maxDepth = Integer.MAX_VALUE：无限制深度（等同于 deepCopy(T)）</li>
     * </ul>
     *
     * @param source   源对象
     * @param maxDepth 最大递归深度（必须 >= 0）
     * @param <T>      对象类型
     * @return 完全独立的副本，source 为 null 时返回 null
     * @throws IllegalArgumentException 如果 maxDepth < 0
     * @throws BaseException            如果拷贝过程中发生反射异常
     */
    public static <T> T deepCopy(T source, int maxDepth) {
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth must be >= 0");
        }
        return deepCopy(source, maxDepth, new IdentityHashMap<>());
    }

    /**
     * 深拷贝对象（内部递归方法）。
     *
     * @param source         源对象
     * @param remainingDepth 剩余递归深度
     * @param visited        已拷贝对象映射（用于循环引用检测）
     * @param <T>            对象类型
     * @return 完全独立的副本
     */
    @SuppressWarnings("unchecked")
    private static <T> T deepCopy(T source, int remainingDepth, IdentityHashMap<Object, Object> visited) {
        if (source == null) {
            return null;
        }

        // 1. 不可变类型直接返回引用
        if (isImmutableType(source)) {
            return source;
        }

        // 2. 循环引用检测
        if (visited.containsKey(source)) {
            return (T) visited.get(source);
        }

        Class<?> clazz = source.getClass();

        // 3. 数组类型
        if (clazz.isArray()) {
            return deepCopyArray(source, remainingDepth, visited);
        }

        // 4. Collection 类型
        if (source instanceof Collection) {
            return deepCopyCollection((Collection<?>) source, remainingDepth, visited);
        }

        // 5. Map 类型
        if (source instanceof Map) {
            return deepCopyMap((Map<?, ?>) source, remainingDepth, visited);
        }

        // 6. 普通 Java Bean — 递归反射拷贝
        return deepCopyBean(source, remainingDepth, visited);
    }

    /**
     * 判断是否为不可变类型（直接返回引用，不深拷贝）。
     */
    private static boolean isImmutableType(Object obj) {
        if (obj == null) {
            return false;
        }
        // 基本类型包装类、String、BigDecimal、BigInteger、Class、URI、URL、UUID、enum
        return obj instanceof String
                || obj instanceof Boolean
                || obj instanceof Byte
                || obj instanceof Short
                || obj instanceof Integer
                || obj instanceof Long
                || obj instanceof Float
                || obj instanceof Double
                || obj instanceof Character
                || obj instanceof BigDecimal
                || obj instanceof BigInteger
                || obj instanceof Class
                || obj instanceof java.net.URI
                || obj instanceof java.net.URL
                || obj instanceof java.util.UUID
                || obj instanceof Enum;
    }

    /**
     * 深拷贝数组。
     */
    @SuppressWarnings("unchecked")
    private static <T> T deepCopyArray(T source, int remainingDepth, IdentityHashMap<Object, Object> visited) {
        Class<?> clazz = source.getClass();
        Class<?> componentType = clazz.getComponentType();

        visited.put(source, null);

        if (componentType.isPrimitive()) {
            // 基本类型数组：使用 Array.newInstance 创建新数组并拷贝元素
            int length = java.lang.reflect.Array.getLength(source);
            Object destArray = java.lang.reflect.Array.newInstance(componentType, length);
            System.arraycopy(source, 0, destArray, 0, length);
            return (T) destArray;
        } else {
            // 对象数组
            Object[] srcArray = (Object[]) source;
            Object[] destArray = (Object[]) java.lang.reflect.Array.newInstance(componentType, srcArray.length);
            visited.put(source, destArray);

            if (remainingDepth <= 0) {
                // remainingDepth <= 0：不递归，元素直接引用（浅拷贝）
                for (int i = 0; i < srcArray.length; i++) {
                    destArray[i] = srcArray[i];
                }
            } else {
                // remainingDepth > 0：递归拷贝
                for (int i = 0; i < srcArray.length; i++) {
                    destArray[i] = deepCopy(srcArray[i], remainingDepth - 1, visited);
                }
            }
            return (T) destArray;
        }
    }

    /**
     * 深拷贝 Collection。
     */
    @SuppressWarnings("unchecked")
    private static <T> T deepCopyCollection(Collection<?> source, int remainingDepth, IdentityHashMap<Object, Object> visited) {
        // 记录当前映射关系，防止 Collection 内部元素循环引用
        visited.put(source, null);

        // 尝试实例化 source 的实际 Collection 类型
        Collection<Object> result;
        try {
            @SuppressWarnings("unchecked")
            Collection<Object> instance = source.getClass().getDeclaredConstructor().newInstance();
            result = instance;
        } catch (Exception e) {
            // 回退路径：尝试通过 CommonType 解析接口对应的实现类
            Class<?> implClass = CommonType.resolveImplementation(source.getClass());
            if (implClass != null) {
                try {
                    result = (Collection<Object>) implClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    result = new ArrayList<>(source.size());
                }
            } else {
                result = new ArrayList<>(source.size());
            }
        }
        visited.put(source, result);

        if (remainingDepth <= 0) {
            // remainingDepth <= 0：不递归，元素直接引用（浅拷贝）
            for (Object item : source) {
                result.add(item);
            }
        } else {
            // remainingDepth > 0：递归拷贝
            for (Object item : source) {
                result.add(deepCopy(item, remainingDepth - 1, visited));
            }
        }
        return (T) result;
    }

    /**
     * 深拷贝 Map。
     */
    @SuppressWarnings("unchecked")
    private static <T> T deepCopyMap(Map<?, ?> source, int remainingDepth, IdentityHashMap<Object, Object> visited) {
        // 记录当前映射关系，防止 Map 内部 key/value 循环引用
        visited.put(source, null);

        // 尝试实例化 source 的实际 Map 类型
        Map<Object, Object> result;
        try {
            @SuppressWarnings("unchecked")
            Map<Object, Object> instance = source.getClass().getDeclaredConstructor().newInstance();
            result = instance;
        } catch (Exception e) {
            // 回退路径：尝试通过 CommonType 解析接口对应的实现类
            Class<?> implClass = CommonType.resolveImplementation(source.getClass());
            if (implClass != null) {
                try {
                    result = (Map<Object, Object>) implClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    result = new java.util.HashMap<>(source.size());
                }
            } else {
                result = new java.util.HashMap<>(source.size());
            }
        }
        visited.put(source, result);

        if (remainingDepth <= 0) {
            // remainingDepth <= 0：不递归，key/value 直接引用（浅拷贝）
            for (Map.Entry<?, ?> entry : source.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
        } else {
            // remainingDepth > 0：递归拷贝
            for (Map.Entry<?, ?> entry : source.entrySet()) {
                Object keyCopy = deepCopy(entry.getKey(), remainingDepth - 1, visited);
                Object valueCopy = deepCopy(entry.getValue(), remainingDepth - 1, visited);
                result.put(keyCopy, valueCopy);
            }
        }
        return (T) result;
    }

    /**
     * 深拷贝普通 Java Bean。
     */
    @SuppressWarnings("unchecked")
    private static <T> T deepCopyBean(T source, int remainingDepth, IdentityHashMap<Object, Object> visited) {
        Class<?> clazz = source.getClass();

        // 创建新实例
        T target;
        try {
            target = (T) clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new BaseException("Failed to deep copy: no default constructor for " + clazz.getName(), e);
        }

        // 先放入 visited，防止循环引用
        visited.put(source, target);

        // 遍历所有属性
        BeanDesc desc = BeanCopier.getOrCreateBeanDesc(clazz);
        for (String propName : desc.getPropertyNames()) {
            if ("class".equals(propName)) {
                continue;
            }

            // 跳过 transient 和 static 字段
            Field field = desc.getField(propName);
            if (field != null) {
                int mod = field.getModifiers();
                if (Modifier.isTransient(mod) || Modifier.isStatic(mod)) {
                    continue;
                }
            }

            PropertyResult<Object> propResult = desc.getPropertyValue(source, propName);
            if (!propResult.exists()) {
                continue;
            }
            Object value = propResult.getValue();

            if (remainingDepth <= 0) {
                // remainingDepth <= 0：不递归，属性值直接浅拷贝
                // 当 value 为 null 且属性类型为接口时，通过 CommonType 创建空实例
                if (value == null) {
                    Class<?> propType = desc.getPropertyType(propName);
                    if (propType != null && propType.isInterface()) {
                        Class<?> implClass = CommonType.resolveImplementation(propType);
                        if (implClass != null) {
                            try {
                                value = implClass.getDeclaredConstructor().newInstance();
                            } catch (Exception ex) {
                                // 创建失败，保持 null
                            }
                        }
                    }
                }
                desc.setPropertyValue(target, propName, value);
            } else {
                // remainingDepth > 0：递归拷贝
                Object copiedValue = deepCopy(value, remainingDepth - 1, visited);
                // 当 copiedValue 为 null 且属性类型为接口时，通过 CommonType 创建空实例
                if (copiedValue == null) {
                    Class<?> propType = desc.getPropertyType(propName);
                    if (propType != null && propType.isInterface()) {
                        Class<?> implClass = CommonType.resolveImplementation(propType);
                        if (implClass != null) {
                            try {
                                copiedValue = implClass.getDeclaredConstructor().newInstance();
                            } catch (Exception ex) {
                                // 创建失败，保持 null
                            }
                        }
                    }
                }
                desc.setPropertyValue(target, propName, copiedValue);
            }
        }
        return target;
    }

    // ========== hasNullField ==========

    /**
     * 判断bean是否存在null属性。
     * <p>
     * bean为null时返回true。
     *
     * @param bean 待检查的对象
     * @return 是否存在null属性
     */
    public static boolean hasNullField(Object bean) {
        if (bean == null) {
            return true;
        }
        for (String name : BeanCopier.getOrCreateBeanDesc(bean.getClass()).getPropertyNames()) {
            if ("class".equals(name)) {
                continue;
            }
            PropertyResult<Object> result = BeanCopier.getOrCreateBeanDesc(bean.getClass()).getPropertyValue(bean, name);
            if (!result.exists()) {
                return true;
            }
            if (result.getValue() == null) {
                return true;
            }
        }
        return false;
    }
}