package com.tingfeng.util.java.base.common.inter;

public interface PoolMemberActionI<T> {
       /**
        * 创建一个成员 
        * @return
        */
       public T create();
       /**
        * 销毁一个成员
        * @return
        */
       public boolean destroy(T t);
       /**
        * 当超过最大运行时间时回调,系统会自动将此条移出pool池
        * @param t
        */
       public void onOverMaxRunTime(T t);
       /**
        * 当运行空闲检测时发生一次，比如调用destroy发生异常
        * @param e
        */
       public void onWorkException(Exception e); 
}
