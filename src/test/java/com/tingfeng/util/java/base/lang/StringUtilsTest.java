package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.collection.CollectionUtils;
import com.tingfeng.util.java.base.lang.base.Tuple2;
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
        Assert.assertEquals(null, StringUtils.appendValue(false, null));
        Assert.assertEquals(StringUtils.STR_NULL_OBJ, StringUtils.appendValue(true, null));
        Assert.assertEquals("123123", StringUtils.appendValue(false, null, null, "123123"));
        Assert.assertEquals("null123123null", StringUtils.appendValue(true, null, "123123", null));
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
        Assert.assertNotNull(unescape);
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
        String sbcString = "ＨＥＬＬＯ　ＷＯＲＬＤ";
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
        Assert.assertEquals(4, StringUtils.getStringLength("你好")); // 根据Unicode East Asian Width标准，每个汉字算2个长度
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
    
    @Test
    public void testFullWidthHalfWidthConversionDetails() {
        // 测试全角半角转换的详细信息
        String sbcString = "ＨＥＬＬＯ　ＷＯＲＬＤ";
        String dbcString = "HELLO WORLD";
        
        // 测试全角转半角
        String converted = StringUtils.toSbcCaseByDbcCase(sbcString);
        Assert.assertEquals("全角转半角应该正确", dbcString, converted);
        
        // 验证字符长度
        Assert.assertEquals("半角字符串长度应该正确", 11, dbcString.length());
        Assert.assertEquals("转换后的字符串长度应该正确", 11, converted.length());
        
        // 验证空格转换
        String halfWidthSpace = " ";
        String fullWidthSpace = "　"; // U+3000 全角空格
        Assert.assertEquals("半角空格转全角应该正确", fullWidthSpace, StringUtils.toDbcCaseBySbcCase(halfWidthSpace));
        Assert.assertEquals("全角空格转半角应该正确", halfWidthSpace, StringUtils.toSbcCaseByDbcCase(fullWidthSpace));
    }
    
    @Test
    public void testFullWidthHalfWidthAsciiConversion() {
        // 测试字母转换
        String halfWidthLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        String fullWidthLetters = "ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ";
        
        Assert.assertEquals("半角字母转全角应该正确", fullWidthLetters, StringUtils.toDbcCaseBySbcCase(halfWidthLetters));
        Assert.assertEquals("全角字母转半角应该正确", halfWidthLetters, StringUtils.toSbcCaseByDbcCase(fullWidthLetters));
        
        // 测试数字转换
        String halfWidthDigits = "0123456789";
        String fullWidthDigits = "０１２３４５６７８９";
        
        Assert.assertEquals("半角数字转全角应该正确", fullWidthDigits, StringUtils.toDbcCaseBySbcCase(halfWidthDigits));
        Assert.assertEquals("全角数字转半角应该正确", halfWidthDigits, StringUtils.toSbcCaseByDbcCase(fullWidthDigits));
    }
    
    @Test
    public void testFullWidthHalfWidthPunctuationConversion() {
        // 测试标点符号转换
        String halfWidthPunctuation = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
        String fullWidthPunctuation = "！＂＃＄％＆＇（）＊＋，－．／：；＜＝＞？＠［＼］＾＿｀｛｜｝～";
        
        Assert.assertEquals("半角标点转全角应该正确", fullWidthPunctuation, StringUtils.toDbcCaseBySbcCase(halfWidthPunctuation));
        Assert.assertEquals("全角标点转半角应该正确", halfWidthPunctuation, StringUtils.toSbcCaseByDbcCase(fullWidthPunctuation));
    }
    
    @Test
    public void testFullWidthHalfWidthCjkUnchanged() {
        // 测试中日韩字符应该保持不变
        String chinese = "你好世界";
        String japanese = "こんにちは";
        String korean = "안녕하세요";
        
        Assert.assertEquals("中文字符应该保持不变", chinese, StringUtils.toDbcCaseBySbcCase(chinese));
        Assert.assertEquals("中文字符应该保持不变", chinese, StringUtils.toSbcCaseByDbcCase(chinese));
        
        Assert.assertEquals("日文字符应该保持不变", japanese, StringUtils.toDbcCaseBySbcCase(japanese));
        Assert.assertEquals("日文字符应该保持不变", japanese, StringUtils.toSbcCaseByDbcCase(japanese));
        
        Assert.assertEquals("韩文字符应该保持不变", korean, StringUtils.toDbcCaseBySbcCase(korean));
        Assert.assertEquals("韩文字符应该保持不变", korean, StringUtils.toSbcCaseByDbcCase(korean));
    }
    
    @Test
    public void testFullWidthHalfWidthMixedContent() {
        // 测试混合内容
        String mixedHalfWidth = "Hello 123! 你好";
        String mixedFullWidth = "Ｈｅｌｌｏ　１２３！　你好";
        
        Assert.assertEquals("混合半角转全角应该正确", "Ｈｅｌｌｏ　１２３！　你好", StringUtils.toDbcCaseBySbcCase(mixedHalfWidth));
        Assert.assertEquals("混合全角转半角应该正确", "Hello 123! 你好", StringUtils.toSbcCaseByDbcCase(mixedFullWidth));
    }
    
    @Test
    public void testFullWidthHalfWidthEdgeCases() {
        // 测试边界情况
        Assert.assertEquals("空字符串应该保持不变", "", StringUtils.toDbcCaseBySbcCase(""));
        Assert.assertEquals("空字符串应该保持不变", "", StringUtils.toSbcCaseByDbcCase(""));
        
        Assert.assertEquals("null应该返回空字符串", "", StringUtils.toDbcCaseBySbcCase(null));
        Assert.assertEquals("null应该返回空字符串", "", StringUtils.toSbcCaseByDbcCase(null));
        
        // 测试双向转换
        String original = "Test123! 测试";
        String fullWidth = StringUtils.toDbcCaseBySbcCase(original);
        String backToHalfWidth = StringUtils.toSbcCaseByDbcCase(fullWidth);
        
        Assert.assertEquals("双向转换应该恢复原始内容", original, backToHalfWidth);
    }
    
    @Test
    public void testFullWidthHalfWidthSpecialUnicodeRanges() {
        // 测试特殊Unicode范围
        // 半角片假名（U+FF65-U+FF9F）应该保持不变
        String halfWidthKatakana = "ｦｧｨｩｪｫｬｭｮｯｰｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝ";
        Assert.assertEquals("半角片假名应该保持不变", halfWidthKatakana, StringUtils.toDbcCaseBySbcCase(halfWidthKatakana));
        Assert.assertEquals("半角片假名应该保持不变", halfWidthKatakana, StringUtils.toSbcCaseByDbcCase(halfWidthKatakana));
        
        // 全角片假名（U+30A0-U+30FF）应该保持不变
        String fullWidthKatakana = "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲン";
        Assert.assertEquals("全角片假名应该保持不变", fullWidthKatakana, StringUtils.toDbcCaseBySbcCase(fullWidthKatakana));
        Assert.assertEquals("全角片假名应该保持不变", fullWidthKatakana, StringUtils.toSbcCaseByDbcCase(fullWidthKatakana));
    }
    
    @Test
    public void testGetStringLengthInternationalCharacters() {
        // ASCII字符：1个长度
        Assert.assertEquals("ASCII字符应该算1个长度", 5, StringUtils.getStringLength("hello"));
        
        // 中文字符：根据Unicode East Asian Width标准算2个长度
        Assert.assertEquals("中文字符应该算2个长度", 4, StringUtils.getStringLength("你好"));
        
        // 日文平假名：全角（2个长度）
        Assert.assertEquals("日文平假名应该算2个长度", 4, StringUtils.getStringLength("こん"));
        
        // 日文片假名：全角（2个长度）
        Assert.assertEquals("日文片假名应该算2个长度", 6, StringUtils.getStringLength("コンニ"));
        
        // 韩文：全角（2个长度）
        Assert.assertEquals("韩文应该算2个长度", 4, StringUtils.getStringLength("안녕"));
        
        // 混合字符
        Assert.assertEquals("混合字符长度计算应该正确", 6, StringUtils.getStringLength("a你b好"));
        
        // 全角ASCII变体：全角（2个长度）
        Assert.assertEquals("全角ASCII变体应该算2个长度", 10, StringUtils.getStringLength("ＨＥＬＬＯ"));
        
        // 拉丁文补充：半角（1个长度）
        Assert.assertEquals("拉丁文补充字符应该算1个长度", 4, StringUtils.getStringLength("café"));
        
        // 空字符串
        Assert.assertEquals("空字符串长度应该为0", 0, StringUtils.getStringLength(""));
        
        // null字符串
        Assert.assertEquals("null字符串长度应该为0", 0, StringUtils.getStringLength(null));
    }
    
    @Test
    public void testGetStringLengthEmojiAndSpecialCharacters() {
        // 表情符号（需要代理对）：每个emoji算2个长度（全角）
        // 注意：Java中emoji使用代理对，但按照Unicode East Asian Width标准，emoji算作全角字符
        Assert.assertEquals("表情符号长度计算应该正确", 2, StringUtils.getStringLength("😀"));
        
        // 多个表情符号
        Assert.assertEquals("多个表情符号长度计算应该正确", 4, StringUtils.getStringLength("😀😁"));
        
        // 中日韩符号和标点：全角（2个长度）
        Assert.assertEquals("中日韩符号应该算2个长度", 2, StringUtils.getStringLength("。"));
        Assert.assertEquals("中日韩符号应该算2个长度", 2, StringUtils.getStringLength("，"));
        
        // 混合emoji和文字
        Assert.assertEquals("混合emoji和文字长度计算应该正确", 6, StringUtils.getStringLength("你好😀"));
    }
    
    @Test
    public void testGetStringLengthEdgeCases() {
        // 单个ASCII字符
        Assert.assertEquals("单个ASCII字符应该算1个长度", 1, StringUtils.getStringLength("a"));
        
        // 单个中文字符
        Assert.assertEquals("单个中文字符应该算2个长度", 2, StringUtils.getStringLength("你"));
        
        // 空格字符
        Assert.assertEquals("空格字符应该算1个长度", 1, StringUtils.getStringLength(" "));
        
        // 制表符
        Assert.assertEquals("制表符应该算1个长度", 1, StringUtils.getStringLength("\t"));
        
        // 换行符
        Assert.assertEquals("换行符应该算1个长度", 1, StringUtils.getStringLength("\n"));
    }
    
    @Test
    public void testGetStringLengthConsistency() {
        // 测试一致性：相同字符多次出现
        String repeated = "你好你好你好";
        int length = StringUtils.getStringLength(repeated);
        Assert.assertEquals("重复中文字符长度应该一致", 12, length); // 6个字符 × 2 = 12
        
        // 测试一致性：不同顺序的相同字符
        String str1 = "abc你好";
        String str2 = "你好abc";
        int length1 = StringUtils.getStringLength(str1);
        int length2 = StringUtils.getStringLength(str2);
        Assert.assertEquals("不同顺序的相同字符长度应该相同", 7, length1); // 3 + 4 = 7
        Assert.assertEquals("不同顺序的相同字符长度应该相同", 7, length2); // 4 + 3 = 7
    }
    
    @Test
    public void testEmojiCharacterDetails() {
        // 测试表情符号的详细字符信息
        String emoji = "😀";
        Assert.assertEquals("表情符号长度计算应该正确", 2, StringUtils.getStringLength(emoji));
        
        // 验证表情符号使用代理对
        Assert.assertEquals("表情符号应该使用代理对", 2, emoji.length());
        
        // 验证代理对字符的Unicode范围
        char firstChar = emoji.charAt(0);
        char secondChar = emoji.charAt(1);
        Assert.assertTrue("第一个字符应该是高位代理", Character.isHighSurrogate(firstChar));
        Assert.assertTrue("第二个字符应该是低位代理", Character.isLowSurrogate(secondChar));
    }
    
    @Test
    public void testKatakanaCharacterDetails() {
        // 测试日文片假名的详细字符信息
        String katakana = "コンニ";
        Assert.assertEquals("日文片假名长度计算应该正确", 6, StringUtils.getStringLength(katakana));
        
        // 验证日文片假名的字符数量
        Assert.assertEquals("日文片假名应该有3个字符", 3, katakana.length());
        
        // 验证日文片假名的Unicode范围（U+30A0-U+30FF）
        for (int i = 0; i < katakana.length(); i++) {
            char c = katakana.charAt(i);
            Assert.assertTrue("日文片假名应该在正确的Unicode范围内", c >= 0x30A0 && c <= 0x30FF);
        }
    }

    @Test
    public void testSafeParseInteger() {
        // normal valid integers
        Tuple2<Boolean, Integer> result1 = StringUtils.safeParseInteger("123");
        Assert.assertTrue(result1.get_1());
        Assert.assertEquals(Integer.valueOf(123), result1.get_2());

        Tuple2<Boolean, Integer> result2 = StringUtils.safeParseInteger("-100");
        Assert.assertTrue(result2.get_1());
        Assert.assertEquals(Integer.valueOf(-100), result2.get_2());

        // +100 should also pass for positive numbers
        Tuple2<Boolean, Integer> result3 = StringUtils.safeParseInteger("+100");
        Assert.assertTrue(result3.get_1());
        Assert.assertEquals(Integer.valueOf(100), result3.get_2());

        // invalid
        Tuple2<Boolean, Integer> result4 = StringUtils.safeParseInteger(null);
        Assert.assertFalse(result4.get_1());

        Tuple2<Boolean, Integer> result5 = StringUtils.safeParseInteger("");
        Assert.assertFalse(result5.get_1());

        Tuple2<Boolean, Integer> result6 = StringUtils.safeParseInteger("abc");
        Assert.assertFalse(result6.get_1());

        // boundary values
        Tuple2<Boolean, Integer> result7 = StringUtils.safeParseInteger("2147483647");
        Assert.assertTrue(result7.get_1());

        Tuple2<Boolean, Integer> result8 = StringUtils.safeParseInteger("-2147483648");
        Assert.assertTrue(result8.get_1());
    }

    @Test
    public void testSafeParseByte() {
        Tuple2<Boolean, Byte> r1 = StringUtils.safeParseByte("127");
        Assert.assertTrue(r1.get_1());
        Assert.assertEquals(Byte.valueOf((byte)127), r1.get_2());

        Tuple2<Boolean, Byte> r2 = StringUtils.safeParseByte("-128");
        Assert.assertTrue(r2.get_1());
        Assert.assertEquals(Byte.valueOf((byte)-128), r2.get_2());

        Tuple2<Boolean, Byte> r3 = StringUtils.safeParseByte("+100");
        Assert.assertTrue(r3.get_1());
        Assert.assertEquals(Byte.valueOf((byte)100), r3.get_2());

        Assert.assertFalse(StringUtils.safeParseByte(null).get_1());
        Assert.assertFalse(StringUtils.safeParseByte("").get_1());
        Assert.assertFalse(StringUtils.safeParseByte("abc").get_1());
    }

    @Test
    public void testSafeParseShort() {
        Tuple2<Boolean, Short> r1 = StringUtils.safeParseShort("32767");
        Assert.assertTrue(r1.get_1());
        Assert.assertEquals(Short.valueOf((short)32767), r1.get_2());

        Tuple2<Boolean, Short> r2 = StringUtils.safeParseShort("-32768");
        Assert.assertTrue(r2.get_1());
        Assert.assertEquals(Short.valueOf((short)-32768), r2.get_2());

        Tuple2<Boolean, Short> r3 = StringUtils.safeParseShort("+100");
        Assert.assertTrue(r3.get_1());
        Assert.assertEquals(Short.valueOf((short)100), r3.get_2());

        Assert.assertFalse(StringUtils.safeParseShort(null).get_1());
        Assert.assertFalse(StringUtils.safeParseShort("").get_1());
        Assert.assertFalse(StringUtils.safeParseShort("abc").get_1());
    }

    @Test
    public void testSafeParseLong() {
        Tuple2<Boolean, Long> r1 = StringUtils.safeParseLong("9223372036854775807");
        Assert.assertTrue(r1.get_1());
        Assert.assertEquals(Long.valueOf(9223372036854775807L), r1.get_2());

        Tuple2<Boolean, Long> r2 = StringUtils.safeParseLong("-9223372036854775808");
        Assert.assertTrue(r2.get_1());
        Assert.assertEquals(Long.valueOf(-9223372036854775808L), r2.get_2());

        Tuple2<Boolean, Long> r3 = StringUtils.safeParseLong("+100");
        Assert.assertTrue(r3.get_1());
        Assert.assertEquals(Long.valueOf(100L), r3.get_2());

        Tuple2<Boolean, Long> r4 = StringUtils.safeParseLong("100L");
        Assert.assertTrue(r4.get_1());
        Assert.assertEquals(Long.valueOf(100L), r4.get_2());

        Assert.assertFalse(StringUtils.safeParseLong(null).get_1());
        Assert.assertFalse(StringUtils.safeParseLong("").get_1());
        Assert.assertFalse(StringUtils.safeParseLong("abc").get_1());
    }

    @Test
    public void testSafeParseFloat() {
        Tuple2<Boolean, Float> r1 = StringUtils.safeParseFloat("3.14");
        Assert.assertTrue(r1.get_1());
        Assert.assertEquals(Float.valueOf(3.14f), r1.get_2());

        Tuple2<Boolean, Float> r2 = StringUtils.safeParseFloat("-3.14");
        Assert.assertTrue(r2.get_1());
        Assert.assertEquals(Float.valueOf(-3.14f), r2.get_2());

        Tuple2<Boolean, Float> r3 = StringUtils.safeParseFloat("+3.14");
        Assert.assertTrue(r3.get_1());
        Assert.assertEquals(Float.valueOf(3.14f), r3.get_2());

        Assert.assertFalse(StringUtils.safeParseFloat(null).get_1());
        Assert.assertFalse(StringUtils.safeParseFloat("").get_1());
        Assert.assertFalse(StringUtils.safeParseFloat("abc").get_1());
    }

    @Test
    public void testSafeParseDouble() {
        Tuple2<Boolean, Double> r1 = StringUtils.safeParseDouble("3.1415926");
        Assert.assertTrue(r1.get_1());
        Assert.assertEquals(Double.valueOf(3.1415926), r1.get_2());

        Tuple2<Boolean, Double> r2 = StringUtils.safeParseDouble("-3.1415926");
        Assert.assertTrue(r2.get_1());
        Assert.assertEquals(Double.valueOf(-3.1415926), r2.get_2());

        Tuple2<Boolean, Double> r3 = StringUtils.safeParseDouble("+3.1415926");
        Assert.assertTrue(r3.get_1());
        Assert.assertEquals(Double.valueOf(3.1415926), r3.get_2());

        Assert.assertFalse(StringUtils.safeParseDouble(null).get_1());
        Assert.assertFalse(StringUtils.safeParseDouble("").get_1());
        Assert.assertFalse(StringUtils.safeParseDouble("abc").get_1());
    }

    @Test
    public void testIsHex() {
        // valid hex
        Assert.assertTrue(StringUtils.isHex("0x0"));
        Assert.assertTrue(StringUtils.isHex("0xABC"));
        Assert.assertTrue(StringUtils.isHex("0XDEF123"));
        Assert.assertTrue(StringUtils.isHex("0xabcdef"));
        Assert.assertTrue(StringUtils.isHex("-0xABC"));
        Assert.assertTrue(StringUtils.isHex("+0xABC"));
        // lowercase hex
        Assert.assertTrue(StringUtils.isHex("0x1a2b3c"));
        // invalid
        Assert.assertFalse(StringUtils.isHex("0x"));
        Assert.assertFalse(StringUtils.isHex("0xGHI"));
        Assert.assertFalse(StringUtils.isHex("abc"));
        Assert.assertFalse(StringUtils.isHex(null));
        Assert.assertFalse(StringUtils.isHex(""));
    }
}