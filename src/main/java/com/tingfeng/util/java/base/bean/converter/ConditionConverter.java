package com.tingfeng.util.java.base.bean.converter;

/**
 * 条件类型转换器接口
 * 当 matches() 返回 true 时触发转换
 * 支持多个同类型的 ConditionConverter，按 order 排序
 * @param <S> 源类型
 * @param <T> 目标类型
 */
public interface ConditionConverter<S, T> extends Converter<S, T> {

    /**
     * 判断是否满足转换条件
     * @param source 源对象
     * @return 是否满足条件
     */
    boolean matches(S source);

}
