package com.tingfeng.util.java.base.common.utils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.charset.Charset;

/**
 * @Author wangGang
 * @Description //TODO
 * @Date 2019-05-08 17:16
 **/
public class MathUtilsTest {

    @Test
    public void encodeTest(){
        System.out.println('a' > 'A');
        System.out.println(MathUtils.toDecimal("0fff0215","0123456789abcdef".toCharArray()));
        System.out.println(MathUtils.toRadix("0fff02156s9d5gm3m48u",16,62));
        String binaryStr = new BigInteger("0fff02156s9d5gm3m48u".getBytes(Charset.forName("iso-8859-1"))).toString(16);
        System.out.println(MathUtils.toRadix(binaryStr,16,62));
        System.out.println(Base64Utils.enCode("0fff02156s9d5gm3m48u"));
    }

    @Test
    public void toRadixTest(){
        String num = "0123456789456123456";
        String radix = "0123456789abcdef".substring(0,RandomUtils.randomInt(1,16));
        System.out.println(MathUtils.toRadix(num,radix.toCharArray()));
        TestUtils.printTime(1,100000,index -> {
            long numA = RandomUtils.randomLong(0,Long.MAX_VALUE);
            String re = MathUtils.toRadix(String.valueOf(numA),radix.toCharArray());
            if(!re.equals(new BigInteger(String.valueOf(numA)).toString(radix.length()))){
                System.out.println(numA + "," + radix);
            }
            Assert.assertTrue(re.equals(new BigInteger(String.valueOf(numA)).toString(radix.length())));
        });
    }

    @Test
    public void toRadix2Test(){
        String num = "1999999999999";
        System.out.println(MathUtils.toRadix(num,62));
    }

    @Test
    public void addPositiveIntegerTest(){
        String a = "99123";
        String b = "88";
        char[] re = MathUtils.addPositiveInteger(a.toCharArray(),b.toCharArray(),"0123456789".toCharArray());
        System.out.println(new String(re));
        a = "10ff";
        b = "0abc";
        re = MathUtils.addPositiveInteger(a.toCharArray(),b.toCharArray(),"0123456789abcdef".toCharArray());
        System.out.println(new String(re));
    }

}
