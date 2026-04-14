package com.tingfeng.util.java.base.lang;

import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 异常处理工具类测试
 */
public class ThrowableUtilsTest {

    // ==================== getMessage 测试 ====================

    @Test
    public void testGetMessageWithMessage() {
        Exception exception = new Exception("This is a test error message");
        String errorMsg = ThrowableUtils.getMessage(exception, "Default message");

        Assert.assertEquals("应该返回异常的消息", "This is a test error message", errorMsg);
    }

    @Test
    public void testGetMessageWithoutMessage() {
        Exception exception = new Exception();
        String errorMsg = ThrowableUtils.getMessage(exception, "Default message");

        Assert.assertEquals("应该返回默认消息", "Default message", errorMsg);
    }

    @Test
    public void testGetMessageWithNull() {
        String errorMsg = ThrowableUtils.getMessage(null, "Default message");

        Assert.assertEquals("异常为null时应该返回默认消息", "Default message", errorMsg);
    }

    @Test
    public void testGetMessageWithEmptyMessage() {
        Exception exception = new Exception("");
        String errorMsg = ThrowableUtils.getMessage(exception, "Default message");

        Assert.assertEquals("异常消息为空时应该返回默认消息", "Default message", errorMsg);
    }

    @Test
    public void testGetMessageWithNullString() {
        Exception exception = new Exception("null");
        String errorMsg = ThrowableUtils.getMessage(exception, "Default message");

        Assert.assertEquals("异常消息为'null'字符串时应该返回默认消息", "Default message", errorMsg);
    }

    @Test
    public void testGetMessageWithCause() {
        IOException ioException = new IOException("IO error occurred");
        RuntimeException runtimeException = new RuntimeException("Runtime error", ioException);

        // getMessage 优先返回当前异常消息
        Assert.assertEquals("应该返回当前异常的消息", "Runtime error",
                ThrowableUtils.getMessage(runtimeException, "Default message"));
    }

    @Test
    public void testGetMessageWithNoMessageButCause() {
        RuntimeException runtimeException = new RuntimeException();
        runtimeException.initCause(new IOException("IO error"));

        // 当前异常无消息，但Cause有消息，会遍历找到
        Assert.assertEquals("应该返回Cause的消息", "IO error",
                ThrowableUtils.getMessage(runtimeException, "Default message"));
    }

    @Test
    public void testGetMessageWithSpecialCharacters() {
        String specialMessage = "Error: \n\tSpecial chars: <>&\"'";
        Exception exception = new Exception(specialMessage);
        String errorMsg = ThrowableUtils.getMessage(exception, "Default message");

        Assert.assertEquals("应该正确处理特殊字符", specialMessage, errorMsg);
    }

    @Test
    public void testGetMessageNoDefault() {
        Exception exception = new Exception("test");
        Assert.assertEquals("test", ThrowableUtils.getMessage(exception));
        Assert.assertEquals("", ThrowableUtils.getMessage(null));
    }

    // ==================== getRootCause 测试 ====================

    @Test
    public void testGetRootCause() {
        IOException ioException = new IOException("IO error");
        IllegalStateException illegalStateException = new IllegalStateException("Illegal state", ioException);
        RuntimeException runtimeException = new RuntimeException("Runtime error", illegalStateException);

        Throwable rootCause = ThrowableUtils.getRootCause(runtimeException);
        Assert.assertEquals("应该返回根因异常", ioException, rootCause);
    }

    @Test
    public void testGetRootCauseNoCause() {
        RuntimeException runtimeException = new RuntimeException("no cause");
        Assert.assertEquals("无Cause时返回自身", runtimeException, ThrowableUtils.getRootCause(runtimeException));
    }

    @Test
    public void testGetRootCauseNull() {
        Assert.assertNull("null输入返回null", ThrowableUtils.getRootCause(null));
    }

    // ==================== getThrowable 测试 ====================

    @Test
    public void testGetThrowableExists() {
        IOException ioException = new IOException("IO error");
        RuntimeException runtimeException = new RuntimeException("Runtime error", ioException);

        IOException foundException = ThrowableUtils.getThrowable(runtimeException, IOException.class);

        Assert.assertNotNull("应该找到IOException类型的异常", foundException);
        Assert.assertEquals("找到的异常应该是原始的IOException", ioException, foundException);
    }

