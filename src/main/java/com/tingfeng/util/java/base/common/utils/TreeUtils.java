package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.bean.TreeNode;
import com.tingfeng.util.java.base.common.bean.TreeTraverseContext;
import com.tingfeng.util.java.base.common.constant.TraversalPolicy;
import com.tingfeng.util.java.base.common.exception.BaseException;
import com.tingfeng.util.java.base.common.inter.returnfunction.FunctionROne;
import com.tingfeng.util.java.base.common.inter.returnfunction.FunctionRTwo;
import com.tingfeng.util.java.base.common.inter.voidfunction.FunctionVOne;
import com.tingfeng.util.java.base.common.inter.voidfunction.FunctionVTwo;
import com.tingfeng.util.java.base.common.utils.support.tree.Traverse;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
     * 适用于节点数量较少的Tree构建， 性能， 1k 节点 -> 13ms ; 5k 节点 -> 374ms ; 1w 节点 -> 1654ms
     * 如对性能敏感,使用本工具类其他 getTreeList 方法。
     * @param list 数据来源list
     * @param addChildAction 加入一个child的Action,第一个参数是child，第二个是parent
     * @param isChildAction  判断第一个参数是否是第二个参数的子节点,第一个参数是child，第二个是parent，返回true/false
     * @param orderAction 排序操作,if null won't use it
     * @param <T> 当前排序的类型
     * @return
     */
    public static <T> List<T> getTreeList(List<T> list, FunctionVTwo<T,T> addChildAction, FunctionRTwo<Boolean,T,T> isChildAction, FunctionROne<Integer,T> orderAction) {
        if (list == null || list.isEmpty()) {
            return Collections.EMPTY_LIST;
        } else {
            List<T> rootList = getRootNodes(list, isChildAction);
            if(null != orderAction) {
                rootList = sortList(rootList, orderAction);
            }
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
        list = list.stream().sorted(Comparator.comparingInt(orderAction::run)).collect(Collectors.toList());
        return list;
    }

    /**
     * T = 目标类型，S = 来源类型； 建议执行convertAction后，手动处理(设置为null)节点的Children关联；
     * 注意： 此平铺展开不会影响原有bean 属性中的集合设置，即原来如果有子节点集合，此方法不会自动修改关联关系
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

    /**
     * 遍历 一个 tree结构的数据; 当前遍历层级默认为0,当前 treeList 的父节点默认为 null
     * @param treeList 当前 tree的根节点数据
     * @param traversalPolicy 遍历的策略 {@link TraversalPolicy}
     * @param childrenGetter 获取当前节点的children的方法
     * @param traverseF 用于遍历数据，输入节点信息，返回是否继续遍历
     * @param <T>
     */
    public static <T> void traverse(List<T> treeList, TraversalPolicy traversalPolicy, Function<T,List<T>> childrenGetter, Predicate<TreeTraverseContext<T>> traverseF){
        traverse(treeList,traversalPolicy,null,childrenGetter,traverseF);
    }

    /**
     * 遍历 一个 tree结构的数据； 当前遍历层级默认为0
     * @param treeList 当前 tree的根节点数据
     * @param traversalPolicy 遍历的策略 {@link TraversalPolicy}
     * @param parent 当前 treeList 的父节点,可以指定父节点
     * @param childrenGetter 获取当前节点的children的方法
     * @param traverseF 用于遍历数据，输入节点信息，返回是否继续遍历
     * @param <T>
     */
    public static <T> void traverse(List<T> treeList, TraversalPolicy traversalPolicy,T parent, Function<T,List<T>> childrenGetter,Predicate<TreeTraverseContext<T>> traverseF){
        traverse(treeList,0,traversalPolicy,parent,childrenGetter,traverseF);
    }

    /**
     * 遍历 一个 tree结构的数据
     * @param treeList 当前 tree的根节点数据
     * @param level 指定当前层级信息,默认值为 0
     * @param traversalPolicy 遍历的策略 {@link TraversalPolicy}
     * @param parent 当前 treeList 的父节点,可以指定父节点
     * @param childrenGetter 获取当前节点的children的方法
     * @param traverseF 用于遍历数据，输入节点信息，返回是否继续遍历
     * @param <T>
     */
    public static <T> void traverse(List<T> treeList,int level, TraversalPolicy traversalPolicy,T parent, Function<T,List<T>> childrenGetter,Predicate<TreeTraverseContext<T>> traverseF){
        Traverse traverse = traversalPolicy.getTraverse();
        traverse.traverse(treeList,level,parent,childrenGetter,traverseF);
    }

    /**
     * 将list中的数据转换为树形结构的数据;
     * 操作时将会直接引用List中的对象，<br/> 注意：所以会影响原对象,会给参数中输入的list 对象填充 children 对象的值
     * 本方法以头通过Map快速查找 树的父子关系，内存消耗高，速度快
     * 性能测试： 1w节点 -> 12ms ; 10w 节点 -> 82ms
     * @param list 数据来源list
     * @param comparator if null ,not use ;
     * @param <T> 当前排序的类型
     * @return 树形结构的数据
     */
    public static <T extends TreeNode> List<T> getTreeList(List<T> list, Comparator<T> comparator) {
        if(comparator != null){
            list = list.stream().sorted(comparator).collect(Collectors.toList());
        }
        Map<Object,T> nodeMap = list.stream()
                .collect(Collectors.toMap(T::getId,Function.identity()));
        List<T> rootNodes = new ArrayList<>();
        for (T t : list) {
            if(t.getParentId() == null || t.getParentId().equals(t.getId())){
                rootNodes.add(t);
            }else {
                T parent = nodeMap.get(t.getParentId());
                if (parent == null) {
                    rootNodes.add(t);
                }else {
                    if(null == parent.getChildren()){
                        throw new BaseException("can't add child to tree node, because TreeNode#getChildren() method return null");
                    }
                    parent.getChildren().add(t);
                }
            }
        }
        return rootNodes;
    }

}
