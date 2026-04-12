package com.tingfeng.util.java.base.common.exception.io;

/**
 * Stream 关闭时的异常
 * @author huitoukest
 *
 */
public class StreamCloseException extends IOException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public StreamCloseException() {
    }

    public StreamCloseException(String message) {
        super(message);
    }

    public StreamCloseException(String message, Throwable cause) {
        super(message, cause);
    }

    public StreamCloseException(Throwable cause) {
        super(cause);
    }

    public StreamCloseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
