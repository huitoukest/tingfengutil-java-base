package com.tingfeng.util.java.base.common.inter;

/** 定义了PoolMember的一些基础行为
 * @author huitoukest
 * @param <T> T 就是pool实际打开或者释放的资源本身，比如常见的jdbc的数据库的连接对象
 */
public interface PoolMemberActionI<T> {
       /**
        * 创建一个成员 
        * @return 返回创建的资源
        */
       public T create();
       /**
        * 销毁一个成员
        * @param t  PoolMember的资源实例
        * @return 返回是否资源释放成功
        */
       public boolean destroy(T t);
       /**
        * 当超过最大运行时间时回调,系统会自动将此条移出pool池
        * @param t PoolMember的资源实例
        */
       public void onOverMaxRunTime(T t);
       /**
        * 当运行空闲检测时发生一次，比如调用destroy发生异常
        * @param t PoolMember的资源实例
        * @param e 当前发生的异常
        */
       public void onWorkException(T t,Throwable e);
}
