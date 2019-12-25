package com.tingfeng.util.java.base.common.helper;

import com.tingfeng.util.java.base.common.bean.SimpleCacheMember;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * 实现一个简单的缓存,默认使用访问频率作为控制;
 * 每次访问weight + 1;每次set且成员数量溢出时，所有weight -1;
 * 所以此缓存适用于maxSize大于需要缓存的数量时，且读多写少的情况
 * @author huitoukest
 */
public class SimpleCacheHelper<K,V> {
    private int maxSize;
    private int currentSize = 0;
    /**
     */
    private Map<K,SimpleCacheMember<V>> map;

    private static  final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private static  final Lock  readLock = readWriteLock.readLock();
    private static  final Lock  writeLock = readWriteLock.writeLock();

    public SimpleCacheHelper(int maxSize){
        map = new HashMap<>();
        this.maxSize = maxSize;
    }

    /**
     * 从缓存中取值，没有这返回null
     * @param key
     * @return
     */
    public V get(K key){
        SimpleCacheMember<V> member = null;
        try{
            readLock.lock();
            member = map.get(key);
            if(member != null) {
                member.setWeight(member.getWeight() + 1);
                return member.getValue();
            }
        }finally {
            readLock.unlock();
        }
        return null;
    }

   private <T> T doInReadLock(Supplier<T> supplier){
       try{
           readLock.lock();
           return supplier.get();
       }finally {
           readLock.unlock();
       }
   }
    /**
     * 设置一个缓存值，如果是新set的成员，weight值+2
     * @param key
     * @param value
     */
    public void set(K key,V value){
        try{
            writeLock.lock();
            SimpleCacheMember<V> member = map.get(key);
            if(member == null){
                member = new SimpleCacheMember<>();
                member.setWeight(member.getWeight() + 2);
                currentSize ++ ;
            }else{
                member.setWeight(member.getWeight() + 1);
                removeOverMember();
            }
            member.setValue(value);
            map.put(key,member);
        }finally {
            writeLock.unlock();
        }
    }

    /**
     * 是否包含key
     * @param key
     * @return
     */
    public  boolean containsKey(K key){
        return doInReadLock(() -> this.map.containsKey(key));
    }

    /**
     * 是否包含value
     * @param value
     * @return
     */
    public  boolean containsValue(V value){
        return doInReadLock(() -> this.map.containsValue(value));
    }

    /**
     * 将溢出的成员移出，移出权重值最低的成员;
     * 此中计算方法因为一直递增，有溢出的风险，溢出后自动清除
     */
    private void removeOverMember(){
        while(currentSize > maxSize){
            int minWeight = Integer.MAX_VALUE;
            Object minWeightKey = null;
            Set<K> keys = map.keySet();
            for(Object key : keys){
                SimpleCacheMember<V> member = map.get(key);
                if(member.getWeight() < minWeight){
                    minWeight = member.getWeight();
                    minWeightKey = key;
                }
                member.setWeight(member.getWeight() - 1);
            }
            if(minWeightKey != null){
                map.remove(minWeightKey);
                currentSize -- ;
            }
        }
    }
}