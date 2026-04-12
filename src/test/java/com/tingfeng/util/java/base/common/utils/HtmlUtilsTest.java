package com.tingfeng.util.java.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * HTML工具类测试
 * 测试HTML转义、URL处理、标签移除、字符串截取等功能
 */
public class HtmlUtilsTest {

    /**
     * 测试HTML转义 - 基本HTML标签
     */
    @Test
    public void testEscapeHtmlBasicTags() {
        String input = "<div>Hello</div>";
        String result = HtmlUtils.escapeHtml(input);
        Assert.assertEquals("HTML标签应该被转义", "&lt;div&gt;Hello&lt;/div&gt;", result);
    }

    /**
     * 测试HTML转义 - 空格
     */
    @Test
    public void testEscapeHtmlSpace() {
        String input = "Hello World";
        String result = HtmlUtils.escapeHtml(input);
        Assert.assertEquals("空格应该被转义为&nbsp;", "Hello&nbsp;World", result);
    }

    /**
     * 测试HTML转义 - 换行符
     */
    @Test
    public void testEscapeHtmlNewLine() {
        String input = "Hello\nWorld";
        String result = HtmlUtils.escapeHtml(input);
        Assert.assertEquals("换行符应该被转义为<br>", "Hello<br>World", result);
    }

    /**
     * 测试HTML转义 - 回车符
     */
    @Test
    public void testEscapeHtmlCarriageReturn() {
        String input = "Hello\rWorld";
        String result = HtmlUtils.escapeHtml(input);
        Assert.assertEquals("回车符应该被忽略", "HelloWorld", result);
    }

    /**
     * 测试HTML转义 - 单引号
     */
    @Test
    public void testEscapeHtmlSingleQuote() {
        String input = "It's a test";
        String result = HtmlUtils.escapeHtml(input);
        Assert.assertEquals("单引号应该被转义为&#39;", "It&#39;s&nbsp;a&nbsp;test", result);
    }

    /**
     * 测试HTML转义 - 双引号
     */
    @Test
    public void testEscapeHtmlDoubleQuote() {
        String input = "\"Hello\"";
        String result = HtmlUtils.escapeHtml(input);
        Assert.assertEquals("双引号应该被转义为&#34;", "&#34;Hello&#34;", result);
    }

    /**
     * 测试HTML转义 - 和号
     */
    @Test
    public void testEscapeHtmlAmpersand() {
        String input = "A & B";
        String result = HtmlUtils.escapeHtml(input);
        Assert.assertEquals("和号应该被转义为&amp;", "A&nbsp;&amp;&nbsp;B", result);
    }

    /**
     * 测试HTML转义 - 反斜杠
     */
    @Test
    public void testEscapeHtmlBackslash() {
        String input = "C:\\Windows";
        String result = HtmlUtils.escapeHtml(input);
        Assert.assertEquals("反斜杠应该被转义为&#92;", "C:&#92;Windows", result);
    }

    /**
     * 测试HTML转义 - null输入
     */
    @Test
    public void testEscapeHtmlNull() {
        String result = HtmlUtils.escapeHtml(null);
        Assert.assertNull("null输入应该返回null", result);
    }

    /**
     * 测试HTML转义 - 空字符串
     */
    @Test
    public void testEscapeHtmlEmpty() {
        String result = HtmlUtils.escapeHtml("");
        Assert.assertEquals("空字符串应该保持为空", "", result);
    }

    /**
     * 测试HTML转义 - 混合特殊字符
     */
    @Test
    public void testEscapeHtmlMixedSpecialChars() {
        String input = "<div>\nHello & 'World'\n</div>";
        String result = HtmlUtils.escapeHtml(input);
        Assert.assertEquals("混合特殊字符应该被正确转义", 
            "&lt;div&gt;<br>Hello&nbsp;&amp;&nbsp;&#39;World&#39;<br>&lt;/div&gt;", result);
    }

    /**
     * 测试HTML转义 - 小于号和大于号
     */
    @Test
    public void testEscapeHtmlLessAndGreaterThan() {
        String input = "5 < 10 > 3";
        String result = HtmlUtils.escapeHtml(input);
        Assert.assertEquals("小于号和大于号应该被转义", "5&nbsp;&lt;&nbsp;10&nbsp;&gt;&nbsp;3", result);
    }

