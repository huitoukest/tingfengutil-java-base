package com.tingfeng.util.java.base.common.helper;

import com.tingfeng.util.java.base.common.exception.BaseException;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 一个简单的池工具，提供最大并发数和资源缓存等工具；
 * 效率低于FixedPoolHelper。
 * 实现了 AutoCloseable 接口，支持 try-with-resources 语法
 *
 * @param <T> 池中的资源类型
 * @author huitoukest
 */
public class SimplePoolHelper<T> implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(SimplePoolHelper.class);

    public static final int DEFAULT_MAX_THREAD_SIZE = 4;
    private final int maxThreadCount;
    private int useSize = 0;
    private int idleSize = 0;
    private final List<T> useMembers = new LinkedList<>();
    private final List<T> idleMembers = new LinkedList<>();
    private final Callable<T> openAction;
    private long perSleepTime = 1;
    /**
     *
     * @param openAction 用于打开一个新的资源
     */
    public SimplePoolHelper(Callable<T> openAction){
            this(DEFAULT_MAX_THREAD_SIZE,openAction);
    }

    /**
     *
     * @param maxThreadSize
     * @param openAction
     */
    public SimplePoolHelper(int maxThreadSize, Callable<T> openAction){
       if(maxThreadSize < 0){
           maxThreadCount = 1;
       }else {
           maxThreadCount = maxThreadSize;
       }
       this.openAction = openAction;
    }

    /**
     * 获取资源
     * 当资源不足时，会等待直到有资源可用或创建新资源
     * @return 池中的资源
     */
    public T get(){
        try {
            while (true) {
                synchronized(SimplePoolHelper.this){
                    if(idleSize > 0){
                        // 从空闲池获取资源
                        idleSize --;
                        T t = idleMembers.get(0);
                        idleMembers.remove(0);
                        useSize++;
                        useMembers.add(t);
                        return t;
                    }else if(useSize < maxThreadCount){
                        // 创建新资源
                        T t = openAction.call();
                        useSize++;
                        useMembers.add(t);
                        return t;
                    }
                }
                // 资源不足，等待
                if(getPerSleepTime() > 0) {
                    Thread.sleep(getPerSleepTime());
                }
            }
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 释放资源
     * 将资源返回空闲池，等待下一次使用
     * @param t 要释放的资源
     */
    public void release(T t){
        synchronized(SimplePoolHelper.this) {
            boolean re = useMembers.remove(t);
            if (re) {
                idleMembers.add(t);
                idleSize++;
                useSize--;
            }
        }
    }

    public long getPerSleepTime() {
        return perSleepTime;
    }

    public void setPerSleepTime(long perSleepTime) {
        this.perSleepTime = perSleepTime;
    }

    /**
     * 获取使用中的资源数量
     * @return 使用中的资源数量
     */
    public synchronized int getUseSize() {
        return useSize;
    }

    /**
     * 获取空闲资源数量
     * @return 空闲资源数量
     */
    public synchronized int getIdleSize() {
        return idleSize;
    }

    /**
     * 获取最大线程数
     * @return 最大线程数
     */
    public synchronized int getMaxThreadCount() {
        return maxThreadCount;
    }

    /**
     * 关闭所有资源
     * 实现 AutoCloseable 接口，支持 try-with-resources 语法
     */
    @Override
    public void close() {
        synchronized(SimplePoolHelper.this) {
            // 关闭使用中的资源
            for (T resource : useMembers) {
                if (resource instanceof AutoCloseable) {
                    try {
                        ((AutoCloseable) resource).close();
                    } catch (Exception e) {
                        // 记录异常，但不影响其他资源的关闭
                        logger.warn("Failed to close resource", e);
                    }
                }
            }
            // 关闭空闲资源
            for (T resource : idleMembers) {
                if (resource instanceof AutoCloseable) {
                    try {
                        ((AutoCloseable) resource).close();
                    } catch (Exception e) {
                        // 记录异常，但不影响其他资源的关闭
                        logger.warn("Failed to close resource", e);
                    }
                }
            }
            // 清空资源列表
            useMembers.clear();
            idleMembers.clear();
            useSize = 0;
            idleSize = 0;
        }
    }
}