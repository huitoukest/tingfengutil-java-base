package com.java.base.threads;

import com.tingfeng.util.java.base.common.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author huitoukest
 * @Description 测试condition的功能
 * @Date 2019-06-19 16:20
 **/
public class ConditionTest {

    /**
     * 模拟生产者与消费者
     */
    @Test
    public void conditionTest() {
        int maxSize = 10;
        //锁对象
        Lock lock = new ReentrantLock();
        //生产者条件
        Condition producerCondition = lock.newCondition();
        //消费者条件
        Condition consumerCondition = lock.newCondition();
        //产品池
        List<Integer> productPool = new ArrayList<>((int) (maxSize * 1.5));
        // 减少执行次数：5个线程，每个线程200次循环
        TestUtils.printTime(10, 200, (threadIndex, innerIndex) -> {
            //即定义奇数线程是消费者,其余的为生产者，消费者
            if ((threadIndex & 1) == 1) {
                try {
                    lock.lock();
                    //没有产品了
                    while (productPool.size() == 0) {
                        //释放条件直到被环境(缓冲池满了就会被唤醒)或测试完成
                        consumerCondition.await();
                    }
                    //这个时候由于锁实质上之前是释放的状态，所以再次被唤醒
                    //的时候池中的产品数量已经有很多了，且数量不固定（但是小于max值）
                    productPool.remove(0);
                    producerCondition.signal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            } else {
                //生产者
                try {
                    lock.lock();
                    //生产池子满了
                    while (productPool.size() == maxSize) {
                        //生产者释放条件，将锁让与其余想要获取此锁的等待中的线程
                        producerCondition.await();
                    }
                    productPool.add(1);
                    consumerCondition.signal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            }
        });
        // 测试完成，唤醒所有等待的线程
        try {
            lock.lock();
            // 唤醒所有等待的生产者和消费者
            producerCondition.signalAll();
            consumerCondition.signalAll();
        } finally {
            lock.unlock();
        }
        Assert.assertTrue(productPool.isEmpty());
    }
    
    /**
     * 测试Condition基本功能
     */
    @Test
    public void testConditionBasic() {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        final boolean[] signalReceived = {false};
        
        Thread signalThread = new Thread(() -> {
            try {
                Thread.sleep(100);
                lock.lock();
                try {
                    signalReceived[0] = true;
                    condition.signal();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        signalThread.start();
        
        try {
            lock.lock();
            try {
                // 等待信号，最多等待1秒
                boolean signaled = condition.await(1, java.util.concurrent.TimeUnit.SECONDS);
                Assert.assertTrue("应该收到信号", signaled);
                Assert.assertTrue("信号接收标志应该为true", signalReceived[0]);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail("线程被中断");
        }
        
        try {
            signalThread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}