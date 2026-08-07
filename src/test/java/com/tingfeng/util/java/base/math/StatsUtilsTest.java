package com.tingfeng.util.java.base.math;

import org.junit.Assert;
import org.junit.Test;

/**
 * StatsUtils 单元测试
 */
public class StatsUtilsTest {

    @Test
    public void medianTest() {
        // 奇数个元素: 取排序后中间值
        Assert.assertEquals(3.0, StatsUtils.median(new double[]{5, 3, 1, 4, 2}), 1e-9);
        // 偶数个元素: 取两中位均值
        Assert.assertEquals(2.5, StatsUtils.median(new double[]{1, 2, 3, 4}), 1e-9);
        // 单元素
        Assert.assertEquals(7.0, StatsUtils.median(new double[]{7}), 1e-9);
        // 含重复值
        Assert.assertEquals(2.0, StatsUtils.median(new double[]{1, 2, 2, 3, 4}), 1e-9);
    }

    @Test
    public void medianShouldNotModifyInputTest() {
        double[] values = {5, 3, 1, 4, 2};
        StatsUtils.median(values);
        Assert.assertArrayEquals(new double[]{5, 3, 1, 4, 2}, values, 1e-9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void medianNullTest() {
        StatsUtils.median(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void medianEmptyTest() {
        StatsUtils.median(new double[0]);
    }

    @Test
    public void varianceTest() {
        // 经典 8 值集: mean=5, Σ(x-mean)²=32
        double[] values = {2, 4, 4, 4, 5, 5, 7, 9};
        // 总体方差 = 32/8 = 4
        Assert.assertEquals(4.0, StatsUtils.variance(values, false), 1e-9);
        // 样本方差 = 32/7
        Assert.assertEquals(32.0 / 7.0, StatsUtils.variance(values, true), 1e-9);
        // 常量数组方差恒为 0
        Assert.assertEquals(0.0, StatsUtils.variance(new double[]{3, 3, 3}, false), 1e-9);
        // 总体方差单元素为 0
        Assert.assertEquals(0.0, StatsUtils.variance(new double[]{42.0}, false), 1e-9);
    }

    @Test
    public void varianceWelfordLargeValuesTest() {
        // 大数场景: 朴素两遍算法（Σx² 先求和）会在 double 中丢失低位精度,
        // Welford 一遍在线算法不受影响: mean=1e12+0.5, m2=0.5
        double[] values = {1e12, 1e12 + 1};
        Assert.assertEquals(0.25, StatsUtils.variance(values, false), 1e-9);
        Assert.assertEquals(0.5, StatsUtils.variance(values, true), 1e-9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void varianceNullTest() {
        StatsUtils.variance(null, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void varianceEmptyTest() {
        StatsUtils.variance(new double[0], true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void varianceSampleLessThanTwoTest() {
        // 样本方差 n<2 数学上未定义
        StatsUtils.variance(new double[]{1.0}, true);
    }

    @Test
    public void stddevTest() {
        // sqrt(32/8) = 2
        Assert.assertEquals(2.0, StatsUtils.stddev(new double[]{2, 4, 4, 4, 5, 5, 7, 9}, false), 1e-9);
        // sqrt(32/7)
        Assert.assertEquals(Math.sqrt(32.0 / 7.0), StatsUtils.stddev(new double[]{2, 4, 4, 4, 5, 5, 7, 9}, true), 1e-9);
        // 单元素总体标准差为 0
        Assert.assertEquals(0.0, StatsUtils.stddev(new double[]{5.0}, false), 1e-9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void stddevSampleLessThanTwoTest() {
        StatsUtils.stddev(new double[]{1.0}, true);
    }

    @Test
    public void percentileTest() {
        double[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        // P0 与 P100 边界
        Assert.assertEquals(1.0, StatsUtils.percentile(values, 0), 1e-9);
        Assert.assertEquals(10.0, StatsUtils.percentile(values, 100), 1e-9);
        // P25: position = 0.25*9 = 2.25 → 3 + 0.25*(4-3) = 3.25
        Assert.assertEquals(3.25, StatsUtils.percentile(values, 25), 1e-9);
        // P50: position = 0.5*9 = 4.5 → 5 + 0.5*(6-5) = 5.5
        Assert.assertEquals(5.5, StatsUtils.percentile(values, 50), 1e-9);
        // P90: position = 0.9*9 = 8.1 → 9 + 0.1*(10-9) = 9.1
        Assert.assertEquals(9.1, StatsUtils.percentile(values, 90), 1e-9);
        // P99: position = 0.99*9 = 8.91 → 9 + 0.91*(10-9) = 9.91
        Assert.assertEquals(9.91, StatsUtils.percentile(values, 99), 1e-9);
        // 单元素任意 p 均为该元素
        Assert.assertEquals(7.0, StatsUtils.percentile(new double[]{7}, 60), 1e-9);
        // 与中位数一致性: 奇数长度 P50 = median
        Assert.assertEquals(3.0, StatsUtils.percentile(new double[]{1, 2, 3, 4, 5}, 50), 1e-9);
        // 偶数长度 P50 = 两中位均值
        Assert.assertEquals(2.5, StatsUtils.percentile(new double[]{1, 2, 3, 4}, 50), 1e-9);
    }

    @Test
    public void percentileShouldNotModifyInputTest() {
        double[] values = {5, 3, 1, 4, 2};
        StatsUtils.percentile(values, 50);
        Assert.assertArrayEquals(new double[]{5, 3, 1, 4, 2}, values, 1e-9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void percentileNullTest() {
        StatsUtils.percentile(null, 50);
    }

    @Test(expected = IllegalArgumentException.class)
    public void percentileEmptyTest() {
        StatsUtils.percentile(new double[0], 50);
    }

    @Test(expected = IllegalArgumentException.class)
    public void percentileNegativePTest() {
        StatsUtils.percentile(new double[]{1, 2, 3}, -0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void percentileTooLargePTest() {
        StatsUtils.percentile(new double[]{1, 2, 3}, 100.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void percentileNaNTest() {
        StatsUtils.percentile(new double[]{1, 2, 3}, Double.NaN);
    }

    @Test
    public void weightedAverageTest() {
        // (70*1 + 80*2 + 90*3) / (1+2+3) = 500/6
        Assert.assertEquals(500.0 / 6.0, StatsUtils.weightedAverage(new double[]{70, 80, 90}, new double[]{1, 2, 3}), 1e-9);
        // 允许单个权重为 0: (70*0 + 80*2 + 90*3) / 5 = 86
        Assert.assertEquals(86.0, StatsUtils.weightedAverage(new double[]{70, 80, 90}, new double[]{0, 2, 3}), 1e-9);
        // 单元素
        Assert.assertEquals(5.0, StatsUtils.weightedAverage(new double[]{5}, new double[]{3}), 1e-9);
        // 等权重退化为算术平均
        Assert.assertEquals(3.0, StatsUtils.weightedAverage(new double[]{2, 3, 4}, new double[]{1, 1, 1}), 1e-9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void weightedAverageNullValuesTest() {
        StatsUtils.weightedAverage(null, new double[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void weightedAverageNullWeightsTest() {
        StatsUtils.weightedAverage(new double[]{1}, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void weightedAverageLengthMismatchTest() {
        StatsUtils.weightedAverage(new double[]{1, 2}, new double[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void weightedAverageEmptyTest() {
        StatsUtils.weightedAverage(new double[0], new double[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void weightedAverageNegativeWeightTest() {
        StatsUtils.weightedAverage(new double[]{1, 2}, new double[]{1, -1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void weightedAverageNaNWeightTest() {
        StatsUtils.weightedAverage(new double[]{1, 2}, new double[]{1, Double.NaN});
    }

    @Test(expected = IllegalArgumentException.class)
    public void weightedAverageAllZeroWeightTest() {
        // 全零权重: 总权重为 0 时数学上未定义
        StatsUtils.weightedAverage(new double[]{1, 2}, new double[]{0, 0});
    }
}
