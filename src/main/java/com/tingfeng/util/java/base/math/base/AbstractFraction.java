package com.tingfeng.util.java.base.common.bean.fraction;

import com.tingfeng.util.java.base.common.inter.IFractionOperation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 分数抽象基类，定义了所有分数类型的基本行为和属性
 * 提供线程安全的分数操作，支持各种数值转换和格式化
 * 
 * @author huitoukest
 */
public abstract class AbstractFraction {
    
    /**
     * 分数值转为小数，默认使用四舍五入，可能精度丢失
     * @return 计算得到的结果值
     */
    abstract public double toDouble();

    /**
     * 转为 BigDecimal 来查看结果
     * @param newScale 保留的小数位数
     * @param roundingMode 小数的末位收尾机制，比如归零或四舍五入
     * @return 当前数值结果
     */
    abstract BigDecimal toBigDecimal(int newScale, RoundingMode roundingMode);

    /**
     * 转为字符串展示值
     * @param newScale 缩放的位数，默认四舍五入
     * @return 分数对应的字符串结果
     */
    abstract String toString(int newScale);

    /**
     * 标准的 A/B 的分数形式，以英文斜杠隔开
     * @return 分数字符串表示，如 "3/4"
     */
    abstract String getValue();

    /**
     * 获取分子值
     * @return 分子数值
     */
    abstract Number getNumerator();
    
    /**
     * 获取分母值
     * @return 分母数值
     */
    abstract Number getDenominator();
    
    /**
     * 转换为带分数形式
     * 例如：7/3 -> 2 1/3
     * @return 带分数对象，包含整数部分和分数部分
     */
    public abstract MixedNumber toMixedNumber();
    
    /**
     * 转换为百分数形式
     * @param scale 小数位数
     * @return 百分数字符串，如 "75.00%"
     */
    public String toPercentage(int scale) {
        BigDecimal decimal = toBigDecimal(scale + 2, RoundingMode.HALF_UP);
        BigDecimal percentage = decimal.multiply(BigDecimal.valueOf(100));
        return percentage.setScale(scale, RoundingMode.HALF_UP).toString() + "%";
    }
    
    /**
     * 创建分数的工厂方法
     * 用于创建新的分数实例
     * @param numerator 分子
     * @param denominator 分母
     * @return 新的分数对象
     */
    protected abstract AbstractFraction createFraction(Number numerator, Number denominator);
    
    /**
     * 检查两个分数是否数学上相等（通过比较简化形式）
     * @param obj 要比较的对象
     * @return true 如果数学上相等
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        AbstractFraction other = (AbstractFraction) obj;
        // 使用 compareTo 方法比较，确保数学上的相等性
        return this.compareTo(other) == 0;
    }
    
    /**
     * 计算哈希码，基于简化后的分数形式
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        if (isZero()) {
            return Objects.hash(0, 1);
        }
        // 使用简化形式计算哈希码，确保数学相等的分数有相同的哈希码
        AbstractFraction simplified = (AbstractFraction) ((IFractionOperation<?>) this).simpleFraction();
        return Objects.hash(simplified.getNumerator(), simplified.getDenominator());
    }
    
    /**
     * 转换为字符串表示
     * @return 分数的字符串表示，如 "IntFraction{numerator=3, denominator=4}"
     */
    @Override
    public String toString() {
        return String.format("%s{numerator=%s, denominator=%s}", 
            getClass().getSimpleName(), getNumerator(), getDenominator());
    }
    
    /**
     * 比较两个分数
     * @param other 另一个分数
     * @return 比较结果
     */
    public abstract int compareTo(AbstractFraction other);
    
    /**
     * 判断是否为正数
     * @return true 如果值 > 0
     */
    public abstract boolean isPositive();
    
    /**
     * 判断是否为 0
     * @return true 如果值 = 0
     */
    public abstract boolean isZero();
    
    /**
     * 判断是否为负数
     * @return true 如果值 < 0
     */
    public abstract boolean isNegative();
    
    /**
     * 是否是最简分数
     * @return true 如果分子分母互质
     */
    public abstract boolean isSimpleFraction();
}