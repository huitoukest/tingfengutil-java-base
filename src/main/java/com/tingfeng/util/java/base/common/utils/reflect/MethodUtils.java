package com.tingfeng.util.java.base.common.utils.reflect;

import com.tingfeng.util.java.base.common.constant.Constants;

/**
 * 获取当前调用的方法的名称
 */
public class MethodUtils {

    /**
     *
     * @return 返回调用此方法的方法的完整方法名称
     */
    public static String getCurrentMethodName(){
        StackTraceElement stackTraceElement = Thread.currentThread() .getStackTrace()[2];
        return stackTraceElement.getClassName() + Constants.Symbol.dot + stackTraceElement.getMethodName();

    }
    
    /**
     * 
     * @param depth 调用栈深度，默认为2（直接调用者）
     * @return 返回指定深度的方法的完整方法名称
     */
    public static String getCurrentMethodName(int depth){
        // 确保深度至少为2，因为0是getStackTrace()方法，1是当前方法
        int realDepth = Math.max(2, depth);
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (realDepth < stackTrace.length) {
            StackTraceElement stackTraceElement = stackTrace[realDepth];
            return stackTraceElement.getClassName() + Constants.Symbol.dot + stackTraceElement.getMethodName();
        }
        StackTraceElement stackTraceElement = stackTrace[2];
        return stackTraceElement.getClassName() + Constants.Symbol.dot + stackTraceElement.getMethodName();
    }

    /**
     *
     * @return 返回调用此方法的方法的简单方法名称
     */
    public static String getCurrentSimpleMethodName(){
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }
    
    /**
     * 
     * @param depth 调用栈深度，默认为2（直接调用者）
     * @return 返回指定深度的方法的简单方法名称
     */
    public static String getCurrentSimpleMethodName(int depth){
        // 确保深度至少为2，因为0是getStackTrace()方法，1是当前方法
        int realDepth = Math.max(2, depth);
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (realDepth < stackTrace.length) {
            return stackTrace[realDepth].getMethodName();
        }
        return stackTrace[2].getMethodName();
    }
}