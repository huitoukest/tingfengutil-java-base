package com.tingfeng.util.java.base.lang.support;

import com.tingfeng.util.java.base.cache.SimpleCacheHelper;
import com.tingfeng.util.java.base.common.constant.Constants;
import com.tingfeng.util.java.base.lang.StringUtils;
import com.tingfeng.util.java.base.lang.exception.BaseException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字段反射操作实现。
 *
 * 包级私有，不对外暴露。通过 {@link ReflectUtils} 对外提供统一 API。
 */
class ReflectFieldOps {

    /**
     * 数量固定的属性资源缓存
     */
    private static SimpleCacheHelper<String, List<Field>> DATA_FILED_LIST_CACHE = new SimpleCacheHelper<>(100);

    /**
     * 缓存类的单个自动获取
     */
    private static SimpleCacheHelper<String, Field> DATA_FILED_CACHE = new SimpleCacheHelper<>(1024);

    private ReflectFieldOps() {

    }

    /**
     * 是否是静态属性
     *
     * @param field 字段
     * @return 是静态属性返回 true，否则返回 false
     */
    static boolean isStatic(Field field) {
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        return isStatic;
    }

    /**
     * 是否是 final 属性
     *
     * @param field 字段
     * @return 是 final 属性返回 true，否则返回 false
     */
    static boolean isFinal(Field field) {
        return java.lang.reflect.Modifier.isFinal(field.getModifiers());
    }

    /**
     * 返回一个类下的所有属性
     *
     * @param cls                      类
     * @param isContainsStatic         是否包含静态属性
     * @param isFinal                  是否包含 final 属性
     * @param isUseCache               是否将结果缓存
     * @param containsParentPrivateField 是否包含父类的私有属性
     * @return 属性列表
     */
    static List<Field> getFields(Class<?> cls, boolean isContainsStatic, boolean isFinal, boolean isUseCache, boolean containsParentPrivateField) {
        if (cls == null || cls.getName().equals(Object.class.getName())) {
            return Collections.EMPTY_LIST;
        }
        List<Field> fieldList = null;
        String key = StringUtils.doAppend(sb -> {
            sb.append(cls.getName());
            sb.append(Constants.Symbol.semicolon);
            sb.append(isContainsStatic);
            sb.append(isFinal);
            sb.append(containsParentPrivateField);
            return sb.toString();
        });
        if (isUseCache) {
            fieldList = DATA_FILED_LIST_CACHE.get(key);
            if (null == fieldList) {
                fieldList = new ArrayList<>();
                Collections.addAll(fieldList, cls.getDeclaredFields());
                if (!isContainsStatic) {
                    fieldList = fieldList.stream().filter(f -> !isStatic(f)).collect(Collectors.toList());
                }
                if (!isFinal) {
                    fieldList = fieldList.stream().filter(f -> !isFinal(f)).collect(Collectors.toList());
                }
                if (containsParentPrivateField) {
                    fieldList.addAll(getFields(cls.getSuperclass(), isContainsStatic, isFinal, false, containsParentPrivateField));
                }
                DATA_FILED_LIST_CACHE.set(key, fieldList);
            }
        } else {
            fieldList = Arrays.asList(cls.getDeclaredFields());
        }
        return fieldList;
    }

    /**
     * 返回一个类下的所有属性
     *
     * @param cls              类
     * @param isContainsStatic 是否包含静态属性
     * @param isFinal          是否包含 final 属性
     * @param isUseCache       是否将结果缓存
     * @return 属性列表
     */
    static List<Field> getFields(Class<?> cls, boolean isContainsStatic, boolean isFinal, boolean isUseCache) {
        return getFields(cls, isContainsStatic, isFinal, isUseCache, false);
    }

    /**
     * 返回一个类下的所有属性，不包含静态属性
     *
     * @param cls 类
     * @return 属性列表
     */
    static List<Field> getFields(Class<?> cls) {
        return getFields(cls, false, false, true);
    }

