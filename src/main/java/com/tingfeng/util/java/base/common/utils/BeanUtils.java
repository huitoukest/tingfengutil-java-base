package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.bean.tuple.Tuple2;
import com.tingfeng.util.java.base.common.exception.BaseException;
import com.tingfeng.util.java.base.common.helper.SimpleCacheHelper;
import com.tingfeng.util.java.base.common.inter.PropertyFunction;
import com.tingfeng.util.java.base.common.inter.returnfunction.Function2;
import com.tingfeng.util.java.base.common.utils.reflect.ReflectUtils;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author huitoukest
 * 做一个方法，可以将一个JavaBean风格对象的属性值拷贝到另一个对象的同名属性中 (如果不存在同名属性的就不拷贝）
 * @version 20180917
 **/
public class BeanUtils {
    private static final Log logger = LogFactory.getLog(BeanUtils.class);

    /**
     * 数量固定的属性资源缓存,K = 拷贝的目标类Class
     * BEAN_COPY_METHOD_CACHE = Map[拷贝的来源类Class,Map[属性名称,[来源对象的此属性读取方法,目标对象的此属性赋值方法]]];
     */
    private static SimpleCacheHelper<Class, Map<Class,Map<String, Tuple2<Method, Method>>>> BEAN_COPY_METHOD_CACHE = new SimpleCacheHelper<>(512);

    /**
     * 注意：不copy类型是final或者static的属性
     * 返回一个拷贝之后的新的数组,通过方法和属性赋值新建对象来达到赋值的目的,是浅复制;
     * 深度拷贝基础属性，浅拷贝数组和集合，优先通过getter和setter读取属性，如果没有getter和setter则直接读取属性
     *
     * @param sourceList
     * @param targetClass
     * @param exceptFields 需要排除的字段
     * @param <T>
     * @return
     */
    public static <T> List<T> copyListProperties(List<? extends Object> sourceList, Class<T> targetClass, String... exceptFields) {
        List<T> targetList = new ArrayList<T>();
        try {
            for (Object o : sourceList) {
                T t = targetClass.newInstance();
                copyProperties(t, o, exceptFields);
                targetList.add(t);
            }
        } catch (Exception e) {
            throw new BaseException(e);
        }
        return targetList;
    }

    /**
     * 注意：不copy类型是final或者static的属性
     * 默认copy符合java bean标准的属性
     *
     * @param target
     * @param source
     * @param exceptFields 对于来源对象中的某些属性不进行拷贝
     */
    public static void copyProperties(Object target, Object source, String... exceptFields) {
        copyProperties(target, source, true, exceptFields);
    }

    /**
     * copy bean的属性
     *
     * @param target
     * @param source
     * @param strictBeanCopyMode 是否采用严格的bean copy 模式,false = 会尝试copy 没有getter、setter的属性字段；
     *                           true = 仅仅 copy 符合bean标准的属性
     * @param exceptFields
     */
    public static void copyProperties(Object target, Object source, boolean strictBeanCopyMode, String... exceptFields) {
        if (strictBeanCopyMode) {
            List<String> list = null;
            if (exceptFields != null) {
                list = Arrays.asList(exceptFields);
            }
            copyProperties(target, source, null, null, list);
        } else {
            copyProperties(target, source, null, null, exceptFields);
        }
    }

    /**
     * 注意：
     * 1. 不copy类型是final或者static的属性
     * 2. 判断和过滤的优先级如下 exceptFields &gt; predicate &gt; mapper ;
     * 注意： 通过反射机制，效率较低，需要高性能请使用另一个通过PropertyDescriptor实现的copyProperties方法
     * 浅复制普通Bean对象,,通过方法和属性赋值新建对象来达到赋值的目的;如果对象中存在集合/数组那么会执行浅复制,其它非基础数据的对象,会执行深度复制,将会自动对它们也进行深度拷贝;
     * 如果存在setter方法,将优先使用setter和getter方法,如果不存在,那么直接使用属性操作;
     *
     * @param target       目标对象
     * @param source       源对象
     * @param predicate    传入源对象的 Tuple2[字段,字段值] ; 返回是否进行拷贝true or false; null 时不生效
     * @param mapper       传入源对象的 Tuple2[字段,字段值] ; 返回转换后的值，将使用此值拷贝到目标对象对应的字段中; null 时不生效
     * @param exceptFields 对于来源对象中的某些属性不进行拷贝； 优先级高于predicate
     */
    public static <T> void copyProperties(Object target, Object source, Predicate<Tuple2<Field, Object>> predicate, Function<Tuple2<Field, Object>, Object> mapper, String... exceptFields) {
        Class<?> sourceClz = source.getClass();
        Class<?> targetClz = target.getClass();
        // 得到Class对象所表征的类的所有属性(包括私有属性)
        List<Field> fieldsS = ReflectUtils.getFields(sourceClz, false, false, true, true);
        List<Field> fieldsT = ReflectUtils.getFields(targetClz, false, false, true, true);
        List<Field> fieldsSourceUse = fieldsS.stream()
                .filter(field -> !ArrayUtils.isContain(exceptFields, field.getName()))
                .collect(Collectors.toList());
        Map<String, Field> targetFieldMap = fieldsT.stream()
                .map(it -> {
                    it.setAccessible(true);
                    return it;
                })
                .collect(Collectors.toMap(it -> it.getName(), it -> it, (a, b) -> b));
        try {
            fieldsSourceUse.forEach(field -> {
                field.setAccessible(true);
                String fieldName = field.getName();
                Field targetField = targetFieldMap.get(fieldName);
                if (null != targetField) {
                    //即使value是null也拷贝
                    Object value = ReflectUtils.getFieldValue(true, source, fieldName, null, null);
                    Tuple2 tuple2 = null;
                    if (predicate == null || predicate.test(tuple2 = new Tuple2(field, value))) {
                        if (mapper != null) {
                            value = mapper.apply(tuple2);
                        }
                        Class<?> valueClass = targetField.getType();
                        if (value != null) {
                            valueClass = value.getClass();
                        }
                        ReflectUtils.setFieldValue(true, target, targetField.getName(), new Object[]{value}, valueClass);
                    }
                }
            });
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 注意：不copy类型是final或者static的属性
     * 默认copy符合java bean标准的属性
     *
     * @param target
     * @param source
     */
    public static void copyProperties(Object target, Object source) {
        String[] args = null;
        copyProperties(target, source, args);
    }

    /**
     * 通过遍历Bean属性的方式来初始化map,适用于bean属性很少的情况;
     * map中的key支持a.b.c的方式
     * 注意:这种方式不支持自我调用的bean(即a中存在a的引用的情况)的自动赋值;
     *
     * @param cls 目标类型
     * @param map map中保存的是当前对象的属性和值得键值对
     */
    public static <T> T getBeanByMap(Class<T> cls, Map<String, ?> map) {
        try {
            T t = cls.newInstance();
            return getBeanByMap(t, map);
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 通过遍历Bean属性的方式来初始化map,适用于bean属性很少的情况;
     * map中的key支持a.b.c的方式
     * 注意:这种方式不支持自我调用的bean(即a中存在a的引用的情况)的自动赋值;
     *
     * @param t   目标对象
     * @param map map中保存的是当前对象的属性和值得键值对
     */
    public static <T> T getBeanByMap(T t, Map<String, ?> map) {
        Set<String> keySet = map.keySet();
        for (String fieldName : keySet) {
            Object object = map.get(fieldName);
            try {
                Class<?> typeClass = ReflectUtils.getTypeByFieldName(t.getClass(), fieldName);
                Object valueObject = ObjectUtils.getObject(typeClass, object);
                if (valueObject == null) {
                    continue;
                }
                ReflectUtils.setFieldValue(true, t, fieldName, new Object[]{valueObject}, valueObject.getClass());
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("getBeanByMap error:", e);
                }
                continue;
            }
        }
        return t;
    }

    /**
     * 封装系统的Introspector#getBeanInfo，默认带有缓存
     *
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> BeanInfo getBeanInfo(Class<T> cls) {
        return ObjectUtils.getValue(null, () -> Introspector.getBeanInfo(cls));
    }

    /**
     * 通过Introspector机制来实现属性的拷贝，效率更高,但是要求符合Bean规范；走getter setter拷贝值;
     * 浅复制普通Bean对象
     *
     * @param target       目标对象
     * @param source       源对象
     * @param predicate    传入源对象的 Tuple2[字段名称,字段值 ] ; 返回是否进行拷贝true or false; null 时不生效
     * @param mapper       传入源对象的 Tuple2[字段名称,字段值 ] ; 返回转换后的值，将使用此值拷贝到目标对象对应的字段中; null 时不生效
     * @param exceptFields 对于来源对象中的某些属性不进行拷贝； 优先级高于predicate
     */
    public static <T> void copyProperties(Object target, Object source, Predicate<Tuple2<String, Object>> predicate, Function<Tuple2<String, Object>, Object> mapper, Collection<String> exceptFields) {
        Map<String, Tuple2<Method, Method>> map = getBeanCopyMethodMap(target, source, true);
        try {
            Set<String> exceptSet = null;
            if (exceptFields != null && !(exceptFields instanceof Set)) {
                exceptSet = exceptFields.stream().collect(Collectors.toSet());
            }
            Set<Map.Entry<String, Tuple2<Method, Method>>> entries = map.entrySet();
            for (Map.Entry<String, Tuple2<Method, Method>> entry : entries) {
                if (exceptSet != null && exceptSet.contains(entry.getKey())) {
                    continue;
                }
                Method srcMethod = entry.getValue().get_1();
                Object value = srcMethod.invoke(source, null);


                Tuple2<String, Object> tuple2 = null;
                if (predicate == null || predicate.test(tuple2 = new Tuple2<>(entry.getKey(), value))) {
                    if (mapper != null) {
                        value = mapper.apply(tuple2);
                    }
                    entry.getValue().get_2().invoke(target, value);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new BaseException(e);
        }
    }

    /**
     * 返回拷贝属性时，可用户的PropertyDescriptor成对描述
     * @param target
     * @param source
     * @param useCache 是否使用缓存 (缓存了bean的读写方法等描述对象)
     * @return
     */
    public static Map<String, Tuple2<Method, Method>> getBeanCopyMethodMap(Object target, Object source, boolean useCache) {
        Map<String, Tuple2<Method, Method>> map = null;
        if (useCache) {
            map = getWithBeanCopyByMultiKey(target.getClass(),source.getClass());
            if (map == null) {
                map = getBeanCopyPropertyDescriptorMap(target,source);
                putWithBeanCopyByMultiKey(target.getClass(),source.getClass(), map);
            }
        } else {
            map = getBeanCopyPropertyDescriptorMap(target,source);
        }
        return map;
    }

    private static Map<String, Tuple2<Method, Method>> getWithBeanCopyByMultiKey(Class targetClass,Class sourceClass){
        Map<Class, Map<String, Tuple2<Method, Method>>> classMapMap = BEAN_COPY_METHOD_CACHE.get(targetClass);
        if(classMapMap != null){
            return classMapMap.get(sourceClass);
        }
        return null;
    }

    private static synchronized void putWithBeanCopyByMultiKey(Class targetClass,Class sourceClass,Map<String, Tuple2<Method, Method>> methodMap){
        Map<Class, Map<String, Tuple2<Method, Method>>> classMapMap = BEAN_COPY_METHOD_CACHE.get(targetClass);
        if(classMapMap == null){
            classMapMap = new HashMap<>();
            BEAN_COPY_METHOD_CACHE.set(targetClass,classMapMap);
        }
        classMapMap.put(sourceClass,methodMap);
    }

    /**
     * 返回Bean copy 使用的 PropertyDescriptor
     * @param targetBean
     * @param sourceBean
     * @return Map[属性名称,[来源对象的此属性读取方法,目标对象的此属性赋值方法]]
     */
    private static Map<String, Tuple2<Method, Method>> getBeanCopyPropertyDescriptorMap(Object targetBean, Object sourceBean){
        try {
            BeanInfo sourceBeanInfo = Introspector.getBeanInfo(sourceBean.getClass());
            BeanInfo targetBeanInfo = Introspector.getBeanInfo(targetBean.getClass());
            Map<String, Method> sMap = Arrays.asList(sourceBeanInfo.getPropertyDescriptors()).stream()
                    .filter(it -> it.getReadMethod() != null)
                    .peek(it -> it.getReadMethod().setAccessible(true))
                    .collect(Collectors.toMap(it -> it.getName(), PropertyDescriptor::getReadMethod));
            Map<String, Method> tMap = Arrays.asList(targetBeanInfo.getPropertyDescriptors()).stream()
                    .filter(it -> it.getWriteMethod() != null)
                    .peek(it -> it.getWriteMethod().setAccessible(true))
                    .collect(Collectors.toMap(it -> it.getName(), it -> it.getWriteMethod()));
            //source的读方法和target的写方法做一个映射，并缓存
            Map<String, Tuple2<Method, Method>> sToTMap = sMap.keySet().stream()
                    .filter(it -> tMap.containsKey(it))
                    .collect(Collectors.toMap(it -> it, it -> new Tuple2(sMap.get(it), tMap.get(it))));
            return sToTMap;
        } catch (IntrospectionException e) {
            throw new BaseException(e);
        }
    }

    /**
     * 将一个java bean Obj转为Map.
     * null 属性会自动忽略.
     * @param obj
     * @param ignoreProperties 忽略的 对象Property 值, 例如 通过Lambda使用bean的get方法引用即可; 如  User::getId ; 如果不传则不使用
     * @param <T>              必须是标准的java bean.
     * @return
     */
    public static <T> Map<String, Object> toMap(T obj, PropertyFunction<T, ?>... ignoreProperties) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
            Set<String> columnNames = Arrays.asList(ignoreProperties).stream()
                    .map(LambdaUtils::getFieldName).collect(Collectors.toSet());
			columnNames.add("class");
            return Arrays.asList(descriptors).stream()
                    .filter(it -> !columnNames.contains(it.getName()))
                    .map(it -> {
                        try {
                            return new Tuple2<String, Object>(it.getName(), it.getReadMethod().invoke(obj));
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    })
					.filter(it -> null != it.get_2())
					.collect(Collectors.toMap(Tuple2::get_1, Tuple2::get_2));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 要求要符合javaBean规范
     * 通过getter方法的名字 拿到对应的 属性名称
     *
     * @param getterName bean 标准的getter 方法的名称
     * @return
     */
    public static String getFieldNameByGetter(String getterName) {
        if (getterName.startsWith("get")) {
            getterName = getterName.substring(3);
        } else if (getterName.startsWith("is")) {
            getterName = getterName.substring(2);
        }
        // 小写第一个字母
        return StringUtils.firstLetterToLower(getterName);
    }

    /**
     * 获取一个转换器，将输入的字符串数组转为一个bean
     * @param filedNames  需要转换的字段名称
     * @param beanCls 需要转为的目标对象的class， 必须是标准的java bean, 带有标准的getter 与 setter 方法
     * @param filedValueConverter  自定义的属性转换器：输入[当前字段名称，当前内容字符串], 返回转换后的对象; 传入 null 则不使用
     * @param <T>
     * @return
     */
    public static <T> Function<String[],T> createBeanConverter(String[] filedNames, Class<T> beanCls, Function2<Object,String,String> filedValueConverter){
        PropertyDescriptor[] propertyDescriptors;
        try {
            propertyDescriptors = Introspector.getBeanInfo(beanCls).getPropertyDescriptors();
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
        Map<String, PropertyDescriptor> beanFiledNameMap = Arrays.asList(propertyDescriptors)
                .stream()
                .collect(Collectors.toMap(PropertyDescriptor::getName, Function.identity()));
        Map<Integer, PropertyDescriptor> filedIndexMap = IntStream.range(0, filedNames.length)
                .mapToObj(index -> {
                    PropertyDescriptor propertyDescriptor = beanFiledNameMap.get(filedNames[index]);
                    if (propertyDescriptor == null || propertyDescriptor.getWriteMethod() == null) {
                        return null;
                    }
                    return new Tuple2<>(index,propertyDescriptor);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Tuple2::get_1, Tuple2::get_2));
        return contents -> {
            T bean;
            try {
                bean = beanCls.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            IntStream.range(0,contents.length)
                    .forEach(index -> {
                        PropertyDescriptor propertyDescriptor = filedIndexMap.get(index);
                        if(propertyDescriptor != null) {
                            Object filedValue = contents[index];
                            if (null != filedValueConverter) {
                                filedValue = filedValueConverter.run(propertyDescriptor.getName(), contents[index]);
                            } else {
                                filedValue = ObjectUtils.getObject(propertyDescriptor.getPropertyType(), contents[index]);
                            }
                            try {
                                propertyDescriptor.getWriteMethod().invoke(bean, filedValue);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
            return bean;
        };
    }
}
