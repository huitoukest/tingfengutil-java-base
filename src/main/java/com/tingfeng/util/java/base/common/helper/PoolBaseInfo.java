package com.tingfeng.util.java.base.common.helper;

public class PoolBaseInfo {
    private int minSize = 0;
    private int maxSize = 100;
    private int maxQueueSize = Integer.MAX_VALUE;
    private long maxRunTime = Integer.MAX_VALUE;
    /**
     * 默认等待获取实例的最长等待时间,perWaitTime的整数
     */
    private long maxWaitTime = Integer.MAX_VALUE;
    /**
     * 运行的实例最长闲置时间，默认2分钟
     */
    private long maxIdleTime = 120000;
    /**
     * 每隔perCheckTime便会检查一次当前池中实例的运行空闲和超时情况，默认10毫秒;
     */
    private long perCheckTime = 10;
    /**
     * 每隔perWaitTime,当前等待的open的任务便会尝再次open获取资源，默认10毫秒;
     */
    private long perWaitTime = 10;
    
    public PoolBaseInfo() {}
    
    /**
     * 
     * @param minSize 保持的最小实例数量
     * @param maxSize 运行最大并发数量
     */
    public PoolBaseInfo(int minSize, int maxSize) {
        super();
        this.minSize = minSize;
        this.maxSize = maxSize;
    }
    
    /**
     * 
     * @param minSize 保持的最小实例数量
     * @param maxSize 运行最大并发数量
     * @param maxRunTime  最长运行时间，超过时间运行超时会自动关闭实例
     */
    public PoolBaseInfo(int minSize, int maxSize, long maxRunTime) {
        super();
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.maxRunTime = maxRunTime;
    }
    
    /**
     * 
     * @param minSize 保持的最小实例数量
     * @param maxSize 运行最大并发数量
     * @param maxRunTime  最长运行时间，超过时间运行超时会自动关闭实例
     * @param maxIdleTime  运行的实例最长闲置时间，默认2分钟
     */
    public PoolBaseInfo(int minSize, int maxSize, long maxRunTime, long maxIdleTime) {
        super();
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.maxRunTime = maxRunTime;
        this.maxIdleTime = maxIdleTime;

    }
    /**
     * 
     * @param minSize 保持的最小实例数量
     * @param maxSize 运行最大并发数量
     * @param maxQueueSize 最呆等待队列长度
     * @param maxRunTime  最长运行时间，超过时间运行超时会自动关闭实例
     * @param maxIdleTime  运行的实例最长闲置时间，默认2分钟
     */
    public PoolBaseInfo(int minSize, int maxSize, int maxQueueSize, long maxRunTime, long maxIdleTime) {
        super();
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.maxQueueSize = maxQueueSize;
        this.maxRunTime = maxRunTime;
        this.maxIdleTime = maxIdleTime;
    }
    
    /**
     * 
     * @param minSize 保持的最小实例数量
     * @param maxSize 运行最大并发数量
     * @param maxQueueSize 最呆等待队列长度
     * @param maxRunTime  最长运行时间，超过时间运行超时会自动关闭实例
     * @param maxWaitTime 默认等待获取实例的最长等待时间
     * @param maxIdleTime 运行的实例最长闲置时间，默认2分钟
     */
    
    public PoolBaseInfo(int minSize, int maxSize, int maxQueueSize, long maxRunTime, long maxWaitTime,
            long maxIdleTime) {
        super();
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.maxQueueSize = maxQueueSize;
        this.maxRunTime = maxRunTime;
        this.maxWaitTime = maxWaitTime;
        this.maxIdleTime = maxIdleTime;
    }
    public long getMaxWaitTime() {
        return maxWaitTime;
    }
    public void setMaxWaitTime(long maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }
    public int getMinSize() {
        return minSize;
    }

    public void setMinSize(int minSize) {
        this.minSize = minSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public long getMaxRunTime() {
        return maxRunTime;
    }

    public void setMaxRunTime(long maxRunTime) {
        this.maxRunTime = maxRunTime;
    }

    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public long getPerCheckTime() {
        return perCheckTime;
    }

    public void setPerCheckTime(long perCheckTime) {
        this.perCheckTime = perCheckTime;
    }

    public long getPerWaitTime() {
        return perWaitTime;
    }

    public void setPerWaitTime(long perWaitTime) {
        this.perWaitTime = perWaitTime;
    }
    
    
    
}
