package com.tingfeng.util.java.base.common.exception;

/**
 * 超过最大等待队列数值的时候报错
 * @author WangGang
 *
 */
public class OverPoolWaitSizeException  extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public OverPoolWaitSizeException(String msg){
        super(msg);
    }
}
