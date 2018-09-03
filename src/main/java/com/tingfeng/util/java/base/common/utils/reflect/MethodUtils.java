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
     * @return 返回调用此方法的方法的简单方法名称
     */
    public static String getCurrentSimpleMethodName(){
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }
}
