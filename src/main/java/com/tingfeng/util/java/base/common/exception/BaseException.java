package com.tingfeng.util.java.base.common.exception;

/**
 * 用BaseException表示所有tingfeng.util工具包下的异常
 * @author huitoukest
 */
public class BaseException extends RuntimeException {
    private static final long serialVersionUID = 3593377889771697624L;

    public BaseException() {
        super();
    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(Throwable cause) {
        super(cause);
    }

    protected BaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
