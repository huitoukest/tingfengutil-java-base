package com.tingfeng.util.java.base.math;

import org.junit.Assert;
import org.junit.Test;

/**
 * NumberUtils 单元测试
 */
public class NumberUtilsTest {

    @Test
    public void isNumberTest() {
        // 合法数字
        Assert.assertTrue(NumberUtils.isNumber("123"));
        Assert.assertTrue(NumberUtils.isNumber("+123"));
        Assert.assertTrue(NumberUtils.isNumber("-123"));
        Assert.assertTrue(NumberUtils.isNumber("0"));
        Assert.assertTrue(NumberUtils.isNumber("12.34"));
        Assert.assertTrue(NumberUtils.isNumber("-12.34"));
        Assert.assertTrue(NumberUtils.isNumber("0.5"));
        // 超长整数不限制长度
        Assert.assertTrue(NumberUtils.isNumber("123456789012345678901234567890"));
        // null 与空串
        Assert.assertFalse(NumberUtils.isNumber(null));
        Assert.assertFalse(NumberUtils.isNumber(""));
        // 非法格式
        Assert.assertFalse(NumberUtils.isNumber("abc"));
        Assert.assertFalse(NumberUtils.isNumber("12a"));
        Assert.assertFalse(NumberUtils.isNumber("12."));
        Assert.assertFalse(NumberUtils.isNumber(".5"));
        Assert.assertFalse(NumberUtils.isNumber("1e5"));
        Assert.assertFalse(NumberUtils.isNumber("1.2.3"));
        Assert.assertFalse(NumberUtils.isNumber("+"));
        Assert.assertFalse(NumberUtils.isNumber("-"));
        Assert.assertFalse(NumberUtils.isNumber("12 34"));
    }

    @Test
    public void roundTest() {
        Assert.assertEquals(3.14, NumberUtils.round(3.14159, 2), 1e-9);
        Assert.assertEquals(3.14, NumberUtils.round(3.135, 2), 1e-9);
        Assert.assertEquals(3.0, NumberUtils.round(3.0, 2), 1e-9);
        Assert.assertEquals(0.0, NumberUtils.round(0.0, 5), 1e-9);
        Assert.assertEquals(-3.14, NumberUtils.round(-3.14159, 2), 1e-9);
        Assert.assertEquals(12345.68, NumberUtils.round(12345.6789, 2), 1e-9);
        // HALF_UP 进位
        Assert.assertEquals(100.0, NumberUtils.round(99.999, 2), 1e-9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void roundNegativeScaleTest() {
        // SS10: scale < 0 显式抛异常
        NumberUtils.round(3.14159, -1);
    }

    @Test
    public void approxEqualsTest() {
        // 相等与误差内
        Assert.assertTrue(NumberUtils.approxEquals(1.0, 1.0, 1e-6));
        Assert.assertTrue(NumberUtils.approxEquals(1.0, 1.0000001, 1e-6));
        Assert.assertTrue(NumberUtils.approxEquals(1.0000001, 1.0, 1e-6));
        // 边界：|a - b| == epsilon（整数精确值）
        Assert.assertTrue(NumberUtils.approxEquals(1.0, 2.0, 1.0));
        Assert.assertFalse(NumberUtils.approxEquals(1.0, 2.0, 0.5));
        // 误差外
        Assert.assertFalse(NumberUtils.approxEquals(1.0, 1.001, 1e-6));
        // 双 NaN 视为相等
        Assert.assertTrue(NumberUtils.approxEquals(Double.NaN, Double.NaN, 1e-6));
        // NaN 与有限值不等
        Assert.assertFalse(NumberUtils.approxEquals(Double.NaN, 1.0, 1e-6));
        Assert.assertFalse(NumberUtils.approxEquals(1.0, Double.NaN, 1e-6));
        // 无穷：同号相等短路
        Assert.assertTrue(NumberUtils.approxEquals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1e-6));
        Assert.assertTrue(NumberUtils.approxEquals(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 1e-6));
        // 正负无穷不等；无穷与有限值不等
        Assert.assertFalse(NumberUtils.approxEquals(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 1e-6));
        Assert.assertFalse(NumberUtils.approxEquals(Double.POSITIVE_INFINITY, 1.0, 1e-6));
    }

    @Test(expected = IllegalArgumentException.class)
    public void approxEqualsZeroEpsilonTest() {
        NumberUtils.approxEquals(1.0, 1.0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void approxEqualsNegativeEpsilonTest() {
        NumberUtils.approxEquals(1.0, 1.0, -0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void approxEqualsNanEpsilonTest() {
        NumberUtils.approxEquals(1.0, 1.0, Double.NaN);
    }

    @Test
    public void percentOfTest() {
        // 正常
        Assert.assertEquals(25.0, NumberUtils.percentOf(50.0, 200.0), 1e-9);
        Assert.assertEquals(0.0, NumberUtils.percentOf(0.0, 200.0), 1e-9);
        // 负数占比
        Assert.assertEquals(-25.0, NumberUtils.percentOf(-50.0, 200.0), 1e-9);
        // 超过 100%
        Assert.assertEquals(150.0, NumberUtils.percentOf(3.0, 2.0), 1e-9);
        // 除零：total == 0 返回 NaN（含 -0.0 与 0/0）
        Assert.assertTrue(Double.isNaN(NumberUtils.percentOf(1.0, 0.0)));
        Assert.assertTrue(Double.isNaN(NumberUtils.percentOf(1.0, -0.0)));
        Assert.assertTrue(Double.isNaN(NumberUtils.percentOf(0.0, 0.0)));
    }
}
