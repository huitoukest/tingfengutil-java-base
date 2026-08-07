package com.tingfeng.util.java.base.math;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Locale;

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

    /**
     * 测试亿位为 0 但更高位非零时亿单位不丢失（B9 修复）
     */
    @Test
    public void testToChinaUpperYiUnitWithZeroYiDigit() throws Exception {
        String result = CurrencyUtils.toChinaUpper("100000001000");
        assertEquals("100000001000应该转换为壹仟亿零壹仟元整", "壹仟亿零壹仟元整", result);
    }

    /**
     * 测试万亿级金额转换（B9 五节节循环）
     */
    @Test
    public void testToChinaUpperWanYiLevel() throws Exception {
        String result = CurrencyUtils.toChinaUpper("123456789012345");
        assertEquals("123456789012345应该转换为壹佰贰拾叁万亿肆仟伍佰陆拾柒亿捌仟玖佰零壹万贰仟叁佰肆拾伍元整",
                "壹佰贰拾叁万亿肆仟伍佰陆拾柒亿捌仟玖佰零壹万贰仟叁佰肆拾伍元整", result);
    }

    /**
     * 测试亿亿级金额转换（B9 亿亿节）
     */
    @Test
    public void testToChinaUpperYiYiLevel() throws Exception {
        String result = CurrencyUtils.toChinaUpper("10000000000000000");
        assertEquals("10000000000000000应该转换为壹亿亿元整", "壹亿亿元整", result);
    }

    /**
     * 测试 long 分上限金额（B9 五节覆盖 long 分全范围）
     */
    @Test
    public void testToChinaUpperLongFenMax() throws Exception {
        String result = CurrencyUtils.toChinaUpper("92233720368547758.07");
        assertEquals("92233720368547758.07应该转换为玖亿亿贰仟贰佰叁拾叁万亿柒仟贰佰零叁亿陆仟捌佰伍拾肆万柒仟柒佰伍拾捌元零柒分",
                "玖亿亿贰仟贰佰叁拾叁万亿柒仟贰佰零叁亿陆仟捌佰伍拾肆万柒仟柒佰伍拾捌元零柒分", result);
    }

    /**
     * 测试大整数格式化不再饱和溢出（B8 修复）
     */
    @Test
    public void testFormatMoneyStringHugeInteger() {
        String result = CurrencyUtils.formatMoneyString(1e19, "default");
        assertEquals("1e19应该格式化为10000000000000000000", "10000000000000000000", result);
    }

    /**
     * 测试超长金额字符串抛出明确异常（B17 修复）
     */
    @Test(expected = IllegalArgumentException.class)
    public void testToChinaUpperTooLongMoney() throws Exception {
        CurrencyUtils.toChinaUpper("999999999999999999999");
    }

    /**
     * 测试元部分乘 100 溢出 long 分范围时抛出明确异常（B17 修复）
     */
    @Test(expected = IllegalArgumentException.class)
    public void testToChinaUpperFenOverflow() throws Exception {
        CurrencyUtils.toChinaUpper("92233720368547759");
    }

    /**
     * 测试分转元字符串 - 千分位与两位小数
     */
    @Test
    public void testFenToYuanStringBasic() {
        assertEquals("123456分应该转换为1,234.56", "1,234.56", CurrencyUtils.fenToYuanString(123456L));
        assertEquals("123分应该转换为1.23", "1.23", CurrencyUtils.fenToYuanString(123L));
        assertEquals("100分应该转换为1.00", "1.00", CurrencyUtils.fenToYuanString(100L));
        assertEquals("5分应该转换为0.05", "0.05", CurrencyUtils.fenToYuanString(5L));
    }

    /**
     * 测试分转元字符串 - 负数（负号前置）
     */
    @Test
    public void testFenToYuanStringNegative() {
        assertEquals("-123456分应该转换为-1,234.56", "-1,234.56", CurrencyUtils.fenToYuanString(-123456L));
        assertEquals("-1分应该转换为-0.01", "-0.01", CurrencyUtils.fenToYuanString(-1L));
        assertEquals("-100分应该转换为-1.00", "-1.00", CurrencyUtils.fenToYuanString(-100L));
    }

    /**
     * 测试分转元字符串 - 零
     */
    @Test
    public void testFenToYuanStringZero() {
        assertEquals("0分应该转换为0.00", "0.00", CurrencyUtils.fenToYuanString(0L));
    }

    /**
     * 测试分转元字符串 - 多组千分位
     */
    @Test
    public void testFenToYuanStringThousandSeparator() {
        assertEquals("100000000分应该转换为1,000,000.00", "1,000,000.00", CurrencyUtils.fenToYuanString(100000000L));
        assertEquals("9223372036854775807分应该转换为92,233,720,368,547,758.07",
                "92,233,720,368,547,758.07", CurrencyUtils.fenToYuanString(Long.MAX_VALUE));
    }

    /**
     * 测试分转元字符串 - long 最小值（BigDecimal 全程精确，无溢出）
     */
    @Test
    public void testFenToYuanStringLongMin() {
        assertEquals("Long.MIN_VALUE分应该转换为-92,233,720,368,547,758.08",
                "-92,233,720,368,547,758.08", CurrencyUtils.fenToYuanString(Long.MIN_VALUE));
    }

    /**
     * 测试分转元字符串 - 符号集写死 Locale.US（D9：防 de_DE 等默认 Locale 千分位/小数点互换漂移）
     */
    @Test
    public void testFenToYuanStringLocaleFixed() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            assertEquals("de_DE 下仍应输出 US 格式 1,234.56", "1,234.56", CurrencyUtils.fenToYuanString(123456L));
            assertEquals("de_DE 下负数仍应输出 US 格式 -1,234.56", "-1,234.56", CurrencyUtils.fenToYuanString(-123456L));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    /**
     * 测试等额本息月供 - 已知值（年利率 4.9%、24 期、10 万本金，独立计算期望 4382.66）
     */
    @Test
    public void testPmtKnownValue() {
        BigDecimal result = CurrencyUtils.pmt(new BigDecimal("0.049"), 24, new BigDecimal("100000"));
        assertEquals("年利率4.9%24期10万应该得到月供4382.66", new BigDecimal("4382.66"), result);
    }

    /**
     * 测试等额本息月供 - 零利率特判（r == 0 时返回 principal/months，避免除零）
     */
    @Test
    public void testPmtZeroRate() {
        BigDecimal result = CurrencyUtils.pmt(BigDecimal.ZERO, 24, new BigDecimal("100000"));
        assertEquals("零利率24期10万应该均摊为4166.67", new BigDecimal("4166.67"), result);
    }

    /**
     * 测试等额本息月供 - 单期公式路径（pmt = P*(1+r)，本金+当月利息）
     */
    @Test
    public void testPmtSinglePeriod() {
        BigDecimal result = CurrencyUtils.pmt(new BigDecimal("0.12"), 1, new BigDecimal("1200"));
        assertEquals("年利率12%单期1200应该得到1212.00", new BigDecimal("1212.00"), result);
    }

    /**
     * 测试等额本息月供 - 零本金返回 0.00
     */
    @Test
    public void testPmtZeroPrincipal() {
        BigDecimal result = CurrencyUtils.pmt(new BigDecimal("0.049"), 24, BigDecimal.ZERO);
        assertEquals("零本金应该得到0.00", new BigDecimal("0.00"), result);
    }

    /**
     * 测试等额本息月供 - 结果保留两位小数（HALF_UP，金额分惯例）
     */
    @Test
    public void testPmtScaleTwo() {
        BigDecimal result = CurrencyUtils.pmt(new BigDecimal("0.049"), 24, new BigDecimal("100000"));
        assertEquals("月供结果 scale 应为 2", 2, result.scale());
    }

    /**
     * 测试等额本息月供 - null 参数异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPmtNullAnnualRate() {
        CurrencyUtils.pmt(null, 24, new BigDecimal("100000"));
    }

    /**
     * 测试等额本息月供 - null 本金异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPmtNullPrincipal() {
        CurrencyUtils.pmt(new BigDecimal("0.049"), 24, null);
    }

    /**
     * 测试等额本息月供 - 负年利率异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPmtNegativeRate() {
        CurrencyUtils.pmt(new BigDecimal("-0.049"), 24, new BigDecimal("100000"));
    }

    /**
     * 测试等额本息月供 - 期数为 0 异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPmtZeroMonths() {
        CurrencyUtils.pmt(new BigDecimal("0.049"), 0, new BigDecimal("100000"));
    }

    /**
     * 测试等额本息月供 - 期数为负异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPmtNegativeMonths() {
        CurrencyUtils.pmt(new BigDecimal("0.049"), -1, new BigDecimal("100000"));
    }

    /**
     * 测试等额本息月供 - 负本金异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPmtNegativePrincipal() {
        CurrencyUtils.pmt(new BigDecimal("0.049"), 24, new BigDecimal("-100000"));
    }

    /**
     * 测试涨跌幅 - 正常上涨（100 → 150，涨幅 50%）
     */
    @Test
    public void testPercentChangeIncrease() {
        assertEquals("100涨到150应该为50.0%", 50.0, CurrencyUtils.percentChange(100.0, 150.0), 0.0001);
    }

    /**
     * 测试涨跌幅 - 正常下跌（100 → 50，跌幅 50%）
     */
    @Test
    public void testPercentChangeDecrease() {
        assertEquals("100跌到50应该为-50.0%", -50.0, CurrencyUtils.percentChange(100.0, 50.0), 0.0001);
    }

    /**
     * 测试涨跌幅 - 无变化返回 0
     */
    @Test
    public void testPercentChangeNoChange() {
        assertEquals("100到100应该为0.0%", 0.0, CurrencyUtils.percentChange(100.0, 100.0), 0.0001);
    }

    /**
     * 测试涨跌幅 - old=0 且 new=0 返回 0
     */
    @Test
    public void testPercentChangeZeroToZero() {
        assertEquals("0到0应该为0.0%", 0.0, CurrencyUtils.percentChange(0.0, 0.0), 0.0001);
    }

    /**
     * 测试涨跌幅 - old=0 上涨返回正无穷（从 0 涨语义显式）
     */
    @Test
    public void testPercentChangeZeroToPositive() {
        assertEquals("0涨到5应该为正无穷", Double.POSITIVE_INFINITY, CurrencyUtils.percentChange(0.0, 5.0), 0.0);
    }

    /**
     * 测试涨跌幅 - old=0 下跌返回负无穷（从 0 跌语义显式）
     */
    @Test
    public void testPercentChangeZeroToNegative() {
        assertEquals("0跌到-5应该为负无穷", Double.NEGATIVE_INFINITY, CurrencyUtils.percentChange(0.0, -5.0), 0.0);
    }

    /**
     * 测试涨跌幅 - 负基数场景（-100 → -200，绝对值翻倍，公式结果为 +100%）
     *
     * 注意：负基数下 (newValue-oldValue)/oldValue*100 的符号与"绝对值增减"方向一致，
     * 如 -100 → -50（绝对值缩小）公式结果为 -50.0，这是标准涨跌幅公式的已知语义
     */
    @Test
    public void testPercentChangeNegativeBase() {
        assertEquals("-100到-200应该为100.0%", 100.0, CurrencyUtils.percentChange(-100.0, -200.0), 0.0001);
        assertEquals("-100到-50（绝对值缩小）应该为-50.0%", -50.0, CurrencyUtils.percentChange(-100.0, -50.0), 0.0001);
    }
}