package com.tingfeng.util.java.base.math.base;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

/**
 * 带分数 Comparable 实现测试
 *
 * 覆盖 BigInteger 交叉相乘全序比较的正负符号约定与各类边界
 */
public class MixedNumberTest {

    @Test
    public void compareToPositive() {
        // 2 1/3 vs 2 1/4：同整数部分比较分数部分
        IntMixedNumber a = new IntMixedNumber(2, 1, 3);
        IntMixedNumber b = new IntMixedNumber(2, 1, 4);
        Assert.assertTrue("2 1/3 应大于 2 1/4", a.compareTo(b) > 0);
        Assert.assertTrue("2 1/4 应小于 2 1/3", b.compareTo(a) < 0);
        // 2 1/3 vs 2 2/3
        Assert.assertTrue("2 1/3 应小于 2 2/3", a.compareTo(new IntMixedNumber(2, 2, 3)) < 0);
    }

    @Test
    public void compareToNegativeVsPositive() {
        // -2 1/3 vs 2 1/3：符号约定（分数部分恒正，符号由整数部分决定）
        IntMixedNumber negative = new IntMixedNumber(-2, 1, 3);
        IntMixedNumber positive = new IntMixedNumber(2, 1, 3);
        Assert.assertTrue("-2 1/3 应小于 2 1/3", negative.compareTo(positive) < 0);
        Assert.assertTrue("2 1/3 应大于 -2 1/3", positive.compareTo(negative) > 0);
    }

    @Test
    public void compareToNegative() {
        // -2 1/3 vs -2 1/4：-7/3 < -9/4（-2.333 < -2.25）
        IntMixedNumber a = new IntMixedNumber(-2, 1, 3);
        IntMixedNumber b = new IntMixedNumber(-2, 1, 4);
        Assert.assertTrue("-2 1/3 应小于 -2 1/4", a.compareTo(b) < 0);
        Assert.assertTrue("-2 1/4 应大于 -2 1/3", b.compareTo(a) > 0);
    }

    @Test
    public void compareToEqual() {
        // 同值带分数比较相等
        IntMixedNumber a = new IntMixedNumber(2, 1, 3);
        IntMixedNumber b = new IntMixedNumber(2, 1, 3);
        Assert.assertEquals("同值带分数比较应为 0", 0, a.compareTo(b));
        // 负号：-2 1/3 vs -2 1/3
        Assert.assertEquals("同值负带分数比较应为 0", 0,
                new IntMixedNumber(-2, 1, 3).compareTo(new IntMixedNumber(-2, 1, 3)));
    }

    @Test
    public void compareToZeroWholePart() {
        // 整数部分为 0 时 signum()=0 → +num，与 toImproperNumerator wholePart>=0 分支一致
        IntMixedNumber half = new IntMixedNumber(0, 1, 2);
        IntMixedNumber third = new IntMixedNumber(0, 1, 3);
        Assert.assertTrue("0 1/2 应大于 0 1/3", half.compareTo(third) > 0);
        Assert.assertTrue("0 1/2 应小于 1 1/3", half.compareTo(new IntMixedNumber(1, 1, 3)) < 0);
        // 0 1/2 vs 0：正纯分数大于零
        Assert.assertTrue("0 1/2 应大于 0", half.compareTo(new IntMixedNumber(0, 0, 1)) > 0);
    }

    @Test
    public void compareToNegativeWholePart() {
        // -2 1/3 的假分数为 -7/3，应小于 0
        Assert.assertTrue("-2 1/3 应小于 0",
                new IntMixedNumber(-2, 1, 3).compareTo(new IntMixedNumber(0, 0, 1)) < 0);
        // -2 1/3 vs -3：-2.333 > -3
        Assert.assertTrue("-2 1/3 应大于 -3",
                new IntMixedNumber(-2, 1, 3).compareTo(new IntMixedNumber(-3, 0, 1)) > 0);
    }

    @Test
    public void compareToCrossType() {
        // 跨类型比较：Int vs Long vs BigInteger
        IntMixedNumber intMixed = new IntMixedNumber(2, 1, 3);
        LongMixedNumber longMixed = new LongMixedNumber(2L, 1L, 3L);
        BigMixedNumber bigMixed = new BigMixedNumber(BigInteger.valueOf(2), BigInteger.ONE, BigInteger.valueOf(3));
        Assert.assertEquals("Int 与 Long 同值比较应为 0", 0, intMixed.compareTo(longMixed));
        Assert.assertEquals("Int 与 BigInteger 同值比较应为 0", 0, intMixed.compareTo(bigMixed));
        Assert.assertEquals("Long 与 BigInteger 同值比较应为 0", 0, longMixed.compareTo(bigMixed));
        // 跨类型不同值
        BigMixedNumber bigNegative = new BigMixedNumber(BigInteger.valueOf(-2), BigInteger.ONE, BigInteger.valueOf(3));
        Assert.assertTrue("Int 正数应大于 BigInteger 负数", intMixed.compareTo(bigNegative) > 0);
    }

    @Test
    public void compareToBigIntegerRange() {
        // 超出 long 范围的整数部分仍可精确比较
        BigMixedNumber huge = new BigMixedNumber(new BigInteger("99999999999999999999999999"), BigInteger.ONE, BigInteger.valueOf(3));
        BigMixedNumber small = new BigMixedNumber(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(3));
        Assert.assertTrue("超大带分数应大于小带分数", huge.compareTo(small) > 0);
        Assert.assertTrue("小带分数应小于超大带分数", small.compareTo(huge) < 0);
        // 超大负数
        BigMixedNumber hugeNegative = new BigMixedNumber(new BigInteger("-99999999999999999999999999"), BigInteger.ONE, BigInteger.valueOf(3));
        Assert.assertTrue("超大负数应小于小正数", hugeNegative.compareTo(small) < 0);
    }

    @Test
    public void compareToNull() {
        try {
            new IntMixedNumber(2, 1, 3).compareTo(null);
            Assert.fail("应该抛出 NullPointerException");
        } catch (NullPointerException e) {
            Assert.assertTrue("异常信息应说明比较对象", e.getMessage().contains("null"));
        }
    }
}
