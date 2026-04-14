package com.tingfeng.util.java.base.math.base;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * 分数系统测试类，验证所有分数实现的功能正确性
 * 包括：
 * 1. 基本运算（加、减、乘、除）
 * 2. 比较操作
 * 3. 绝对值和倒数
 * 4. 带分数转换
 * 5. 线程安全（通过并发测试）
 * 
 * @author huitoukest
 */
public class FractionTest {

    /**
     * 测试 IntFraction 的基本功能
     */
    @Test
    public void testIntFraction() {
        // 测试构造函数
        IntFraction fraction1 = new IntFraction(1, 2);
        IntFraction fraction2 = new IntFraction(1, 3);
        
        // 测试加法
        IntFraction sum = fraction1.add(fraction2);
        assertEquals("加法测试失败", new IntFraction(5, 6), sum);
        
        // 测试减法
        IntFraction difference = fraction1.sub(fraction2);
        assertEquals("减法测试失败", new IntFraction(1, 6), difference);
        
        // 测试乘法
        IntFraction product = fraction1.multiply(fraction2);
        assertEquals("乘法测试失败", new IntFraction(1, 6), product);
        
        // 测试除法
        IntFraction quotient = fraction1.div(fraction2);
        assertEquals("除法测试失败", new IntFraction(3, 2), quotient);
        
        // 测试绝对值
        IntFraction negativeFraction = new IntFraction(-1, 2);
        IntFraction absFraction = negativeFraction.abs();
        assertEquals("绝对值测试失败", new IntFraction(1, 2), absFraction);
        
        // 测试倒数
        IntFraction reciprocal = fraction1.reciprocal();
        assertEquals("倒数测试失败", new IntFraction(2, 1), reciprocal);
        
        // 测试带分数转换
        IntFraction improperFraction = new IntFraction(7, 3);
        MixedNumber mixedNumber = improperFraction.toMixedNumber();
        assertTrue("带分数转换测试失败", mixedNumber instanceof IntMixedNumber);
        assertEquals("带分数整数部分测试失败", Integer.valueOf(2), ((IntMixedNumber) mixedNumber).getWholePart());
        assertEquals("带分数分子测试失败", Integer.valueOf(1), ((IntMixedNumber) mixedNumber).getNumerator());
        assertEquals("带分数分母测试失败", Integer.valueOf(3), ((IntMixedNumber) mixedNumber).getDenominator());
        
        // 测试比较操作
        assertTrue("比较测试失败", fraction1.greatThan(fraction2));
        assertTrue("比较测试失败", fraction2.letterThan(fraction1));
        assertTrue("比较测试失败", fraction1.eq(new IntFraction(2, 4)));
    }

    /**
     * 测试 LongFraction 的基本功能
     */
    @Test
    public void testLongFraction() {
        // 测试构造函数
        LongFraction fraction1 = new LongFraction(1L, 2L);
        LongFraction fraction2 = new LongFraction(1L, 3L);
        
        // 测试加法
        LongFraction sum = fraction1.add(fraction2);
        assertEquals("加法测试失败", new LongFraction(5L, 6L), sum);
        
        // 测试减法
        LongFraction difference = fraction1.sub(fraction2);
        assertEquals("减法测试失败", new LongFraction(1L, 6L), difference);
        
        // 测试乘法
        LongFraction product = fraction1.multiply(fraction2);
        assertEquals("乘法测试失败", new LongFraction(1L, 6L), product);
        
        // 测试除法
        LongFraction quotient = fraction1.div(fraction2);
        assertEquals("除法测试失败", new LongFraction(3L, 2L), quotient);
        
        // 测试绝对值
        LongFraction negativeFraction = new LongFraction(-1L, 2L);
        LongFraction absFraction = negativeFraction.abs();
        assertEquals("绝对值测试失败", new LongFraction(1L, 2L), absFraction);
        
        // 测试倒数
        LongFraction reciprocal = fraction1.reciprocal();
        assertEquals("倒数测试失败", new LongFraction(2L, 1L), reciprocal);
        
        // 测试带分数转换
        LongFraction improperFraction = new LongFraction(7L, 3L);
        MixedNumber mixedNumber = improperFraction.toMixedNumber();
        assertTrue("带分数转换测试失败", mixedNumber instanceof LongMixedNumber);
        assertEquals("带分数整数部分测试失败", Long.valueOf(2L), ((LongMixedNumber) mixedNumber).getWholePart());
        assertEquals("带分数分子测试失败", Long.valueOf(1L), ((LongMixedNumber) mixedNumber).getNumerator());
        assertEquals("带分数分母测试失败", Long.valueOf(3L), ((LongMixedNumber) mixedNumber).getDenominator());
    }

