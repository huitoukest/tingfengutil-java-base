package com.tingfeng.util.java.base.common.helper;

import com.tingfeng.util.java.base.common.bean.SimpleCacheMember;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 实现一个简单的缓存,默认使用访问频率作为控制;
 * 每次访问weight + 1;每次set且成员数量溢出时，所有weight -1;
 * 所以此缓存适用于maxSize大于需要缓存的数量时
 * @author huitoukest
 */
public class SimpleCacheHelper<T> {
    private int maxSize;
    private int currentSize = 0;
    /**
     */
    private Map<Object,SimpleCacheMember<T>> map;

    public SimpleCacheHelper(int maxSize){
        map = new HashMap<>();
        this.maxSize = maxSize;
    }

    /**
     * 从缓存中取值，没有这返回null
     * @param key
     * @return
     */
    public synchronized T get(Object key){
        SimpleCacheMember<T> member = map.get(key);
        if(null == member){
            return null;
        }
        member.setWeight(member.getWeight() + 1);
        return member.getValue();
    }

    /**
     * 设置一个缓存值，如果是新set的成员，weight值+2
     * @param key
     * @param value
     */
    public synchronized void set(Object key,T value){
        SimpleCacheMember<T> member = map.get(key);
        if(member == null){
            member = new SimpleCacheMember<>();
            member.setWeight(member.getWeight() + 2);
            currentSize ++ ;
        }else{
            member.setValue(value);
            member.setWeight(member.getWeight() + 1);
        }
        map.put(key,member);
    }

    /**
     * 将溢出的成员移出，移出权重值最低的成员
     */
    private void removeOverMember(){
        while(currentSize > maxSize){
            int minWeight = Integer.MAX_VALUE;
            Object minWeightKey = null;
            Set<Object> keys = map.keySet();
            for(Object key : keys){
                SimpleCacheMember<T> member = map.get(key);
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