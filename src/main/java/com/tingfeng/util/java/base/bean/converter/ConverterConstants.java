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

    // ======== 冒泡注册相关常量 ========

    /** 默认冒泡层数（向上冒泡 1 层：父类 + 直接接口） */
    public static final int BUBBLE_DEFAULT = 1;

    /** 无限冒泡标记（冒泡到 Object 为止） */
    public static final int BUBBLE_UNLIMITED = -1;

    /** 不冒泡 */
    public static final int BUBBLE_NONE = 0;
}
