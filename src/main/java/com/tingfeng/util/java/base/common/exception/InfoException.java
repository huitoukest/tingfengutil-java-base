package com.tingfeng.util.java.base.common.exception;

/**
 * 此异常用于传递信息
 */
public class InfoException extends BaseException {
    public InfoException() {
    }

    public InfoException(String message) {
        super(message);
    }

    public InfoException(String message, Throwable cause) {
        super(message, cause);
    }

    public InfoException(Throwable cause) {
        super(cause);
    }

    public InfoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
