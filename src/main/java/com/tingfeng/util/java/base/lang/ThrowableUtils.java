package com.tingfeng.util.java.base.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 异常处理工具类
 * <p>
 * 提供异常信息的获取、异常链处理、栈追踪转换等功能
 * </p>
 */
public final class ThrowableUtils {

    private ThrowableUtils() {
    }

    // ==================== 消息获取 ====================

    /**
     * 获取异常消息，遍历异常链返回第一个有消息的异常消息
     *
     * @param throwable  异常对象
     * @param defaultMsg 默认消息
     * @return 异常消息，若无则返回默认消息
     */
    public static String getMessage(Throwable throwable, String defaultMsg) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && !message.isEmpty() && !"null".equals(message)) {
                return message;
            }
            // 检测循环引用
            if (current.equals(current.getCause())) {
                break;
            }
            current = current.getCause();
        }
        return defaultMsg;
    }

    /**
     * 获取异常消息，默认为空字符串
     *
     * @param throwable 异常对象
     * @return 异常消息，若无则返回空字符串
     */
    public static String getMessage(Throwable throwable) {
        return getMessage(throwable, "");
    }

    // ==================== 根因获取 ====================

    /**
     * 获取根因异常
     *
     * @param throwable 异常对象
     * @return 根因异常，若无Cause则返回原异常
     */
    public static Throwable getRootCause(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        Throwable root = throwable;
        while (root.getCause() != null && !root.equals(root.getCause())) {
            root = root.getCause();
        }
        return root;
    }

    // ==================== 异常链处理 ====================

    /**
     * 从异常链中获取指定类型的异常
     *
     * @param throwable 异常对象
     * @param clazz     目标异常类型
     * @param <T>       异常类型
     * @return 找到的第一个匹配异常，若无则返回 null
     */
    public static <T extends Throwable> T getThrowable(Throwable throwable, Class<T> clazz) {
        Throwable current = throwable;
        while (current != null) {
            if (clazz.isAssignableFrom(current.getClass())) {
                return clazz.cast(current);
            }
            if (current.equals(current.getCause())) {
                return null;
            }
            current = current.getCause();
        }
        return null;
    }

    /**
     * 获取异常链上所有异常
     *
     * @param throwable 异常对象
     * @return 异常列表（从原始异常到根因）
     */
    public static List<Throwable> getCauseChain(Throwable throwable) {
        if (throwable == null) {
            return Collections.emptyList();
        }
        List<Throwable> chain = new ArrayList<>();
        Throwable current = throwable;
        while (current != null) {
            chain.add(current);
            if (current.equals(current.getCause())) {
                break;
            }
            current = current.getCause();
        }
        return chain;
    }

    /**
     * 检查异常链中是否包含指定类型的异常
     *
     * @param throwable 异常对象
     * @param clazz     目标异常类型
     * @return true 表示包含
     */
    public static boolean isCauseOf(Throwable throwable, Class<? extends Throwable> clazz) {
        return getThrowable(throwable, clazz) != null;
    }

    // ==================== 栈追踪 ====================

    /**
     * 将栈追踪转换为字符串
     *
     * @param throwable 异常对象
     * @return 栈追踪字符串
     */
    public static String getStackTraceAsString(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * 获取简化的栈追踪信息（仅类名和方法名）
     *
     * @param throwable 异常对象
     * @return 简化栈追踪字符串
     */
    public static String getSimpleStackTrace(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        if (stackTrace == null || stackTrace.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(stackTrace.length * 64);
        for (StackTraceElement element : stackTrace) {
            sb.append("  at ")
              .append(element.getClassName())
              .append(".")
              .append(element.getMethodName())
              .append("(")
              .append(element.getFileName() != null ? element.getFileName() : "Unknown")
              .append(":")
              .append(element.getLineNumber() >= 0 ? element.getLineNumber() : "?")
              .append(")\n");
        }
        return sb.toString();
    }

    // ==================== 解包 ====================

    /**
     * 解包异常（处理 InvocationTargetException, ExecutionException 等包装异常）
     *
     * @param throwable 异常对象
     * @return 解包后的实际异常
     */
    public static Throwable unwrap(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        Throwable current = throwable;
        while (current instanceof java.lang.reflect.InvocationTargetException
                || current instanceof java.util.concurrent.ExecutionException) {
            Throwable cause = current.getCause();
            if (cause == null) {
                break;
            }
            current = cause;
        }
        return current;
    }

    // ==================== 类型判断 ====================

    /**
     * 判断是否为检查型异常
     *
     * @param throwable 异常对象
     * @return true 表示检查型异常（继承自 Exception 但非 RuntimeException）
     */
    public static boolean isCheckedException(Throwable throwable) {
        return throwable instanceof Exception
                && !(throwable instanceof RuntimeException);
    }

    /**
     * 判断是否为未检查型异常
     *
     * @param throwable 异常对象
     * @return true 表示未检查型异常
     */
    public static boolean isUncheckedException(Throwable throwable) {
        return throwable instanceof RuntimeException
                || throwable instanceof Error;
    }

    // ==================== 被压制的异常 ====================

    /**
     * 获取被压制的异常列表
     *
     * @param throwable 异常对象
     * @return 被压制的异常列表，若无则返回空列表
     */
    public static List<Throwable> getSuppressed(Throwable throwable) {
        if (throwable == null) {
            return Collections.emptyList();
        }
        Throwable[] suppressed = throwable.getSuppressed();
        if (suppressed == null || suppressed.length == 0) {
            return Collections.emptyList();
        }
        List<Throwable> list = new ArrayList<>(suppressed.length);
        Collections.addAll(list, suppressed);
        return list;
    }
}
