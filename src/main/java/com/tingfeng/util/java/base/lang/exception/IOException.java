package com.tingfeng.util.java.base.lang.exception;

import com.tingfeng.util.java.base.lang.exception.BaseException;

/**
 * Stream 关闭时的异常
 * @author huitoukest
 *
 */
public class IOException extends BaseException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public IOException() {
    }

    public IOException(String message) {
        super(message);
    }

    public IOException(String message, Throwable cause) {
        super(message, cause);
    }

    public IOException(Throwable cause) {
        super(cause);
    }

    public IOException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
