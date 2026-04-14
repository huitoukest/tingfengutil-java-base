package com.tingfeng.util.java.base.math.base;

import com.tingfeng.util.java.base.common.inter.IFractionOperation;
import com.tingfeng.util.java.base.math.MathUtils;
import com.tingfeng.util.java.base.lang.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 长整数分数类，适用于较大数值的分数计算
 * 分子分母使用 long 类型，比 IntFraction 有更大的数值范围
 * 当最简分数的分子分母乘积不超过 Long.MAX_VALUE 时不会溢出
 * 
 * 特性：
 * - 线程安全：不可变对象，每次运算生成新对象
 * - 溢出保护：所有运算都检查溢出情况
 * - 高效计算：简化分数时直接返回新对象
 * - 符号规范：分母始终为正，符号由分子决定
 * 
 * @author huitoukest
 */
public class LongFraction extends AbstractFraction implements IFractionOperation<LongFraction>, Comparable<LongFraction> {
    
    /**
     * 分子
     */
    private final long numerator;
    
    /**
     * 分母
     */
    private final long denominator;

    /**
     * 构造函数
     * @param numerator 分子
     * @param denominator 分母，不能为 0
     * @throws ArithmeticException 当分母为 0 时抛出
     */
    public LongFraction(long numerator, long denominator) {
        if (denominator == 0) {
            throw new ArithmeticException("分母不能为 0");
        }
        // 标准化：分母始终为正，符号由分子决定
        if (denominator < 0) {
            this.numerator = -numerator;
            this.denominator = -denominator;
        } else {
            this.numerator = numerator;
            this.denominator = denominator;
        }
    }

    /**
     * 默认构造函数，创建 0/1 分数
     */
    public LongFraction() {
        this.numerator = 0;
        this.denominator = 1;
    }

