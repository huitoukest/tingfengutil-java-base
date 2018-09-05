package com.tingfeng.util.java.base.common.utils.reflect;

import com.tingfeng.util.java.base.common.constant.ObjectType;
import com.tingfeng.util.java.base.common.constant.ObjectTypeString;
import com.tingfeng.util.java.base.common.exception.BaseException;
import com.tingfeng.util.java.base.common.helper.SimpleCacheHelper;
import com.tingfeng.util.java.base.common.utils.ObjectUtils;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author huitoukest
 * java反射的一些工具方法;
 */
public class ReflectUtils {
    private static Pattern needConvertFiled = Pattern.compile("^[_a-z][^A-Z]|[_a-z]$");
    private static SimpleCacheHelper<String,List<Field>> DATA_FILED_CACHE = new SimpleCacheHelper<>(100);

    /**
     * 是否是静态方法
     * @param cls            类名
     * @param methodName     方法名称
     * @param parameterTypes 每个参数的类型
     * @return
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static boolean isStaticMethod(Class<?> cls, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        try {
            method = cls.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new BaseException(e);
        }
        int modifiers = method.getModifiers();
        return Modifier.isStatic(modifiers);
    }

    /**
     * 是否是静态方法
     *
     * @param method
     * @return
     */
    public static boolean isStatic(Method method) {
        int modifiers = method.getModifiers();
        return Modifier.isStatic(modifiers);
    }

    /**
     * 是否是静态属性
     *
     * @param field
     * @return
     */
    public static boolean isStatic(Field field) {
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        return isStatic;
    }

    /**
     * 是否是final属性
     *
     * @param field
     * @return
     */
    public static boolean isFinal(Field field) {
        return java.lang.reflect.Modifier.isFinal(field.getModifiers());
    }

    /**
     * 是否是静态方法
     *
     * @param method
     * @return
     */
    public static boolean isFinal(Method method) {
        return java.lang.reflect.Modifier.isFinal(method.getModifiers());
    }

    /**
     * 判断这个类是不是java.lang/math/utils包中自带的类;
     *
     * @param clazz
     * @return
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
     * @param field
     * @return 如果此类是基础数据或者包装类型或者Date类型, 返回true;否则返回false; 如果cls为null,返回false;
     */
    public static boolean isJavaBaseDataField(Field field) {
        return isJavaBaseDataClass(field.getType().getCanonicalName());
    }

    /**
     * @param cls
     * @return 如果此类是基础数据或者包装类型或者Date类型, 返回true;否则返回false; 如果cls为null,返回false;
     */
    public static boolean isJavaBaseDataClass(Class<?> cls) {
        if (cls == null) return false;
        return isJavaBaseDataClass(cls.getCanonicalName());
    }

