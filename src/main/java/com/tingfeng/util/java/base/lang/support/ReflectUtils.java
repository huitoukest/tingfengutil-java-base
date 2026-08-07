package com.tingfeng.util.java.base.lang.support;

import com.tingfeng.util.java.base.common.constant.PrimitiveType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

/**
 * 反射工具类（存量兼容门面）。
 *
 * 全量方法委托至拆分后的实现类，签名与原版本完全一致，二进制兼容：
 * ReflectFieldOps（字段族：getField/getFields/setFieldValue/getFieldValue/isStatic/isFinal 等）、
 * ReflectMethodOps（方法族：getMethod/invokeMethod/setter/getter/isStatic 等）、
 * ReflectNameOps（命名族：getGetterName/getSetterName/formatGetterOrSetterFieldName/needConvertFiled）。
 *
 * @author huitoukest
 */
public class ReflectUtils {

    private ReflectUtils() {

    }

    // ==================== 类判断（保留门面实现） ====================

    /**
     * 判断这个类是不是 java.lang/math/utils 包中自带的类;
     *
     * @param clazz 类
     * @return 是基础类返回 true，否则返回 false
     */
    public static boolean isBaseJavaClass(Class<?> clazz) {
        boolean isBaseClass = false;
        if (clazz.isArray()) {
            isBaseClass = false;
        } else if (clazz.isPrimitive() || clazz.getPackage() == null
                || clazz.getPackage().getName().equals("java.lang")
                || clazz.getPackage().getName().equals("java.math")
                || clazz.getPackage().getName().equals("java.util")) {
            isBaseClass = true;
        }
        return isBaseClass;
    }

    /**
     * 判断类是否是基础数据或者包装类型或者 Date 类型
     *
     * @param cls 类
     * @return 如果是基础数据或者包装类型或者 Date 类型返回 true，否则返回 false；如果 cls 为 null 返回 false
     */
    public static boolean isJavaBaseDataClass(Class<?> cls) {
        if (cls == null) {
            return false;
        }
        return PrimitiveType.isPrimitiveOrWrapper(cls)
                || Date.class.equals(cls)
                || String.class.equals(cls);
    }

    /**
     * 判断类名是否是基础数据或者包装类型或者 Date 类型
     *
     * @param clsName 类名
     * @return 如果是基础数据或者包装类型或者 Date 类型返回 true，否则返回 false；如果 clsName 为 null 返回 false
     */
    public static boolean isJavaBaseDataClass(String clsName) {
        if (clsName == null) {
            return false;
        }
        for (PrimitiveType pt : PrimitiveType.values()) {
            if (pt.getPrimitiveType().getName().equals(clsName)
                    || pt.getWrapperType().getName().equals(clsName)) {
                return true;
            }
        }
        return "java.util.Date".equals(clsName) || "java.lang.String".equals(clsName);
    }

