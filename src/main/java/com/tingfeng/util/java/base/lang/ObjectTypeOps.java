package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.common.constant.ObjectType;
import com.tingfeng.util.java.base.common.constant.PrimitiveType;
import com.tingfeng.util.java.base.validate.JudgeEmptyHelper;
import com.tingfeng.util.java.base.lang.base.ConvertI;
import com.tingfeng.util.java.base.datetime.DateUtils;
import com.tingfeng.util.java.base.lang.StringUtils;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * 对象类型判断、判空与取值操作实现。
 *
 * 包级私有，不对外暴露。通过 {@link ObjectUtils} 对外提供统一 API。
 */
class ObjectTypeOps {

    private static final Map<String, ObjectType> OBJECT_TYPE_MAP = new HashMap<>(18);

    static {
        OBJECT_TYPE_MAP.put(java.lang.Boolean.class.getName(), ObjectType.BOOLEAN);
        OBJECT_TYPE_MAP.put(boolean.class.getName(), ObjectType.BOOLEAN);
        OBJECT_TYPE_MAP.put(java.util.Date.class.getName(), ObjectType.DATE);
        OBJECT_TYPE_MAP.put(java.lang.Float.class.getName(), ObjectType.FLOAT);
        OBJECT_TYPE_MAP.put(float.class.getName(), ObjectType.FLOAT);
        OBJECT_TYPE_MAP.put(java.lang.Double.class.getName(), ObjectType.DOUBLE);
        OBJECT_TYPE_MAP.put(double.class.getName(), ObjectType.DOUBLE);
        OBJECT_TYPE_MAP.put(java.lang.Long.class.getName(), ObjectType.LONG);
        OBJECT_TYPE_MAP.put(long.class.getName(), ObjectType.LONG);
        OBJECT_TYPE_MAP.put(java.lang.Integer.class.getName(), ObjectType.INTEGER);
        OBJECT_TYPE_MAP.put(int.class.getName(), ObjectType.INTEGER);
        OBJECT_TYPE_MAP.put(java.lang.String.class.getName(), ObjectType.STRING);
        OBJECT_TYPE_MAP.put(java.lang.Short.class.getName(), ObjectType.SHORT);
        OBJECT_TYPE_MAP.put(short.class.getName(), ObjectType.SHORT);
        OBJECT_TYPE_MAP.put(java.lang.Byte.class.getName(), ObjectType.BYTE);
        OBJECT_TYPE_MAP.put(byte.class.getName(), ObjectType.BYTE);
    }

    private ObjectTypeOps() {

    }

    /**
     * 通过class获取对象的类型
     *
     * @param cls class对象
     * @return 对象类型枚举
     */
    static ObjectType getObjectType(Class<?> cls) {
        return getObjectType(cls.getName());
    }

    /**
     * 通过className获取对象的类型
     *
     * @param className 类的全限定名
     * @return 对象类型枚举，未知类型返回ObjectType.OTHER
     */
    static ObjectType getObjectType(String className) {
        return OBJECT_TYPE_MAP.getOrDefault(className, ObjectType.OTHER);
    }

    /**
     * 通过属性来返回此属性的类型
     *
     * @param field 反射字段
     * @return 对象类型枚举
     */
    static ObjectType getObjectType(Field field) {
        return getObjectType(field.getType().getCanonicalName());
    }

    /**
     * 判断className对应的类型是否是基础数据类型（包装类型或基本类型）
     *
     * @param clsName 类的全限定名
     * @return 是否为基础数据类型
     */
    static boolean isBaseTypeObject(String clsName) {
        return getObjectType(clsName) != ObjectType.OTHER;
    }

    /**
     * 判断field对应的类型是否是基础数据类型（包装类型或基本类型）
     *
     * @param field 反射字段
     * @return 是否为基础数据类型
     */
    static boolean isBaseTypeObject(Field field) {
        return isBaseTypeObject(field.getType().getCanonicalName());
    }

    /**
     * 判断cls对应的类型是否是基础数据类型（包装类型或基本类型）
     *
     * @param cls class对象
     * @return 是否为基础数据类型
     */
    static boolean isBaseTypeObject(Class<?> cls) {
        return isBaseTypeObject(cls.getName());
    }

