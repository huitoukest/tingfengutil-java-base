package com.tingfeng.util.java.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * 货币处理工具类测试
 */
public class CurrencyUtilsTest {

    /**
     * 测试将数字转换为中国大写字符串 - 整数
     */
    @Test
    public void testToChinaUpperInteger() throws Exception {
        String result = CurrencyUtils.toChinaUpper("123");
        assertEquals("123应该转换为壹佰贰拾叁元整", "壹佰贰拾叁元整", result);
    }

    /**
     * 测试将数字转换为中国大写字符串 - 带小数
     */
    @Test
    public void testToChinaUpperWithDecimal() throws Exception {
        String result = CurrencyUtils.toChinaUpper("123.45");
        assertEquals("123.45应该转换为壹佰贰拾叁元肆角伍分", "壹佰贰拾叁元肆角伍分", result);
    }

    /**
     * 测试将数字转换为中国大写字符串 - 只有角
     */
    @Test
    public void testToChinaUpperWithJiao() throws Exception {
        String result = CurrencyUtils.toChinaUpper("123.4");
        assertEquals("123.4应该转换为壹佰贰拾叁元肆角", "壹佰贰拾叁元肆角", result);
    }

    /**
     * 测试将数字转换为中国大写字符串 - 零
     */
    @Test
    public void testToChinaUpperZero() throws Exception {
        String result = CurrencyUtils.toChinaUpper("0");
        assertEquals("0应该转换为零元整", "零元整", result);
    }

    /**
     * 测试将数字转换为中国大写字符串 - 小数点后只有零
     */
    @Test
    public void testToChinaUpperZeroDecimal() throws Exception {
        String result = CurrencyUtils.toChinaUpper("123.00");
        assertEquals("123.00应该转换为壹佰贰拾叁元整", "壹佰贰拾叁元整", result);
    }

    /**
     * 测试将数字转换为中国大写字符串 - 负数
     */
    @Test
    public void testToChinaUpperNegative() throws Exception {
        String result = CurrencyUtils.toChinaUpper("-123");
        assertEquals("-123应该转换为负壹佰贰拾叁元整", "负壹佰贰拾叁元整", result);
    }

    /**
     * 测试将数字转换为中国大写字符串 - 带前导零
     */
    @Test
    public void testToChinaUpperWithLeadingZeros() throws Exception {
        String result = CurrencyUtils.toChinaUpper("00123");
        assertEquals("00123应该转换为壹佰贰拾叁元整", "壹佰贰拾叁元整", result);
    }

    /**
     * 测试将数字转换为中国大写字符串 - 大数字
     */
    @Test
    public void testToChinaUpperLargeNumber() throws Exception {
        String result = CurrencyUtils.toChinaUpper("100000000");
        assertEquals("100000000应该转换为壹亿元整", "壹亿元整", result);
    }

    /**
     * 测试将数字转换为中国大写字符串 - 万位
     */
    @Test
    public void testToChinaUpperWan() throws Exception {
        String result = CurrencyUtils.toChinaUpper("10000");
        assertEquals("10000应该转换为壹万元整", "壹万元整", result);
    }

    /**
     * 测试将数字转换为中国大写字符串 - 千位
     */
    @Test
    public void testToChinaUpperQian() throws Exception {
        String result = CurrencyUtils.toChinaUpper("1000");
        assertEquals("1000应该转换为壹仟元整", "壹仟元整", result);
    }

    /**
     * 测试将数字转换为中国大写字符串 - 佰位
     */
    @Test
    public void testToChinaUpperBai() throws Exception {
        String result = CurrencyUtils.toChinaUpper("100");
        assertEquals("100应该转换为壹佰元整", "壹佰元整", result);
    }

    /**
     * 测试将数字转换为中国大写字符串 - 拾位
     */
    @Test
    public void testToChinaUpperShi() throws Exception {
        String result = CurrencyUtils.toChinaUpper("10");
        assertEquals("10应该转换为壹拾元整", "壹拾元整", result);
    }

