package com.tingfeng.util.java.base.common.utils.version;

import java.util.Date;

/**
 * 版本号的一个分段。
 * 包含原始字符串、推断后的值（Long/Date/String）、以及是否参与比较的标记。
 * comparable=false 的段在 {@link VersionParsedResult#compareTo} 中被跳过。
 */
public class VersionSegment implements Comparable<VersionSegment> {

    /** 推断后的值：Long / Date / String */
    private final Object value;

    /** 原始字符串 */
    private final String raw;

    /** 是否参与比较，默认 true */
    private final boolean comparable;

    /**
     * 构造版本分段。
     *
     * @param value      推断后的值（Long / Date / String）
     * @param raw        原始字符串
     * @param comparable 是否参与比较
     */
    public VersionSegment(Object value, String raw, boolean comparable) {
        this.value = value;
        this.raw = raw;
        this.comparable = comparable;
    }

    /**
     * 获取推断后的值。
     *
     * @return 推断后的值（Long / Date / String）
     */
    public Object getValue() {
        return value;
    }

    /**
     * 获取原始字符串。
     *
     * @return 原始字符串
     */
    public String getRaw() {
        return raw;
    }

    /**
     * 判断是否纳入比较。
     *
     * @return true 表示参与比较
     */
    public boolean isComparable() {
        return comparable;
    }

    /**
     * 比较规则：同索引必须类型相同。
     * comparable=false 的段在此处返回 0，不参与比较；comparable 不一致则抛出异常。
     *
     * @param other 要比较的另一个分段
     * @return 比较结果：负数、零、正数
     * @throws IllegalArgumentException 若类型不匹配
     */
    @Override
    public int compareTo(VersionSegment other) {
        if (this.comparable != other.comparable) {
            throw new IllegalArgumentException("Comparable flag mismatch");
        }
        if (!this.comparable) {
            return 0;
        }
        if (this.value.getClass() != other.value.getClass()) {
            throw new IllegalArgumentException("Type mismatch: "
                    + this.value.getClass() + " vs " + other.value.getClass());
        }
        if (value instanceof Long) {
            return Long.compare((Long) value, (Long) other.value);
        } else if (value instanceof Date) {
            return ((Date) value).compareTo((Date) other.value);
        } else {
            return ((String) value).compareTo((String) other.value);
        }
    }

    /**
     * 返回原始字符串表示。
     *
     * @return 原始字符串
     */
    @Override
    public String toString() {
        return raw;
    }
}
