package com.tingfeng.util.java.base.common.exception.io;

/**
 * FileNotFoundException 的异常
 * @author huitoukest
 *
 */
public class FileNotFoundException extends IOException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public FileNotFoundException() {
    }

    public FileNotFoundException(String message) {
        super(message);
    }

    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileNotFoundException(Throwable cause) {
        super(cause);
    }

    public FileNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
