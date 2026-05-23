package com.tingfeng.util.java.base.bean.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 转换器查找结果封装
 * <p>
 * 将 ConditionConverter 和普通 Converter 分类返回，
 * 两者内部均已按 (registrationOrder, order, 源类名, 原始目标类名) 排序，
 * 提供 getAllConverters() 方法合并返回所有可用转换器。
 */
public class ConverterSearchResult<S, T> {

    private final List<ConditionConverter<S, T>> conditionConverters;
    private final List<Converter<S, T>> converters;

    /**
     * @param conditionConverters ConditionConverter 列表（已排序）
     * @param converters          普通 Converter 列表（已排序）
     */
    public ConverterSearchResult(List<ConditionConverter<S, T>> conditionConverters,
                                 List<Converter<S, T>> converters) {
        this.conditionConverters = conditionConverters != null ? conditionConverters : Collections.emptyList();
        this.converters = converters != null ? converters : Collections.emptyList();
    }

    public List<ConditionConverter<S, T>> getConditionConverters() {
        return conditionConverters;
    }

    /**
     * 返回普通 Converter 列表（已按排序规则排序）
     *
     * @return 普通 Converter 列表，不为 null
     */
    public List<Converter<S, T>> getConverters() {
        return converters;
    }

    /**
     * 返回第一个普通 Converter，或 null（无可用时）
     *
     * @return 第一个 Converter，或 null
     */
    public Converter<S, T> getConverter() {
        return converters.isEmpty() ? null : converters.get(0);
    }

    /**
     * 返回所有转换器合并列表（ConditionConverter 在前，普通 Converter 在后），
     * 两者各自已按排序规则排列。
     */
    public List<Converter<S, T>> getAllConverters() {
        List<Converter<S, T>> result = new ArrayList<>(
                conditionConverters.size() + converters.size());
        result.addAll(conditionConverters);
        result.addAll(converters);
        return result;
    }

    public boolean isEmpty() {
        return conditionConverters.isEmpty() && converters.isEmpty();
    }

    public int size() {
        return conditionConverters.size() + converters.size();
    }
}
