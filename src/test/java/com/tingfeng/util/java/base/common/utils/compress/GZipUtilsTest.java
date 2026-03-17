package com.tingfeng.util.java.base.common.utils.compress;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * GZIP工具类测试
 */
public class GZipUtilsTest {

    /**
     * 测试压缩 - 基本功能
     */
    @Test
    public void testCompressBasic() {
        String original = "Hello World! This is a test string for GZIP compression.";
        byte[] input = original.getBytes();
        
        byte[] compressed = GZipUtils.compress(input);
        Assert.assertNotNull("压缩结果不能为空", compressed);
        Assert.assertTrue("压缩后的数据应该比原数据小", compressed.length < input.length);
    }

    /**
     * 测试压缩 - 空数组
     */
    @Test
    public void testCompressEmpty() {
        byte[] input = new byte[0];
        
        byte[] compressed = GZipUtils.compress(input);
        Assert.assertNotNull("压缩结果不能为空", compressed);
    }

    /**
     * 测试压缩 - 单字节
     */
    @Test
    public void testCompressSingleByte() {
        byte[] input = {65};
        
        byte[] compressed = GZipUtils.compress(input);
        Assert.assertNotNull("压缩结果不能为空", compressed);
    }

    /**
     * 测试压缩 - 重复数据
     */
    @Test
    public void testCompressRepeatedData() {
        byte[] input = new byte[1000];
        Arrays.fill(input, (byte) 65);
        
        byte[] compressed = GZipUtils.compress(input);
        Assert.assertNotNull("压缩结果不能为空", compressed);
        Assert.assertTrue("重复数据应该有很好的压缩效果", compressed.length < input.length / 10);
    }

    /**
     * 测试压缩 - 随机数据
     */
    @Test
    public void testCompressRandomData() {
        byte[] input = new byte[1000];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (Math.random() * 256);
        }
        
