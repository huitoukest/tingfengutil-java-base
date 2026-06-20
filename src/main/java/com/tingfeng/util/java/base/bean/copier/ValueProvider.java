package com.tingfeng.util.java.base.bean.copier;

/**
 * 值提供者接口，用于从源对象获取属性值。
 *
 * 配合 BeanCopier 使用，提供统一的属性值读取抽象。
 *
 * @author huitoukest
 * @param <T> 源对象类型
 */
public interface ValueProvider<T> {

    /**
     * 获取指定 key 的值
     *
     * @param key  属性名（支持 a.b.c 点号路径）
     * @param type 期望的目标类型（暂不用于过滤，保留扩展）
     * @return 属性值，不存在返回 null
     */
    Object value(String key, Class<?> type);

    /**
     * 是否包含指定 key
     *
     * @param key 属性名
     * @return true 表示该 key 存在值
     */
    boolean containsKey(String key);
}