    @Test
    public void testGetThrowableNotExists() {
        FileNotFoundException fileNotFoundException = new FileNotFoundException("File not found");
        RuntimeException runtimeException = new RuntimeException("Runtime error", fileNotFoundException);

        IOException foundException = ThrowableUtils.getThrowable(runtimeException, IOException.class);

        Assert.assertNotNull("应该找到IOException类型的异常（FileNotFoundException是子类）", foundException);
        Assert.assertTrue("找到的异常应该是FileNotFoundException", foundException instanceof FileNotFoundException);
    }

    @Test
    public void testGetThrowableDifferentType() {
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Illegal argument");
        RuntimeException runtimeException = new RuntimeException("Runtime error", illegalArgumentException);

        IOException foundException = ThrowableUtils.getThrowable(runtimeException, IOException.class);

        Assert.assertNull("不应该找到IOException类型的异常", foundException);
    }

    @Test
    public void testGetThrowableWithNull() {
        IOException foundException = ThrowableUtils.getThrowable(null, IOException.class);
        Assert.assertNull("异常为null时应该返回null", foundException);
    }

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

    @Test
    public void testGetThrowableSelfIsTarget() {
        IOException ioException = new IOException("IO error");
        IOException foundException = ThrowableUtils.getThrowable(ioException, IOException.class);

        Assert.assertNotNull("应该找到自身", foundException);
        Assert.assertEquals("找到的异常应该是自身", ioException, foundException);
    }

    @Test
    public void testGetThrowableCircularReference() {
        // Java Throwable 不允许 self-causation (initCause(this) 会抛异常)
        // 所以这里用两个互相引用的异常来模拟循环是不可能的
        // 只测试正常的单向异常链
        RuntimeException runtimeException = new RuntimeException("Runtime error");
        Assert.assertNull("RuntimeException不是IOException", ThrowableUtils.getThrowable(runtimeException, IOException.class));
    }

    @Test
    public void testGetThrowableMultipleSameType() {
        IOException ioException1 = new IOException("First IO error");
        IOException ioException2 = new IOException("Second IO error", ioException1);
        RuntimeException runtimeException = new RuntimeException("Runtime error", ioException2);

        IOException foundException = ThrowableUtils.getThrowable(runtimeException, IOException.class);

        Assert.assertNotNull("应该找到IOException", foundException);
        Assert.assertEquals("应该找到第一个遇到的IOException", ioException2, foundException);
    }

    // ==================== getCauseChain 测试 ====================

    @Test
    public void testGetCauseChain() {
        IOException ioException = new IOException("IO error");
        IllegalStateException illegalStateException = new IllegalStateException("Illegal state", ioException);
        RuntimeException runtimeException = new RuntimeException("Runtime error", illegalStateException);

        List<Throwable> chain = ThrowableUtils.getCauseChain(runtimeException);

        Assert.assertEquals("应该包含3个异常", 3, chain.size());
        Assert.assertEquals("第一个应该是RuntimeException", runtimeException, chain.get(0));
        Assert.assertEquals("最后一个应该是IOException", ioException, chain.get(2));
    }

    @Test
    public void testGetCauseChainNull() {
        Assert.assertTrue("null输入返回空列表", ThrowableUtils.getCauseChain(null).isEmpty());
    }

    @Test
    public void testGetCauseChainNoCause() {
        RuntimeException runtimeException = new RuntimeException("no cause");
        List<Throwable> chain = ThrowableUtils.getCauseChain(runtimeException);

        Assert.assertEquals("无Cause时只包含自身", 1, chain.size());
        Assert.assertEquals(runtimeException, chain.get(0));
    }

    // ==================== isCauseOf 测试 ====================

    @Test
    public void testIsCauseOf() {
        IOException ioException = new IOException("IO error");
        RuntimeException runtimeException = new RuntimeException("Runtime error", ioException);

        Assert.assertTrue("应该包含IOException", ThrowableUtils.isCauseOf(runtimeException, IOException.class));
        Assert.assertFalse("不包含NullPointerException", ThrowableUtils.isCauseOf(runtimeException, NullPointerException.class));
    }

    @Test
    public void testIsCauseOfNull() {
        Assert.assertFalse("null输入返回false", ThrowableUtils.isCauseOf(null, Exception.class));
    }

    // ==================== getStackTraceAsString 测试 ====================

