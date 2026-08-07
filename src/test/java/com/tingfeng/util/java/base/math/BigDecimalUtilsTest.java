package com.tingfeng.util.java.base.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Assert;
import org.junit.Test;

/**
 * BigDecimalUtils 单元测试
 */
public class BigDecimalUtilsTest {

    @Test
    public void safeDivideNormalTest() {
        // 精确除尽
        Assert.assertEquals(new BigDecimal("0.25"), BigDecimalUtils.safeDivide(new BigDecimal("1"), new BigDecimal("4"), 2, BigDecimal.ZERO));
        // 整数结果
        Assert.assertEquals(new BigDecimal("5.00"), BigDecimalUtils.safeDivide(new BigDecimal("10"), new BigDecimal("2"), 2, BigDecimal.ZERO));
        // 负数
        Assert.assertEquals(new BigDecimal("-0.50"), BigDecimalUtils.safeDivide(new BigDecimal("-1"), new BigDecimal("2"), 2, BigDecimal.ZERO));
        // 被除数为 0
        Assert.assertEquals(new BigDecimal("0.00"), BigDecimalUtils.safeDivide(BigDecimal.ZERO, new BigDecimal("3"), 2, BigDecimal.ZERO));
    }

    @Test
    public void safeDivideZeroDivisorFallbackTest() {
        // 除数为 0：返回 fallback（断言同一引用）
        BigDecimal fallback = new BigDecimal("-1");
        Assert.assertSame(fallback, BigDecimalUtils.safeDivide(new BigDecimal("1"), BigDecimal.ZERO, 2, fallback));
        // 非零字面量 0 值（如 "0.00"）同样识别为除零
        Assert.assertSame(fallback, BigDecimalUtils.safeDivide(new BigDecimal("1"), new BigDecimal("0.00"), 2, fallback));
        // 5 参版本除零同样返回 fallback
        Assert.assertSame(fallback, BigDecimalUtils.safeDivide(new BigDecimal("1"), BigDecimal.ZERO, 2, RoundingMode.HALF_UP, fallback));
    }

    @Test
    public void safeDivideHalfUpTest() {
        // 除不尽：1/3 = 0.333... → HALF_UP → 0.33
        Assert.assertEquals(new BigDecimal("0.33"), BigDecimalUtils.safeDivide(new BigDecimal("1"), new BigDecimal("3"), 2, BigDecimal.ZERO));
        // 1/6 = 0.1666... → HALF_UP → 0.17
        Assert.assertEquals(new BigDecimal("0.17"), BigDecimalUtils.safeDivide(new BigDecimal("1"), new BigDecimal("6"), 2, BigDecimal.ZERO));
        // 99.999/100 边界进位
        Assert.assertEquals(new BigDecimal("1.00"), BigDecimalUtils.safeDivide(new BigDecimal("99.999"), new BigDecimal("100"), 2, BigDecimal.ZERO));
    }

    @Test
    public void safeDivideWithModeTest() {
        BigDecimal one = BigDecimal.ONE;
        BigDecimal three = new BigDecimal("3");
        // FLOOR: 1/3 → 0.33（向下）；CEILING: 1/3 → 0.34（向上）
        Assert.assertEquals(new BigDecimal("0.33"), BigDecimalUtils.safeDivide(one, three, 2, RoundingMode.FLOOR, BigDecimal.ZERO));
        Assert.assertEquals(new BigDecimal("0.34"), BigDecimalUtils.safeDivide(one, three, 2, RoundingMode.CEILING, BigDecimal.ZERO));
        // DOWN: -1/3 = -0.333... → 向零舍入 → -0.33
        Assert.assertEquals(new BigDecimal("-0.33"), BigDecimalUtils.safeDivide(new BigDecimal("-1"), three, 2, RoundingMode.DOWN, BigDecimal.ZERO));
    }

    @Test(expected = IllegalArgumentException.class)
    public void safeDivideNullDividendTest() {
        BigDecimalUtils.safeDivide(null, BigDecimal.ONE, 2, BigDecimal.ZERO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void safeDivideNullDivisorTest() {
        BigDecimalUtils.safeDivide(BigDecimal.ONE, null, 2, BigDecimal.ZERO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void safeDivideNullFallbackTest() {
        BigDecimalUtils.safeDivide(BigDecimal.ONE, BigDecimal.TEN, 2, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void safeDivideNegativeScaleTest() {
        BigDecimalUtils.safeDivide(BigDecimal.ONE, BigDecimal.TEN, -1, BigDecimal.ZERO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void safeDivideWithModeNullModeTest() {
        BigDecimalUtils.safeDivide(BigDecimal.ONE, BigDecimal.TEN, 2, null, BigDecimal.ZERO);
    }
}
