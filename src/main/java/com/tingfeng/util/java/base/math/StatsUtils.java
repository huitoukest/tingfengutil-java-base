package com.tingfeng.util.java.base.math;

import java.util.Arrays;

/**
 * 基础统计工具类：中位数、方差、标准差、百分位、加权平均
 *
 * 全部方法以 double[] 作为输入；int/long 数组可经
 * Arrays.stream(ints).asDoubleStream().toArray() 转换后调用。
 *
 * 精度说明: 方差与标准差采用 Welford 一遍在线算法，只维护均值与
 * 二阶中心矩累加量，避免朴素两遍算法（Σx² - (Σx)²/n）在大数值场景下
 * 因 double 精度不足丢失低位导致的漂移。
 */
public final class StatsUtils {

    private StatsUtils() {}

    /**
     * 计算中位数
     *
     * 先拷贝后排序，不污染入参数组；奇数个元素取排序后中间值，
     * 偶数个元素取两个中间值的算术平均。
     *
     * @param values 数值数组，非 null 且长度大于 0
     * @return 中位数
     * @throws IllegalArgumentException values 为 null 或空数组时抛出
     */
    public static double median(double[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("values must not be null or empty");
        }
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        int mid = sorted.length / 2;
        if (sorted.length % 2 == 1) {
            return sorted[mid];
        }
        return (sorted[mid - 1] + sorted[mid]) / 2.0;
    }

    /**
     * 计算方差
     *
     * 采用 Welford 一遍在线算法（防大数减小数精度漂移），结果经钳制
     * 保证非负（浮点尾差可能产生极小负值）。
     *
     * @param values 数值数组，非 null 且长度大于 0
     * @param isSample true 表示样本方差（除以 n-1），false 表示总体方差（除以 n）
     * @return 方差；单元素数组按总体方差计算时为 0.0
     * @throws IllegalArgumentException values 为 null 或空数组、或 isSample 为 true 且元素少于 2 个时抛出
     */
    public static double variance(double[] values, boolean isSample) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("values must not be null or empty");
        }
        if (isSample && values.length < 2) {
            throw new IllegalArgumentException("sample variance requires at least 2 values, but got " + values.length);
        }
        if (values.length == 1) {
            // 样本方差 n<2 已在上面拒绝；单元素总体方差按数学定义恒为 0
            return 0.0;
        }
        int count = 0;
        double mean = 0.0;
        double m2 = 0.0;
        for (double value : values) {
            count++;
            double delta = value - mean;
            mean += delta / count;
            m2 += delta * (value - mean);
        }
        double result = isSample ? m2 / (values.length - 1) : m2 / values.length;
        return result < 0 ? 0.0 : result;
    }

    /**
     * 计算标准差
     *
     * 即方差的开平方，算法与边界语义与 {@link #variance(double[], boolean)} 一致。
     *
     * @param values 数值数组，非 null 且长度大于 0
     * @param isSample true 表示样本标准差，false 表示总体标准差
     * @return 标准差；单元素数组按总体标准差计算时为 0.0
     * @throws IllegalArgumentException values 为 null 或空数组、或 isSample 为 true 且元素少于 2 个时抛出
     */
    public static double stddev(double[] values, boolean isSample) {
        return Math.sqrt(variance(values, isSample));
    }

    /**
     * 计算百分位数（R-7 线性插值，与 Excel PERCENTILE 一致）
     *
     * 排序后按 position = p / 100 * (n - 1) 定位，整数部分两侧线性插值。
     * p=50 时与 {@link #median(double[])} 结果一致。
     *
     * @param values 数值数组，非 null 且长度大于 0
     * @param p 百分位，取值范围 [0, 100]
     * @return 百分位数
     * @throws IllegalArgumentException values 为 null 或空数组、或 p 超出 [0, 100]（含 NaN）时抛出
     */
    public static double percentile(double[] values, double p) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("values must not be null or empty");
        }
        if (Double.isNaN(p) || p < 0 || p > 100) {
            throw new IllegalArgumentException("p must be in [0, 100]: " + p);
        }
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        double position = p / 100.0 * (sorted.length - 1);
        int lower = (int) Math.floor(position);
        double frac = position - lower;
        if (lower + 1 < sorted.length) {
            return sorted[lower] + frac * (sorted[lower + 1] - sorted[lower]);
        }
        return sorted[lower];
    }

    /**
     * 计算加权平均
     *
     * 结果 = Σ(v * w) / Σw；单个权重允许为 0，但总权重必须大于 0。
     *
     * @param values 数值数组，非 null 且长度大于 0
     * @param weights 权重数组，非 null、与 values 等长、元素非负且非 NaN、总权重大于 0
     * @return 加权平均值
     * @throws IllegalArgumentException 任一数组为 null、长度不等或为空、权重为负或 NaN、总权重不大于 0 时抛出
     */
    public static double weightedAverage(double[] values, double[] weights) {
        if (values == null || weights == null) {
            throw new IllegalArgumentException("values and weights must not be null");
        }
        if (values.length == 0 || values.length != weights.length) {
            throw new IllegalArgumentException("values and weights must be non-empty and have the same length: "
                    + "values=" + values.length + ", weights=" + weights.length);
        }
        double weightSum = 0.0;
        double weightedSum = 0.0;
        for (int i = 0; i < values.length; i++) {
            double weight = weights[i];
            if (weight < 0 || Double.isNaN(weight)) {
                throw new IllegalArgumentException("weight must not be negative or NaN at index " + i + ": " + weight);
            }
            weightSum += weight;
            weightedSum += values[i] * weight;
        }
        if (weightSum <= 0) {
            throw new IllegalArgumentException("total weight must be positive: " + weightSum);
        }
        return weightedSum / weightSum;
    }
}
