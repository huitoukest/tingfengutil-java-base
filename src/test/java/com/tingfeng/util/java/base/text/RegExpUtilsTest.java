package com.tingfeng.util.java.base.text;

import com.tingfeng.util.java.base.common.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式工具类测试
 */
public class RegExpUtilsTest {

    /**
     * 测试获取Pattern对象 - 默认标志
     */
    @Test
    public void testGetPatternDefault() {
        Pattern pattern = RegExpUtils.getPattern("\\d+");
        Assert.assertNotNull("Pattern对象不能为空", pattern);
        Assert.assertTrue("Pattern应该匹配数字", pattern.matcher("123").matches());
    }

    /**
     * 测试获取Pattern对象 - 自定义标志
     */
    @Test
    public void testGetPatternWithFlags() {
        Pattern pattern = RegExpUtils.getPattern("hello", Pattern.CASE_INSENSITIVE);
        Assert.assertNotNull("Pattern对象不能为空", pattern);
        Assert.assertTrue("Pattern应该忽略大小写", pattern.matcher("HELLO").matches());
    }

    /**
     * 测试获取Pattern对象 - null输入
     */
    @Test
    public void testGetPatternWithNull() {
        Pattern pattern = RegExpUtils.getPattern(null);
        Assert.assertNull("null输入应该返回null", pattern);
    }

    /**
     * 测试获取Pattern对象 - 缓存功能
     */
    @Test
    public void testGetPatternCache() {
        Pattern pattern1 = RegExpUtils.getPattern("\\d+");
        Pattern pattern2 = RegExpUtils.getPattern("\\d+");
        Assert.assertSame("相同的正则表达式应该返回缓存的Pattern对象", pattern1, pattern2);
    }

    /**
     * 测试字符串匹配 - 完全匹配
     */
    @Test
    public void testIsMatchFullMatch() {
        boolean result = RegExpUtils.isMatch("123", "\\d+", true);
        Assert.assertTrue("123应该完全匹配\\d+", result);
    }

    /**
     * 测试字符串匹配 - 部分匹配
     */
    @Test
    public void testIsMatchPartialMatch() {
        boolean result = RegExpUtils.isMatch("abc123def", "\\d+", false);
        Assert.assertTrue("abc123def应该部分匹配\\d+", result);
    }

    /**
     * 测试字符串匹配 - 不匹配
     */
    @Test
    public void testIsMatchNoMatch() {
        boolean result = RegExpUtils.isMatch("abc", "\\d+", false);
        Assert.assertFalse("abc不应该匹配\\d+", result);
    }

    /**
     * 测试字符串匹配 - null输入
     */
    @Test
    public void testIsMatchWithNull() {
        boolean result = RegExpUtils.isMatch(null, "\\d+", false);
        Assert.assertFalse("null字符串不应该匹配", result);
        
        boolean result2 = RegExpUtils.isMatch("123", null, false);
        Assert.assertFalse("null正则表达式不应该匹配", result2);
    }

    /**
     * 测试默认匹配方法
     */
    @Test
    public void testIsMatchDefault() {
        boolean result = RegExpUtils.isMatch("abc123def", "\\d+");
        Assert.assertTrue("默认应该部分匹配", result);
    }

    /**
     * 测试判断是否为整数
     */
    @Test
    public void testIsIntegerNumber() {
        Assert.assertTrue("123应该是整数", RegExpUtils.isIntegerNumber("123"));
        Assert.assertTrue("-123应该是整数", RegExpUtils.isIntegerNumber("-123"));
        Assert.assertTrue("+123应该是整数", RegExpUtils.isIntegerNumber("+123"));
        Assert.assertFalse("12.3不应该是整数", RegExpUtils.isIntegerNumber("12.3"));
        Assert.assertFalse("abc不应该是整数", RegExpUtils.isIntegerNumber("abc"));
        Assert.assertFalse("0123不应该是整数（前导零）", RegExpUtils.isIntegerNumber("0123"));
        Assert.assertFalse("null不应该是整数", RegExpUtils.isIntegerNumber(null));
    }

