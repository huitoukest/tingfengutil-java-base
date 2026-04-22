package com.tingfeng.util.java.base.bean.converter;

import com.tingfeng.util.java.base.bean.base.ConverterInfo;
import com.tingfeng.util.java.base.lang.StringUtils;

import java.util.function.Function;

/**
 * 转换工具
 * 默认不处理 null 转换的情况
 * 1. 将会在 bean 属性 copy 使用
 * 2. 将会在 csv 的读取与写的时候使用
 * <p>
 * 保留此类以保持向后兼容，内部委托给 ConverterRegistry
 */
public class ConverterUtil {

    private static final ConverterRegistry MANAGER = ConverterRegistry.getInstance();

    private ConverterUtil() {}

    /**
     * 注册转换器（保持向后兼容）
     * @param converterInfo 转换器信息
     */
    public static void register(ConverterInfo<?, ?> converterInfo) {
        MANAGER.register(new ConverterInfoAdapter(converterInfo));
    }

    /**
     * 移除转换器
     * @param converter 要移除的转换器
     * @return 是否成功移除
     */
    public static boolean unregister(Converter<?, ?> converter) {
        return MANAGER.unregister(converter);
    }

    /**
     * 获取转换器
     * @param src 源类型
     * @param target 目标类型
     * @return 转换函数
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <S, T> Function<S, T> getConverter(Class<S> src, Class<T> target) {
        java.util.List converters = MANAGER.findAll(src, target);
        if (converters == null || converters.isEmpty()) {
            return null;
        }
        Converter<S, T> converter = (Converter<S, T>) converters.get(0);
        return converter != null ? converter::convert : null;
    }

    /**
     * 快捷转换方法
     * @param source 源对象
     * @param target 目标类型
     * @return 转换后的对象
     */
    public static <T> T convert(Object source, Class<T> target) {
        return MANAGER.convert(source, target);
    }

    /**
     * 当值不为空时执行转换
     * @param converter 转换函数
     * @return 包装后的函数
     */
    public static <T> Function<String, T> convertWhenNotBlank(Function<String, T> converter) {
        return src -> {
            if (StringUtils.isEmpty(src, true)) {
                return null;
            }
            return converter.apply(src);
        };
    }

    /**
     * 清空所有转换器
     */
    public static void clear() {
        MANAGER.clear();
    }

    /**
     * 适配器：将 ConverterInfo 转换为 Converter
     */
    private static class ConverterInfoAdapter implements Converter<Object, Object> {
        private final ConverterInfo<?, ?> info;

        ConverterInfoAdapter(ConverterInfo<?, ?> info) {
            this.info = info;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object convert(Object source) {
            return ((java.util.function.Function<Object, Object>) info.getConvertMethod()).apply(source);
        }

        @Override
        public Class<Object> getSourceType() {
            return (Class<Object>) info.getSource();
        }

        @Override
        public Class<Object> getTargetType() {
            return (Class<Object>) info.getTarget();
        }
    }
}
