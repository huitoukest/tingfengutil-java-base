package com.tingfeng.util.java.base.bean.copier;

import com.tingfeng.util.java.base.cache.SimpleCacheHelper;
import com.tingfeng.util.java.base.lang.exception.BaseException;
import com.tingfeng.util.java.base.lang.support.ReflectUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean属性描述缓存，对传入的Class执行内省并建立属性读写映射。
 * <p>
 * 对同一个Class只内省一次，结果通过SimpleCacheHelper缓存（容量512）。
 *
 * @author huitoukest
 */
public class BeanDesc {

    /** BeanDesc实例缓存，容量512 */
    private static final SimpleCacheHelper<Class<?>, BeanDesc> CACHE =
            new SimpleCacheHelper<>(512);

    private static final Logger log = LoggerFactory.getLogger(BeanDesc.class);

    /** 有getter/setter的属性描述映射 */
    private final Map<String, PropertyDescriptor> pdMap;

    /** 所有属性映射（含父类私有，不含static/final） */
    private final Map<String, Field> fieldMap;

    /** 缓存的所有属性名称集合（pdMap ∪ fieldMap） */
    private final Set<String> allPropertyNames;

    /** 缓存的所有Field属性名称集合（仅fieldMap） */
    private final Set<String> allFieldNames;

    /** 仅当前类属性名称集合（不含父类） */
    private final Set<String> currentClassPropertyNames;

    /**
     * 获取指定Class的BeanDesc实例（带缓存）
     *
     * @param clazz 要描述的类
     * @return BeanDesc实例
     */
    public static BeanDesc getInstance(Class<?> clazz) {
        BeanDesc desc = CACHE.get(clazz);
        if (desc == null) {
            desc = new BeanDesc(clazz);
            CACHE.set(clazz, desc);
        }
        return desc;
    }

    /**
     * 私有构造器，执行内省并建立属性映射
     *
     * @param clazz 要内省的类
     */
    private BeanDesc(Class<?> clazz) {
        this.pdMap = new ConcurrentHashMap<>();
        this.fieldMap = new ConcurrentHashMap<>();

        introspect(clazz);

        Set<String> allNames = new HashSet<>(pdMap.keySet());
        allNames.addAll(fieldMap.keySet());
        this.allPropertyNames = Collections.unmodifiableSet(allNames);
        this.allFieldNames = Collections.unmodifiableSet(fieldMap.keySet());
        this.currentClassPropertyNames = Collections.unmodifiableSet(
                collectCurrentClassPropertyNames(clazz));
    }

    /**
     * 收集仅属于当前类的属性名
     *
     * @param clazz 当前类
     * @return 仅当前类属性的名称集合
     */
    private Set<String> collectCurrentClassPropertyNames(Class<?> clazz) {
        Set<String> currentNames = new HashSet<>();

        // 从 pdMap 中筛选出声明类为当前类的属性
        for (Map.Entry<String, PropertyDescriptor> entry : pdMap.entrySet()) {
            PropertyDescriptor pd = entry.getValue();
            Class<?> declaringClass = getDeclaringClass(pd);
            if (declaringClass == clazz) {
                currentNames.add(entry.getKey());
            }
        }

        // 从 fieldMap 中筛选出声明类为当前类的字段
        for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
            if (entry.getValue().getDeclaringClass() == clazz) {
                currentNames.add(entry.getKey());
            }
        }

