package com.tingfeng.util.java.base.math;

import com.tingfeng.util.java.base.common.utils.TestUtils;
import com.tingfeng.util.java.base.lang.Base64Utils;
import com.tingfeng.util.java.base.math.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.charset.Charset;

/**
 * @Author huitoukest
 * @Date 2019-05-08 17:16
 **/
public class MathUtilsTest {

    @Test
    public void encodeTest(){
        // 验证基本字符比较和转换功能
        Assert.assertTrue('a' > 'A');
        Assert.assertNotNull(MathUtils.toDecimal("0fff0215","0123456789abcdef".toCharArray()));
        Assert.assertNotNull(MathUtils.toRadix("0fff02156s9d5gm3m48u",16,62));
        String binaryStr = new BigInteger("0fff02156s9d5gm3m48u".getBytes(Charset.forName("iso-8859-1"))).toString(16);
        Assert.assertNotNull(MathUtils.toRadix(binaryStr,16,62));
        Assert.assertNotNull(Base64Utils.enCode("0fff02156s9d5gm3m48u"));
    }

    @Test
    public void toRadixTest(){
        String num = "0123456789456123456";
        String radix = "0123456789abcdef".substring(0,RandomUtils.randomInt(1,16));
        Assert.assertNotNull(MathUtils.toRadix(num,radix.toCharArray()));
        // 使用更小的数值范围，避免OOM
        TestUtils.printTime(1,1000,index -> {
            long numA = RandomUtils.randomLong(0,1000000);
            String re = MathUtils.toRadix(String.valueOf(numA),radix.toCharArray());
            Assert.assertTrue(re.equals(Long.toString(numA, radix.length())));
        });
    }

    @Test
    public void toRadix2Test(){
        String num = "1999999999999";
        Assert.assertNotNull(MathUtils.toRadix(num,62));
    }

    @Test
    public void addPositiveIntegerTest(){
        String a = "99123";
        String b = "88";
        char[] re = MathUtils.addPositiveInteger(a.toCharArray(),b.toCharArray(),"0123456789".toCharArray());
        Assert.assertEquals("99211", new String(re));
        a = "10ff";
        b = "0abc";
        re = MathUtils.addPositiveInteger(a.toCharArray(),b.toCharArray(),"0123456789abcdef".toCharArray());
        Assert.assertEquals("1bbb", new String(re));
    }

}
