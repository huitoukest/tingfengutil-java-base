package com.tingfeng.util.java.base.common.inter;

import com.tingfeng.util.java.base.common.bean.fraction.AbstractFraction;

/**
 * 分数运算操作接口，定义了分数的基本数学运算和行为
 * 支持线程安全的分数计算，提供溢出保护和异常处理
 * 
 * @param <T> 分数类型，必须是 AbstractFraction 的子类
 * @author huitoukest
 */
public interface IFractionOperation<T extends AbstractFraction> extends Comparable<T>{
    
    /**
     * 当前对象加上 other 值
     * 线程安全，自动处理溢出情况
     * @param other 需要加的对象，不能为 null
     * @return 相加后的新分数对象
     * @throws ArithmeticException 当计算结果溢出时抛出
     * @throws NullPointerException 当 other 为 null 时抛出
     */
    T add(T other);

    /**
     * 当前对象减去 other 对象
     * 线程安全，自动处理溢出情况
     * @param other 需要减去的值，不能为 null
     * @return 相减后的新分数对象
     * @throws ArithmeticException 当计算结果溢出时抛出
     * @throws NullPointerException 当 other 为 null 时抛出
     */
    T sub(T other);

    /**
     * 当前对象乘以 other 对象
     * 线程安全，自动处理溢出情况
     * @param other 需要乘以的数值，不能为 null
     * @return 相乘后的新分数对象
     * @throws ArithmeticException 当计算结果溢出时抛出
     * @throws NullPointerException 当 other 为 null 时抛出
     */
    T multiply(T other);

    /**
     * 当前对象除以 other 对象
     * 线程安全，自动检查除零错误
     * @param other 需要除的数值，不能为 null 且分子不能为 0
     * @return 当前对象除以 other 对象的结果
     * @throws ArithmeticException 当 other 的分子为 0（除零错误）或计算溢出时抛出
     * @throws NullPointerException 当 other 为 null 时抛出
     */
    T div(T other);

    /**
     * 判断当前对象和 other 是否相等
     * 通过比较简化后的分数形式来判断数学上的相等性
     * @param other 需要判断的对象，不能为 null
     * @return 是否相等的结果
     * @throws NullPointerException 当 other 为 null 时抛出
     */
    default boolean eq(T other){
        if (other == null) {
            throw new NullPointerException("比较对象不能为 null");
        }
        return this.compareTo(other) == 0;
    }

    /**
     * 当前值是否大于 other 值
     * @param other 需要比较的对象，不能为 null
     * @return 是否大于
     * @throws NullPointerException 当 other 为 null 时抛出
     */
    default boolean greatThan(T other){
        if (other == null) {
            throw new NullPointerException("比较对象不能为 null");
        }
        return this.compareTo(other) > 0;
    }

    /**
     * 当前值是否小于 other 值
     * @param other 需要比较的对象，不能为 null
     * @return 是否小于
     * @throws NullPointerException 当 other 为 null 时抛出
     */
    default boolean letterThan(T other){
        if (other == null) {
            throw new NullPointerException("比较对象不能为 null");
        }
        return this.compareTo(other) < 0;
    }
    
    /**
     * 当前值是否大于等于 other 值
     * @param other 需要比较的对象，不能为 null
     * @return 是否大于等于
     * @throws NullPointerException 当 other 为 null 时抛出
     */
    default boolean greatThanOrEqual(T other) {
        if (other == null) {
            throw new NullPointerException("比较对象不能为 null");
        }
        return this.compareTo(other) >= 0;
    }
    
    /**
     * 当前值是否小于等于 other 值
     * @param other 需要比较的对象，不能为 null
     * @return 是否小于等于
     * @throws NullPointerException 当 other 为 null 时抛出
     */
    default boolean letterThanOrEqual(T other) {
        if (other == null) {
            throw new NullPointerException("比较对象不能为 null");
        }
        return this.compareTo(other) <= 0;
    }

    /**
     * 求自身的最简分数（约分）
     * 自动处理符号位：分母始终为正，符号由分子决定
     * 当分子为 0 时，返回 0/1
     * 
     * @return 最简分数形式的新对象
     */
    T simpleFraction();
    
    /**
     * 获取当前分数的绝对值
     * 返回一个新的分数对象，符号为正
     * @return 绝对值分数
     */
    T abs();
    
    /**
     * 获取当前分数的倒数（1/当前分数）
     * 注意：当前分数不能为 0
     * @return 倒数分数
     * @throws ArithmeticException 当当前分数为 0 时抛出
     */
    T reciprocal();
    
    /**
     * 判断是否为真分数（分子绝对值小于分母）
     * @return 是否为真分数
     */
    default boolean isProperFraction() {
        return Math.abs(getNumerator().longValue()) < Math.abs(getDenominator().longValue());
    }
    
    /**
     * 判断是否为假分数（分子绝对值大于等于分母）
     * @return 是否为假分数
     */
    default boolean isImproperFraction() {
        return Math.abs(getNumerator().longValue()) >= Math.abs(getDenominator().longValue());
    }
    
    /**
     * 获取分子值
     * @return 分子
     */
    <N extends Number> N getNumerator();
    
    /**
     * 获取分母值  
     * @return 分母
     */
    <N extends Number> N getDenominator();
    
    /**
     * 创建分数的工厂方法
     * 用于在接口默认方法中创建新的分数实例
     * @param numerator 分子
     * @param denominator 分母
     * @return 新的分数对象
     */
    T createFraction(Number numerator, Number denominator);
    
    /**
     * 判断是否为正数
     * @return true 如果值 > 0
     */
    boolean isPositive();
    
    /**
     * 判断是否为 0
     * @return true 如果值 = 0
     */
    boolean isZero();
    
    /**
     * 判断是否为负数
     * @return true 如果值 < 0
     */
    boolean isNegative();
}