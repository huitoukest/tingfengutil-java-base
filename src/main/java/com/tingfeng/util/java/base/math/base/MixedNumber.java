package com.tingfeng.util.java.base.math.base;

import java.math.BigInteger;

/**
 * 带分数抽象基类，用于表示假分数的整数部分和分数部分
 * 例如：7/3 = 2 1/3，其中整数部分是2，分数部分是1/3
 * 
 * @param <N> 数值类型
 * @author huitoukest
 */
public abstract class MixedNumber<N extends Number> implements Comparable<MixedNumber<? extends Number>> {
    
    /**
     * 获取整数部分
     * @return 整数部分
     */
    public abstract N getWholePart();
    
    /**
     * 获取分数部分的分子
     * @return 分数部分的分子
     */
    public abstract N getNumerator();
    
    /**
     * 获取分数部分的分母
     * @return 分数部分的分母
     */
    public abstract N getDenominator();
    
    /**
     * 判断是否为正数
     * @return true 如果带分数值大于 0
     */
    public abstract boolean isPositive();
    
    /**
     * 是否为负数
     * @return true 如果带分数值小于 0
     */
    public abstract boolean isNegative();
    
    /**
     * 是否为零
     * @return true 如果带分数值等于 0
     */
    public abstract boolean isZero();
    
    /**
     * 转换为假分数的分子
     * 计算公式：wholePart * denominator + (sign * numerator)
     * @return 假分数的分子
     */
    public abstract N toImproperNumerator();
    
    /**
     * 转换为假分数的分母
     * @return 假分数的分母
     */
    public abstract N toImproperDenominator();
    
    /**
     * 转换为小数形式
     * @return 小数表示
     */
    public abstract double toDouble();
    
    /**
     * 获取标准化的字符串表示
     * 例如：2 1/3, -1 2/5, 0 3/4
     * @return 标准化字符串
     */
    @Override
    public abstract String toString();
    
    /**
     * 获取紧凑的字符串表示
     * 例如：2'1/3, -1'2/5
     * @return 紧凑字符串
     */
    public abstract String toCompactString();
    
    @Override
    public abstract boolean equals(Object obj);
    
    @Override
    public abstract int hashCode();
    
    /**
     * 与另一个带分数比较大小
     *
     * 使用 BigInteger 交叉相乘全序精确比较，无舍入、无精度损失：
     * 1. 整数部分/分子/分母三件套经 BigInteger 转换（Integer/Long/BigInteger 均安全）
     * 2. 按符号约定构造假分数分子 improper = whole × den + (whole.signum() >= 0 ? num : -num)，
     *    与各子类 toImproperNumerator 语义一致（分数部分恒正，符号由整数部分决定）
     * 3. 比较 improperA × denB 与 improperB × denA，分母恒正无符号翻转
     *
     * @param other 另一个带分数
     * @return 负数/零/正数，分别表示小于/等于/大于
     * @throws NullPointerException 当 other 为 null 时抛出
     */
    @Override
    public int compareTo(MixedNumber<? extends Number> other) {
        if (other == null) {
            throw new NullPointerException("比较对象不能为 null");
        }
        BigInteger wholeA = new BigInteger(getWholePart().toString());
        BigInteger numeratorA = new BigInteger(getNumerator().toString());
        BigInteger denominatorA = new BigInteger(getDenominator().toString());
        BigInteger wholeB = new BigInteger(other.getWholePart().toString());
        BigInteger numeratorB = new BigInteger(other.getNumerator().toString());
        BigInteger denominatorB = new BigInteger(other.getDenominator().toString());
        
        BigInteger improperA = wholeA.multiply(denominatorA)
                .add(wholeA.signum() >= 0 ? numeratorA : numeratorA.negate());
        BigInteger improperB = wholeB.multiply(denominatorB)
                .add(wholeB.signum() >= 0 ? numeratorB : numeratorB.negate());
        
        return improperA.multiply(denominatorB).compareTo(improperB.multiply(denominatorA));
    }
}