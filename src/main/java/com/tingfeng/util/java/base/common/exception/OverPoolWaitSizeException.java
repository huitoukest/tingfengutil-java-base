package com.tingfeng.util.java.base.common.exception;

/**
 * 在Pool运行过程中，当超过最大等待队列数值的时候报错
 * @author huitoukest
 *
 */
public class OverPoolWaitSizeException  extends BaseException{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public OverPoolWaitSizeException(String msg){
        super(msg);
    }
}
