package com.tingfeng.util.java.base.common.constant;

/**
 * Class 类型判断工具类
 * 基于 PrimitiveType 提供高效的静态方法
 *
 * @author tingfeng
 */
public class ClassUtils {

    private ClassUtils() {}

    // ========== 基础类型判断 ==========

    /**
     * 判断是否为原始类型
     *
     * @param cls 要检查的类型
     * @return 是否为原始类型
     */
    public static boolean isPrimitive(Class<?> cls) {
        return cls != null && cls.isPrimitive();
    }

    /**
     * 判断是否为包装类型
     *
     * @param cls 要检查的类型
     * @return 是否为包装类型
     */
    public static boolean isWrapper(Class<?> cls) {
        return cls != null && PrimitiveType.fromWrapper(cls) != null;
    }

    /**
     * 判断是否为基础类型（原始类型或包装类型）
     *
     * @param cls 要检查的类型
     * @return 是否为基础类型
     */
    public static boolean isBaseType(Class<?> cls) {
        return isPrimitive(cls) || isWrapper(cls);
    }

    // ========== 类型转换 ==========

    /**
     * 原始类型转包装类型
     * int.class -> Integer.class
     * 如果不是原始类型则返回原值
     *
     * @param cls 原始类型
     * @return 包装类型
     */
    public static Class<?> toWrapper(Class<?> cls) {
        return PrimitiveType.toWrapperType(cls);
    }

    /**
     * 包装类型转原始类型
     * Integer.class -> int.class
     * 如果不是包装类型则返回原值
     *
     * @param cls 包装类型
     * @return 原始类型
     */
    public static Class<?> toPrimitive(Class<?> cls) {
        return PrimitiveType.toPrimitiveType(cls);
    }

    /**
     * 自动装箱：如果是原始类型则转为包装类型，否则返回原值
     *
     * @param cls 要转换的类型
     * @return 原始类型转包装类型，其他类型原样返回
     */
    public static Class<?> autoBox(Class<?> cls) {
        return isPrimitive(cls) ? toWrapper(cls) : cls;
    }

    // ========== 业务类型判断 ==========

    /**
     * 判断是否为数值类型
     * 包含：Byte, Short, Integer, Long, Float, Double, BigDecimal, BigInteger 等
     *
     * @param cls 要检查的类型
     * @return 是否为数值类型
     */
    public static boolean isNumber(Class<?> cls) {
        return cls != null && Number.class.isAssignableFrom(cls);
    }

    /**
     * 判断是否为字符类型
     * 包含：char, Character
     *
     * @param cls 要检查的类型
     * @return 是否为字符类型
     */
    public static boolean isChar(Class<?> cls) {
        return cls == char.class || cls == Character.class;
    }

    /**
     * 判断是否为布尔类型
     * 包含：boolean, Boolean
     *
     * @param cls 要检查的类型
     * @return 是否为布尔类型
     */
    public static boolean isBoolean(Class<?> cls) {
        return cls == boolean.class || cls == Boolean.class;
    }

    // ========== 数组支持 ==========

    /**
     * 判断是否为数组类型
     *
     * @param cls 要检查的类型
     * @return 是否为数组类型
     */
    public static boolean isArray(Class<?> cls) {
        return cls != null && cls.isArray();
    }

    /**
     * 获取数组的组件类型
     *
     * @param cls 数组类型
     * @return 组件类型，非数组返回 null
     */
    public static Class<?> getComponentType(Class<?> cls) {
        return cls != null && cls.isArray() ? cls.getComponentType() : null;
    }
}
