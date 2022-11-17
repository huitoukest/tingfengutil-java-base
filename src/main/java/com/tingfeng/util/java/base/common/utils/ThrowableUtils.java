package com.tingfeng.util.java.base.common.utils;

/**
 * 异常处理的一个工具类
 */
public class ThrowableUtils {
    /**
     * 获取异常信息
     * @param e
     * @param defaultMsg
     * @return
     */
    public static String getErrorMsg(Throwable e,String defaultMsg) {
        try {
            if(null != e){
                if(null != e.getMessage() && e.getMessage().length() > 0 && !"null".equals(e.getMessage())){
                    return e.getMessage();
                }else{
                    return getErrorMsg(e,defaultMsg);
                }
            }
        }catch (Throwable e1){
            return defaultMsg;
        }
        return defaultMsg;
    }

    /**
     * 从当前异常实例 获取特定的Throwable
     * @param throwable 当前异常实例
     * @param exception T类型的异常对应的 类
     * @param <T> 指定的异常的类型
     * @return 如果没有获取到则返回 null, 否则返回第一次获取到的指定 T类型的 实例
     */
    public static <T extends Throwable> T getThrowable(Throwable throwable,Class<T> exception){
        Throwable tmp = throwable;
        while (tmp != null){
            if(tmp.getClass().getName().equals(exception.getName())){
                return (T)tmp;
            }
            if(tmp.equals(tmp.getCause())){
                return null;
            }
            tmp = tmp.getCause();
        }
        return null;
    }
}
