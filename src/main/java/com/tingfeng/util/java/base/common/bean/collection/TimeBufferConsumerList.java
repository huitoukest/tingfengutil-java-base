package com.tingfeng.util.java.base.common.bean.collection;

import com.tingfeng.util.java.base.common.abs.collection.BaseTimeBufferConsumerCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * 线程安全，基于时间和缓冲区触发消费的List
 * @param <T>
 */
public class TimeBufferConsumerList<T> extends BaseTimeBufferConsumerCollection<T> {

    private final int batchSize;
    private final Consumer<List<T>> consumer;
    private final int maxHoldMs;

    private List<T> buffer = new ArrayList<>(512);

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private long lastConsumerTime = System.currentTimeMillis();

    /**
     * if the add elements size >= batchSize, or maxHoldMs milliseconds elapsed and 0 <= elements size ;
     * then call the consumerIfMatch, to invoke to consumer elements
     * @param checkInterval
     * @param batchSize
     * @param maxHoldMs
     * @param consumer
     */
    public TimeBufferConsumerList(int checkInterval,int batchSize, int maxHoldMs,Consumer<List<T>> consumer) {
        super(checkInterval);
        if(checkInterval >= maxHoldMs){
           throw new IllegalArgumentException("checkInterval must great than maxHoldMs");
        }
        this.batchSize = batchSize;
        this.maxHoldMs = maxHoldMs;
        this.consumer = consumer;
    }

    public TimeBufferConsumerList(int batchSize, Consumer<List<T>> consumer) {
        this(batchSize, 128,5, consumer);
    }

    @Override
    public synchronized void add(T t) {
        consumerIfMatch();
        this.buffer.add(t);
    }

    @Override
    public synchronized void consumerIfMatch() {
        if(System.currentTimeMillis() - lastConsumerTime >= maxHoldMs || this.buffer.size() >= batchSize){
            if(!this.buffer.isEmpty()) {
                this.consumer.accept(Collections.unmodifiableList(this.buffer));
            }
            this.buffer.clear();
            lastConsumerTime = System.currentTimeMillis();
        }
    }
}
