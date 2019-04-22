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
     * 获取特定的Throwable
     * @param throwable
     * @param exception
     * @param <T>
     * @return
     */
    public <T extends Throwable> T getThrowable(Throwable throwable,Class<T> exception){
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
