package com.tingfeng.util.java.base.bean.copier;

/**
 * 属性访问模式枚举，描述属性值的访问方式。
 *
 * @author huitoukest
 */
public enum PropertyAccessMode {

    /**
     * 通过 getter 方法访问
     */
    GETTER_METHOD,

    /**
     * 直接字段访问
     */
    FIELD_ACCESS,

    /**
     * 通过 setter 方法访问
     */
    SETTER_METHOD
}