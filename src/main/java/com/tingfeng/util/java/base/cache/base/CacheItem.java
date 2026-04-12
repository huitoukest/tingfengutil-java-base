package com.tingfeng.util.java.base.common.bean.cache;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CacheItem<K,V> {
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