    /**
     * 测试判断是否为浮点数
     */
    @Test
    public void testIsFloatNumber() {
        Assert.assertTrue("123.45应该是浮点数", RegExpUtils.isFloatNumber("123.45"));
        Assert.assertTrue("-123.45应该是浮点数", RegExpUtils.isFloatNumber("-123.45"));
        Assert.assertTrue("+123.45应该是浮点数", RegExpUtils.isFloatNumber("+123.45"));
        Assert.assertTrue("0.45应该是浮点数", RegExpUtils.isFloatNumber("0.45"));
        Assert.assertFalse("123不应该是浮点数", RegExpUtils.isFloatNumber("123"));
        Assert.assertFalse("abc不应该是浮点数", RegExpUtils.isFloatNumber("abc"));
        Assert.assertFalse("null不应该是浮点数", RegExpUtils.isFloatNumber(null));
    }

    /**
     * 测试判断是否为纯字母
     */
    @Test
    public void testIsLetter() {
        Assert.assertTrue("abc应该是纯字母", RegExpUtils.isLetter("abc"));
        Assert.assertTrue("ABC应该是纯字母", RegExpUtils.isLetter("ABC"));
        Assert.assertTrue("AbC应该是纯字母", RegExpUtils.isLetter("AbC"));
        Assert.assertFalse("abc123不应该是纯字母", RegExpUtils.isLetter("abc123"));
        Assert.assertFalse("abc def不应该是纯字母", RegExpUtils.isLetter("abc def"));
        Assert.assertFalse("null不应该是纯字母", RegExpUtils.isLetter(null));
    }

    /**
     * 测试URL正则表达式
     */
    @Test
    public void testUrlPattern() {
        Assert.assertTrue("http://example.com应该是URL",
            RegExpUtils.isMatch("http://example.com", RegExpUtils.PatternStr.URL, true));
        Assert.assertTrue("https://example.com应该是URL",
            RegExpUtils.isMatch("https://example.com", RegExpUtils.PatternStr.URL, true));
        Assert.assertTrue("https://example.com/path?query=value应该是URL",
            RegExpUtils.isMatch("https://example.com/path?query=value", RegExpUtils.PatternStr.URL, true));
        Assert.assertFalse("example.com不应该是URL（缺少协议）",
            RegExpUtils.isMatch("example.com", RegExpUtils.PatternStr.URL, true));
    }

    /**
     * 测试HTTP正则表达式
     */
    @Test
    public void testHttpPattern() {
        Assert.assertTrue("http://example.com应该是HTTP",
            RegExpUtils.isMatch("http://example.com", RegExpUtils.PatternStr.HTTP, true));
        Assert.assertTrue("https://example.com应该是HTTP",
            RegExpUtils.isMatch("https://example.com", RegExpUtils.PatternStr.HTTP, true));
        Assert.assertFalse("ftp://example.com不应该是HTTP",
            RegExpUtils.isMatch("ftp://example.com", RegExpUtils.PatternStr.HTTP, true));
    }

    /**
     * 测试邮箱正则表达式
     */
    @Test
    public void testEmailPattern() {
        Assert.assertTrue("test@example.com应该是邮箱",
            RegExpUtils.isMatch("test@example.com", RegExpUtils.PatternStr.EMAIL, true));
        Assert.assertTrue("test.user@example.co.uk应该是邮箱",
            RegExpUtils.isMatch("test.user@example.co.uk", RegExpUtils.PatternStr.EMAIL, true));
        Assert.assertFalse("test@example不应该是邮箱（域名太短）",
            RegExpUtils.isMatch("test@example", RegExpUtils.PatternStr.EMAIL, true));
        Assert.assertFalse("test@.com不应该是邮箱（缺少用户名）",
            RegExpUtils.isMatch("@example.com", RegExpUtils.PatternStr.EMAIL, true));
    }

