package com.tingfeng.util.java.base.bean;

import com.tingfeng.util.java.base.bean.copier.BeanCopier;
import com.tingfeng.util.java.base.bean.copier.CopyOptions;
import com.tingfeng.util.java.base.bean.copier.ValueProvider;
import com.tingfeng.util.java.base.lang.exception.BaseException;

import java.util.ArrayList;
import java.util.Arrays;
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
public final class BeanUtil {

    /**
     * 私有构造器，禁止外部实例化
     */
    private BeanUtil() {
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
            T target = targetClass.newInstance();
            BeanCopier.copy(source, target, null);
            return target;
        } catch (InstantiationException | IllegalAccessException e) {
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
            T target = targetClass.newInstance();
            BeanCopier.copy(source, target, options);
            return target;
        } catch (InstantiationException | IllegalAccessException e) {
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
            T target = targetClass.newInstance();
            BeanCopier.copyFromProvider(provider, target, null);
            return target;
        } catch (InstantiationException | IllegalAccessException e) {
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
            T target = targetClass.newInstance();
            BeanCopier.copyFromProvider(provider, target, options);
            return target;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BaseException(e);
        }
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
     */
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
}