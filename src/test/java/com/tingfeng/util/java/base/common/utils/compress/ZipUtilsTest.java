package com.tingfeng.util.java.base.common.utils.compress;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.zip.DataFormatException;

/**
 * ZIP工具类测试
 */
public class ZipUtilsTest {

    /**
     * 测试压缩 - 基本功能
     */
    @Test
    public void testCompressBasic() {
        String original = "Hello World! This is a test string for compression.Hello World! This is a test string for compression.Hello World! This is a test string for compression.Hello World! This is a test string for compression.Hello World! This is a test string for compression.Hello World! This is a test string for compression.Hello World! This is a test string for compression.Hello World! This is a test string for compression.";
        byte[] input = original.getBytes();
        
        byte[] compressed = ZipUtils.compress(input);
        Assert.assertNotNull("压缩结果不能为空", compressed);
        Assert.assertTrue("压缩后的数据应该比原数据小", compressed.length < input.length);
    }

    /**
     * 测试压缩 - 默认级别
     */
    @Test
    public void testCompressDefaultLevel() {
        String original = "Test content for compression with default level.";
        byte[] input = original.getBytes();
        
        byte[] compressed1 = ZipUtils.compress(input);
        byte[] compressed2 = ZipUtils.compress(input, ZipUtils.DEFAULT_LEVEL);
        
        Assert.assertArrayEquals("默认级别应该与指定默认级别相同", compressed1, compressed2);
    }

    /**
     * 测试压缩 - 不同级别
     */
    @Test
    public void testCompressDifferentLevels() {
        String original = "Test content for compression with different levels. " +
                        "This is a longer string to show the difference between compression levels.";
        byte[] input = original.getBytes();
        
        byte[] compressed1 = ZipUtils.compress(input, 1);
        byte[] compressed3 = ZipUtils.compress(input, 3);
        byte[] compressed9 = ZipUtils.compress(input, 9);
        
        Assert.assertNotNull("级别1压缩结果不能为空", compressed1);
        Assert.assertNotNull("级别3压缩结果不能为空", compressed3);
        Assert.assertNotNull("级别9压缩结果不能为空", compressed9);
        
        // 级别越高，压缩率通常越好
        Assert.assertTrue("级别9的压缩率应该比级别1好", compressed9.length <= compressed1.length);
    }

    /**
     * 测试压缩 - 级别0（不压缩）
     */
    @Test
    public void testCompressLevel0() {
        String original = "Test content";
        byte[] input = original.getBytes();
        
        byte[] compressed = ZipUtils.compress(input, 0);
        Assert.assertNotNull("压缩结果不能为空", compressed);
    }

    /**
     * 测试压缩 - 空数组
     */
    @Test
    public void testCompressEmpty() {
        byte[] input = new byte[0];
        
        byte[] compressed = ZipUtils.compress(input);
        Assert.assertNotNull("压缩结果不能为空", compressed);
    }

    /**
     * 测试压缩 - 单字节
     */
    @Test
    public void testCompressSingleByte() {
        byte[] input = {65};
        
        byte[] compressed = ZipUtils.compress(input);
        Assert.assertNotNull("压缩结果不能为空", compressed);
    }

    /**
     * 测试压缩 - 重复数据
     */
    @Test
    public void testCompressRepeatedData() {
        byte[] input = new byte[1000];
        Arrays.fill(input, (byte) 65);
        
        byte[] compressed = ZipUtils.compress(input);
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
        
        byte[] compressed = ZipUtils.compress(input);
        Assert.assertNotNull("压缩结果不能为空", compressed);
        // 随机数据压缩效果通常不好
        Assert.assertTrue("随机数据压缩后可能比原数据大或差不多", compressed.length <= input.length * 2);
    }

    /**
     * 测试解压 - 基本功能
     */
    @Test
    public void testUncompressBasic() throws DataFormatException {
        String original = "Hello World! This is a test string for compression.";
        byte[] input = original.getBytes();
        
        byte[] compressed = ZipUtils.compress(input);
        byte[] uncompressed = ZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("解压后的数据应该与原数据相同", input, uncompressed);
    }

    /**
     * 测试解压 - 空数组
     */
    @Test
    public void testUncompressEmpty() throws DataFormatException {
        byte[] input = new byte[0];
        
        byte[] compressed = ZipUtils.compress(input);
        byte[] uncompressed = ZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("解压后的空数组应该与原数组相同", input, uncompressed);
    }

    /**
     * 测试解压 - 单字节
     */
    @Test
    public void testUncompressSingleByte() throws DataFormatException {
        byte[] input = {65};
        
        byte[] compressed = ZipUtils.compress(input);
        byte[] uncompressed = ZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("解压后的单字节数组应该与原数组相同", input, uncompressed);
    }

