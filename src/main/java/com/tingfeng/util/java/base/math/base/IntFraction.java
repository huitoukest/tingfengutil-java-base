package com.tingfeng.util.java.base.math.base;

import com.tingfeng.util.java.base.lang.inter.IFractionOperation;
import com.tingfeng.util.java.base.math.MathUtils;
import com.tingfeng.util.java.base.lang.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 整数分数类，适用于一般的分数计算
 * 分子分母使用 int 类型，计算效率高但需要注意溢出风险
 * 当最简分数的分子分母乘积不超过 Integer.MAX_VALUE 时不会溢出
 * 
 * 特性：
 * - 线程安全：不可变对象，每次运算生成新对象
 * - 溢出保护：所有运算都检查溢出情况
 * - 高效计算：简化分数时直接返回新对象
 * - 符号规范：分母始终为正，符号由分子决定
 * 
 * @author huitoukest
 */
public class IntFraction extends AbstractFraction implements IFractionOperation<IntFraction>, Comparable<IntFraction> {
    
    /**
     * 分子
     */
    private final int numerator;
    
    /**
     * 分母
     */
    private final int denominator;

    /**
     * 构造函数
     * @param numerator 分子
     * @param denominator 分母，不能为 0
     * @throws ArithmeticException 当分母为 0 时抛出
     */
    public IntFraction(int numerator, int denominator) {
        if (denominator == 0) {
            throw new ArithmeticException("分母不能为 0");
        }
        // 分母为 MIN_VALUE 时标准化取反会溢出，无法保证分母恒正的不变量
        if (denominator == Integer.MIN_VALUE) {
            // 分子为 0 时数学上等于 0/1，可安全表示
            if (numerator == 0) {
                this.numerator = 0;
                this.denominator = 1;
            } else {
                throw new ArithmeticException("分母为 Integer.MIN_VALUE 时无法标准化（取反后超出 int 范围）");
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
     * 从标准的 A/B 格式字符串解析分数
     * @param str 分数字符串，格式为 "分子/分母"
     * @throws IllegalArgumentException 当格式不正确或分母为 0 时抛出
     */
    public IntFraction(String str) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException("分数字符串不能为空");
        }
        
        String[] parts = str.trim().split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("分数字符串格式错误，应为 '分子/分母' 格式");
        }
        
        try {
            int num = Integer.parseInt(parts[0].trim());
            int den = Integer.parseInt(parts[1].trim());
            
            if (den == 0) {
                throw new ArithmeticException("分母不能为 0");
            }
            
            // 分母为 MIN_VALUE 时标准化取反会溢出，无法保证分母恒正的不变量
            if (den == Integer.MIN_VALUE) {
                // 分子为 0 时数学上等于 0/1，可安全表示
                if (num == 0) {
                    this.numerator = 0;
                    this.denominator = 1;
                } else {
                    throw new ArithmeticException("分母为 Integer.MIN_VALUE 时无法标准化（取反后超出 int 范围）");
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
    public IntFraction add(IntFraction other) {
        if (other == null) {
            throw new NullPointerException("加数不能为 null");
        }
        
        IntFraction a = this.simpleFraction();
        IntFraction b = other.simpleFraction();
        
        // 根据防溢出策略：强制升级为 long 计算，天然避免溢出
        // 计算最小公倍数
        long lcm = MathUtils.lcm(Math.abs((long) a.denominator), Math.abs((long) b.denominator));
        
        // 计算 lcm / denominator
        long lcmDivA = lcm / a.denominator;
        long lcmDivB = lcm / b.denominator;
        
        // 使用 long 计算新分子，避免溢出
        long newNumerator = (long) a.numerator * lcmDivA + (long) b.numerator * lcmDivB;
        
        // 先创建 LongFraction 进行化简，然后再转换回 IntFraction
        LongFraction longFraction = new LongFraction(newNumerator, lcm).simpleFraction();
        
        // 检查结果是否在 int 范围内
        long simplifiedNumerator = longFraction.getNumerator().longValue();
        long simplifiedDenominator = longFraction.getDenominator().longValue();
        
        if (simplifiedNumerator > Integer.MAX_VALUE || simplifiedNumerator < Integer.MIN_VALUE) {
            throw new ArithmeticException("加法运算结果分子溢出: " + simplifiedNumerator);
        }
        if (simplifiedDenominator > Integer.MAX_VALUE || simplifiedDenominator < Integer.MIN_VALUE) {
            throw new ArithmeticException("加法运算结果分母溢出: " + simplifiedDenominator);
        }
        
        return new IntFraction((int) simplifiedNumerator, (int) simplifiedDenominator);
    }

    /**
     * 减法运算
     * @param other 另一个分数
     * @return 相减结果
     * @throws NullPointerException 当 other 为 null 时抛出
     * @throws ArithmeticException 当计算溢出时抛出
     */
    @Override
    public IntFraction sub(IntFraction other) {
        if (other == null) {
            throw new NullPointerException("减数不能为 null");
        }
        
        // 委托 LongFraction 减法，保持旧语义（SS4 减法溢出修复自动传导）
        LongFraction result = toLongFraction().sub(other.toLongFraction());
        return toIntFraction(result, "加法运算结果分子溢出", "加法运算结果分母溢出");
    }

    /**
     * 乘法运算
     * @param other 另一个分数
     * @return 相乘结果
     * @throws NullPointerException 当 other 为 null 时抛出
     * @throws ArithmeticException 当计算溢出时抛出
     */
    @Override
    public IntFraction multiply(IntFraction other) {
        if (other == null) {
            throw new NullPointerException("乘数不能为 null");
        }
        
        // 委托 LongFraction 乘法，BigInteger 中间量天然避免中间溢出
        LongFraction result = toLongFraction().multiply(other.toLongFraction());
        return toIntFraction(result, "乘法运算结果分子溢出", "乘法运算结果分母溢出");
    }

    /**
     * 除法运算
     * @param other 另一个分数
     * @return 相除结果
     * @throws NullPointerException 当 other 为 null 时抛出
     * @throws ArithmeticException 当 other 为 0（除零错误）或计算溢出时抛出
     */
    @Override
    public IntFraction div(IntFraction other) {
        if (other == null) {
            throw new NullPointerException("除数不能为 null");
        }
        
        // 委托 LongFraction 除法，倒数构造与除零检查由 LongFraction 完成并透传
        LongFraction result = toLongFraction().div(other.toLongFraction());
        return toIntFraction(result, "乘法运算结果分子溢出", "乘法运算结果分母溢出");
    }

    /**
     * 获取最简分数形式
     * @return 最简分数
     */
    @Override
    public IntFraction simpleFraction() {
        if (numerator == 0) {
            return new IntFraction(0, 1);
        }
        
        // 委托 LongFraction 化简，BigInteger 计算避免 Math.abs(INT_MIN) 的陷阱
        LongFraction simplified = toLongFraction().simpleFraction();
        
        // 检查结果是否在 int 范围内
        long newNumeratorLong = simplified.getNumerator().longValue();
        long newDenominatorLong = simplified.getDenominator().longValue();
        
        if (newNumeratorLong > Integer.MAX_VALUE || newNumeratorLong < Integer.MIN_VALUE ||
            newDenominatorLong > Integer.MAX_VALUE || newDenominatorLong < 1) {
            throw new ArithmeticException("分数化简结果溢出");
        }
        
        return new IntFraction((int) newNumeratorLong, (int) newDenominatorLong);
    }

    /**
     * 比较两个分数
     * @param other 另一个分数
     * @return 比较结果
     */
    @Override
    public int compareTo(IntFraction other) {
        if (other == null) {
            throw new NullPointerException("比较对象不能为 null");
        }
        
        // 快速路径：如果引用相同
        if (this == other) {
            return 0;
        }
        
        // 委托 LongFraction 比较，BigInteger 交叉相乘语义等价且避免溢出
        return toLongFraction().compareTo(other.toLongFraction());
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
        
        // 避免 Math.abs(INT_MIN) 的陷阱，使用 long 类型处理
        long absNumeratorLong = Math.abs((long) numerator);
        long absDenominatorLong = Math.abs((long) denominator);
        long gcdLong = MathUtils.gcd(absNumeratorLong, absDenominatorLong);
        
        return gcdLong == 1;
    }

    /**
     * 获取分子
     * @return 分子
     */
    @Override
    public Integer getNumerator() {
        return numerator;
    }

    /**
     * 获取分母
     * @return 分母
     */
    @Override
    public Integer getDenominator() {
        return denominator;
    }

    /**
     * 创建分数的工厂方法
     * @param numerator 分子
     * @param denominator 分母
     * @return 新的分数对象
     */
    @Override
    public IntFraction createFraction(Number numerator, Number denominator) {
        return new IntFraction(numerator.intValue(), denominator.intValue());
    }

    /**
     * 重写 equals 方法，基于简化后的形式比较
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        IntFraction other = (IntFraction) obj;
        return this.compareTo(other) == 0;
    }

    /**
     * 重写 hashCode 方法，基于简化后的形式
     */
    @Override
    public int hashCode() {
        IntFraction simplified = this.simpleFraction();
        return Objects.hash(simplified.numerator, simplified.denominator);
    }

    /**
     * 重写 toString 方法
     */
    @Override
    public String toString() {
        return String.format("IntFraction{%d/%d}", numerator, denominator);
    }
    
    /**
     * 获取当前分数的绝对值
     * @return 绝对值分数
     */
    @Override
    public IntFraction abs() {
        if (isPositive() || isZero()) {
            return this;
        }
        // 分子为 MIN_VALUE 时绝对值超出 int 表示范围，拒绝静默返回负数
        if (numerator == Integer.MIN_VALUE) {
            throw new ArithmeticException("分子为 Integer.MIN_VALUE，绝对值无法用 int 表示");
        }
        return new IntFraction(-numerator, denominator);
    }
    
    /**
     * 获取当前分数的倒数
     * @return 倒数分数
     * @throws ArithmeticException 当当前分数为 0 时抛出
     */
    @Override
    public IntFraction reciprocal() {
        if (numerator == 0) {
            throw new ArithmeticException("无法计算 0 的倒数");
        }
        return new IntFraction(denominator, numerator).simpleFraction();
    }
    
    /**
     * 转换为带分数形式
     * @return 带分数对象
     */
    @Override
    public MixedNumber toMixedNumber() {
        IntFraction simplified = this.simpleFraction();
        if (simplified.isZero()) {
            return new IntMixedNumber(0, 0, 1);
        }
        
        int wholePart = (int) (Math.abs(((long) simplified.numerator)) / simplified.denominator);
        int numeratorPart = (int) (Math.abs(((long) simplified.numerator)) % simplified.denominator);
        
        if (simplified.isNegative()) {
            wholePart = -wholePart;
        }
        
        return new IntMixedNumber(wholePart, numeratorPart, simplified.denominator);
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
        if (!(other instanceof IntFraction)) {
            throw new IllegalArgumentException("只能与 IntFraction 类型比较");
        }
        return compareTo((IntFraction) other);
    }
    
    /**
     * 转换为同值的 LongFraction
     * @return LongFraction 表示
     */
    private LongFraction toLongFraction() {
        return new LongFraction(numerator, denominator);
    }
    
    /**
     * 将 LongFraction 运算结果检查后转换为 IntFraction
     * @param fraction 运算结果
     * @param numeratorMessage 分子溢出异常消息
     * @param denominatorMessage 分母溢出异常消息
     * @return int 范围内的结果
     * @throws ArithmeticException 当结果超出 int 范围时抛出
     */
    private IntFraction toIntFraction(LongFraction fraction, String numeratorMessage, String denominatorMessage) {
        long simplifiedNumerator = fraction.getNumerator().longValue();
        long simplifiedDenominator = fraction.getDenominator().longValue();
        
        if (simplifiedNumerator > Integer.MAX_VALUE || simplifiedNumerator < Integer.MIN_VALUE) {
            throw new ArithmeticException(numeratorMessage + ": " + simplifiedNumerator);
        }
        if (simplifiedDenominator > Integer.MAX_VALUE || simplifiedDenominator < Integer.MIN_VALUE) {
            throw new ArithmeticException(denominatorMessage + ": " + simplifiedDenominator);
        }
        return new IntFraction((int) simplifiedNumerator, (int) simplifiedDenominator);
    }
}