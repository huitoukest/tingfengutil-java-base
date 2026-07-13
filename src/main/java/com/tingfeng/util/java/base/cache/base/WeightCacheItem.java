package com.tingfeng.util.java.base.cache.base;

public class WeightCacheItem<T> extends BaseCacheItem<T> implements WeightAware {
    private long weight;

    public WeightCacheItem() {
    }

    public WeightCacheItem(T value) {
        super(value, 0);
    }

    public WeightCacheItem(long weight, T value) {
        super(value, 0);
        this.weight = weight;
    }

    public WeightCacheItem(long weight, T value, long expireTime) {
        super(value, expireTime);
        this.weight = weight;
    }

    @Override
    public long getWeight() {
        return weight;
    }

    @Override
    public WeightCacheItem<T> setWeight(long weight) {
        this.weight = weight;
        return this;
    }

    @Override
    public WeightCacheItem<T> setValue(T value) {
        super.setValue(value);
        return this;
    }

    @Override
    public WeightCacheItem<T> setExpireTime(long expireTime) {
        super.setExpireTime(expireTime);
        return this;
    }
}