    /**
     * 在此类,和其超类中寻找此属性
     *
     * @param cls           类
     * @param fieldName     属性名称
     * @param setAccessible 是否设置可访问
     * @param useCache      是否使用缓存
     * @return 字段对象，未找到时返回 null
     */
    static Field getField(Class<?> cls, String fieldName, boolean setAccessible, boolean useCache) {
        try {
            String key = cls.getName() + Constants.Symbol.semicolon + fieldName;
            Field field = null;
            if (useCache) {
                field = DATA_FILED_CACHE.get(key);
                if (field == null && !DATA_FILED_CACHE.containsKey(key)) {
                    field = cls.getDeclaredField(fieldName);
                    DATA_FILED_CACHE.set(key, field);
                }
            } else {
                // useCache=false 时不读写缓存，直接反射查找，保证拿到最新字段
                field = cls.getDeclaredField(fieldName);
            }
            if (null != field) {
                field.setAccessible(setAccessible);
            }
            return field;
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 在此类,和其超类中寻找此属性，默认使用缓存
     *
     * @param cls           类
     * @param fieldName     属性名称
     * @param setAccessible 是否设置可访问
     * @return 字段对象，未找到时返回 null
     */
    static Field getField(Class<?> cls, String fieldName, boolean setAccessible) {
        return getField(cls, fieldName, setAccessible, true);
    }

    /**
     * 在此类,和其超类中寻找此属性
     *
     * @param cls       类
     * @param fieldName 属性名称
     * @return 字段对象，未找到时返回 null
     */
    static Field getField(Class<?> cls, String fieldName) {
        return getField(cls, fieldName, false);
    }

    /**
     * 给属性设置值,会先尝试调用其 setter 方法,如果没有 setter 方法会直接给属性赋值
     * 支持 a.b.c 的链式调用取值; 基础数据类型属性需要手动传入参数
     *
     * @param isReadNotPublicField 如果 Field 属性不是 public,那么直接赋值可能会失败，设置是否读取非 public 的属性
     * @param obj                  此属性的对象实例
     * @param filedName            属性的名称
     * @param values               参数的值
     * @param parameterTypes       参数类型
     */
    static void setFieldValue(boolean isReadNotPublicField, Object obj, String filedName, Object[] values, Class<?>... parameterTypes) {
        if (obj == null) {
            return;
        }
        if (filedName.indexOf(".") > 0 && !filedName.endsWith(".")) {
            String[] fieldNameStrings = filedName.split("\\.", 2);
            String nextString = filedName.substring(fieldNameStrings[0].length() + 1);
            Object objTemp = getFieldValue(isReadNotPublicField, obj, fieldNameStrings[0], null, null);
            Class<?> clsTemp = null;
            if (objTemp == null) {
                try {
                    Field field = getField(obj.getClass(), fieldNameStrings[0]);
                    if (field != null) {
                        clsTemp = Class.forName(field.getType().getCanonicalName());
                        objTemp = clsTemp.newInstance();
                        setFieldValue(true, obj, fieldNameStrings[0], new Object[]{objTemp}, objTemp.getClass());
                    }
                } catch (Exception e) {
                    throw new BaseException(e);
                }
            }
            setFieldValue(isReadNotPublicField, objTemp, nextString, values, parameterTypes);
        } else {
            Field field = null;
            try {
                Method method = ReflectUtils.getMethod(obj.getClass(), ReflectUtils.getSetterName(filedName), parameterTypes);
                if (method != null) {
                    method.invoke(obj, values);
                } else {
                    field = getField(obj.getClass(), filedName);
                    if (isReadNotPublicField) {
                        field.setAccessible(true);
                    }
                    field.set(obj, values[0]);
                }
            } catch (Exception e) {
                throw new BaseException(e);
            }
        }
    }

    /**
     * 给属性设置值,会先尝试调用其 setter 方法,如果没有 setter 方法会直接给属性赋值
     * 支持 a.b.c 的链式调用取值; 基础数据类型属性需要手动传入参数
     * 会读取非 public 的属性
     *
     * @param obj            此属性的对象实例
     * @param filedName      属性的名称
     * @param values         参数的值
     * @param parameterTypes 参数类型
     */
    static void setFieldValue(Object obj, String filedName, Object[] values, Class<?>... parameterTypes) {
        setFieldValue(true, obj, filedName, values, parameterTypes);
    }

    /**
     * 给属性设置值,会先尝试调用其 setter 方法,如果没有 setter 方法会直接给属性赋值
     * 支持 a.b.c 的链式调用取值; 基础数据类型属性需要手动传入参数
     * 会读取非 public 的属性
     *
     * @param obj       此属性的对象实例
     * @param filedName 属性的名称
     * @param value     参数的值,通过此值来推断对象类型，不支持基础数据类型
     */
    static void setFieldValue(Object obj, String filedName, Object value) {
        Class<?> parameterType = null;
        if (null == value) {
            Field field = getField(obj.getClass(), filedName);
            if (null != field) {
                parameterType = field.getType();
            }
        } else {
            parameterType = value.getClass();
        }
        setFieldValue(true, obj, filedName, new Object[]{value}, parameterType);
    }

    /**
     * 取属性值,会先尝试调用其 getter 方法,如果没有 getter 方法会直接操作属性
     *
     * @param obj 此属性的对象实例
     * @return 如果没有找到属性会返回 null;
     */
    static Object getFieldValue(boolean isReadNotPublicField, Object obj, String filedName) {
        Object[] values = null;
        Class<?>[] parameterTypes = null;
        return getFieldValue(isReadNotPublicField, obj, filedName, values, parameterTypes);
    }

    /**
     * 取属性值,会先尝试调用其 getter 方法,如果没有 getter 方法会直接操作属性
     * 支持 a.b.c 对象链式属性调用
     *
     * @param obj            此属性的对象实例
     * @param values         参数的值
     * @param parameterTypes 参数类型
     * @return 如果没有找到属性会返回 null;
     */
    static Object getFieldValue(boolean isReadNotPublicField, Object obj, String filedName, Object[] values, Class<?>[] parameterTypes) {
        if (obj == null) {
            return null;
        }
        if (filedName.indexOf(".") > 0 && !filedName.endsWith(".")) {
            String[] fieldNameStrings = filedName.split("\\.", 2);
            Object objTemp = getFieldValue(isReadNotPublicField, obj, fieldNameStrings[0], null, null);
            return getFieldValue(isReadNotPublicField, objTemp, fieldNameStrings[1], values, parameterTypes);
        } else {
            Field field = null;
            try {
                // 此方法不需要参数，如：getName(),getAge()
                Method method = ReflectUtils.getMethod(obj.getClass(), ReflectUtils.getGetterName(filedName), parameterTypes);
                if (method != null) {
                    return method.invoke(obj);
                } else {
                    field = getField(obj.getClass(), filedName);
                    if (field == null) {
                        return null;
                    }
                    if (isReadNotPublicField) {
                        field.setAccessible(true);
                    }
                    return field.get(obj);
                }
            } catch (Exception e) {
                throw new BaseException(e);
            }
        }
    }

    /**
     * 判断字段是否是基础数据或者包装类型或者 Date 类型
     *
     * @param field 字段
     * @return 如果是基础数据或者包装类型或者 Date 类型返回 true，否则返回 false
     */
    static boolean isJavaBaseDataField(Field field) {
        return ReflectUtils.isJavaBaseDataClass(field.getType().getCanonicalName());
    }

    /**
     * 根据实体类名得到实体的所有属性名称
     *
     * @param objClass 实体类名,包含包名
     * @return 属性名称数组
     * @throws ClassNotFoundException 类不存在时抛出
     */
    static String[] getFieldNames(String objClass) throws ClassNotFoundException {
        String[] wageStrArray = null;
        if (objClass != null) {
            Class<?> cls = Class.forName(objClass);
            // 这里便是获得实体Bean中所有属性的方法
            List<Field> fields = getFields(cls, true, true, true, false);
            StringBuffer sb = new StringBuffer();
            // 这里不多说了
            for (int i = 0; i < fields.size(); i++) {
                sb.append(fields.get(i).getName());
                // 这是分割符 是为了去掉最后那个逗号
                // 比如 如果不去最后那个逗号 最后打印出来的结果是 "id,name,"
                // 去了以后打印出来的是 "id,name"
                if (i < fields.size() - 1) {
                    sb.append(",");
                }
            }
            // split(",");这是根据逗号来切割字符串使字符串变成一个数组
            wageStrArray = sb.toString().split(",");
            return wageStrArray;
        } else {
            return wageStrArray;
        }
    }

    /**
     * 将属性的值转换为一个数组
     *
     * @param f 字段数组
     * @param o 对象实例
     * @return 属性值数组
     * @throws Exception 反射取值异常时抛出
     */
    static Object[] fieldToValue(Field[] f, Object o) throws Exception {
        Object[] value = new Object[f.length];
        for (int i = 0; i < f.length; i++) {
            value[i] = f[i].get(o);
        }
        return value;
    }

    /**
     * 得到除开指定名称的属性列
     *
     * @param cls            类
     * @param exceptCoulumns 排除的属性名称
     * @return 属性名称列表
     */
    static List<String> getFieldNames(Class<?> cls, String... exceptCoulumns) {
        List<String> nameList = getBeanColumnNameList(cls);
        if (exceptCoulumns != null) {
            for (String s : exceptCoulumns) {
                nameList.remove(s);
            }
        }
        return nameList;
    }

    /**
     * 得到除开指定名称的属性列
     *
     * @param cls            类
     * @param exceptCoulumns 排除的属性名称
     * @return 属性名称列表
     */
    static List<String> getFieldNames(Class<?> cls, List<String> exceptCoulumns) {
        List<String> nameList = getBeanColumnNameList(cls);
        if (exceptCoulumns != null) {
            for (String s : exceptCoulumns) {
                nameList.remove(s);
            }
        }
        return nameList;
    }

    /**
     * 返回此类的列的属性名称,不包含静态属性
     *
     * @param cls 类
     * @return 属性名称列表
     */
    private static List<String> getBeanColumnNameList(Class<?> cls) {
        List<String> list = new ArrayList<String>();
        List<Field> fs = getFields(cls, false, true, true, false);
        for (Field field : fs) {
            field.setAccessible(true);
            list.add(field.getName());
        }
        return list;
    }

    /**
     * 返回此类中此名称的属性的类型, 如果不存在则返回 null
     *
     * @param cls       类
     * @param filedName 属性名称,支持 a.b.c 的方式
     * @return 属性类型，不存在则返回 null
     */
    static Class<?> getTypeByFieldName(Class<?> cls, String filedName) {
        try {
            if (filedName.indexOf(".") > 0 && !filedName.endsWith(".")) {
                String[] fieldNameStrings = filedName.split("\\.", 2);
                String nextString = filedName.substring(filedName.indexOf(fieldNameStrings[1]));
                Field field = cls.getDeclaredField(fieldNameStrings[0]);
                return getTypeByFieldName(field.getType(), nextString);
            } else {
                Field field = cls.getDeclaredField(filedName);
                return field.getType();
            }
        } catch (Exception e) {
            return null;
        }
    }
}
