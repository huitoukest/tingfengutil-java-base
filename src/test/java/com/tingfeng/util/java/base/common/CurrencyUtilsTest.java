package com.tingfeng.util.java.base.common;

import org.junit.Assert;
import org.junit.Test;

import com.tingfeng.util.java.base.common.utils.CurrencyUtils;
/**
 * 货币测试类
 * @author huitoukest
 *
 */
public class CurrencyUtilsTest {
	@Test
    public void test(){
        try {
        	Assert.assertEquals(CurrencyUtils.toChinaUpper("0"),"零圆整");
        	Assert.assertEquals(CurrencyUtils.toChinaUpper("000000000000"),"零圆整");
        	Assert.assertEquals(CurrencyUtils.toChinaUpper("-000000000000"),"零圆整");
        	Assert.assertEquals(CurrencyUtils.toChinaUpper("-215224635421.00"),"负贰仟壹佰伍拾贰亿贰仟肆佰陆拾叁万伍仟肆佰贰拾壹圆整");
        	Assert.assertEquals(CurrencyUtils.toChinaUpper("0.00"),"零圆整");
        	Assert.assertEquals(CurrencyUtils.toChinaUpper("0.03"),"叁分");
        	Assert.assertEquals(CurrencyUtils.toChinaUpper("0.43"),"肆角叁分");
        	Assert.assertEquals(CurrencyUtils.toChinaUpper("0000.43"),"肆角叁分");
        	Assert.assertEquals(CurrencyUtils.toChinaUpper("10000.43"),"壹万圆零肆角叁分");
        	Assert.assertEquals(CurrencyUtils.toChinaUpper("000000000000.000"),"零圆整");
        	Assert.assertEquals(CurrencyUtils.toChinaUpper("421.0"),"肆佰贰拾壹圆整");
        	Assert.assertEquals(CurrencyUtils.toChinaUpper("421.0000"),"肆佰贰拾壹圆整");
        	Assert.assertEquals(CurrencyUtils.toChinaUpper("215224635421.00"),"贰仟壹佰伍拾贰亿贰仟肆佰陆拾叁万伍仟肆佰贰拾壹圆整");
        	Assert.assertEquals(CurrencyUtils.toChinaUpper("215224635421.53"),"贰仟壹佰伍拾贰亿贰仟肆佰陆拾叁万伍仟肆佰贰拾壹圆伍角叁分");
        	Assert.assertEquals(CurrencyUtils.toChinaUpper("215224635421.03"),     "贰仟壹佰伍拾贰亿贰仟肆佰陆拾叁万伍仟肆佰贰拾壹圆零叁分");
        	Assert.assertEquals(CurrencyUtils.toChinaUpper("2463215224635421.03"),"贰仟肆佰陆拾叁万贰仟壹佰伍拾贰亿贰仟肆佰陆拾叁万伍仟肆佰贰拾壹圆零叁分");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("1"),"壹圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("10"),"壹拾圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("10.01"),"壹拾圆零壹分");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("10000100.01"),"壹仟万零壹佰圆零壹分");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("20.7"),"贰拾圆零柒角");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("10.70"),"壹拾圆零柒角");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("18.5"),"壹拾捌圆伍角");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("200.5"),"贰佰圆零伍角");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("2000"),"贰仟圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("50000"),"伍万圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("500000"),"伍拾万圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("5000000"),"伍佰万圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("50000000"),"伍仟万圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("500000000"),"伍亿圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("5000000000"),"伍拾亿圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("5000000001"),"伍拾亿零壹圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("5000000021"),"伍拾亿零贰拾壹圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("5000000421"),"伍拾亿零肆佰贰拾壹圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("5000005421"),"伍拾亿零伍仟肆佰贰拾壹圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("5000035421"),"伍拾亿零叁万伍仟肆佰贰拾壹圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("5000635421"),"伍拾亿零陆拾叁万伍仟肆佰贰拾壹圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("5004635421"),"伍拾亿零肆佰陆拾叁万伍仟肆佰贰拾壹圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("5024635421"),"伍拾亿贰仟肆佰陆拾叁万伍仟肆佰贰拾壹圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("5224635421"),"伍拾贰亿贰仟肆佰陆拾叁万伍仟肆佰贰拾壹圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("15224635421"),"壹佰伍拾贰亿贰仟肆佰陆拾叁万伍仟肆佰贰拾壹圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("215224635421"),"贰仟壹佰伍拾贰亿贰仟肆佰陆拾叁万伍仟肆佰贰拾壹圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("500021"),"伍拾万零贰拾壹圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("5000821"),"伍佰万零捌佰贰拾壹圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("5050006501"),"伍拾亿伍仟万陆仟伍佰零壹圆整");
            Assert.assertEquals(CurrencyUtils.toChinaUpper("550300001"),"伍亿伍仟零叁拾万零壹圆整");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
