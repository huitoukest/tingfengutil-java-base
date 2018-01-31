package com.tingfeng.util.java.base.common;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.tingfeng.util.java.base.common.helper.PoolBaseInfo;
import com.tingfeng.util.java.base.common.helper.PoolHelper;
import com.tingfeng.util.java.base.common.inter.PoolMemberActionI;

public class PoolHelperTest {
    
    @Test
    public void testPoolHelper() {
        final AtomicInteger atom = new AtomicInteger(0);
        PoolBaseInfo baseInfo = new PoolBaseInfo();
        baseInfo.setMaxSize(10);
        baseInfo.setMaxRunTime(50000);
        baseInfo.setMaxQueueSize(100);
        baseInfo.setMaxIdleTime(6000);
        baseInfo.setMaxWaitTime(Integer.MAX_VALUE);
        final Set<Object> set = new HashSet<Object>();
        final PoolHelper<Runnable> poolHelper = new  PoolHelper<Runnable>(new PoolMemberActionI<Runnable>() {

            @Override
            public Runnable create() {
                return new  Runnable() {
                    @Override
                    public void run() {
                        long time = (long) (Math.random() * 1000);
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
        },baseInfo);
        
        for(int i = 0 ; i< 100 ;i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {               
                    Runnable run = poolHelper.open();                    
                    atom.incrementAndGet();
                    synchronized(PoolHelperTest.class) {
                        set.add(run);
                        System.out.println("atom count : " + atom.get() + ",setSize:" + set.size());
                    }
                    run.run();
                    atom.decrementAndGet();
                    poolHelper.close(run);
                    
                }
            }).start();
        }
        try {
            System.in.read();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
