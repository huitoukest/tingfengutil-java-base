package com.tingfeng.util.java.base.math.base;

import java.util.Objects;

/**
 * 长整数带分数类，用于表示由整数部分和分数部分组成的带分数
 * 例如：7/3 = 2 1/3，其中整数部分是2，分数部分是1/3
 * 
 * 特性：
 * - 线程安全：所有操作都是无状态的
 * - 符号处理：符号由整数部分决定，分数部分始终为正
 * - 自动规范化：分数部分始终是最简形式
 * 
 * @author huitoukest
 */
public class LongMixedNumber extends MixedNumber<Long> {
    
    /**
     * 整数部分
     */
    private final long wholePart;
    
    /**
     * 分数部分的分子
     */
    private final long numerator;
    
    /**
     * 分数部分的分母
     */
    private final long denominator;

    /**
     * 构造函数
     * @param wholePart 整数部分
     * @param numerator 分数部分的分子，必须是非负数
     * @param denominator 分数部分的分母，必须是正数
     * @throws ArithmeticException 当分母为 0 时抛出
     * @throws IllegalArgumentException 当分子为负数或分母为非正数时抛出
     */
    public LongMixedNumber(long wholePart, long numerator, long denominator) {
        if (denominator <= 0) {
            throw new ArithmeticException("分母必须为正数");
        }
        if (numerator < 0) {
            throw new IllegalArgumentException("分子必须为非负数");
        }
        
        // 规范化：确保分数部分是真分数且为最简形式
        long gcd = gcd(numerator, denominator);
        long simplifiedNumerator = numerator / gcd;
        long simplifiedDenominator = denominator / gcd;
        
        // 如果分数部分大于等于1，转换到整数部分
        long additionalWhole = simplifiedNumerator / simplifiedDenominator;
        if (additionalWhole > 0) {
            this.wholePart = wholePart + (wholePart >= 0 ? additionalWhole : -additionalWhole);
            this.numerator = simplifiedNumerator % simplifiedDenominator;
            this.denominator = simplifiedDenominator;
        } else {
            this.wholePart = wholePart;
            this.numerator = simplifiedNumerator;
            this.denominator = simplifiedDenominator;
        }
    }

    /**
     * 计算最大公约数
     * @param a 第一个数
     * @param b 第二个数
     * @return 最大公约数
     */
    private long gcd(long a, long b) {
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    /**
     * 获取整数部分
     * @return 整数部分
     */
    @Override
    public Long getWholePart() {
        return wholePart;
    }

    /**
     * 获取分数部分的分子
     * @return 分数部分的分子
     */
    @Override
    public Long getNumerator() {
        return numerator;
    }

    /**
     * 获取分数部分的分母
     * @return 分数部分的分母
     */
    @Override
    public Long getDenominator() {
        return denominator;
    }

    /**
     * 判断是否为正数
     * @return true 如果带分数值大于 0
     */
    @Override
    public boolean isPositive() {
        return wholePart > 0 || (wholePart == 0 && numerator > 0);
    }

    /**
     * 是否为负数
     * @return true 如果带分数值小于 0
     */
    @Override
    public boolean isNegative() {
        return wholePart < 0;
    }

    /**
     * 是否为零
     * @return true 如果带分数值等于 0
     */
    @Override
    public boolean isZero() {
        return wholePart == 0 && numerator == 0;
    }

    /**
     * 转换为假分数的分子
     * 计算公式：wholePart * denominator + (sign * numerator)
     * @return 假分数的分子
     */
    @Override
    public Long toImproperNumerator() {
        if (wholePart >= 0) {
            return wholePart * denominator + numerator;
        } else {
            return wholePart * denominator - numerator;
        }
    }

    /**
     * 转换为假分数的分母
     * @return 假分数的分母
     */
    @Override
    public Long toImproperDenominator() {
        return denominator;
    }

    /**
     * 转换为小数形式
     * @return 小数表示
     */
    @Override
    public double toDouble() {
        return wholePart + (double) numerator / denominator;
    }

    /**
     * 获取标准化的字符串表示
     * 例如：2 1/3, -1 2/5, 0 3/4
     * @return 标准化字符串
     */
    @Override
    public String toString() {
        if (isZero()) {
            return "0";
        }
        
        if (numerator == 0) {
            return String.valueOf(wholePart);
        }
        
        return String.format("%d %d/%d", wholePart, numerator, denominator);
    }

    /**
     * 获取紧凑的字符串表示
     * 例如：2'1/3, -1'2/5
     * @return 紧凑字符串
     */
    @Override
    public String toCompactString() {
        if (isZero()) {
            return "0";
        }
        
        if (numerator == 0) {
            return String.valueOf(wholePart);
        }
        
        return String.format("%d'%d/%d", wholePart, numerator, denominator);
    }

    /**
     * 重写 equals 方法
     * @param obj 要比较的对象
     * @return true 如果相等
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        LongMixedNumber other = (LongMixedNumber) obj;
        return wholePart == other.wholePart &&
               numerator == other.numerator &&
               denominator == other.denominator;
    }

    /**
     * 重写 hashCode 方法
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(wholePart, numerator, denominator);
    }
}