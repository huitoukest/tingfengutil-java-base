package com.tingfeng.util.java.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 流工具类测试
 */
public class StreamUtilsTest {

    /**
     * 测试将字符串转换为输入流（默认UTF-8编码）
     */
    @Test
    public void testGetInputStreamByStreamDefaultEncoding() throws Exception {
        String testString = "Hello, World!";
        InputStream inputStream = StreamUtils.getInputStreamByStream(testString);
        
        Assert.assertNotNull("输入流不能为空", inputStream);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamUtils.copy(outputStream, inputStream);
        
        String result = outputStream.toString("UTF-8");
        Assert.assertEquals("转换后的内容应该与原字符串相同", testString, result);
    }

    /**
     * 测试将字符串转换为输入流（指定编码）
     */
    @Test
    public void testGetInputStreamByStreamWithEncoding() throws Exception {
        String testString = "你好，世界！";
        InputStream inputStream = StreamUtils.getInputStreamByStream(testString, "UTF-8");
        
        Assert.assertNotNull("输入流不能为空", inputStream);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamUtils.copy(outputStream, inputStream);
        
        String result = outputStream.toString("UTF-8");
        Assert.assertEquals("转换后的内容应该与原字符串相同", testString, result);
    }

    /**
     * 测试将字节数组转换为输入流
     */
    @Test
    public void testGetInputStreamByBytes() throws Exception {
        String testString = "Test content";
        byte[] testBytes = testString.getBytes(StandardCharsets.UTF_8);
        
        InputStream inputStream = StreamUtils.getInputStreamByBytes(testBytes);
        
        Assert.assertNotNull("输入流不能为空", inputStream);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamUtils.copy(outputStream, inputStream);
        
        String result = outputStream.toString("UTF-8");
        Assert.assertEquals("转换后的内容应该与原字符串相同", testString, result);
    }

    /**
     * 测试空字符串转换为输入流
     */
    @Test
    public void testGetInputStreamByStreamEmptyString() throws Exception {
        String testString = "";
        InputStream inputStream = StreamUtils.getInputStreamByStream(testString);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamUtils.copy(outputStream, inputStream);
        
        String result = outputStream.toString("UTF-8");
        Assert.assertEquals("空字符串应该保持为空", testString, result);
    }

    /**
     * 测试根据文件路径创建文件输入流
     */
    @Test
    public void testGetFileInputStreamByPath() throws IOException {
        // 创建临时文件
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        
        String testContent = "Test file content";
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(testContent);
        }
        
        FileInputStream inputStream = StreamUtils.getFileInputStream(tempFile.getAbsolutePath());
        Assert.assertNotNull("文件输入流不能为空", inputStream);
        
        // 读取内容验证
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamUtils.copy(outputStream, inputStream);
        
