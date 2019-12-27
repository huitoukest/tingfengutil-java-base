package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.exception.BaseException;
import com.tingfeng.util.java.base.common.inter.returnfunction.FunctionROne;
import com.tingfeng.util.java.base.common.inter.returnfunction.FunctionRTwo;
import com.tingfeng.util.java.base.common.inter.voidfunction.FunctionVOne;
import com.tingfeng.util.java.base.common.inter.voidfunction.FunctionVTwo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author huitoukest
 * 一个通用的对Tree结构(数据)操作的工具类
 *
 */
public class TreeUtils {

    /**
     * 找出指定列表中的所有的根节点;
     * 即1. 没有父节点的节点都是根节点,2. 如果父节点是自己,那么自己是根节点;
     * @param list 数据List
     * @param isChildAction 判断第一个参数是否是第二个参数的子节点
     * @return
     */
    public static <T> List<T> getRootNodes( List<T> list,FunctionRTwo<Boolean,T,T> isChildAction) {
        List<T> parentList = new ArrayList<T>();
        if (list == null || list.isEmpty()) {
            return parentList;
        }
        try {
            for (int i = 0; i < list.size(); i++) {
                T parentT = list.get(i);
                boolean isParent = true;
                for (int j = 0; j < list.size(); j++) {
                    if (j == i) {
                        continue;
                    }
                    T nodeT = list.get(j);
                    //如果parentT节点有父节点,那么它不是父节点;
                    if (isChildAction.run(parentT, nodeT)) {
                        isParent = false;
                        break;
                    }
                }
                if (isParent) {
                    parentList.add(parentT);
                }
            }
        }catch (Exception e){
            throw new BaseException(e);
        }
        return parentList;
    }

    /**
     * 将list中的数据转换为树形结构的数据;
     * 操作时将会直接引用List中的对象，所以会影响原对象
     * @param list 数据来源list
     * @param addChildAction 加入一个child的Action,第一个参数是child，第二个是parent
     * @param isChildAction  判断第一个参数是否是第二个参数的子节点,第一个参数是child，第二个是parent，返回true/false
     * @param orderAction 排序操作
     * @param <T> 当前操作的类型
     * @return
     */
    public static <T> List<T> getTreeList(List<T> list, FunctionVTwo<T,T> addChildAction, FunctionRTwo<Boolean,T,T> isChildAction, FunctionROne<Integer,T> orderAction) {
        if (list == null || list.isEmpty()) {
            return Collections.EMPTY_LIST;
        } else {
            List<T> rootList = getRootNodes(list, isChildAction);
            rootList = sortList(rootList,orderAction);
            List<T> remainList = new ArrayList<>();
            remainList.addAll(list);
            remainList.removeAll(rootList);
            if(!remainList.isEmpty()) {
                List<T> childList = getTreeList(remainList, addChildAction, isChildAction, orderAction);
                for (T t : childList) {
                    addNodeToTreeList(rootList, t, addChildAction, isChildAction);
                }
            }
            return rootList;
        }
    }

    /**
     * 将一个叶子节点加入到parentList中;
     * 并且在加入节点的同时进行排序;
     * @param parentList 父列表
     * @param childNode 子node
     * @param addChildAction 加入一个child的Action,第一个参数是child，第二个是parent
     * @param isChildAction 判断第一个参数是否是第二个参数的子节点,第一个参数是child，第二个是parent，返回true/false
     * @param <T> 当前操作的类型
     * @return
     */
    protected static <T> boolean addNodeToTreeList(List<T> parentList, T childNode, FunctionVTwo<T,T> addChildAction, FunctionRTwo<Boolean,T,T> isChildAction) {
        if (childNode == null || parentList == null || parentList.isEmpty()) {
            return false;
        }
        try {
            for (T parent : parentList) {
                if (isChildAction.run(childNode, parent)) {
                    addChildAction.run(childNode,parent);
                    return true;
                }
            }
        }catch (Exception e){
            throw new BaseException(e);
        }
        return false;
    }

    /**
     * 非递归排序 默认升序
     * @param list
     * @param orderAction
     * @param <T>
     * @return
     */
    public static <T> List<T> sortList(List<T> list,FunctionROne<Integer,T> orderAction){
        Collections.sort(list,(a,b)->{
            try {
                return orderAction.run(a) - orderAction.run(b);
            }catch (Exception e){
                throw new BaseException(e);
            }
        });
        return list;
    }

    /**
     * T = 目标类型，S = 来源类型； 建议执行convertAction后，手动处理(设置为null)节点的Children关联；
     * @param treeList 原始tree结构的list
     * @param flatAction 获取子节点的action
     * @param convertAction 转换类型
     * @param <T>
     * @param <S>
     * @return
     */
    public static <T,S> List<T> flatList(List<S> treeList,FunctionROne<List<S>,S> flatAction,FunctionROne<T,S> convertAction){
        List<T> reList = new ArrayList<>();
        FunctionVOne<List<S>>[] adder = new FunctionVOne[1];
        adder[0] = (tmpList) -> {
           if(ObjectUtils.isNotEmpty(tmpList)){
               tmpList.forEach(it -> {
                   reList.add(convertAction.run(it));
                   adder[0].accept(flatAction.run(it));
               });
           }
        };
        adder[0].accept(treeList);
        return reList;
    }
}