        return currentNames;
    }

    /**
     * 获取 PropertyDescriptor 的声明类
     * <p>
     * 通过 readMethod 或 writeMethod 的 declaring class 来判断
     */
    private Class<?> getDeclaringClass(PropertyDescriptor pd) {
        if (pd.getReadMethod() != null) {
            return pd.getReadMethod().getDeclaringClass();
        }
        if (pd.getWriteMethod() != null) {
            return pd.getWriteMethod().getDeclaringClass();
        }
        return null;
    }

    /**
     * 执行内省：解析PropertyDescriptor和Field
     */
    private void introspect(Class<?> clazz) {
        // 1. 通过Introspector获取有getter/setter的属性
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
            if (pds != null) {
                for (PropertyDescriptor pd : pds) {
                    // 过滤掉class属性（来自Introspector的默认属性）
                    if (!"class".equals(pd.getName())) {
                        pdMap.put(pd.getName(), pd);
                    }
                }
            }
        } catch (IntrospectionException e) {
            throw new BaseException(e);
        }

        // 2. 通过ReflectUtils获取所有Field（含父类私有，不含static/final）
        java.util.List<Field> fields = ReflectUtils.getFields(clazz, false, false, true, true);
        for (Field field : fields) {
            fieldMap.put(field.getName(), field);
        }
    }

    /**
     * 获取所有可读属性的名称集合（pdMap ∪ fieldMap的key集合）
     *
     * @return 属性名称集合
     */
    public Set<String> getPropertyNames() {
        return allPropertyNames;
    }

    /**
     * 获取所有Field属性的名称集合（仅fieldMap的key集合）
     *
     * @return Field属性名称集合
     */
    public Set<String> getFieldNames() {
        return allFieldNames;
    }

    /**
     * 获取仅当前类声明的属性名称集合（不含父类属性）。
     * <p>
     * 用于在 copySuperclassProperties=false 时限制拷贝范围。
     *
     * @return 仅当前类属性名称集合
     */
    public Set<String> getCurrentClassPropertyNames() {
        return currentClassPropertyNames;
    }

    /**
     * 根据属性名获取实际属性名（大小写不敏感匹配）。
     * <p>
     * 先尝试精确匹配，若不存在则遍历所有属性名进行大小写不敏感比较。
     *
     * @param name 要查找的属性名
     * @return 实际属性名，若未找到则返回null
     */
    public String getPropertyNameIgnoreCase(String name) {
        if (name == null) {
            return null;
        }
        // 先尝试精确匹配
        if (pdMap.containsKey(name) || fieldMap.containsKey(name)) {
            return name;
        }
        // 大小写不敏感遍历
        String lowerName = name.toLowerCase();
        for (String propertyName : getPropertyNames()) {
            if (propertyName.toLowerCase().equals(lowerName)) {
                return propertyName;
            }
        }
        return null;
    }

    /**
     * 判断属性是否有PropertyDescriptor模式（有getter/setter）
     *
     * @param name 属性名称
     * @return 是否有PD模式
     */
    public boolean hasPropertyDescriptor(String name) {
        return pdMap.containsKey(name);
    }

    /**
     * 获取指定属性名的类型。
     * <p>
     * 先尝试从 PropertyDescriptor 获取，若无则从 Field 获取。
     *
     * @param propName 属性名
     * @return 属性类型，若不存在则返回 null
     */
    public Class<?> getPropertyType(String propName) {
        PropertyDescriptor pd = pdMap.get(propName);
        if (pd != null) {
            return pd.getPropertyType();
        }
        Field field = fieldMap.get(propName);
        if (field != null) {
            return field.getType();
        }
        return null;
    }

    /**
     * 获取属性值：先尝试PD.getReadMethod.invoke，失败则降级使用field.get
     *
     * @param bean 要读取的bean实例
     * @param name 属性名称
     * @return 属性值结果，包含属性值和访问模式
     */
    public PropertyResult<Object> getPropertyValue(Object bean, String name) {
        PropertyDescriptor pd = pdMap.get(name);
        if (pd != null && pd.getReadMethod() != null) {
            try {
                return PropertyResult.of(pd.getReadMethod().invoke(bean), PropertyAccessMode.GETTER_METHOD);
            } catch (Exception e) {
                log.debug("get property via method failed: {}, fallback to field", name, e);
            }
        }

        Field field = fieldMap.get(name);
        if (field != null) {
            try {
                field.setAccessible(true);
                return PropertyResult.of(field.get(bean), PropertyAccessMode.FIELD_ACCESS);
            } catch (IllegalAccessException e) {
                // 降级到下一个
            }
        }

        return PropertyResult.notFound();
    }

    /**
     * 根据属性名获取对应的 Field。
     *
     * @param propName 属性名
     * @return 对应的 Field，若不存在则返回 null
     */
    public Field getField(String propName) {
        return fieldMap.get(propName);
    }

    /**
     * 设置属性值：先尝试PD.getWriteMethod.invoke，失败则降级使用field.set
     *
     * @param bean 要写入的bean实例
     * @param name 属性名称
     * @param value 要设置的值
     * @return 是否设置成功
     */
    public boolean setPropertyValue(Object bean, String name, Object value) {
        PropertyDescriptor pd = pdMap.get(name);
        if (pd != null && pd.getWriteMethod() != null) {
            try {
                pd.getWriteMethod().invoke(bean, value);
                return true;
            } catch (Exception e) {
                log.debug("set property via method failed: {}, fallback to field", name, e);
            }
        }

        Field field = fieldMap.get(name);
        if (field != null) {
            try {
                field.setAccessible(true);
                field.set(bean, value);
                return true;
            } catch (IllegalAccessException e) {
                // 保留IllegalAccessException包装，不阻断流程
            }
        }

        return false;
    }
}