    /**
     * 把xml字符串反序列化为对象
     *
     * 安全警告：底层使用 XMLDecoder 反序列化，该 API 已被 JDK 官方标记弃用，
     * 存在 XXE（XML 外部实体注入）与任意类实例化攻击风险，仅可处理可信 XML。
     *
     * @param xml xml格式字符串
     * @return 反序列化后的对象
     * @throws UnsupportedEncodingException UTF8编码异常
     */
    static Object getObjectByXml(String xml) throws UnsupportedEncodingException {
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
    static Boolean isBoolean(Class<?> cls) {
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
    static <T> T getObject(Class<T> cls, Object obj) {
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
        // Handle String and Date separately (not covered by PrimitiveType)
        if (cls == java.lang.String.class) {
            return (T) value;
        }
        if (cls == java.util.Date.class) {
            return (T) DateUtils.getDate(value);
        }
        // Handle primitive types (int, boolean, etc.) and wrapper types (Integer, Boolean, etc.)
        PrimitiveType pt = PrimitiveType.fromPrimitive(cls);
        if (pt != null) {
            return handlePrimitiveByType(pt, value);
        }
        pt = PrimitiveType.fromWrapper(cls);
        if (pt != null) {
            return handleWrapperByType(pt, value);
        }
        return (T) obj;
    }

    /**
     * 判断参数数组中是否任意一个对象为null
     *
     * @param objs 待检查的对象数组
     * @return 如果有任意一个对象为null则返回true，否则返回false
     */
    static boolean isAnyNull(Object... objs) {
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
    static boolean isAllNull(Object... objs) {
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
    static boolean isNotNull(Object obj) {
        return !isNull(obj);
    }

    /**
     * 判断参数数组中是否所有对象都不为null
     *
     * @param objs 待检查的对象数组
     * @return 如果所有对象都不为null则返回true，否则返回false
     */
    static boolean isAllNotNull(Object... objs) {
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
    static boolean isAnyNotNull(Object... objs) {
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
    static boolean isNull(Object obj) {
        return null == obj;
    }

    /**
     * 判断参数数组中是否所有对象都为空（字符串为空串、数组和集合无元素、Map无键值对）
     *
     * @param isTrim  字符串是否自动去除首尾空白字符
     * @param objs    待检查的对象数组
     * @return 所有对象都为空返回true，否则返回false
     */
    static boolean isAllEmpty(boolean isTrim, Object... objs) {
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
    static boolean isAnyEmpty(Object... objs) {
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
    static boolean isEmpty(Object obj, boolean isTrim) {
        return isEmpty(obj, false, isTrim);
    }

    /**
     * 判断对象是否为空（null或空字符串，不做trim处理）
     *
     * @param obj 待检查的对象
     * @return 对象为null或空字符串返回true，否则返回false
     */
    static boolean isEmpty(Object obj) {
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
    static boolean isEmpty(Object obj, boolean recursive, final boolean isTrim) {
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
     * 返回values中第一个非null的值，如果所有值都为null则返回默认值
     *
     * @param defaultValue 默认值
     * @param values       可变参数列表，从左到右返回第一个非null值
     * @param <T>          返回值类型
     * @return 第一个非null值，如果都不存在则返回默认值
     */
    static <T> T getValue(T defaultValue, Object... values) {
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
    static <T, S> T getValue(T defaultValue, S source, ConvertI<S, T> convert) {
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
    static <T> T getValue(T defaultValue, Callable<T> call) {
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
    static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj, true);
    }

    /**
     * 判断对象是否是Java基础类型或常见类型
     *
     * @param obj 待检查的对象
     * @return 是否为Java基础/常见类型（包含Boolean、Byte、Short、Long、Integer、Float、Double、String、Date、数组、Collection）
     */
    static boolean isBaseJavaType(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj instanceof Object[] || obj instanceof Collection || obj instanceof Boolean || obj instanceof Byte
                || obj instanceof Short || obj instanceof Long || obj instanceof Integer || obj instanceof Float
                || obj instanceof Double || obj instanceof Date || obj instanceof String;
    }

    /**
     * 尝试运行功能
     * @param supplier
     * @return null = 异常,成功返回运行的值
     * @param <T>
     */
    static <T> T tryDo(Supplier<T> supplier) {
        try {
            return supplier.get();
        }catch (Exception e){
            return null;
        }
    }

    /**
     * 根据 PrimitiveType 处理原始类型的值转换
     *
     * @param pt    PrimitiveType 枚举
     * @param value 字符串值
     * @param <T>   目标类型
     * @return 转换后的值
     */
    @SuppressWarnings("unchecked")
    private static <T> T handlePrimitiveByType(PrimitiveType pt, String value) {
        switch (pt) {
            case BOOLEAN:
                return (T) StringUtils.getBoolean(value, false);
            case BYTE:
                return (T) StringUtils.getByte(value, (byte) 0);
            case DOUBLE:
                return (T) StringUtils.getDouble(value, 0d);
            case FLOAT:
                return (T) StringUtils.getFloat(value, 0f);
            case INT:
                return (T) StringUtils.getInteger(value, 0);
            case LONG:
                return (T) StringUtils.getLong(value, 0L);
            case SHORT:
                return (T) StringUtils.getShort(value, (short) 0);
            default:
                return (T) value;
        }
    }

    /**
     * 根据 PrimitiveType 处理包装类型的值转换
     *
     * @param pt    PrimitiveType 枚举
     * @param value 字符串值
     * @param <T>   目标类型
     * @return 转换后的值
     */
    @SuppressWarnings("unchecked")
    private static <T> T handleWrapperByType(PrimitiveType pt, String value) {
        switch (pt) {
            case BOOLEAN:
                return (T) StringUtils.getBoolean(value);
            case BYTE:
                return (T) StringUtils.getByte(value);
            case DOUBLE:
                return (T) StringUtils.getDouble(value);
            case FLOAT:
                return (T) StringUtils.getFloat(value);
            case INT:
                return (T) StringUtils.getInteger(value);
            case LONG:
                return (T) StringUtils.getLong(value);
            case SHORT:
                return (T) StringUtils.getShort(value);
            default:
                return (T) value;
        }
    }
}
