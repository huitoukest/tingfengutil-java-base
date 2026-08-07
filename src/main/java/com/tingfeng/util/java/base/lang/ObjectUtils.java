package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.common.constant.ObjectType;
import com.tingfeng.util.java.base.lang.base.ConvertI;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * 一些通用的对象工具类（存量兼容门面）
 *
 * 全部方法委托至拆分后的实现类，签名与行为与拆分前完全一致；实现类为包级私有，不对外暴露：
 * ObjectCompareOps（比较族）、ObjectCopyOps（拷贝族）、ObjectTypeOps（类型判断/判空/取值族）。
 */
public class ObjectUtils {

    private ObjectUtils() {

    }

    /** 通过class获取对象的类型，同 {@link ObjectTypeOps#getObjectType(Class)} */
    public static ObjectType getObjectType(Class<?> cls) {
        return ObjectTypeOps.getObjectType(cls);
    }

    /** 通过className获取对象的类型，同 {@link ObjectTypeOps#getObjectType(String)} */
    public static ObjectType getObjectType(String className) {
        return ObjectTypeOps.getObjectType(className);
    }

    /** 通过属性来返回此属性的类型，同 {@link ObjectTypeOps#getObjectType(Field)} */
    public static ObjectType getObjectType(Field field) {
        return ObjectTypeOps.getObjectType(field);
    }

    /** 判断className对应的类型是否是基础数据类型，同 {@link ObjectTypeOps#isBaseTypeObject(String)} */
    public static boolean isBaseTypeObject(String clsName) {
        return ObjectTypeOps.isBaseTypeObject(clsName);
    }

    /** 判断field对应的类型是否是基础数据类型，同 {@link ObjectTypeOps#isBaseTypeObject(Field)} */
    public static boolean isBaseTypeObject(Field field) {
        return ObjectTypeOps.isBaseTypeObject(field);
    }

    /** 判断cls对应的类型是否是基础数据类型，同 {@link ObjectTypeOps#isBaseTypeObject(Class)} */
    public static boolean isBaseTypeObject(Class<?> cls) {
        return ObjectTypeOps.isBaseTypeObject(cls);
    }

    /** 把xml字符串反序列化为对象，同 {@link ObjectTypeOps#getObjectByXml(String)} */
    public static Object getObjectByXml(String xml) throws UnsupportedEncodingException {
        return ObjectTypeOps.getObjectByXml(xml);
    }

    /** 判断cls是否是布尔类型（boolean或Boolean），同 {@link ObjectTypeOps#isBoolean(Class)} */
    public static Boolean isBoolean(Class<?> cls) {
        return ObjectTypeOps.isBoolean(cls);
    }

    /** 如果cls是基础数据类型和包装类型则返回转换之后的值,否则返回原值，同 {@link ObjectTypeOps#getObject(Class, Object)} */
    public static <T> T getObject(Class<T> cls, Object obj) {
        return ObjectTypeOps.getObject(cls, obj);
    }

    /** 判断参数数组中是否任意一个对象为null，同 {@link ObjectTypeOps#isAnyNull(Object...)} */
    public static boolean isAnyNull(Object... objs) {
        return ObjectTypeOps.isAnyNull(objs);
    }

    /** 判断参数数组中是否所有对象都为null，同 {@link ObjectTypeOps#isAllNull(Object...)} */
    public static boolean isAllNull(Object... objs) {
        return ObjectTypeOps.isAllNull(objs);
    }

    /** 判断对象是否不为null，同 {@link ObjectTypeOps#isNotNull(Object)} */
    public static boolean isNotNull(Object obj) {
        return ObjectTypeOps.isNotNull(obj);
    }

    /** 判断参数数组中是否所有对象都不为null，同 {@link ObjectTypeOps#isAllNotNull(Object...)} */
    public static boolean isAllNotNull(Object... objs) {
        return ObjectTypeOps.isAllNotNull(objs);
    }

    /** 判断参数数组中是否任意一个对象不为null，同 {@link ObjectTypeOps#isAnyNotNull(Object...)} */
    public static boolean isAnyNotNull(Object... objs) {
        return ObjectTypeOps.isAnyNotNull(objs);
    }

    /** 判断对象是否为null，同 {@link ObjectTypeOps#isNull(Object)} */
    public static boolean isNull(Object obj) {
        return ObjectTypeOps.isNull(obj);
    }

    /** 判断参数数组中是否所有对象都为空，同 {@link ObjectTypeOps#isAllEmpty(boolean, Object...)} */
    public static boolean isAllEmpty(boolean isTrim, Object... objs) {
        return ObjectTypeOps.isAllEmpty(isTrim, objs);
    }

    /** 判断参数数组中是否任意一个对象为空，同 {@link ObjectTypeOps#isAnyEmpty(Object...)} */
    public static boolean isAnyEmpty(Object... objs) {
        return ObjectTypeOps.isAnyEmpty(objs);
    }

