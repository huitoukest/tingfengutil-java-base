package com.tingfeng.util.java.base.math.base;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

/**
 * 分数系统的详细测试类
 * 测试边界情况、溢出保护、分数化简、带分数转换等功能
 *
 * @author huitoukest
 */
public class FractionDetailedTest {

    /**
     * 测试边界值
     */
    @Test
    public void testBoundaryValues() {
        // 测试 Integer.MIN_VALUE 和 Integer.MAX_VALUE
        IntFraction minInt = new IntFraction(Integer.MIN_VALUE, 1);
        IntFraction maxInt = new IntFraction(Integer.MAX_VALUE, 1);

        Assert.assertEquals("最小值应该是负数", Integer.MIN_VALUE, minInt.getNumerator().intValue());
        Assert.assertEquals("最大值应该是正数", Integer.MAX_VALUE, maxInt.getNumerator().intValue());

        // 测试 Long.MIN_VALUE 和 Long.MAX_VALUE
        LongFraction minLong = new LongFraction(Long.MIN_VALUE, 1);
        LongFraction maxLong = new LongFraction(Long.MAX_VALUE, 1);

        Assert.assertEquals("最小值应该是负数", Long.MIN_VALUE, minLong.getNumerator().longValue());
        Assert.assertEquals("最大值应该是正数", Long.MAX_VALUE, maxLong.getNumerator().longValue());

        // 测试 BigInteger 边界值
        BigInteger bigMin = BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.TEN);
        BigInteger bigMax = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN);
        BigFraction bigFractionMin = new BigFraction(bigMin, BigInteger.ONE);
        BigFraction bigFractionMax = new BigFraction(bigMax, BigInteger.ONE);

        Assert.assertEquals("BigInteger 最小值应该正确", bigMin, bigFractionMin.getNumerator());
        Assert.assertEquals("BigInteger 最大值应该正确", bigMax, bigFractionMax.getNumerator());
    }

    /**
     * 测试溢出情况
     */
    @Test
    public void testOverflowCases() {
        // 测试 IntFraction 加法溢出
        IntFraction intMax = new IntFraction(Integer.MAX_VALUE, 1);
        IntFraction intOne = new IntFraction(1, 1);

        try {
            intMax.add(intOne);
            Assert.fail("应该抛出加法溢出异常");
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应包含溢出", e.getMessage().contains("溢出"));
        }

        // 测试 IntFraction 乘法溢出
        IntFraction intLarge = new IntFraction(Integer.MAX_VALUE / 2, 1);

        try {
            intLarge.multiply(intLarge);
            Assert.fail("应该抛出乘法溢出异常");
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应包含溢出", e.getMessage().contains("溢出"));
        }

        // 测试 LongFraction 加法溢出
        LongFraction longMax = new LongFraction(Long.MAX_VALUE, 1);
        LongFraction longOne = new LongFraction(1, 1);

        try {
            longMax.add(longOne);
            Assert.fail("应该抛出加法溢出异常");
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应包含溢出", e.getMessage().contains("溢出"));
        }

        // 测试 LongFraction 乘法溢出
        LongFraction longLarge = new LongFraction(Long.MAX_VALUE / 2, 1);

        try {
            longLarge.multiply(longLarge);
            Assert.fail("应该抛出乘法溢出异常");
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应包含溢出", e.getMessage().contains("溢出"));
        }
    }

    /**
     * 测试分数化简
     */
    @Test
    public void testFractionSimplification() {
        // 测试基本化简
        IntFraction fraction1 = new IntFraction(4, 8);
        IntFraction simplified1 = fraction1.simpleFraction();
        Assert.assertEquals("4/8 应该化简为 1/2", "1/2", simplified1.getValue());

        // 测试负数化简
        IntFraction fraction2 = new IntFraction(-6, 9);
        IntFraction simplified2 = fraction2.simpleFraction();
        Assert.assertEquals("-6/9 应该化简为 -2/3", "-2/3", simplified2.getValue());

        // 测试分子为 0 的情况
        IntFraction fraction3 = new IntFraction(0, 5);
        IntFraction simplified3 = fraction3.simpleFraction();
        Assert.assertEquals("0/5 应该化简为 0/1", "0/1", simplified3.getValue());

        // 测试分母为 1 的情况
        IntFraction fraction4 = new IntFraction(5, 1);
        IntFraction simplified4 = fraction4.simpleFraction();
        Assert.assertEquals("5/1 应该保持不变", "5/1", simplified4.getValue());

        // 测试分子和分母相等的情况
        IntFraction fraction5 = new IntFraction(7, 7);
        IntFraction simplified5 = fraction5.simpleFraction();
        Assert.assertEquals("7/7 应该化简为 1/1", "1/1", simplified5.getValue());

        // 测试大数化简
        LongFraction longFraction = new LongFraction(1234567890, 2469135780L);
        LongFraction simplifiedLong = longFraction.simpleFraction();
        Assert.assertEquals("1234567890/2469135780 应该化简为 1/2", "1/2", simplifiedLong.getValue());
    }

    /**
     * 测试带分数转换
     */
    @Test
    public void testMixedNumberConversion() {
        // 测试正假分数
        IntFraction improper1 = new IntFraction(7, 3);
        MixedNumber mixed1 = improper1.toMixedNumber();
        Assert.assertEquals("7/3 应该转换为 2 1/3", "2 1/3", mixed1.toString());

        // 测试负假分数
        IntFraction improper2 = new IntFraction(-7, 3);
        MixedNumber mixed2 = improper2.toMixedNumber();
        Assert.assertEquals("-7/3 应该转换为 -2 1/3", "-2 1/3", mixed2.toString());

        // 测试真分数
        IntFraction proper = new IntFraction(2, 3);
        MixedNumber mixed3 = proper.toMixedNumber();
        Assert.assertEquals("2/3 应该转换为 0 2/3", "0 2/3", mixed3.toString());

        // 测试整数
        IntFraction integer = new IntFraction(5, 1);
        MixedNumber mixed4 = integer.toMixedNumber();
        Assert.assertEquals("5/1 应该转换为 5", "5", mixed4.toString());

        // 测试分子和分母相等的情况
        IntFraction equal = new IntFraction(3, 3);
        MixedNumber mixed5 = equal.toMixedNumber();
        Assert.assertEquals("3/3 应该转换为 1", "1", mixed5.toString());

        // 测试 LongFraction 带分数转换
        LongFraction longImproper = new LongFraction(1000000000L, 3);
        MixedNumber longMixed = longImproper.toMixedNumber();
        Assert.assertTrue("1000000000/3 应该转换为带分数", longMixed.toString().contains(" "));
    }

    /**
     * 测试分数运算
     */
    @Test
    public void testFractionOperations() {
        // 测试加法
        IntFraction a = new IntFraction(1, 2);
        IntFraction b = new IntFraction(1, 3);
        IntFraction sum = a.add(b);
        Assert.assertEquals("1/2 + 1/3 应该等于 5/6", "5/6", sum.getValue());

        // 测试减法
        IntFraction difference = a.sub(b);
        Assert.assertEquals("1/2 - 1/3 应该等于 1/6", "1/6", difference.getValue());

        // 测试乘法
        IntFraction product = a.multiply(b);
        Assert.assertEquals("1/2 * 1/3 应该等于 1/6", "1/6", product.getValue());

        // 测试除法
        IntFraction quotient = a.div(b);
        Assert.assertEquals("1/2 / 1/3 应该等于 3/2", "3/2", quotient.getValue());

        // 测试链式运算
        IntFraction result = a.add(b).multiply(a).div(b);
        Assert.assertNotNull("链式运算结果不应为 null", result);
    }

    /**
     * 测试比较功能
     */
    @Test
    public void testComparison() {
        IntFraction half = new IntFraction(1, 2);
        IntFraction third = new IntFraction(1, 3);
        IntFraction twoThirds = new IntFraction(2, 3);
        IntFraction anotherHalf = new IntFraction(2, 4);

        // 测试大于
        Assert.assertTrue("1/2 应该大于 1/3", half.greatThan(third));
        Assert.assertFalse("1/3 不应该大于 1/2", third.greatThan(half));

        // 测试小于
        Assert.assertTrue("1/3 应该小于 1/2", third.letterThan(half));
        Assert.assertFalse("1/2 不应该小于 1/3", half.letterThan(third));

        // 测试等于
        Assert.assertTrue("1/2 应该等于 2/4", half.eq(anotherHalf));
        Assert.assertFalse("1/2 不应该等于 1/3", half.eq(third));

        // 测试大于等于
        Assert.assertTrue("1/2 应该大于等于 1/3", half.greatThanOrEqual(third));
        Assert.assertTrue("1/2 应该大于等于 2/4", half.greatThanOrEqual(anotherHalf));

        // 测试小于等于
        Assert.assertTrue("1/3 应该小于等于 1/2", third.letterThanOrEqual(half));
        Assert.assertTrue("1/2 应该小于等于 2/4", half.letterThanOrEqual(anotherHalf));
    }

    /**
     * 测试特殊值
     */
    @Test
    public void testSpecialValues() {
        // 测试零
        IntFraction zero = new IntFraction(0, 1);
        Assert.assertTrue("0/1 应该是零", zero.isZero());
        Assert.assertFalse("0/1 不应该是正数", zero.isPositive());
        Assert.assertFalse("0/1 不应该是负数", zero.isNegative());

        // 测试正数
        IntFraction positive = new IntFraction(1, 2);
        Assert.assertTrue("1/2 应该是正数", positive.isPositive());
        Assert.assertFalse("1/2 不应该是零", positive.isZero());
        Assert.assertFalse("1/2 不应该是负数", positive.isNegative());

        // 测试负数
        IntFraction negative = new IntFraction(-1, 2);
        Assert.assertTrue("-1/2 应该是负数", negative.isNegative());
        Assert.assertFalse("-1/2 不应该是零", negative.isZero());
        Assert.assertFalse("-1/2 不应该是正数", negative.isPositive());

        // 测试真分数和假分数
        IntFraction proper = new IntFraction(1, 2);
        Assert.assertTrue("1/2 应该是真分数", proper.isProperFraction());
        Assert.assertFalse("1/2 不应该是假分数", proper.isImproperFraction());

        IntFraction improper = new IntFraction(3, 2);
        Assert.assertTrue("3/2 应该是假分数", improper.isImproperFraction());
        Assert.assertFalse("3/2 不应该是真分数", improper.isProperFraction());
    }

    /**
     * 测试字符串表示
     */
    @Test
    public void testStringRepresentation() {
        IntFraction fraction1 = new IntFraction(1, 2);
        Assert.assertEquals("1/2 的字符串表示应该是 '1/2'", "1/2", fraction1.getValue());

        IntFraction fraction2 = new IntFraction(-3, 4);
        Assert.assertEquals("-3/4 的字符串表示应该是 '-3/4'", "-3/4", fraction2.getValue());

        IntFraction fraction3 = new IntFraction(5, 1);
        Assert.assertEquals("5/1 的字符串表示应该是 '5/1'", "5/1", fraction3.getValue());

        IntFraction fraction4 = new IntFraction(0, 1);
        Assert.assertEquals("0/1 的字符串表示应该是 '0/1'", "0/1", fraction4.getValue());

        // 测试 toString 方法
        Assert.assertTrue("toString 方法应该包含分数字符串", fraction1.toString().contains("1/2"));
    }

    /**
     * 测试哈希码和相等性
     */
    @Test
    public void testHashCodeAndEquality() {
        IntFraction fraction1 = new IntFraction(1, 2);
        IntFraction fraction2 = new IntFraction(2, 4);
        IntFraction fraction3 = new IntFraction(1, 3);

        // 测试相等性
        Assert.assertTrue("1/2 应该等于 2/4", fraction1.equals(fraction2));
        Assert.assertFalse("1/2 不应该等于 1/3", fraction1.equals(fraction3));

        // 测试哈希码
        Assert.assertEquals("相等的分数应该有相同的哈希码", fraction1.hashCode(), fraction2.hashCode());
        Assert.assertNotEquals("不相等的分数应该有不同的哈希码", fraction1.hashCode(), fraction3.hashCode());
    }

    /**
     * 测试 BigFraction 的特殊功能
     */
    @Test
    public void testBigFractionSpecial() {
        // 测试大数运算
        BigFraction big1 = new BigFraction("123456789012345678901234567890", "1");
        BigFraction big2 = new BigFraction("987654321098765432109876543210", "1");

        BigFraction sum = big1.add(big2);
        Assert.assertNotNull("大数相加结果不应为 null", sum);

        BigFraction product = big1.multiply(big2);
        Assert.assertNotNull("大数相乘结果不应为 null", product);

        // 测试大数化简
        BigFraction bigFraction = new BigFraction("12345678901234567890", "24691357802469135780");
        BigFraction simplified = bigFraction.simpleFraction();
        Assert.assertEquals("大数分数应该正确化简", "1/2", simplified.getValue());
    }

    /**
     * 测试异常情况
     */
    @Test
    public void testExceptions() {
        // 测试分母为零
        try {
            new IntFraction(1, 0);
            Assert.fail("分母为零应该抛出异常");
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应包含分母不能为零", e.getMessage().contains("分母不能为 0"));
        }

        // 测试除以零
        IntFraction fraction = new IntFraction(1, 2);
        IntFraction zero = new IntFraction(0, 1);
        try {
            fraction.div(zero);
            Assert.fail("除以零应该抛出异常");
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应包含除零错误", e.getMessage().contains("除零错误"));
        }

        // 测试零的倒数
        try {
            zero.reciprocal();
            Assert.fail("零的倒数应该抛出异常");
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应包含不能取倒数", e.getMessage().contains("倒数"));
        }

        // 测试无效的字符串格式
        try {
            new IntFraction("invalid");
            Assert.fail("无效的字符串格式应该抛出异常");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue("异常信息应包含格式错误", e.getMessage().contains("格式错误"));
        }
    }
}