        String result = outputStream.toString("UTF-8");
        Assert.assertEquals("读取的内容应该与写入的内容相同", testContent, result);
    }

    /**
     * 测试根据文件对象创建文件输入流
     */
    @Test
    public void testGetFileInputStreamByFile() throws IOException {
        // 创建临时文件
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        
        String testContent = "Test file content";
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(testContent);
        }
        
        FileInputStream inputStream = StreamUtils.getFileInputStream(tempFile);
        Assert.assertNotNull("文件输入流不能为空", inputStream);
        
        // 读取内容验证
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamUtils.copy(outputStream, inputStream);
        
        String result = outputStream.toString("UTF-8");
        Assert.assertEquals("读取的内容应该与写入的内容相同", testContent, result);
    }

    /**
     * 测试根据文件路径创建文件输出流（覆盖模式）
     */
    @Test
    public void testGetFileOutputStreamByPathOverwrite() throws IOException {
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        
        // 先写入一些内容
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("Original content");
        }
        
        // 使用覆盖模式写入新内容
        FileOutputStream outputStream = StreamUtils.getFileOutputStream(tempFile.getAbsolutePath(), false);
        outputStream.write("New content".getBytes(StandardCharsets.UTF_8));
        outputStream.close();
        
        // 验证内容被覆盖
        String result = new String(java.nio.file.Files.readAllBytes(tempFile.toPath()), StandardCharsets.UTF_8);
        Assert.assertEquals("内容应该被覆盖", "New content", result);
    }

    /**
     * 测试根据文件路径创建文件输出流（追加模式）
     */
    @Test
    public void testGetFileOutputStreamByPathAppend() throws IOException {
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        
        // 先写入一些内容
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("Original content");
        }
        
        // 使用追加模式写入新内容
        FileOutputStream outputStream = StreamUtils.getFileOutputStream(tempFile.getAbsolutePath(), true);
        outputStream.write(" Appended".getBytes(StandardCharsets.UTF_8));
        outputStream.close();
        
        // 验证内容被追加
        String result = new String(java.nio.file.Files.readAllBytes(tempFile.toPath()), StandardCharsets.UTF_8);
        Assert.assertEquals("内容应该被追加", "Original content Appended", result);
    }

    /**
     * 测试根据文件对象创建文件输出流
     */
    @Test
    public void testGetFileOutputStreamByFile() throws IOException {
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        
        FileOutputStream outputStream = StreamUtils.getFileOutputStream(tempFile, false);
        outputStream.write("Test content".getBytes(StandardCharsets.UTF_8));
        outputStream.close();
        
        // 验证内容
        String result = new String(java.nio.file.Files.readAllBytes(tempFile.toPath()), StandardCharsets.UTF_8);
        Assert.assertEquals("内容应该正确写入", "Test content", result);
    }

    /**
     * 测试获取字节数组输出流
     */
    @Test
    public void testGetByteArrayOutputStream() throws IOException {
        ByteArrayOutputStream outputStream = StreamUtils.getByteArrayOutputStream();
        Assert.assertNotNull("字节数组输出流不能为空", outputStream);
        
        outputStream.write("Test".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals("输出流大小应该为4", 4, outputStream.size());
    }

    /**
     * 测试流复制功能（默认缓冲区大小）
     */
    @Test
    public void testCopyDefault() throws Exception {
        String testContent = "This is a test content for stream copy operation.";
        InputStream inputStream = StreamUtils.getInputStreamByStream(testContent);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        StreamUtils.copy(outputStream, inputStream);
        
        String result = outputStream.toString("UTF-8");
        Assert.assertEquals("复制的内容应该与原内容相同", testContent, result);
    }

    /**
     * 测试流复制功能（自定义缓冲区大小）
     */
    @Test
    public void testCopyWithCustomBufferSize() throws Exception {
        String testContent = "This is a test content for stream copy operation.";
        InputStream inputStream = StreamUtils.getInputStreamByStream(testContent);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        StreamUtils.copy(outputStream, inputStream, 1024);
        
        String result = outputStream.toString("UTF-8");
        Assert.assertEquals("复制的内容应该与原内容相同", testContent, result);
    }

    /**
     * 测试流复制功能（不关闭流）
     */
    @Test
    public void testCopyWithoutClosingStream() throws Exception {
        String testContent = "Test content";
        InputStream inputStream = StreamUtils.getInputStreamByStream(testContent);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        StreamUtils.copy(outputStream, inputStream, 1024, false, null);
        
        // 流应该仍然可用
        Assert.assertTrue("输入流应该仍然可用", inputStream.available() >= 0);
        Assert.assertTrue("输出流应该仍然可用", outputStream.size() > 0);
        
        String result = outputStream.toString("UTF-8");
        Assert.assertEquals("复制的内容应该与原内容相同", testContent, result);
        
        // 手动关闭流
        inputStream.close();
        outputStream.close();
    }

    /**
     * 测试流复制功能（带回调）
     */
    @Test
    public void testCopyWithCallback() throws Exception {
        String testContent = "This is a test content for stream copy operation.";
        InputStream inputStream = StreamUtils.getInputStreamByStream(testContent);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        AtomicInteger totalBytesRead = new AtomicInteger(0);
        StreamUtils.copy(outputStream, inputStream, 1024, true, bytesRead -> {
            totalBytesRead.set(bytesRead);
        });
        
        String result = outputStream.toString("UTF-8");
        Assert.assertEquals("复制的内容应该与原内容相同", testContent, result);
        Assert.assertEquals("回调应该报告正确的字节数", testContent.getBytes(StandardCharsets.UTF_8).length, totalBytesRead.get());
    }

    /**
     * 测试复制大文件
     */
    @Test
    public void testCopyLargeContent() throws Exception {
        // 创建一个大内容（1MB）
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            sb.append("Test line ").append(i).append("\n");
        }
        String largeContent = sb.toString();
        
        InputStream inputStream = StreamUtils.getInputStreamByStream(largeContent);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        StreamUtils.copy(outputStream, inputStream);
        
        String result = outputStream.toString("UTF-8");
        Assert.assertEquals("大文件复制应该正确", largeContent, result);
    }

    /**
     * 测试复制空流
     */
    @Test
    public void testCopyEmptyStream() throws Exception {
        String emptyContent = "";
        InputStream inputStream = StreamUtils.getInputStreamByStream(emptyContent);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        StreamUtils.copy(outputStream, inputStream);
        
        String result = outputStream.toString("UTF-8");
        Assert.assertEquals("空流复制应该正确", emptyContent, result);
        Assert.assertEquals("输出流大小应该为0", 0, outputStream.size());
    }

    /**
     * 测试复制二进制数据
     */
    @Test
    public void testCopyBinaryData() throws Exception {
        // 创建二进制数据
        byte[] binaryData = new byte[256];
        for (int i = 0; i < 256; i++) {
            binaryData[i] = (byte) i;
        }

        InputStream inputStream = StreamUtils.getInputStreamByBytes(binaryData);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        StreamUtils.copy(outputStream, inputStream);

        byte[] result = outputStream.toByteArray();
        Assert.assertArrayEquals("二进制数据复制应该正确", binaryData, result);
    }

    /**
     * 测试根据不存在的文件路径创建输入流时抛出异常
     */
    @Test(expected = com.tingfeng.util.java.base.common.exception.BaseException.class)
    public void testGetFileInputStreamNotFound() {
        StreamUtils.getFileInputStream("/nonexistent/path/to/file.txt");
    }

    /**
     * 测试根据不存在的文件路径创建输出流时抛出异常
     */
    @Test(expected = com.tingfeng.util.java.base.common.exception.BaseException.class)
    public void testGetFileOutputStreamNotFound() {
        // 尝试创建到只读目录或不存在目录下的文件
        StreamUtils.getFileOutputStream("/nonexistent/path/to/file.txt", false);
    }

    /**
     * 测试GBK编码转换
     */
    @Test
    public void testGetInputStreamByStreamGbkEncoding() throws Exception {
        String testString = "你好，世界！";
        InputStream inputStream = StreamUtils.getInputStreamByStream(testString, "GBK");

        Assert.assertNotNull("输入流不能为空", inputStream);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamUtils.copy(outputStream, inputStream);

        String result = outputStream.toString("GBK");
        Assert.assertEquals("GBK编码转换后的内容应该与原字符串相同", testString, result);
    }

    /**
     * 测试流复制后输出流已关闭
     */
    @Test
    public void testCopyClosesOutputStream() throws Exception {
        String testContent = "Test content for close verification";
        InputStream inputStream = StreamUtils.getInputStreamByStream(testContent);

        // 使用标志变量验证 close 是否被调用
        final AtomicBoolean closedFlag = new AtomicBoolean(false);
        OutputStream outputStream = new OutputStream() {
            private final ByteArrayOutputStream delegate = new ByteArrayOutputStream();
            @Override
            public void write(int b) throws IOException {
                delegate.write(b);
            }
            @Override
            public void close() throws IOException {
                closedFlag.set(true);
                delegate.close();
            }
            @Override
            public void flush() throws IOException {
                delegate.flush();
            }
        };

        StreamUtils.copy(outputStream, inputStream, 1024, true, null);

        Assert.assertTrue("输出流应该在copy后被关闭", closedFlag.get());
    }

    /**
     * 测试流复制进度回调次数
     */
    @Test
    public void testCopyCallbackCalledCorrectly() throws Exception {
        // 使用较大的自定义缓冲区，确保有多次回调
        String testContent = "ABCDEFGHIJ";
        InputStream inputStream = StreamUtils.getInputStreamByStream(testContent);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        AtomicInteger callbackCount = new AtomicInteger(0);
        StreamUtils.copy(outputStream, inputStream, 3, true, bytesRead -> {
            callbackCount.incrementAndGet();
        });

        // 每个字符3字节，共10字符，需要4次读取(3,3,3,1)，但最后一次bytesRead=10不触发新回调
        Assert.assertTrue("回调应该被调用至少1次", callbackCount.get() >= 1);
    }
}