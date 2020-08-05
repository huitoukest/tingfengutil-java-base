package com.tingfeng.util.java.base.common.bean.fraction;

import com.tingfeng.util.java.base.common.inter.IFractionOperation;
import com.tingfeng.util.java.base.common.utils.MathUtils;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 适用于大数字的分数计算
 * 分子分母是BigInteger 类型 的分数
 * 当分母为零时,分子默认为1; 默认均保留原始的分子分母值  , 计算时使用最简分数形式生成新值 ;
 * 最简分数的符号位以分子为准 , 分母均为正.
 */
public class BigFraction extends AbstractFraction implements IFractionOperation<BigFraction> {

    /**
     * 分子
     */
    private BigInteger numerator;
    /**
     * 分母
     */
    private BigInteger denominator;



    public BigFraction(BigInteger numerator, BigInteger denominator){
        if(BigInteger.ZERO.equals(denominator)){
            throw new ArithmeticException("denominator can not be zero!");
        }
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public BigFraction(long numerator, long denominator){
        if(denominator == 0){
            throw new ArithmeticException("denominator can not be zero!");
        }
        this.numerator = BigInteger.valueOf(numerator);
        this.denominator = BigInteger.valueOf(denominator);
    }

    public BigFraction(){

    }

    /**
     * 从标准的 A/B的格式中解析得到分数
     * @param str
     */
    public BigFraction(String str){
        String[] array = str.split("/");
        this.denominator = new BigInteger(array[1]);
        if(BigInteger.ZERO.equals(denominator)){
            throw new ArithmeticException("denominator can not be zero!");
        }
        this.numerator = new BigInteger(array[0]);
    }

    /**
     * 默认保留15位小数 , 四舍五入原则
     * @return
     */
    @Override
    public double toDouble() {
        return  toBigDecimal(15,RoundingMode.HALF_UP).doubleValue();
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
        return StringUtils.append(this.numerator.toString(), "/", this.denominator.toString());
    }

    @Override
    boolean isPositive() {
        int reN = this.getNumerator().compareTo(BigInteger.ZERO);
        int reD = this.getDenominator().compareTo(BigInteger.ZERO);
        return ( reN > 0 && reD > 0 ) ||
                ( reN < 0 && reD < 0 ) ;
    }

    @Override
    boolean isZero() {
        return BigInteger.ZERO.equals(this.getNumerator());
    }

    @Override
    boolean isNegative() {
        int reN = this.getNumerator().compareTo(BigInteger.ZERO);
        int reD = this.getDenominator().compareTo(BigInteger.ZERO);
        return ( reN > 0 && reD < 0 ) ||
                ( reN < 0 && reD > 0 ) ;
    }

    @Override
    public BigFraction add(BigFraction other) {
        other = other.simpleFraction();
        BigFraction thisFraction= this.simpleFraction();
        BigInteger lcm = MathUtils.lcm(thisFraction.getNumerator(), other.getDenominator());
        BigFraction re = new BigFraction(thisFraction.getNumerator().multiply(lcm).divide(thisFraction.getDenominator())
                .add(other.getNumerator().multiply(lcm).divide(other.getDenominator())), lcm);
        return re.simpleFraction();
    }

    @Override
    public BigFraction sub(BigFraction other) {
        other = other.simpleFraction();
        BigFraction thisFraction= this.simpleFraction();
        BigInteger lcm = MathUtils.lcm(thisFraction.getDenominator(), other.getDenominator());
        BigFraction re = new BigFraction(thisFraction.getNumerator().multiply(lcm).divide(thisFraction.getDenominator())
                .subtract(other.getNumerator().multiply(lcm).divide(other.getDenominator())), lcm);
        return re.simpleFraction();
    }

    @Override
    public BigFraction multiply(BigFraction other) {
        if(other.isZero() || this.isZero()){
            return new BigFraction(BigInteger.ZERO , BigInteger.ONE);
        }
        other = other.simpleFraction();
        BigFraction thisFraction= this.simpleFraction();
        BigFraction re = new BigFraction(thisFraction.getNumerator().multiply(other.getNumerator())
                ,thisFraction.getDenominator().multiply(other.getDenominator()));
        return re.simpleFraction();
    }

    @Override
    public BigFraction div(BigFraction other) {
        return this.multiply(new BigFraction(other.getDenominator() , other.getNumerator()));
    }


    @Override
    public BigFraction simpleFraction() {
        if(this.isZero()){
            return new BigFraction(BigInteger.ZERO , BigInteger.ONE);
        }else {
            BigInteger de = this.denominator.abs();
            BigInteger nu = this.numerator.abs();
            BigInteger gcd = MathUtils.gcd(nu, de);
            if (this.isNegative()) {
                nu = nu.negate();
            }
            return new BigFraction( nu.divide(gcd), de.divide(gcd));
        }
    }

    public BigInteger getNumerator() {
        return numerator;
    }

    public void setNumerator(BigInteger numerator) {
        this.numerator = numerator;
    }

    public BigInteger getDenominator() {
        return denominator;
    }

    public void setDenominator(BigInteger denominator) {
        if(BigInteger.ZERO.equals(denominator)){
            throw new ArithmeticException("denominator can not be zero!");
        }
        this.denominator = denominator;
    }

    @Override
    public int compareTo(BigFraction o) {
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
                BigFraction other = o.simpleFraction();
                BigFraction thisFraction= this.simpleFraction();
                other.setNumerator( other.getNumerator().negate() );
                thisFraction.setNumerator( thisFraction.getNumerator().negate() );
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
    private int compareToTwoPositiveFraction(BigFraction a, BigFraction b , boolean isSimple){
        //正分数 , 同符号下判断
        BigFraction thisFraction = isSimple ? a : a.simpleFraction();
        BigFraction other = isSimple ? b : b.simpleFraction();

        //分子大,分母小的更大
        if(thisFraction.getNumerator() == other.getNumerator()){
            int re = thisFraction.getDenominator().subtract(other.getDenominator()).compareTo(BigInteger.ZERO);
            return  re == 0 ? 0 : re < 0 ? 1 : -1;
        }
        if(thisFraction.getNumerator().compareTo(other.getNumerator()) > 0 &&
                thisFraction.getDenominator().compareTo(other.getDenominator()) < 0){
                return 1;
        }
        //分子分母大小无法统一的情况,转为BigInt计算,兼容一些大数字
        BigInteger valueA =thisFraction.getNumerator().multiply(other.getDenominator());
        BigInteger valueB = other.getNumerator().multiply(thisFraction.getDenominator());
        return   valueA.compareTo(valueB);
    }

    @Override
    public boolean isSimpleFraction() {
        BigInteger de = this.denominator.abs();
        BigInteger nu = this.numerator.abs();
        BigInteger gcd = MathUtils.gcd(nu, de);
        return gcd.equals(BigInteger.ONE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BigFraction fraction = (BigFraction) o;
        return this.eq(fraction);
    }

    @Override
    public int hashCode() {
        if(this.isZero())
            return Objects.hash(BigInteger.ZERO);
        BigFraction value = this.simpleFraction();
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
