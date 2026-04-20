package com.tingfeng.util.java.base.bean.converter;

import com.tingfeng.util.java.base.bean.converter.defaults.DefaultConverters;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 转换器工厂类
 */
public class ConverterUtils {

    private ConverterUtils() {}

    /**
     * 全局转换器注册中心单例
     */
    private static volatile DefaultConverterRegistry INSTANCE;

    public static DefaultConverterRegistry getInstance() {
        if (INSTANCE == null) {
            synchronized (ConverterUtils.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DefaultConverterRegistry();
                }
            }
        }
        return INSTANCE;
    }

    // ==================== Converter 工厂方法 ====================

    /**
     * 创建转换器
     *
     * @param src 源类型
     * @param target 目标类型
     * @param converter 转换函数，null 时不生效
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换器
     */
    public static <S, T> Converter<S, T> of(Class<S> src, Class<T> target, Function<S, T> converter) {
        if (converter == null) {
            return null;
        }
        return new Converter<S, T>() {
            @Override
            public T convert(S source) {
                return converter.apply(source);
            }

            @Override
            public Class<S> getSourceType() {
                return src;
            }

            @Override
            public Class<T> getTargetType() {
                return target;
            }
        };
    }

    /**
     * 注册转换器
     *
     * @param src 源类型
     * @param target 目标类型
     * @param converter 转换函数，null 时不生效
     * @param <S> 源类型
     * @param <T> 目标类型
     */
    public static <S, T> void register(Class<S> src, Class<T> target, Function<S, T> converter) {
        Converter<S, T> c = of(src, target, converter);
        if (c != null) {
            getInstance().register(c);
        }
    }

    // ==================== ConditionConverter 工厂方法 ====================
    // 参数顺序：src, target, order, matcher, converter

    /**
     * 创建条件转换器
     *
     * @param src 源类型
     * @param target 目标类型
     * @param order 排序标识，值越小优先级越高
     * @param matcher 条件匹配函数，返回 true 时触发转换
     * @param converter 转换函数
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 条件转换器
     */
    public static <S, T> ConditionConverter<S, T> of(Class<S> src, Class<T> target, int order,
            Predicate<S> matcher, Function<S, T> converter) {
        if (converter == null || matcher == null) {
            return null;
        }
        return new ConditionConverter<S, T>() {
            @Override
            public T convert(S source) {
                return converter.apply(source);
            }

            @Override
            public Class<S> getSourceType() {
                return src;
            }

            @Override
            public Class<T> getTargetType() {
                return target;
            }

            @Override
            public int order() {
                return order;
            }

            @Override
            public boolean matches(S source) {
                return matcher.test(source);
            }
        };
    }

    /**
     * 创建条件转换器（使用默认 order = 0）
     *
     * @param src 源类型
     * @param target 目标类型
     * @param matcher 条件匹配函数
     * @param converter 转换函数
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 条件转换器
     */
    public static <S, T> ConditionConverter<S, T> of(Class<S> src, Class<T> target,
            Predicate<S> matcher, Function<S, T> converter) {
        return of(src, target, 0, matcher, converter);
    }

    /**
     * 注册条件转换器
     *
     * @param src 源类型
     * @param target 目标类型
     * @param order 排序标识
     * @param matcher 条件匹配函数
     * @param converter 转换函数
     * @param <S> 源类型
     * @param <T> 目标类型
     */
    public static <S, T> void register(Class<S> src, Class<T> target, int order,
            Predicate<S> matcher, Function<S, T> converter) {
        ConditionConverter<S, T> c = of(src, target, order, matcher, converter);
        if (c != null) {
            getInstance().register(c);
        }
    }

    /**
     * 注册条件转换器（使用默认 order = 0）
     *
     * @param src 源类型
     * @param target 目标类型
     * @param matcher 条件匹配函数
     * @param converter 转换函数
     * @param <S> 源类型
     * @param <T> 目标类型
     */
    public static <S, T> void register(Class<S> src, Class<T> target,
            Predicate<S> matcher, Function<S, T> converter) {
        register(src, target, 0, matcher, converter);
    }

    // ==================== 便捷转换方法 ====================

    /**
     * 执行转换
     *
     * @param source 源对象
     * @param target 目标类型
     * @param <T> 目标类型
     * @return 转换后的对象
     */
    public static <T> T convert(Object source, Class<T> target) {
        return getInstance().convert(source, target);
    }

    /**
     * 执行转换，失败返回默认值
     *
     * @param source 源对象
     * @param target 目标类型
     * @param defaultValue 默认值
     * @param <T> 目标类型
     * @return 转换后的对象，失败返回默认值
     */
    public static <T> T convert(Object source, Class<T> target, T defaultValue) {
        return getInstance().convert(source, target, defaultValue);
    }

    /**
     * 清空所有已注册的转换器
     */
    public static void clear() {
        getInstance().clear();
    }

    /**
     * 重置为默认转换器配置（先清空，再注册默认转换器）
     */
    public static void resetConverter() {
        getInstance().resetConverter();
    }

    /**
     * 注册所有默认转换器
     */
    public static void registerDefaults() {
        DefaultConverters.registerDefaults(getInstance());
    }
}
