package com.tingfeng.util.java.base.cache.base;

public interface KeyAware<K> {
    K getKey();
    KeyAware<K> setKey(K key);
}
