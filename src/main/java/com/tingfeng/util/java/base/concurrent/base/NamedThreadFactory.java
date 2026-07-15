package com.tingfeng.util.java.base.concurrent.base;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

    /**
     * 线程名称前缀
     */
    private String prefix = "default";
    /**
     * 线程组
     */
    private ThreadGroup group;
    /**
     * 线程组内编号
     */
    private AtomicInteger threadNumber = new AtomicInteger(1);
    /**
     * 是否守护线程
     */
    private boolean isDaemon;
    /**
     * 无法捕获的异常统一处理
     */
    private Thread.UncaughtExceptionHandler handler;
    /**
     * 线程优先级别
     */
    private int threadPriority = Thread.NORM_PRIORITY;


    public NamedThreadFactory(String prefix,ThreadGroup group,boolean isDaemon){
        this.prefix = prefix;
        this.group = group;
        this.isDaemon = isDaemon;
    }

    public NamedThreadFactory(String prefix,boolean isDaemon){
        this(prefix, null, isDaemon);
    }

    public NamedThreadFactory(String prefix, boolean isDaemon, int startIndex) {
        this.prefix = prefix;
        this.isDaemon = isDaemon;
        this.threadNumber = new AtomicInteger(startIndex);
    }

    @Override
    public Thread newThread(Runnable r) {
        final Thread t = new Thread(this.group, r, String.format("%s%s", prefix, threadNumber.getAndIncrement()));
        //设置守护线程
        t.setDaemon(isDaemon);
        //异常处理
        t.setUncaughtExceptionHandler(handler);
        //优先级
        t.setPriority(threadPriority);
        return t;
    }

    public NamedThreadFactory setPriority(int threadPriority) {
        this.threadPriority = threadPriority;
        return this;
    }

    public NamedThreadFactory setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler) {
        this.handler = handler;
        return this;
    }
}
