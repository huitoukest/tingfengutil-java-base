package com.tingfeng.util.java.base.common.bean.fraction;

import com.tingfeng.util.java.base.common.inter.IFractionOperation;
import com.tingfeng.util.java.base.common.utils.MathUtils;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 适用于简单的分数计算
 * 分子分母是long类型 的分数 , 计算值过大时可能溢出, 溢出时结果会错误 (当最简分数的分母分母中的两个最大值相乘不超过Int上限则不会溢出) ;
 * 分数本身的结构也是可以抽象的, 但是为了提高计算效率使用java 原始类型, 所以无法使用泛型 ;
 * 当分母为零时,分子默认为1; 默认均保留原始的分子分母值  , 计算时使用最简分数形式生成新值 ;
 * 最简分数的符号位以分子为准 , 分母均为正.
 */
public class LongFraction extends AbstractFraction implements IFractionOperation<LongFraction> {

    /**
     * 分子
     */
    private long numerator;
    /**
     * 分母
     */
    private long denominator;


    public LongFraction(long numerator, long denominator){
        assert denominator != 0;
        this.numerator = numerator;
        this.denominator = denominator;
    }


    public LongFraction(){

    }

    /**
     * 从标准的 A/B的格式中解析得到分数
     * @param str
     */
    public LongFraction(String str){
        String[] array = str.split("/");
        this.numerator = Integer.parseInt(array[0]);
        this.denominator = Integer.parseInt(array[1]);
        assert numerator != this.denominator;
    }

    @Override
    public double toDouble() {
        return  numerator * 1.0 / denominator;
    }

    @Override
    BigDecimal toBigDecimal(int newScale, RoundingMode roundingMode) {
        BigDecimal bigDecimal = new BigDecimal(this.numerator);
        return bigDecimal.divide(new BigDecimal(this.denominator), newScale, roundingMode);
    }

    @Override
    String toString(int newScale) {
        return toBigDecimal(newScale, RoundingMode.HALF_UP).toString();
    }

    @Override
    String getValue() {
        return StringUtils.append(this.numerator, "/", this.denominator);
    }

    @Override
    boolean isPositive() {
        return ( this.getNumerator() > 0 && this.getDenominator() > 0 ) ||
                ( this.getNumerator() < 0 && this.getDenominator() < 0 ) ;
    }

    @Override
    boolean isZero() {
        return this.getNumerator() == 0;
    }

    @Override
    boolean isNegative() {
        return ( this.getNumerator() > 0 && this.getDenominator() < 0 ) ||
                ( this.getNumerator() < 0 && this.getDenominator() > 0 ) ;
    }

    @Override
    public LongFraction add(LongFraction other) {
        other = other.simpleFraction();
        LongFraction thisFraction= this.simpleFraction();
        long lcm = MathUtils.lcm(thisFraction.getDenominator(), other.getDenominator());
        LongFraction re = new LongFraction(thisFraction.getNumerator() * lcm / thisFraction.getDenominator()
                + other.getNumerator() * lcm / other.getDenominator(), lcm);
        return re.simpleFraction();
    }

    @Override
    public LongFraction sub(LongFraction other) {
        other = other.simpleFraction();
        LongFraction thisFraction= this.simpleFraction();
        long lcm = MathUtils.lcm(thisFraction.getDenominator(), other.getDenominator());
        LongFraction re = new LongFraction(thisFraction.getNumerator() * (lcm / thisFraction.getDenominator())
                - other.getNumerator() * (lcm / other.getDenominator()), lcm);
        return re.simpleFraction();
    }

    @Override
    public LongFraction multiply(LongFraction other) {
        if(other.getNumerator() == 0 || this.getNumerator() == 0){
            return new LongFraction(0 , 1);
        }
        other = other.simpleFraction();
        LongFraction thisFraction= this.simpleFraction();
        LongFraction re = new LongFraction(thisFraction.getNumerator() * other.getNumerator()
                ,thisFraction.getDenominator() * other.getDenominator());
        return re.simpleFraction();
    }

    @Override
    public LongFraction div(LongFraction other) {
        return this.multiply(new LongFraction(other.getDenominator() , other.getNumerator()));
    }


    @Override
    public LongFraction simpleFraction() {
        if(this.numerator == 0){
            return new LongFraction(0 , 1);
        }else {
            long de = Math.abs(this.denominator);
            long nu = Math.abs(this.numerator);
            long gcd = MathUtils.gcd(nu, de);
            if (this.isNegative()) {
                nu = - nu;
            }
            return new LongFraction( nu / gcd, de / gcd);
        }
    }

    public long getNumerator() {
        return numerator;
    }

    public void setNumerator(long numerator) {
        this.numerator = numerator;
    }

    public long getDenominator() {
        return denominator;
    }

    public void setDenominator(long denominator) {
        assert denominator != 0;
        this.denominator = denominator;
    }

    @Override
    public int compareTo(LongFraction o) {
        //判断正负号
        if(this.isZero()){
            if(o.isZero()) {
                return 0;
            }else if(o.isNegative()){
                return 1;
            }else{
                return -1;
            }
        }else if(this.isPositive()){
            if(o.isZero() || o.isNegative()){
                return 1;
            }else {
                //正分数 , 同符号下判断
                return compareToTwoPositiveFraction(this, o, false);
            }
        }else if(this.isNegative()){
            if(o.isZero() || o.isPositive()){
                return  -1;
            }else {
                //负分数, 同符号下判断 ;
                LongFraction other = o.simpleFraction();
                LongFraction thisFraction= this.simpleFraction();
                other.setNumerator( - other.getNumerator() );
                thisFraction.setNumerator( - thisFraction.getNumerator() );
                return - compareToTwoPositiveFraction(thisFraction, other, true);
            }
        }
        return 0;
    }

    /**
     * 比较两个分数
     * @param a
     * @param b
     * @return
     */
    private int compareToTwoPositiveFraction(LongFraction a, LongFraction b , boolean isSimple){
        //正分数 , 同符号下判断
        LongFraction thisFraction = isSimple ? a : a.simpleFraction();
        LongFraction other = isSimple ? b : b.simpleFraction();

        //分子大,分母小的更大
        if(thisFraction.getNumerator() == other.getNumerator()){
            long re = thisFraction.getDenominator() - other.getDenominator();
            return  re == 0 ? 0 : re < 0 ? 1 : -1;
        }else if(thisFraction.getNumerator() > other.getNumerator()){
            long re = thisFraction.getDenominator() - other.getDenominator();
            if(re < 0){
                return 1;
            }
        }
        //分子分母大小无法统一的情况,转为BigInt计算,兼容一些大数字
        BigInteger valueA = BigInteger.valueOf(thisFraction.getNumerator()).multiply(BigInteger.valueOf(other.getDenominator()));
        BigInteger valueB = BigInteger.valueOf(other.getNumerator()).multiply(BigInteger.valueOf(thisFraction.getDenominator()));
        return   valueA.compareTo(valueB);
    }

    @Override
    public boolean isSimpleFraction() {
        long de = Math.abs(this.denominator);
        long nu = Math.abs(this.numerator);
        long gcd = MathUtils.gcd(nu, de);
        return gcd == 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LongFraction fraction = (LongFraction) o;
        return this.eq(fraction);
    }

    @Override
    public int hashCode() {
        if(numerator == 0)
            return Objects.hash(numerator);
        LongFraction value = this.simpleFraction();
        return Objects.hash(value.getNumerator(), value.getDenominator());
    }

    @Override
    public String toString() {
        return "LongFraction{" +
                "numerator=" + numerator +
                ", denominator=" + denominator +
                '}';
    }
}
