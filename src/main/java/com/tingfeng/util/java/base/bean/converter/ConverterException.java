package com.tingfeng.util.java.base.bean.converter;

import com.tingfeng.util.java.base.lang.exception.BaseException;

/**
 * 转换异常
 */
public class ConverterException extends BaseException {

    private static final long serialVersionUID = 1L;

    public ConverterException(String message) {
        super(message);
    }

    public ConverterException(String message, Throwable cause) {
        super(message, cause);
    }
}
