package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.common.constant.ObjectType;
import com.tingfeng.util.java.base.common.constant.ObjectTypeString;
import com.tingfeng.util.java.base.validate.JudgeEmptyHelper;
import com.tingfeng.util.java.base.lang.base.ConvertI;
import com.tingfeng.util.java.base.datetime.DateUtils;
import com.tingfeng.util.java.base.lang.StringUtils;

import java.beans.XMLDecoder;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * 一些通用的对象工具类
 */
public class ObjectUtils {

    private ObjectUtils() {

    }
    private static final Map<String, ObjectType> OBJECT_TYPE_MAP = new HashMap<>(18);

    static {
        OBJECT_TYPE_MAP.put(ObjectTypeString.clsNameBoolean, ObjectType.Boolean);
        OBJECT_TYPE_MAP.put(ObjectTypeString.clsNameBaseBoolean, ObjectType.Boolean);
        OBJECT_TYPE_MAP.put(ObjectTypeString.clsNameDate, ObjectType.Date);
        OBJECT_TYPE_MAP.put(ObjectTypeString.clsNameFloat, ObjectType.Float);
        OBJECT_TYPE_MAP.put(ObjectTypeString.clsNameBaseFloat, ObjectType.Float);
        OBJECT_TYPE_MAP.put(ObjectTypeString.clsNameDouble, ObjectType.Double);
        OBJECT_TYPE_MAP.put(ObjectTypeString.clsNameBaseDouble, ObjectType.Double);
        OBJECT_TYPE_MAP.put(ObjectTypeString.clsNameLong, ObjectType.Long);
        OBJECT_TYPE_MAP.put(ObjectTypeString.clsNameBaseLong, ObjectType.Long);
        OBJECT_TYPE_MAP.put(ObjectTypeString.clsNameInteger, ObjectType.Integer);
        OBJECT_TYPE_MAP.put(ObjectTypeString.clsNameBaseInt, ObjectType.Integer);
        OBJECT_TYPE_MAP.put(ObjectTypeString.clsNameString, ObjectType.String);
        OBJECT_TYPE_MAP.put(ObjectTypeString.clsNameShort, ObjectType.Short);
        OBJECT_TYPE_MAP.put(ObjectTypeString.clsNameBaseShort, ObjectType.Short);
        OBJECT_TYPE_MAP.put(ObjectTypeString.clsNameByte, ObjectType.Byte);
        OBJECT_TYPE_MAP.put(ObjectTypeString.clsNameBaseByte, ObjectType.Byte);
    }

    /**
     * 通过class获取对象的类型
     *
     * @param cls class对象
     * @return 对象类型枚举
     */
    public static ObjectType getObjectType(Class<?> cls) {
        return getObjectType(cls.getName());
    }

    /**
     * 通过className获取对象的类型
     *
     * @param className 类的全限定名
     * @return 对象类型枚举，未知类型返回ObjectType.Other
     */
    public static ObjectType getObjectType(String className) {
        return OBJECT_TYPE_MAP.getOrDefault(className, ObjectType.Other);
    }

    /**
     * 通过属性来返回此属性的类型
     *
     * @param field 反射字段
     * @return 对象类型枚举
     */
    public static ObjectType getObjectType(Field field) {
        return getObjectType(field.getType().getCanonicalName());
    }

    /**
     * 判断className对应的类型是否是基础数据类型（包装类型或基本类型）
     *
     * @param clsName 类的全限定名
     * @return 是否为基础数据类型
     */
    public static boolean isBaseTypeObject(String clsName) {
        return getObjectType(clsName) != ObjectType.Other;
    }

    /**
     * 判断field对应的类型是否是基础数据类型（包装类型或基本类型）
     *
     * @param field 反射字段
     * @return 是否为基础数据类型
     */
    public static boolean isBaseTypeObject(Field field) {
        return isBaseTypeObject(field.getType().getCanonicalName());
    }

    /**
     * 判断cls对应的类型是否是基础数据类型（包装类型或基本类型）
     *
     * @param cls class对象
     * @return 是否为基础数据类型
     */
    public static boolean isBaseTypeObject(Class<?> cls) {
        return isBaseTypeObject(cls.getName());
    }

