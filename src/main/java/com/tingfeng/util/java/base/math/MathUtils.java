package com.tingfeng.util.java.base.math;

import java.math.BigInteger;

/**
 * 第一版，通过与十进制之间的互转实现各个任意进制之间的互转
 * 第二版，先实现任意指定进制的大数的计算工具，然后直接实现任意进制的互转
 *
 * 进制转换实现已下沉至包私有 {@link RadixOps}，本类保留门面方法保证 API 兼容。
 *
 * @author huitoukest
 **/
public class MathUtils {

    /**
     * 两个指定进制数的相加（自动计算字符映射）
     * @param valueA 第一个数值的字符数组
     * @param valueB 第二个数值的字符数组
     * @param radixValues 进制采用的基数数组
     */
    public static char[] addPositiveInteger(char[] valueA,char[] valueB,char[] radixValues) {
        return RadixOps.addPositiveInteger(valueA, valueB, radixValues);
    }

    /**
     * 两个指定进制数的相加（使用预计算的字符映射）
     * @param valueA 第一个数值的字符数组
     * @param valueB 第二个数值的字符数组
     * @param radixValues 进制采用的基数数组,索引映射为char字符
     * @param valueCharMaps 字符到值得映射，使用字符的ascii码的值作为索引，值就是其真实代表的值
     */
    public static char[] addPositiveInteger(char[] valueA,char[] valueB,char[] radixValues,int[] valueCharMaps){
        return RadixOps.addPositiveInteger(valueA, valueB, radixValues, valueCharMaps);
    }

    /**
     * 根据进制基数数组生成字符到数值的映射表
     * @param radixValues 进制基数数组，索引为字符位置，值为对应数值
     * @return 字符到数值的映射数组，使用ASCII码作为索引
     */
    public static int[] getValueCharMaps(char[] radixValues) {
        return RadixOps.getValueCharMaps(radixValues);
    }

    /**
     * 任意进制字符串转换为另一种进制字符串（使用默认62进制字符集）
     * @param str 待转换的字符串
     * @param srcRadix 源进制
     * @param toRadix 目标进制
     * @return 转换后的字符串
     */
    public static String toRadix(String str,int srcRadix,int toRadix) {
        return RadixOps.toRadix(str, srcRadix, toRadix);
    }


    /**
     * 十进制字符串转换为指定进制字符串（使用默认62进制字符集）
     * @param str 十进制字符串
     * @param toRadix 目标进制
     * @return 转换后的字符串
     */
    public static String toRadix(String str,int toRadix) {
        return RadixOps.toRadix(str, toRadix);
    }

    /**
     * 十进制字符串转换为指定进制字符串
     * @param str 十进制字符串
     * @param radixValues 目标进制字符集
     * @return 转换后的字符串
     */
    public static String toRadix(String str,char[] radixValues) {
        return RadixOps.toRadix(str, radixValues);
    }

    /**
     * 任意进制数据转换为另一种进制
     * @param str 待转换的字符串
     * @param radixValues 目标进制字符集
     * @param srcRadix 源进制
     * @param toRadix 目标进制
     * @return 转换后的字符串
     */
    public static String toRadix(String str,char[] radixValues,int srcRadix,int toRadix) {
        return RadixOps.toRadix(str, radixValues, srcRadix, toRadix);
    }

    /**
     * 十进制 long 值转换为指定进制字符串（使用默认62进制字符集）
     *
     * 免字符串往返的高频场景便捷入口，结果与 {@link #toRadix(String, int)} 一致。
     *
     * @param value 十进制数值
     * @param toRadix 目标进制，范围 2 ~ 62
     * @return 转换后的字符串
     * @throws IllegalArgumentException 目标进制小于 2 时抛出，避免 radix=1 时除留余数法死循环
     */
    public static String toRadix(long value, int toRadix) {
        if (toRadix < 2) {
            throw new IllegalArgumentException("radix must be >= 2: " + toRadix);
        }
        return RadixOps.toRadix(String.valueOf(value), toRadix);
    }

    /**
     * 将任意进制字符串解码为十进制BigInteger（使用指定字符集）
     * @param str 数字字符串，其进制由radixValues长度决定
     * @param radixValues 进制字符集
     * @return 十进制BigInteger
     */
    public static BigInteger toDecimal(String str,char[] radixValues) {
        return RadixOps.toDecimal(str, radixValues);
    }

    /**
     * 将任意进制字符串解码为十进制BigInteger
     * @param str 数字字符串，其进制值等同于baseChars的长度
     * @param radixValues 进制字符集
     * @param srcRadix 源进制
     * @return 十进制BigInteger
     */
    public static BigInteger toDecimal(String str,char[] radixValues,int srcRadix) {
        return RadixOps.toDecimal(str, radixValues, srcRadix);
    }

    /**
     * 求两个整数的最大公约数（欧几里得算法）
     * @param a 第一个整数
     * @param b 第二个整数
     * @return 最大公约数
     */
    public static int gcd(int a, int b){
        // 入口取绝对值（升级为 long 计算，避免 Integer.MIN_VALUE 取绝对值溢出）
        long la = Math.abs((long) a);
        long lb = Math.abs((long) b);
        while (lb > 0) {
            long r = la % lb;
            la = lb;
            lb = r;
        }
        return (int) la;
    }

