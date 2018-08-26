package com.tingfeng.util.java.base.common.exception;

/**
 * 需要通过异常方式将结果抛出给其他的对象的时候
 * 然后代理对象捕获此异常,并返回值
 * @author huitoukest
 *
 */
public class ReturnException extends BaseException{
	private static final long serialVersionUID = 1L;
	/**
	 * 用来当前线程的返回值
	 */
	final public static ThreadLocal<Object> returnValue=new ThreadLocal<Object>();
	/**
	 * 用来记录当前线程是否发生了returnException
	 */
	final public static ThreadLocal<Boolean> isReturnException=new ThreadLocal<Boolean>();

	/**
	 * 用来记录此返回的对象
	 * @param returnValue 返回的对象
	 */
	public ReturnException(Object returnValue){
		this(returnValue,null);
	}

	/**
	 *  用来记录此返回的对象
	 * @param returnValue 返回的对象
	 * @param msg 返回的消息
	 */
	public ReturnException(Object returnValue,String msg){
		super(msg);
		ReturnException.returnValue.set(returnValue);
		ReturnException.isReturnException.set(true);
	}

}
