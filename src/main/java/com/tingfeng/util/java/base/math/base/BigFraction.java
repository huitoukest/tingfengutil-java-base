package com.tingfeng.util.java.base.math.base;

import com.tingfeng.util.java.base.common.inter.IFractionOperation;
import com.tingfeng.util.java.base.math.MathUtils;
import com.tingfeng.util.java.base.lang.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 大整数分数类，适用于超大数值的分数计算
 * 分子分母使用 BigInteger 类型，理论上没有数值范围限制
 * 适合处理超大整数的精确分数计算
 * 
 * 特性：
 * - 线程安全：不可变对象，每次运算生成新对象
 * - 无溢出：BigInteger 自动处理大数运算
 * - 高效计算：简化分数时直接返回新对象
 * - 符号规范：分母始终为正，符号由分子决定
 * 
 * @author huitoukest
 */
public class BigFraction extends AbstractFraction implements IFractionOperation<BigFraction>, Comparable<BigFraction> {
    
    /**
     * 分子
     */
    private final BigInteger numerator;
    
    /**
     * 分母
     */
    private final BigInteger denominator;
    /**
     * 构造函数
     * @param numerator 分子
     * @param denominator 分母，不能为 0
     * @throws ArithmeticException 当分母为 0 时抛出
     */
    public BigFraction(String numerator, String denominator) {
        this(new BigInteger(numerator), new BigInteger(denominator));
    }
    /**
     * 构造函数
     * @param numerator 分子
     * @param denominator 分母，不能为 0
     * @throws ArithmeticException 当分母为 0 时抛出
     */
    public BigFraction(BigInteger numerator, BigInteger denominator) {
        if (numerator == null || denominator == null) {
            throw new NullPointerException("分子和分母不能为 null");
        }
        if (BigInteger.ZERO.equals(denominator)) {
            throw new ArithmeticException("分母不能为 0");
        }
        // 标准化：分母始终为正，符号由分子决定
        if (denominator.signum() < 0) {
            this.numerator = numerator.negate();
            this.denominator = denominator.negate();
        } else {
            this.numerator = numerator;
            this.denominator = denominator;
        }
    }

    /**
     * 构造函数（long 类型）
     * @param numerator 分子
     * @param denominator 分母，不能为 0
     * @throws ArithmeticException 当分母为 0 时抛出
     */
    public BigFraction(long numerator, long denominator) {
        if (denominator == 0) {
            throw new ArithmeticException("分母不能为 0");
        }
        // 标准化：分母始终为正，符号由分子决定
        if (denominator < 0) {
            this.numerator = BigInteger.valueOf(-numerator);
            this.denominator = BigInteger.valueOf(-denominator);
        } else {
            this.numerator = BigInteger.valueOf(numerator);
            this.denominator = BigInteger.valueOf(denominator);
        }
    }

    /**
     * 默认构造函数，创建 0/1 分数
     */
    public BigFraction() {
        this.numerator = BigInteger.ZERO;
        this.denominator = BigInteger.ONE;
    }