    /**
     * 测试年龄正则表达式
     */
    @Test
    public void testAgePattern() {
        Assert.assertTrue("25应该是年龄",
            RegExpUtils.isMatch("25", RegExpUtils.PatternStr.AGE, true));
        Assert.assertTrue("99应该是年龄",
            RegExpUtils.isMatch("99", RegExpUtils.PatternStr.AGE, true));
        Assert.assertFalse("0不应该是年龄",
            RegExpUtils.isMatch("0", RegExpUtils.PatternStr.AGE, true));
        Assert.assertFalse("100不应该是年龄（超过99）",
            RegExpUtils.isMatch("100", RegExpUtils.PatternStr.AGE, true));
        Assert.assertFalse("-25不应该是年龄（负数）",
            RegExpUtils.isMatch("-25", RegExpUtils.PatternStr.AGE, true));
    }

    /**
     * 测试生日正则表达式
     */
    @Test
    public void testBirthdayPattern() {
        Assert.assertTrue("2023-01-01应该是生日格式",
            RegExpUtils.isMatch("2023-01-01", RegExpUtils.PatternStr.BIRTHDAY, true));
        Assert.assertTrue("1999-12-31应该是生日格式",
            RegExpUtils.isMatch("1999-12-31", RegExpUtils.PatternStr.BIRTHDAY, true));
        Assert.assertFalse("2023/01/01不应该是生日格式",
            RegExpUtils.isMatch("2023/01/01", RegExpUtils.PatternStr.BIRTHDAY, true));
        Assert.assertFalse("2023-1-1不应该是生日格式（月份和日期需要两位）",
            RegExpUtils.isMatch("2023-1-1", RegExpUtils.PatternStr.BIRTHDAY, true));
    }

    /**
     * 测试中国手机号正则表达式
     */
    @Test
    public void testPhoneCNPattern() {
        Assert.assertTrue("13812345678应该是中国手机号",
            RegExpUtils.isMatch("13812345678", RegExpUtils.PatternStr.PHONE_CN, true));
        Assert.assertTrue("15912345678应该是中国手机号",
            RegExpUtils.isMatch("15912345678", RegExpUtils.PatternStr.PHONE_CN, true));
        Assert.assertTrue("18612345678应该是中国手机号",
            RegExpUtils.isMatch("18612345678", RegExpUtils.PatternStr.PHONE_CN, true));
        Assert.assertTrue("16612345678应该是中国手机号",
            RegExpUtils.isMatch("16612345678", RegExpUtils.PatternStr.PHONE_CN, true));
        Assert.assertFalse("12312345678不应该是中国手机号（号段不对）",
            RegExpUtils.isMatch("12312345678", RegExpUtils.PatternStr.PHONE_CN, true));
        Assert.assertFalse("1381234567不应该是中国手机号（位数不对）",
            RegExpUtils.isMatch("1381234567", RegExpUtils.PatternStr.PHONE_CN, true));
    }

    /**
     * 测试IPv4正则表达式
     */
    @Test
    public void testIpV4Pattern() {
        Assert.assertTrue("192.168.1.1应该是IPv4地址",
            RegExpUtils.isMatch("192.168.1.1", RegExpUtils.PatternStr.IP_V4, true));
        Assert.assertTrue("0.0.0.0应该是IPv4地址",
            RegExpUtils.isMatch("0.0.0.0", RegExpUtils.PatternStr.IP_V4, true));
        Assert.assertTrue("255.255.255.255应该是IPv4地址",
            RegExpUtils.isMatch("255.255.255.255", RegExpUtils.PatternStr.IP_V4, true));
        Assert.assertFalse("256.168.1.1不应该是IPv4地址（超过255）",
            RegExpUtils.isMatch("256.168.1.1", RegExpUtils.PatternStr.IP_V4, true));
        Assert.assertFalse("192.168.1不应该是IPv4地址（缺少一段）",
            RegExpUtils.isMatch("192.168.1", RegExpUtils.PatternStr.IP_V4, true));
    }

