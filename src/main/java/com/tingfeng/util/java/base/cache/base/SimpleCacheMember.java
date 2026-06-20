package com.tingfeng.util.java.base.cache.base;

/**
 * 缓存成员，包含权重值、实际对象和过期时间
 *
 * @param <T> 对象类型
 */
public class SimpleCacheMember<T> {
    /**
     * 权重增量，相对于 globalBase 的偏移量
     * actualWeight = globalBase + weight
     */
    private long weight;
    private T value;
    /**
     * 过期时间戳（毫秒），0 表示不过期
     */
    private long expireTime;

    public SimpleCacheMember() {
        this.weight = 0;
    }

    public SimpleCacheMember(T value) {
        this.value = value;
        this.weight = 0;
    }

    public SimpleCacheMember(long weight, T value) {
        this.weight = weight;
        this.value = value;
        this.expireTime = 0;
    }

    public SimpleCacheMember(long weight, T value, long expireTime) {
        this.weight = weight;
        this.value = value;
        this.expireTime = expireTime;
    }

    public long getWeight() {
        return weight;
    }

    public SimpleCacheMember<T> setWeight(long weight) {
        this.weight = weight;
        return this;
    }

    public T getValue() {
        return value;
    }

    public SimpleCacheMember<T> setValue(T value) {
        this.value = value;
        return this;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public SimpleCacheMember<T> setExpireTime(long expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    /**
     * 判断是否已过期
     *
     * @return true=已过期，false=未过期或永不过期
     */
    public boolean isExpired() {
        return expireTime > 0 && System.currentTimeMillis() > expireTime;
    }
}