    /**
     * 从标准的 A/B 格式字符串解析分数
     * @param str 分数字符串，格式为 "分子/分母"
     * @throws IllegalArgumentException 当格式不正确或分母为 0 时抛出
     */
    public BigFraction(String str) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException("分数字符串不能为空");
        }
        
        String[] parts = str.trim().split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("分数字符串格式错误，应为 '分子/分母' 格式");
        }
        
        try {
            BigInteger num = new BigInteger(parts[0].trim());
            BigInteger den = new BigInteger(parts[1].trim());
            
            if (BigInteger.ZERO.equals(den)) {
                throw new ArithmeticException("分母不能为 0");
            }
            
            // 标准化：分母始终为正，符号由分子决定
            if (den.signum() < 0) {
                this.numerator = num.negate();
                this.denominator = den.negate();
            } else {
                this.numerator = num;
                this.denominator = den;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("分数字符串包含非数字字符: " + str);
        }
    }

    /**
     * 默认保留15位小数，四舍五入原则
     * @return double 值
     */
    @Override
    public double toDouble() {
        return toBigDecimal(15, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 转换为 BigDecimal 类型
     * @param newScale 小数位数
     * @param roundingMode 舍入模式
     * @return BigDecimal 表示
     */
    @Override
    BigDecimal toBigDecimal(int newScale, RoundingMode roundingMode) {
        BigDecimal bigDecimal = new BigDecimal(this.numerator);
        return bigDecimal.divide(new BigDecimal(this.denominator), newScale, roundingMode);
    }

    /**
     * 转换为指定精度的字符串
     * @param newScale 小数位数
     * @return 字符串表示
     */
    @Override
    String toString(int newScale) {
        return toBigDecimal(newScale, RoundingMode.HALF_UP).toString();
    }

    /**
     * 获取标准分数字符串
     * @return 如 "3/4"
     */
    @Override
    String getValue() {
        return StringUtils.append(this.numerator.toString(), "/", this.denominator.toString());
    }

    /**
     * 判断是否为正数
     * @return true 如果值 > 0
     */
    @Override
    public boolean isPositive() {
        return numerator.signum() > 0;
    }

    /**
     * 判断是否为 0
     * @return true 如果值 = 0
     */
    @Override
    public boolean isZero() {
        return BigInteger.ZERO.equals(this.numerator);
    }

    /**
     * 判断是否为负数
     * @return true 如果值 < 0
     */
    @Override
    public boolean isNegative() {
        return numerator.signum() < 0;
    }

    /**
     * 加法运算
     * @param other 另一个分数
     * @return 相加结果
     * @throws NullPointerException 当 other 为 null 时抛出
     */
    @Override
    public BigFraction add(BigFraction other) {
        if (other == null) {
            throw new NullPointerException("加数不能为 null");
        }
        
        BigFraction a = this.simpleFraction();
        BigFraction b = other.simpleFraction();
        
        // 计算最小公倍数
        BigInteger lcm = MathUtils.lcm(a.denominator, b.denominator);
        
        // 计算新分子
        BigInteger newNumerator = a.numerator.multiply(lcm).divide(a.denominator)
                                       .add(b.numerator.multiply(lcm).divide(b.denominator));
        
        return new BigFraction(newNumerator, lcm).simpleFraction();
    }

    /**
     * 减法运算
     * @param other 另一个分数
     * @return 相减结果
     * @throws NullPointerException 当 other 为 null 时抛出
     */
    @Override
    public BigFraction sub(BigFraction other) {
        if (other == null) {
            throw new NullPointerException("减数不能为 null");
        }
        
        // 转换为加法：a - b = a + (-b)
        BigFraction negativeOther = new BigFraction(other.numerator.negate(), other.denominator);
        return this.add(negativeOther);
    }

    /**
     * 乘法运算
     * @param other 另一个分数
     * @return 相乘结果
     * @throws NullPointerException 当 other 为 null 时抛出
     */
    @Override
    public BigFraction multiply(BigFraction other) {
        if (other == null) {
            throw new NullPointerException("乘数不能为 null");
        }
        
        if (this.isZero() || other.isZero()) {
            return new BigFraction(BigInteger.ZERO, BigInteger.ONE);
        }
        
        BigFraction a = this.simpleFraction();
        BigFraction b = other.simpleFraction();
        
        // 计算分子和分母的乘积
        BigInteger newNumerator = a.numerator.multiply(b.numerator);
        BigInteger newDenominator = a.denominator.multiply(b.denominator);
        
        return new BigFraction(newNumerator, newDenominator).simpleFraction();
    }

    /**
     * 除法运算
     * @param other 另一个分数
     * @return 相除结果
     * @throws NullPointerException 当 other 为 null 时抛出
     * @throws ArithmeticException 当 other 为 0（除零错误）时抛出
     */
    @Override
    public BigFraction div(BigFraction other) {
        if (other == null) {
            throw new NullPointerException("除数不能为 null");
        }
        
        if (other.isZero()) {
            throw new ArithmeticException("除零错误：不能除以 0");
        }
        
        // 转换为乘法：a / b = a * (1/b)
        BigFraction reciprocal = new BigFraction(other.denominator, other.numerator);
        return this.multiply(reciprocal);
    }

    /**
     * 获取最简分数形式
     * @return 最简分数
     */
    @Override
    public BigFraction simpleFraction() {
        if (BigInteger.ZERO.equals(numerator)) {
            return new BigFraction(BigInteger.ZERO, BigInteger.ONE);
        }
        
        BigInteger absNumerator = numerator.abs();
        BigInteger absDenominator = denominator.abs();
        BigInteger gcd = MathUtils.gcd(absNumerator, absDenominator);
        
        if (gcd.equals(BigInteger.ONE)) {
            return this;
        }
        
        // 符号处理：分母为正，符号由分子决定
        BigInteger newNumerator = numerator.divide(gcd);
        BigInteger newDenominator = absDenominator.divide(gcd);
        
        return new BigFraction(newNumerator, newDenominator);
    }

    /**
     * 比较两个分数
     * @param other 另一个分数
     * @return 比较结果
     */
    @Override
    public int compareTo(BigFraction other) {
        if (other == null) {
            throw new NullPointerException("比较对象不能为 null");
        }
        
        // 快速路径：如果引用相同
        if (this == other) {
            return 0;
        }
        
        // 快速路径：符号不同
        boolean thisPositive = this.isPositive();
        boolean otherPositive = other.isPositive();
        
        if (thisPositive && !otherPositive) {
            return 1;
        }
        if (!thisPositive && otherPositive) {
            return -1;
        }
        
        // 处理零的情况
        if (this.isZero()) {
            return other.isZero() ? 0 : (otherPositive ? -1 : 1);
        }
        if (other.isZero()) {
            return thisPositive ? 1 : -1;
        }
        
        // 交叉相乘比较（避免除法，提高精度）
        BigFraction a = this.simpleFraction();
        BigFraction b = other.simpleFraction();
        
        // a/b vs c/d => 比较 a*d vs c*b
        BigInteger left = a.numerator.multiply(b.denominator);
        BigInteger right = b.numerator.multiply(a.denominator);
        
        return left.compareTo(right);
    }

    /**
     * 判断是否是最简分数
     * @return true 如果已经是最简形式
     */
    @Override
    public boolean isSimpleFraction() {
        if (BigInteger.ZERO.equals(numerator)) {
            return BigInteger.ONE.equals(denominator);
        }
        
        BigInteger absNumerator = numerator.abs();
        BigInteger absDenominator = denominator.abs();
        BigInteger gcd = MathUtils.gcd(absNumerator, absDenominator);
        
        return BigInteger.ONE.equals(gcd);
    }

    /**
     * 获取分子
     * @return 分子
     */
    @Override
    public BigInteger getNumerator() {
        return numerator;
    }

    /**
     * 获取分母
     * @return 分母
     */
    @Override
    public BigInteger getDenominator() {
        return denominator;
    }

    /**
     * 创建分数的工厂方法
     * @param numerator 分子
     * @param denominator 分母
     * @return 新的分数对象
     */
    @Override
    public BigFraction createFraction(Number numerator, Number denominator) {
        return new BigFraction(
            numerator instanceof BigInteger ? (BigInteger) numerator : BigInteger.valueOf(numerator.longValue()),
            denominator instanceof BigInteger ? (BigInteger) denominator : BigInteger.valueOf(denominator.longValue())
        );
    }

    /**
     * 重写 equals 方法，基于简化后的形式比较
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        BigFraction other = (BigFraction) obj;
        return this.compareTo(other) == 0;
    }

    /**
     * 重写 hashCode 方法，基于简化后的形式
     */
    @Override
    public int hashCode() {
        BigFraction simplified = this.simpleFraction();
        return Objects.hash(simplified.numerator, simplified.denominator);
    }

    /**
     * 重写 toString 方法
     */
    @Override
    public String toString() {
        return String.format("BigFraction{%s/%s}", numerator.toString(), denominator.toString());
    }
    
    /**
     * 获取当前分数的绝对值
     * @return 绝对值分数
     */
    @Override
    public BigFraction abs() {
        if (isPositive() || isZero()) {
            return this;
        }
        return new BigFraction(numerator.abs(), denominator);
    }
    
    /**
     * 获取当前分数的倒数
     * @return 倒数分数
     * @throws ArithmeticException 当当前分数为 0 时抛出
     */
    @Override
    public BigFraction reciprocal() {
        if (numerator == null || numerator.equals(BigInteger.ZERO)) {
            throw new ArithmeticException("无法计算 0 的倒数");
        }
        return new BigFraction(denominator, numerator).simpleFraction();
    }
    
    /**
     * 转换为带分数形式
     * @return 带分数对象
     */
    @Override
    public MixedNumber toMixedNumber() {
        BigFraction simplified = this.simpleFraction();
        if (simplified.isZero()) {
            return new BigMixedNumber(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ONE);
        }
        
        BigInteger absNumerator = simplified.numerator.abs();
        BigInteger[] divisionResult = absNumerator.divideAndRemainder(simplified.denominator);
        BigInteger wholePart = divisionResult[0];
        BigInteger numeratorPart = divisionResult[1];
        
        if (simplified.isNegative()) {
            wholePart = wholePart.negate();
        }
        
        return new BigMixedNumber(wholePart, numeratorPart, simplified.denominator);
    }
    
    /**
     * 比较两个分数
     * @param other 另一个分数
     * @return 比较结果
     */
    @Override
    public int compareTo(AbstractFraction other) {
        if (other == null) {
            throw new NullPointerException("比较对象不能为 null");
        }
        if (!(other instanceof BigFraction)) {
            throw new IllegalArgumentException("只能与 BigFraction 类型比较");
        }
        return compareTo((BigFraction) other);
    }
}