    /**
     * 从标准的 A/B 格式字符串解析分数
     * @param str 分数字符串，格式为 "分子/分母"
     * @throws IllegalArgumentException 当格式不正确或分母为 0 时抛出
     */
    public LongFraction(String str) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException("分数字符串不能为空");
        }
        
        String[] parts = str.trim().split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("分数字符串格式错误，应为 '分子/分母' 格式");
        }
        
        try {
            long num = Long.parseLong(parts[0].trim());
            long den = Long.parseLong(parts[1].trim());
            
            if (den == 0) {
                throw new ArithmeticException("分母不能为 0");
            }
            
            // 标准化：分母始终为正，符号由分子决定
            if (den < 0) {
                this.numerator = -num;
                this.denominator = -den;
            } else {
                this.numerator = num;
                this.denominator = den;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("分数字符串包含非数字字符: " + str);
        }
    }

    /**
     * 转换为 double 类型
     * @return 小数表示
     */
    @Override
    public double toDouble() {
        return numerator * 1.0 / denominator;
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
        return StringUtils.append(this.numerator, "/", this.denominator);
    }

    /**
     * 判断是否为正数
     * @return true 如果值 > 0
     */
    @Override
    public boolean isPositive() {
        return numerator > 0;
    }

    /**
     * 判断是否为 0
     * @return true 如果值 = 0
     */
    @Override
    public boolean isZero() {
        return numerator == 0;
    }

    /**
     * 判断是否为负数
     * @return true 如果值 < 0
     */
    @Override
    public boolean isNegative() {
        return numerator < 0;
    }

    /**
     * 加法运算
     * @param other 另一个分数
     * @return 相加结果
     * @throws NullPointerException 当 other 为 null 时抛出
     * @throws ArithmeticException 当计算溢出时抛出
     */
    @Override
    public LongFraction add(LongFraction other) {
        if (other == null) {
            throw new NullPointerException("加数不能为 null");
        }
        
        LongFraction a = this.simpleFraction();
        LongFraction b = other.simpleFraction();
        
        // 根据防溢出策略：使用 Math_exact 抛异常
        // 计算最小公倍数
        BigInteger lcm = MathUtils.lcm(BigInteger.valueOf(a.denominator).abs(), BigInteger.valueOf(b.denominator).abs());
        
        // 计算 lcm / denominator
        BigInteger lcmDivA = lcm.divide(BigInteger.valueOf(a.denominator));
        BigInteger lcmDivB = lcm.divide(BigInteger.valueOf(b.denominator));
        
        // 先转换为 BigInteger 计算，避免溢出
        BigInteger tempA = BigInteger.valueOf(a.numerator).multiply(lcmDivA);
        BigInteger tempB = BigInteger.valueOf(b.numerator).multiply(lcmDivB);
        BigInteger newNumerator = tempA.add(tempB);
        
        // 先创建 BigFraction 进行化简，然后再转换回 LongFraction
        BigFraction bigFraction = new BigFraction(newNumerator, lcm).simpleFraction();
        
        // 检查结果是否在 long 范围内
        BigInteger simplifiedNumerator = bigFraction.getNumerator();
        BigInteger simplifiedDenominator = bigFraction.getDenominator();
        
        if (simplifiedNumerator.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 || 
            simplifiedNumerator.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0) {
            throw new ArithmeticException("加法运算结果分子溢出: " + simplifiedNumerator);
        }
        if (simplifiedDenominator.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 || 
            simplifiedDenominator.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0) {
            throw new ArithmeticException("加法运算结果分母溢出: " + simplifiedDenominator);
        }
        
        return new LongFraction(simplifiedNumerator.longValue(), simplifiedDenominator.longValue());
    }

    /**
     * 减法运算
     * @param other 另一个分数
     * @return 相减结果
     * @throws NullPointerException 当 other 为 null 时抛出
     * @throws ArithmeticException 当计算溢出时抛出
     */
    @Override
    public LongFraction sub(LongFraction other) {
        if (other == null) {
            throw new NullPointerException("减数不能为 null");
        }
        
        // 转换为加法：a - b = a + (-b)
        LongFraction negativeOther = new LongFraction(-other.numerator, other.denominator);
        return this.add(negativeOther);
    }

    /**
     * 乘法运算
     * @param other 另一个分数
     * @return 相乘结果
     * @throws NullPointerException 当 other 为 null 时抛出
     * @throws ArithmeticException 当计算溢出时抛出
     */
    @Override
    public LongFraction multiply(LongFraction other) {
        if (other == null) {
            throw new NullPointerException("乘数不能为 null");
        }
        
        if (this.isZero() || other.isZero()) {
            return new LongFraction(0, 1);
        }
        
        LongFraction a = this.simpleFraction();
        LongFraction b = other.simpleFraction();
        
        // 根据防溢出策略：先转换为 BigInteger 计算，避免溢出
        BigInteger newNumerator = BigInteger.valueOf(a.numerator).multiply(BigInteger.valueOf(b.numerator));
        BigInteger newDenominator = BigInteger.valueOf(a.denominator).multiply(BigInteger.valueOf(b.denominator));
        
        // 先创建 BigFraction 进行化简，然后再转换回 LongFraction
        BigFraction bigFraction = new BigFraction(newNumerator, newDenominator).simpleFraction();
        
        // 检查结果是否在 long 范围内
        BigInteger simplifiedNumerator = bigFraction.getNumerator();
        BigInteger simplifiedDenominator = bigFraction.getDenominator();
        
        if (simplifiedNumerator.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 || 
            simplifiedNumerator.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0) {
            throw new ArithmeticException("乘法运算结果分子溢出: " + simplifiedNumerator);
        }
        if (simplifiedDenominator.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 || 
            simplifiedDenominator.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0) {
            throw new ArithmeticException("乘法运算结果分母溢出: " + simplifiedDenominator);
        }
        
        return new LongFraction(simplifiedNumerator.longValue(), simplifiedDenominator.longValue());
    }

    /**
     * 除法运算
     * @param other 另一个分数
     * @return 相除结果
     * @throws NullPointerException 当 other 为 null 时抛出
     * @throws ArithmeticException 当 other 为 0（除零错误）或计算溢出时抛出
     */
    @Override
    public LongFraction div(LongFraction other) {
        if (other == null) {
            throw new NullPointerException("除数不能为 null");
        }
        
        if (other.isZero()) {
            throw new ArithmeticException("除零错误：不能除以 0");
        }
        
        // 转换为乘法：a / b = a * (1/b)
        LongFraction reciprocal = new LongFraction(other.denominator, other.numerator);
        return this.multiply(reciprocal);
    }

    /**
     * 获取最简分数形式
     * @return 最简分数
     */
    @Override
    public LongFraction simpleFraction() {
        if (numerator == 0) {
            return new LongFraction(0, 1);
        }
        
        // 避免 Math.abs(Long.MIN_VALUE) 的陷阱，使用 BigInteger 处理
        BigInteger numeratorBig = BigInteger.valueOf(numerator);
        BigInteger denominatorBig = BigInteger.valueOf(denominator);
        
        BigInteger absNumeratorBig = numeratorBig.abs();
        BigInteger absDenominatorBig = denominatorBig.abs();
        BigInteger gcdBig = absNumeratorBig.gcd(absDenominatorBig);
        
        if (gcdBig.equals(BigInteger.ONE)) {
            return this;
        }
        
        // 符号处理：分母为正，符号由分子决定
        BigInteger newNumeratorBig = numeratorBig.divide(gcdBig);
        BigInteger newDenominatorBig = absDenominatorBig.divide(gcdBig);
        
        // 检查结果是否在 long 范围内
        if (newNumeratorBig.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ||
            newNumeratorBig.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0 ||
            newDenominatorBig.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ||
            newDenominatorBig.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0) {
            throw new ArithmeticException("分数化简结果溢出");
        }
        
        return new LongFraction(newNumeratorBig.longValue(), newDenominatorBig.longValue());
    }

    /**
     * 比较两个分数
     * @param other 另一个分数
     * @return 比较结果
     */
    @Override
    public int compareTo(LongFraction other) {
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
        
        // 根据防溢出策略：使用 BigInteger 比较，避免溢出
        LongFraction a = this.simpleFraction();
        LongFraction b = other.simpleFraction();
        
        BigInteger left = BigInteger.valueOf(a.numerator).multiply(BigInteger.valueOf(b.denominator));
        BigInteger right = BigInteger.valueOf(b.numerator).multiply(BigInteger.valueOf(a.denominator));
        
        return left.compareTo(right);
    }

    /**
     * 判断是否是最简分数
     * @return true 如果已经是最简形式
     */
    @Override
    public boolean isSimpleFraction() {
        if (numerator == 0) {
            return denominator == 1;
        }
        
        // 避免 Math.abs(Long.MIN_VALUE) 的陷阱，使用 BigInteger 处理
        BigInteger numeratorBig = BigInteger.valueOf(numerator);
        BigInteger denominatorBig = BigInteger.valueOf(denominator);
        
        BigInteger absNumeratorBig = numeratorBig.abs();
        BigInteger absDenominatorBig = denominatorBig.abs();
        BigInteger gcdBig = absNumeratorBig.gcd(absDenominatorBig);
        
        return gcdBig.equals(BigInteger.ONE);
    }

    /**
     * 获取分子
     * @return 分子
     */
    @Override
    public Long getNumerator() {
        return numerator;
    }

    /**
     * 获取分母
     * @return 分母
     */
    @Override
    public Number getDenominator() {
        return denominator;
    }

    /**
     * 创建分数的工厂方法
     * @param numerator 分子
     * @param denominator 分母
     * @return 新的分数对象
     */
    @Override
    public LongFraction createFraction(Number numerator, Number denominator) {
        return new LongFraction(numerator.longValue(), denominator.longValue());
    }

    /**
     * 重写 equals 方法，基于简化后的形式比较
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        LongFraction other = (LongFraction) obj;
        return this.compareTo(other) == 0;
    }

    /**
     * 重写 hashCode 方法，基于简化后的形式
     */
    @Override
    public int hashCode() {
        LongFraction simplified = this.simpleFraction();
        return Objects.hash(simplified.numerator, simplified.denominator);
    }

    /**
     * 重写 toString 方法
     */
    @Override
    public String toString() {
        return String.format("LongFraction{%d/%d}", numerator, denominator);
    }
    
    /**
     * 获取当前分数的绝对值
     * @return 绝对值分数
     */
    @Override
    public LongFraction abs() {
        if (isPositive() || isZero()) {
            return this;
        }
        return new LongFraction(-numerator, denominator);
    }
    
    /**
     * 获取当前分数的倒数
     * @return 倒数分数
     * @throws ArithmeticException 当当前分数为 0 时抛出
     */
    @Override
    public LongFraction reciprocal() {
        if (numerator == 0) {
            throw new ArithmeticException("无法计算 0 的倒数");
        }
        return new LongFraction(denominator, numerator).simpleFraction();
    }
    
    /**
     * 转换为带分数形式
     * @return 带分数对象
     */
    @Override
    public MixedNumber toMixedNumber() {
        LongFraction simplified = this.simpleFraction();
        if (simplified.isZero()) {
            return new LongMixedNumber(0, 0, 1);
        }
        
        long wholePart = Math.abs(simplified.numerator) / simplified.denominator;
        long numeratorPart = Math.abs(simplified.numerator) % simplified.denominator;
        
        if (simplified.isNegative()) {
            wholePart = -wholePart;
        }
        
        return new LongMixedNumber(wholePart, numeratorPart, simplified.denominator);
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
        if (!(other instanceof LongFraction)) {
            throw new IllegalArgumentException("只能与 LongFraction 类型比较");
        }
        return compareTo((LongFraction) other);
    }
}