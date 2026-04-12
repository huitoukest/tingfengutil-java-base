package com.tingfeng.util.java.base.common.exception;

/**
 * 在Pool运行过程中，超过最大等待时间的时候报Exception
 * @author huitoukest
 *
 */
public class OverPoolWaitTimeException  extends BaseException{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public OverPoolWaitTimeException(String msg){
        super(msg);
    }

    public OverPoolWaitTimeException() {
    }
    public OverPoolWaitTimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public OverPoolWaitTimeException(Throwable cause) {
        super(cause);
    }

    public OverPoolWaitTimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
