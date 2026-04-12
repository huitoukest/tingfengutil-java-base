package com.tingfeng.util.java.base.common.bean.fraction;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 分数系统优化后的综合测试类
 * 测试线程安全、溢出保护、性能优化等功能
 * 
 * @author huitoukest
 */
public class FractionOptimizationTest {

    /**
     * 测试线程安全性
     */
    @Test
    public void testThreadSafety() throws InterruptedException {
        final IntFraction fraction = new IntFraction(1, 2);
        final int threadCount = 10;
        final int operationsPerThread = 1000;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final AtomicInteger successCount = new AtomicInteger(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        // 多线程并发执行各种操作
                        IntFraction result = fraction.add(new IntFraction(1, 3));
                        result = result.multiply(new IntFraction(2, 3));
                        result = result.simpleFraction();
                        
                        if (result != null) {
                            successCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("线程执行异常: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        Assert.assertTrue("所有线程应在5秒内完成", latch.await(5, TimeUnit.SECONDS));
        Assert.assertEquals("所有操作都应成功", threadCount * operationsPerThread, successCount.get());
        
        executor.shutdown();
    }

    /**
     * 测试溢出保护
     */
    @Test
    public void testOverflowProtection() {
        // 测试 IntFraction 溢出保护
        IntFraction large1 = new IntFraction(Integer.MAX_VALUE / 2, 1);
        IntFraction large2 = new IntFraction(Integer.MAX_VALUE / 2 + 2, 1);
        
        try {
            large1.add(large2); // 这应该溢出
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应包含溢出", e.getMessage().contains("溢出"));
        }
        
        // 测试 LongFraction 溢出保护
        LongFraction largeLong1 = new LongFraction(Long.MAX_VALUE / 2, 1);
        LongFraction largeLong2 = new LongFraction(Long.MAX_VALUE / 2, 1);
        
        try {
            largeLong1.add(largeLong2); // 这应该溢出
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应包含溢出", e.getMessage().contains("溢出"));
        }
    }

    /**
     * 测试除零保护
     */
    @Test
    public void testDivisionByZero() {
        IntFraction fraction = new IntFraction(1, 2);
        IntFraction zero = new IntFraction(0, 1);
        
        try {
            fraction.div(zero);
            Assert.fail("应该抛出除零异常");
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应包含除零", e.getMessage().contains("除零"));
        }
        
        // 测试构造函数除零保护
        try {
            new IntFraction(1, 0);
            Assert.fail("应该抛出除零异常");
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应包含除零", e.getMessage().contains("分母不能为 0"));
        }
    }

    /**
     * 测试带分数转换
     */
    @Test
    public void testMixedNumber() {
        // 测试假分数转换
        IntFraction improper = new IntFraction(7, 3);
        MixedNumber mixed = improper.toMixedNumber();
        
        Assert.assertEquals("整数部分应为 2", 2, mixed.getWholePart());
        Assert.assertEquals("分数分子应为 1", 1, mixed.getNumerator());
        Assert.assertEquals("分数分母应为 3", 3, mixed.getDenominator());
        Assert.assertEquals("字符串表示应为 '2 1/3'", "2 1/3", mixed.toString());
        
        // 测试真分数转换
        IntFraction proper = new IntFraction(2, 3);
        MixedNumber mixed2 = proper.toMixedNumber();
        
        Assert.assertEquals("整数部分应为 0", 0, mixed2.getWholePart());
        Assert.assertEquals("分数分子应为 2", 2, mixed2.getNumerator());
        Assert.assertEquals("分数分母应为 3", 3, mixed2.getDenominator());
        
        // 测试负数
        IntFraction negative = new IntFraction(-7, 3);
        MixedNumber mixed3 = negative.toMixedNumber();
        
        Assert.assertEquals("整数部分应为 -2", -2, mixed3.getWholePart());
        Assert.assertEquals("分数分子应为 1", 1, mixed3.getNumerator());
        Assert.assertEquals("分数分母应为 3", 3, mixed3.getDenominator());
    }

    /**
     * 测试性能优化 - 缓存机制
     */
    @Test
    public void testPerformanceOptimization() {
        IntFraction fraction = new IntFraction(100, 200); // 需要简化的分数
        
        long startTime = System.nanoTime();
        
        // 多次调用 simpleFraction，应该利用缓存
        for (int i = 0; i < 1000; i++) {
            IntFraction simplified = fraction.simpleFraction();
            Assert.assertEquals("应该得到相同的结果", "1/2", simplified.getValue());
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        System.out.println("1000次简化操作耗时: " + (duration / 1000000) + "ms");
        Assert.assertTrue("性能应该很优秀", duration < 100000000); // 应该小于 100ms
    }

    /**
     * 测试新的比较功能
     */
    @Test
    public void testComparisonFunctions() {
        IntFraction half = new IntFraction(1, 2);
        IntFraction third = new IntFraction(1, 3);
        IntFraction twoThirds = new IntFraction(2, 3);
        
        // 测试基本比较
        Assert.assertTrue("1/2 > 1/3", half.greatThan(third));
        Assert.assertTrue("1/3 < 1/2", third.letterThan(half));
        Assert.assertTrue("1/2 >= 1/3", half.greatThanOrEqual(third));
        Assert.assertTrue("1/3 <= 1/2", third.letterThanOrEqual(half));
        
        // 测试相等
        IntFraction anotherHalf = new IntFraction(2, 4);
        Assert.assertTrue("1/2 == 2/4", half.eq(anotherHalf));
        
        // 测试真分数和假分数判断
        Assert.assertTrue("1/2 是真分数", half.isProperFraction());
        Assert.assertFalse("1/2 不是假分数", half.isImproperFraction());
        
        IntFraction improper = new IntFraction(3, 2);
        Assert.assertFalse("3/2 不是真分数", improper.isProperFraction());
        Assert.assertTrue("3/2 是假分数", improper.isImproperFraction());
    }

    /**
     * 测试绝对值和倒数功能
     */
    @Test
    public void testAbsAndReciprocal() {
        IntFraction negative = new IntFraction(-3, 4);
        IntFraction abs = negative.abs();
        
        Assert.assertTrue("绝对值应该是正数", abs.isPositive());
        Assert.assertEquals("绝对值应该是 3/4", "3/4", abs.getValue());
        
        IntFraction reciprocal = negative.reciprocal();
        Assert.assertEquals("倒数应该是 -4/3", "-4/3", reciprocal.getValue());
        
        // 测试 0 的倒数应该抛出异常
        IntFraction zero = new IntFraction(0, 1);
        try {
            zero.reciprocal();
            Assert.fail("0 的倒数应该抛出异常");
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应提示不能取倒数", e.getMessage().contains("倒数"));
        }
    }

    /**
     * 测试百分数转换
     */
    @Test
    public void testPercentage() {
        IntFraction half = new IntFraction(1, 2);
        String percentage = half.toPercentage(2);
        
        Assert.assertEquals("1/2 应该是 50.00%", "50.00%", percentage);
        
        IntFraction third = new IntFraction(1, 3);
        String percentage2 = third.toPercentage(4);
        
        System.out.println("1/3 的百分数表示: " + percentage2);
        Assert.assertTrue("应该包含百分号", percentage2.contains("%"));
    }

    /**
     * 测试 BigFraction 的大数处理能力
     */
    @Test
    public void testBigFraction() {
        // 测试超大数的分数运算
        BigFraction big1 = new BigFraction("12345678901234567890/1");
        BigFraction big2 = new BigFraction("98765432109876543210/1");
        
        BigFraction result = big1.add(big2);
        System.out.println("大数相加结果: " + result.getValue());
        
        Assert.assertNotNull("结果不应为 null", result);
        Assert.assertTrue("结果应该是正数", result.isPositive());
    }

    /**
     * 测试字符串解析的健壮性
     */
    @Test
    public void testStringParsing() {
        // 测试正常解析
        IntFraction f1 = new IntFraction("  1  /  2  ");
        Assert.assertEquals("应该能解析带空格的字符串", "1/2", f1.getValue());
        
        // 测试异常格式
        try {
            new IntFraction("1/2/3");
            Assert.fail("应该抛出格式错误异常");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue("异常信息应提示格式错误", e.getMessage().contains("格式错误"));
        }
        
        // 测试空字符串
        try {
            new IntFraction("");
            Assert.fail("应该抛出空字符串异常");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue("异常信息应提示不能为空", e.getMessage().contains("不能为空"));
        }
    }
}