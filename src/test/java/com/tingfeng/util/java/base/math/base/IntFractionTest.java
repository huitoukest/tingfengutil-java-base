package com.tingfeng.util.java.base.math.base;

import com.tingfeng.util.java.base.math.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.RoundingMode;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class IntFractionTest {

    @Test
    public void toDouble() {
        IntFraction fraction = new IntFraction(RandomUtils.randomInt() , RandomUtils.randomInt());
        double result = fraction.toDouble();
        Assert.assertTrue("toDouble should return valid value", Double.isFinite(result));
    }

    @Test
    public void toBigDecimal() {
        IntFraction fraction = new IntFraction(RandomUtils.randomInt() , RandomUtils.randomInt());
        int scale = RandomUtils.randomInt(0,10);
        Assert.assertNotNull("toBigDecimal should return value", fraction.toBigDecimal(scale, RoundingMode.HALF_UP));
    }

    @Test
    public void testToString() {
        IntFraction fraction = new IntFraction(RandomUtils.randomInt() , RandomUtils.randomInt());
        int scale = RandomUtils.randomInt(0,15);
        Assert.assertNotNull("toString should return value", fraction.toString(scale));
    }

    @Test
    public void getValue() {
        IntFraction fraction = new IntFraction(RandomUtils.randomInt() , RandomUtils.randomInt());
        Assert.assertNotNull("getValue should return value", fraction.getValue());
    }

    @Test
    public void isPositive() {
        IntFraction fraction = new IntFraction(RandomUtils.randomInt(1,1000) , RandomUtils.randomInt(1,1000));
        Assert.assertTrue(fraction.isPositive());
        fraction = new IntFraction(RandomUtils.randomInt(-10000,-1) , RandomUtils.randomInt(-10000,-1));
        Assert.assertTrue(fraction.isPositive());
    }

    @Test
    public void isZero() {
        IntFraction fraction = new IntFraction(0 , RandomUtils.randomInt());
        Assert.assertTrue(fraction.isZero());
    }

    @Test
    public void isNegative() {
        IntFraction fraction = new IntFraction(RandomUtils.randomInt(1,1000) , RandomUtils.randomInt(-10000,-1));
        Assert.assertTrue(fraction.isNegative());
        fraction = new IntFraction(RandomUtils.randomInt(-10000,-1) , RandomUtils.randomInt(1,1000));
        Assert.assertTrue(fraction.isNegative());
    }

    @Test
    public void add() {
        //1/3 + 4/6 = 1;
        IntFraction fractionA = new IntFraction(1 ,3 );
        IntFraction fractionB = new IntFraction(4 ,6);

        IntFraction re = fractionA.add(fractionB);
        Assert.assertEquals("1/1", re.getValue());

        //1/3 + -4/6 = -1/3
        fractionA = new IntFraction(1 ,3 );
        fractionB = new IntFraction(-4 ,6);

        re = fractionA.add(fractionB);
        Assert.assertEquals("-1/3", re.getValue());

        //1/-3 + -4/6 = -1
        fractionA = new IntFraction(1 ,-3 );
        fractionB = new IntFraction(-4 ,6);

        re = fractionA.add(fractionB);
        Assert.assertEquals("-1/1", re.getValue());

    }

    @Test
    public void multiply() {
        IntFraction fractionA = new IntFraction(1 ,3 );
        IntFraction fractionB = new IntFraction(5 ,8);

        IntFraction re = fractionA.multiply(fractionB);
        Assert.assertEquals("5/24", re.getValue());

        fractionA = new IntFraction(1 ,3 );
        fractionB = new IntFraction(-6 ,10);

        re = fractionA.multiply(fractionB);
        Assert.assertEquals("-1/5", re.getValue());

        fractionA = new IntFraction(10 ,-3 );
        fractionB = new IntFraction(-4 ,6);

        re = fractionA.multiply(fractionB);
        Assert.assertEquals("20/9", re.getValue());
    }

    @Test
    public void sub(){
        IntFraction fractionA = new IntFraction(1 ,3 );
        IntFraction fractionB = new IntFraction(4 ,6);

        IntFraction re = fractionA.sub(fractionB);
        Assert.assertEquals("-1/3", re.getValue());

        fractionA = new IntFraction(1 ,3 );
        fractionB = new IntFraction(-4 ,6);

        re = fractionA.sub(fractionB);

        Assert.assertEquals("1/1", re.getValue());
        fractionA = new IntFraction(1 ,-3 );
        fractionB = new IntFraction(-4 ,6);

        re = fractionA.sub(fractionB);
        Assert.assertEquals("1/3", re.getValue());
    }

    @Test
    public void div() {
        IntFraction fractionA = new IntFraction(4 ,9);
        IntFraction fractionB = new IntFraction(5 ,6);

        IntFraction re = fractionA.div(fractionB);
        Assert.assertEquals("8/15", re.getValue());

        fractionA = new IntFraction(7 ,10);
        fractionB = new IntFraction(4 ,5);

        re = fractionA.div(fractionB);
        Assert.assertEquals("7/8", re.getValue());

        fractionA = new IntFraction(5 ,-12 );
        fractionB = new IntFraction(5 ,11);

        re = fractionA.div(fractionB);
        Assert.assertEquals("-11/12", re.getValue());
    }

    @Test
    public void simpleFraction() {
        int baseValue = RandomUtils.randomInt(-1000,1000);
        int multipleValue = RandomUtils.randomInt(100);
        IntFraction fraction = new IntFraction( multipleValue , baseValue * multipleValue).simpleFraction();
        Assert.assertEquals(Math.abs(baseValue), fraction.getDenominator().intValue());
    }

    @Test
    public void compareTo() {
        //等0比较
        IntFraction fractionA = new IntFraction(0 , RandomUtils.randomInt(99999));
        IntFraction fractionB = new IntFraction(0 , RandomUtils.randomInt(99999));
        Assert.assertTrue(fractionA.equals(fractionB));
        Assert.assertTrue(fractionA.compareTo(fractionB) == 0);
        Assert.assertTrue(fractionA.eq(fractionB));

        Supplier<Integer> supplierRandom = () ->  {
            int value = RandomUtils.randomInt(-99999 ,99999);
            if(value == 0) return 1;
            return value;
        };

         fractionA = new IntFraction("-30405/9494");
         fractionB = new IntFraction("-30405/84323");
         Assert.assertTrue(fractionA.compareTo(fractionB) < 0 );

        //其它比较, 减少循环次数到1000次
        IntStream.rangeClosed(1, 1000).forEach(it -> {
            IntFraction tmpA = new IntFraction(supplierRandom.get() , supplierRandom.get() );
            IntFraction tmpB = new IntFraction(supplierRandom.get() , supplierRandom.get() );
            int value = tmpA.compareTo(tmpB);
            if(value == 0){
                //由于有精度问题 , 我们这里暂定15位
                if(!tmpA.simpleFraction().toString(15).equals(tmpB.simpleFraction().toString(15))) {
                    Assert.fail("相等的分数简化后应该相同: " + tmpA.getValue() + " vs " + tmpB.getValue());
                }
                Assert.assertTrue(tmpA.hashCode() == tmpB.hashCode());
                Assert.assertTrue(tmpA.equals(tmpB));
            }else if(value < 0){
                if(tmpA.toDouble() - tmpB.toDouble() >= 0) {
                    Assert.fail("小于关系判断错误: " + tmpA.getValue() + " >= " + tmpB.getValue());
                }
            }else if(value > 0){
                if(tmpA.toDouble() - tmpB.toDouble() <= 0) {
                    Assert.fail("大于关系判断错误: " + tmpA.getValue() + " <= " + tmpB.getValue());
                }
            }
        });
    }

    @Test
    public void isSimpleFraction() {
        int baseValue = RandomUtils.randomInt(3,1000);
        int multipleValue = RandomUtils.randomInt(2,100);
        IntFraction fraction = new IntFraction( multipleValue , baseValue * multipleValue);
        Assert.assertFalse(fraction.isSimpleFraction());
        fraction = fraction.simpleFraction();
        Assert.assertTrue(fraction.isSimpleFraction());
    }

    @Test
    public void constructorMinValueDenominator() {
        // 分母为 MIN_VALUE 时标准化取反溢出，非零分子应显式抛异常而非静默破坏不变量
        try {
            new IntFraction(1, Integer.MIN_VALUE);
            Assert.fail("应该抛出 ArithmeticException");
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应说明无法标准化", e.getMessage().contains("MIN_VALUE"));
        }
        // 分子为 0 特判：0/MIN_VALUE 数学上等于 0/1，可安全表示
        IntFraction zero = new IntFraction(0, Integer.MIN_VALUE);
        Assert.assertEquals("0/1", zero.getValue());
        // String 构造器同样处理
        try {
            new IntFraction("1/" + Integer.MIN_VALUE);
            Assert.fail("应该抛出 ArithmeticException");
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应说明无法标准化", e.getMessage().contains("MIN_VALUE"));
        }
        IntFraction zeroStr = new IntFraction("0/" + Integer.MIN_VALUE);
        Assert.assertEquals("0/1", zeroStr.getValue());
    }

    @Test
    public void absMinValueNumerator() {
        // 分子为 MIN_VALUE 时 |MIN_VALUE| 超出 int 范围，应抛异常而非返回负数
        try {
            new IntFraction(Integer.MIN_VALUE, 1).abs();
            Assert.fail("应该抛出 ArithmeticException");
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应说明绝对值不可表示", e.getMessage().contains("绝对值"));
        }
        // 普通负数绝对值不受影响
        Assert.assertEquals("3/4", new IntFraction(-3, 4).abs().getValue());
    }

    @Test
    public void subMinValueNumerator() {
        // 1 - MIN_VALUE = 2147483649 超出 int 范围：应抛溢出异常而非静默返回错误负数
        try {
            new IntFraction(1, 1).sub(new IntFraction(Integer.MIN_VALUE, 1));
            Assert.fail("应该抛出 ArithmeticException");
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应包含溢出", e.getMessage().contains("溢出"));
        }
    }

    @Test
    public void toMixedNumberMinValueNumerator() {
        // MIN_VALUE/3：绝对值经 long 升级后正确转换
        MixedNumber mixed = new IntFraction(Integer.MIN_VALUE, 3).toMixedNumber();
        Assert.assertEquals(Integer.valueOf(-715827882), ((IntMixedNumber) mixed).getWholePart());
        Assert.assertEquals(Integer.valueOf(2), ((IntMixedNumber) mixed).getNumerator());
        Assert.assertEquals(Integer.valueOf(3), ((IntMixedNumber) mixed).getDenominator());
    }
}