package com.tingfeng.util.java.base.cache.base;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EntryCacheItem<K, V> extends BaseCacheItem<V> implements KeyAware<K> {
    private K key;

    public EntryCacheItem() {
    }

    public EntryCacheItem(K key, V value, long expireTime) {
        super(value, expireTime);
        this.key = key;
    }
}
