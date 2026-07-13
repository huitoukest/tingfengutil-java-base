package com.tingfeng.util.java.base.collection.base;

import com.tingfeng.util.java.base.LogUtils;
import com.tingfeng.util.java.base.lang.inter.collection.BufferConsumerCollection;

public abstract class BaseTimeBufferConsumerCollection<T> implements BufferConsumerCollection<T>, AutoCloseable {

    private final int checkInterval;
    private final Thread consumerThread;
    private volatile boolean running = true;

    /**
     * 检查的间隔时间，单位毫秒
     * @param checkInterval 检查间隔，必须大于0
     */
    public BaseTimeBufferConsumerCollection(int checkInterval) {
        if(checkInterval < 1){
                throw new IllegalArgumentException("checkInterval must great than 0");
        }
        this.checkInterval = checkInterval;
        this.consumerThread = initThread();
    }

    private Thread initThread() {
        Thread thread = new Thread(() -> {
            while (running) {
                try {
                    consumerIfMatch();
                } catch (Exception e) {
                    LogUtils.error("BaseTimeBufferConsumerCollection consumerIfMatch error", e);
                }
                if (running) {
                    try {
                        Thread.sleep(checkInterval);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.setName("buffer-consumer-" + System.identityHashCode(this));
        thread.start();
        return thread;
    }

    /**
     * 关闭此消费者，停止后台检查线程并清理资源
     */
    public void shutdown() {
        running = false;
        if (consumerThread != null) {
            consumerThread.interrupt();
        }
    }

    /**
     * 实现 AutoCloseable，支持 try-with-resources 方式关闭
     */
    @Override
    public void close() {
        shutdown();
    }

    @Override
    public abstract void add(T t);

    public abstract void consumerIfMatch();
}
