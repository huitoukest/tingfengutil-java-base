package com.tingfeng.util.java.base.bean.copier;

import com.tingfeng.util.java.base.bean.converter.Converter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Bean拷贝配置选项，用于封装拷贝策略。
 *
 * 提供链式API以灵活配置拷贝行为。
 *
 * @author huitoukest
 */
public class CopyOptions {

    /** 是否忽略null值，默认为false */
    private boolean ignoreNull = false;

    /** 要忽略的属性名称集合，默认为空集合 */
    private Set<String> ignoreProperties = new HashSet<>();

    /** 字段映射：sourceField -> targetField，默认为null（无映射） */
    private Map<String, String> fieldMapping = null;

    /** 是否强制使用Field访问（绕过getter/setter），默认为false */
    private boolean forceFieldAccess = false;

    /** 是否使用转换器，默认为true */
    private boolean useConverter = true;

    /** 是否忽略属性名大小写，默认为false */
    private boolean ignoreCase = false;

    /** 转换器不匹配时是否忽略错误，默认为true */
    private boolean ignoreNoMatchConverterError = true;

    /** 是否拷贝父类属性，默认为true */
    private boolean copySuperclassProperties = true;

    /** 临时转换器列表，优先级高于全局ConverterRegistry */
    private List<Converter<?, ?>> customConverters = null;

    /**
     * 私有构造器，禁止外部直接实例化
     */
    private CopyOptions() {
    }

    /**
     * 创建默认的CopyOptions实例。
     *
     * @return 新的CopyOptions实例（非单例）
     */
    public static CopyOptions create() {
        return new CopyOptions();
    }

    /**
     * 设置是否忽略null值。
     *
     * @param ignoreNull 是否忽略null
     * @return this
     */
    public CopyOptions setIgnoreNull(boolean ignoreNull) {
        this.ignoreNull = ignoreNull;
        return this;
    }

    /**
     * 设置要忽略的属性集合（Collection方式）。
     *
     * @param ignoreProperties 要忽略的属性名称集合
     * @return this
     */
    public CopyOptions setIgnoreProperties(Collection<String> ignoreProperties) {
        this.ignoreProperties = new HashSet<>(ignoreProperties);
        return this;
    }

    /**
     * 设置要忽略的属性集合（可变参数方式）。
     *
     * @param ignoreProperties 要忽略的属性名称
     * @return this
     */
    public CopyOptions setIgnoreProperties(String... ignoreProperties) {
        this.ignoreProperties = new HashSet<>();
        for (String property : ignoreProperties) {
            this.ignoreProperties.add(property);
        }
        return this;
    }

    /**
     * 设置字段映射。
     *
     * @param fieldMapping 字段映射关系（sourceField -> targetField）
     * @return this
     */
    public CopyOptions setFieldMapping(Map<String, String> fieldMapping) {
        this.fieldMapping = fieldMapping;
        return this;
    }

    /**
     * 设置是否强制使用Field访问（绕过getter/setter）。
     *
     * @param forceFieldAccess 是否强制使用Field访问
     * @return this
     */
    public CopyOptions setForceFieldAccess(boolean forceFieldAccess) {
        this.forceFieldAccess = forceFieldAccess;
        return this;
    }

    /**
     * 设置是否使用转换器。
     *
     * @param useConverter 是否使用转换器
     * @return this
     */
    public CopyOptions setUseConverter(boolean useConverter) {
        this.useConverter = useConverter;
        return this;
    }

    /**
     * 设置是否忽略属性名大小写。
     *
     * @param ignoreCase 是否忽略大小写
     * @return this
     */
    public CopyOptions setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        return this;
    }

    /**
     * 设置转换器不匹配时是否忽略错误。
     *
     * @param ignoreNoMatchConverterError 是否忽略转换器不匹配错误
     * @return this
     */
    public CopyOptions setIgnoreNoMatchConverterError(boolean ignoreNoMatchConverterError) {
        this.ignoreNoMatchConverterError = ignoreNoMatchConverterError;
        return this;
    }

    /**
     * 设置是否拷贝父类属性。
     *
     * @param copySuperclassProperties 是否拷贝父类属性
     * @return this
     */
    public CopyOptions setCopySuperclassProperties(boolean copySuperclassProperties) {
        this.copySuperclassProperties = copySuperclassProperties;
        return this;
    }

    /**
     * 设置临时转换器列表（可变参数方式）。
     *
     * 如果 converters 为 null 或空则清空列表。
     *
     * @param converters 临时转换器数组
     * @return this（支持链式调用）
     */
    public CopyOptions setCustomConverters(Converter<?, ?>... converters) {
        if (converters == null || converters.length == 0) {
            this.customConverters = null;
        } else {
            this.customConverters = Arrays.asList(converters);
        }
        return this;
    }

    /**
     * 获取是否忽略null值。
     *
     * @return 是否忽略null
     */
    public boolean isIgnoreNull() {
        return ignoreNull;
    }

    /**
     * 获取要忽略的属性名称集合（不可变副本）。
     *
     * @return 要忽略的属性名称集合（永远不为null）
     */
    public Set<String> getIgnoreProperties() {
        return Collections.unmodifiableSet(ignoreProperties);
    }

    /**
     * 获取字段映射（不可变副本或null）。
     *
     * @return 字段映射关系，若无映射则返回null
     */
    public Map<String, String> getFieldMapping() {
        if (fieldMapping == null) {
            return null;
        }
        return Collections.unmodifiableMap(fieldMapping);
    }

    /**
     * 获取是否强制使用Field访问。
     *
     * @return 是否强制使用Field访问
     */
    public boolean isForceFieldAccess() {
        return forceFieldAccess;
    }

    /**
     * 获取是否使用转换器。
     *
     * @return 是否使用转换器
     */
    public boolean isUseConverter() {
        return useConverter;
    }

    /**
     * 获取是否忽略属性名大小写。
     *
     * @return 是否忽略大小写
     */
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    /**
     * 获取转换器不匹配时是否忽略错误。
     *
     * @return 是否忽略转换器不匹配错误
     */
    public boolean isIgnoreNoMatchConverterError() {
        return ignoreNoMatchConverterError;
    }

    /**
     * 获取是否拷贝父类属性。
     *
     * @return 是否拷贝父类属性
     */
    public boolean isCopySuperclassProperties() {
        return copySuperclassProperties;
    }

    /**
     * 获取临时转换器列表（不可变副本或空列表）。
     *
     * @return 临时转换器列表，无临时转换器时返回空列表
     */
    public List<Converter<?, ?>> getCustomConverters() {
        if (customConverters == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(customConverters);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CopyOptions that = (CopyOptions) o;
        return ignoreNull == that.ignoreNull
                && forceFieldAccess == that.forceFieldAccess
                && useConverter == that.useConverter
                && ignoreCase == that.ignoreCase
                && ignoreNoMatchConverterError == that.ignoreNoMatchConverterError
                && copySuperclassProperties == that.copySuperclassProperties
                && Objects.equals(ignoreProperties, that.ignoreProperties)
                && Objects.equals(fieldMapping, that.fieldMapping)
                && Objects.equals(customConverters, that.customConverters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ignoreNull, ignoreProperties, fieldMapping, forceFieldAccess, useConverter,
                ignoreCase, ignoreNoMatchConverterError, copySuperclassProperties, customConverters);
    }
}