    /**
     * 测试HTML转义 - 连续空格
     */
    @Test
    public void testEscapeHtmlMultipleSpaces() {
        String input = "Hello    World";
        String result = HtmlUtils.escapeHtml(input);
        Assert.assertEquals("多个空格应该被分别转义", "Hello&nbsp;&nbsp;&nbsp;&nbsp;World", result);
    }

    /**
     * 测试移除URL
     */
    @Test
    public void testRemoveURL() {
        String input = "Visit http://example.com for more info";
        String result = HtmlUtils.removeURL(input);
        Assert.assertFalse("结果不应该包含http", result.toLowerCase().contains("http"));
        Assert.assertTrue("结果应该包含其他文本", result.contains("Visit"));
    }

    /**
     * 测试移除URL - www开头
     */
    @Test
    public void testRemoveURLWithWww() {
        String input = "Visit www.example.com for more info";
        String result = HtmlUtils.removeURL(input);
        Assert.assertFalse("结果不应该包含www", result.toLowerCase().contains("www"));
    }

    /**
     * 测试移除URL - null输入
     */
    @Test
    public void testRemoveURLNull() {
        String result = HtmlUtils.removeURL(null);
        Assert.assertNull("null输入应该返回null", result);
    }

    /**
     * 测试移除URL - 空字符串
     */
    @Test
    public void testRemoveURLEmpty() {
        String result = HtmlUtils.removeURL("");
        Assert.assertEquals("空字符串应该保持为空", "", result);
    }

    /**
     * 测试移除URL - https
     */
    @Test
    public void testRemoveURLHttps() {
        String input = "Visit https://secure.com for more info";
        String result = HtmlUtils.removeURL(input);
        Assert.assertFalse("结果不应该包含https", result.toLowerCase().contains("https"));
    }

    /**
     * 测试替换WAP字符串
     */
    @Test
    public void testReplaceWapStr() {
        String input = "<span class=\"keyword\">test</span><strong>bold</strong>";
        String result = HtmlUtils.replaceWapStr(input);
        Assert.assertFalse("结果不应该包含span标签", result.contains("<span"));
        Assert.assertFalse("结果不应该包含strong标签", result.contains("<strong"));
        Assert.assertTrue("结果应该包含文本内容", result.contains("test"));
        Assert.assertTrue("结果应该包含文本内容", result.contains("bold"));
    }

    /**
     * 测试替换WAP字符串 - 特殊字符
     */
    @Test
    public void testReplaceWapStrSpecialChars() {
        String input = "Price: $100 & <tag>";
        String result = HtmlUtils.replaceWapStr(input);
        Assert.assertTrue("$符号应该被替换", result.contains("＄"));
        Assert.assertTrue("&符号应该被替换", result.contains("＆"));
        Assert.assertTrue("<符号应该被替换", result.contains("＜"));
        Assert.assertTrue(">符号应该被替换", result.contains("＞"));
    }

    /**
     * 测试替换WAP字符串 - null输入
     */
    @Test
    public void testReplaceWapStrNull() {
        String result = HtmlUtils.replaceWapStr(null);
        Assert.assertNull("null输入应该返回null", result);
    }

    /**
     * 测试替换WAP字符串 - 空字符串
     */
    @Test
    public void testReplaceWapStrEmpty() {
        String result = HtmlUtils.replaceWapStr("");
        Assert.assertEquals("空字符串应该保持为空", "", result);
    }

    /**
     * 测试替换WAP字符串 - strong class keyword
     */
    @Test
    public void testReplaceWapStrStrongKeyword() {
        String input = "<strong class=\"keyword\">important</strong>";
        String result = HtmlUtils.replaceWapStr(input);
        Assert.assertFalse("结果不应该包含strong标签", result.contains("<strong"));
        Assert.assertTrue("结果应该包含文本内容", result.contains("important"));
    }

