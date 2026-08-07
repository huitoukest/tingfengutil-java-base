package com.tingfeng.util.java.base.math.base;

import com.tingfeng.util.java.base.lang.inter.IFractionOperation;
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
        // 分母为 MIN_VALUE 时标准化取反会溢出，无法保证分母恒正的不变量
        if (denominator == Long.MIN_VALUE) {
            // 分子为 0 时数学上等于 0/1，可安全表示
            if (numerator == 0) {
                this.numerator = 0;
                this.denominator = 1;
            } else {
                throw new ArithmeticException("分母为 Long.MIN_VALUE 时无法标准化（取反后超出 long 范围）");
            }
            return;
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
            
            // 分母为 MIN_VALUE 时标准化取反会溢出，无法保证分母恒正的不变量
            if (den == Long.MIN_VALUE) {
                // 分子为 0 时数学上等于 0/1，可安全表示
                if (num == 0) {
                    this.numerator = 0;
                    this.denominator = 1;
                } else {
                    throw new ArithmeticException("分母为 Long.MIN_VALUE 时无法标准化（取反后超出 long 范围）");
                }
                return;
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
    public String toString(int newScale) {
        return toBigDecimal(newScale, RoundingMode.HALF_UP).toString();
    }

    /**
     * 获取标准分数字符串
     * @return 如 "3/4"
     */
    @Override
    public String getValue() {
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
        
        // 委托 BigFraction 减法，BigInteger negate 无溢出（other 分子为 Long.MIN_VALUE 时安全）
        BigFraction result = toBigFraction().sub(other.toBigFraction());
        return toLongFractionChecked(result, "减法运算结果分子溢出", "减法运算结果分母溢出");
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
        
        // 委托 BigFraction 乘法，BigInteger 中间量天然避免中间溢出
        BigFraction result = toBigFraction().multiply(other.toBigFraction());
        return toLongFractionChecked(result, "乘法运算结果分子溢出", "乘法运算结果分母溢出");
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
        
        // 委托 BigFraction 除法，倒数构造与除零检查由 BigFraction 完成并透传
        BigFraction result = toBigFraction().div(other.toBigFraction());
        return toLongFractionChecked(result, "乘法运算结果分子溢出", "乘法运算结果分母溢出");
    }

    /**
     * 加法运算（long 重载）
     * @param value 加数
     * @return 相加结果
     * @throws ArithmeticException 当计算溢出时抛出
     */
    public LongFraction add(long value) {
        return add(new LongFraction(value, 1));
    }

    /**
     * 减法运算（long 重载）
     * @param value 减数
     * @return 相减结果
     * @throws ArithmeticException 当计算溢出时抛出
     */
    public LongFraction sub(long value) {
        return sub(new LongFraction(value, 1));
    }

    /**
     * 乘法运算（long 重载）
     * @param value 乘数
     * @return 相乘结果
     * @throws ArithmeticException 当计算溢出时抛出
     */
    public LongFraction multiply(long value) {
        return multiply(new LongFraction(value, 1));
    }

    /**
     * 除法运算（long 重载）
     * @param value 除数
     * @return 相除结果
     * @throws ArithmeticException 当 value 为 0（除零错误）或计算溢出时抛出
     */
    public LongFraction div(long value) {
        return div(new LongFraction(value, 1));
    }

    /**
     * 加法运算（double 重载）
     *
     * double 值经 {@link #fromDouble(double)} 精确展开为等价分数后参与运算
     *
     * @param value 加数
     * @return 相加结果
     * @throws IllegalArgumentException 当 value 无法用 long 范围分数表示时抛出
     * @throws ArithmeticException 当计算溢出时抛出
     */
    public LongFraction add(double value) {
        return add(fromDouble(value));
    }

    /**
     * 减法运算（double 重载）
     *
     * double 值经 {@link #fromDouble(double)} 精确展开为等价分数后参与运算
     *
     * @param value 减数
     * @return 相减结果
     * @throws IllegalArgumentException 当 value 无法用 long 范围分数表示时抛出
     * @throws ArithmeticException 当计算溢出时抛出
     */
    public LongFraction sub(double value) {
        return sub(fromDouble(value));
    }

    /**
     * 乘法运算（double 重载）
     *
     * double 值经 {@link #fromDouble(double)} 精确展开为等价分数后参与运算
     *
     * @param value 乘数
     * @return 相乘结果
     * @throws IllegalArgumentException 当 value 无法用 long 范围分数表示时抛出
     * @throws ArithmeticException 当计算溢出时抛出
     */
    public LongFraction multiply(double value) {
        return multiply(fromDouble(value));
    }

    /**
     * 除法运算（double 重载）
     *
     * double 值经 {@link #fromDouble(double)} 精确展开为等价分数后参与运算
     *
     * @param value 除数
     * @return 相除结果
     * @throws IllegalArgumentException 当 value 无法用 long 范围分数表示时抛出
     * @throws ArithmeticException 当 value 为 0（除零错误）或计算溢出时抛出
     */
    public LongFraction div(double value) {
        return div(fromDouble(value));
    }

    /**
     * 将 double 值精确展开为等价的 LongFraction
     *
     * 展开规则（按 {@link BigDecimal#valueOf(double)} 的 scale 符号双向展开）：
     * - scale >= 0: 分子 = unscaledValue、分母 = 10^scale（如 0.5 → 5/10）
     * - scale < 0: 分子 = unscaledValue × 10^(-scale)、分母 = 1（如 1e18 → 10^18/1）
     *
     * 双向超限校验：分母（scale >= 0 分支）或分子（scale < 0 分支）超出 long 范围时
     * 统一抛 IllegalArgumentException。严禁将未校验的 BigInteger.longValue() 结果传入构造器，
     * 构造器仅校验分母为 0，不查分子溢出，超出 long 范围的值会静默截断
     *
     * @param value double 值
     * @return 等价分数
     * @throws IllegalArgumentException 当 value 为 NaN/Infinity 或展开后分子/分母超出 long 范围时抛出
     */
    private static LongFraction fromDouble(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("不能将 NaN/Infinity 展开为分数: " + value);
        }
        BigDecimal decimal = BigDecimal.valueOf(value);
        BigInteger unscaledValue = decimal.unscaledValue();
        int scale = decimal.scale();
        if (scale >= 0) {
            // 分母 = 10^scale；unscaledValue 来自 Double.toString 系数（不超过 17 位有效数字），必在 long 范围内
            BigInteger denominator = BigInteger.TEN.pow(scale);
            if (denominator.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                throw new IllegalArgumentException("展开分母超出 long 范围: 10^" + scale);
            }
            return new LongFraction(unscaledValue.longValue(), denominator.longValue());
        }
        // scale < 0: 分子 = unscaledValue × 10^(-scale)
        BigInteger numerator = unscaledValue.multiply(BigInteger.TEN.pow(-scale));
        if (numerator.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
                || numerator.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0) {
            throw new IllegalArgumentException("展开分子超出 long 范围: " + numerator);
        }
        return new LongFraction(numerator.longValue(), 1L);
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
        
        // 委托 BigFraction 化简，BigInteger 计算避免 Math.abs(Long.MIN_VALUE) 的陷阱
        BigFraction simplified = toBigFraction().simpleFraction();
        
        // 防御性检查结果是否在 long 范围内（输入为 long，化简结果必然不超限）
        BigInteger newNumeratorBig = simplified.getNumerator();
        BigInteger newDenominatorBig = simplified.getDenominator();
        
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
        
        // 委托 BigFraction 比较，BigInteger 交叉相乘语义等价且避免溢出
        return toBigFraction().compareTo(other.toBigFraction());
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
        // instanceof 语义：涵盖 Fraction 等 LongFraction 子类，保证与 hashCode 的对称性
        if (!(obj instanceof LongFraction)) return false;
        
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
        // 分子为 MIN_VALUE 时绝对值超出 long 表示范围，拒绝静默返回负数
        if (numerator == Long.MIN_VALUE) {
            throw new ArithmeticException("分子为 Long.MIN_VALUE，绝对值无法用 long 表示");
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
        
        // 使用 BigInteger 取绝对值，避免 Math.abs(Long.MIN_VALUE) 溢出
        BigInteger absNumerator = BigInteger.valueOf(simplified.numerator).abs();
        BigInteger denominatorBig = BigInteger.valueOf(simplified.denominator);
        BigInteger[] divisionResult = absNumerator.divideAndRemainder(denominatorBig);
        
        // 整数部分可能超出 long 表示范围（如 MIN_VALUE/1 的绝对值为 2^63），显式拒绝
        if (divisionResult[0].compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            throw new ArithmeticException("带分数整数部分溢出: " + divisionResult[0]);
        }
        long wholePart = divisionResult[0].longValue();
        long numeratorPart = divisionResult[1].longValue();
        
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
    
    /**
     * 转换为同值的 BigFraction
     * @return BigFraction 表示
     */
    private BigFraction toBigFraction() {
        return new BigFraction(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }
    
    /**
     * 将 BigFraction 运算结果检查后转换为 LongFraction
     * @param fraction 运算结果
     * @param numeratorMessage 分子溢出异常消息
     * @param denominatorMessage 分母溢出异常消息
     * @return long 范围内的结果
     * @throws ArithmeticException 当结果超出 long 范围时抛出
     */
    private LongFraction toLongFractionChecked(BigFraction fraction, String numeratorMessage, String denominatorMessage) {
        BigInteger simplifiedNumerator = fraction.getNumerator();
        BigInteger simplifiedDenominator = fraction.getDenominator();
        
        if (simplifiedNumerator.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ||
            simplifiedNumerator.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0) {
            throw new ArithmeticException(numeratorMessage + ": " + simplifiedNumerator);
        }
        if (simplifiedDenominator.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ||
            simplifiedDenominator.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0) {
            throw new ArithmeticException(denominatorMessage + ": " + simplifiedDenominator);
        }
        return new LongFraction(simplifiedNumerator.longValue(), simplifiedDenominator.longValue());
    }
}