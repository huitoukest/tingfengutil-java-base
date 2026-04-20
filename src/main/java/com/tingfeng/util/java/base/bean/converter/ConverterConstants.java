package com.tingfeng.util.java.base.bean.converter;

/**
 * Converter 模块常量定义
 */
public class ConverterConstants {

    private ConverterConstants() {}

    /** 默认 order 值，值越小优先级越高 */
    public static final int ORDER_DEFAULT = 0;

    /** 最高优先级（值最小） */
    public static final int ORDER_HIGHEST = Integer.MIN_VALUE;

    /** 最低优先级（值最大） */
    public static final int ORDER_LOWEST = Integer.MAX_VALUE;
}
