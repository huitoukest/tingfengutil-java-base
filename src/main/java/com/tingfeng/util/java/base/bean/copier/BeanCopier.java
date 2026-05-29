package com.tingfeng.util.java.base.bean.copier;

import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.cache.SimpleCacheHelper;
import com.tingfeng.util.java.base.lang.base.UnionKey;
import com.tingfeng.util.java.base.lang.exception.BaseException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

/**
 * Bean属性拷贝器，支持选项化配置。
 * <p>
 * 核心方法 {@link #copy(Object, Object, CopyOptions)} 执行浅拷贝，流程：
 * <ul>
 *   <li>获取 source/target 的 BeanDesc（带缓存）</li>
 *   <li>遍历 target 可写属性，通过 fieldMapping/ignoreProperties 等选项过滤</li>
 *   <li>从 source 取值，null判断，类型转换，写入 target</li>
 * </ul>
 *
 * @author huitoukest
 */
@Slf4j
public class BeanCopier {

    /** BeanDesc 缓存，key = UnionKey(class, class)，容量512 */
    private static final SimpleCacheHelper<UnionKey, BeanDesc> BEAN_DESC_CACHE =
            new SimpleCacheHelper<>(512);

    /**
     * 拷贝 bean 属性（浅拷贝）
     * <p>
     * 前置检查：
     * <ul>
     *   <li>source == null 或 target == null → 直接 return（不抛异常）</li>
     * </ul>
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
        } else {
            // 使用所有属性名（pdMap ∪ fieldMap）
            targetPropertyNames = targetDesc.getPropertyNames();
        }

        // 获取配置
        Set<String> ignoreProperties = options.getIgnoreProperties();
        boolean ignoreNull = options.isIgnoreNull();
        boolean useConverter = options.isUseConverter();
        boolean ignoreNoMatchConverterError = options.isIgnoreNoMatchConverterError();
        Map<String, String> fieldMapping = options.getFieldMapping();

        // 构建 source 小写名→原名映射（ignoreCase 时使用）
        Map<String, String> sourceLowerNameMap = null;
        if (options.isIgnoreCase()) {
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

            // c. resolveSourceFieldName：处理精确+大小写不敏感匹配
            sourceFieldName = resolveSourceFieldName(sourceDesc, sourceFieldName, sourceLowerNameMap);
            if (sourceFieldName == null) {
                // 无匹配 → 跳过
                continue;
            }

            // d. source 取值
            PropertyResult<Object> result = sourceDesc.getPropertyValue(source, sourceFieldName);
            if (!result.exists()) {
                // sourceDesc 没有此属性 → 跳过
                continue;
            }
            Object value = result.getValue();

            // e. null 判断
            if (value == null && ignoreNull) {
                continue;
            }

            // f. 类型转换
            if (value != null) {
                Class<?> targetType = getTargetPropertyType(targetDesc, propName);
                if (targetType != null && !value.getClass().equals(targetType) && useConverter) {
                    try {
                        @SuppressWarnings("unchecked")
                        Object convertedValue = ConverterRegistry.getInstance()
                                .convert(value, (Class<Object>) targetType, value);
                        value = convertedValue;
                    } catch (Exception e) {
                        // 转换失败
                        boolean hasConverter = !ConverterRegistry.getInstance()
                                .findConverters(value.getClass(), targetType).isEmpty();
                        if (hasConverter) {
                            // 场景A：有Converter但转换失败 → 直接抛异常，不受 ignoreNoMatchConverterError 控制
                            throw new BaseException(
                                    "Property conversion failed: " + propName, e);
                        }
                        // 场景B：无Converter类型不匹配 → 受 ignoreNoMatchConverterError 控制
                        if (!ignoreNoMatchConverterError) {
                            throw new BaseException(
                                    "Property conversion failed: " + propName, e);
                        }
                        // ignoreNoMatchConverterError=true → 跳过该属性
                        if (log.isDebugEnabled()) {
                            log.debug("Property conversion failed: " + propName + ", skipping", e);
                        }
                        continue;
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
     * 从 ValueProvider 拷贝属性到 target Bean（浅拷贝）
     * <p>
     * 与 {@link #copy(Object, Object, CopyOptions)} 的区别：
     * <ul>
     *   <li>取值来源：copy() 从 source.getPropertyValue() 取值；copyFromProvider() 从 provider.value() 取值</li>
     *   <li>source 不存在属性：copy() 通过 try-catch 跳过；copyFromProvider() 通过 provider.containsKey() 检查</li>
     * </ul>
     *
     * <p>
     * 前置检查：
     * <ul>
     *   <li>provider == null 或 target == null → 直接 return（不抛异常）</li>
     * </ul>
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
        } else {
            // 使用所有属性名（pdMap ∪ fieldMap）
            targetPropertyNames = targetDesc.getPropertyNames();
        }

        // 获取配置
        Set<String> ignoreProperties = options.getIgnoreProperties();
        boolean ignoreNull = options.isIgnoreNull();
        boolean useConverter = options.isUseConverter();
        boolean ignoreNoMatchConverterError = options.isIgnoreNoMatchConverterError();
        Map<String, String> fieldMapping = options.getFieldMapping();

        // 构建 source 小写名→原名映射（ignoreCase 时使用，copyFromProvider 无 sourceDesc，用 null 传参）
        Map<String, String> sourceLowerNameMap = null;
        // 注意：copyFromProvider 不需要 ignoreCase 因为它是通过 provider.containsKey() 检查的
        // 这里只为保持方法签名一致性

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

            // c. 检查 provider 是否包含该 key（核心区别于 copy() 方法）
            if (!provider.containsKey(sourceFieldName)) {
                continue;
            }

            // d. 从 provider 取值
            Class<?> propertyType = getTargetPropertyType(targetDesc, propName);
            Object value = provider.value(sourceFieldName, propertyType);

            // e. null 判断
            if (value == null && ignoreNull) {
                continue;
            }

            // f. 类型转换
            if (value != null) {
                Class<?> targetType = getTargetPropertyType(targetDesc, propName);
                if (targetType != null && !value.getClass().equals(targetType) && useConverter) {
                    try {
                        @SuppressWarnings("unchecked")
                        Object convertedValue = ConverterRegistry.getInstance()
                                .convert(value, (Class<Object>) targetType, value);
                        value = convertedValue;
                    } catch (Exception e) {
                        // 转换失败
                        boolean hasConverter = !ConverterRegistry.getInstance()
                                .findConverters(value.getClass(), targetType).isEmpty();
                        if (hasConverter) {
                            // 场景A：有Converter但转换失败 → 直接抛异常
                            throw new BaseException(
                                    "Property conversion failed: " + propName, e);
                        }
                        // 场景B：无Converter类型不匹配 → 受 ignoreNoMatchConverterError 控制
                        if (!ignoreNoMatchConverterError) {
                            throw new BaseException(
                                    "Property conversion failed: " + propName, e);
                        }
                        // ignoreNoMatchConverterError=true → 跳过该属性
                        if (log.isDebugEnabled()) {
                            log.debug("Property conversion failed: " + propName + ", skipping", e);
                        }
                        continue;
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
     * @param beanClass 要描述的类
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
     * <ul>
     *   <li>属性名为 "class" → 跳过</li>
     *   <li>属性值在 ignoreFields 中 → 跳过</li>
     *   <li>属性值为 null → 跳过</li>
     * </ul>
     *
     * @param bean         源对象
     * @param options      拷贝选项（可为 null，使用默认选项）
     * @param ignoreFields 要忽略的属性名
     * @return 属性名-属性值的 HashMap
     */
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
     * <ol>
     *   <li>精确匹配 + 同类型</li>
     *   <li>大小写不敏感匹配 + 同类型</li>
     *   <li>精确匹配 + 不同类型</li>
     *   <li>大小写不敏感匹配 + 不同类型</li>
     * </ol>
     *
     * @param sourceDesc source 的 BeanDesc
     * @param sourceFieldName 要解析的属性名
     * @param sourceLowerNameMap 小写名→原名映射（ignoreCase 时非null）
     * @return 解析后的属性名，若无匹配则返回 null
     */
private static String resolveSourceFieldName(BeanDesc sourceDesc, String sourceFieldName,
                                               Map<String, String> sourceLowerNameMap) {
        Set<String> propertyNames = sourceDesc.getPropertyNames();

        // 获取 source 属性类型（用于同类型判断）
        Class<?> sourcePropType = sourceDesc.getPropertyType(sourceFieldName);

// 优先级1: 精确匹配 + 同类型
        if (propertyNames.contains(sourceFieldName)) {
            // 检查类型是否一致
            Class<?> srcType = sourceDesc.getPropertyType(sourceFieldName);
            if (srcType != null && sourcePropType != null && srcType.equals(sourcePropType)) {
                return sourceFieldName;
            }
        }

        // 优先级2: 大小写不敏感匹配 + 同类型
        if (sourceLowerNameMap != null) {
            String lowerName = sourceFieldName.toLowerCase();
            String matchedName = sourceLowerNameMap.get(lowerName);
            if (matchedName != null) {
                Class<?> srcType = sourceDesc.getPropertyType(matchedName);
                if (srcType != null && sourcePropType != null && srcType.equals(sourcePropType)) {
                    return matchedName;
                }
            }
        }

        // 优先级3: 精确匹配 + 不同类型
        if (propertyNames.contains(sourceFieldName)) {
            return sourceFieldName;
        }

        // 优先级4: 大小写不敏感匹配 + 不同类型
        if (sourceLowerNameMap != null) {
            String lowerName = sourceFieldName.toLowerCase();
            String matchedName = sourceLowerNameMap.get(lowerName);
            if (matchedName != null) {
                return matchedName;
            }
        }

        return null;
    }
}