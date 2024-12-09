package com.tingfeng.util.java.base.common.utils.cache;

import com.tingfeng.util.java.base.common.bean.cache.CacheItem;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 通用的缓存接口,适用时请走CacheUtil获取实例
 */
public interface Cache<K,V> {
    V get(K k);
    List<V> list(Collection<? extends K> keys);
    List<V> all();
    Iterator<CacheItem<K,V>> iterator();
    void put(K k,V v,long expireMills);
    void put(Map<? extends K,? extends V> kvMap,long expireMills);
    V cache(K k,long expireMills, Supplier<V> supplier);
    <T> T doInReadLock(Supplier<T> supplier);
    <T> T doInWriteLock(Supplier<T> supplier);
    <T> T doInReadLock(K k, Function<CacheItem<K,V>,T> handler);
    <T> T doInWriteLock(K k,Function<CacheItem<K,V>,T> handler);
    /**
     * 清理缓存
     */
    void cleanAll();
    /**
     * 查询的时候采用copyRead方式
     * 1. 当存储的值为容器或这复杂对象时,通过copy值对象返回可以防止其它线程对缓存对象直接读写的问题
     * 2. 对 {@link #get},{@link #list},{@link #all},{@link #iterator},{@link #cache}方法适用
     * @param v
     * @return
     */
    V copyRead(V v);

    /**
     * 淘汰不需要的数据
     * 1. 所有缓存适用固定2线程定时调度此方法
     * 2. 由于调度线程共用,此方法必须迅速执行,不建议包含耗时操作
     * @return
     */
    void eliminationData();

    /**
     * 有效数据判断
     * @return
     */
    boolean isValidItem(CacheItem<K,V> cacheItem);

    default V getValue(CacheItem<K,V> cacheItem){
        return Optional.ofNullable(cacheItem)
                .map(CacheItem::getValue).orElse(null);
    }
}