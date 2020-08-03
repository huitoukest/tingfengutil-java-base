package com.tingfeng.util.java.base.common.bean.fraction;

import com.tingfeng.util.java.base.common.utils.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.RoundingMode;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class LongFractionTest {

    @Test
    public void toDouble() {
        LongFraction fraction = new LongFraction(RandomUtils.randomInt() , RandomUtils.randomInt());
        System.out.println(fraction.toDouble());
    }

    @Test
    public void toBigDecimal() {
        LongFraction fraction = new LongFraction(RandomUtils.randomInt() , RandomUtils.randomInt());
        System.out.println(fraction.toBigDecimal(RandomUtils.randomInt(0,10), RoundingMode.HALF_UP));
    }

    @Test
    public void testToString() {
        LongFraction fraction = new LongFraction(RandomUtils.randomInt() , RandomUtils.randomInt());
        System.out.println(fraction.toString(RandomUtils.randomInt(0,15)));
    }

    @Test
    public void getValue() {
        LongFraction fraction = new LongFraction(RandomUtils.randomInt() , RandomUtils.randomInt());
        System.out.println(fraction.getValue());
    }

    @Test
    public void isPositive() {
        LongFraction fraction = new LongFraction(RandomUtils.randomInt(1,1000) , RandomUtils.randomInt(1,1000));
        System.out.println(fraction.getValue());
        Assert.assertTrue(fraction.isPositive());
        fraction = new LongFraction(RandomUtils.randomInt(-10000,-1) , RandomUtils.randomInt(-10000,-1));
        System.out.println(fraction.getValue());
        Assert.assertTrue(fraction.isPositive());
    }

    @Test
    public void isZero() {
        LongFraction fraction = new LongFraction(0 , RandomUtils.randomInt());
        System.out.println(fraction.getValue());
        Assert.assertTrue(fraction.isZero());
    }

    @Test
    public void isNegative() {
        LongFraction fraction = new LongFraction(RandomUtils.randomInt(1,1000) , RandomUtils.randomInt(-10000,-1));
        System.out.println(fraction.getValue());
        Assert.assertTrue(fraction.isNegative());
        fraction = new LongFraction(RandomUtils.randomInt(-10000,-1) , RandomUtils.randomInt(1,1000));
        System.out.println(fraction.getValue());
        Assert.assertTrue(fraction.isNegative());
    }

    @Test
    public void add() {
        //1/3 + 4/6 = 1;
        LongFraction fractionA = new LongFraction(1 ,3 );
        LongFraction fractionB = new LongFraction(4 ,6);

        LongFraction re = fractionA.add(fractionB);
        Assert.assertEquals("1/1", re.getValue());

        //1/3 + -4/6 = -1/3
        fractionA = new LongFraction(1 ,3 );
        fractionB = new LongFraction(-4 ,6);

        re = fractionA.add(fractionB);
        Assert.assertEquals("-1/3", re.getValue());

        //1/-3 + -4/6 = -1
        fractionA = new LongFraction(1 ,-3 );
        fractionB = new LongFraction(-4 ,6);

        re = fractionA.add(fractionB);
        Assert.assertEquals("-1/1", re.getValue());

    }

    @Test
    public void multiply() {
        LongFraction fractionA = new LongFraction(1 ,3 );
        LongFraction fractionB = new LongFraction(5 ,8);

        LongFraction re = fractionA.multiply(fractionB);
        Assert.assertEquals("5/24", re.getValue());

        fractionA = new LongFraction(1 ,3 );
        fractionB = new LongFraction(-6 ,10);

        re = fractionA.multiply(fractionB);
        Assert.assertEquals("-1/5", re.getValue());

        fractionA = new LongFraction(10 ,-3 );
        fractionB = new LongFraction(-4 ,6);

        re = fractionA.multiply(fractionB);
        Assert.assertEquals("20/9", re.getValue());
    }

    @Test
    public void sub(){
        LongFraction fractionA = new LongFraction(1 ,3 );
        LongFraction fractionB = new LongFraction(4 ,6);

        LongFraction re = fractionA.sub(fractionB);
        Assert.assertEquals("-1/3", re.getValue());

        fractionA = new LongFraction(1 ,3 );
        fractionB = new LongFraction(-4 ,6);

        re = fractionA.sub(fractionB);

        Assert.assertEquals("1/1", re.getValue());
        fractionA = new LongFraction(1 ,-3 );
        fractionB = new LongFraction(-4 ,6);

        re = fractionA.sub(fractionB);
        Assert.assertEquals("1/3", re.getValue());
    }

    @Test
    public void div() {
        LongFraction fractionA = new LongFraction(4 ,9);
        LongFraction fractionB = new LongFraction(5 ,6);

        LongFraction re = fractionA.div(fractionB);
        Assert.assertEquals("8/15", re.getValue());

        fractionA = new LongFraction(7 ,10);
        fractionB = new LongFraction(4 ,5);

        re = fractionA.div(fractionB);
        Assert.assertEquals("7/8", re.getValue());

        fractionA = new LongFraction(5 ,-12 );
        fractionB = new LongFraction(5 ,11);

        re = fractionA.div(fractionB);
        Assert.assertEquals("-11/12", re.getValue());
    }

    @Test
    public void simpleFraction() {
        int baseValue = RandomUtils.randomInt(-1000,1000);
        int multipleValue = RandomUtils.randomInt(100);
        LongFraction fraction = new LongFraction( multipleValue , baseValue * multipleValue).simpleFraction();
        System.out.println(fraction.getValue());
        Assert.assertEquals(Math.abs(baseValue), fraction.getDenominator());
    }

    @Test
    public void compareTo() {
        //等0比较
        LongFraction fractionA = new LongFraction(0 , RandomUtils.randomInt(99999));
        LongFraction fractionB = new LongFraction(0 , RandomUtils.randomInt(99999));
        Assert.assertTrue(fractionA.equals(fractionB));
        Assert.assertTrue(fractionA.compareTo(fractionB) == 0);
        Assert.assertTrue(fractionA.eq(fractionB));

        Supplier<Integer> supplierRandom = () ->  {
            int value = RandomUtils.randomInt(-99999 ,99999);
            if(value == 0) return 1;
            return value;
        };

         fractionA = new LongFraction("-60102/65432");
         fractionB = new LongFraction("-99859/53461");
         Assert.assertTrue(fractionA.compareTo(fractionB) > 0 );

        //其它比较, 多次测试
        IntStream.rangeClosed(1, 1000000).forEach(it -> {
            LongFraction tmpA = new LongFraction(supplierRandom.get() , supplierRandom.get() );
            LongFraction tmpB = new LongFraction(supplierRandom.get() , supplierRandom.get() );
            int value = tmpA.compareTo(tmpB);
            if(value == 0){
                //由于有精度问题 , 我们这里暂定15位
                if(!tmpA.simpleFraction().toString(15).equals(tmpB.simpleFraction().toString(15))) {
                    System.out.println(tmpA.getValue());
                    System.out.println(tmpB.getValue());
                    Assert.assertTrue(false);
                }
                Assert.assertTrue(tmpA.hashCode() == tmpB.hashCode());
                Assert.assertTrue(tmpA.equals(tmpB));
            }else if(value < 0){
                if(tmpA.toDouble() - tmpB.toDouble() >= 0) {
                    System.out.println(tmpA.getValue());
                    System.out.println(tmpB.getValue());
                    Assert.assertTrue(false);
                }
            }else if(value > 0){
                if(tmpA.toDouble() - tmpB.toDouble() <= 0) {
                    System.out.println(tmpA.getValue());
                    System.out.println(tmpB.getValue());
                    Assert.assertTrue(false);
                }
            }
        });
    }

    @Test
    public void isSimpleFraction() {
        int baseValue = RandomUtils.randomInt(3,1000);
        int multipleValue = RandomUtils.randomInt(2,100);
        LongFraction fraction = new LongFraction( multipleValue , baseValue * multipleValue);
        Assert.assertFalse(fraction.isSimpleFraction());
        fraction = fraction.simpleFraction();
        Assert.assertTrue(fraction.isSimpleFraction());
    }
}