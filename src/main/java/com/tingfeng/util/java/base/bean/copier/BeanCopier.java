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
                    // 预查询 Converter 是否存在，避免在 catch 块内重复查询
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
     * <p>
     * 注意：与 {@link #copy(Object, Object, CopyOptions)} 不同，
     * toMap() 始终忽略 null 属性值，不受 CopyOptions.isIgnoreNull() 控制。
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
}