    /**
     * 测试分隔符正则表达式
     */
    @Test
    public void testSplitPattern() {
        String[] parts = "a,b;c d".split(RegExpUtils.PatternStr.SPLIT);
        Assert.assertEquals("应该分割成4部分", 4, parts.length);
        Assert.assertEquals("第一部分应该是a", "a", parts[0]);
        Assert.assertEquals("第二部分应该是b", "b", parts[1]);
        Assert.assertEquals("第三部分应该是c", "c", parts[2]);
        Assert.assertEquals("第四部分应该是d", "d", parts[3]);
    }

    /**
     * 测试空白字符正则表达式
     */
    @Test
    public void testBlankPattern() {
        String result = "a\tb\nc d".replaceAll(RegExpUtils.PatternStr.BLANK, "");
        Assert.assertEquals("应该去除所有空白字符", "abcd", result);
    }

    /**
     * 测试整数或浮点数正则表达式
     */
    @Test
    public void testIntOrFloatNumberPattern() {
        Assert.assertTrue("123应该是整数或浮点数",
            RegExpUtils.isMatch("123", RegExpUtils.PatternStr.NUMBER, true));
        Assert.assertTrue("123.45应该是整数或浮点数",
            RegExpUtils.isMatch("123.45", RegExpUtils.PatternStr.NUMBER, true));
        Assert.assertTrue("-123应该是整数或浮点数",
            RegExpUtils.isMatch("-123", RegExpUtils.PatternStr.NUMBER, true));
        Assert.assertTrue("+123.45应该是整数或浮点数",
            RegExpUtils.isMatch("+123.45", RegExpUtils.PatternStr.NUMBER, true));
        Assert.assertTrue("0.45应该是整数或浮点数",
            RegExpUtils.isMatch("0.45", RegExpUtils.PatternStr.NUMBER, true));
    }

    /**
     * 测试非整数正则表达式
     */
    @Test
    public void testNotIntegerPattern() {
        String result = "abc123def".replaceAll(RegExpUtils.PatternStr.NOT_INTEGER, "");
        Assert.assertEquals("应该去除所有数字", "abcdef", result);
    }

    @Test
    public void timeReplaceTest() {
        String time = "2017-10:20";
        String regEx = "^(\\d{4})\\D*(\\d{2})\\D*(\\d{2})\\D*"
                + "((\\d{2})\\D*(\\d{2})\\D*(\\d{2})\\D*(\\d{3})){0,1}$";
        String content = time.replaceFirst(regEx, "$1@$2@$3@$5@$6@$7@$8");
        content = content.replaceAll("@", "\\\n");
        Assert.assertNotNull(content);
    }

    @Test
    public void testGetHttpStatus(){
        String str = "HTTP/1.1 200 OK";
        Pattern pattern = RegExpUtils.getPattern(RegExpUtils.PatternStr.HTTP_STATUS);
        Matcher m = pattern.matcher(str);
        Assert.assertTrue(m.find());
        Assert.assertEquals("200", m.group(1));
    }

    @Test
    public void isMathTest(){
        String str = "HTTP/1.1 200 OK";
        boolean result = RegExpUtils.isMatch(str,"^HTTP.*");
        Assert.assertTrue(result);
    }

    @Test
    public void speedTest(){
        // 性能测试保持8线程但减少循环次数到10000
        TestUtils.printTime(8,10000,index -> {
            String str = "HTTP/1.1 200 OK";
            Pattern pattern = Pattern.compile("^HTTP.[\\d\\.].{1,20}");
            boolean result = pattern.matcher(str).find();
            Assert.assertTrue(result);
        });

        TestUtils.printTime(8,10000,index -> {
            String str = "HTTP/1.1 200 OK";
            boolean result = RegExpUtils.isMatch(str,"^HTTP.[\\d\\.].{1,20}");
            Assert.assertTrue(result);
        });
    }
}