    /**
     * 求两个整数的最小公倍数
     * @param m 第一个整数
     * @param n 第二个整数
     * @return 最小公倍数
     */
    public static int lcm(int m, int n) {
        // 根据防溢出策略：强制升级为 long 计算，天然避免溢出
        int gcdValue = gcd(m , n);
        long divided = (long) m / gcdValue;
        long result = divided * n;
        
        // 检查结果是否在 int 范围内
        if (result > Integer.MAX_VALUE || result < Integer.MIN_VALUE) {
            throw new ArithmeticException("最小公倍数计算溢出: " + result);
        }
        
        return (int) result;
    }

    /**
     * 求两个长整数的最大公约数（欧几里得算法）
     * @param a 第一个长整数
     * @param b 第二个长整数
     * @return 最大公约数
     */
    public static long gcd(long a, long b){
        // Long.MIN_VALUE 的绝对值超出 long 表示范围，转 BigInteger 计算避免溢出
        if (a == Long.MIN_VALUE || b == Long.MIN_VALUE) {
            return BigInteger.valueOf(a).gcd(BigInteger.valueOf(b)).longValue();
        }
        long la = Math.abs(a);
        long lb = Math.abs(b);
        while (lb > 0) {
            long r = la % lb;
            la = lb;
            lb = r;
        }
        return la;
    }

    /**
     * 求两个长整数的最小公倍数
     * @param m 第一个长整数
     * @param n 第二个长整数
     * @return 最小公倍数
     */
    public static long lcm(long m, long n) {
        // 根据防溢出策略：使用 Math.multiplyExact 检测溢出
        long gcdValue = gcd(m , n);
        long divided = m / gcdValue;
        return Math.multiplyExact(divided, n);
    }

    /**
     * 求两个大整数的最大公约数
     * @param a 第一个大整数
     * @param b 第二个大整数
     * @return 最大公约数
     */
    public static BigInteger gcd(BigInteger a, BigInteger b) {
        return a.gcd(b);
    }

    /**
     * 求两个大整数的最小公倍数
     * @param a 第一个大整数
     * @param b 第二个大整数
     * @return 最小公倍数
     */
    public static BigInteger lcm(BigInteger a, BigInteger b) {
        BigInteger gcdValue = gcd(a , b);
        return a.divide(gcdValue).multiply(b);
    }

    /**
     * 判断 long 值是否为 2 的幂
     *
     * @param value 待判断的数值
     * @return 是 2 的幂返回 true；0、负数及非 2 的幂返回 false
     */
    public static boolean isPowerOfTwo(long value) {
        return value > 0 && (value & (value - 1)) == 0;
    }

    /**
     * 将 int 值夹取到 [min, max] 区间
     *
     * @param value 待夹取的数值
     * @param min 区间下界
     * @param max 区间上界
     * @return 夹取后的值
     * @throws IllegalArgumentException min 大于 max 时抛出
     */
    public static int clamp(int value, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max: min=" + min + ", max=" + max);
        }
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 将 long 值夹取到 [min, max] 区间
     *
     * @param value 待夹取的数值
     * @param min 区间下界
     * @param max 区间上界
     * @return 夹取后的值
     * @throws IllegalArgumentException min 大于 max 时抛出
     */
    public static long clamp(long value, long min, long max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max: min=" + min + ", max=" + max);
        }
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 将 double 值夹取到 [min, max] 区间
     *
     * @param value 待夹取的数值
     * @param min 区间下界
     * @param max 区间上界
     * @return 夹取后的值
     * @throws IllegalArgumentException min 大于 max 时抛出
     */
    public static double clamp(double value, double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max: min=" + min + ", max=" + max);
        }
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 求长整数的整数平方根（向下取整）
     *
     * 整数牛顿法估算后双向校正；比较一律用除法而非乘法，
     * 避免 x 接近 3037000500 时 (x+1)² 超出 long 范围溢出。
     *
     * @param value 非负长整数
     * @return 满足 x² ≤ value < (x+1)² 的最大整数 x
     * @throws IllegalArgumentException value 为负数时抛出
     */
    public static long isqrt(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("value must be >= 0: " + value);
        }
        if (value < 2) {
            return value;
        }
        long x = (long) Math.sqrt(value);
        // 向下校正：除法比较避免 x² 溢出
        while (x > value / x) {
            x--;
        }
        // 向上校正：除法比较避免 (x+1)² 溢出
        while (x + 1 <= value / (x + 1)) {
            x++;
        }
        return x;
    }

    /**
     * 求 n 的阶乘（BigInteger 精确返回）
     *
     * @param n 非负整数
     * @return n!，其中 0! = 1
     * @throws IllegalArgumentException n 为负数时抛出
     */
    public static BigInteger factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0: " + n);
        }
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }

    /**
     * 求组合数 C(n, k)（BigInteger 精确返回）
     *
     * 取 k = min(k, n-k) 对称优化，乘除交替迭代，
     * 利用组合数递推性质保证每步整除，避免 factorial 大中间值。
     *
     * @param n 总数，非负
     * @param k 选取数，范围 [0, n]
     * @return C(n, k)
     * @throws IllegalArgumentException n < 0、k < 0 或 k > n 时抛出
     */
    public static BigInteger combinationCount(int n, int k) {
        if (n < 0 || k < 0 || k > n) {
            throw new IllegalArgumentException("invalid combination params: n=" + n + ", k=" + k);
        }
        k = Math.min(k, n - k);
        BigInteger result = BigInteger.ONE;
        for (int i = 1; i <= k; i++) {
            result = result.multiply(BigInteger.valueOf(n - k + i)).divide(BigInteger.valueOf(i));
        }
        return result;
    }
}