package com.tingfeng.util.java.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 异常处理工具类测试
 */
public class ThrowableUtilsTest {

    /**
     * 测试获取异常信息 - 有消息的异常
     */
    @Test
    public void testGetErrorMsgWithMessage() {
        Exception exception = new Exception("This is a test error message");
        String errorMsg = ThrowableUtils.getErrorMsg(exception, "Default message");
        
        Assert.assertEquals("应该返回异常的消息", "This is a test error message", errorMsg);
    }

    /**
     * 测试获取异常信息 - 没有消息的异常
     */
    @Test
    public void testGetErrorMsgWithoutMessage() {
        Exception exception = new Exception();
        String errorMsg = ThrowableUtils.getErrorMsg(exception, "Default message");
        
        Assert.assertEquals("应该返回默认消息", "Default message", errorMsg);
    }

    /**
     * 测试获取异常信息 - 异常为null
     */
    @Test
    public void testGetErrorMsgWithNull() {
        String errorMsg = ThrowableUtils.getErrorMsg(null, "Default message");
        
        Assert.assertEquals("异常为null时应该返回默认消息", "Default message", errorMsg);
    }

    /**
     * 测试获取异常信息 - 异常消息为空字符串
     */
    @Test
    public void testGetErrorMsgWithEmptyMessage() {
        Exception exception = new Exception("");
        String errorMsg = ThrowableUtils.getErrorMsg(exception, "Default message");
        
        Assert.assertEquals("异常消息为空时应该返回默认消息", "Default message", errorMsg);
    }

    /**
     * 测试获取异常信息 - 异常消息为"null"字符串
     */
    @Test
    public void testGetErrorMsgWithNullMessage() {
        Exception exception = new Exception("null");
        String errorMsg = ThrowableUtils.getErrorMsg(exception, "Default message");
        
        Assert.assertEquals("异常消息为'null'字符串时应该返回默认消息", "Default message", errorMsg);
    }

    /**
     * 测试获取异常信息 - 嵌套异常
     */
    @Test
    public void testGetErrorMsgWithCause() {
        IOException ioException = new IOException("IO error occurred");
        RuntimeException runtimeException = new RuntimeException("Runtime error", ioException);
        
        String errorMsg = ThrowableUtils.getErrorMsg(runtimeException, "Default message");
        
        Assert.assertEquals("应该返回当前异常的消息", "Runtime error", errorMsg);
    }

    /**
     * 测试获取特定类型的异常 - 异常链中存在目标异常
     */
    @Test
    public void testGetThrowableExists() {
        IOException ioException = new IOException("IO error");
        RuntimeException runtimeException = new RuntimeException("Runtime error", ioException);
        
        IOException foundException = ThrowableUtils.getThrowable(runtimeException, IOException.class);
        
        Assert.assertNotNull("应该找到IOException类型的异常", foundException);
        Assert.assertEquals("找到的异常应该是原始的IOException", ioException, foundException);
    }

    /**
     * 测试获取特定类型的异常 - 异常链中不存在目标异常
     */
    @Test
    public void testGetThrowableNotExists() {
        FileNotFoundException fileNotFoundException = new FileNotFoundException("File not found");
        RuntimeException runtimeException = new RuntimeException("Runtime error", fileNotFoundException);
        
        IOException foundException = ThrowableUtils.getThrowable(runtimeException, IOException.class);
        
        Assert.assertNotNull("应该找到IOException类型的异常（FileNotFoundException是子类）", foundException);
        Assert.assertTrue("找到的异常应该是FileNotFoundException", foundException instanceof FileNotFoundException);
    }

    /**
     * 测试获取特定类型的异常 - 异常链中不存在目标类型
     */
    @Test
    public void testGetThrowableDifferentType() {
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Illegal argument");
        RuntimeException runtimeException = new RuntimeException("Runtime error", illegalArgumentException);
        
        IOException foundException = ThrowableUtils.getThrowable(runtimeException, IOException.class);
        
        Assert.assertNull("不应该找到IOException类型的异常", foundException);
    }

    /**
     * 测试获取特定类型的异常 - 异常为null
     */
    @Test
    public void testGetThrowableWithNull() {
        IOException foundException = ThrowableUtils.getThrowable(null, IOException.class);
        
        Assert.assertNull("异常为null时应该返回null", foundException);
    }

    /**
     * 测试获取特定类型的异常 - 深层嵌套异常
     */
    @Test
    public void testGetThrowableDeepNesting() {
        IOException ioException = new IOException("IO error");
        IllegalStateException illegalStateException = new IllegalStateException("Illegal state", ioException);
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Illegal argument", illegalStateException);
        RuntimeException runtimeException = new RuntimeException("Runtime error", illegalArgumentException);
        
        IOException foundException = ThrowableUtils.getThrowable(runtimeException, IOException.class);
        
        Assert.assertNotNull("应该找到深层嵌套的IOException", foundException);
        Assert.assertEquals("找到的异常应该是原始的IOException", ioException, foundException);
    }

    /**
     * 测试获取特定类型的异常 - 自身就是目标类型
     */
    @Test
    public void testGetThrowableSelfIsTarget() {
        IOException ioException = new IOException("IO error");
        
        IOException foundException = ThrowableUtils.getThrowable(ioException, IOException.class);
        
        Assert.assertNotNull("应该找到自身", foundException);
        Assert.assertEquals("找到的异常应该是自身", ioException, foundException);
    }

    /**
     * 测试获取特定类型的异常 - 循环引用（异常的cause指向自己）
     */
    @Test
    public void testGetThrowableCircularReference() {
        RuntimeException runtimeException = new RuntimeException("Runtime error");
        runtimeException.initCause(runtimeException); // 创建循环引用
        
        IOException foundException = ThrowableUtils.getThrowable(runtimeException, IOException.class);
        
        Assert.assertNull("循环引用时应该返回null", foundException);
    }

    /**
     * 测试获取特定类型的异常 - 多个相同类型的异常
     */
    @Test
    public void testGetThrowableMultipleSameType() {
        IOException ioException1 = new IOException("First IO error");
        IOException ioException2 = new IOException("Second IO error", ioException1);
        RuntimeException runtimeException = new RuntimeException("Runtime error", ioException2);
        
        IOException foundException = ThrowableUtils.getThrowable(runtimeException, IOException.class);
        
        Assert.assertNotNull("应该找到IOException", foundException);
        Assert.assertEquals("应该找到第一个遇到的IOException", ioException2, foundException);
    }

    /**
     * 测试获取异常信息 - 异常消息包含特殊字符
     */
    @Test
    public void testGetErrorMsgWithSpecialCharacters() {
        String specialMessage = "Error: \n\tSpecial chars: <>&\"'";
        Exception exception = new Exception(specialMessage);
        String errorMsg = ThrowableUtils.getErrorMsg(exception, "Default message");
        
        Assert.assertEquals("应该正确处理特殊字符", specialMessage, errorMsg);
    }

    /**
     * 测试获取异常信息 - 异常消息很长
     */
    @Test
    public void testGetErrorMsgWithLongMessage() {
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("This is a long error message. ");
        }
        Exception exception = new Exception(longMessage.toString());
        String errorMsg = ThrowableUtils.getErrorMsg(exception, "Default message");
        
        Assert.assertTrue("应该返回长消息", errorMsg.length() > 1000);
        Assert.assertEquals("应该返回完整的异常消息", longMessage.toString(), errorMsg);
    }
}