package com.tingfeng.util.java.base.common.bean.collection;

import com.tingfeng.util.java.base.common.abs.collection.BaseTimeBufferConsumerCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 线程安全，基于时间和缓冲区触发消费的List
 * @param <T>
 */
public class TimeBufferConsumerList<T> extends BaseTimeBufferConsumerCollection<T> {

    private final int batchSize;
    private final Consumer<List<T>> consumer;
    private final int maxHoldMs;
    private LinkedList<List<T>> buffer = new LinkedList<>() ;
    private List<T> currentBuffer = new ArrayList<>();;
    private volatile long lastConsumerTime = System.currentTimeMillis();

    /**
     * if the add elements size >= batchSize, or maxHoldMs milliseconds elapsed and 0 <= elements size ;
     * then call the consumerIfMatch, to invoke to consumer elements
     * @param checkInterval
     * @param batchSize must less than maxIntValue / 2;
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
    public void add(T t) {
        synchronized(this.buffer) {
            this.currentBuffer.add(t);
            if(this.currentBuffer.size() >= batchSize){
                this.buffer.add(currentBuffer);
                this.currentBuffer = new ArrayList<>();
            }
        }
        consumerIfMatch();
    }

    @Override
    public void consumerIfMatch() {
        List<T> consumerList = Collections.emptyList();
        boolean timeMatch = System.currentTimeMillis() - lastConsumerTime >= maxHoldMs;
        synchronized(this.buffer) {
            if(timeMatch){
                this.buffer.add(currentBuffer);
                this.currentBuffer = new ArrayList<>();
            }
            List<T> firstList = this.buffer.stream().findFirst().orElse(Collections.emptyList());
            if(timeMatch || firstList.size() >= batchSize) {
                consumerList = firstList;
                this.buffer.removeFirst();
            }
        }
        if(!consumerList.isEmpty()) {
            this.consumer.accept(consumerList);
            lastConsumerTime = System.currentTimeMillis();
        }
    }
}