    /**
     * 测试将数字转换为中国大写字符串 - 个位
     */
    @Test
    public void testToChinaUpperOne() throws Exception {
        String result = CurrencyUtils.toChinaUpper("1");
        assertEquals("1应该转换为壹元整", "壹元整", result);
    }

    /**
     * 测试将数字转换为中国大写字符串 - 包含零的数字
     */
    @Test
    public void testToChinaUpperWithZeros() throws Exception {
        String result = CurrencyUtils.toChinaUpper("10101");
        assertEquals("10101应该转换为壹万零壹佰零壹元整", "壹万零壹佰零壹元整", result);
    }

    /**
     * 测试将数字转换为中国大写字符串 - 亿万组合
     */
    @Test
    public void testToChinaUpperYiWan() throws Exception {
        String result = CurrencyUtils.toChinaUpper("100000001");
        assertEquals("100000001应该转换为壹亿零壹元整", "壹亿零壹元整", result);
    }

    /**
     * 测试将数字转换为中国大写字符串 - 格式错误
     */
    @Test(expected = Exception.class)
    public void testToChinaUpperInvalidFormat() throws Exception {
        CurrencyUtils.toChinaUpper("abc");
    }

    /**
     * 测试将数字转换为中国大写字符串 - 空字符串
     */
    @Test(expected = Exception.class)
    public void testToChinaUpperEmptyString() throws Exception {
        CurrencyUtils.toChinaUpper("");
    }

    /**
     * 测试将数字转换为中国大写字符串 - 只有负号
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
        assertEquals("123.0应该格式化为123", "123", result);
    }

    /**
     * 测试格式化货币字符串 - 默认样式（小数）
     */
    @Test
    public void testFormatMoneyStringDefaultDecimal() {
        String result = CurrencyUtils.formatMoneyString(123.45, "default");
        assertEquals("123.45应该保持原样", "123.45", result);
    }

    /**
     * 测试格式化货币字符串 - 默认样式（零）
     */
    @Test
    public void testFormatMoneyStringDefaultZero() {
        String result = CurrencyUtils.formatMoneyString(0.0, "default");
        assertEquals("0.0应该格式化为空字符串", "", result);
    }

    /**
     * 测试格式化货币字符串 - 自定义格式
     */
    @Test
    public void testFormatMoneyStringCustomFormat() {
        String result = CurrencyUtils.formatMoneyString(1234.5, "#,###.00");
        assertEquals("1234.5应该格式化为1,234.50", "1,234.50", result);
    }

    /**
     * 测试格式化货币字符串 - 两位小数格式
     */
    @Test
    public void testFormatMoneyStringTwoDecimal() {
        String result = CurrencyUtils.formatMoneyString(123.456, "#.00");
        assertEquals("123.456应该格式化为123.46（四舍五入）", "123.46", result);
    }

    /**
     * 测试格式化货币字符串 - 一位小数格式
     */
    @Test
    public void testFormatMoneyStringOneDecimal() {
        String result = CurrencyUtils.formatMoneyString(123.456, "#.0");
        assertEquals("123.456应该格式化为123.5（四舍五入）", "123.5", result);
    }

    /**
     * 测试格式化货币字符串 - 无小数格式
     */
    @Test
    public void testFormatMoneyStringNoDecimal() {
        String result = CurrencyUtils.formatMoneyString(123.999, "#");
        assertEquals("123.999应该格式化为124（四舍五入）", "124", result);
    }

    /**
     * 测试格式化货币字符串 - null样式
     */
    @Test
    public void testFormatMoneyStringNullStyle() {
        String result = CurrencyUtils.formatMoneyString(123.45, null);
        assertEquals("null样式应该返回字符串表示", "123.45", result);
    }

