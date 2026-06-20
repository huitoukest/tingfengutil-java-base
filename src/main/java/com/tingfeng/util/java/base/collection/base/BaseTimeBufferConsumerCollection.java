package com.tingfeng.util.java.base.collection.base;

import com.tingfeng.util.java.base.lang.inter.collection.BufferConsumerCollection;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class BaseTimeBufferConsumerCollection<T> implements BufferConsumerCollection<T> {

    private int checkInterval = 1;
    private static final ThreadPoolExecutor POOL_EXECUTOR = new ThreadPoolExecutor(2, 10, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());
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
