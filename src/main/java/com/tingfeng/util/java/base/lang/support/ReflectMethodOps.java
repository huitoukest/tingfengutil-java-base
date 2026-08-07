package com.tingfeng.util.java.base.lang.support;

import com.tingfeng.util.java.base.cache.SimpleCacheHelper;
import com.tingfeng.util.java.base.common.constant.Constants;
import com.tingfeng.util.java.base.lang.StringUtils;
import com.tingfeng.util.java.base.lang.exception.BaseException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 方法反射操作实现。
 *
 * 包级私有，不对外暴露。通过 {@link ReflectUtils} 对外提供统一 API。
 */
class ReflectMethodOps {

    /**
     * 数量固定的方法缓存
     */
    private static SimpleCacheHelper<String, Method> DATA_METHOD_CACHE = new SimpleCacheHelper<>(1024);

    private ReflectMethodOps() {

    }

    /**
     * 是否是静态方法
     *
     * @param cls            类名
     * @param methodName     方法名称
     * @param parameterTypes 每个参数的类型
     * @return 是静态方法返回 true，否则返回 false
     */
    static boolean isStaticMethod(Class<?> cls, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        try {
            method = cls.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new BaseException(e);
        }
        int modifiers = method.getModifiers();
        return Modifier.isStatic(modifiers);
    }

    /**
     * 是否是静态方法
     *
     * @param method 方法
     * @return 是静态方法返回 true，否则返回 false
     */
    static boolean isStatic(Method method) {
        int modifiers = method.getModifiers();
        return Modifier.isStatic(modifiers);
    }

    /**
     * 是否是 final 方法
     *
     * @param method 方法
     * @return 是 final 方法返回 true，否则返回 false
     */
    static boolean isFinal(Method method) {
        return java.lang.reflect.Modifier.isFinal(method.getModifiers());
    }

    /**
     * 通过类、方法名称、参数类型来获取方法,默认使用缓存
     *
     * @param cls         类
     * @param methodName  方法名称
     * @param paramsTypes 参数类型
     * @return 方法对象，未找到时返回 null
     */
    static Method getMethod(Class<?> cls, String methodName, Class<?>... paramsTypes) {
        return getMethod(cls, methodName, true, paramsTypes);
    }

    /**
     * 通过类、方法名称、参数类型来获取方法
     *
     * @param cls         类
     * @param methodName  方法名称
     * @param useCache    是否使用缓存
     * @param paramsTypes 参数类型
     * @return 方法对象，未找到时返回 null
     */
    static Method getMethod(Class<?> cls, String methodName, boolean useCache, Class<?>... paramsTypes) {
        Method method = null;
        if (useCache) {
            String key = getMethodKey(cls, methodName, paramsTypes);
            method = DATA_METHOD_CACHE.get(key);
            if (method == null && !DATA_METHOD_CACHE.containsKey(key)) {
                method = getMethodByNative(cls, methodName, paramsTypes);
                DATA_METHOD_CACHE.set(key, method);
            }
        } else {
            method = getMethodByNative(cls, methodName, paramsTypes);
        }
        return method;
    }

    /**
     * 封装原始的反射获取方法，没有方法时不会抛出 NoSuchMethodException 异常，而是返回 null
     *
     * @param cls         类
     * @param methodName  方法名称
     * @param paramsTypes 参数类型
     * @return 方法对象，未找到时返回 null
     */
    static Method getMethodByNative(Class<?> cls, String methodName, Class<?>... paramsTypes) {
        try {
            return cls.getMethod(methodName, paramsTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * 根据方法的名称和参数获取其缓存的 key 值
     *
     * @param cls         类
     * @param methodName  方法名称
     * @param paramsTypes 参数类型
     * @return 缓存 key
     */
    private static String getMethodKey(Class<?> cls, String methodName, Class<?>... paramsTypes) {
        return StringUtils.doAppend(sb -> {
            sb.append(cls.getName());
            sb.append(Constants.Symbol.semicolon);
            sb.append(methodName);
            sb.append(Constants.Symbol.semicolon);
            if (paramsTypes != null) {
                for (Class<?> it : paramsTypes) {
                    sb.append(it.getName());
                }
            }
            return sb.toString();
        });
    }

    /**
     * 通过反射来调用方法
     *
     * @param obj        对象
     * @param methodName 方法名称
     * @return 方法返回值
     * @throws NoSuchMethodException 方法不存在时抛出
     */
    static Object invokeMethod(Object obj, String methodName) throws NoSuchMethodException {
        Class<?>[] classes = null;
        return invokeMethod(obj, methodName, null, classes);
    }

    /**
     * 通过反射来调用一个方法
     *
     * @param obj            对象
     * @param methodName     方法名称
     * @param params         参数的值
     * @param parameterTypes 参数类型
     * @return 方法返回值
     * @throws NoSuchMethodException 方法不存在时抛出
     */
    static Object invokeMethod(Object obj, String methodName, Object[] params, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = getMethod(obj.getClass(), methodName, parameterTypes);
        try {
            method.setAccessible(true);
            return method.invoke(obj, params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new BaseException(e);
        }
    }

    /**
     * 通过反射调用指定 setter 方法,成功返回 true,否则返回 false;
     *
     * @param obj   对象
     * @param attr  属性名称,如 name
     * @param value 参数的值
     * @param type  参数的类型
     * @return 设置成功返回 true，失败返回 false
     */
    static boolean setter(Object obj, String attr, Object value, Class<?> type) {
        try {
            String methodName = ReflectUtils.getSetterName(attr);
            // 第一个参数表示方法名称，setAge、setName,第二个参数表示类型，如int.class,String.class
            Method method = getMethod(obj.getClass(), methodName, type);
            if (method == null) {
                return false;
            }
            // 调用方法
            method.invoke(obj, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 通过反射调用指定 setter 方法,成功返回 true,否则返回 false;
     *
     * @param obj   对象
     * @param attr  属性名称,如 name
     * @param value 参数的值
     * @return 设置成功返回 true，失败返回 false
     */
    static boolean setter(Object obj, String attr, Object value) {
        Class<?> type = ReflectUtils.getField(obj.getClass(), attr).getType();
        return setter(obj, attr, value, type);
    }

    /**
     * 通过反射调用指定 getter 方法,成功返回相应的值,否则返回 null;
     *
     * @param obj  对象
     * @param attr 属性名称,如 name
     * @return 属性值
     */
    static Object getter(Object obj, String attr) {// 调用getter方法
        try {
            // 此方法不需要参数，如：getName(),getAge()
            Method method = getMethod(obj.getClass(), ReflectUtils.getGetterName(attr));
            if (method != null) {
                return method.invoke(obj);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
