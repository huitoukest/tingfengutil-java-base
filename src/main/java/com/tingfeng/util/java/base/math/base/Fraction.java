package com.tingfeng.util.java.base.math.base;

/**
 * 分数快速使用类，作为 LongFraction 的别名
 * 提供简洁的 API 用于一般的分数计算
 * 
 * 特性：
 * - 基于 LongFraction，性能优异
 * - 数值范围：分子分母在 long 范围内
 * - 线程安全：继承 LongFraction 的线程安全特性
 * 
 * 使用示例：
 * <pre>
 * Fraction f1 = new Fraction(1, 3);
 * Fraction f2 = new Fraction(1, 4);
 * Fraction result = f1.add(f2); // 7/12
 * </pre>
 * 
 * @author huitoukest
 * @see LongFraction
 */
public class Fraction extends LongFraction {
    
    /**
     * 构造函数
     * @param numerator 分子
     * @param denominator 分母，不能为 0
     * @throws ArithmeticException 当分母为 0 时抛出
     */
    public Fraction(long numerator, long denominator) {
        super(numerator, denominator);
    }
    
    /**
     * 默认构造函数，创建 0/1 分数
     */
    public Fraction() {
        super();
    }
    
    /**
     * 从字符串解析分数
     * @param str 分数字符串，格式为 "分子/分母"
     * @throws IllegalArgumentException 当格式不正确或分母为 0 时抛出
     */
    public Fraction(String str) {
        super(str);
    }
    
    /**
     * 创建分数的工厂方法（覆盖父类方法，返回 Fraction 类型）
     * @param numerator 分子
     * @param denominator 分母
     * @return 新的分数对象
     */
    @Override
    public Fraction createFraction(Number numerator, Number denominator) {
        return new Fraction(numerator.longValue(), denominator.longValue());
    }
    
    /**
     * 重写 toString 方法，显示为 Fraction 类型
     */
    @Override
    public String toString() {
        return String.format("Fraction{%d/%d}", getNumerator().longValue(), getDenominator().longValue());
    }
}