    /**
     * 得到实体类
     *
     * @param objClass 实体类名,包含包名
     * @return 实体类，类不存在时返回 null
     */
    public static Class<?> getObjectClass(String objClass) {
        Class<?> entityClass = null;
        try {
            entityClass = Class.forName(objClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return entityClass;
    }

    // ==================== 字段族委托 ====================

    /**
     * 是否是静态属性，同 {@link ReflectFieldOps#isStatic(Field)}
     *
     * @param field 字段
     * @return 是静态属性返回 true，否则返回 false
     */
    public static boolean isStatic(Field field) {
        return ReflectFieldOps.isStatic(field);
    }

    /**
     * 是否是 final 属性，同 {@link ReflectFieldOps#isFinal(Field)}
     *
     * @param field 字段
     * @return 是 final 属性返回 true，否则返回 false
     */
    public static boolean isFinal(Field field) {
        return ReflectFieldOps.isFinal(field);
    }

    /**
     * 返回一个类下的所有属性，同 {@link ReflectFieldOps#getFields(Class, boolean, boolean, boolean, boolean)}
     *
     * @param cls                      类
     * @param isContainsStatic         是否包含静态属性
     * @param isFinal                  是否包含 final 属性
     * @param isUseCache               是否将结果缓存
     * @param containsParentPrivateField 是否包含父类的私有属性
     * @return 属性列表
     */
    public static List<Field> getFields(Class<?> cls, boolean isContainsStatic, boolean isFinal, boolean isUseCache, boolean containsParentPrivateField) {
        return ReflectFieldOps.getFields(cls, isContainsStatic, isFinal, isUseCache, containsParentPrivateField);
    }

    /**
     * 返回一个类下的所有属性，同 {@link ReflectFieldOps#getFields(Class, boolean, boolean, boolean)}
     *
     * @param cls              类
     * @param isContainsStatic 是否包含静态属性
     * @param isFinal          是否包含 final 属性
     * @param isUseCache       是否将结果缓存
     * @return 属性列表
     */
    public static List<Field> getFields(Class<?> cls, boolean isContainsStatic, boolean isFinal, boolean isUseCache) {
        return ReflectFieldOps.getFields(cls, isContainsStatic, isFinal, isUseCache);
    }

    /**
     * 返回一个类下的所有属性，不包含静态属性，同 {@link ReflectFieldOps#getFields(Class)}
     *
     * @param cls 类
     * @return 属性列表
     */
    public static List<Field> getFields(Class<?> cls) {
        return ReflectFieldOps.getFields(cls);
    }

    /**
     * 在此类,和其超类中寻找此属性，同 {@link ReflectFieldOps#getField(Class, String, boolean, boolean)}
     *
     * @param cls           类
     * @param fieldName     属性名称
     * @param setAccessible 是否设置可访问
     * @param useCache      是否使用缓存
     * @return 字段对象，未找到时返回 null
     */
    public static Field getField(Class<?> cls, String fieldName, boolean setAccessible, boolean useCache) {
        return ReflectFieldOps.getField(cls, fieldName, setAccessible, useCache);
    }

    /**
     * 在此类,和其超类中寻找此属性，默认使用缓存，同 {@link ReflectFieldOps#getField(Class, String, boolean)}
     *
     * @param cls           类
     * @param fieldName     属性名称
     * @param setAccessible 是否设置可访问
     * @return 字段对象，未找到时返回 null
     */
    public static Field getField(Class<?> cls, String fieldName, boolean setAccessible) {
        return ReflectFieldOps.getField(cls, fieldName, setAccessible);
    }

    /**
     * 在此类,和其超类中寻找此属性，同 {@link ReflectFieldOps#getField(Class, String)}
     *
     * @param cls       类
     * @param fieldName 属性名称
     * @return 字段对象，未找到时返回 null
     */
    public static Field getField(Class<?> cls, String fieldName) {
        return ReflectFieldOps.getField(cls, fieldName);
    }

    /**
     * 给属性设置值,会先尝试调用其 setter 方法,如果没有 setter 方法会直接给属性赋值
     * 支持 a.b.c 的链式调用取值; 基础数据类型属性需要手动传入参数，
     * 同 {@link ReflectFieldOps#setFieldValue(boolean, Object, String, Object[], Class[])}
     *
     * @param isReadNotPublicField 如果 Field 属性不是 public,那么直接赋值可能会失败，设置是否读取非 public 的属性
     * @param obj                  此属性的对象实例
     * @param filedName            属性的名称
     * @param values               参数的值
     * @param parameterTypes       参数类型
     */
    public static void setFieldValue(boolean isReadNotPublicField, Object obj, String filedName, Object[] values, Class<?>... parameterTypes) {
        ReflectFieldOps.setFieldValue(isReadNotPublicField, obj, filedName, values, parameterTypes);
    }

    /**
     * 给属性设置值,会先尝试调用其 setter 方法,如果没有 setter 方法会直接给属性赋值
     * 支持 a.b.c 的链式调用取值; 基础数据类型属性需要手动传入参数，会读取非 public 的属性，
     * 同 {@link ReflectFieldOps#setFieldValue(Object, String, Object[], Class[])}
     *
     * @param obj            此属性的对象实例
     * @param filedName      属性的名称
     * @param values         参数的值
     * @param parameterTypes 参数类型
     */
    public static void setFieldValue(Object obj, String filedName, Object[] values, Class<?>... parameterTypes) {
        ReflectFieldOps.setFieldValue(obj, filedName, values, parameterTypes);
    }

    /**
     * 给属性设置值,会先尝试调用其 setter 方法,如果没有 setter 方法会直接给属性赋值
     * 支持 a.b.c 的链式调用取值; 基础数据类型属性需要手动传入参数，会读取非 public 的属性，
     * 同 {@link ReflectFieldOps#setFieldValue(Object, String, Object)}
     *
     * @param obj       此属性的对象实例
     * @param filedName 属性的名称
     * @param value     参数的值,通过此值来推断对象类型，不支持基础数据类型
     */
    public static void setFieldValue(Object obj, String filedName, Object value) {
        ReflectFieldOps.setFieldValue(obj, filedName, value);
    }

    /**
     * 取属性值,会先尝试调用其 getter 方法,如果没有 getter 方法会直接操作属性，
     * 同 {@link ReflectFieldOps#getFieldValue(boolean, Object, String)}
     *
     * @param obj 此属性的对象实例
     * @return 如果没有找到属性会返回 null;
     */
    public static Object getFieldValue(boolean isReadNotPublicField, Object obj, String filedName) {
        return ReflectFieldOps.getFieldValue(isReadNotPublicField, obj, filedName);
    }

    /**
     * 取属性值,会先尝试调用其 getter 方法,如果没有 getter 方法会直接操作属性
     * 支持 a.b.c 对象链式属性调用，
     * 同 {@link ReflectFieldOps#getFieldValue(boolean, Object, String, Object[], Class[])}
     *
     * @param obj            此属性的对象实例
     * @param values         参数的值
     * @param parameterTypes 参数类型
     * @return 如果没有找到属性会返回 null;
     */
    public static Object getFieldValue(boolean isReadNotPublicField, Object obj, String filedName, Object[] values, Class<?>[] parameterTypes) {
        return ReflectFieldOps.getFieldValue(isReadNotPublicField, obj, filedName, values, parameterTypes);
    }

    /**
     * 判断字段是否是基础数据或者包装类型或者 Date 类型，同 {@link ReflectFieldOps#isJavaBaseDataField(Field)}
     *
     * @param field 字段
     * @return 如果是基础数据或者包装类型或者 Date 类型返回 true，否则返回 false
     */
    public static boolean isJavaBaseDataField(Field field) {
        return ReflectFieldOps.isJavaBaseDataField(field);
    }

    /**
     * 根据实体类名得到实体的所有属性名称，同 {@link ReflectFieldOps#getFieldNames(String)}
     *
     * @param objClass 实体类名,包含包名
     * @return 属性名称数组
     * @throws ClassNotFoundException 类不存在时抛出
     */
    public static String[] getFieldNames(String objClass) throws ClassNotFoundException {
        return ReflectFieldOps.getFieldNames(objClass);
    }

    /**
     * 将属性的值转换为一个数组，同 {@link ReflectFieldOps#fieldToValue(Field[], Object)}
     *
     * @param f 字段数组
     * @param o 对象实例
     * @return 属性值数组
     * @throws Exception 反射取值异常时抛出
     */
    public static Object[] fieldToValue(Field[] f, Object o) throws Exception {
        return ReflectFieldOps.fieldToValue(f, o);
    }

    /**
     * 得到除开指定名称的属性列，同 {@link ReflectFieldOps#getFieldNames(Class, String[])}
     *
     * @param cls            类
     * @param exceptCoulumns 排除的属性名称
     * @return 属性名称列表
     */
    public static List<String> getFieldNames(Class<?> cls, String... exceptCoulumns) {
        return ReflectFieldOps.getFieldNames(cls, exceptCoulumns);
    }

    /**
     * 得到除开指定名称的属性列，同 {@link ReflectFieldOps#getFieldNames(Class, List)}
     *
     * @param cls            类
     * @param exceptCoulumns 排除的属性名称
     * @return 属性名称列表
     */
    public static List<String> getFieldNames(Class<?> cls, List<String> exceptCoulumns) {
        return ReflectFieldOps.getFieldNames(cls, exceptCoulumns);
    }

    /**
     * 返回此类中此名称的属性的类型, 如果不存在则返回 null，同 {@link ReflectFieldOps#getTypeByFieldName(Class, String)}
     *
     * @param cls       类
     * @param filedName 属性名称,支持 a.b.c 的方式
     * @return 属性类型，不存在则返回 null
     */
    public static Class<?> getTypeByFieldName(Class<?> cls, String filedName) {
        return ReflectFieldOps.getTypeByFieldName(cls, filedName);
    }

    // ==================== 方法族委托 ====================

    /**
     * 是否是静态方法，同 {@link ReflectMethodOps#isStaticMethod(Class, String, Class[])}
     *
     * @param cls            类名
     * @param methodName     方法名称
     * @param parameterTypes 每个参数的类型
     * @return 是静态方法返回 true，否则返回 false
     */
    public static boolean isStaticMethod(Class<?> cls, String methodName, Class<?>... parameterTypes) {
        return ReflectMethodOps.isStaticMethod(cls, methodName, parameterTypes);
    }

    /**
     * 是否是静态方法，同 {@link ReflectMethodOps#isStatic(Method)}
     *
     * @param method 方法
     * @return 是静态方法返回 true，否则返回 false
     */
    public static boolean isStatic(Method method) {
        return ReflectMethodOps.isStatic(method);
    }

    /**
     * 是否是 final 方法，同 {@link ReflectMethodOps#isFinal(Method)}
     *
     * @param method 方法
     * @return 是 final 方法返回 true，否则返回 false
     */
    public static boolean isFinal(Method method) {
        return ReflectMethodOps.isFinal(method);
    }

    /**
     * 通过反射来调用方法，同 {@link ReflectMethodOps#invokeMethod(Object, String)}
     *
     * @param obj        对象
     * @param methodName 方法名称
     * @return 方法返回值
     * @throws NoSuchMethodException 方法不存在时抛出
     */
    public static Object invokeMethod(Object obj, String methodName) throws NoSuchMethodException {
        return ReflectMethodOps.invokeMethod(obj, methodName);
    }

    /**
     * 通过反射来调用一个方法，同 {@link ReflectMethodOps#invokeMethod(Object, String, Object[], Class[])}
     *
     * @param obj            对象
     * @param methodName     方法名称
     * @param params         参数的值
     * @param parameterTypes 参数类型
     * @return 方法返回值
     * @throws NoSuchMethodException 方法不存在时抛出
     */
    public static Object invokeMethod(Object obj, String methodName, Object[] params, Class<?>... parameterTypes) throws NoSuchMethodException {
        return ReflectMethodOps.invokeMethod(obj, methodName, params, parameterTypes);
    }

    /**
     * 通过反射调用指定 setter 方法,成功返回 true,否则返回 false;，
     * 同 {@link ReflectMethodOps#setter(Object, String, Object, Class)}
     *
     * @param obj   对象
     * @param attr  属性名称,如 name
     * @param value 参数的值
     * @param type  参数的类型
     * @return 设置成功返回 true，失败返回 false
     */
    public static boolean setter(Object obj, String attr, Object value, Class<?> type) {
        return ReflectMethodOps.setter(obj, attr, value, type);
    }

    /**
     * 通过反射调用指定 setter 方法,成功返回 true,否则返回 false;，
     * 同 {@link ReflectMethodOps#setter(Object, String, Object)}
     *
     * @param obj   对象
     * @param attr  属性名称,如 name
     * @param value 参数的值
     * @return 设置成功返回 true，失败返回 false
     */
    public static boolean setter(Object obj, String attr, Object value) {
        return ReflectMethodOps.setter(obj, attr, value);
    }

    /**
     * 通过反射调用指定 getter 方法,成功返回相应的值,否则返回 null;，
     * 同 {@link ReflectMethodOps#getter(Object, String)}
     *
     * @param obj  对象
     * @param attr 属性名称,如 name
     * @return 属性值
     */
    public static Object getter(Object obj, String attr) {
        return ReflectMethodOps.getter(obj, attr);
    }

    /**
     * 通过类、方法名称、参数类型来获取方法,默认使用缓存，同 {@link ReflectMethodOps#getMethod(Class, String, Class[])}
     *
     * @param cls         类
     * @param methodName  方法名称
     * @param paramsTypes 参数类型
     * @return 方法对象，未找到时返回 null
     */
    public static Method getMethod(Class<?> cls, String methodName, Class<?>... paramsTypes) {
        return ReflectMethodOps.getMethod(cls, methodName, paramsTypes);
    }

    /**
     * 通过类、方法名称、参数类型来获取方法，同 {@link ReflectMethodOps#getMethod(Class, String, boolean, Class[])}
     *
     * @param cls         类
     * @param methodName  方法名称
     * @param useCache    是否使用缓存
     * @param paramsTypes 参数类型
     * @return 方法对象，未找到时返回 null
     */
    public static Method getMethod(Class<?> cls, String methodName, boolean useCache, Class<?>... paramsTypes) {
        return ReflectMethodOps.getMethod(cls, methodName, useCache, paramsTypes);
    }

    /**
     * 封装原始的反射获取方法，没有方法时不会抛出 NoSuchMethodException 异常，而是返回 null，
     * 同 {@link ReflectMethodOps#getMethodByNative(Class, String, Class[])}
     *
     * @param cls         类
     * @param methodName  方法名称
     * @param paramsTypes 参数类型
     * @return 方法对象，未找到时返回 null
     */
    public static Method getMethodByNative(Class<?> cls, String methodName, Class<?>... paramsTypes) {
        return ReflectMethodOps.getMethodByNative(cls, methodName, paramsTypes);
    }

    // ==================== 命名族委托 ====================

    /**
     * 根据属性名称生成 getter 方法名，同 {@link ReflectNameOps#getGetterName(String)}
     *
     * @param fieldName 属性名称
     * @return getter 方法名
     */
    public static String getGetterName(String fieldName) {
        return ReflectNameOps.getGetterName(fieldName);
    }

    /**
     * 根据属性名称生成 setter 方法名，同 {@link ReflectNameOps#getSetterName(String)}
     *
     * @param fieldName 属性名称
     * @return setter 方法名
     */
    public static String getSetterName(String fieldName) {
        return ReflectNameOps.getSetterName(fieldName);
    }
}
