package com.tingfeng.util.java.base.math.base;

import com.tingfeng.util.java.base.math.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.RoundingMode;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class LongFractionTest {

    @Test
    public void toDouble() {
        LongFraction fraction = new LongFraction(RandomUtils.randomInt() , RandomUtils.randomInt());
        double result = fraction.toDouble();
        Assert.assertTrue("toDouble should return valid value", Double.isFinite(result));
    }

    @Test
    public void toBigDecimal() {
        LongFraction fraction = new LongFraction(RandomUtils.randomInt() , RandomUtils.randomInt());
        int scale = RandomUtils.randomInt(0,10);
        Assert.assertNotNull("toBigDecimal should return value", fraction.toBigDecimal(scale, RoundingMode.HALF_UP));
    }

    @Test
    public void testToString() {
        LongFraction fraction = new LongFraction(RandomUtils.randomInt() , RandomUtils.randomInt());
        int scale = RandomUtils.randomInt(0,15);
        Assert.assertNotNull("toString should return value", fraction.toString(scale));
    }

    @Test
    public void getValue() {
        LongFraction fraction = new LongFraction(RandomUtils.randomInt() , RandomUtils.randomInt());
        Assert.assertNotNull("getValue should return value", fraction.getValue());
    }

    @Test
    public void isPositive() {
        LongFraction fraction = new LongFraction(RandomUtils.randomInt(1,1000) , RandomUtils.randomInt(1,1000));
        Assert.assertTrue(fraction.isPositive());
        fraction = new LongFraction(RandomUtils.randomInt(-10000,-1) , RandomUtils.randomInt(-10000,-1));
        Assert.assertTrue(fraction.isPositive());
    }

    @Test
    public void isZero() {
        LongFraction fraction = new LongFraction(0 , RandomUtils.randomInt());
        Assert.assertTrue(fraction.isZero());
    }

    @Test
    public void isNegative() {
        LongFraction fraction = new LongFraction(RandomUtils.randomInt(1,1000) , RandomUtils.randomInt(-10000,-1));
        Assert.assertTrue(fraction.isNegative());
        fraction = new LongFraction(RandomUtils.randomInt(-10000,-1) , RandomUtils.randomInt(1,1000));
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
        Assert.assertEquals(Math.abs(baseValue), fraction.getDenominator().intValue());
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

        //其它比较, 减少循环次数到1000次
        IntStream.rangeClosed(1, 1000).forEach(it -> {
            LongFraction tmpA = new LongFraction(supplierRandom.get() , supplierRandom.get() );
            LongFraction tmpB = new LongFraction(supplierRandom.get() , supplierRandom.get() );
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
        LongFraction fraction = new LongFraction( multipleValue , baseValue * multipleValue);
        Assert.assertFalse(fraction.isSimpleFraction());
        fraction = fraction.simpleFraction();
        Assert.assertTrue(fraction.isSimpleFraction());
    }

    @Test
    public void constructorMinValueDenominator() {
        // 分母为 MIN_VALUE 时标准化取反溢出，非零分子应显式抛异常而非静默破坏不变量
        try {
            new LongFraction(1L, Long.MIN_VALUE);
            Assert.fail("应该抛出 ArithmeticException");
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应说明无法标准化", e.getMessage().contains("MIN_VALUE"));
        }
        // 分子为 0 特判：0/MIN_VALUE 数学上等于 0/1，可安全表示
        LongFraction zero = new LongFraction(0L, Long.MIN_VALUE);
        Assert.assertEquals("0/1", zero.getValue());
        // String 构造器同样处理
        try {
            new LongFraction("1/" + Long.MIN_VALUE);
            Assert.fail("应该抛出 ArithmeticException");
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应说明无法标准化", e.getMessage().contains("MIN_VALUE"));
        }
        LongFraction zeroStr = new LongFraction("0/" + Long.MIN_VALUE);
        Assert.assertEquals("0/1", zeroStr.getValue());
    }

    @Test
    public void absMinValueNumerator() {
        // 分子为 MIN_VALUE 时 |MIN_VALUE| 超出 long 范围，应抛异常而非返回负数
        try {
            new LongFraction(Long.MIN_VALUE, 1L).abs();
            Assert.fail("应该抛出 ArithmeticException");
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应说明绝对值不可表示", e.getMessage().contains("绝对值"));
        }
        // 普通负数绝对值不受影响
        Assert.assertEquals("3/4", new LongFraction(-3L, 4L).abs().getValue());
    }

    @Test
    public void subMinValueNumerator() {
        // 1 - MIN_VALUE = 9223372036854775809 超出 long 范围：应抛溢出异常而非静默返回错误负数
        try {
            new LongFraction(1L, 1L).sub(new LongFraction(Long.MIN_VALUE, 1L));
            Assert.fail("应该抛出 ArithmeticException");
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应包含溢出", e.getMessage().contains("溢出"));
        }
        // 常规减法不受影响
        Assert.assertEquals("-1/3", new LongFraction(1L, 3L).sub(new LongFraction(4L, 6L)).getValue());
    }

    @Test
    public void toMixedNumberMinValueNumerator() {
        // MIN_VALUE/3：BigInteger.abs() 修复后正确转换（旧实现 Math.abs 溢出会产生负余数并抛错误异常）
        // 符号约定：负数分数的整数部分取负、分数部分恒正（-2^63/3 = -3074457345618258602 又 2/3）
        MixedNumber mixed = new LongFraction(Long.MIN_VALUE, 3L).toMixedNumber();
        Assert.assertEquals(Long.valueOf(-3074457345618258602L), ((LongMixedNumber) mixed).getWholePart());
        Assert.assertEquals(Long.valueOf(2L), ((LongMixedNumber) mixed).getNumerator());
        Assert.assertEquals(Long.valueOf(3L), ((LongMixedNumber) mixed).getDenominator());
        // MIN_VALUE/1：整数部分绝对值 2^63 超出 long 范围，应显式抛异常
        try {
            new LongFraction(Long.MIN_VALUE, 1L).toMixedNumber();
            Assert.fail("应该抛出 ArithmeticException");
        } catch (ArithmeticException e) {
            Assert.assertTrue("异常信息应说明整数部分溢出", e.getMessage().contains("溢出"));
        }
    }

    @Test
    public void addLong() {
        LongFraction result = new LongFraction(1, 2).add(1L);
        Assert.assertEquals("1/2 + 1 应为 3/2", "3/2", result.getValue());
        result = new LongFraction(1, 2).add(-1L);
        Assert.assertEquals("1/2 + (-1) 应为 -1/2", "-1/2", result.getValue());
    }

    @Test
    public void subLong() {
        LongFraction result = new LongFraction(1, 2).sub(1L);
        Assert.assertEquals("1/2 - 1 应为 -1/2", "-1/2", result.getValue());
    }

    @Test
    public void multiplyLong() {
        LongFraction result = new LongFraction(1, 2).multiply(4L);
        Assert.assertEquals("1/2 × 4 应为 2/1", "2/1", result.getValue());
    }

    @Test
    public void divLong() {
        LongFraction result = new LongFraction(1, 2).div(2L);
        Assert.assertEquals("1/2 ÷ 2 应为 1/4", "1/4", result.getValue());
    }

    @Test
    public void divLongZero() {
        try {
            new LongFraction(1, 2).div(0L);
            Assert.fail("应该抛出 ArithmeticException");
        } catch (ArithmeticException e) {
            // 既有 div(LongFraction) 除零异常透传（BigFraction 消息"除零错误：不能除以 0"）
            Assert.assertTrue("除零异常信息应说明不能除以 0", e.getMessage().contains("不能除以"));
        }
    }

    @Test
    public void addDouble() {
        // 1/3 + 0.5 = 5/6（0.5 精确展开为 1/2，无浮点误差）
        LongFraction result = new LongFraction(1, 3).add(0.5);
        Assert.assertEquals("1/3 + 0.5 应为 5/6", "5/6", result.getValue());
        // 0.1 精确展开为 1/10
        result = new LongFraction(1, 1).add(0.1);
        Assert.assertEquals("1 + 0.1 应为 11/10", "11/10", result.getValue());
        // 1e-2 精确展开为 1/100
        result = new LongFraction(0, 1).add(1e-2);
        Assert.assertEquals("0 + 1e-2 应为 1/100", "1/100", result.getValue());
    }

    @Test
    public void addDoubleScaleNegative() {
        // scale < 0 合法分支：1e18 展开为 10^18/1
        LongFraction result = new LongFraction(0, 1).add(1e18);
        Assert.assertEquals("0 + 1e18 应为 1000000000000000000/1", "1000000000000000000/1", result.getValue());
    }

    @Test
    public void subDouble() {
        LongFraction result = new LongFraction(1, 3).sub(0.5);
        Assert.assertEquals("1/3 - 0.5 应为 -1/6", "-1/6", result.getValue());
        result = new LongFraction(0, 1).sub(0.5);
        Assert.assertEquals("0 - 0.5 应为 -1/2", "-1/2", result.getValue());
    }

    @Test
    public void multiplyDouble() {
        LongFraction result = new LongFraction(2, 3).multiply(0.5);
        Assert.assertEquals("2/3 × 0.5 应为 1/3", "1/3", result.getValue());
    }

    @Test
    public void divDouble() {
        LongFraction result = new LongFraction(1, 2).div(0.5);
        Assert.assertEquals("1/2 ÷ 0.5 应为 1/1", "1/1", result.getValue());
    }

    @Test
    public void divDoubleZero() {
        try {
            new LongFraction(1, 2).div(0.0);
            Assert.fail("应该抛出 ArithmeticException");
        } catch (ArithmeticException e) {
            // 既有 div(LongFraction) 除零异常透传（BigFraction 消息"除零错误：不能除以 0"）
            Assert.assertTrue("除零异常信息应说明不能除以 0", e.getMessage().contains("不能除以"));
        }
    }

    @Test
    public void addDoublePrecision() {
        // 0.1 + 0.2 = 3/10 精确成立（BigDecimal 展开无二进制浮点误差）
        LongFraction result = new LongFraction(0, 1).add(0.1).add(0.2);
        Assert.assertEquals("0.1 + 0.2 应为 3/10", "3/10", result.getValue());
    }

    @Test
    public void addDoubleEqualsSameType() {
        // 混合运算与同型运算结果一致
        LongFraction viaDouble = new LongFraction(1, 3).add(0.5);
        LongFraction viaFraction = new LongFraction(1, 3).add(new LongFraction(5, 10));
        Assert.assertEquals("混合运算与同型运算结果应一致", viaFraction.getValue(), viaDouble.getValue());
        Assert.assertTrue(viaDouble.equals(viaFraction));
    }

    @Test
    public void addDoubleDenominatorOverflow() {
        // 1e-19 → scale=20 → 分母 10^20 超出 long 范围，抛 IllegalArgumentException（非 ArithmeticException 非静默截断）
        try {
            new LongFraction(0, 1).add(1e-19);
            Assert.fail("应该抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue("异常信息应说明分母超出范围", e.getMessage().contains("分母"));
        }
    }

    @Test
    public void addDoubleNumeratorOverflow() {
        // 1e19 → scale=-19 → 分子 10^19 超出 long 范围，抛 IllegalArgumentException（非静默截断）
        try {
            new LongFraction(0, 1).add(1e19);
            Assert.fail("应该抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue("异常信息应说明分子超出范围", e.getMessage().contains("分子"));
        }
        // Double.MAX_VALUE → 分子远超 long 范围
        try {
            new LongFraction(0, 1).add(Double.MAX_VALUE);
            Assert.fail("应该抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue("异常信息应说明分子超出范围", e.getMessage().contains("分子"));
        }
    }

    @Test
    public void addDoubleNonFinite() {
        try {
            new LongFraction(0, 1).add(Double.NaN);
            Assert.fail("应该抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue("异常信息应说明 NaN", e.getMessage().contains("NaN"));
        }
        try {
            new LongFraction(0, 1).add(Double.POSITIVE_INFINITY);
            Assert.fail("应该抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue("异常信息应说明 Infinity", e.getMessage().contains("Infinity"));
        }
    }

    @Test
    public void fractionInheritMixedOperation() {
        // Fraction 继承 LongFraction，自动获得混合运算（返回类型为 LongFraction）
        LongFraction result = new Fraction(1, 2).add(0.5);
        Assert.assertEquals("Fraction 1/2 + 0.5 应为 1/1", "1/1", result.getValue());
        result = new Fraction(1, 2).multiply(4L);
        Assert.assertEquals("Fraction 1/2 × 4 应为 2/1", "2/1", result.getValue());
    }
}