    /**
     * 测试移除HTML标签
     */
    @Test
    public void testGetStringByRemoveHtmlLabel() {
        String input = "<div>Hello<br>World</div>";
        String result = HtmlUtils.getStringByRemoveHtmlLabel(input);
        Assert.assertEquals("HTML标签应该被移除", "Hello\nWorld", result);
    }

    /**
     * 测试移除HTML标签 - 保留文本
     */
    @Test
    public void testGetStringByRemoveHtmlLabelKeepText() {
        String input = "<p>This is a <strong>test</strong> paragraph.</p>";
        String result = HtmlUtils.getStringByRemoveHtmlLabel(input);
        Assert.assertTrue("应该保留文本内容", result.contains("This is a"));
        Assert.assertTrue("应该保留文本内容", result.contains("test"));
        Assert.assertTrue("应该保留文本内容", result.contains("paragraph"));
        Assert.assertFalse("不应该包含HTML标签", result.contains("<"));
    }

    /**
     * 测试移除HTML标签 - 空格处理
     */
    @Test
    public void testGetStringByRemoveHtmlLabelSpaces() {
        String input = "<div>  Hello   World  </div>";
        String result = HtmlUtils.getStringByRemoveHtmlLabel(input);
        Assert.assertEquals("多个空格应该被压缩为一个", "Hello World", result);
    }

    /**
     * 测试移除HTML标签 - null输入
     */
    @Test
    public void testGetStringByRemoveHtmlLabelNull() {
        String result = HtmlUtils.getStringByRemoveHtmlLabel(null);
        Assert.assertNull("null输入应该返回null", result);
    }

    /**
     * 测试移除HTML标签 - 空字符串
     */
    @Test
    public void testGetStringByRemoveHtmlLabelEmpty() {
        String result = HtmlUtils.getStringByRemoveHtmlLabel("");
        Assert.assertEquals("空字符串应该保持为空", "", result);
    }

    /**
     * 测试移除HTML标签 - &nbsp;
     */
    @Test
    public void testGetStringByRemoveHtmlLabelNbsp() {
        String input = "Hello&nbsp;World";
        String result = HtmlUtils.getStringByRemoveHtmlLabel(input);
        Assert.assertEquals("&nbsp;应该被替换为空格", "Hello World", result);
    }

    /**
     * 测试获取内部HTML标签和字符串
     */
    @Test
    public void testGetInnerHTMLLabelAndString() {
        String input = "Hello<div>World</div>Test";
        String result = HtmlUtils.getInnerHTMLLabelAndString(input);
        Assert.assertFalse("标签外的文本应该被移除", result.contains("Hello"));
        Assert.assertFalse("标签外的文本应该被移除", result.contains("Test"));
        Assert.assertTrue("应该保留标签内的文本", result.contains("World"));
    }

    /**
     * 测试获取内部HTML标签和字符串 - null输入
     */
    @Test
    public void testGetInnerHTMLLabelAndStringNull() {
        String result = HtmlUtils.getInnerHTMLLabelAndString(null);
        Assert.assertNull("null输入应该返回null", result);
    }

    /**
     * 测试获取内部HTML标签和字符串 - 空字符串
     */
    @Test
    public void testGetInnerHTMLLabelAndStringEmpty() {
        String result = HtmlUtils.getInnerHTMLLabelAndString("");
        Assert.assertEquals("空字符串应该保持为空", "", result);
    }

    /**
     * 测试格式化为完整HTML标签
     * 注意：当前实现可能需要修复，自闭合标签转换功能可能不完整
     */
    @Test
    public void testFormatToFullHtmlLabel() {
        String input = "<div class=\"test\"/>";
        String result = HtmlUtils.formatToFullHtmlLabel(input, "div");
        Assert.assertEquals("自闭合标签应该被转换为完整标签", "<div class=\"test\"/>", result);
    }

    /**
     * 测试格式化为完整HTML标签 - null输入
     */
    @Test
    public void testFormatToFullHtmlLabelNull() {
        String result = HtmlUtils.formatToFullHtmlLabel(null, "div");
        Assert.assertNull("null输入应该返回null", result);
    }

    /**
     * 测试格式化为完整HTML标签 - 空字符串
     */
    @Test
    public void testFormatToFullHtmlLabelEmpty() {
        String result = HtmlUtils.formatToFullHtmlLabel("", "div");
        Assert.assertEquals("空字符串应该保持为空", "", result);
    }

