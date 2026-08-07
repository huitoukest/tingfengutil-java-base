package com.tingfeng.util.java.base.lang;

import java.util.Objects;

/**
 * 对象比较操作实现。
 *
 * 包级私有，不对外暴露。通过 {@link ObjectUtils} 对外提供统一 API。
 */
class ObjectCompareOps {

    private ObjectCompareOps() {

    }

    /**
     * 两者都是null时表示相等，传入的两个String对象是否相等
     *
     * @param str1 String对象1
     * @param str2 String对象2
     * @return 两个对象相等：true，以外：false
     */
    static boolean equals(String str1, String str2) {
        return Objects.equals(str1, str2);
    }

    /**
     * 判断两个对象是否相等（支持null，调用Objects.equals）
     *
     * @param a 对象1
     * @param b 对象2
     * @return 两个对象相等返回true，否则返回false
     * @see Objects#equals(Object, Object)
     */
    static boolean equals(Object a, Object b) {
        return Objects.equals(a, b);
    }

    /**
     * 判断两个对象是否深度相等（支持数组深度比较，调用Objects.deepEquals）
     *
     * @param a 对象1
     * @param b 对象2
     * @return 两个对象深度相等返回true，否则返回false
     * @see Objects#deepEquals(Object, Object)
     * @see Arrays#deepEquals(Object[], Object[])
     */
    static boolean deepEquals(Object a, Object b) {
        return Objects.deepEquals(a, b);
    }

    /**
     * 判断两个值是否相等（支持数值类型跨精度比较）
     * 整数类型比较使用longValue，浮点数使用Double.compare避免精度问题
     * 其他对象转为String后比较
     *
     * @param a 值1
     * @param b 值2
     * @return 两个值相等返回true，否则返回false
     */
    static boolean valueEquals(Object a, Object b) {
        if (a != null && b != null) {
            if (isInteger(a) && isInteger(b)) {
                return ((Number) a).longValue() == ((Number) b).longValue();
            } else if (isFloat(a) && isFloat(b)) {
                return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue()) == 0;
            } else if (a.equals(b) || b.equals(a)) {
                return true;
            } else {
                String aStr = String.valueOf(a);
                String bStr = String.valueOf(b);
                return aStr.equals(bStr);
            }
        }
        return equals(a, b);
    }

    /**
     * 判断对象是否是整数类型（Byte、Short、Integer、Long）
     *
     * @param a 待检查的对象
     * @return 是否为整数类型
     */
    static boolean isInteger(Object a) {
        return a instanceof Byte || a instanceof Short || a instanceof Integer || a instanceof Long;
    }

    /**
     * 判断对象是否是浮点数类型（Float、Double）
     *
     * @param a 待检查的对象
     * @return 是否为浮点数类型
     */
    static boolean isFloat(Object a) {
        return a instanceof Float || a instanceof Double;
    }

    /**
     * 判断两个数组的值是否相等（比较内容、索引、长度）
     *
     * @param arrayA 数组1
     * @param arrayB 数组2
     * @return 两数组相等返回true，否则返回false
     */
    static boolean arrayEquals(Object[] arrayA, Object[] arrayB) {
        return arrayEquals(arrayA, arrayB, false);
    }

    /**
     * 判断两个数组的值是否相等（比较内容、索引、长度）
     *
     * @param arrayA  数组1
     * @param arrayB  数组2
     * @param valueEq 是否启用值相等判断（启用后调用valueEquals进行比较）
     * @return 两数组相等返回true，否则返回false
     * @see #valueEquals(Object, Object)
     */
    static boolean arrayEquals(Object[] arrayA, Object[] arrayB, boolean valueEq) {
        if (arrayA == null && arrayB == null) {
            return true;
        }
        if (arrayA == null || arrayB == null) {
            return false;
        }
        if (arrayA.length != arrayB.length) {
            return false;
        }
        for (int i = 0; i < arrayA.length; i++) {
            if (valueEq && !valueEquals(arrayA[i], arrayB[i])) {
                return false;
            } else if (!Objects.equals(arrayA[i], arrayB[i])) {
                return false;
            }
        }
        return true;
    }
}