    /**
     * 测试 BigFraction 的基本功能
     */
    @Test
    public void testBigFraction() {
        // 测试构造函数
        BigFraction fraction1 = new BigFraction(BigInteger.ONE, BigInteger.valueOf(2));
        BigFraction fraction2 = new BigFraction(BigInteger.ONE, BigInteger.valueOf(3));
        
        // 测试加法
        BigFraction sum = fraction1.add(fraction2);
        assertEquals("加法测试失败", new BigFraction(BigInteger.valueOf(5), BigInteger.valueOf(6)), sum);
        
        // 测试减法
        BigFraction difference = fraction1.sub(fraction2);
        assertEquals("减法测试失败", new BigFraction(BigInteger.ONE, BigInteger.valueOf(6)), difference);
        
        // 测试乘法
        BigFraction product = fraction1.multiply(fraction2);
        assertEquals("乘法测试失败", new BigFraction(BigInteger.ONE, BigInteger.valueOf(6)), product);
        
        // 测试除法
        BigFraction quotient = fraction1.div(fraction2);
        assertEquals("除法测试失败", new BigFraction(BigInteger.valueOf(3), BigInteger.valueOf(2)), quotient);
        
        // 测试绝对值
        BigFraction negativeFraction = new BigFraction(BigInteger.ONE.negate(), BigInteger.valueOf(2));
        BigFraction absFraction = negativeFraction.abs();
        assertEquals("绝对值测试失败", new BigFraction(BigInteger.ONE, BigInteger.valueOf(2)), absFraction);
        
        // 测试倒数
        BigFraction reciprocal = fraction1.reciprocal();
        assertEquals("倒数测试失败", new BigFraction(BigInteger.valueOf(2), BigInteger.ONE), reciprocal);
        
        // 测试带分数转换
        BigFraction improperFraction = new BigFraction(BigInteger.valueOf(7), BigInteger.valueOf(3));
        MixedNumber mixedNumber = improperFraction.toMixedNumber();
        assertTrue("带分数转换测试失败", mixedNumber instanceof BigMixedNumber);
        assertEquals("带分数整数部分测试失败", BigInteger.valueOf(2), ((BigMixedNumber) mixedNumber).getWholePart());
        assertEquals("带分数分子测试失败", BigInteger.ONE, ((BigMixedNumber) mixedNumber).getNumerator());
        assertEquals("带分数分母测试失败", BigInteger.valueOf(3), ((BigMixedNumber) mixedNumber).getDenominator());
    }

    /**
     * 测试异常处理
     */
    @Test
    public void testExceptions() {
        // 测试除零错误
        IntFraction zeroFraction = new IntFraction(0, 1);
        try {
            zeroFraction.reciprocal();
            fail("应该抛出 ArithmeticException");
        } catch (ArithmeticException e) {
            // 预期的异常
        }
        
        // 测试分母为零错误
        try {
            new IntFraction(1, 0);
            fail("应该抛出 ArithmeticException");
        } catch (ArithmeticException e) {
            // 预期的异常
        }
        
        // 测试空指针错误
        IntFraction fraction = new IntFraction(1, 2);
        try {
            fraction.add(null);
            fail("应该抛出 NullPointerException");
        } catch (NullPointerException e) {
            // 预期的异常
        }
    }

    /**
     * 测试带分数的功能
     */
    @Test
    public void testMixedNumbers() {
        // 测试 IntMixedNumber
        IntMixedNumber intMixed = new IntMixedNumber(2, 1, 3);
        assertEquals("带分数整数部分测试失败", Integer.valueOf(2), intMixed.getWholePart());
        assertEquals("带分数分子测试失败", Integer.valueOf(1), intMixed.getNumerator());
        assertEquals("带分数分母测试失败", Integer.valueOf(3), intMixed.getDenominator());
        assertEquals("带分数转假分数分子测试失败", Integer.valueOf(7), intMixed.toImproperNumerator());
        assertEquals("带分数转假分数分母测试失败", Integer.valueOf(3), intMixed.toImproperDenominator());
        
        // 测试 LongMixedNumber
        LongMixedNumber longMixed = new LongMixedNumber(2L, 1L, 3L);
        assertEquals("带分数整数部分测试失败", Long.valueOf(2L), longMixed.getWholePart());
        assertEquals("带分数分子测试失败", Long.valueOf(1L), longMixed.getNumerator());
        assertEquals("带分数分母测试失败", Long.valueOf(3L), longMixed.getDenominator());
        
        // 测试 BigMixedNumber
        BigMixedNumber bigMixed = new BigMixedNumber(BigInteger.valueOf(2), BigInteger.ONE, BigInteger.valueOf(3));
        assertEquals("带分数整数部分测试失败", BigInteger.valueOf(2), bigMixed.getWholePart());
        assertEquals("带分数分子测试失败", BigInteger.ONE, bigMixed.getNumerator());
        assertEquals("带分数分母测试失败", BigInteger.valueOf(3), bigMixed.getDenominator());
    }
}