package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.lang.CharSetUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

/**
 * 字符转码工具类测试
 */
public class CharSetUtilsTest {

    /**
     * 测试转换为UTF-8 - UTF-8编码
     */
    @Test
    public void testConvertToUTF8FromUTF8() throws UnsupportedEncodingException {
        String content = "测试内容";
        String result = CharSetUtils.convertToUTF8(content);
        Assert.assertEquals("UTF-8编码的内容应该保持不变", content, result);
    }

    /**
     * 测试转换为UTF-8 - ISO8859-1编码
     */
    @Test
    public void testConvertToUTF8FromISO88591() throws UnsupportedEncodingException {
        String content = "test";
        byte[] isoBytes = content.getBytes("ISO-8859-1");
        String isoContent = new String(isoBytes, "ISO-8859-1");
        
        String result = CharSetUtils.convertToUTF8(isoContent);
        Assert.assertEquals("ISO-8859-1编码的内容应该正确转换", content, result);
    }

    /**
     * 测试转换为UTF-8 - GBK编码
     */
    @Test
    public void testConvertToUTF8FromGBK() throws UnsupportedEncodingException {
        String content = "测试";
        byte[] gbkBytes = content.getBytes("GBK");
        String gbkContent = new String(gbkBytes, "GBK");
        
        String result = CharSetUtils.convertToUTF8(gbkContent);
        Assert.assertEquals("GBK编码的内容应该正确转换", content, result);
    }

    /**
     * 测试转换为UTF-8 - UNICODE编码
     */
    @Test
    public void testConvertToUTF8FromUnicode() throws UnsupportedEncodingException {
        String content = "test";
        byte[] unicodeBytes = content.getBytes("Unicode");
        String unicodeContent = new String(unicodeBytes, "Unicode");
        
        String result = CharSetUtils.convertToUTF8(unicodeContent);
        Assert.assertNotNull("UNICODE编码的内容应该能够转换", result);
    }

    /**
     * 测试转换为UTF-8 - 英文内容
     */
    @Test
    public void testConvertToUTF8English() throws UnsupportedEncodingException {
        String content = "Hello World";
        String result = CharSetUtils.convertToUTF8(content);
        Assert.assertEquals("英文内容应该正确转换", content, result);
    }

    /**
     * 测试转换为UTF-8 - 中文内容
     */
    @Test
    public void testConvertToUTF8Chinese() throws UnsupportedEncodingException {
        String content = "你好世界";
        String result = CharSetUtils.convertToUTF8(content);
        Assert.assertEquals("中文内容应该正确转换", content, result);
    }

    /**
     * 测试转换为UTF-8 - 混合内容
     */
    @Test
    public void testConvertToUTF8Mixed() throws UnsupportedEncodingException {
        String content = "Hello 你好 World 世界";
        String result = CharSetUtils.convertToUTF8(content);
        Assert.assertEquals("混合内容应该正确转换", content, result);
    }

    /**
     * 测试转换为UTF-8 - 空字符串
     */
    @Test
    public void testConvertToUTF8Empty() throws UnsupportedEncodingException {
        String content = "";
        String result = CharSetUtils.convertToUTF8(content);
        Assert.assertEquals("空字符串应该保持为空", content, result);
    }

    /**
     * 测试转换为UTF-8 - 特殊字符
     */
    @Test
    public void testConvertToUTF8SpecialChars() throws UnsupportedEncodingException {
        String content = "!@#$%^&*()_+-={}[]|\\:\";'<>,.?/~`";
        String result = CharSetUtils.convertToUTF8(content);
        Assert.assertEquals("特殊字符应该正确转换", content, result);
    }

    /**
     * 测试转换为UTF-8 - 数字
     */
    @Test
    public void testConvertToUTF8Numbers() throws UnsupportedEncodingException {
        String content = "1234567890";
        String result = CharSetUtils.convertToUTF8(content);
        Assert.assertEquals("数字应该正确转换", content, result);
    }

