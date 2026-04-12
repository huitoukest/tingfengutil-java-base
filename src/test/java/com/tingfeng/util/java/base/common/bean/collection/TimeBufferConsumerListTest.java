package com.tingfeng.util.java.base.common.bean.collection;

import com.tingfeng.util.java.base.common.utils.RandomUtils;
import com.tingfeng.util.java.base.common.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TimeBufferConsumerListTest {

    @Test
    public void test() throws InterruptedException {
        AtomicInteger consumerSize = new AtomicInteger(0);
        TimeBufferConsumerList<Integer> timeBufferConsumerList = new TimeBufferConsumerList<>(10,64,100,list -> {
            int size = list.size();
            System.out.println("Buffer1,当前时间:" + System.currentTimeMillis() +",当前消费数量:" + size + ",消费总数:" + consumerSize.addAndGet(size));
        });
        TimeBufferConsumerList<Integer> timeBufferConsumerList2 = new TimeBufferConsumerList<>(10,64,100,list -> {
            int size = list.size();
            System.out.println("Buffer2,当前时间:" + System.currentTimeMillis() +",当前消费数量:" + size + ",消费总数:" + consumerSize.addAndGet(size));
        });
        int thread = 10;
        int cycle = 50;
        int total = thread * cycle;
        CountDownLatch latch = new CountDownLatch(1);
        
        TestUtils.printTime(thread,cycle, index -> {
            try {
                Thread.sleep(RandomUtils.randomInt(2,10));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
            timeBufferConsumerList.add(1);
            timeBufferConsumerList2.add(1);
            
            // 当所有元素都添加完成后，通知主线程
            if (index == thread * cycle - 1) {
                latch.countDown();
            }
        });

        // 等待所有元素添加完成
        latch.await(5, TimeUnit.SECONDS);
        
        // 等待消费者处理完成，最多等待10秒
        long startTime = System.currentTimeMillis();
        while (consumerSize.intValue() < total * 2 && System.currentTimeMillis() - startTime < 10000) {
            Thread.sleep(10);
        }
        
        Thread.sleep(500); // 给消费者一点额外时间完成处理
        Assert.assertEquals(total * 2, consumerSize.intValue());
    }
}