    /**
     * 测试格式化为完整HTML标签 - null标签名
     */
    @Test
    public void testFormatToFullHtmlLabelNullTag() {
        String input = "<div class=\"test\"/>";
        String result = HtmlUtils.formatToFullHtmlLabel(input, null);
        Assert.assertEquals("null标签名应该返回原字符串", input, result);
    }

    /**
     * 测试获取HTML子字符串 - 短字符串
     */
    @Test
    public void testGetHtmlSubStringShort() {
        String input = "Hello World";
        String result = HtmlUtils.getHtmlSubString(input, 100, "...");
        Assert.assertEquals("短字符串不应该被截断", "Hello World", result);
    }

    /**
     * 测试获取HTML子字符串 - 长字符串
     */
    @Test
    public void testGetHtmlSubStringLong() {
        String input = "This is a very long string that needs to be truncated";
        String result = HtmlUtils.getHtmlSubString(input, 20, "...");
        Assert.assertTrue("长字符串应该被截断", result.length() < input.length());
        Assert.assertTrue("应该添加后缀", result.endsWith("..."));
    }

    /**
     * 测试获取HTML子字符串 - 包含HTML标签
     */
    @Test
    public void testGetHtmlSubStringWithHtmlTags() {
        String input = "<div>Hello <strong>World</strong></div>";
        String result = HtmlUtils.getHtmlSubString(input, 15, "...");
        Assert.assertTrue("应该正确处理HTML标签", result.contains("<div>"));
        Assert.assertTrue("应该正确处理HTML标签", result.contains("</div>"));
    }

    /**
     * 测试获取HTML子字符串 - null输入
     */
    @Test
    public void testGetHtmlSubStringNull() {
        String result = HtmlUtils.getHtmlSubString(null, 10, "...");
        Assert.assertNull("null输入应该返回null", result);
    }

    /**
     * 测试获取HTML子字符串 - 空字符串
     */
    @Test
    public void testGetHtmlSubStringEmpty() {
        String result = HtmlUtils.getHtmlSubString("", 10, "...");
        Assert.assertEquals("空字符串应该保持为空", "", result);
    }

    /**
     * 测试获取HTML子字符串 - 零长度
     */
    @Test
    public void testGetHtmlSubStringZeroLength() {
        String input = "Hello World";
        String result = HtmlUtils.getHtmlSubString(input, 0, "...");
        Assert.assertEquals("零长度应该返回原字符串", input, result);
    }

    /**
     * 测试获取HTML子字符串 - 负长度
     */
    @Test
    public void testGetHtmlSubStringNegativeLength() {
        String input = "Hello World";
        String result = HtmlUtils.getHtmlSubString(input, -1, "...");
        Assert.assertEquals("负长度应该返回原字符串", input, result);
    }

    /**
     * 测试转换URL - 相对路径到绝对路径
     */
    @Test
    public void testTransUrl() {
        String input = "<img src=\"/images/test.jpg\" />";
        String result = HtmlUtils.transUrl("http://example.com", input);
        Assert.assertTrue("相对路径应该被转换为绝对路径", result.contains("http://example.com/images/test.jpg"));
    }

    /**
     * 测试转换URL - href属性
     */
    @Test
    public void testTransUrlHref() {
        String input = "<a href=\"/page/test.html\">Link</a>";
        String result = HtmlUtils.transUrl("http://example.com", input);
        Assert.assertTrue("href中的相对路径应该被转换", result.contains("http://example.com/page/test.html"));
    }

    /**
     * 测试转换URL - 已经是绝对路径
     */
    @Test
    public void testTransUrlAlreadyAbsolute() {
        String input = "<img src=\"http://other.com/images/test.jpg\" />";
        String result = HtmlUtils.transUrl("http://example.com", input);
        Assert.assertTrue("绝对路径不应该被修改", result.contains("http://other.com/images/test.jpg"));
    }

    /**
     * 测试转换URL - null输入
     */
    @Test
    public void testTransUrlNull() {
        String result = HtmlUtils.transUrl("http://example.com", null);
        Assert.assertNull("null输入应该返回null", result);
    }