    /**
     * 把xml字符串反序列化为对象
     *
     * @param xml xml格式字符串
     * @return 反序列化后的对象
     * @throws UnsupportedEncodingException UTF8编码异常
     */
    public static Object getObjectByXml(String xml) throws UnsupportedEncodingException {
        XMLDecoder decoder = null;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes("UTF8"));
            decoder = new XMLDecoder(new BufferedInputStream(in));
            return decoder.readObject();
        } finally {
            if (null != decoder) {
                decoder.close();
            }
        }
    }

    /**
     * 判断cls是否是布尔类型（boolean或Boolean）
     *
     * @param cls class对象
     * @return 是否为布尔类型
     */
    public static Boolean isBoolean(Class<?> cls) {
        return Boolean.valueOf(cls != null && (Boolean.TYPE.isAssignableFrom(cls) || Boolean.class.isAssignableFrom(cls)));
    }

    /**
     * 如果cls是基础数据类型和包装类型则返回转换之后的值,否则返回原值
     * 这里如果obj是T类型，则直接返回，否则将之转为String类型后自动如果是基础数据类型
     * 则转为T类型，否则返回null
     *
     * @param cls 需要转换的目标类型的class文件
     * @param obj 传入的数据
     * @param <T> 需要转换的目标类型
     * @return T对象
     */
    public static <T> T getObject(Class<T> cls, Object obj) {
        if (cls == null) {
            return (T) obj;
        }
        if (obj == null) {
            return null;
        }
        if (cls.getName().equals(obj.getClass().getName())) {
            return (T) obj;
        }
        String value = obj.toString();
        switch (cls.getName()) {
            case ObjectTypeString.clsNameBoolean:
                return (T) StringUtils.getBoolean(value);
            case ObjectTypeString.clsNameByte:
                return (T) StringUtils.getByte(value);
            case ObjectTypeString.clsNameDate:
                return (T) DateUtils.getDate(value);
            case ObjectTypeString.clsNameLong:
                return (T) StringUtils.getLong(value);
            case ObjectTypeString.clsNameInteger:
                return (T) StringUtils.getInteger(value);
            case ObjectTypeString.clsNameFloat:
                return (T) StringUtils.getFloat(value);
            case ObjectTypeString.clsNameDouble:
                return (T) StringUtils.getDouble(value);
            case ObjectTypeString.clsNameShort:
                return (T) StringUtils.getShort(value);
            case ObjectTypeString.clsNameString:
                return (T) value;
            case ObjectTypeString.clsNameBaseBoolean:
                return (T) StringUtils.getBoolean(value, false);
            case ObjectTypeString.clsNameBaseByte:
                return (T) StringUtils.getByte(value, (byte) 0);
            case ObjectTypeString.clsNameBaseDouble:
                return (T) StringUtils.getDouble(value, 0d);
            case ObjectTypeString.clsNameBaseFloat:
                return (T) StringUtils.getFloat(value, 0f);
            case ObjectTypeString.clsNameBaseInt:
                return (T) StringUtils.getInteger(value, 0);
            case ObjectTypeString.clsNameBaseLong:
                return (T) StringUtils.getLong(value, 0L);
            case ObjectTypeString.clsNameBaseShort:
                return (T) StringUtils.getShort(value, (short) 0);
            default:
                break;
        }
        return (T) obj;
    }

    /**
     * 判断参数数组中是否任意一个对象为null
     *
     * @param objs 待检查的对象数组
     * @return 如果有任意一个对象为null则返回true，否则返回false
     */
    public static boolean isAnyNull(Object... objs) {
        if (isNull(objs)) {
            return true;
        }
        for (Object obj : objs) {
            if (isNull(obj)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断参数数组中是否所有对象都为null
     *
     * @param objs 待检查的对象数组
     * @return 如果所有对象都为null则返回true，否则返回false
     */
    public static boolean isAllNull(Object... objs) {
        if (!isNull(objs)) {
            for (Object obj : objs) {
                if (!isNull(obj)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 判断对象是否不为null
     *
     * @param obj 待检查的对象
     * @return 对象不为null返回true，否则返回false
     */
    public static boolean isNotNull(Object obj) {
        return !isNull(obj);
    }

    /**
     * 判断参数数组中是否所有对象都不为null
     *
     * @param objs 待检查的对象数组
     * @return 如果所有对象都不为null则返回true，否则返回false
     */
    public static boolean isAllNotNull(Object... objs) {
        if (!isNull(objs)) {
            for (Object obj : objs) {
                if (isNull(obj)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 判断参数数组中是否任意一个对象不为null
     *
     * @param objs 待检查的对象数组
     * @return 如果有任意一个对象不为null则返回true，否则返回false
     */
    public static boolean isAnyNotNull(Object... objs) {
        if (!isNull(objs)) {
            for (Object obj : objs) {
                if (!isNull(obj)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断对象是否为null
     *
     * @param obj 待检查的对象
     * @return 对象为null返回true，否则返回false
     */
    public static boolean isNull(Object obj) {
        return null == obj;
    }

    /**
     * 判断参数数组中是否所有对象都为空（字符串为空串、数组和集合无元素、Map无键值对）
     *
     * @param isTrim  字符串是否自动去除首尾空白字符
     * @param objs    待检查的对象数组
     * @return 所有对象都为空返回true，否则返回false
     */
    public static boolean isAllEmpty(boolean isTrim, Object... objs) {
        if (isEmpty(objs, isTrim)) {
            return true;
        }
        for (Object obj : objs) {
            if (!isEmpty(obj, isTrim)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断参数数组中是否任意一个对象为空
     *
     * @param objs 待检查的对象数组
     * @return 如果有任意一个对象为空则返回true，否则返回false
     */
    public static boolean isAnyEmpty(Object... objs) {
        if (isEmpty(objs, true)) {
            return true;
        }
        for (Object obj : objs) {
            if (isEmpty(obj, true)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断对象是否为空（null、空字符串、空集合、空数组）
     *
     * @param obj    待检查的对象
     * @param isTrim 如果是字符串，是否去除首尾空白字符
     * @return 对象为空返回true，否则返回false
     */
    public static boolean isEmpty(Object obj, boolean isTrim) {
        return isEmpty(obj, false, isTrim);
    }

    /**
     * 判断对象是否为空（null或空字符串，不做trim处理）
     *
     * @param obj 待检查的对象
     * @return 对象为null或空字符串返回true，否则返回false
     */
    public static boolean isEmpty(Object obj) {
        return isEmpty(obj, false, false);
    }

    /**
     * 检查一个对象是否为空，如果是数组、Collection、Map结构则检查size或length是否大于0
     *
     * @param obj       待检查的对象
     * @param recursive 是否递归检查容器中的元素是否为空
     * @param isTrim    字符串是否去除首尾空白字符
     * @return 对象为空返回true，否则返回false
     */
    public static boolean isEmpty(Object obj, boolean recursive, final boolean isTrim) {
        JudgeEmptyHelper judgeEmptyHelper = JudgeEmptyHelper.newInstance(recursive,isTrim);
        if (null == obj) {
            return true;
        }
        if (obj instanceof Map) {
            return judgeEmptyHelper.dealMap((Map<?, ?>) obj);
        }
        if (obj instanceof Collection) {
            return judgeEmptyHelper.dealCollection((Collection<?>) obj);
        }
        if (obj instanceof CharSequence) {
            return judgeEmptyHelper.dealCharSequence((CharSequence) obj);
        }
        if (obj.getClass().isArray()) {
            return judgeEmptyHelper.dealArray(obj);
        }
        if(obj instanceof Optional){
            return judgeEmptyHelper.dealOptional((Optional) obj);
        }
        return judgeEmptyHelper.dealCommonObject(obj);
    }

    /**
     * 两者都是null时表示相等，传入的两个String对象是否相等
     *
     * @param str1 String对象1
     * @param str2 String对象2
     * @return 两个对象相等：true，以外：false
     */
    public static boolean equals(String str1, String str2) {
        return Objects.equals(str1, str2);
    }

    /**
     * 返回values中第一个非null的值，如果所有值都为null则返回默认值
     *
     * @param defaultValue 默认值
     * @param values       可变参数列表，从左到右返回第一个非null值
     * @param <T>          返回值类型
     * @return 第一个非null值，如果都不存在则返回默认值
     */
    public static <T> T getValue(T defaultValue, Object... values) {
        if (null == values || values.length == 0) {
            return defaultValue;
        }
        for (Object obj : values) {
            if (!isNull(obj)) {
                return (T) obj;
            }
        }
        return defaultValue;
    }

    /**
     * 将source通过convert转换后返回，转换失败时返回默认值
     *
     * @param defaultValue 默认值
     * @param source       待转换的源值
     * @param convert      转换函数
     * @param <T>          返回值类型
     * @param <S>          源值类型
     * @return 转换后的值或默认值
     */
    public static <T, S> T getValue(T defaultValue, S source, ConvertI<S, T> convert) {
        if (source == null) {
            return defaultValue;
        }
        try {
            return convert.apply(source);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 通过Callable获取值，调用异常时返回默认值
     *
     * @param defaultValue 默认值
     * @param call          值的获取函数
     * @param <T>           返回值类型
     * @return Callable返回的值，异常时返回默认值
     */
    public static <T> T getValue(T defaultValue, Callable<T> call) {
        try {
            return call.call();
        } catch (NullPointerException e) {
            return defaultValue;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断对象是否不为空
     *
     * @param obj 待检查的对象
     * @return 对象不为空返回true，否则返回false
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj, true);
    }

    /**
     * 深度拷贝数组对象（浅拷贝，只拷贝引用）
     *
     * @param src  源数组
     * @param dest 目标数组
     * @param <T>  数组元素类型
     */
    public static <T> void cloneArray(T[] src, T[] dest) {
        if (src != null) {
            System.arraycopy(src, 0, dest, 0, src.length);
        }
    }

    /**
     * 深度拷贝对象（通过序列化方式）
     *
     * @param src 待拷贝的对象
     * @param <T> 对象类型
     * @return 拷贝后的新对象
     */
    public static <T> T clone(T src) {
        return deepCopy(src);
    }

    /**
     * 深度拷贝普通的对象（通过Java序列化方式）
     *
     * @param src 待拷贝的对象
     * @param <T> 对象类型
     * @return 拷贝后的新对象
     */
    private static <T> T deepCopy(T src) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(src);
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            return (T) in.readObject();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断对象是否是Java基础类型或常见类型
     *
     * @param obj 待检查的对象
     * @return 是否为Java基础/常见类型（包含Boolean、Byte、Short、Long、Integer、Float、Double、String、Date、数组、Collection）
     */
    public static boolean isBaseJavaType(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj instanceof Object[] || obj instanceof Collection || obj instanceof Boolean || obj instanceof Byte
                || obj instanceof Short || obj instanceof Long || obj instanceof Integer || obj instanceof Float
                || obj instanceof Double || obj instanceof Date || obj instanceof String;
    }

    /**
     * 判断两个对象是否相等（支持null，调用Objects.equals）
     *
     * @param a 对象1
     * @param b 对象2
     * @return 两个对象相等返回true，否则返回false
     * @see Objects#equals(Object, Object)
     */
    public static boolean equals(Object a, Object b) {
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
    public static boolean deepEquals(Object a, Object b) {
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
    public static boolean valueEquals(Object a, Object b) {
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
    public static boolean isInteger(Object a) {
        return a instanceof Byte || a instanceof Short || a instanceof Integer || a instanceof Long;
    }

    /**
     * 判断对象是否是浮点数类型（Float、Double）
     *
     * @param a 待检查的对象
     * @return 是否为浮点数类型
     */
    public static boolean isFloat(Object a) {
        return a instanceof Float || a instanceof Double;
    }

    /**
     * 判断两个数组的值是否相等（比较内容、索引、长度）
     *
     * @param arrayA 数组1
     * @param arrayB 数组2
     * @return 两数组相等返回true，否则返回false
     */
    public static boolean arrayEquals(Object[] arrayA, Object[] arrayB) {
        return arrayEquals(arrayA, arrayB, false);
    }

    /**
     * 判断两个数组的值是否相等（比较内容、索引、长度）
     *
     * @param arrayA  数组1
     * @param arrayB  数组2
     * @param valueEq 是否启用值相等判断（启用后调用valueEquals进行比较）
     * @return 两数组相等返回true，否则返回false
     * @see ObjectUtils#valueEquals(Object, Object)
     */
    public static boolean arrayEquals(Object[] arrayA, Object[] arrayB, boolean valueEq) {
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
