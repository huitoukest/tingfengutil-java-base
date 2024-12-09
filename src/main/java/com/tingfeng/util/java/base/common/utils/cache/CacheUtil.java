package com.tingfeng.util.java.base.common.utils.cache;

import com.tingfeng.util.java.base.common.constant.CacheType;
import com.tingfeng.util.java.base.common.exception.InfoException;
import com.tingfeng.util.java.base.common.utils.ThreadUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 本地缓存工具
 * 1. 此工具改为对象新建方式
 * 2. 在创立一个工具来管理此缓存对象，实现缓存分组的概念，一个组一个Lock，POOL则是通用的。或者使用多个 ReentrantReadWriteLock 的方式作为分组key
 */
public class CacheUtil {
    private static final int CACHE_POOL_CORE_SIZE = 4;
    private static final ThreadPoolExecutor CACHE_POOL = new ThreadPoolExecutor(CACHE_POOL_CORE_SIZE,CACHE_POOL_CORE_SIZE,1, TimeUnit.MINUTES,new ArrayBlockingQueue<>(16),
            ThreadUtils.newNamedThreadFactory("cachePool",false), new ThreadPoolExecutor.DiscardPolicy());
    /**
     * 缓存过期的检查间隔,单位毫秒,可配置(大于0)
     */
    public static long checkInterval = 50;

    private static final Map<Object, Cache> CACHE_MAP = new HashMap<>();
    static {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    CACHE_MAP.forEach((k, v) -> {
                        while (true) {
                            if(CACHE_POOL.getActiveCount() >= CACHE_POOL_CORE_SIZE){
                                ThreadUtils.sleep(checkInterval);
                                continue;
                            }
                            CACHE_POOL.execute(() -> v.eliminationData());
                            break;
                        }
                    });
                } finally {
                    ThreadUtils.sleep(checkInterval);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 获取一个缓存实例
     * @param cacheType 缓存类型
     * @param cacheFlag 缓存的标识(全局唯一)
     * @return 缓存实例
     * @param <K>
     * @param <V>
     */
    public static <K,V> Cache<K,V> getCache(CacheType cacheType,Object cacheFlag){
        Cache cache = CACHE_MAP.get(cacheFlag);
        if(cache == null) {
            synchronized (cacheFlag) {
                cache = CACHE_MAP.get(cacheFlag);
                if(cache == null){
                    cache = getCache(cacheType);
                }
                CACHE_MAP.put(cacheFlag, cache);
            }
        }
        return cache;
    }

    private static <K,V> Cache<K,V> getCache(CacheType cacheType){
        switch (cacheType){
            case TIME_MEMORY:{
                return new TimeMemoryCache<>();
            }
            default:{
                throw new InfoException("invalid cacheType");
            }
        }
    }
}
