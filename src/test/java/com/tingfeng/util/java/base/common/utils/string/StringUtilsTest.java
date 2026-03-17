package com.tingfeng.util.java.base.common.utils.string;

import com.tingfeng.util.java.base.common.utils.CollectionUtils;
import com.tingfeng.util.java.base.common.utils.RandomUtils;
import com.tingfeng.util.java.base.common.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtilsTest {

    @Test
    public void trimSymbolTest(){
        Assert.assertEquals("2,3", StringUtils.trimSymbol(",2,3,", ","));
        Assert.assertEquals("test", StringUtils.trimSymbol("test", ","));
        Assert.assertEquals("", StringUtils.trimSymbol("", ","));
    }

    @Test
    public void testAppend(){
        Assert.assertEquals("", StringUtils.appendValue(false, null));
        Assert.assertEquals("null", StringUtils.appendValue(true, null));
        Assert.assertEquals("123123", StringUtils.appendValue(false, new Object[]{null, null, "123123"}));
        Assert.assertEquals("123123", StringUtils.appendValue(true, new Object[]{null, "123123", null}));
        Assert.assertEquals("abc123", StringUtils.append("a", "b", "c", 123));
    }

    @Test
    public void testKMPIndexOf(){
        String str = "abbccddeeff";
        String subStr = "cc";
        Assert.assertEquals(3, StringUtils.indexOfByKMP(str, subStr));
        
        // 测试性能
        List<Integer> strList = Stream.generate(() -> (int) (Math.random() * 2))
                         .limit(10000).collect(Collectors.toList());
        String longStr = CollectionUtils.join(strList, "");
        String longSubStr = longStr.substring(5000, 5100);
        int[] next = StringUtils.getKmpNextArray(StringUtils.getCharArray(longSubStr));
        int position1 = longStr.indexOf(longSubStr);
        int position2 = StringUtils.indexOfByKMP(longStr, longSubStr, next);
        Assert.assertEquals(position1, position2);
    }

    @Test
    public void indexOfTest(){
        String a = "abbccddeeff";
        String target = "cc";
        Assert.assertEquals(-1, StringUtils.indexOf(a, target, 1, 3));
        Assert.assertEquals(3, StringUtils.indexOf(a, target, 1, 4));
        Assert.assertEquals(3, StringUtils.indexOf(a, target, 1, 5));
    }

    @Test
    public void lastIndexOfTest(){
        String a = "abbccaccddeeff";
        String target = "cc";
        Assert.assertEquals(-1, StringUtils.lastIndexOf(a, target, 1, 3));
        Assert.assertEquals(3, StringUtils.lastIndexOf(a, target, 1, 6));
        Assert.assertEquals(6, StringUtils.lastIndexOf(a, target, 1, 7));
        Assert.assertEquals(6, StringUtils.lastIndexOf(a, target, 6, 7));
        Assert.assertEquals(6, StringUtils.lastIndexOf(a, target, 4, 9));
        Assert.assertEquals(-1, StringUtils.lastIndexOf(a, target, 7, 10));
    }

    @Test
    public void testSplitByStr() {
        Assert.assertEquals(Collections.emptyList(), StringUtils.splitByStr(null, "a"));
        Assert.assertEquals(Collections.emptyList(), StringUtils.splitByStr("a,b,c", ",", 0));
        Assert.assertEquals(Collections.emptyList(), StringUtils.splitByStr("a,b,c", ",", -1));
        Assert.assertEquals("a", StringUtils.splitByStr("a,b,c", ",", 1).stream().collect(Collectors.joining("@")));
        Assert.assertEquals("a", StringUtils.splitByStr("a,", ",", 1).stream().collect(Collectors.joining("@")));
        Assert.assertEquals("a@", StringUtils.splitByStr("a,", ",", 2).stream().collect(Collectors.joining("@")));
        Assert.assertEquals("a", StringUtils.splitByStr("a", ",", 1).stream().collect(Collectors.joining("@")));
        Assert.assertEquals("a", StringUtils.splitByStr("a", ",", 2).stream().collect(Collectors.joining("@")));
        Assert.assertEquals("a", StringUtils.splitByStr("a", ",", 3).stream().collect(Collectors.joining("@")));
        Assert.assertEquals("a@b@c@d", StringUtils.splitByStr("a,b,c,d", ",", 4).stream().collect(Collectors.joining("@")));
        Assert.assertEquals("a@b@c@d@e@@", StringUtils.splitByStr("a,b,c,d,e,,", ",").stream().collect(Collectors.joining("@")));
        Assert.assertEquals("a@b@c@d@e@", StringUtils.splitByStr("a,b,c,d,e,", ",").stream().collect(Collectors.joining("@")));
    }

    @Test
    public void testEquals() {
        Assert.assertTrue(StringUtils.equals("", ""));
        Assert.assertTrue(StringUtils.equals("", "", ""));
        Assert.assertTrue(StringUtils.equals("123", 1, 2, 3));
        Assert.assertFalse(StringUtils.equals("123", 1, null, 3));
        Assert.assertTrue(StringUtils.equals("123456", 1, 2, 3, "", "", 4, 5, 6, ""));
        Assert.assertTrue(StringUtils.equals("123456", 1, 2, 3, "", "", 4, 5, 6));
        Assert.assertFalse(StringUtils.equals("123", ""));
        Assert.assertFalse(StringUtils.equals("123", 1));
        Assert.assertFalse(StringUtils.equals("123", 1, 3));
        Assert.assertFalse(StringUtils.equals("123", 1, 2, 3, 4));
        Assert.assertTrue(StringUtils.equals("123456", 1, 2, 3, "456"));
        Assert.assertTrue(StringUtils.equals("123456", "123", "456"));
    }

    @Test
    public void unescape() {
        String str = "\\\\123";
        String unescape = StringUtils.unescape(str);
        System.out.println(unescape);
    }

    @Test
    public void testGetStringByStream() {
        String testString = "Hello, World!";
        InputStream in = new ByteArrayInputStream(testString.getBytes());
        String result = StringUtils.getStringByStream(in);
        Assert.assertEquals(testString, result);
    }

    @Test
    public void testGetStringByBytes() throws Exception {
        String testString = "Hello, World!";
        byte[] bytes = testString.getBytes("UTF-8");
        String result = StringUtils.getStringByBytes(bytes, "UTF-8");
        Assert.assertEquals(testString, result);
        String resultDefault = StringUtils.getStringByBytes(bytes);
        Assert.assertEquals(testString, resultDefault);
    }

    @Test
    public void testToLowerFirstChar() {
        Assert.assertEquals("helloWorld", StringUtils.toLowerFirstChar("HelloWorld"));
        Assert.assertEquals("h", StringUtils.toLowerFirstChar("H"));
        Assert.assertEquals("", StringUtils.toLowerFirstChar(""));
    }

    @Test
    public void testGetValue() {
        Integer result = StringUtils.getValue("123", 0, 999, Integer::parseInt);
        Assert.assertEquals(Integer.valueOf(123), result);
        Integer emptyResult = StringUtils.getValue("", 0, 999, Integer::parseInt);
        Assert.assertEquals(Integer.valueOf(0), emptyResult);
        Integer errorResult = StringUtils.getValue("abc", 0, 999, Integer::parseInt);
        Assert.assertEquals(Integer.valueOf(999), errorResult);
    }

    @Test
    public void testTypeConversions() {
        // Integer
        Assert.assertEquals(Integer.valueOf(123), StringUtils.getInteger("123"));
        Assert.assertEquals(Integer.valueOf(999), StringUtils.getInteger("abc", 999));
        Assert.assertEquals(Integer.valueOf(0), StringUtils.getInteger("", 0, 999));
        
        // Long
        Assert.assertEquals(Long.valueOf(123L), StringUtils.getLong("123"));
        Assert.assertEquals(Long.valueOf(999L), StringUtils.getLong("abc", 999L));
        
        // Double
        Assert.assertEquals(Double.valueOf(123.45), StringUtils.getDouble("123.45"));
        Assert.assertEquals(Double.valueOf(999.99), StringUtils.getDouble("abc", 999.99));
        
        // Float
        Assert.assertEquals(Float.valueOf(123.45f), StringUtils.getFloat("123.45"));
        Assert.assertEquals(Float.valueOf(999.99f), StringUtils.getFloat("abc", 999.99f));
        
        // Short
        Assert.assertEquals(Short.valueOf((short) 123), StringUtils.getShort("123"));
        Assert.assertEquals(Short.valueOf((short) 999), StringUtils.getShort("abc", (short) 999));
        
        // Byte
        Assert.assertEquals(Byte.valueOf((byte) 123), StringUtils.getByte("123"));
        Assert.assertEquals(Byte.valueOf((byte) 99), StringUtils.getByte("abc", (byte) 99));
        
        // Boolean
        Assert.assertEquals(Boolean.TRUE, StringUtils.getBoolean("true"));
        Assert.assertEquals(Boolean.FALSE, StringUtils.getBoolean("false"));
        Assert.assertEquals(Boolean.TRUE, StringUtils.getBoolean("abc", true));
    }

    @Test
    public void testCaseConversion() {
        String sbcString = "ＨＥＬＬＯ ＷＯＲＬＤ";
        String dbcString = "HELLO WORLD";
        Assert.assertEquals(dbcString, StringUtils.toSbcCaseByDbcCase(sbcString));
        Assert.assertEquals(sbcString, StringUtils.toDbcCaseBySbcCase(dbcString));
    }

    @Test
    public void testCamelToUnderline() {
        Assert.assertEquals("hello_world", StringUtils.camelToUnderline("helloWorld"));
        Assert.assertEquals("hello", StringUtils.camelToUnderline("hello"));
        Assert.assertEquals("", StringUtils.camelToUnderline(""));
    }

    @Test
    public void testUnderlineToCamel() {
        Assert.assertEquals("helloWorld", StringUtils.underlineToCamel("hello_world"));
        Assert.assertEquals("hello", StringUtils.underlineToCamel("hello"));
        Assert.assertEquals("", StringUtils.underlineToCamel(""));
    }

    @Test
    public void testIsEmpty() {
        Assert.assertTrue(StringUtils.isEmpty(null));
        Assert.assertTrue(StringUtils.isEmpty(""));
        Assert.assertTrue(StringUtils.isEmpty("   "));
        Assert.assertFalse(StringUtils.isEmpty("test"));
        
        Assert.assertTrue(StringUtils.isEmpty("   ", true));
        Assert.assertFalse(StringUtils.isEmpty("   ", false));
    }

    @Test
    public void testIsNotEmpty() {
        Assert.assertFalse(StringUtils.isNotEmpty(null));
        Assert.assertFalse(StringUtils.isNotEmpty(""));
        Assert.assertFalse(StringUtils.isNotEmpty("   "));
        Assert.assertTrue(StringUtils.isNotEmpty("test"));
    }

    @Test
    public void testIsUpperCase() {
        Assert.assertTrue(StringUtils.isUpperCase("HELLO"));
        Assert.assertFalse(StringUtils.isUpperCase("Hello"));
        Assert.assertFalse(StringUtils.isUpperCase("hello"));
    }

    @Test
    public void testIsContainUpperCase() {
        Assert.assertTrue(StringUtils.isContainUpperCase("Hello"));
        Assert.assertFalse(StringUtils.isContainUpperCase("hello"));
    }

    @Test
    public void testIsAnyItemContainsSource() {
        String[] array = {"hello", "world", "test"};
        Assert.assertTrue(StringUtils.isAnyItemContainsSource(array, "ell"));
        Assert.assertFalse(StringUtils.isAnyItemContainsSource(array, "xyz"));
    }

    @Test
    public void testIsContainsAnyItem() {
        List<String> items = Arrays.asList("hello", "world", "test");
        Assert.assertTrue(StringUtils.isContainsAnyItem("hello world", items));
        Assert.assertFalse(StringUtils.isContainsAnyItem("xyz", items));
    }

    @Test
    public void testIsContainsAllItem() {
        List<String> items = Arrays.asList("hello", "world");
        Assert.assertTrue(StringUtils.isContainsAllItem("hello world", items));
        Assert.assertFalse(StringUtils.isContainsAllItem("hello", items));
    }

    @Test
    public void testIsCharSequence() {
        Assert.assertTrue(StringUtils.isCharSequence(String.class));
        Assert.assertTrue(StringUtils.isCharSequence(StringBuilder.class));
        Assert.assertFalse(StringUtils.isCharSequence(Integer.class));
        
        Assert.assertTrue(StringUtils.isCharSequence("java.lang.String"));
        Assert.assertFalse(StringUtils.isCharSequence("java.lang.Integer"));
    }

    @Test
    public void testIsContainLowerCase() {
        Assert.assertTrue(StringUtils.isContainLowerCase("Hello"));
        Assert.assertFalse(StringUtils.isContainLowerCase("HELLO"));
    }

    @Test
    public void testToUpperFirstChar() {
        Assert.assertEquals("Hello", StringUtils.toUpperFirstChar("hello"));
        Assert.assertEquals("H", StringUtils.toUpperFirstChar("h"));
    }

    @Test
    public void testToLowerByPrefix() {
        Assert.assertEquals("helloWorld", StringUtils.toLowerByPrefix("HelloWorld", 1));
    }

    @Test
    public void testRemovePrefixAfterPrefixToLower() {
        Assert.assertEquals("elloWorld", StringUtils.removePrefixAfterPrefixToLower("HelloWorld", 1));
    }

    @Test
    public void testGetSubString() {
        Assert.assertEquals("hel", StringUtils.getSubString("hello", 3));
        Assert.assertEquals("hello", StringUtils.getSubString("hello", 10));
        Assert.assertNull(StringUtils.getSubString(null, 3));
    }

    @Test
    public void testGetStringByLimitLength() {
        String testString = "Hello World";
        String result = StringUtils.getStringByLimitLength(testString, 5);
        Assert.assertTrue(result.length() <= 5 * 2 + 3); // 5个汉字长度 + "..."
    }

    @Test
    public void testGetStringLength() {
        Assert.assertEquals(5, StringUtils.getStringLength("hello"));
        Assert.assertEquals(6, StringUtils.getStringLength("你好")); // 每个汉字算2个长度
    }

    @Test
    public void testToHideEmailPrefix() {
        String email = "test@example.com";
        String hidden = StringUtils.toHideEmailPrefix(email);
        Assert.assertTrue(hidden.startsWith("****"));
        Assert.assertTrue(hidden.endsWith("@example.com"));
    }

    @Test
    public void testRepeat() {
        Assert.assertEquals("***", StringUtils.repeat("*", 3));
        Assert.assertEquals("", StringUtils.repeat("*", 0));
    }

    @Test
    public void testFormatFloat() {
        Assert.assertEquals("123.45", StringUtils.formatFloat(123.45f, "#.00"));
    }

    @Test
    public void testReplaceBlank() {
        String testString = "Hello\nWorld\t";
        String result = StringUtils.replaceBlank(testString);
        Assert.assertEquals("HelloWorld", result);
    }

    @Test
    public void testGetStringByChangCoding() {
        String testString = "Hello World";
        String result = StringUtils.getStringByChangCoding(testString, "UTF-8", "UTF-8");
        Assert.assertEquals(testString, result);
    }

    @Test
    public void testReplaceByReg() {
        String testString = "Hello 123 World";
        String result = StringUtils.replaceByReg(testString, "\\d+", "456");
        Assert.assertEquals("Hello 456 World", result);
    }

    @Test
    public void testGetSubStringPositions() {
        String testString = "Hello,World,Test";
        List<Integer> positions = StringUtils.getSubStringPositions(testString, ",");
        Assert.assertNotNull(positions);
    }

    @Test
    public void testGetStringsByPattern() {
        String testString = "Hello 123 World 456";
        Set<String> result = StringUtils.getStringsByPattern(testString, "\\d+");
        Assert.assertTrue(result.contains("123"));
        Assert.assertTrue(result.contains("456"));
    }

    @Test
    public void testEncodeURL() {
        String testString = "Hello World";
        String result = StringUtils.encodeURL(testString, "UTF-8");
        Assert.assertEquals("Hello+World", result);
    }

    @Test
    public void testReplaceBracketStr() {
        String testString = "（Hello）";
        String result = StringUtils.replaceBracketStr(testString);
        Assert.assertEquals("(Hello)", result);
    }

    @Test
    public void testDoAppend() {
        String result = StringUtils.doAppend(sb -> {
            sb.append("Hello");
            sb.append(" ");
            sb.append("World");
            return sb.toString();
        });
        Assert.assertEquals("Hello World", result);
    }

    @Test
    public void testGetKmpNextArray() {
        String pattern = "ABABC";
        int[] next = StringUtils.getKmpNextArray(pattern.toCharArray());
        Assert.assertNotNull(next);
        Assert.assertEquals(5, next.length);
    }

    @Test
    public void testGetCharArray() {
        String testString = "Hello";
        char[] chars = StringUtils.getCharArray(testString);
        Assert.assertNotNull(chars);
        Assert.assertEquals(5, chars.length);
        Assert.assertEquals('H', chars[0]);
    }

    @Test
    public void testLeftPad() {
        String result = StringUtils.leftPad("123", 5, '0');
        Assert.assertEquals("00123", result);
    }

    @Test
    public void testRightPad() {
        String result = StringUtils.rightPad("123", 5, '0');
        Assert.assertEquals("12300", result);
    }

    @Test
    public void testReverse() {
        String testString = "Hello";
        String result = StringUtils.reverse(testString);
        Assert.assertEquals("olleH", result);
    }

    @Test
    public void testFirstLetterToLower() {
        Assert.assertEquals("hello", StringUtils.firstLetterToLower("Hello"));
        Assert.assertEquals("h", StringUtils.firstLetterToLower("H"));
    }

    @Test
    public void testReplaceByTemplate() {
        String content = "Hello ${name}, welcome to ${place}!";
        Map<String, String> params = new HashMap<>();
        params.put("name", "World");
        params.put("place", "Java");
        String result = StringUtils.replaceByTemplate(content, params);
        Assert.assertEquals("Hello World, welcome to Java!", result);
    }

    @Test
    public void testSplit() {
        String testString = "a,b,c,d";
        String[] result = StringUtils.split(testString, ",");
        Assert.assertEquals(4, result.length);
        Assert.assertEquals("a", result[0]);
        Assert.assertEquals("d", result[3]);
    }

    @Test
    public void testToDecodeStringUrl() {
        String encodedUrl = "Hello%20World";
        String result = StringUtils.toDecodeStringUrl(encodedUrl, "UTF-8");
        Assert.assertEquals("Hello World", result);
    }

    @Test
    public void testToString() {
        Assert.assertEquals("123", StringUtils.toString(123, 0));
        Assert.assertEquals("", StringUtils.toString(0, 0));
        Assert.assertNull(StringUtils.toString(null, 0));
    }
}