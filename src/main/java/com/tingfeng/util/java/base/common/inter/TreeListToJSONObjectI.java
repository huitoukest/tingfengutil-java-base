package com.tingfeng.util.java.base.common.inter;

import java.util.List;
/**
 * 普通的List bean对象转换为JSONObject形式的键值形式
 * @author huitoukest
 *
 */
public interface TreeListToJSONObjectI<T> {
	/**
	 * @param t bean对象
	 * @return JSONObject中的key
	 */
	public String getKey(T t);
	/**
	 * 得到子节点
	 * @param t bean对象
	 * @return
	 */
	public List<T> getChilren(T t);
	/**
	 * 设置子节点
	 * @param t bean对象
	 * @param children
	 */
	public void setChilren(T t,List<T> children);
	/**
	 * 得到子节点的名称 
	 * @param t bean对象
	 * @return
	 */
	public String getChilrenName(T t);
}
