package com.tingfeng.util.java.base.common.utils;

/**
 * 异常处理的一个工具类
 */
public class ThrowableUtils {

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
}