    /** 判断对象是否为空（null、空字符串、空集合、空数组），同 {@link ObjectTypeOps#isEmpty(Object, boolean)} */
    public static boolean isEmpty(Object obj, boolean isTrim) {
        return ObjectTypeOps.isEmpty(obj, isTrim);
    }

    /** 判断对象是否为空（null或空字符串，不做trim处理），同 {@link ObjectTypeOps#isEmpty(Object)} */
    public static boolean isEmpty(Object obj) {
        return ObjectTypeOps.isEmpty(obj);
    }

    /** 检查一个对象是否为空（容器递归），同 {@link ObjectTypeOps#isEmpty(Object, boolean, boolean)} */
    public static boolean isEmpty(Object obj, boolean recursive, final boolean isTrim) {
        return ObjectTypeOps.isEmpty(obj, recursive, isTrim);
    }

    /** 两者都是null时表示相等，传入的两个String对象是否相等，同 {@link ObjectCompareOps#equals(String, String)} */
    public static boolean equals(String str1, String str2) {
        return ObjectCompareOps.equals(str1, str2);
    }

    /** 返回values中第一个非null的值，如果所有值都为null则返回默认值，同 {@link ObjectTypeOps#getValue(Object, Object...)} */
    public static <T> T getValue(T defaultValue, Object... values) {
        return ObjectTypeOps.getValue(defaultValue, values);
    }

    /** 将source通过convert转换后返回，转换失败时返回默认值，同 {@link ObjectTypeOps#getValue(Object, Object, ConvertI)} */
    public static <T, S> T getValue(T defaultValue, S source, ConvertI<S, T> convert) {
        return ObjectTypeOps.getValue(defaultValue, source, convert);
    }

    /** 通过Callable获取值，调用异常时返回默认值，同 {@link ObjectTypeOps#getValue(Object, Callable)} */
    public static <T> T getValue(T defaultValue, Callable<T> call) {
        return ObjectTypeOps.getValue(defaultValue, call);
    }

    /** 判断对象是否不为空，同 {@link ObjectTypeOps#isNotEmpty(Object)} */
    public static boolean isNotEmpty(Object obj) {
        return ObjectTypeOps.isNotEmpty(obj);
    }

    /** 深度拷贝数组对象（浅拷贝，只拷贝引用），同 {@link ObjectCopyOps#cloneArray(Object[], Object[])} */
    public static <T> void cloneArray(T[] src, T[] dest) {
        ObjectCopyOps.cloneArray(src, dest);
    }

    /** 深度拷贝对象（通过序列化方式），同 {@link ObjectCopyOps#clone(Object)} */
    public static <T> T clone(T src) {
        return ObjectCopyOps.clone(src);
    }

    /** 判断对象是否是Java基础类型或常见类型，同 {@link ObjectTypeOps#isBaseJavaType(Object)} */
    public static boolean isBaseJavaType(Object obj) {
        return ObjectTypeOps.isBaseJavaType(obj);
    }

    /** 判断两个对象是否相等（支持null），同 {@link ObjectCompareOps#equals(Object, Object)} */
    public static boolean equals(Object a, Object b) {
        return ObjectCompareOps.equals(a, b);
    }

    /** 判断两个对象是否深度相等（支持数组深度比较），同 {@link ObjectCompareOps#deepEquals(Object, Object)} */
    public static boolean deepEquals(Object a, Object b) {
        return ObjectCompareOps.deepEquals(a, b);
    }

    /** 判断两个值是否相等（支持数值类型跨精度比较），同 {@link ObjectCompareOps#valueEquals(Object, Object)} */
    public static boolean valueEquals(Object a, Object b) {
        return ObjectCompareOps.valueEquals(a, b);
    }

    /** 判断对象是否是整数类型（Byte、Short、Integer、Long），同 {@link ObjectCompareOps#isInteger(Object)} */
    public static boolean isInteger(Object a) {
        return ObjectCompareOps.isInteger(a);
    }

    /** 判断对象是否是浮点数类型（Float、Double），同 {@link ObjectCompareOps#isFloat(Object)} */
    public static boolean isFloat(Object a) {
        return ObjectCompareOps.isFloat(a);
    }

    /** 判断两个数组的值是否相等，同 {@link ObjectCompareOps#arrayEquals(Object[], Object[])} */
    public static boolean arrayEquals(Object[] arrayA, Object[] arrayB) {
        return ObjectCompareOps.arrayEquals(arrayA, arrayB);
    }

    /** 判断两个数组的值是否相等（可启用值相等判断），同 {@link ObjectCompareOps#arrayEquals(Object[], Object[], boolean)} */
    public static boolean arrayEquals(Object[] arrayA, Object[] arrayB, boolean valueEq) {
        return ObjectCompareOps.arrayEquals(arrayA, arrayB, valueEq);
    }

    /** 尝试运行功能，异常返回null，同 {@link ObjectTypeOps#tryDo(Supplier)} */
    public static <T> T tryDo(Supplier<T> supplier) {
        return ObjectTypeOps.tryDo(supplier);
    }
}
