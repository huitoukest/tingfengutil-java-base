package com.tingfeng.util.java.base.common.helper;

import com.tingfeng.util.java.base.common.exception.BaseException;
import com.tingfeng.util.java.base.common.inter.returnfunction.FunctionROne;
import com.tingfeng.util.java.base.common.inter.voidfunction.FunctionVOne;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 固定大小的简单的池对象，效率较高
 */
public class FixedPoolHelper<T> {
    public static final int DEFAULT_MAX_THREAD_SIZE = 4;
    private int poolSize = 4;
    private int currentThread = 0;
    private List<T> dataList = null;
    private Callable<T> openAction = null;
    /**
     * 用于在使用时初始化一个资源
     */
    private FunctionVOne<T> initDataAction = null;
    /**
     * @param openAction 用于打开一个新的资源
     */
    public FixedPoolHelper(Callable<T> openAction){
        this(DEFAULT_MAX_THREAD_SIZE,openAction);
    }

    /**
     * @param poolSize
     * @param openAction
     */
    public FixedPoolHelper(int poolSize, Callable<T> openAction){
        this(poolSize,openAction,null);
    }

    /**
     *
     * @param poolSize
     * @param openAction
     * @param initDataAction 用于在使用时初始化一个资源
     */
    public FixedPoolHelper(int poolSize, Callable<T> openAction,FunctionVOne<T> initDataAction){
        if(poolSize < 0){
            this.poolSize = 1;
        }else {
            this.poolSize = poolSize;
        }
        dataList = new ArrayList<>();
        this.openAction = openAction;
        this.initDataAction = initDataAction;
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
     * 此方法是线程安全的,这里的currentThread不需要同步，
     * 只需要大概正确就行，这样反而效率更高
     * @param run
     * @param <R>
     * @return
     */
    public <R> R run(FunctionROne<R,T> run) {
           try{
               int flag = currentThread % poolSize;
               T t = dataList.get(flag);
               if(currentThread > poolSize){
                   currentThread = flag;
               }
               currentThread ++;
               synchronized (t) {
                   if(null != initDataAction){
                       initDataAction.run(t);
                   }
                   return run.run(t);
               }
           }catch (Exception e){
               throw new BaseException(e);
           }
    }

    public FunctionVOne<T> getInitDataAction() {
        return initDataAction;
    }

    public void setInitDataAction(FunctionVOne<T> initDataAction) {
        this.initDataAction = initDataAction;
    }
}
