package com.tingfeng.util.java.base.common.bean.fraction;

import com.tingfeng.util.java.base.common.inter.IFractionOperation;
import com.tingfeng.util.java.base.common.utils.MathUtils;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 适用于简单的分数计算
 * 分子分母是int类型 的分数 , 计算值过大时可能溢出, 溢出时结果会错误 (当最简分数的分母分母中的两个最大值相乘不超过Int上限则不会溢出) ;
 * 分数本身的结构也是可以抽象的, 但是为了提高计算效率使用java 原始类型, 所以无法使用泛型 ;
 * 当分母为零时,分子默认为1; 默认均保留原始的分子分母值  , 计算时使用最简分数形式生成新值 ;
 * 最简分数的符号位以分子为准 , 分母均为正.
 * @author huitoukest
 */
public class IntFraction extends AbstractFraction implements IFractionOperation<IntFraction> {
    /**
     * 分子
     */
    private int numerator;
    /**
     * 分母
     */
    private int denominator;


    public IntFraction(int numerator, int denominator){
        assert denominator != 0;
        this.numerator = numerator;
        this.denominator = denominator;
    }


    public IntFraction(){

    }

    /**
     * 从标准的 A/B的格式中解析得到分数
     * @param str
     */
    public IntFraction(String str){
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
    public IntFraction add(IntFraction other) {
        other = other.simpleFraction();
        IntFraction thisFraction= this.simpleFraction();
        int lcm = MathUtils.lcm(thisFraction.getDenominator(), other.getDenominator());
        IntFraction re = new IntFraction(thisFraction.getNumerator() * lcm / thisFraction.getDenominator()
                + other.getNumerator() * lcm / other.getDenominator(), lcm);
        return re.simpleFraction();
    }

    @Override
    public IntFraction sub(IntFraction other) {
        other = other.simpleFraction();
        IntFraction thisFraction= this.simpleFraction();
        int lcm = MathUtils.lcm(thisFraction.getDenominator(), other.getDenominator());
        IntFraction re = new IntFraction(thisFraction.getNumerator() * (lcm / thisFraction.getDenominator())
                - other.getNumerator() * (lcm / other.getDenominator()), lcm);
        return re.simpleFraction();
    }

    @Override
    public IntFraction multiply(IntFraction other) {
        if(other.getNumerator() == 0 || this.getNumerator() == 0){
            return new IntFraction(0 , 1);
        }
        other = other.simpleFraction();
        IntFraction thisFraction= this.simpleFraction();
        IntFraction re = new IntFraction(thisFraction.getNumerator() * other.getNumerator()
                ,thisFraction.getDenominator() * other.getDenominator());
        return re.simpleFraction();
    }

    @Override
    public IntFraction div(IntFraction other) {
        return this.multiply(new IntFraction(other.getDenominator() , other.getNumerator()));
    }


    @Override
    public IntFraction simpleFraction() {
        if(this.numerator == 0){
            return new IntFraction(0 , 1);
        }else {
            int de = Math.abs(this.denominator);
            int nu = Math.abs(this.numerator);
            int gcd = MathUtils.gcd(nu, de);
            if (this.isNegative()) {
                nu = - nu;
            }
            return new IntFraction( nu / gcd, de / gcd);
        }
    }

    public int getNumerator() {
        return numerator;
    }

    public void setNumerator(int numerator) {
        this.numerator = numerator;
    }

    public int getDenominator() {
        return denominator;
    }

    public void setDenominator(int denominator) {
        assert denominator != 0;
        this.denominator = denominator;
    }

    @Override
    public int compareTo(IntFraction o) {
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
                IntFraction other = o.simpleFraction();
                IntFraction thisFraction= this.simpleFraction();
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
    private int compareToTwoPositiveFraction(IntFraction a, IntFraction b , boolean isSimple){
        //正分数 , 同符号下判断
        IntFraction thisFraction = isSimple ? a : a.simpleFraction();
        IntFraction other = isSimple ? b : b.simpleFraction();

        //分子大,分母小的更大
        if(thisFraction.getNumerator() == other.getNumerator()){
            int re = thisFraction.getDenominator() - other.getDenominator();
            return  re == 0 ? 0 : re < 0 ? 1 : -1;
        }else if(thisFraction.getNumerator() > other.getNumerator()){
            int re = thisFraction.getDenominator() - other.getDenominator();
            if(re < 0){
                return 1;
            }
        }
        //分子分母大小无法统一的情况,转为long计算,兼容一些大数字
        return thisFraction.getNumerator()  * 1L * other.getDenominator() - other.getNumerator() * 1L* thisFraction.getDenominator() > 0 ? 1 : -1;
    }

    @Override
    public boolean isSimpleFraction() {
        int de = Math.abs(this.denominator);
        int nu = Math.abs(this.numerator);
        int gcd = MathUtils.gcd(nu, de);
        return gcd == 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntFraction fraction = (IntFraction) o;
        return this.eq(fraction);
    }

    @Override
    public int hashCode() {
        if(numerator == 0)
            return Objects.hash(numerator);
        IntFraction value = this.simpleFraction();
        return Objects.hash(value.getNumerator(), value.getDenominator());
    }

    @Override
    public String toString() {
        return "IntFraction{" +
                "numerator=" + numerator +
                ", denominator=" + denominator +
                '}';
    }
}
