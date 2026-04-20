package com.tingfeng.util.java.base.common.constant;

/**
 * Java 原始类型枚举
 * 统一管理原始类型、包装类型、数组类型的映射关系
 * 可在类型处理、转换、反射、泛型等工具类中复用
 *
 * @author tingfeng
 */
public enum PrimitiveType {
    BOOLEAN(boolean.class, Boolean.class, boolean[].class),
    INT(int.class, Integer.class, int[].class),
    LONG(long.class, Long.class, long[].class),
    DOUBLE(double.class, Double.class, double[].class),
    FLOAT(float.class, Float.class, float[].class),
    CHAR(char.class, Character.class, char[].class),
    BYTE(byte.class, Byte.class, byte[].class),
    SHORT(short.class, Short.class, short[].class);

    /**
     * 原始类型，如 int.class
     */
    private final Class<?> primitiveType;

    /**
     * 包装类型，如 Integer.class
     */
    private final Class<?> wrapperType;

    /**
     * 数组类型，如 int[].class
     */
    private final Class<?> arrayType;

    PrimitiveType(Class<?> primitiveType, Class<?> wrapperType, Class<?> arrayType) {
        this.primitiveType = primitiveType;
        this.wrapperType = wrapperType;
        this.arrayType = arrayType;
    }

    public Class<?> getPrimitiveType() {
        return primitiveType;
    }

    public Class<?> getWrapperType() {
        return wrapperType;
    }

    public Class<?> getArrayType() {
        return arrayType;
    }

    /**
     * 获取对应的包装类型
     * int.class -> Integer.class
     */
    public Class<?> toWrapper() {
        return wrapperType;
    }

    /**
     * 获取对应的原始类型
     * Integer.class -> int.class
     */
    public Class<?> toPrimitive() {
        return primitiveType;
    }

    /**
     * 判断传入的Class是否为当前枚举的原始类型
     */
    public boolean isPrimitive(Class<?> cls) {
        return cls == primitiveType;
    }

    /**
     * 判断传入的Class是否为当前枚举的包装类型
     */
    public boolean isWrapper(Class<?> cls) {
        return cls == wrapperType;
    }

    /**
     * 判断传入的Class是否为当前枚举的数组类型
     */
    public boolean isArray(Class<?> cls) {
        return cls == arrayType;
    }

    /**
     * 从原始类型获取枚举
     *
     * @param cls 原始类型，如 int.class
     * @return 对应的 PrimitiveType，未找到返回 null
     */
    public static PrimitiveType fromPrimitive(Class<?> cls) {
        if (cls == null) {
            return null;
        }
        for (PrimitiveType pt : values()) {
            if (pt.primitiveType == cls) {
                return pt;
            }
        }
        return null;
    }

    /**
     * 从包装类型获取枚举
     *
     * @param cls 包装类型，如 Integer.class
     * @return 对应的 PrimitiveType，未找到返回 null
     */
    public static PrimitiveType fromWrapper(Class<?> cls) {
        if (cls == null) {
            return null;
        }
        for (PrimitiveType pt : values()) {
            if (pt.wrapperType == cls) {
                return pt;
            }
        }
        return null;
    }

    /**
     * 原始类型转包装类型
     * 如果不是原始类型则返回原值
     *
     * @param cls 原始类型，如 int.class
     * @return 包装类型，如 Integer.class
     */
    public static Class<?> toWrapperType(Class<?> cls) {
        PrimitiveType pt = fromPrimitive(cls);
        return pt != null ? pt.wrapperType : cls;
    }

    /**
     * 包装类型转原始类型
     * 如果不是包装类型则返回原值
     *
     * @param cls 包装类型，如 Integer.class
     * @return 原始类型，如 int.class
     */
    public static Class<?> toPrimitiveType(Class<?> cls) {
        PrimitiveType pt = fromWrapper(cls);
        return pt != null ? pt.primitiveType : cls;
    }

    /**
     * 判断是否为原始类型（包含包装类型）
     *
     * @param cls 要检查的类型
     * @return 是否为原始或包装类型
     */
    public static boolean isPrimitiveOrWrapper(Class<?> cls) {
        return fromPrimitive(cls) != null || fromWrapper(cls) != null;
    }

    /**
     * 自动装箱：如果是原始类型则转为包装类型，否则返回原值
     *
     * @param cls 要转换的类型
     * @return 原始类型转包装类型，其他类型原样返回
     */
    public static Class<?> autoBox(Class<?> cls) {
        return toWrapperType(cls);
    }
}
