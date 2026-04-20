package com.tingfeng.util.java.base.bean.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 转换器查找结果封装
 * <p>
 * 将 ConditionConverter 和普通 Converter 分类返回，
 * 提供 getAllConverters() 方法按优先级（ConditionConverter -> Converter）返回所有可用转换器。
 */
public class ConverterSearchResult<S, T> {

    private final List<ConditionConverter<S, T>> conditionConverters;
    private final Converter<S, T> converter;

    public ConverterSearchResult(List<ConditionConverter<S, T>> conditionConverters, Converter<S, T> converter) {
        this.conditionConverters = conditionConverters != null ? conditionConverters : Collections.emptyList();
        this.converter = converter;
    }

    public List<ConditionConverter<S, T>> getConditionConverters() {
        return conditionConverters;
    }

    public Converter<S, T> getConverter() {
        return converter;
    }

    /**
     * 返回所有转换器（按优先级：ConditionConverter 先于普通 Converter）
     */
    public List<Converter<S, T>> getAllConverters() {
        List<Converter<S, T>> result = new ArrayList<>(conditionConverters.size() + (converter != null ? 1 : 0));
        result.addAll(conditionConverters);
        if (converter != null) {
            result.add(converter);
        }
        return result;
    }

    public boolean isEmpty() {
        return conditionConverters.isEmpty() && converter == null;
    }

    public int size() {
        return conditionConverters.size() + (converter != null ? 1 : 0);
    }
}