    /**
     * @param clsName
     * @return 如果此类是基础数据或者包装类型或者Date类型, 返回true;否则返回false; 如果cls为null,返回false;
     */
    public static boolean isJavaBaseDataClass(String clsName) {

        switch (clsName) {
            case ObjectTypeString.clsNameBoolean:
                return true;
            case ObjectTypeString.clsNameByte:
                return true;
            case ObjectTypeString.clsNameDate:
                return true;
            case ObjectTypeString.clsNameLong:
                return true;
            case ObjectTypeString.clsNameInteger:
                return true;
            case ObjectTypeString.clsNameFloat:
                return true;
            case ObjectTypeString.clsNameDouble:
                return true;
            case ObjectTypeString.clsNameShort:
                return true;
            case ObjectTypeString.clsNameString:
                return true;
            case ObjectTypeString.clsNameBaseBoolean:
                return true;
            case ObjectTypeString.clsNameBaseByte:
                return true;
            case ObjectTypeString.clsNameBaseDouble:
                return true;
            case ObjectTypeString.clsNameBaseFloat:
                return true;
            case ObjectTypeString.clsNameBaseInt:
                return true;
            case ObjectTypeString.clsNameBaseLong:
                return true;
            case ObjectTypeString.clsNameBaseShort:
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * 返回一个类下的所有属性
     *
     * @param cls
     * @param isContainsStatic 是否包含静态属性
     * @param isFinal          是否包含final属性
     * @param isUseCache 是否将结果缓存
     * @returnC
     */
    public static List<Field> getFields(Class<?> cls, boolean isContainsStatic, boolean isFinal,boolean isUseCache) {
        List<Field> fieldList = null;
        String key = cls.getCanonicalName() + isContainsStatic + isFinal;
        boolean cacheChange = false;
        if(isUseCache){
            fieldList = DATA_FILED_CACHE.get(key);
        }
        if(null == fieldList) {
            cacheChange = true;
            fieldList = new ArrayList<>();
            Collections.addAll(fieldList, cls.getDeclaredFields());
            if (!isContainsStatic) {
                fieldList = fieldList.stream().filter(f -> !isStatic(f)).collect(Collectors.toList());
            }
            if (!isFinal) {
                fieldList = fieldList.stream().filter(f -> !isFinal(f)).collect(Collectors.toList());
            }
        }
        if(isUseCache && cacheChange) {
            DATA_FILED_CACHE.set(key, fieldList);
        }
        return fieldList;
    }

    /**
     * 返回一个类下的所有属性，不包含静态属性
     *
     * @param cls
     * @return
     */
    public static List<Field> getFields(Class<?> cls) {
        return getFields(cls, false, false,true);
    }

    /**
     * 在此类,和其超类中寻找此属性
     *
     * @param cls
     * @param fieldName
     * @return
     */
    public static Field getField(Class<?> cls, String fieldName, boolean setAccessible) {
        try {
            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(setAccessible);
            return field;
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 在此类,和其超类中寻找此属性
     *
     * @param cls
     * @param fieldName
     * @return
     */
    public static Field getField(Class<?> cls, String fieldName) {
        return getField(cls, fieldName, false);
    }

    /**
     * 给属性设置值,会先尝试调用其setter方法,如果没有setter方法会直接给属性赋值
     * 支持a.b.c的链式调用取值; 基础数据类型属性需要手动传入参数
     *
     * @param isReadNotPublicField 如果Field属性不是public,那么直接赋值可能会失败，设置是否读取非public的属性
     * @param obj                  此属性的对象实例
     * @param filedName            属性的名称
     * @param values               参数的值
     * @param parameterTypes       参数类型
     * @return
     * @throws NoSuchFieldException
     * @throws InstantiationException
     */
    public static void setFieldValue(boolean isReadNotPublicField, Object obj, String filedName, Object[] values, Class<?>... parameterTypes) {
        if (obj == null) {
            return;
        }
        if (filedName.indexOf(".") > 0 && !filedName.endsWith(".")) {
            String[] fieldNameStrings = filedName.split("\\.", 2);
            String nextString = filedName.substring(fieldNameStrings[0].length() + 1);
            Object objTemp = getFieldValue(isReadNotPublicField, obj, fieldNameStrings[0], null);
            Class<?> clsTemp = null;
            if (objTemp == null) {
                try {
                    Field field = obj.getClass().getDeclaredField(fieldNameStrings[0]);
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
                try {
                    field = obj.getClass().getDeclaredField(filedName);
                    Method method = obj.getClass().getDeclaredMethod(getSetterName(field.getName()), parameterTypes);
                    method.invoke(obj, values);
                } catch (NoSuchMethodException e) {
                    if (isReadNotPublicField) {
                        field.setAccessible(true);
                    }
                    field.set(obj, values[0]);
                }
            }catch (Exception e){
                throw  new BaseException(e);
            }
        }
    }

    /**
     * 给属性设置值,会先尝试调用其setter方法,如果没有setter方法会直接给属性赋值
     * 支持a.b.c的链式调用取值; 基础数据类型属性需要手动传入参数
     * 会读取非public的属性
     * @param obj                  此属性的对象实例
     * @param filedName            属性的名称
     * @param values               参数的值
     * @param parameterTypes       参数类型
     * @return
     * @throws NoSuchFieldException
     * @throws InstantiationException
     */
    public static void setFieldValue(Object obj, String filedName, Object[] values, Class<?>... parameterTypes){
        setFieldValue(true,obj,filedName,values,parameterTypes);
    }

    /**
     * 给属性设置值,会先尝试调用其setter方法,如果没有setter方法会直接给属性赋值
     * 支持a.b.c的链式调用取值; 基础数据类型属性需要手动传入参数
     * 会读取非public的属性,通过
     * @param obj                  此属性的对象实例
     * @param filedName            属性的名称
     * @param value              参数的值,通过此值来推断对象类型，不支持基础数据类型
     * @return
     * @throws NoSuchFieldException
     * @throws InstantiationException
     */
    public static void setFieldValue(Object obj, String filedName, Object value){
           Class<?> parameterType = null;
           if(null == value){
               Field field = getField(obj.getClass(),filedName);
               if(null != field){
                   parameterType = field.getType();
               }
           }else {
               parameterType = value.getClass();
           }
           setFieldValue(true,obj,filedName,new Object[]{value},parameterType);
    }

    /**
     * 取属性值,会先尝试调用其getter方法,如果没有getter方法会直接给操作属性
     *
     * @param obj            此属性的对象实例
     * @param values         参数的值
     * @param parameterTypes 参数类型
     * @return 如果没有找到属性会返回null;
     */
    public static Object getFieldValue(boolean isReadNotPublicField, Object obj, String filedName, Object[] values, Class<?>... parameterTypes) {
        if (obj == null) {
            return null;
        }
        if (filedName.indexOf(".") > 0 && !filedName.endsWith(".")) {
            String[] fieldNameStrings = filedName.split("\\.", 2);
            Object objTemp = getFieldValue(isReadNotPublicField, obj, fieldNameStrings[0], null);
            return getFieldValue(isReadNotPublicField, objTemp, fieldNameStrings[1], values, parameterTypes);
        } else {
            Field field = null;
            try {
                field = obj.getClass().getDeclaredField(filedName);
            } catch (java.lang.NoSuchFieldException e) {
                return null;
            }
            try {
                try{
                    Method method = obj.getClass().getDeclaredMethod(getGetterName(filedName), parameterTypes);// 此方法不需要参数，如：getName(),getAge()
                    if (method != null) {
                        return method.invoke(obj);
                    }else{
                        if (isReadNotPublicField) {
                            field.setAccessible(true);
                        }
                        return field.get(obj);
                    }
                }catch (NoSuchMethodException e){
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
     * 通过反射来调用犯法
     *
     * @param obj
     * @param methodName
     * @return
     * @throws NoSuchMethodException
     */
    public static Object invokeMethod(Object obj, String methodName) throws NoSuchMethodException {
        Class<?>[] classes = null;
        return invokeMethod(obj, methodName, null, classes);
    }

    /**
     * 通过反射来调用一个方法
     *
     * @param obj
     * @param methodName
     * @param params
     * @param parameterTypes 参数类型
     * @return
     */
    public static Object invokeMethod(Object obj, String methodName, Object[] params, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = obj.getClass().getDeclaredMethod(methodName, parameterTypes);
        try {
            method.setAccessible(true);
            return method.invoke(obj, params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new BaseException(e);
        }
    }

    /**
     * 通过反射调用指定getter方法,成功返回true,否则返回false;
     *
     * @param obj
     * @param attr  属性名称,如name
     * @param value 参数的值
     * @param type  参数的类型
     * @return 设置成功返回true，失败返回false
     */
    public static boolean setter(Object obj, String attr, Object value, Class<?> type) {
        try {
            String methodName = getSetterName(attr);
            // 第一个参数表示方法名称，setAge、setName,第二个参数表示类型，如int.class,String.class
            Method method = obj.getClass().getDeclaredMethod(methodName, type);
            if (method == null) {
                return false;
            }
            method.invoke(obj, value);// 调用方法
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 通过反射调用指定getter方法,成功返回true,否则返回false;
     *
     * @param obj
     * @param attr  属性名称,如name
     * @param value 参数的值
     * @return 设置成功返回true，失败返回false
     */
    public static boolean setter(Object obj, String attr, Object value) {
        Class<?> type = getField(obj.getClass(), attr).getType();
        return setter(obj, attr, value, type);
    }

    /**
     * 通过反射调用指定getter方法,成功返回相应的值,否则返回null;
     *
     * @param obj
     * @param attr 属性名称,如name
     * @return
     */
    public static Object getter(Object obj, String attr) {// 调用getter方法
        try {
            Method method = obj.getClass().getDeclaredMethod(getGetterName(attr));// 此方法不需要参数，如：getName(),getAge()
            if (method != null) {
                return method.invoke(obj);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * fieldName 是当前属性的名称
     * <br/>
     * 1、如果属性名的第二个字母大写，那么该属性名直接用作 getter/setter 方法中 get/set 的后部分，就是说大小写不变。例如属性名为uName，方法是getuName/setuName。
     * <p>
     * 2、如果属性名的前两个字母是大写（一般的专有名词和缩略词都会大写），也是属性名直接用作 getter/setter 方法中 get/set 的后部分。例如属性名为URL，方法是getURL/setURL。
     * <p>
     * 3、如果属性名的首字母大写，也是属性名直接用作 getter/setter 方法中 get/set 的后部分。例如属性名为Name，方法是getName/setName，这种是最糟糕的情况，会找不到属性出错，因为默认的属性名是name。
     * <p>
     * 4、如果属性名以"is"开头，则getter方法会省掉get，set方法会去掉is。例如属性名为isOK，方法是isOK/setOK。
     *
     * @param fieldName
     * @return
     */
    public static String getGetterName(String fieldName) {// 单词首字母大写
        boolean hasIs = fieldName.startsWith("is");
        String str = formateGetterOrSetterFieldName(fieldName);
        if (hasIs) {
            str = "is" + str;
        } else {
            str = "get" + str;
        }

        return str;
    }

    /**
     * fieldName 是当前属性的名称
     * <br/>
     * 1、如果属性名的第二个字母大写，那么该属性名直接用作 getter/setter 方法中 get/set 的后部分，就是说大小写不变。例如属性名为uName，方法是getuName/setuName。
     * <p>
     * 2、如果属性名的前两个字母是大写（一般的专有名词和缩略词都会大写），也是属性名直接用作 getter/setter 方法中 get/set 的后部分。例如属性名为URL，方法是getURL/setURL。
     * <p>
     * 3、如果属性名的首字母大写，也是属性名直接用作 getter/setter 方法中 get/set 的后部分。例如属性名为Name，方法是getName/setName，这种是最糟糕的情况，会找不到属性出错，因为默认的属性名是name。
     * <p>
     * 4、如果属性名以"is"开头，则getter方法会省掉get，set方法会去掉is。例如属性名为isOK，方法是isOK/setOK。
     *
     * @param fieldName
     * @return
     */
    public static String getSetterName(String fieldName) {// 单词首字母大写
        boolean hasIs = fieldName.startsWith("is");
        String str = formateGetterOrSetterFieldName(fieldName);
        if (!hasIs) {
            str = "set" + str;
        }
        return str;
    }

    /**
     * fieldName 是当前属性的名称
     * <br/>
     * 1、如果属性名的第二个字母大写，那么该属性名直接用作 getter/setter 方法中 get/set 的后部分，就是说大小写不变。例如属性名为uName，方法是getuName/setuName。
     * <p>
     * 2、如果属性名的前两个字母是大写（一般的专有名词和缩略词都会大写），也是属性名直接用作 getter/setter 方法中 get/set 的后部分。例如属性名为URL，方法是getURL/setURL。
     * <p>
     * 3、如果属性名的首字母大写，也是属性名直接用作 getter/setter 方法中 get/set 的后部分。例如属性名为Name，方法是getName/setName，这种是最糟糕的情况，会找不到属性出错，因为默认的属性名是name。
     * <p>
     * 4、如果属性名以"is"开头，则getter方法会省掉get，set方法会去掉is。例如属性名为isOK，方法是isOK/setOK。
     * 即->只有前两位字母都是小写，或者只有一位小写字母时，首字母大写
     * @param fieldName
     * @return
     */
    private static String formateGetterOrSetterFieldName(String fieldName){
        if(needConvertFiled.matcher(fieldName).find()){
            fieldName = StringUtils.toUpperFirstChar(fieldName);
        }
        return fieldName;
    }

    /**
     * 根据实体得到实体的所有属性
     *
     * @param objClass
     * @return
     * @throws ClassNotFoundException
     */
    public static String[] getFieldNames(String objClass) throws ClassNotFoundException {
        String[] wageStrArray = null;
        if (objClass != null) {
            Class<?> class1 = Class.forName(objClass);
            Field[] field = class1.getDeclaredFields();// 这里便是获得实体Bean中所有属性的方法
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < field.length; i++) {// 这里不多说了

                sb.append(field[i].getName());

                // 这是分割符 是为了去掉最后那个逗号

                // 比如 如果不去最后那个逗号 最后打印出来的结果是 "id,name,"

                // 去了以后打印出来的是 "id,name"
                if (i < field.length - 1) {
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
     * @param f
     * @param o
     * @return
     * @throws Exception
     */
    public static Object[] fieldToValue(Field[] f, Object o) throws Exception {
        Object[] value = new Object[f.length];
        for (int i = 0; i < f.length; i++) {
            value[i] = f[i].get(o);
        }
        return value;
    }

    /**
     * 得到实体类
     *
     * @param objClass 实体类包含包名
     * @return
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

    /**
     * 得到除开指定名称的属性列
     */
    public static List<String> getFieldNames(Class<?> cls, String... exceptCoulumns) {
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
     */
    public static List<String> getFieldNames(Class<?> cls, List<String> exceptCoulumns) {
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
     * @param cls
     * @return
     */
    private static List<String> getBeanColumnNameList(Class<?> cls) {
        List<String> list = new ArrayList<String>();
        Class<?> clazz = cls;
        Field[] fs = clazz.getDeclaredFields();
        for (Field field : fs) {
            boolean isStatic = Modifier.isStatic(field.getModifiers());
            if (isStatic)
                continue;
            field.setAccessible(true);
            list.add(field.getName());
        }
        return list;
    }

    /**
     * @param cls
     * @param filedName 支持a.b.c的方式
     * @return 返回此类中此名称的属性的类型, 如果不存在则返回null
     */
    public static Class<?> getTypeByFieldName(Class<?> cls, String filedName) {
        try {
            if (filedName.indexOf(".") > 0 && !filedName.endsWith(".")) {
                String[] fieldNameStrings = filedName.split("\\.",2);
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
