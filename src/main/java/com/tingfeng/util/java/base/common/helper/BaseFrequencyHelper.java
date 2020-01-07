package com.tingfeng.util.java.base.common.helper;

/**
 * 每秒的频率控制类
 * @author wanggang
 */
public abstract class BaseFrequencyHelper {
    /**
     * 每秒内的增量
     */
    private  int incrementalCount = 0;
    private  long lastSecond = System.currentTimeMillis() / 1000;
    /**
     * 每秒的最大访问次数
     */
    private Integer secondMaxCount;

    public BaseFrequencyHelper(){

    }

    public BaseFrequencyHelper(Integer secondMaxCount){
        this.secondMaxCount = secondMaxCount;
    }

    /**
     * 检查频率并在超出频率后睡眠一段时间再继续
     * @throws InterruptedException
     */
    public synchronized void checkAndLimitFrequency(){
        if(incrementalCount++ >= secondMaxCount){
            long currentSecond = System.currentTimeMillis() / 1000;
            if (lastSecond != currentSecond) {
                lastSecond = currentSecond;
                incrementalCount = 0;
            }else{
                long sleepTime = getSleepTimeWhileOverMaxCount(incrementalCount,secondMaxCount);
                if(sleepTime > 0 ) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                checkAndLimitFrequency();
            }
        }
    }

    /**
     * 获取达到条件后的随眠时间，在每一次有访问时调用
     * @param incrementalCount 当期秒内的访问次数
     * @param secondMaxCount 每秒的最大访问次数
     * @return the sleepTime ,unit  millisecond 毫秒
     */
    public abstract long getSleepTimeWhileOverMaxCount(int incrementalCount,int secondMaxCount);
}
