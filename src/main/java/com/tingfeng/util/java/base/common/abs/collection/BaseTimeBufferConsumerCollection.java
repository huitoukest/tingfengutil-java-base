package com.tingfeng.util.java.base.common.abs.collection;

import com.tingfeng.util.java.base.common.inter.collection.BufferConsumerCollection;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class BaseTimeBufferConsumerCollection<T> implements BufferConsumerCollection<T> {

    private int checkInterval = 1;
    private static final ThreadPoolExecutor POOL_EXECUTOR = new ThreadPoolExecutor(1,Integer.MAX_VALUE,10, TimeUnit.SECONDS,new SynchronousQueue<>());
    /**
     * 检查的间隔时间，单位毫秒
     * @param checkInterval
     */
    public BaseTimeBufferConsumerCollection(int checkInterval) {
        if(checkInterval < 1){
                throw new IllegalArgumentException("checkInterval must great than 0");
        }
        this.checkInterval = checkInterval;
        initTask();
    }
    private void initTask(){
        POOL_EXECUTOR.submit(() -> {
                while (true) {
                    try {
                        consumerIfMatch();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            Thread.sleep(checkInterval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            });
    }

    @Override
    public abstract void add(T t);

    public abstract void consumerIfMatch();
}
