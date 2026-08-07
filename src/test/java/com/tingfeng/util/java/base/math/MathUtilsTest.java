package com.tingfeng.util.java.base.math;

import com.tingfeng.util.java.base.common.utils.TestUtils;
import com.tingfeng.util.java.base.lang.Base64Utils;
import com.tingfeng.util.java.base.math.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.charset.Charset;

/**
 * @Author huitoukest
 * @Date 2019-05-08 17:16
 **/
public class MathUtilsTest {

    @Test
    public void encodeTest(){
        // 验证基本字符比较和转换功能
        Assert.assertTrue('a' > 'A');
        Assert.assertNotNull(MathUtils.toDecimal("0fff0215","0123456789abcdef".toCharArray()));
        Assert.assertNotNull(MathUtils.toRadix("0fff02156s9d5gm3m48u",16,62));
        String binaryStr = new BigInteger("0fff02156s9d5gm3m48u".getBytes(Charset.forName("iso-8859-1"))).toString(16);
        Assert.assertNotNull(MathUtils.toRadix(binaryStr,16,62));
        Assert.assertNotNull(Base64Utils.enCode("0fff02156s9d5gm3m48u"));
    }

    @Test
    public void toRadixTest(){
        String num = "0123456789456123456";
        String radix = "0123456789abcdef".substring(0,RandomUtils.randomInt(2,16));
        Assert.assertNotNull(MathUtils.toRadix(num,radix.toCharArray()));
        // 使用更小的数值范围和迭代次数，避免OOM
        TestUtils.printTime(1,100,index -> {
            long numA = RandomUtils.randomLong(0,100000);
            String re = MathUtils.toRadix(String.valueOf(numA),radix.toCharArray());
            Assert.assertTrue(re.equals(Long.toString(numA, radix.length())));
        });
    }

    @Test
    public void toRadix2Test(){
        String num = "1999999999999";
        Assert.assertNotNull(MathUtils.toRadix(num,62));
    }

    @Test
    public void addPositiveIntegerTest(){
        String a = "99123";
        String b = "88";
        char[] re = MathUtils.addPositiveInteger(a.toCharArray(),b.toCharArray(),"0123456789".toCharArray());
        Assert.assertEquals("99211", new String(re));
        a = "10ff";
        b = "0abc";
        re = MathUtils.addPositiveInteger(a.toCharArray(),b.toCharArray(),"0123456789abcdef".toCharArray());
        Assert.assertEquals("1bbb", new String(re));
    }

    @Test
    public void addPositiveIntegerSingleDigitTest(){
        // B1: 单位数不进位加法，扫描须含最高结果位（原实现误判为全0返回空数组）
        Assert.assertEquals("2", new String(MathUtils.addPositiveInteger("1".toCharArray(),"1".toCharArray(),"0123456789".toCharArray())));
        Assert.assertEquals("5", new String(MathUtils.addPositiveInteger("2".toCharArray(),"3".toCharArray(),"0123456789".toCharArray())));
        Assert.assertEquals("9", new String(MathUtils.addPositiveInteger("9".toCharArray(),"0".toCharArray(),"0123456789".toCharArray())));
        // 进位场景回归
        Assert.assertEquals("18", new String(MathUtils.addPositiveInteger("9".toCharArray(),"9".toCharArray(),"0123456789".toCharArray())));
        Assert.assertEquals("100", new String(MathUtils.addPositiveInteger("99".toCharArray(),"1".toCharArray(),"0123456789".toCharArray())));
    }

