package com.tingfeng.util.java.base.bean.copier;

import com.tingfeng.util.java.base.bean.BeanUtils;
import com.tingfeng.util.java.base.bean.converter.ConditionConverter;
import com.tingfeng.util.java.base.bean.converter.Converter;
import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.cache.SimpleCacheHelper;
import com.tingfeng.util.java.base.lang.base.UnionKey;
import com.tingfeng.util.java.base.lang.exception.BaseException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Bean属性拷贝器，支持选项化配置。
 *
 * 核心方法 {@link #copy(Object, Object, CopyOptions)} 执行浅拷贝，流程：
 * - 获取 source/target 的 BeanDesc（带缓存）
 * - 遍历 target 可写属性，通过 fieldMapping/ignoreProperties 等选项过滤
 * - 从 source 取值，null判断，类型转换，写入 target
 *
 * @author huitoukest
 */
@Slf4j
public class BeanCopier {

    /** BeanDesc 缓存，key = UnionKey(class, class)，容量512 */
    private static final SimpleCacheHelper<UnionKey, BeanDesc> BEAN_DESC_CACHE =
            new SimpleCacheHelper<>(512);

    /** 嵌套转换最大深度，防止无限递归 */
    private static final int MAX_NESTED_DEPTH = 10;

    /**
     * 拷贝 bean 属性（浅拷贝）
     *
     * 前置检查：
     * - source == null 或 target == null → 直接 return（不抛异常）
     *
     * @param source  源对象
     * @param target 目标对象
     * @param options 拷贝选项（可为 null，使用默认选项）
     */
    public static void copy(Object source, Object target, CopyOptions options) {
        // 前置检查
        if (source == null || target == null) {
            return;
        }

        // 补全默认选项
        if (options == null) {
            options = CopyOptions.create();
        }

        // 获取 BeanDesc（带缓存）
        BeanDesc sourceDesc = getOrCreateBeanDesc(source.getClass());
        BeanDesc targetDesc = getOrCreateBeanDesc(target.getClass());

        // 获取 target 可写属性名集合
        Set<String> targetPropertyNames;
        if (options.isForceFieldAccess()) {
            // 仅使用 fieldMap：通过反射获取
            targetPropertyNames = getFieldMapKeySet(targetDesc);
        } else if (options.isCopySuperclassProperties()) {
            // 使用所有属性名（pdMap ∪ fieldMap）
            targetPropertyNames = targetDesc.getPropertyNames();
        } else {
            // 仅使用当前类属性名（不含父类）
            targetPropertyNames = targetDesc.getCurrentClassPropertyNames();
        }

        // 创建 source 值提供者
        ValueProvider<Object> sourceValueProvider = new SourceValueProvider(source, sourceDesc);

        // 提取公共拷贝逻辑
        copyProperties(sourceValueProvider, options, target, targetDesc, targetPropertyNames,
                source.getClass(), sourceDesc);
    }

    /**
     * 从 ValueProvider 拷贝属性到 target Bean（浅拷贝）
     *
     * 与 {@link #copy(Object, Object, CopyOptions)} 的区别：
     * - 取值来源：copy() 从 source.getPropertyValue() 取值；copyFromProvider() 从 provider.value() 取值
     * - source 不存在属性：copy() 通过 try-catch 跳过；copyFromProvider() 通过 provider.containsKey() 检查
     *
     * 前置检查：
     * - provider == null 或 target == null → 直接 return（不抛异常）
     *
     * @param provider 值提供者
     * @param target   目标对象
     * @param options   拷贝选项（可为 null，使用默认选项）
     */
    public static void copyFromProvider(ValueProvider<?> provider, Object target, CopyOptions options) {
        // 前置检查
        if (provider == null || target == null) {
            return;
        }

        // 补全默认选项
        if (options == null) {
            options = CopyOptions.create();
        }

        // 获取 target 的 BeanDesc（带缓存）
        BeanDesc targetDesc = getOrCreateBeanDesc(target.getClass());

        // 获取 target 可写属性名集合
        Set<String> targetPropertyNames;
        if (options.isForceFieldAccess()) {
            // 仅使用 fieldMap：通过反射获取
            targetPropertyNames = getFieldMapKeySet(targetDesc);
        } else if (options.isCopySuperclassProperties()) {
            // 使用所有属性名（pdMap ∪ fieldMap）
            targetPropertyNames = targetDesc.getPropertyNames();
        } else {
            // 仅使用当前类属性名（不含父类）
            targetPropertyNames = targetDesc.getCurrentClassPropertyNames();
        }

        // 提取公共拷贝逻辑
        @SuppressWarnings("unchecked")
        ValueProvider<Object> typedProvider = (ValueProvider<Object>) provider;
        copyProperties(typedProvider, options, target, targetDesc, targetPropertyNames,
                target.getClass(), null);
    }

    /**
     * 公共属性拷贝逻辑，抽象了 copy() 和 copyFromProvider() 的内层循环
     *
     * @param valueProvider       取值函数接口
     * @param options             拷贝选项
     * @param target              目标对象
     * @param targetDesc          目标 BeanDesc
     * @param targetPropertyNames 目标属性名集合
     * @param sourceType          源类型（用于预缓存查询）
     * @param sourceDesc          源 BeanDesc（copyFromProvider 时为 null）
     */
    private static void copyProperties(
            ValueProvider<Object> valueProvider,
            CopyOptions options,
            Object target,
            BeanDesc targetDesc,
            Set<String> targetPropertyNames,
            Class<?> sourceType,
            BeanDesc sourceDesc) {

        // 获取配置
        Set<String> ignoreProperties = options.getIgnoreProperties();
        boolean ignoreNull = options.isIgnoreNull();
        boolean useConverter = options.isUseConverter();
        boolean ignoreNoMatchConverterError = options.isIgnoreNoMatchConverterError();
        Map<String, String> fieldMapping = options.getFieldMapping();

        // 构建 source 小写名→原名映射（ignoreCase 时使用，仅 copy() 有效）
        Map<String, String> sourceLowerNameMap = null;
        if (sourceDesc != null && options.isIgnoreCase()) {
            sourceLowerNameMap = buildLowerCaseNameMap(sourceDesc.getPropertyNames());
        }

        // 遍历 target 属性
        for (String propName : targetPropertyNames) {
            // a. 忽略列表检查
            if (ignoreProperties != null && ignoreProperties.contains(propName)) {
                continue;
            }

            // b. 字段映射
            String sourceFieldName = propName;
            if (fieldMapping != null) {
                sourceFieldName = fieldMapping.getOrDefault(propName, propName);
            }

            // c. 获取目标属性类型（用于同类型匹配）
            Class<?> targetPropertyType = getTargetPropertyType(targetDesc, propName);

            // d. sourceDesc != null 时走 resolveSourceFieldName，sourceDesc == null 时直接用 sourceFieldName
            if (sourceDesc != null) {
                sourceFieldName = resolveSourceFieldName(sourceDesc, sourceFieldName, sourceLowerNameMap, targetPropertyType);
                if (sourceFieldName == null) {
                    // 无匹配 → 跳过
                    continue;
                }
            }
            // e. source 取值检查（两条路径均生效）
            if (!valueProvider.containsKey(sourceFieldName)) {
                continue;
            }

            // f. 从 provider 取值
            Object value = valueProvider.value(sourceFieldName, targetPropertyType);

            // e. null 判断
            if (value == null && ignoreNull) {
                continue;
            }

            // f. 类型转换（预缓存优化）
            if (value != null) {
                if (targetPropertyType != null && !value.getClass().equals(targetPropertyType) && useConverter) {
                    // 1. 优先查找临时 Converter
                    List<Converter<?, ?>> customConverterList = options.getCustomConverters();
                    Converter<?, ?> customConverter = null;
                    if (!customConverterList.isEmpty()) {
                        customConverter = findCustomConverter(customConverterList, value, targetPropertyType);
                    }

                    if (customConverter != null) {
                        // 临时 Converter 命中，直接转换（异常向上传播）
                        value = ((Converter) customConverter).convert(value);
                    } else {
                        // 2. 临时 Converter 未命中，尝试嵌套递归转换
                        Object originalValue = value;
                        value = tryNestedConversion(value, targetPropertyType, propName, targetDesc, options, 1);
                        // 检查嵌套转换是否成功（如果 value 类型变化了，说明嵌套转换生效）
                        boolean nestedConversionWorked = !originalValue.getClass().equals(value.getClass());
                        // 3. 嵌套递归转换未命中，fallback 到全局 ConverterRegistry
                        if (!nestedConversionWorked) {
                            // 嵌套转换未生效，尝试全局转换
                            boolean converterExists = !ConverterRegistry.getInstance()
                                    .findConverters(value.getClass(), targetPropertyType).isEmpty();
                            try {
                                @SuppressWarnings("unchecked")
                                Object convertedValue = ConverterRegistry.getInstance()
                                        .convert(value, (Class<Object>) targetPropertyType, value);
                                value = convertedValue;
                            } catch (Exception e) {
                                // 转换失败
                                if (converterExists) {
                                    // 场景A：有Converter但转换失败 → 直接抛异常，不受 ignoreNoMatchConverterError 控制
                                    throw new BaseException(
                                            buildConversionErrorMessage(propName, value, targetPropertyType), e);
                                }
                                // 场景B：无Converter类型不匹配 → 受 ignoreNoMatchConverterError 控制
                                if (!ignoreNoMatchConverterError) {
                                    throw new BaseException(
                                            buildConversionErrorMessage(propName, value, targetPropertyType), e);
                                }
                                // ignoreNoMatchConverterError=true → 跳过该属性
                                if (log.isDebugEnabled()) {
                                    log.debug(buildConversionErrorMessage(propName, value, targetPropertyType) + ", skipping", e);
                                }
                                continue;
                            }
                        }
                    }
                }
            }

            // g. 赋值
            boolean setSuccess = targetDesc.setPropertyValue(target, propName, value);
            if (!setSuccess) {
                // 赋值失败 → 跳过该属性
                if (log.isDebugEnabled()) {
                    log.debug("Property setting failed: " + propName + ", skipping");
                }
            }
        }
    }

    /**
     * ValueProvider 实现，用于从 source 对象获取属性值
     */
    private static class SourceValueProvider implements ValueProvider<Object> {
        private final Object source;
        private final BeanDesc sourceDesc;

        SourceValueProvider(Object source, BeanDesc sourceDesc) {
            this.source = source;
            this.sourceDesc = sourceDesc;
        }

        @Override
        public Object value(String key, Class<?> type) {
            PropertyResult<Object> result = sourceDesc.getPropertyValue(source, key);
            return result.exists() ? result.getValue() : null;
        }

        @Override
        public boolean containsKey(String key) {
            PropertyResult<Object> result = sourceDesc.getPropertyValue(source, key);
            return result.exists();
        }
    }

    /**
     * 获取 BeanDesc 的 Field 属性名集合（仅 fieldMap 的 key 集合）
     */
    private static Set<String> getFieldMapKeySet(BeanDesc targetDesc) {
        return targetDesc.getFieldNames();
    }

    /**
     * 获取目标属性的类型
     *
     * @param targetDesc target 的 BeanDesc
     * @param propName   属性名
     * @return 属性类型，或 null（未找到）
     */
    private static Class<?> getTargetPropertyType(BeanDesc targetDesc, String propName) {
        return targetDesc.getPropertyType(propName);
    }

    /**
     * 获取指定 Class 的 BeanDesc 实例（带缓存）
     *
     * @param beanClass 要描述的类，不能为 null，否则会抛 NullPointerException
     * @return BeanDesc 实例
     */
    public static BeanDesc getOrCreateBeanDesc(Class<?> beanClass) {
        UnionKey key = new UnionKey(beanClass, beanClass);
        BeanDesc desc = BEAN_DESC_CACHE.get(key);
        if (desc == null) {
            desc = BeanDesc.getInstance(beanClass);
            BEAN_DESC_CACHE.set(key, desc);
        }
        return desc;
    }

/**
     * 将 bean 的属性复制到 Map 中。
     * <p>
     * 遍历 bean 的所有可读属性，过滤条件：
     * - 属性名为 "class" → 跳过
     * - 属性值在 ignoreFields 中 → 跳过
     * - 属性值为 null → 跳过
     * <p>
     * 注意：与 {@link #copy(Object, Object, CopyOptions)} 不同，
     * toMap() 始终忽略 null 属性值，不受 CopyOptions.isIgnoreNull() 控制。
     *
     * @param bean         源对象
     * @param options      拷贝选项（可为 null，使用默认选项）
     * @param ignoreFields 要忽略的属性名
     * @return 属性名-属性值的 HashMap
     * @deprecated 此方法已废弃，推荐直接使用 {@link BeanCopier#copy(Object, Object, CopyOptions)}
     *             将属性拷贝到预先创建的目标Map实例中
     */
    @Deprecated
    public static Map<String, Object> toMap(Object bean, CopyOptions options, String... ignoreFields) {
        if (bean == null) {
            return new java.util.HashMap<>();
        }

        // 补全默认选项
        if (options == null) {
            options = CopyOptions.create();
        }

        // 获取 BeanDesc
        BeanDesc desc = getOrCreateBeanDesc(bean.getClass());

        // 构建忽略属性集合
        Set<String> ignoreSet = new java.util.HashSet<>();
        if (ignoreFields != null) {
            for (String field : ignoreFields) {
                ignoreSet.add(field);
            }
        }
        // 将 CopyOptions 中的 ignoreProperties 也加入忽略集合
        Set<String> optionsIgnore = options.getIgnoreProperties();
        if (optionsIgnore != null) {
            ignoreSet.addAll(optionsIgnore);
        }

        // 遍历所有属性
        Map<String, Object> result = new java.util.HashMap<>();
        for (String propName : desc.getPropertyNames()) {
            // 过滤 "class" 属性
            if ("class".equals(propName)) {
                continue;
            }
            // 过滤忽略列表中的属性
            if (ignoreSet.contains(propName)) {
                continue;
            }

            // 获取属性值
            PropertyResult<Object> propResult = desc.getPropertyValue(bean, propName);
            if (!propResult.exists()) {
                // 获取失败 → 跳过
                continue;
            }
            Object value = propResult.getValue();

            // 过滤 null 值（即使 ignoreNull=false 也要过滤）
            if (value == null) {
                continue;
            }

            result.put(propName, value);
        }

        return result;
    }

    /**
     * 构建小写名→原属性名的映射（用于 ignoreCase 匹配）
     *
     * @param propertyNames 属性名集合
     * @return 小写名→原名映射
     */
    private static Map<String, String> buildLowerCaseNameMap(Set<String> propertyNames) {
        Map<String, String> lowerMap = new java.util.HashMap<>();
        if (propertyNames != null) {
            for (String name : propertyNames) {
                lowerMap.put(name.toLowerCase(), name);
            }
        }
        return lowerMap;
    }

    /**
     * 解析 source 属性名，支持4级优先级匹配：
     * - 优先级1: 精确匹配 + 同类型
     * - 优先级2: 大小写不敏感匹配 + 同类型
     * - 优先级3: 精确匹配 + 不同类型
     * - 优先级4: 大小写不敏感匹配 + 不同类型
     *
     * @param sourceDesc source 的 BeanDesc
     * @param sourceFieldName 要解析的属性名
     * @param sourceLowerNameMap 小写名→原名映射（ignoreCase 时非null）
     * @param targetPropType 目标属性类型（用于同类型比较）
     * @return 解析后的属性名，若无匹配则返回 null
     */
    private static String resolveSourceFieldName(BeanDesc sourceDesc, String sourceFieldName,
                                                 Map<String, String> sourceLowerNameMap,
                                                 Class<?> targetPropType) {
        // 优先级1: 精确匹配 + 同类型
        String result = resolveByExactMatch(sourceDesc, sourceFieldName, targetPropType);
        if (result != null) {
            return result;
        }

        // 优先级2: 大小写不敏感匹配 + 同类型
        result = resolveByIgnoreCaseMatch(sourceDesc, sourceFieldName, sourceLowerNameMap, targetPropType);
        if (result != null) {
            return result;
        }

        // 优先级3: 精确匹配 + 不同类型
        Set<String> propertyNames = sourceDesc.getPropertyNames();
        if (propertyNames.contains(sourceFieldName)) {
            return sourceFieldName;
        }

        // 优先级4: 大小写不敏感匹配 + 不同类型
        return resolveByIgnoreCaseOnly(sourceFieldName, sourceLowerNameMap);
    }

    /**
     * 优先级1: 精确匹配 + 同类型
     *
     * @param sourceDesc source 的 BeanDesc
     * @param sourceFieldName 要解析的属性名
     * @param targetPropType 目标属性类型
     * @return 匹配的属性名，或 null
     */
    private static String resolveByExactMatch(BeanDesc sourceDesc, String sourceFieldName,
                                              Class<?> targetPropType) {
        Set<String> propertyNames = sourceDesc.getPropertyNames();
        if (propertyNames.contains(sourceFieldName)) {
            Class<?> sourcePropType = sourceDesc.getPropertyType(sourceFieldName);
            if (targetPropType != null && sourcePropType != null && targetPropType.equals(sourcePropType)) {
                return sourceFieldName;
            }
        }
        return null;
    }

    /**
     * 优先级2: 大小写不敏感匹配 + 同类型
     *
     * @param sourceDesc source 的 BeanDesc
     * @param sourceFieldName 要解析的属性名
     * @param sourceLowerNameMap 小写名→原名映射
     * @param targetPropType 目标属性类型
     * @return 匹配的属性名，或 null
     */
    private static String resolveByIgnoreCaseMatch(BeanDesc sourceDesc, String sourceFieldName,
                                                    Map<String, String> sourceLowerNameMap,
                                                    Class<?> targetPropType) {
        if (sourceLowerNameMap == null) {
            return null;
        }
        String lowerName = sourceFieldName.toLowerCase();
        String matchedName = sourceLowerNameMap.get(lowerName);
        if (matchedName != null) {
            Class<?> sourcePropType = sourceDesc.getPropertyType(matchedName);
            if (targetPropType != null && sourcePropType != null && targetPropType.equals(sourcePropType)) {
                return matchedName;
            }
        }
        return null;
    }

    /**
     * 优先级4: 大小写不敏感匹配 + 不同类型
     *
     * @param sourceFieldName 要解析的属性名
     * @param sourceLowerNameMap 小写名→原名映射
     * @return 匹配的属性名，或 null
     */
    private static String resolveByIgnoreCaseOnly(String sourceFieldName,
                                                  Map<String, String> sourceLowerNameMap) {
        if (sourceLowerNameMap == null) {
            return null;
        }
        String lowerName = sourceFieldName.toLowerCase();
        String matchedName = sourceLowerNameMap.get(lowerName);
        return matchedName;
    }

    /**
     * 在临时转换器列表中查找匹配的转换器。
     *
     * 匹配规则：
     * - 源类型 isAssignableFrom value.getClass()
     * - 目标类型 equals targetPropertyType
     * - ConditionConverter 必须 matches(value) 返回 true
     *
     * @param customConverters 临时转换器列表
     * @param value 源对象值
     * @param targetPropertyType 目标属性类型
     * @return 匹配的转换器，或 null（未找到）
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Converter<?, ?> findCustomConverter(List<Converter<?, ?>> customConverters,
                                                        Object value, Class<?> targetPropertyType) {
        Class<?> sourceType = value.getClass();
        for (Converter<?, ?> converter : customConverters) {
            Class<?> converterSourceType = converter.getSourceType();
            Class<?> converterTargetType = converter.getTargetType();

            // 检查源类型可赋值 + 目标类型匹配（null检查防御NPE）
            if (converterSourceType != null && converterTargetType != null
                    && converterSourceType.isAssignableFrom(sourceType)
                    && converterTargetType.equals(targetPropertyType)) {
                // ConditionConverter 额外检查 matches
                if (converter instanceof ConditionConverter) {
                    ConditionConverter cc = (ConditionConverter) converter;
                    if (cc.matches(sourceType.cast(value))) {
                        return converter;
                    }
                } else {
                    return converter;
                }
            }
        }
        return null;
    }

    /**
     * 构建类型转换异常信息。
     * <p>
     * 格式：Property '&lt;propName&gt;' conversion failed: &lt;sourceType&gt; → &lt;targetType&gt;, value='&lt;value&gt;'
     * <p>
     * 边界处理：
     * - value 为 null：显示 "null"
     * - value.toString() 超长（&gt;200 字符）：截断并追加 "...({length})"
     * - targetPropertyType 为 null：显示 "Unknown"
     * - 数组/集合：显示类型名 + size（如 "ArrayList(3)"）
     *
     * @param propName           属性名
     * @param value              源值
     * @param targetPropertyType 目标属性类型
     * @return 格式化的异常信息字符串
     */
    private static String buildConversionErrorMessage(
            String propName, Object value, Class<?> targetPropertyType) {
        // 构建目标类型字符串
        String targetTypeStr = (targetPropertyType != null) ? targetPropertyType.getName() : "Unknown";

        // 构建源值字符串
        String valueStr;
        if (value == null) {
            valueStr = "null";
        } else if (value.getClass().isArray()) {
            // 数组类型
            if (value instanceof Object[]) {
                Object[] arr = (Object[]) value;
                valueStr = value.getClass().getComponentType().getName() + "[" + arr.length + "]";
            } else if (value instanceof byte[]) {
                valueStr = "byte[" + ((byte[]) value).length + "]";
            } else if (value instanceof short[]) {
                valueStr = "short[" + ((short[]) value).length + "]";
            } else if (value instanceof int[]) {
                valueStr = "int[" + ((int[]) value).length + "]";
            } else if (value instanceof long[]) {
                valueStr = "long[" + ((long[]) value).length + "]";
            } else if (value instanceof float[]) {
                valueStr = "float[" + ((float[]) value).length + "]";
            } else if (value instanceof double[]) {
                valueStr = "double[" + ((double[]) value).length + "]";
            } else if (value instanceof char[]) {
                valueStr = "char[" + ((char[]) value).length + "]";
            } else if (value instanceof boolean[]) {
                valueStr = "boolean[" + ((boolean[]) value).length + "]";
            } else {
                valueStr = value.getClass().getName();
            }
        } else if (value instanceof java.util.Collection) {
            // 集合类型
            java.util.Collection<?> coll = (java.util.Collection<?>) value;
            valueStr = value.getClass().getSimpleName() + "(" + coll.size() + ")";
        } else {
            // 普通对象，使用 toString
            String toStringResult = value.toString();
            if (toStringResult.length() > 200) {
                valueStr = toStringResult.substring(0, 200) + "...(" + toStringResult.length() + ")";
            } else {
                valueStr = toStringResult;
            }
        }

        // 组装完整消息
        String sourceTypeStr = (value != null) ? value.getClass().getName() : "null";
        return "Property '" + propName + "' conversion failed: " + sourceTypeStr
                + " -> " + targetTypeStr + ", value='" + valueStr + "'";
    }

    /**
     * 判断目标类型是否为「简单类型」—— 不需要嵌套递归的。
     *
     * 包括：基本类型包装类、String、BigDecimal/BigInteger、枚举、Map、Collection、
     * Date、URI、URL、UUID、Class、byte[] 等。
     *
     * @param type 目标属性类型
     * @return true 表示是简单类型，不需要嵌套转换
     */
    private static boolean isSimpleType(Class<?> type) {
        if (type == null) {
            return true;
        }
        // 基本类型包装类
        if (type == Boolean.class || type == Byte.class || type == Character.class
                || type == Short.class || type == Integer.class || type == Long.class
                || type == Float.class || type == Double.class || type == Void.class) {
            return true;
        }
        // 简单类型
        if (type == String.class || type == BigDecimal.class || type == BigInteger.class
                || type == Date.class || type == URI.class || type == URL.class
                || type == UUID.class || type == Class.class || type == byte[].class) {
            return true;
        }
        // 枚举
        if (type.isEnum()) {
            return true;
        }
        // Map/Collection（不做自动嵌套转换）
        if (Map.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type)) {
            return true;
        }
        return false;
    }

    /**
     * 解析 List 类型属性的元素类型（通过 Field 的泛型参数）。
     *
     * 例如 List&lt;User&gt; → User.class；List&lt;String&gt; → String.class。
     * 支持多层嵌套泛型（如 List&lt;List&lt;User&gt;&gt; → List.class 作为元素类型）。
     *
     * @param targetDesc 目标 BeanDesc
     * @param propName   属性名
     * @return 元素类型，或 null（无法解析时）
     */
    private static Class<?> resolveListElementType(BeanDesc targetDesc, String propName) {
        Field field = targetDesc.getField(propName);
        if (field == null) {
            return null;
        }
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType)) {
            return null;
        }
        ParameterizedType parameterizedType = (ParameterizedType) genericType;
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        if (typeArguments == null || typeArguments.length == 0) {
            return null;
        }
        Type elementType = typeArguments[0];
        // 如果元素类型还是泛型参数，返回 null（无法解析）
        if (elementType instanceof java.lang.reflect.TypeVariable) {
            return null;
        }
        if (elementType instanceof Class) {
            return (Class<?>) elementType;
        }
        // 处理嵌套泛型如 List<User> 的情况
        if (elementType instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) elementType).getRawType();
            if (rawType instanceof Class) {
                return (Class<?>) rawType;
            }
        }
        return null;
    }

    /**
     * 尝试执行嵌套转换（Map→Bean, List&lt;Map&gt;→List&lt;Bean&gt;）。
     *
     * 返回转换后的值，若无需嵌套转换则返回原始 value。
     * 内部通过 depth 参数控制递归深度，超过 MAX_NESTED_DEPTH 时直接返回原始值。
     *
     * @param value              当前属性值
     * @param targetPropertyType 目标属性类型
     * @param propName           属性名（用于获取 Field 泛型类型）
     * @param targetDesc         目标 BeanDesc
     * @param options            拷贝选项（传递到递归调用）
     * @param depth              当前递归深度（外部首次传入 1）
     * @return 转换后的值，或原始 value（无需转换时）
     */
    private static Object tryNestedConversion(
            Object value, Class<?> targetPropertyType,
            String propName, BeanDesc targetDesc,
            CopyOptions options, int depth) {
        // 深度超限，降级返回原始值
        if (depth > MAX_NESTED_DEPTH) {
            if (log.isDebugEnabled()) {
                log.debug("Nested conversion depth limit exceeded ({}), returning original value", MAX_NESTED_DEPTH);
            }
            return value;
        }
        // 空值不转换
        if (value == null) {
            return value;
        }
        // Map → Bean 递归转换
        if (value instanceof Map) {
            // 简单类型不触发嵌套转换
            if (isSimpleType(targetPropertyType)) {
                return value;
            }
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapValue = (Map<String, Object>) value;
                return BeanUtils.toBean(mapValue, targetPropertyType, options);
            } catch (Exception e) {
                // 嵌套转换失败，降级返回原始值
                if (log.isDebugEnabled()) {
                    log.debug("Nested Map→Bean conversion failed, returning original value", e);
                }
                return value;
            }
        }
        // List<Map> → List<Bean> 递归转换
        // 注意：即使 targetPropertyType 是 List/Collection 类型，也需要检查元素是否需要转换
        if (value instanceof List) {
            List<?> listValue = (List<?>) value;
            if (listValue.isEmpty()) {
                return value;
            }
            // 检查 List 元素是否都是 Map（只有 Map 元素才需要转换）
            boolean hasMapElement = false;
            for (Object item : listValue) {
                if (item instanceof Map) {
                    hasMapElement = true;
                    break;
                }
            }
            if (!hasMapElement) {
                // 没有 Map 元素，无需转换
                return value;
            }
            // 解析目标 List 的元素类型
            Class<?> elementType = resolveListElementType(targetDesc, propName);
            if (elementType == null) {
                // 无法解析元素类型，降级返回原始 List
                return value;
            }
            // 元素类型是简单类型，不递归转换
            if (isSimpleType(elementType)) {
                return value;
            }
            // 执行逐元素递归转换
            try {
                List<Object> convertedList = new ArrayList<>(listValue.size());
                for (Object item : listValue) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> mapItem = (Map<String, Object>) item;
                        Object convertedItem = BeanUtils.toBean(mapItem, elementType, options);
                        convertedList.add(convertedItem);
                    } else {
                        convertedList.add(item);
                    }
                }
                return convertedList;
            } catch (Exception e) {
                // 嵌套转换失败，降级返回原始 List
                if (log.isDebugEnabled()) {
                    log.debug("Nested List element conversion failed, returning original value", e);
                }
                return value;
            }
        }
        return value;
    }
}