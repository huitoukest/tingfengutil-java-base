package com.tingfeng.util.java.base.common.exception;

/**
 * 超过最大等待队列数值的时候报错
 * @author WangGang
 *
 */
public class OverPoolWaitTimeException  extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public OverPoolWaitTimeException(String msg){
        super(msg);
    }
}
