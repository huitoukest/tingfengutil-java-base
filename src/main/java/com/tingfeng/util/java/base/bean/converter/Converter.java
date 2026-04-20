package com.tingfeng.util.java.base.bean.converter;

/**
 * 类型转换器接口
 * @param <S> 源类型
 * @param <T> 目标类型
 */
public interface Converter<S, T> {

    /**
     * 执行类型转换
     * @param source 源对象
     * @return 转换后的目标对象
     */
    T convert(S source);

    /**
     * 获取源类型
     * @return 源类型 Class
     */
    Class<S> getSourceType();

    /**
     * 获取目标类型
     * @return 目标类型 Class
     */
    Class<T> getTargetType();
}
