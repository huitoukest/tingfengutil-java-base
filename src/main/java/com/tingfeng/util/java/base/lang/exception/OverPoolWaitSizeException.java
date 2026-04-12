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

    public OverPoolWaitSizeException() {
    }

    public OverPoolWaitSizeException(String message, Throwable cause) {
        super(message, cause);
    }

    public OverPoolWaitSizeException(Throwable cause) {
        super(cause);
    }

    public OverPoolWaitSizeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public OverPoolWaitSizeException(String msg){
        super(msg);
    }
}
