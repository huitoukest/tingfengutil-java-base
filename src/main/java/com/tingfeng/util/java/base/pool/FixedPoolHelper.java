package com.tingfeng.util.java.base.pool;

import com.tingfeng.util.java.base.lang.exception.BaseException;
import com.tingfeng.util.java.base.lang.inter.returnfunction.FunctionROne;
import com.tingfeng.util.java.base.lang.inter.voidfunction.FunctionVOne;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 固定大小的简单的池对象，效率较高
 * 用于管理和复用资源，支持线程安全的资源分配
 * 实现了 AutoCloseable 接口，支持 try-with-resources 语法
 *
 * @param <T> 池中的资源类型
 * @author huitoukest
 */
public class FixedPoolHelper<T> implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(FixedPoolHelper.class);

    /**
     * 默认最大线程池大小
     */
    public static final int DEFAULT_MAX_THREAD_SIZE = 4;

    /**
     * 池大小
     */
    private final int poolSize;

    /**
     * 当前线程索引，用于循环分配资源
     */
    private int currentThread = 0;

    /**
     * 资源列表
     */
    private final List<T> dataList;

    /**
     * 用于创建新资源的回调函数
     */
    private final Callable<T> openAction;

    /**
     * 用于在使用时初始化资源的回调函数
     */
    private FunctionVOne<T> initDataAction;
    /**
     * 构造函数，使用默认池大小
     * @param openAction 用于创建新资源的回调函数
     */
    public FixedPoolHelper(Callable<T> openAction){
        this(DEFAULT_MAX_THREAD_SIZE,openAction);
    }

    /**
     * 构造函数，指定池大小
     * @param poolSize 池大小
     * @param openAction 用于创建新资源的回调函数
     */
    public FixedPoolHelper(int poolSize, Callable<T> openAction){
        this(poolSize,openAction,null);
    }

    /**
     * 构造函数，指定池大小和初始化回调
     * @param poolSize 池大小
     * @param openAction 用于创建新资源的回调函数
     * @param initDataAction 用于在使用时初始化资源的回调函数
     */
    public FixedPoolHelper(int poolSize, Callable<T> openAction, FunctionVOne<T> initDataAction){
        if(poolSize <= 0){
            this.poolSize = 1;
        }else {
            this.poolSize = poolSize;
        }
        dataList = new ArrayList<>();
        this.openAction = openAction;
        this.initDataAction = initDataAction;
        initDataList();
    }

    /**
     * 初始化资源列表
     */
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
     * 执行操作，使用池中的资源
     * 此方法是线程安全的
     * <p>实现说明：
     * 这里的currentThread不需要同步，只需要大概正确就行，这样反而效率更高
     * @param run 要执行的操作
     * @param <R> 返回值类型
     * @return 操作结果
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
                       initDataAction.accept(t);
                   }
                   return run.run(t);
               }
           }catch (Exception e){
               throw new BaseException(e);
           }
    }

    /**
     * 获取初始化资源的回调函数
     * @return 初始化资源的回调函数
     */
    public FunctionVOne<T> getInitDataAction() {
        return initDataAction;
    }

    /**
     * 设置初始化资源的回调函数
     * @param initDataAction 初始化资源的回调函数
     */
    public void setInitDataAction(FunctionVOne<T> initDataAction) {
        this.initDataAction = initDataAction;
    }

    /**
     * 关闭所有资源
     * 实现 AutoCloseable 接口，支持 try-with-resources 语法
     */
    @Override
    public void close() {
        if (dataList != null) {
            for (T resource : dataList) {
                if (resource instanceof AutoCloseable) {
                    try {
                        ((AutoCloseable) resource).close();
                    } catch (Exception e) {
                        // 记录异常，但不影响其他资源的关闭
                        logger.warn("Failed to close resource", e);
                    }
                }
            }
            dataList.clear();
        }
    }
}