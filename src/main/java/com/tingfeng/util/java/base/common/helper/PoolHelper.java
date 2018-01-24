package com.tingfeng.util.java.base.common.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.tingfeng.util.java.base.common.bean.PoolMember;
import com.tingfeng.util.java.base.common.exception.OverPoolWaitSizeException;
import com.tingfeng.util.java.base.common.exception.OverPoolWaitTimeException;
import com.tingfeng.util.java.base.common.inter.PoolMemberActionI;
/**
 * 任务池工具
 * @author WangGang
 *
 * @param <T>
 */
public class PoolHelper<T>{
    private PoolMemberActionI<T>  poolMemberAction;
    private PoolBaseInfo poolBaseInfo;
    private final static int tryTime = 10;//阻塞时每0.01秒尝试一次获取
    private final AtomicInteger waitCount = new AtomicInteger(0);
    private boolean isShutDown = false;
    private boolean isRunning = false;
    private final Object openLockKey = new Object();
    
    private final List<PoolMember<T>>  runingList = new ArrayList<>(10);
    private final List<PoolMember<T>>  idleList = new ArrayList<>(10);    
    private final List<PoolMember<T>>  delList = new ArrayList<>(10);
    private final Map<T,PoolMember<T>> runMap = new HashMap<>(10);
    private Thread thread = null;
    
    /**
     * 资源检查和操作
     */
    private  void workCheck() {
        if(null == thread) {
            thread =  new Thread(new Runnable() {    
                @Override
                public void run() {
                    while(true) {
                        synchronized(PoolHelper.this) {
                            //try {
                            delList.clear();
                            PoolMember<T> member = null;
                                try {
                                    //找出闲置,运行超时
                                    for(int j = 0; j < PoolHelper.this.runingList.size() ; j ++) {
                                        member =  PoolHelper.this.runingList.get(j);
                                        try {
                                            if(isOverMaxRunTime(member)) {
                                                PoolHelper.this.poolMemberAction.onOverMaxRunTime(member.getMember());
                                            }
                                        }catch (Exception e) {
                                            e.printStackTrace();
                                        }finally {
                                            PoolHelper.this.delList.add(member);
                                        }
                                        if(!member.isUse()) {
                                            PoolHelper.this.idleList.add(member);
                                        }
                                    }
                                    //消除运行超时
                                    for(int j = 0; j < PoolHelper.this.delList.size() ; j ++) {
                                        member =  PoolHelper.this.delList.get(j);
                                        PoolHelper.this.runingList.remove(member);
                                        PoolHelper.this.poolMemberAction.destroy(member.getMember());
                                    }
                                    PoolHelper.this.delList.clear();
                                    //找出闲置超时
                                    for(int j = 0; j < PoolHelper.this.idleList.size() ; j ++) {
                                        member =PoolHelper. this.idleList.get(j);
                                        if(PoolHelper.this.isOverMaxIdleTime(member)) {
                                            PoolHelper.this.delList.add(member);
                                        }
                                    }
                                   //消除闲置超时
                                    for(int j = 0; j < PoolHelper.this.delList.size() ; j ++) {
                                        member =  PoolHelper.this.delList.get(j);
                                        PoolHelper.this.idleList.remove(member);
                                        PoolHelper.this.poolMemberAction.destroy(member.getMember());
                                    }
                                }catch (Exception e) {
                                   e.printStackTrace();
                                   PoolHelper.this.poolMemberAction.onWorkException(e);
                                }
                                //如果停止标记，并且当前再没有任务
                                if(isShutDown && PoolHelper.this.idleList.size() == 0 && PoolHelper.this.runingList.size() == 0) {
                                    isRunning = false;
                                    break;
                                }
                        }//end synchronized
                        try {
                            Thread.sleep(tryTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }    
                    }//end while
                }//end run
            });
        }
        if(!isRunning && !isShutDown) {
            isRunning = true;
            thread.start();
        }
    }
    
    
    
    public PoolHelper(PoolMemberActionI<T>  poolMemberAction,PoolBaseInfo poolBaseInfo) {
        this.poolBaseInfo = poolBaseInfo;
        this.poolMemberAction = poolMemberAction;
        startPoll();
    }
    
    public PoolHelper(PoolMemberActionI<T>  poolMemberAction) {
        this.poolBaseInfo =  new PoolBaseInfo();
        this.poolMemberAction = poolMemberAction;
        startPoll();
    }    
    
    public synchronized int getPoolSize() {
        return runingList.size() + idleList.size();
    }
    
    private synchronized int getRunListSize() {
        return runingList.size();
    }
    
    public T open() { 
        if(waitCount.get() > poolBaseInfo.getMaxQueueSize()) {
            throw new OverPoolWaitSizeException("wait size:" + poolBaseInfo.getMaxQueueSize());
        }
        PoolMember<T> member = null;
        T t = null;
        int tryCount = 0 ;
        int maxTryCount = (int) (poolBaseInfo.getMaxWaitTime() / tryTime);
        
        waitCount.incrementAndGet();
        try {
            do {
                if(tryCount  >= maxTryCount) {
                    throw new OverPoolWaitTimeException("wait time:" + poolBaseInfo.getMaxWaitTime());
                }
                synchronized(this) {
                    int runSize = getRunListSize();
                    if(isShutDown) {
                        throw new RuntimeException("pool has shutDown,if you want to work agin,to startPoll");
                    }
                    
                    if(runSize < poolBaseInfo.getMaxSize()) {//如果没有达到最大队列
                        t = poolMemberAction.create();
                        member = new PoolMember<>();
                        member.setMember(t);
                        runingList.add(member);    
                    }else if(idleList.size() > 0) {
                            member =  idleList.get(0);
                            runingList.add(member);
                            member.setUpdateTime(System.currentTimeMillis());
                            idleList.remove(0);
                            t = member.getMember();
                    }
                    if(null != member) {
                        member.setUse(true);
                        runMap.put(t,member);
                    }
                }
                tryCount ++ ;
                try {
                    Thread.sleep(tryTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }while(t == null);   
        }finally {
            waitCount.decrementAndGet();
        }
        return t;
    }
    
    protected boolean isOverMaxIdleTime(PoolMember<T> member) {
        if(!member.isUse() && System.currentTimeMillis() - member.getUpdateTime() > poolBaseInfo.getMaxIdleTime()) {
            return true;
        }
        return false;
    }
    
    protected boolean isOverMaxRunTime(PoolMember<T> member) {
        if(member.isUse() && System.currentTimeMillis() - member.getUpdateTime() > poolBaseInfo.getMaxRunTime()) {
            return true;
        }
        return false;
    }
    
    public void close(T t) {
       PoolMember<T> poolMember = runMap.get(t);       
       if(null != poolMember) {
               poolMember.setUse(false);
               runMap.remove(t);
       }
    }
    
    /**
     * when all member is close,it will shutDown
     */
    public synchronized void shutDownPoll() {
        isShutDown = true;
    }
    
    /**
     * when all member is close,it will shutDown
     */
    public synchronized void startPoll() {
        isShutDown = false;
        workCheck();
    }
    
}
