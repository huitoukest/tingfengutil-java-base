package com.tingfeng.util.java.base.common.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.tingfeng.util.java.base.common.inter.TreeDataStructureI;

/**
 * 
 * @author huitoukest
 * 一个通用的对Tree结构(数据)操作的工具类
 */
public class TreeUtils {
	/**
	 * 将list中的数据转换为树形结构的数据;
	 * @param tds
	 * @param list
	 * @return 返回个树形结构的list;
	 * @throws NoSuchFieldException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 */
	public static <T> List<T> getTreeListBycommonList(TreeDataStructureI<T> tds,List<T> list,Class<T> cls) throws SecurityException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException{
		   List<T> parentList=new ArrayList<T>();
		   List<T>  tmpList=BeanUtils.middleCopyListByField(list,cls);
		   return getTreeListBycommonList(parentList, tds, tmpList);
		   
	} 
	/**
	 * 找出指定列表中的所有的根节点;
	 * 即没有父节点的节点都是根节点,如果父节点是自己,那么自己是根节点;
	 * @param tds
	 * @param list
	 * @return
	 */
	public static <T> List<T> getRootParentsOfTree(TreeDataStructureI<T> tds,List<T> list){
		List<T> parentList=new ArrayList<T>();
		if(list==null||list.isEmpty())
			return parentList;
		for(int i=0;i<list.size();i++){
			T parentT=list.get(i);
			boolean isParent=true;
			for(int j=0;j<list.size();j++){
				if(j==i)
					continue;
				T nodeT=list.get(j);
				//如果parentT节点有父节点,那么它不是父节点;
				if(tds.isChildOfNode(parentT,nodeT)){
					isParent=false;
					break;
				}
			}
			if(isParent){
				parentList.add(parentT);
			}
		}
		return parentList;
	}
	protected static <T> List<T> getTreeListBycommonList(List<T> parentList,TreeDataStructureI<T> tds,List<T> list){
		  	  if(list==null||list.isEmpty()){
		  		  return parentList;
		  	  }else{
		  		    List<T> rootList=getRootParentsOfTree(tds, list);
		  		  	if(parentList.isEmpty()){
		  		    	parentList.addAll(rootList);		  		    			  		    	
		  		    }else{
		  		    	 for(T t:rootList){
		  		    		   addNodeToTreeList(parentList, tds, t);
		  		    	 }
		  		    }
		  		  	list.removeAll(rootList);
		  		  	return getTreeListBycommonList(parentList, tds, list);
		  	  }
	} 
	/**
	 * 将一个叶子节点加入到parentList中;
	 * 并且在加入节点的同时进行排序;
	 * @param parentList
	 * @param tds
	 * @param leafNode
	 */
	protected static <T> boolean addNodeToTreeList(List<T> parentList,TreeDataStructureI<T> tds,T leafNode){
	  	if(leafNode==null||parentList==null||parentList.isEmpty())
	  	return false;
	  	for(T parent:parentList){
	  		if(tds.isChildOfNode(leafNode, parent)){
	  			boolean isAdd=false;
	  			List<T> childrendsList=tds.getChilrens(parent);
	  			if(childrendsList==null)
	  			{
	  				childrendsList=new ArrayList<T>();
	  				tds.setChilrens(parent, childrendsList);
	  			}
  				for(int i=0;i<childrendsList.size();i++){
	  					T brother=childrendsList.get(i);
	  					if(tds.getOrder(brother)>tds.getOrder(leafNode))
	  					{
	  						tds.getChilrens(parent).add(i, leafNode);
	  						isAdd=true;
	  						break;
	  					}
	  				}	  		
	  				if(!isAdd)
	  				tds.getChilrens(parent).add(leafNode);
	  			return true;
	  		}else{
	  			boolean result=addNodeToTreeList(tds.getChilrens(parent), tds, leafNode);
	  			if(result){
	  				return result;
	  			}
	  		}
	  	}
	  	return false;
	}	
}
