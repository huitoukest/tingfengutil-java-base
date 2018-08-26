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
 * @param <T> T 就是pool实际打开或者释放的资源本身，比如常见的jdbc的数据库的连接对象
 */
public class PoolHelper<T>{
    /**
     * PoolMember 的动作
     */
    private PoolMemberActionI<T>  poolMemberAction;
    /**
     * 当前pool的配置信息
     */
    private PoolBaseInfo poolBaseInfo;
    /**
     * 当前等待对象的数量
     */
    private final AtomicInteger waitCount = new AtomicInteger(0);
    /**
     * 当前pool是否已经关闭
     */
    private boolean isShutDown = false;
    /**
     * 当前pool是否正在运行
     */
    private boolean isRunning = false;
    /**
     * 当前pool中正在开启使用的PoolMember列表
     */
    private final List<PoolMember<T>>  runingList = new ArrayList<>(10);
    /**
     * 当前总的pool中正在闲置的PoolMember列表
     */
    private final List<PoolMember<T>>  idleList = new ArrayList<>(10);
    /**
     * 每次查找后新增的闲置池,最终会在数据处理后合并到idleList中
     */
    private final List<PoolMember<T>>  idleListPer = new ArrayList<>(10);
    /**
     * 在每次检查pool后需要释放删除的PoolMember列表
     */
    private final List<PoolMember<T>>  delList = new ArrayList<>(10);
    /**
     * 当前运行资源的一个资源T和PoolMember的Map映射
     */
    private final Map<T,PoolMember<T>> runMap = new HashMap<>(10);
    /**
     * 默认的一个线程用来检查pool中的状态、对象和资源
     */
    private Thread thread = null;
    
    /**
     * 资源检查和操作，包括超时，队列长度超过，闲置资源的回收等
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
                            idleListPer.clear();
                            PoolMember<T> member = null;
                                try {
                                    //找出闲置,运行超时
                                    for(int j = 0; j < PoolHelper.this.runingList.size() ; j ++) {
                                        member =  PoolHelper.this.runingList.get(j);
                                        try {
                                            if(isOverMaxRunTime(member)) {
                                                PoolHelper.this.delList.add(member);
                                                PoolHelper.this.poolMemberAction.onOverMaxRunTime(member.getMember());
                                                continue;
                                            }
                                        }catch (Throwable e) {
                                            e.printStackTrace();
                                        }
                                        if(!member.isUse()) {
                                            PoolHelper.this.idleList.add(member);
                                            PoolHelper.this.idleListPer.add(member);
                                        }
                                    }
                                    //消除运行超时
                                    for(int j = 0; j < PoolHelper.this.delList.size() ; j ++) {
                                        member =  PoolHelper.this.delList.get(j);
                                        PoolHelper.this.runingList.remove(member);
                                        PoolHelper.this.poolMemberAction.destroy(member.getMember());
                                    }
                                    //消除新增的闲置超时
                                    PoolHelper.this.runingList.removeAll(idleListPer);
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
                                }catch (Throwable e) {
                                   e.printStackTrace();
                                   PoolHelper.this.poolMemberAction.onWorkException(member.getMember(),e);
                                }
                                //如果停止标记，并且当前再没有任务
                                if(isShutDown && PoolHelper.this.idleList.size() == 0 && PoolHelper.this.runingList.size() == 0) {
                                    isRunning = false;
                                    break;
                                }
                        }//end synchronized
                        try {
                            Thread.sleep(poolBaseInfo.getPerCheckTime());
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


    /**
     *
     * @param poolMemberAction PoolMemberActionI<T> 打开和释放资源的动作对象
     * @param poolBaseInfo PoolBaseInfo pool配置信息
     */
    public PoolHelper(PoolMemberActionI<T>  poolMemberAction,PoolBaseInfo poolBaseInfo) {
        this.poolBaseInfo = poolBaseInfo;
        this.poolMemberAction = poolMemberAction;
        startPool();
    }

    /**
     *
     * @param poolMemberAction PoolMemberActionI<T> 打开和释放资源的动作对象
     */
    public PoolHelper(PoolMemberActionI<T>  poolMemberAction) {
        this.poolBaseInfo =  new PoolBaseInfo();
        this.poolMemberAction = poolMemberAction;
        startPool();
    }

