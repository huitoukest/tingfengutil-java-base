package com.tingfeng.util.java.base.common.utils.version;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 版本解析结果，包含分段列表和自定义比较器映射。
 * 比较规则：
 * - 两边的 segments size 必须一致，否则抛 IAE
 * - 逐索引比较，同索引 comparable 标记必须一致，否则抛 IAE
 * - 跳过 comparable=false 的段
 * - 优先使用自定义比较器，无自定义比较器的索引使用默认类型比较
 * - 以左侧对象的 comparators 为准（防不对称问题）
 * <p><b>注意:</b> 此类为可变对象。{@link #putComparator(int, Comparator)} 和
 * {@link #putAllComparators(Map)} 会修改当前实例的 comparators 配置。
 * 若需独立副本，请使用 {@link #copy()} 方法。</p>
 */
public class VersionParsedResult implements Comparable<VersionParsedResult> {

    private final List<VersionSegment> segments;

    /**
     * 自定义比较器映射 key=索引, value=比较器。
     * 以左侧 VersionParsedResult 上的 comparators 为准。
     */
    private final Map<Integer, Comparator<VersionSegment>> comparators = new HashMap<>();

    /**
     * 构造解析结果。
     *
     * @param segments 分段列表
     */
    public VersionParsedResult(List<VersionSegment> segments) {
        this.segments = segments;
    }

    /**
     * 获取第一个 comparable=true 的段。
     * 当所有 segment 的 comparable 均为 false 时返回 null。
     *
     * @return 第一个可比段，全部不可比时返回 null
     */
    public VersionSegment getFirst() {
        return segments.stream()
                .filter(VersionSegment::isComparable)
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取最后一个 comparable=true 的段。
     * 当所有 segment 的 comparable 均为 false 时返回 null。
     *
     * @return 最后一个可比段，全部不可比时返回 null
     */
    public VersionSegment getLast() {
        for (int i = segments.size() - 1; i >= 0; i--) {
            if (segments.get(i).isComparable()) {
                return segments.get(i);
            }
        }
        return null;
    }

    /**
     * 分段总数。
     *
     * @return 分段数量
     */
    public int size() {
        return segments.size();
    }

    /**
     * 获取分段列表。
     *
     * @return 分段列表
     */
    public List<VersionSegment> getSegments() {
        return segments;
    }

    /**
     * 设置指定索引的自定义比较器。
     * <p><b>注意:</b> 此方法会修改当前实例。若需保留原实例的 comparators 配置，
     * 请先调用 {@link #copy()} 创建副本后再设置。</p>
     *
     * @param index      分段索引
     * @param comparator 自定义比较器
     */
    public void putComparator(int index, Comparator<VersionSegment> comparator) {
        comparators.put(index, comparator);
    }

    /**
     * 批量设置自定义比较器。
     * <p><b>注意:</b> 此方法会修改当前实例。若需保留原实例的 comparators 配置，
     * 请先调用 {@link #copy()} 创建副本后再设置。</p>
     *
     * @param map 索引到比较器的映射
     */
    public void putAllComparators(Map<Integer, Comparator<VersionSegment>> map) {
        comparators.putAll(map);
    }

    /**
     * 获取自定义比较器映射。
     *
     * @return 自定义比较器映射
     */
    public Map<Integer, Comparator<VersionSegment>> getComparators() {
        return comparators;
    }

    /**
     * 创建当前解析结果的独立副本。
     * <p>副本拥有独立的 comparators 映射和 segments 列表，
     * 修改副本的 comparators 不会影响原实例。</p>
     *
     * @return 当前结果的深度独立副本
     */
    public VersionParsedResult copy() {
        VersionParsedResult cloned = new VersionParsedResult(new ArrayList<>(segments));
        cloned.comparators.putAll(this.comparators);
        return cloned;
    }

    /**
     * 比较逻辑：以 this.comparators 为准（左侧优先）。
     * 规则：
     * 1. size 必须一致，否则抛 IAE
     * 2. 逐索引比较
     *    a. comparable 标记一致校验
     *    b. comparable=false 则跳过
     *    c. 有自定义比较器则使用自定义
     *    d. 无自定义比较器则使用 segment.compareTo()
     * 3. 全部相等返回 0
     *
     * @param other 要比较的另一个解析结果
     * @return 比较结果：负数、零、正数
     * @throws IllegalArgumentException 若 size 不一致、或 comparable 标记不一致
     */
    @Override
    public int compareTo(VersionParsedResult other) {
        return compareTo(other, this.comparators);
    }

    /**
     * 使用指定比较器映射进行比较。
     * 规则同 {@link #compareTo(VersionParsedResult)}，但使用传入的 comparators。
     *
     * @param other      要比较的另一个解析结果
     * @param comparators 自定义比较器映射
     * @return 比较结果：负数、零、正数
     * @throws IllegalArgumentException 若 size 不一致、或 comparable 标记不一致
     */
    public int compareTo(VersionParsedResult other, Map<Integer, Comparator<VersionSegment>> comparators) {
        // 1. size 必须一致
        if (this.segments.size() != other.segments.size()) {
            throw new IllegalArgumentException("Segment count mismatch: "
                    + this.segments.size() + " vs " + other.segments.size());
        }

        // 2. 逐段比较
        for (int i = 0; i < segments.size(); i++) {
            VersionSegment s1 = this.segments.get(i);
            VersionSegment s2 = other.segments.get(i);

            if (s1.isComparable() != s2.isComparable()) {
                throw new IllegalArgumentException("Comparable flag mismatch at index " + i);
            }
            if (!s1.isComparable()) {
                continue;
            }

            // 优先使用自定义比较器
            Comparator<VersionSegment> custom = comparators.get(i);
            if (custom != null) {
                int cmp = custom.compare(s1, s2);
                if (cmp != 0) {
                    return cmp;
                }
            } else {
                int cmp = s1.compareTo(s2);
                if (cmp != 0) {
                    return cmp;
                }
            }
        }
        return 0;
    }
}