    /**
     * 测试解压 - 大数据
     */
    @Test
    public void testUncompressLargeData() throws DataFormatException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("This is line ").append(i).append(" of test data.\n");
        }
        byte[] input = sb.toString().getBytes();
        
        byte[] compressed = ZipUtils.compress(input);
        byte[] uncompressed = ZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("解压后的大数据应该与原数据相同", input, uncompressed);
    }

    /**
     * 测试解压 - 重复数据
     */
    @Test
    public void testUncompressRepeatedData() throws DataFormatException {
        byte[] input = new byte[1000];
        Arrays.fill(input, (byte) 65);
        
        byte[] compressed = ZipUtils.compress(input);
        byte[] uncompressed = ZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("解压后的重复数据应该与原数据相同", input, uncompressed);
    }

    /**
     * 测试压缩解压 - 完整流程
     */
    @Test
    public void testCompressUncompressCycle() throws DataFormatException {
        String original = "This is a complete test of the compression and decompression cycle. " +
                        "We will compress the data, then decompress it, and verify that the result " +
                        "matches the original data exactly.";
        byte[] input = original.getBytes();
        
        byte[] compressed = ZipUtils.compress(input);
        byte[] uncompressed = ZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("压缩解压循环后应该恢复原数据", input, uncompressed);
        String result = new String(uncompressed);
        Assert.assertEquals("字符串内容应该完全相同", original, result);
    }

    /**
     * 测试压缩解压 - 多次循环
     */
    @Test
    public void testMultipleCompressUncompressCycles() throws DataFormatException {
        String original = "Test data for multiple compression cycles.";
        byte[] input = original.getBytes();
        
        byte[] data = input;
        for (int i = 0; i < 5; i++) {
            byte[] compressed = ZipUtils.compress(data);
            data = ZipUtils.uncompress(compressed);
        }
        
        Assert.assertArrayEquals("多次压缩解压循环后应该恢复原数据", input, data);
    }

    /**
     * 测试压缩解压 - 二进制数据
     */
    @Test
    public void testCompressUncompressBinaryData() throws DataFormatException {
        byte[] input = new byte[256];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) i;
        }
        
        byte[] compressed = ZipUtils.compress(input);
        byte[] uncompressed = ZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("二进制数据压缩解压后应该恢复原数据", input, uncompressed);
    }

    /**
     * 测试压缩解压 - 中文数据
     */
    @Test
    public void testCompressUncompressChineseData() throws DataFormatException {
        String original = "这是中文测试数据，包含各种汉字和标点符号。";
        byte[] input = original.getBytes();
        
        byte[] compressed = ZipUtils.compress(input);
        byte[] uncompressed = ZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("中文数据压缩解压后应该恢复原数据", input, uncompressed);
        String result = new String(uncompressed);
        Assert.assertEquals("中文内容应该完全相同", original, result);
    }

    /**
     * 测试解压 - 损坏数据
     */
    @Test(expected = DataFormatException.class)
    public void testUncompressCorruptedData() throws DataFormatException {
        byte[] corruptedData = {1, 2, 3, 4, 5};
        ZipUtils.uncompress(corruptedData);
    }

    /**
     * 测试压缩解压 - 性能测试
     */
    @Test
    public void testCompressUncompressPerformance() throws DataFormatException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("Performance test line ").append(i).append("\n");
        }
        byte[] input = sb.toString().getBytes();
        
        long startTime = System.currentTimeMillis();
        byte[] compressed = ZipUtils.compress(input);
        long compressTime = System.currentTimeMillis() - startTime;
        
        startTime = System.currentTimeMillis();
        byte[] uncompressed = ZipUtils.uncompress(compressed);
        long uncompressTime = System.currentTimeMillis() - startTime;
        
        Assert.assertArrayEquals("数据应该正确恢复", input, uncompressed);
        Assert.assertTrue("压缩时间应该在合理范围内", compressTime < 1000);
        Assert.assertTrue("解压时间应该在合理范围内", uncompressTime < 1000);
    }

    /**
     * 测试压缩解压 - 压缩率测试
     */
    @Test
    public void testCompressionRatio() throws DataFormatException {
        String original = "AAAAABBBBBCCCCCDDDDDEEEEEFFFFFGGGGGHHHHHIIIIIJJJJJKKKKKLLLLLMMMMMAAAAABBBBBCCCCCDDDDDEEEEEFFFFFGGGGGHHHHHIIIIIJJJJJKKKKKLLLLLMMMMMAAAAABBBBBCCCCCDDDDDEEEEEFFFFFGGGGGHHHHHIIIIIJJJJJKKKKKLLLLLMMMMM";
        byte[] input = original.getBytes();
        
        byte[] compressed = ZipUtils.compress(input);
        byte[] uncompressed = ZipUtils.uncompress(compressed);
        
        Assert.assertArrayEquals("数据应该正确恢复", input, uncompressed);
        double ratio = (double) compressed.length / input.length;
        Assert.assertTrue("重复数据应该有较好的压缩率", ratio < 0.5);
    }

    /**
     * 测试压缩 - 所有级别
     */
    @Test
    public void testCompressAllLevels() throws DataFormatException {
        String original = "Test data for all compression levels.Hello World! This is a test string for compression.Hello World! This is a test string for compression.Hello World! This is a test string for compression.Hello World! This is a test string for compression.Hello World! This is a test string for compression.";
        byte[] input = original.getBytes();
        
        for (int level = 0; level <= 9; level++) {
            byte[] compressed = ZipUtils.compress(input, level);
            byte[] uncompressed = ZipUtils.uncompress(compressed);
            Assert.assertArrayEquals("级别" + level + "的压缩解压应该正确", input, uncompressed);
        }
    }
}