    /**
     *
     * @return 返回当前pool的size
     */
    public synchronized int getPoolSize() {
        return runingList.size() + idleList.size();
    }

    /**
     * 开启一个pool中的资源，如果pool中的正在运行的资源已经达到了maxSize，
     * 当前方法会阻塞，直到获取到资源T或者超过最大等待时间
     * 会抛出 OverPoolWaitSizeException，OverPoolWaitTimeException
     * @return 返回此资源
     */
    public T open() {
        if(waitCount.get() > poolBaseInfo.getMaxQueueSize()) {
            throw new OverPoolWaitSizeException("wait size:" + poolBaseInfo.getMaxQueueSize());
        }
        PoolMember<T> member = null;
        T t = null;
        int tryCount = 0 ;
        int maxTryCount = (int) (poolBaseInfo.getMaxWaitTime() / poolBaseInfo.getPerWaitTime());
        
        waitCount.incrementAndGet();
        try {
            do {
                if(tryCount  >= maxTryCount) {
                    throw new OverPoolWaitTimeException("wait time:" + poolBaseInfo.getMaxWaitTime());
                }
                synchronized(this) {
                    int poolSize = getPoolSize();
                    if(isShutDown) {
                        throw new RuntimeException("pool has shutDown,if you want to work agin,to startPoll");
                    }
                    if(poolSize < poolBaseInfo.getMaxSize()) {//如果没有达到最大队列
                        t = poolMemberAction.create();
                        member = new PoolMember<>();
                        member.setMember(t);
                        runingList.add(member);    
                    }else if(idleList.size() > 0) {
                        member =  idleList.get(0);
                        member.setUpdateTime(System.currentTimeMillis());
                        idleList.remove(0);
                        runingList.add(member);
                        t = member.getMember();
                    }
                    if(null != member) {
                        member.setUse(true);
                        runMap.put(t,member);
                    }
                }
                tryCount ++ ;
                try {
                    Thread.sleep(poolBaseInfo.getPerWaitTime());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }while(t == null);   
        }finally {
            waitCount.decrementAndGet();
        }
        return t;
    }

    /**
     * 判断当前成员是否超过最大限制时间
     * @param member
     * @return
     */
    protected synchronized boolean isOverMaxIdleTime(PoolMember<T> member) {
        if(!member.isUse() && System.currentTimeMillis() - member.getUpdateTime() > poolBaseInfo.getMaxIdleTime()) {
            return true;
        }
        return false;
    }

    /**
     * 判断当前成员是否超过最大运行/存活时间
     * @param member
     * @return
     */
    protected synchronized boolean isOverMaxRunTime(PoolMember<T> member) {
        if(member.isUse() && System.currentTimeMillis() - member.getUpdateTime() > poolBaseInfo.getMaxRunTime()) {
            return true;
        }
        return false;
    }

    /**
     * 刷新t的状态，表示t仍然需要持续运行
     * 刷新后将会更新其创建时间，被刷新后的对象将会延长其最大等待和运行时间
     * @param t 资源对象
     */
    public synchronized void keepRun(T t){
        if(null == t){
            System.out.println("PoolHelper:can not keepRun a null poolMember");
            return;
        }
        PoolMember<T> poolMember = runMap.get(t);
        if(null != poolMember) {
            poolMember.setUse(true);
            poolMember.setUpdateTime(System.currentTimeMillis());
            poolMember.setCreateTime(System.currentTimeMillis());
        }
    }

    /**
     * 关闭一个pool中的资源
     * @param t
     */
    public synchronized void close(T t) {
       if(null == t){
            System.out.println("PoolHelper:can not close a null poolMember");
            return;
       }
       PoolMember<T> poolMember = runMap.get(t);       
       if(null != poolMember) {
               poolMember.setUse(false);
               runMap.remove(t);
       }
    }
    
    /**
     * when all member is close,it will shutDown
     */
    public synchronized void shutDownPool() {
        isShutDown = true;
    }
    
    /** 当新建PoolHelper实例时也会默认调用此方法
     * when all member is close,it will shutDown
     */
    public synchronized void startPool() {
        isShutDown = false;
        workCheck();
    }
    
}
