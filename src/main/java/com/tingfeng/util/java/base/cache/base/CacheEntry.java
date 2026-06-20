package com.tingfeng.util.java.base.cache.base;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 缓存条目，包含键、值和过期时间
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
@Data
@AllArgsConstructor
public class CacheEntry<K,V> {
    private K key;
    /**
     * 缓存的值
     */
    private V value;
    /**
     * 过期的时间点
     */
    private long expireTime;
}
