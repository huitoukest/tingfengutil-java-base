package com.tingfeng.util.java.base.lang.support;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 泛型与Type相关的工具类
 */
public class GenericTypeUtils {
    /**
     * 获取类实现的指定接口/父类的泛型参数
     *
     * @param clazz          要检查的类
     * @param interfaceType  要查找的接口/父类类型
     * @return 接口的泛型参数数组，如果未实现则返回 null
     */
    public static Type[] getGenericTypeArguments(Class<?> clazz, Class<?> interfaceType) {
        if (clazz == null || interfaceType == null) {
            return null;
        }

        // 检查直接实现的泛型接口
        Type[] interfaces = clazz.getGenericInterfaces();
        for (Type type : interfaces) {
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                if (interfaceType.isAssignableFrom((Class<?>) pt.getRawType())) {
                    return pt.getActualTypeArguments();
                }
            } else if (type instanceof Class) {
                Type[] parentTypes = getGenericTypeArguments((Class<?>) type, interfaceType);
                if (parentTypes != null) {
                    return parentTypes;
                }
            }
        }

        // 递归检查父类
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            if (interfaceType.isAssignableFrom(superClass)) {
                return getGenericTypeArguments(superClass, interfaceType);
            }
        }

        return null;
    }

    /**
     * 判断类是否实现了指定接口且其泛型参数匹配 targetTypes
     *
     * @param clazz         要检查的类
     * @param interfaceType 要查找的接口/父类类型
     * @param targetTypes   可变参数，要匹配的泛型类型（按顺序）
     * @return true 如果接口存在且泛型参数类型按顺序匹配 targetTypes
     */
    public static boolean isImplGenericInterface(Class<?> clazz, Class<?> interfaceType, Class<?>... targetTypes) {
        Type[] typeArgs = getGenericTypeArguments(clazz, interfaceType);

        // targetTypes 为空，只检查是否实现了该接口
        if (targetTypes == null || targetTypes.length == 0) {
            return typeArgs != null;
        }

        if (typeArgs == null || typeArgs.length < targetTypes.length) {
            return false;
        }

        // 按顺序逐个匹配泛型参数
        for (int i = 0; i < targetTypes.length; i++) {
            Type genericType = typeArgs[i];
            if (!(genericType instanceof Class)) {
                return false;
            }
            if (!targetTypes[i].isAssignableFrom((Class<?>) genericType)) {
                return false;
            }
        }
        return true;
    }
}
