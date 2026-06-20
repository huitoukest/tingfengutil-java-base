package com.tingfeng.util.java.base.bean.converter;

import java.util.List;

/**
 * 转换器注册中心接口
 * <p>
 * 核心管理器：注册、移除、查找、构建
 * <p>
 * 自定义转换器注册示例：
 * <pre>
 * public class StringToIntegerConverter implements Converter&lt;String, Integer&gt; {
 *     {@literal @}Override
 *     public Integer convert(String source) {
 *         return Integer.parseInt(source);
 *     }
 *     {@literal @}Override
 *     public Class&lt;String&gt; getSourceType() { return String.class; }
 *     {@literal @}Override
 *     public Class&lt;Integer&gt; getTargetType() { return Integer.class; }
 * }
 * // 注册后自动生效于 BeanCopier 跨类型拷贝
 * ConverterRegistry.getInstance().register(new StringToIntegerConverter());
 * </pre>
 * <p>
 * 方法使用场景说明：
 * <ul>
 *   <li>register：注册自定义 Converter，注册后自动可用于类型转换</li>
 *   <li>unregister：注销指定 Converter，同时移除其冒泡副本</li>
 *   <li>clear：清空所有已注册的转换器（包含默认转换器）</li>
 *   <li>resetConverter：重置为默认转换器配置（先清空再注册默认转换器）</li>
 * </ul>
 * <p>
 * 转换方法说明：
 * <ul>
 *   <li>convert(source, targetType)：转换失败时抛出 ConverterException</li>
 *   <li>convert(source, targetType, defaultValue)：转换失败时返回默认值，source 为 null 时也返回默认值</li>
 * </ul>
 */
public interface ConverterRegistry {

    /**
     * 注册转换器
     * @param converter 转换器
     */
    <S,T> void register(Converter<S, T> converter);

    /**
     * 注销转换器
     * @param converter 要注销的转换器
     * @return 是否成功注销
     */
    <S,T> boolean unregister(Converter<S, T> converter);

    /**
     * 返回所有适用于该类型对的 Converter
     * <p>
     * 注意：返回所有注册的 Converter，不做 matches() 过滤。
     * matches() 过滤由 convert() 方法在遍历时完成。
     *
     * @param source 源类型
     * @param target 目标类型
     * @return 所有匹配的 Converter 列表，可能为空
     */
    <S,T> List<Converter<S, T>> findAll(Class<S> source, Class<T> target);

    /**
     * 返回适用于该类型对的转换器查找结果
     * <p>
     * 结果包含分类后的 ConditionConverter 列表和普通 Converter。
     * 推荐使用此方法替代 findAll()，因为它在内部完成分类，性能更好。
     *
     * @param source 源类型
     * @param target 目标类型
     * @return 转换器查找结果
     */
    <S,T> ConverterSearchResult<S, T> findConverters(Class<S> source, Class<T> target);

    /**
     * 执行转换，失败抛 ConverterException
     * <p>
     * 实现逻辑：遍历 findAll() 结果，对 ConditionConverter 调用 matches()，
     * 返回第一个匹配的 Converter 的转换结果。
     *
     * @param source 源对象
     * @param target 目标类型
     * @param <T> 目标类型
     * @return 转换后的对象
     */
    <S,T> T convert(S source, Class<T> target);

    /**
     * 执行转换，失败返回默认值
     * @param source 源对象
     * @param target 目标类型
     * @param defaultValue 默认值
     * @param <T> 目标类型
     * @return 转换后的对象，失败返回默认值
     */
    <S,T> T convert(S source, Class<T> target, T defaultValue);

    /**
     * 清空所有已注册的转换器
     */
    void clear();

    /**
     * 重置为默认转换器配置（先清空，再注册默认转换器）
     */
    void resetConverter();

    /**
     * 获取全局单例实例
     * @return 全局 ConverterRegistry
     */
    static ConverterRegistry getInstance() {
        return DefaultConverterRegistry.getInstance();
    }
}