    /**
     * 测试格式化货币字符串 - 负数
     */
    @Test
    public void testFormatMoneyStringNegative() {
        String result = CurrencyUtils.formatMoneyString(-123.45, "#.00");
        assertEquals("-123.45应该格式化为-123.45", "-123.45", result);
    }

    /**
     * 测试格式化货币字符串 - 大数字
     */
    @Test
    public void testFormatMoneyStringLargeNumber() {
        String result = CurrencyUtils.formatMoneyString(123456789.12, "#,###.00");
        assertEquals("123456789.12应该格式化为123,456,789.12", "123,456,789.12", result);
    }

    /**
     * 测试将数字转换为中国大写字符串 - 复杂案例1
     */
    @Test
    public void testToChinaUpperComplexCase1() throws Exception {
        String result = CurrencyUtils.toChinaUpper("100100.01");
        assertEquals("100100.01应该转换为壹拾万零壹佰元零壹分", "壹拾万零壹佰元零壹分", result);
    }

    /**
     * 测试将数字转换为中国大写字符串 - 复杂案例2
     */
    @Test
    public void testToChinaUpperComplexCase2() throws Exception {
        String result = CurrencyUtils.toChinaUpper("100010.10");
        assertEquals("100010.10应该转换为壹拾万零壹拾元壹角", "壹拾万零壹拾元壹角", result);
    }

    /**
     * 测试将数字转换为中国大写字符串 - 复杂案例3
     */
    @Test
    public void testToChinaUpperComplexCase3() throws Exception {
        String result = CurrencyUtils.toChinaUpper("100001.11");
        assertEquals("100001.11应该转换为壹拾万零壹元壹角壹分", "壹拾万零壹元壹角壹分", result);
    }

    // 建议添加到单元测试中
    @Test
    public void testYuanConversion() throws Exception {
        assertEquals("壹亿零壹元整", CurrencyUtils.toChinaUpper("100000001"));  // 核心用例
        assertEquals("壹亿元整", CurrencyUtils.toChinaUpper("100000000"));      // 整亿
        assertEquals("壹万零壹元整", CurrencyUtils.toChinaUpper("10001"));        // 万节边界
        assertEquals("壹亿零壹万元整", CurrencyUtils.toChinaUpper("100010000"));   // 亿+万
        assertEquals("壹亿贰仟叁佰肆拾伍万陆仟柒佰捌拾玖元整",
                CurrencyUtils.toChinaUpper("123456789"));  // 完整测试
    }
    
    /**
     * 测试带自定义前缀和元单位的人民币大写转换
     */
    @Test
    public void testToChinaUpperWithPrefixAndUnit() throws Exception {
        // 使用自定义前缀和元单位
        String result = CurrencyUtils.toChinaUpper("123", "人民币", "圆");
        assertEquals("123使用自定义前缀和单位应该转换为人民币壹佰贰拾叁圆整", "人民币壹佰贰拾叁圆整", result);
        
        // 使用"圆"作为单位
        result = CurrencyUtils.toChinaUpper("456.78", "人民币", "圆");
        assertEquals("456.78使用圆单位应该转换为人民币肆佰伍拾陆圆柒角捌分", "人民币肆佰伍拾陆圆柒角捌分", result);
        
        // 使用"元"作为单位
        result = CurrencyUtils.toChinaUpper("456.78", "人民币", "元");
        assertEquals("456.78使用元单位应该转换为人民币肆佰伍拾陆元柒角捌分", "人民币肆佰伍拾陆元柒角捌分", result);
        
        // 使用空前缀
        result = CurrencyUtils.toChinaUpper("123", "", "元");
        assertEquals("123使用空前缀应该转换为壹佰贰拾叁元整", "壹佰贰拾叁元整", result);
        
        // 使用自定义前缀
        result = CurrencyUtils.toChinaUpper("123", "大写:", "元");
        assertEquals("123使用自定义前缀应该转换为大写:壹佰贰拾叁元整", "大写:壹佰贰拾叁元整", result);
    }
}