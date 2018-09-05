package com.tingfeng.util.java.base.common.helper;

import com.tingfeng.util.java.base.common.exception.BaseException;
import com.tingfeng.util.java.base.common.inter.returnfunction.FunctionrOne;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 固定大小的简单的池对象
 */
public class FixedPoolHelper<T> {
    public static final int DEFAULT_MAX_THREAD_SIZE = 4;
    private int poolSize = 4;
    private int currentThread = 0;
    private List<T> dataList = null;
    private Callable<T> openAction = null;

    /**
     * @param openAction 用于打开一个新的资源
     */
    public FixedPoolHelper(Callable<T> openAction){
        this(DEFAULT_MAX_THREAD_SIZE,openAction);
    }

    /**
     *
     * @param poolSize
     * @param openAction
     */
    public FixedPoolHelper(int poolSize, Callable<T> openAction){
        if(poolSize < 0){
            this.poolSize = 1;
        }else {
            this.poolSize = poolSize;
        }
        dataList = new ArrayList<>();
        this.openAction = openAction;
        initDataList();
    }

    private void initDataList(){
        try {
            for(int i = 0;i < poolSize ; i++){
                    dataList.add(openAction.call());
            }
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 此方法是线程安全的
     * @param run
     * @param <R>
     * @return
     */
    public <R> R run(FunctionrOne<R,T> run) {
           try{
               int flag = currentThread % poolSize;
               T t = dataList.get(flag);
               if(currentThread > poolSize){
                   currentThread = flag;
               }
               synchronized (t) {
                   currentThread ++;
                   return run.run(t);
               }
           }catch (Exception e){
               throw new BaseException(e);
           }
    }


}
