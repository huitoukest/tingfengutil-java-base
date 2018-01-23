package com.tingfeng.util.java.base.common;

import org.junit.Test;

import com.tingfeng.util.java.base.common.helper.PoolBaseInfo;
import com.tingfeng.util.java.base.common.helper.PoolHelper;
import com.tingfeng.util.java.base.common.inter.PoolMemberActionI;

public class PoolHelperTest {
    
    @Test
    public void testPoolHelper() {
        PoolBaseInfo baseInfo = new PoolBaseInfo();
        baseInfo.setMaxSize(2);
        baseInfo.setMaxRunTime(5000);
        baseInfo.setMaxQueueSize(16);
        baseInfo.setMaxIdleTime(2000);
        baseInfo.setMaxWaitTime(Integer.MAX_VALUE);
        PoolHelper<Runnable> poolHelper = new  PoolHelper<Runnable>(new PoolMemberActionI<Runnable>() {

            @Override
            public Runnable create() {
                return new  Runnable() {                  
                    @Override
                    public void run() {
                        long time = (long) (Math.random() * 1000 * 10);
                        try {
                            Thread.sleep(time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("use Time : " + time + "  " + Thread.currentThread().getName());       
                    }
                };
            }
            
            @Override
            public boolean destroy(Runnable t) {
                System.out.println("destroy : " + t);
                return true;
            }

            @Override
            public void onOverMaxRunTime(Runnable t) {
                System.out.println("onOverMaxRunTime : " + t);
                
            }

            @Override
            public void onWorkException(Exception e) {
                System.out.println("onWorkException : " + e.getStackTrace() );
            }
        });
        
        for(int i = 0 ; i< 15 ;i++) {
            new Thread(poolHelper.open()).start();
        }
    }
}
