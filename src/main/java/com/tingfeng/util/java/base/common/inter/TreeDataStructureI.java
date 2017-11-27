package com.tingfeng.util.java.base.common.inter;

import java.util.List;
/**
 * 默认没有子节点的节点即父节点;
 * T最好实现hashCode和equals方法;
 * @author huitoukest
 * 
 * @param <T>
 */
public interface TreeDataStructureI<T>{
	public T getParent(T t);
	public List<T> getChilrens(T t);
	public void setChilrens(T parent,List<T> chilrens);
	
	/**
	 * 判断child是否是parent的子节点
	 * @param child
	 * @param parent
	 * @return
	 */
	public boolean isChildOfNode(T child,T parent);
	
	/**
	 * 得到a里面排序的顺序的值,其中默认按照返回值从小到大排序;
	 * @param a
	 * @return
	 */
	public int getOrder(T a);
}
