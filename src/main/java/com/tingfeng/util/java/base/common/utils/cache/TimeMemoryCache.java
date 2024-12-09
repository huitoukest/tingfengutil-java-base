package com.tingfeng.util.java.base.common.utils.cache;

import com.tingfeng.util.java.base.common.bean.cache.CacheItem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 基于时间淘汰策略的内存缓存工具
 * @todo 待功能测试
 */
public class TimeMemoryCache<K,V> implements Cache<K,V>{
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Map<K, CacheItem<K,V>> cacheMap = new HashMap();

    @Override
    public V get(K k) {
        return doInReadLock(() -> Optional.ofNullable(k)
                .map(cacheMap::get)
                .filter(this::isValidItem)
                .map(this::getValue)
                .map(this::copyRead)
                .orElse(null));
    }

    @Override
    public List<V> list(Collection<? extends K> keys) {
        return doInReadLock(() -> keys.stream().map(cacheMap::get)
                .filter(this::isValidItem)
                .map(this::getValue)
                .map(this::copyRead)
                .collect(Collectors.toList())
        );
    }

    @Override
    public List<V> all() {
        return doInReadLock(() -> cacheMap.values()
                .stream()
                .filter(this::isValidItem)
                .map(this::getValue)
                .map(this::copyRead)
                .collect(Collectors.toList())
        );
    }

    @Override
    public Iterator<CacheItem<K, V>> iterator() {
        return doInReadLock(() -> cacheMap.values()
                .stream()
                .filter(this::isValidItem)
                .map(item -> {
                    if(item == null){
                        return null;
                    }
                    CacheItem<K,V> cacheItem = new CacheItem<>(item.getKey(), this.copyRead(item.getValue()), item.getExpireTime());
                    return cacheItem;
                })
                .iterator()
        );
    }

    @Override
    public void put(K k, V v, long expireMills) {
        doInWriteLock(() -> {
           cacheMap.put(k, new CacheItem<>(k, v, System.currentTimeMillis() + expireMills));
           return null;
        });
    }

    @Override
    public void put(Map<? extends K, ? extends V> kvMap, long expireMills) {
        doInWriteLock(() -> {
            kvMap.forEach((k,v) -> {
                cacheMap.put(k, new CacheItem<>(k, v, System.currentTimeMillis() + expireMills));
            });
            return null;
        });
    }

    @Override
    public V cache(K k, long expireMills, Supplier<V> supplier) {
        V v = this.get(k);
        if(v != null){
            return v;
        }
        return doInWriteLock(k, cacheItem -> {
            V newValue = null;
            if(cacheItem == null){
                newValue = supplier.get();
                cacheItem = new CacheItem<>(k, newValue, System.currentTimeMillis() + expireMills);
                cacheMap.put(k, cacheItem);
            }else {
                newValue = cacheItem.getValue();
            }
            return copyRead(newValue);
        });
    }

    @Override
    public <T> T doInReadLock(Supplier<T> supplier) {
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        readLock.lock();
        try{
            return supplier.get();
        }finally {
            readLock.unlock();
        }
    }

    @Override
    public <T> T doInWriteLock(Supplier<T> supplier) {
        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try{
            return supplier.get();
        }finally {
            writeLock.unlock();
        }
    }

    @Override
    public <T> T doInReadLock(K k, Function<CacheItem<K, V>, T> handler) {
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        readLock.lock();
        try{
            return handler.apply(cacheMap.get(k));
        }finally {
            readLock.unlock();
        }
    }

    @Override
    public <T> T doInWriteLock(K k, Function<CacheItem<K, V>, T> handler) {
        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try{
            return handler.apply(cacheMap.get(k));
        }finally {
            writeLock.unlock();
        }
    }

    @Override
    public void cleanAll() {
        doInWriteLock(() -> {
            cacheMap.clear();
            return null;
        });
    }

    @Override
    public V copyRead(V v) {
        if(v == null){
            return null;
        }
        Object reObj = v;
        if(isSameClass(ArrayList.class,v)){
            reObj = new ArrayList<>(((Collection<?>) v));
        }
        if(isSameClass(LinkedList.class,v)){
            reObj = new LinkedList<>(((Collection<?>) v));
        }
        if(isSameClass(HashSet.class,v)){
            reObj = new HashSet<>((Set<?>) v);
        }
        if(isSameClass(LinkedHashSet.class,v)){
            reObj = new LinkedHashSet<>((Set<?>) v);
        }
        if(isSameClass(TreeSet.class,v)){
            reObj = new TreeSet<>((Set<?>) v);
        }
        if(isSameClass(HashMap.class,v)) {
            reObj = new HashMap<>(((Map<?, ?>) v));
        }
        if(isSameClass(LinkedHashMap.class,v)) {
            reObj = new LinkedHashMap<>(((Map<?, ?>) v));
        }
        if(isSameClass(ConcurrentHashMap.class,v)) {
            reObj = new ConcurrentHashMap<>(((Map<?, ?>) v));
        }
        if(isSameClass(TreeMap.class,v)) {
            reObj = new TreeMap<>(((Map<?, ?>) v));
        }
        return (V) reObj;
    }

    private boolean isSameClass(Class cls,Object b){
        return cls.equals(b.getClass());
    }

    @Override
    public void eliminationData() {
        doInWriteLock(() -> {
            List<K> needRemoveKeys = cacheMap.entrySet()
                    .stream()
                    .filter(entry -> {
                        if (this.isValidItem(entry.getValue())) {
                            return false;
                        }
                        return true;
                    }).map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            needRemoveKeys.forEach(cacheMap::remove);
            return null;
        });
    }

    @Override
    public boolean isValidItem(CacheItem<K,V> item){
        return item != null && System.currentTimeMillis() <= item.getExpireTime();
    }
}