    /**
     * 测试转换为指定编码 - UTF-8到GBK
     */
    @Test
    public void testConvertToUTF8ToGBK() throws UnsupportedEncodingException {
        String content = "测试";
        String result = CharSetUtils.convertToUTF8(content, "GBK");
        Assert.assertNotNull("转换结果不能为空", result);
    }

    /**
     * 测试转换为指定编码 - ISO8859-1到UTF-8
     */
    @Test
    public void testConvertToUTF8FromISO88591ToUTF8() throws UnsupportedEncodingException {
        String content = "test";
        String result = CharSetUtils.convertToUTF8(content, "ISO-8859-1");
        Assert.assertEquals("ISO-8859-1到UTF-8的转换应该正确", content, result);
    }

    /**
     * 测试转换为指定编码 - GBK到UTF-8
     */
    @Test
    public void testConvertToUTF8FromGBKToUTF8() throws UnsupportedEncodingException {
        String content = "测试";
        // 将UTF-8字符串转换为GBK编码的字节数组
        byte[] gbkBytes = content.getBytes( "UTF-8");
        // 使用fixISO88591Encoding方法将ISO-8859-1编码的内容转换回正确的GBK编码
        String result = CharSetUtils.convertToUTF8(new String(gbkBytes, "GBK"), "GBK");
        
        Assert.assertEquals("GBK到UTF-8的转换应该正确", content, result);
    }

    /**
     * 测试转换为指定编码 - 空字符串
     */
    @Test
    public void testConvertToUTF8EmptyString() throws UnsupportedEncodingException {
        String content = "";
        String result = CharSetUtils.convertToUTF8(content, "UTF-8");
        Assert.assertEquals("空字符串应该保持为空", content, result);
    }

    /**
     * 测试转换为指定编码 - 不支持的编码
     */
    @Test(expected = UnsupportedEncodingException.class)
    public void testConvertToUTF8UnsupportedEncoding() throws UnsupportedEncodingException {
        String content = "test";
        CharSetUtils.convertToUTF8(content, "UNSUPPORTED_ENCODING");
    }

    /**
     * 测试转换为指定编码 - null输入
     */
    @Test(expected = NullPointerException.class)
    public void testConvertToUTF8NullInput() throws UnsupportedEncodingException {
        CharSetUtils.convertToUTF8(null, "UTF-8");
    }

    /**
     * 测试转换为指定编码 - null编码
     */
    @Test(expected = NullPointerException.class)
    public void testConvertToUTF8NullCharset() throws UnsupportedEncodingException {
        String content = "test";
        CharSetUtils.convertToUTF8(content, null);
    }

    /**
     * 测试自动编码检测 - GBK编码的中文
     */
    @Test
    public void testAutoDetectGBK() throws UnsupportedEncodingException {
        String content = "中文测试";
        byte[] gbkBytes = content.getBytes("GBK");
        String gbkContent = new String(gbkBytes, "GBK");
        
        String result = CharSetUtils.convertToUTF8(gbkContent);
        Assert.assertEquals("应该自动检测并转换GBK编码", content, result);
    }

    /**
     * 测试自动编码检测 - ISO8859-1编码的英文
     */
    @Test
    public void testAutoDetectISO88591() throws UnsupportedEncodingException {
        String content = "English Test";
        byte[] isoBytes = content.getBytes("ISO-8859-1");
        String isoContent = new String(isoBytes, "ISO-8859-1");
        
        String result = CharSetUtils.convertToUTF8(isoContent);
        Assert.assertEquals("应该自动检测并转换ISO-8859-1编码", content, result);
    }

    /**
     * 测试长文本转换
     */
    @Test
    public void testConvertToUTF8LongText() throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("测试内容").append(i).append(" ");
        }
        String content = sb.toString();
        
        String result = CharSetUtils.convertToUTF8(content);
        Assert.assertEquals("长文本应该正确转换", content, result);
    }

    /**
     * 测试emoji表情符号
     */
    @Test
    public void testConvertToUTF8Emoji() throws UnsupportedEncodingException {
        String content = "Hello 😊 World 🌍";
        String result = CharSetUtils.convertToUTF8(content);
        Assert.assertEquals("emoji表情符号应该正确转换", content, result);
    }
}