package com.tingfeng.util.java.base.cache.base;

public class BaseCacheItem<T> {
    private T value;
    private long expireTime;

    public BaseCacheItem() {
    }

    public BaseCacheItem(T value, long expireTime) {
        this.value = value;
        this.expireTime = expireTime;
    }

    public T getValue() {
        return value;
    }

    public BaseCacheItem<T> setValue(T value) {
        this.value = value;
        return this;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public BaseCacheItem<T> setExpireTime(long expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    public boolean isExpired() {
        return expireTime > 0 && System.currentTimeMillis() > expireTime;
    }
}
