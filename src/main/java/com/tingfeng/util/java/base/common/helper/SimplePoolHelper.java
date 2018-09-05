package com.tingfeng.util.java.base.common.helper;

import com.tingfeng.util.java.base.common.exception.BaseException;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 一个简单的池工具，提供最大并发数和资源缓存等工具；
 * 效率低于FixedPoolHelper。
 */
public class SimplePoolHelper<T> {
    public static final int DEFAULT_MAX_THREAD_SIZE = 4;
    private int maxThreadCount = 4;
    private int useSize = 0;
    private int idleSize = 0;
    private List<T> useMembers = new LinkedList<>();
    private List<T> idleMembers = new LinkedList();
    private Callable<T> openAction ;
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

    public T get(){
        try {
            synchronized(SimplePoolHelper.this){
                if(idleSize + useSize <= maxThreadCount){
                    T t = null;
                    if(idleSize > 0){
                        idleSize --;
                        t = idleMembers.get(0);
                        idleMembers.remove(0);
                    }else if(useSize < maxThreadCount){
                        t = openAction.call();
                    }
                    if(t != null) {
                        useSize++;
                        useMembers.add(t);
                        return t;
                    }
                }
            }
            if(getPerSleepTime() > 0) {
                Thread.sleep(getPerSleepTime());
            }
            return get();
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

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
}