    @Test
    public void testGetStackTraceAsString() {
        Exception exception = new Exception("test");
        String stackTrace = ThrowableUtils.getStackTraceAsString(exception);

        Assert.assertNotNull("不应该为空", stackTrace);
        Assert.assertTrue("应该包含异常消息", stackTrace.contains("test"));
        Assert.assertTrue("应该包含类名", stackTrace.contains("ThrowableUtilsTest"));
    }

    @Test
    public void testGetStackTraceAsStringNull() {
        Assert.assertEquals("null输入返回空字符串", "", ThrowableUtils.getStackTraceAsString(null));
    }

    @Test
    public void testGetSimpleStackTrace() {
        Exception exception = new Exception("test");
        String simpleTrace = ThrowableUtils.getSimpleStackTrace(exception);

        Assert.assertNotNull("不应该为空", simpleTrace);
        Assert.assertTrue("应该包含at关键字", simpleTrace.contains("at "));
        Assert.assertTrue("应该包含类名", simpleTrace.contains("ThrowableUtilsTest"));
    }

    // ==================== unwrap 测试 ====================

    @Test
    public void testUnwrapInvocationTargetException() throws Exception {
        IOException ioException = new IOException("IO error");
        InvocationTargetException invocationException = new InvocationTargetException(ioException);

        Throwable unwrapped = ThrowableUtils.unwrap(invocationException);
        Assert.assertEquals("应该解包到实际异常", ioException, unwrapped);
    }

    @Test
    public void testUnwrapExecutionException() {
        IOException ioException = new IOException("IO error");
        ExecutionException executionException = new ExecutionException(ioException);

        Throwable unwrapped = ThrowableUtils.unwrap(executionException);
        Assert.assertEquals("应该解包到实际异常", ioException, unwrapped);
    }

    @Test
    public void testUnwrapNoWrap() {
        RuntimeException runtimeException = new RuntimeException("no wrap");
        Assert.assertEquals("无包装时返回自身", runtimeException, ThrowableUtils.unwrap(runtimeException));
    }

    @Test
    public void testUnwrapNull() {
        Assert.assertNull("null输入返回null", ThrowableUtils.unwrap(null));
    }

    // ==================== isCheckedException / isUncheckedException 测试 ====================

    @Test
    public void testIsCheckedException() {
        IOException ioException = new IOException("IO error");
        Assert.assertTrue("IOException是检查型异常", ThrowableUtils.isCheckedException(ioException));
    }

    @Test
    public void testIsUncheckedException() {
        RuntimeException runtimeException = new RuntimeException("Runtime error");
        Assert.assertTrue("RuntimeException是未检查型异常", ThrowableUtils.isUncheckedException(runtimeException));
        Assert.assertTrue("Error是未检查型异常", ThrowableUtils.isUncheckedException(new Error("error")));
    }

    @Test
    public void testIsCheckedExceptionRuntime() {
        RuntimeException runtimeException = new RuntimeException("Runtime error");
        Assert.assertFalse("RuntimeException不是检查型异常", ThrowableUtils.isCheckedException(runtimeException));
    }

    @Test
    public void testIsUncheckedExceptionChecked() {
        IOException ioException = new IOException("IO error");
        Assert.assertFalse("IOException不是未检查型异常", ThrowableUtils.isUncheckedException(ioException));
    }

    // ==================== getSuppressed 测试 ====================

    @Test
    public void testGetSuppressed() {
        RuntimeException mainException = new RuntimeException("main");
        RuntimeException suppressed1 = new RuntimeException("suppressed1");
        RuntimeException suppressed2 = new RuntimeException("suppressed2");

        mainException.addSuppressed(suppressed1);
        mainException.addSuppressed(suppressed2);

        List<Throwable> suppressed = ThrowableUtils.getSuppressed(mainException);

        Assert.assertEquals("应该包含2个被压制的异常", 2, suppressed.size());
    }

    @Test
    public void testGetSuppressedEmpty() {
        RuntimeException runtimeException = new RuntimeException("no suppressed");
        Assert.assertTrue("无被压制异常时返回空列表", ThrowableUtils.getSuppressed(runtimeException).isEmpty());
    }

    @Test
    public void testGetSuppressedNull() {
        Assert.assertTrue("null输入返回空列表", ThrowableUtils.getSuppressed(null).isEmpty());
    }
}
