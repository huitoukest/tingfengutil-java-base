package com.tingfeng.util.java.base.bean.converter;

/**
 * 类型转换器接口
 * @param <S> 源类型
 * @param <T> 目标类型
 */
public interface Converter<S, T> {

    /**
     * 执行类型转换
     * @param source 源对象
     * @return 转换后的目标对象
     */
    T convert(S source);

    /**
     * 获取源类型
     * @return 源类型 Class
     */
    Class<S> getSourceType();

    /**
     * 获取目标类型
     * @return 目标类型 Class
     */
    Class<T> getTargetType();

    // ==================== 冒泡注册相关方法 ====================

    /**
     * 可冒泡层数。注册时生效，控制向上注册到第几层父类。
     * <ul>
     *   <li>0 = 不冒泡，仅注册自身</li>
     *   <li>1 = 注册自身 + 直接父类（含实现的接口）</li>
     *   <li>n = 注册自身 + 向上 n 层父类链/接口链</li>
     *   <li>-1 = 无限制，冒泡到 Object 为止（含 Object），冒泡产生的副本也保持 -1</li>
     * </ul>
     * 默认值 1，可在实现类中按需重写。
     *
     * @return 冒泡层数
     */
    default int bubbleLevel() {
        return 1;
    }

    /**
     * 注册顺序。由注册工具在冒泡时自动计算。
     * <ul>
     *   <li>原始注册的 Converter 为 0</li>
     *   <li>每冒泡一层 +1</li>
     *   <li>超过 {@link Integer#MAX_VALUE} 时取最大值</li>
     * </ul>
     * 用于排序：值越小越优先，确保精确注册（regOrder=0）始终优先于冒泡副本。
     *
     * @return 注册顺序，值越小越优先
     */
    default int registrationOrder() {
        return 0;
    }

    /**
     * 用户定义的排序顺序。
     * 当 {@link #registrationOrder()} 相同时，此值小的优先。
     * 配合 {@link #registrationOrder()} 实现先按精确度、再按用户优先级排序的查找规则。
     *
     * @return 排序值，值越小越优先
     */
    default int order() {
        return 0;
    }
}
