package com.tingfeng.util.java.base.math.base;

/**
 * 带分数抽象基类，用于表示假分数的整数部分和分数部分
 * 例如：7/3 = 2 1/3，其中整数部分是2，分数部分是1/3
 * 
 * @param <N> 数值类型
 * @author huitoukest
 */
public abstract class MixedNumber<N extends Number> {
    
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
}