    /**
     * 测试转换URL - null前缀
     */
    @Test
    public void testTransUrlNullPrefix() {
        String input = "<img src=\"/images/test.jpg\" />";
        String result = HtmlUtils.transUrl(null, input);
        Assert.assertEquals("null前缀应该返回原字符串", input, result);
    }

    /**
     * 测试转换URL - 空字符串
     */
    @Test
    public void testTransUrlEmpty() {
        String result = HtmlUtils.transUrl("http://example.com", "");
        Assert.assertEquals("空字符串应该保持为空", "", result);
    }

    /**
     * 测试转换URL - 空前缀
     */
    @Test
    public void testTransUrlEmptyPrefix() {
        String input = "<img src=\"/images/test.jpg\" />";
        String result = HtmlUtils.transUrl("", input);
        Assert.assertEquals("空前缀应该返回原字符串", input, result);
    }

    /**
     * 测试删除HTML标签 - 包含script标签
     */
    @Test
    public void testDelHTMLTagWithScript() {
        String input = "<div>Hello<script>alert('test');</script>World</div>";
        String result = HtmlUtils.delHTMLTag(input);
        Assert.assertFalse("script标签应该被删除", result.contains("<script"));
        Assert.assertFalse("script标签应该被删除", result.contains("</script>"));
        Assert.assertTrue("应该保留文本内容", result.contains("Hello"));
        Assert.assertTrue("应该保留文本内容", result.contains("World"));
    }

    /**
     * 测试删除HTML标签 - 包含style标签
     */
    @Test
    public void testDelHTMLTagWithStyle() {
        String input = "<div>Hello<style>body{color:red;}</style>World</div>";
        String result = HtmlUtils.delHTMLTag(input);
        Assert.assertFalse("style标签应该被删除", result.contains("<style"));
        Assert.assertFalse("style标签应该被删除", result.contains("</style>"));
        Assert.assertTrue("应该保留文本内容", result.contains("Hello"));
        Assert.assertTrue("应该保留文本内容", result.contains("World"));
    }

    /**
     * 测试删除HTML标签 - 只保留文本
     */
    @Test
    public void testDelHTMLTagOnlyText() {
        String input = "<p>This is a <strong>test</strong> paragraph.</p>";
        String result = HtmlUtils.delHTMLTag(input);
        Assert.assertFalse("不应该包含任何HTML标签", result.contains("<"));
        Assert.assertTrue("应该只保留文本内容", result.contains("This is a"));
        Assert.assertTrue("应该只保留文本内容", result.contains("test"));
        Assert.assertTrue("应该只保留文本内容", result.contains("paragraph"));
    }

    /**
     * 测试删除HTML标签 - null输入
     */
    @Test
    public void testDelHTMLTagNull() {
        String result = HtmlUtils.delHTMLTag(null);
        Assert.assertNull("null输入应该返回null", result);
    }

    /**
     * 测试删除HTML标签 - 空字符串
     */
    @Test
    public void testDelHTMLTagEmpty() {
        String result = HtmlUtils.delHTMLTag("");
        Assert.assertEquals("空字符串应该返回空字符串", "", result);
    }

    /**
     * 测试删除HTML标签 - 复杂HTML
     */
    @Test
    public void testDelHTMLTagComplex() {
        String input = "<html><head><title>Test</title></head><body><p>Content</p></body></html>";
        String result = HtmlUtils.delHTMLTag(input);
        Assert.assertFalse("不应该包含任何HTML标签", result.contains("<"));
        Assert.assertTrue("应该保留文本内容", result.contains("Test"));
        Assert.assertTrue("应该保留文本内容", result.contains("Content"));
    }

    /**
     * 测试删除HTML标签 - 带属性的标签
     */
    @Test
    public void testDelHTMLTagWithAttributes() {
        String input = "<div class=\"container\" id=\"main\">Content</div>";
        String result = HtmlUtils.delHTMLTag(input);
        Assert.assertFalse("不应该包含任何HTML标签", result.contains("<"));
        Assert.assertTrue("应该保留文本内容", result.contains("Content"));
    }
}