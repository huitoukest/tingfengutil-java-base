package com.tingfeng.util.java.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * 货币处理工具类测试
 */
public class CurrencyUtilsTest {

    /**
     * 测试将数字转换为中国人民币大写字符串 - 整数
     */
    @Test
    public void testToChinaUpperInteger() throws Exception {
        String result = CurrencyUtils.toChinaUpper("123");
        Assert.assertEquals("123应该转换为壹佰贰拾叁圆整", "壹佰贰拾叁圆整", result);
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 带小数
     */
    @Test
    public void testToChinaUpperWithDecimal() throws Exception {
        String result = CurrencyUtils.toChinaUpper("123.45");
        Assert.assertEquals("123.45应该转换为壹佰贰拾叁圆肆角伍分", "壹佰贰拾叁圆肆角伍分", result);
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 只有角
     */
    @Test
    public void testToChinaUpperWithJiao() throws Exception {
        String result = CurrencyUtils.toChinaUpper("123.4");
        Assert.assertEquals("123.4应该转换为壹佰贰拾叁圆肆角", "壹佰贰拾叁圆肆角", result);
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 零
     */
    @Test
    public void testToChinaUpperZero() throws Exception {
        String result = CurrencyUtils.toChinaUpper("0");
        Assert.assertEquals("0应该转换为零圆整", "零圆整", result);
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 小数点后只有零
     */
    @Test
    public void testToChinaUpperZeroDecimal() throws Exception {
        String result = CurrencyUtils.toChinaUpper("123.00");
        Assert.assertEquals("123.00应该转换为壹佰贰拾叁圆整", "壹佰贰拾叁圆整", result);
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 负数
     */
    @Test
    public void testToChinaUpperNegative() throws Exception {
        String result = CurrencyUtils.toChinaUpper("-123");
        Assert.assertEquals("-123应该转换为负壹佰贰拾叁圆整", "负壹佰贰拾叁圆整", result);
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 带前导零
     */
    @Test
    public void testToChinaUpperWithLeadingZeros() throws Exception {
        String result = CurrencyUtils.toChinaUpper("00123");
        Assert.assertEquals("00123应该转换为壹佰贰拾叁圆整", "壹佰贰拾叁圆整", result);
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 大数字
     */
    @Test
    public void testToChinaUpperLargeNumber() throws Exception {
        String result = CurrencyUtils.toChinaUpper("100000000");
        Assert.assertEquals("100000000应该转换为壹亿圆整", "壹亿圆整", result);
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 万位
     */
    @Test
    public void testToChinaUpperWan() throws Exception {
        String result = CurrencyUtils.toChinaUpper("10000");
        Assert.assertEquals("10000应该转换为壹万圆整", "壹万圆整", result);
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 千位
     */
    @Test
    public void testToChinaUpperQian() throws Exception {
        String result = CurrencyUtils.toChinaUpper("1000");
        Assert.assertEquals("1000应该转换为壹仟圆整", "壹仟圆整", result);
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 佰位
     */
    @Test
    public void testToChinaUpperBai() throws Exception {
        String result = CurrencyUtils.toChinaUpper("100");
        Assert.assertEquals("100应该转换为壹佰圆整", "壹佰圆整", result);
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 拾位
     */
    @Test
    public void testToChinaUpperShi() throws Exception {
        String result = CurrencyUtils.toChinaUpper("10");
        Assert.assertEquals("10应该转换为壹拾圆整", "壹拾圆整", result);
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 个位
     */
    @Test
    public void testToChinaUpperOne() throws Exception {
        String result = CurrencyUtils.toChinaUpper("1");
        Assert.assertEquals("1应该转换为壹圆整", "壹圆整", result);
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 包含零的数字
     */
    @Test
    public void testToChinaUpperWithZeros() throws Exception {
        String result = CurrencyUtils.toChinaUpper("10101");
        Assert.assertEquals("10101应该转换为壹万零壹佰零壹圆整", "壹万零壹佰零壹圆整", result);
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 亿万组合
     */
    @Test
    public void testToChinaUpperYiWan() throws Exception {
        String result = CurrencyUtils.toChinaUpper("100000001");
        Assert.assertEquals("100000001应该转换为壹亿零壹圆整", "壹亿零壹圆整", result);
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 格式错误
     */
    @Test(expected = Exception.class)
    public void testToChinaUpperInvalidFormat() throws Exception {
        CurrencyUtils.toChinaUpper("abc");
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 空字符串
     */
    @Test(expected = Exception.class)
    public void testToChinaUpperEmptyString() throws Exception {
        CurrencyUtils.toChinaUpper("");
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 只有负号
     */
    @Test(expected = Exception.class)
    public void testToChinaUpperOnlyMinus() throws Exception {
        CurrencyUtils.toChinaUpper("-");
    }

    /**
     * 测试格式化货币字符串 - 默认样式（整数）
     */
    @Test
    public void testFormatMoneyStringDefaultInteger() {
        String result = CurrencyUtils.formatMoneyString(123.0, "default");
        Assert.assertEquals("123.0应该格式化为123", "123", result);
    }

    /**
     * 测试格式化货币字符串 - 默认样式（小数）
     */
    @Test
    public void testFormatMoneyStringDefaultDecimal() {
        String result = CurrencyUtils.formatMoneyString(123.45, "default");
        Assert.assertEquals("123.45应该保持原样", "123.45", result);
    }

    /**
     * 测试格式化货币字符串 - 默认样式（零）
     */
    @Test
    public void testFormatMoneyStringDefaultZero() {
        String result = CurrencyUtils.formatMoneyString(0.0, "default");
        Assert.assertEquals("0.0应该格式化为空字符串", "", result);
    }

    /**
     * 测试格式化货币字符串 - 自定义格式
     */
    @Test
    public void testFormatMoneyStringCustomFormat() {
        String result = CurrencyUtils.formatMoneyString(1234.5, "#,###.00");
        Assert.assertEquals("1234.5应该格式化为1,234.50", "1,234.50", result);
    }

    /**
     * 测试格式化货币字符串 - 两位小数格式
     */
    @Test
    public void testFormatMoneyStringTwoDecimal() {
        String result = CurrencyUtils.formatMoneyString(123.456, "#.00");
        Assert.assertEquals("123.456应该格式化为123.46（四舍五入）", "123.46", result);
    }

    /**
     * 测试格式化货币字符串 - 一位小数格式
     */
    @Test
    public void testFormatMoneyStringOneDecimal() {
        String result = CurrencyUtils.formatMoneyString(123.456, "#.0");
        Assert.assertEquals("123.456应该格式化为123.5（四舍五入）", "123.5", result);
    }

    /**
     * 测试格式化货币字符串 - 无小数格式
     */
    @Test
    public void testFormatMoneyStringNoDecimal() {
        String result = CurrencyUtils.formatMoneyString(123.999, "#");
        Assert.assertEquals("123.999应该格式化为124（四舍五入）", "124", result);
    }

    /**
     * 测试格式化货币字符串 - null样式
     */
    @Test
    public void testFormatMoneyStringNullStyle() {
        String result = CurrencyUtils.formatMoneyString(123.45, null);
        Assert.assertEquals("null样式应该返回字符串表示", "123.45", result);
    }

    /**
     * 测试格式化货币字符串 - 负数
     */
    @Test
    public void testFormatMoneyStringNegative() {
        String result = CurrencyUtils.formatMoneyString(-123.45, "#.00");
        Assert.assertEquals("-123.45应该格式化为-123.45", "-123.45", result);
    }

    /**
     * 测试格式化货币字符串 - 大数字
     */
    @Test
    public void testFormatMoneyStringLargeNumber() {
        String result = CurrencyUtils.formatMoneyString(123456789.12, "#,###.00");
        Assert.assertEquals("123456789.12应该格式化为123,456,789.12", "123,456,789.12", result);
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 复杂案例1
     */
    @Test
    public void testToChinaUpperComplexCase1() throws Exception {
        String result = CurrencyUtils.toChinaUpper("100100.01");
        Assert.assertEquals("100100.01应该转换为壹拾万零壹佰圆零壹分", "壹拾万零壹佰圆零壹分", result);
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 复杂案例2
     */
    @Test
    public void testToChinaUpperComplexCase2() throws Exception {
        String result = CurrencyUtils.toChinaUpper("100010.10");
        Assert.assertEquals("100010.10应该转换为壹拾万零壹拾圆壹角", "壹拾万零壹拾圆壹角", result);
    }

    /**
     * 测试将数字转换为中国人民币大写字符串 - 复杂案例3
     */
    @Test
    public void testToChinaUpperComplexCase3() throws Exception {
        String result = CurrencyUtils.toChinaUpper("100001.11");
        Assert.assertEquals("100001.11应该转换为壹拾万零壹圆壹角壹分", "壹拾万零壹圆壹角壹分", result);
    }
}