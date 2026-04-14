package com.tingfeng.util.java.base.math.base;

import java.math.BigInteger;
import java.util.Objects;

/**
 * 大整数带分数类，用于表示由整数部分和分数部分组成的带分数
 * 例如：7/3 = 2 1/3，其中整数部分是2，分数部分是1/3
 * 
 * 特性：
 * - 线程安全：所有操作都是无状态的
 * - 符号处理：符号由整数部分决定，分数部分始终为正
 * - 自动规范化：分数部分始终是最简形式
 * 
 * @author huitoukest
 */
public class BigMixedNumber extends MixedNumber<BigInteger> {
    
    /**
     * 整数部分
     */
    private final BigInteger wholePart;
    
    /**
     * 分数部分的分子
     */
    private final BigInteger numerator;
    
    /**
     * 分数部分的分母
     */
    private final BigInteger denominator;

    /**
     * 构造函数
     * @param wholePart 整数部分
     * @param numerator 分数部分的分子，必须是非负数
     * @param denominator 分数部分的分母，必须是正数
     * @throws ArithmeticException 当分母为 0 时抛出
     * @throws IllegalArgumentException 当分子为负数或分母为非正数时抛出
     */
    public BigMixedNumber(BigInteger wholePart, BigInteger numerator, BigInteger denominator) {
        if (wholePart == null || numerator == null || denominator == null) {
            throw new NullPointerException("所有参数都不能为 null");
        }
        if (denominator.compareTo(BigInteger.ZERO) <= 0) {
            throw new ArithmeticException("分母必须为正数");
        }
        if (numerator.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("分子必须为非负数");
        }
        
        // 规范化：确保分数部分是真分数且为最简形式
        BigInteger gcd = numerator.gcd(denominator);
        BigInteger simplifiedNumerator = numerator.divide(gcd);
        BigInteger simplifiedDenominator = denominator.divide(gcd);
        
        // 如果分数部分大于等于1，转换到整数部分
        BigInteger[] divisionResult = simplifiedNumerator.divideAndRemainder(simplifiedDenominator);
        BigInteger additionalWhole = divisionResult[0];
        if (additionalWhole.compareTo(BigInteger.ZERO) > 0) {
            this.wholePart = wholePart.add(wholePart.compareTo(BigInteger.ZERO) >= 0 ? additionalWhole : additionalWhole.negate());
            this.numerator = divisionResult[1];
            this.denominator = simplifiedDenominator;
        } else {
            this.wholePart = wholePart;
            this.numerator = simplifiedNumerator;
            this.denominator = simplifiedDenominator;
        }
    }

    /**
     * 获取整数部分
     * @return 整数部分
     */
    @Override
    public BigInteger getWholePart() {
        return wholePart;
    }

    /**
     * 获取分数部分的分子
     * @return 分数部分的分子
     */
    @Override
    public BigInteger getNumerator() {
        return numerator;
    }

    /**
     * 获取分数部分的分母
     * @return 分数部分的分母
     */
    @Override
    public BigInteger getDenominator() {
        return denominator;
    }

    /**
     * 判断是否为正数
     * @return true 如果带分数值大于 0
     */
    @Override
    public boolean isPositive() {
        return wholePart.compareTo(BigInteger.ZERO) > 0 || 
               (wholePart.compareTo(BigInteger.ZERO) == 0 && numerator.compareTo(BigInteger.ZERO) > 0);
    }

    /**
     * 是否为负数
     * @return true 如果带分数值小于 0
     */
    @Override
    public boolean isNegative() {
        return wholePart.compareTo(BigInteger.ZERO) < 0;
    }

    /**
     * 是否为零
     * @return true 如果带分数值等于 0
     */
    @Override
    public boolean isZero() {
        return wholePart.compareTo(BigInteger.ZERO) == 0 && numerator.compareTo(BigInteger.ZERO) == 0;
    }

    /**
     * 转换为假分数的分子
     * 计算公式：wholePart * denominator + (sign * numerator)
     * @return 假分数的分子
     */
    @Override
    public BigInteger toImproperNumerator() {
        if (wholePart.compareTo(BigInteger.ZERO) >= 0) {
            return wholePart.multiply(denominator).add(numerator);
        } else {
            return wholePart.multiply(denominator).subtract(numerator);
        }
    }

    /**
     * 转换为假分数的分母
     * @return 假分数的分母
     */
    @Override
    public BigInteger toImproperDenominator() {
        return denominator;
    }

    /**
     * 转换为小数形式
     * @return 小数表示
     */
    @Override
    public double toDouble() {
        return wholePart.doubleValue() + numerator.doubleValue() / denominator.doubleValue();
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
        
        if (numerator.compareTo(BigInteger.ZERO) == 0) {
            return wholePart.toString();
        }
        
        return String.format("%s %s/%s", wholePart.toString(), numerator.toString(), denominator.toString());
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
        
        if (numerator.compareTo(BigInteger.ZERO) == 0) {
            return wholePart.toString();
        }
        
        return String.format("%s'%s/%s", wholePart.toString(), numerator.toString(), denominator.toString());
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
        
        BigMixedNumber other = (BigMixedNumber) obj;
        return wholePart.equals(other.wholePart) &&
               numerator.equals(other.numerator) &&
               denominator.equals(other.denominator);
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