    @Test
    public void gcdNegativeTest(){
        // B6: 负数输入取绝对值后计算（原实现对负数返回错误结果）
        Assert.assertEquals(3, MathUtils.gcd(-6, 9));
        Assert.assertEquals(6, MathUtils.gcd(12, -18));
        Assert.assertEquals(7, MathUtils.gcd(-7, -14));
        Assert.assertEquals(3L, MathUtils.gcd(-6L, 9L));
        Assert.assertEquals(6L, MathUtils.gcd(12L, -18L));
        // Long.MIN_VALUE 走 BigInteger 路径避免绝对值溢出
        Assert.assertEquals(1L, MathUtils.gcd(Long.MIN_VALUE, 1L));
        Assert.assertEquals(2L, MathUtils.gcd(2L, Long.MIN_VALUE));
        // 常规输入不回归
        Assert.assertEquals(4, MathUtils.gcd(0, 4));
        Assert.assertEquals(0, MathUtils.gcd(0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPositiveIntegerIllegalCharTest(){
        // B12: 加法遇到未映射字符显式抛异常（原实现静默按0处理）
        MathUtils.addPositiveInteger("1a".toCharArray(),"1".toCharArray(),"0123456789".toCharArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void toDecimalIllegalCharTest(){
        // B12: 解码遇到未映射字符显式抛异常
        MathUtils.toDecimal("12a","0123456789".toCharArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void toDecimalNonAsciiCharTest(){
        // B12: 解码遇到非 ASCII 输入字符显式抛异常（原实现数组越界）
        MathUtils.toDecimal("1中2","0123456789".toCharArray());
    }

    @Test
    public void getValueCharMapsNonAsciiTest(){
        // B13: 非 ASCII 字符集显式拒绝
        try {
            MathUtils.getValueCharMaps(new char[]{'0','1',(char) 256});
            Assert.fail("应抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            MathUtils.getValueCharMaps(new char[]{'0','中'});
            Assert.fail("应抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // 正常字符集映射正确，未映射位置为 -1
        int[] maps = MathUtils.getValueCharMaps("0123456789abcdef".toCharArray());
        Assert.assertEquals(10, maps['a']);
        Assert.assertEquals(15, maps['f']);
        Assert.assertEquals(-1, maps['z']);
    }

    @Test
    public void toRadixLongTest(){
        // SS10: long 重载结果与 String 版一致（同一实现路径，免字符串往返）
        long[] values = {0L, 1L, 255L, 123456789L, Long.MAX_VALUE};
        int[] radixes = {2, 8, 16, 62};
        for (long value : values) {
            for (int radix : radixes) {
                Assert.assertEquals(MathUtils.toRadix(String.valueOf(value), radix), MathUtils.toRadix(value, radix));
            }
        }
        // 典型进制结果抽查（默认62字符集大写）
        Assert.assertEquals("11111111", MathUtils.toRadix(255L, 2));
        Assert.assertEquals("FF", MathUtils.toRadix(255L, 16));
        Assert.assertEquals("47", MathUtils.toRadix(255L, 62));
    }

    @Test(expected = IllegalArgumentException.class)
    public void toRadixLongRadixTooSmallTest(){
        // SS10: radix < 2 显式抛异常（防 radix=1 除留余数法死循环）
        MathUtils.toRadix(123L, 1);
    }

    @Test
    public void toRadixRadixOneTest(){
        // SS1: 4 参版 radix<2 显式抛异常（防 radix=1 除留余数法死循环 OOM）
        // 黑盒: 单字符字符集长度 1，经 min 裁剪后 toRadix=1
        try {
            MathUtils.toRadix("10", "0".toCharArray());
            Assert.fail("应抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // 白盒: 显式 toRadix=1
        try {
            MathUtils.toRadix("10", "0123456789".toCharArray(), 10, 1);
            Assert.fail("应抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // radix=0（<2 族）
        try {
            MathUtils.toRadix("10", "0123456789".toCharArray(), 10, 0);
            Assert.fail("应抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void isPowerOfTwoTest(){
        // 边界: 1 是最小正 2 的幂
        Assert.assertTrue(MathUtils.isPowerOfTwo(1L));
        Assert.assertTrue(MathUtils.isPowerOfTwo(2L));
        Assert.assertTrue(MathUtils.isPowerOfTwo(1024L));
        // 最大正 2 的幂 2^62
        Assert.assertTrue(MathUtils.isPowerOfTwo(1L << 62));
        // 2^63 在 long 中为负数，不满足 value > 0
        Assert.assertFalse(MathUtils.isPowerOfTwo(1L << 63));
        // 0 与负数不是 2 的幂
        Assert.assertFalse(MathUtils.isPowerOfTwo(0L));
        Assert.assertFalse(MathUtils.isPowerOfTwo(-1L));
        Assert.assertFalse(MathUtils.isPowerOfTwo(Long.MIN_VALUE));
        // 非 2 的幂
        Assert.assertFalse(MathUtils.isPowerOfTwo(3L));
        Assert.assertFalse(MathUtils.isPowerOfTwo(100L));
    }

    @Test
    public void clampTest(){
        // int 版
        Assert.assertEquals(5, MathUtils.clamp(5, 0, 10));
        Assert.assertEquals(0, MathUtils.clamp(-5, 0, 10));
        Assert.assertEquals(10, MathUtils.clamp(15, 0, 10));
        // long 版
        Assert.assertEquals(5L, MathUtils.clamp(5L, 0L, 10L));
        Assert.assertEquals(0L, MathUtils.clamp(-5L, 0L, 10L));
        Assert.assertEquals(10L, MathUtils.clamp(15L, 0L, 10L));
        // double 版
        Assert.assertEquals(5.5, MathUtils.clamp(5.5, 0.0, 10.0), 1e-9);
        Assert.assertEquals(0.0, MathUtils.clamp(-5.5, 0.0, 10.0), 1e-9);
        Assert.assertEquals(10.0, MathUtils.clamp(15.5, 0.0, 10.0), 1e-9);
        // 边界值不溢出
        Assert.assertEquals(Integer.MAX_VALUE, MathUtils.clamp(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE));
        Assert.assertEquals(Long.MIN_VALUE, MathUtils.clamp(Long.MIN_VALUE, Long.MIN_VALUE, Long.MAX_VALUE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void clampMinGreaterThanMaxTest(){
        // SS10: min > max 显式抛异常
        MathUtils.clamp(5, 10, 0);
    }

    @Test
    public void isqrtTest(){
        // SS7: 边界 0/1
        Assert.assertEquals(0L, MathUtils.isqrt(0L));
        Assert.assertEquals(1L, MathUtils.isqrt(1L));
        // SS7: 完全平方
        Assert.assertEquals(2L, MathUtils.isqrt(4L));
        Assert.assertEquals(3L, MathUtils.isqrt(9L));
        Assert.assertEquals(10L, MathUtils.isqrt(100L));
        // SS7: 非完全平方（向下取整）
        Assert.assertEquals(1L, MathUtils.isqrt(2L));
        Assert.assertEquals(1L, MathUtils.isqrt(3L));
        Assert.assertEquals(2L, MathUtils.isqrt(8L));
        Assert.assertEquals(3L, MathUtils.isqrt(15L));
        // SS7: 大数边界，3037000499² = 9223372030926249001 仍可表示；3037000500² 溢出 long
        Assert.assertEquals(3037000499L, MathUtils.isqrt(9223372030926249001L));
        Assert.assertEquals(3037000499L, MathUtils.isqrt(Long.MAX_VALUE));
        // SS7: floor 语义 x² ≤ value < (x+1)²
        long root = MathUtils.isqrt(1234567890123456789L);
        Assert.assertTrue(root * root <= 1234567890123456789L);
        Assert.assertTrue((root + 1) * (root + 1) > 1234567890123456789L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void isqrtNegativeTest(){
        // SS7: 负数显式抛异常
        MathUtils.isqrt(-1L);
    }

    @Test
    public void factorialTest(){
        // SS7: 已知值 0! = 1、1! = 1、5! = 120、10! = 3628800、20! = 2432902008176640000
        Assert.assertEquals(BigInteger.ONE, MathUtils.factorial(0));
        Assert.assertEquals(BigInteger.ONE, MathUtils.factorial(1));
        Assert.assertEquals(BigInteger.valueOf(120), MathUtils.factorial(5));
        Assert.assertEquals(BigInteger.valueOf(3628800), MathUtils.factorial(10));
        Assert.assertEquals(new BigInteger("2432902008176640000"), MathUtils.factorial(20));
    }

    @Test(expected = IllegalArgumentException.class)
    public void factorialNegativeTest(){
        // SS7: 负数显式抛异常
        MathUtils.factorial(-1);
    }

    @Test
    public void combinationCountTest(){
        // SS7: 已知值 C(10,3) = 120
        Assert.assertEquals(BigInteger.valueOf(120), MathUtils.combinationCount(10, 3));
        // SS7: 对称性 C(n,k) == C(n,n-k)
        Assert.assertEquals(MathUtils.combinationCount(10, 3), MathUtils.combinationCount(10, 7));
        Assert.assertEquals(MathUtils.combinationCount(30, 12), MathUtils.combinationCount(30, 18));
        // SS7: 边界 0/1
        Assert.assertEquals(BigInteger.ONE, MathUtils.combinationCount(0, 0));
        Assert.assertEquals(BigInteger.ONE, MathUtils.combinationCount(1, 0));
        Assert.assertEquals(BigInteger.ONE, MathUtils.combinationCount(1, 1));
        // SS7: 常规值
        Assert.assertEquals(BigInteger.valueOf(10), MathUtils.combinationCount(5, 2));
        Assert.assertEquals(BigInteger.valueOf(155117520), MathUtils.combinationCount(30, 15));
        // SS7: 大数精确，C(100,50) 与 factorial 公式交叉验证
        BigInteger cross = MathUtils.factorial(100).divide(MathUtils.factorial(50).multiply(MathUtils.factorial(50)));
        Assert.assertEquals(cross, MathUtils.combinationCount(100, 50));
    }

    @Test(expected = IllegalArgumentException.class)
    public void combinationCountKGreaterThanNTest(){
        // SS7: k > n 显式抛异常
        MathUtils.combinationCount(5, 6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void combinationCountNegativeTest(){
        // SS7: n < 0、k < 0 显式抛异常
        MathUtils.combinationCount(-1, 2);
        MathUtils.combinationCount(5, -1);
    }
}