        byte[] compressed = GZipUtils.compress(input);
        Assert.assertNotNull("压缩结果不能为空", compressed);
    }

    /**
     * 测试解压 - 基本功能
     */
    @Test
    public void testUncompressBasic() {
        String original = "Hello World! This is a test string for GZIP compression.";
        byte[] input = original.getBytes();
        
        byte[] compressed = GZipUtils.compress(input);
        byte[] uncompressed = GZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("解压后的数据应该与原数据相同", input, uncompressed);
    }

    /**
     * 测试解压 - 空数组
     */
    @Test
    public void testUncompressEmpty() {
        byte[] input = new byte[0];
        
        byte[] compressed = GZipUtils.compress(input);
        byte[] uncompressed = GZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("解压后的空数组应该与原数组相同", input, uncompressed);
    }

    /**
     * 测试解压 - 单字节
     */
    @Test
    public void testUncompressSingleByte() {
        byte[] input = {65};
        
        byte[] compressed = GZipUtils.compress(input);
        byte[] uncompressed = GZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("解压后的单字节数组应该与原数组相同", input, uncompressed);
    }

    /**
     * 测试解压 - 大数据
     */
    @Test
    public void testUncompressLargeData() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("This is line ").append(i).append(" of test data.\n");
        }
        byte[] input = sb.toString().getBytes();
        
        byte[] compressed = GZipUtils.compress(input);
        byte[] uncompressed = GZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("解压后的大数据应该与原数据相同", input, uncompressed);
    }

    /**
     * 测试解压 - 重复数据
     */
    @Test
    public void testUncompressRepeatedData() {
        byte[] input = new byte[1000];
        Arrays.fill(input, (byte) 65);
        
        byte[] compressed = GZipUtils.compress(input);
        byte[] uncompressed = GZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("解压后的重复数据应该与原数据相同", input, uncompressed);
    }

    /**
     * 测试压缩解压 - 完整流程
     */
    @Test
    public void testCompressUncompressCycle() {
        String original = "This is a complete test of GZIP compression and decompression cycle. " +
                        "We will compress the data, then decompress it, and verify that the result " +
                        "matches the original data exactly.";
        byte[] input = original.getBytes();
        
        byte[] compressed = GZipUtils.compress(input);
        byte[] uncompressed = GZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("压缩解压循环后应该恢复原数据", input, uncompressed);
        String result = new String(uncompressed);
        Assert.assertEquals("字符串内容应该完全相同", original, result);
    }

    /**
     * 测试压缩解压 - 多次循环
     */
    @Test
    public void testMultipleCompressUncompressCycles() {
        String original = "Test data for multiple GZIP compression cycles.";
        byte[] input = original.getBytes();
        
        byte[] data = input;
        for (int i = 0; i < 5; i++) {
            byte[] compressed = GZipUtils.compress(data);
            data = GZipUtils.uncompress(compressed);
        }
        
        Assert.assertArrayEquals("多次压缩解压循环后应该恢复原数据", input, data);
    }

    /**
     * 测试压缩解压 - 二进制数据
     */
    @Test
    public void testCompressUncompressBinaryData() {
        byte[] input = new byte[256];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) i;
        }
        
        byte[] compressed = GZipUtils.compress(input);
        byte[] uncompressed = GZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("二进制数据压缩解压后应该恢复原数据", input, uncompressed);
    }

    /**
     * 测试压缩解压 - 中文数据
     */
    @Test
    public void testCompressUncompressChineseData() {
        String original = "这是中文测试数据，包含各种汉字和标点符号。";
        byte[] input = original.getBytes();
        
        byte[] compressed = GZipUtils.compress(input);
        byte[] uncompressed = GZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("中文数据压缩解压后应该恢复原数据", input, uncompressed);
        String result = new String(uncompressed);
        Assert.assertEquals("中文内容应该完全相同", original, result);
    }

    /**
     * 测试压缩解压 - 特殊字符
     */
    @Test
    public void testCompressUncompressSpecialChars() {
        String original = "!@#$%^&*()_+-={}[]|\\:\";'<>,.?/~`\n\t\r";
        byte[] input = original.getBytes();
        
        byte[] compressed = GZipUtils.compress(input);
        byte[] uncompressed = GZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("特殊字符压缩解压后应该恢复原数据", input, uncompressed);
        String result = new String(uncompressed);
        Assert.assertEquals("特殊字符内容应该完全相同", original, result);
    }

    /**
     * 测试压缩解压 - 混合内容
     */
    @Test
    public void testCompressUncompressMixedContent() {
        String original = "English text 中文内容 123456 !@#$% \n\t\r";
        byte[] input = original.getBytes();
        
        byte[] compressed = GZipUtils.compress(input);
        byte[] uncompressed = GZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("混合内容压缩解压后应该恢复原数据", input, uncompressed);
        String result = new String(uncompressed);
        Assert.assertEquals("混合内容应该完全相同", original, result);
    }

    /**
     * 测试压缩解压 - 性能测试
     */
    @Test
    public void testCompressUncompressPerformance() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("Performance test line ").append(i).append("\n");
        }
        byte[] input = sb.toString().getBytes();
        
        long startTime = System.currentTimeMillis();
        byte[] compressed = GZipUtils.compress(input);
        long compressTime = System.currentTimeMillis() - startTime;
        
        startTime = System.currentTimeMillis();
        byte[] uncompressed = GZipUtils.uncompress(compressed);
        long uncompressTime = System.currentTimeMillis() - startTime;
        
        Assert.assertArrayEquals("数据应该正确恢复", input, uncompressed);
        Assert.assertTrue("压缩时间应该在合理范围内", compressTime < 1000);
        Assert.assertTrue("解压时间应该在合理范围内", uncompressTime < 1000);
    }

    /**
     * 测试压缩解压 - 压缩率测试
     */
    @Test
    public void testCompressionRatio() {
        String original = "AAAAABBBBBCCCCCDDDDDEEEEEFFFFFGGGGGHHHHHIIIIIJJJJJKKKKKLLLLLMMMMM";
        byte[] input = original.getBytes();
        
        byte[] compressed = GZipUtils.compress(input);
        byte[] uncompressed = GZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("数据应该正确恢复", input, uncompressed);
        double ratio = (double) compressed.length / input.length;
        Assert.assertTrue("重复数据应该有较好的压缩率", ratio < 0.5);
    }

    /**
     * 测试压缩 - null输入
     */
    @Test(expected = NullPointerException.class)
    public void testCompressNull() {
        GZipUtils.compress(null);
    }

    /**
     * 测试解压 - null输入
     */
    @Test(expected = NullPointerException.class)
    public void testUncompressNull() {
        GZipUtils.uncompress(null);
    }

    /**
     * 测试解压 - 损坏数据
     */
    @Test(expected = RuntimeException.class)
    public void testUncompressCorruptedData() {
        byte[] corruptedData = {1, 2, 3, 4, 5};
        GZipUtils.uncompress(corruptedData);
    }

    /**
     * 测试压缩解压 - 非常大的数据
     */
    @Test
    public void testCompressUncompressVeryLargeData() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            sb.append("Large data test line ").append(i).append("\n");
        }
        byte[] input = sb.toString().getBytes();
        
        byte[] compressed = GZipUtils.compress(input);
        byte[] uncompressed = GZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("非常大的数据压缩解压后应该恢复原数据", input, uncompressed);
    }

    /**
     * 测试压缩解压 - 零字节数据
     */
    @Test
    public void testCompressUncompressZeroBytes() {
        byte[] input = new byte[1000];
        Arrays.fill(input, (byte) 0);
        
        byte[] compressed = GZipUtils.compress(input);
        byte[] uncompressed = GZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("零字节数据压缩解压后应该恢复原数据", input, uncompressed);
    }

    /**
     * 测试压缩解压 - 全1字节数据
     */
    @Test
    public void testCompressUncompressOneBytes() {
        byte[] input = new byte[1000];
        Arrays.fill(input, (byte) 1);
        
        byte[] compressed = GZipUtils.compress(input);
        byte[] uncompressed = GZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("全1字节数据压缩解压后应该恢复原数据", input, uncompressed);
    }
}