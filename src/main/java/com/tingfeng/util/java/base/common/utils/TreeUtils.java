package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.bean.DefaultTreeNode;
import com.tingfeng.util.java.base.common.bean.GenericTreeNode;
import com.tingfeng.util.java.base.common.bean.TreeNode;
import com.tingfeng.util.java.base.common.exception.BaseException;
import com.tingfeng.util.java.base.common.inter.consumer.ConsumerTwo;
import com.tingfeng.util.java.base.common.inter.returnfunction.Function2;
import com.tingfeng.util.java.base.common.inter.returnfunction.FunctionROne;
import com.tingfeng.util.java.base.common.inter.returnfunction.FunctionRTwo;
import com.tingfeng.util.java.base.common.inter.voidfunction.FunctionVOne;
import com.tingfeng.util.java.base.common.inter.voidfunction.FunctionVTwo;

import java.util.*;
import java.util.function.Function;
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
     * 前序遍历(Preorder Traversal 亦称先序遍历)——访问根结点—>根的左子树—>根的右子树 (Data -> Left -> Right)。
     * @param treeList tree 结构的List
     * @param childrenGetter 获取当前节点的所有子节点
     * @param nodeAndParentConsumer [当前节点，父节点]
     * @param <T>
     */
    /*public static <T> void traverseByDLR(List<T> treeList, Function<T,List<T>> childrenGetter, ConsumerTwo<T,T> nodeAndParentConsumer){
        traverseByDLR(treeList,null,childrenGetter,nodeAndParentConsumer);
    }*/
    /**
     * 前序遍历(Preorder Traversal 亦称先序遍历)——访问根结点—>根的左子树—>根的右子树 (Data -> Left -> Right)。
     * @param treeList tree 结构的List
     * @param parent 当前treeList 的父节点
     * @param childrenGetter 获取当前节点的所有子节点
     * @param nodeAndParentConsumer [当前节点，父节点]
     * @param <T>
     */
    /*public static <T> void traverseByDLR(List<T> treeList,T parent, Function<T,List<T>> childrenGetter, ConsumerTwo<T,T> nodeAndParentConsumer){
        for (T t : treeList) {
            nodeAndParentConsumer.accept(t,parent);
            Optional.ofNullable(childrenGetter.apply(t))
                    .filter(ObjectUtils::isNotEmpty)
                    .ifPresent(children -> {
                        traverseByDLR(children,t,childrenGetter,nodeAndParentConsumer);
                    });
        }
    }*/

    /**
     * 前序遍历 (Preorder Traversal 亦称先序遍历)——访问根结点—>根的右子树—>根的左子树 (Data -> Right -> Left)。
     * @param treeList tree 结构的List
     * @param childrenGetter 获取当前节点的所有子节点
     * @param nodeAndParentConsumer [当前节点，父节点]
     * @param <T>
     */
    /*public static <T> void traverseByDRL(List<T> treeList, Function<T,List<T>> childrenGetter, ConsumerTwo<T,T> nodeAndParentConsumer){
        traverseByDRL(treeList,null,childrenGetter,nodeAndParentConsumer);
    }*/

    /**
     * 前序遍历 (Preorder Traversal 亦称先序遍历)——访问根结点>根的右子树—>根的左子树— (Data -> Right -> Left)。
     * @param treeList tree 结构的List
     * @param parent 当前treeList 的父节点
     * @param childrenGetter 获取当前节点的所有子节点
     * @param nodeAndParentConsumer [当前节点，父节点]
     * @param <T>
     */
    /*public static <T> void traverseByDRL(List<T> treeList,T parent, Function<T,List<T>> childrenGetter, ConsumerTwo<T,T> nodeAndParentConsumer){
        for (int i = treeList.size() - 1; i >= 0 ; i--) {
            T t = treeList.get(i);
            nodeAndParentConsumer.accept(t,parent);
            Optional.ofNullable(childrenGetter.apply(t))
                    .filter(ObjectUtils::isNotEmpty)
                    .ifPresent(children -> {
                        traverseByDRL(children,t,childrenGetter,nodeAndParentConsumer);
                    });

        }
    }*/

    /**
     * LDR：中序遍历(Inorder Traversal)——根的左子树—>根节点—>根的右子树 (Left  -> Data -> Right)。
     * @param treeList tree 结构的List
     * @param parent 当前treeList 的父节点
     * @param childrenGetter 获取当前节点的所有子节点
     * @param canVisitParentPredicate [当前父节点的所有子节点，当前所有子节点 遍历的次数] 返回是否可以遍历父节点(在遍历子节点前触发); 遍历完所有子节点后会再次尝试遍历一次父节点 ; 如果一直返回false，则只会遍历 整棵树的叶子结点
     * @param nodeAndParentConsumer [当前节点，父节点]
     * @param <T>
     */
    /*public static <T> void traverseByLDR(List<T> treeList, T parent, Function<T,List<T>> childrenGetter, Function2<Boolean,List<T>,Integer> canVisitParentPredicate, ConsumerTwo<T,T> nodeAndParentConsumer){
        for (int i = 0; i < treeList.size(); i++) {
            T t = treeList.get(i);
            Optional.ofNullable(childrenGetter.apply(t))
                    .filter(ObjectUtils::isNotEmpty)
                    .ifPresent(children -> {
                        int size = children.size();
                        for (int j = 0; j <= size; j++) {
                            if(canVisitParentPredicate.run(children,j)){
                                nodeAndParentConsumer.accept(t,parent);
                            }
                            if(j < size) {
                                T child = children.get(j);
                                traverseByLDR(treeList,child,childrenGetter,canVisitParentPredicate,nodeAndParentConsumer);
                                nodeAndParentConsumer.accept(child, t);
                            }
                        }
                    });

        }
    }*/

    /**
     * LDR：中序遍历(Inorder Traversal)——根的左子树—>根节点—>根的右子树 (Left  -> Data -> Right)。
     * 默认遍历顺序()
     * @param treeList tree 结构的List
     * @param parent 当前treeList 的父节点
     * @param childrenGetter 获取当前节点的所有子节点
     * @param nodeAndParentConsumer [当前节点，父节点]
     * @param <T>
     */
    /*public static <T> void traverseByLDR(List<T> treeList, T parent, Function<T,List<T>> childrenGetter, ConsumerTwo<T,T> nodeAndParentConsumer){
        traverseByLDR(treeList,parent,childrenGetter,(children,count) -> count == 1,nodeAndParentConsumer);
    }*/

    /**
     * RDL：中序遍历(Inorder Traversal)——根的左子树—>根节点—>根的右子树 (Right  -> Data -> Left)。
     * @param treeList tree 结构的List
     * @param parent 当前treeList 的父节点
     * @param childrenGetter 获取当前节点的所有子节点
     * @param canVisitParentPredicate [当前父节点的所有子节点，当前所有子节点 遍历的次数] 返回是否可以遍历父节点(在遍历子节点前触发); 遍历完所有子节点后会再次尝试遍历一次父节点 ; 如果一直返回false，则只会遍历 整棵树的叶子结点
     * @param nodeAndParentConsumer [当前节点，父节点]
     * @param <T>
     */
    /*public static <T> void traverseByRDL(List<T> treeList, T parent, Function<T,List<T>> childrenGetter, Function2<Boolean,List<T>,Integer> canVisitParentPredicate, ConsumerTwo<T,T> nodeAndParentConsumer){
        for (int i = treeList.size() - 1; i >= 0 ; i--) {
            T t = treeList.get(i);
            Optional.ofNullable(childrenGetter.apply(t))
                    .filter(ObjectUtils::isNotEmpty)
                    .ifPresent(children -> {
                        int size = children.size();
                        for (int j = 0; j <= size; j++) {
                            if(canVisitParentPredicate.run(children,j)){
                                nodeAndParentConsumer.accept(t,parent);
                            }
                            if(j < size) {
                                T child = children.get(size - 1 - j);
                                traverseByLDR(treeList,child,childrenGetter,canVisitParentPredicate,nodeAndParentConsumer);
                                nodeAndParentConsumer.accept(child, t);
                            }
                        }
                    });

        }
    }*/

    /**
     * RDL：中序遍历(Inorder Traversal)——根的左子树—>根节点—>根的右子树 (Right  -> Data -> Left)。
     * @param treeList tree 结构的List
     * @param parent 当前treeList 的父节点
     * @param childrenGetter 获取当前节点的所有子节点
     * @param nodeAndParentConsumer [当前节点，父节点]
     * @param <T>
     */
    /*public static <T> void traverseByRDL(List<T> treeList, T parent, Function<T,List<T>> childrenGetter, ConsumerTwo<T,T> nodeAndParentConsumer){
        traverseByRDL(treeList,parent,childrenGetter,(children,count) -> count == 1,nodeAndParentConsumer);
    }*/


    /**
     * LRD：后序遍历(Inorder Traversal)——根的左子树—>根的右子树—>根节点 (Left -> Right  -> Data)。
     * @param treeList tree 结构的List
     * @param parent 当前treeList 的父节点
     * @param childrenGetter 获取当前节点的所有子节点
     * @param nodeAndParentConsumer [当前节点，父节点]
     * @param <T>
     */
    /*public static <T> void traverseByLRD(List<T> treeList, T parent, Function<T,List<T>> childrenGetter, ConsumerTwo<T,T> nodeAndParentConsumer){
        traverseByLDR(treeList,parent,childrenGetter,(children,count) -> count == children.size(),nodeAndParentConsumer);
    }*/

    /**
     * RLD：后序遍历(Inorder Traversal)—— 根的右子树—>根的左子树—>根节点 (Left -> Right  -> Data)。
     * @param treeList tree 结构的List
     * @param parent 当前treeList 的父节点
     * @param childrenGetter 获取当前节点的所有子节点
     * @param nodeAndParentConsumer [当前节点，父节点]
     * @param <T>
     */
    /*public static <T> void traverseByRLD(List<T> treeList, T parent, Function<T,List<T>> childrenGetter, ConsumerTwo<T,T> nodeAndParentConsumer){
        traverseByRDL(treeList,parent,childrenGetter,(children,count) -> count == children.size(),nodeAndParentConsumer);
    }*/

    /**
     * 将list中的数据转换为树形结构的数据;
     * 操作时将会直接引用List中的对象，所以会影响原对象
     * 本方法以头通过Map快速查找 树的父子关系，内存消耗高，速度快
     * 性能测试： 1w节点 -> 12ms ; 10w 节点 -> 82ms
     * @param list 数据来源list
     * @param comparator if null ,not use ;
     * @param <T> 当前排序的类型
     * @return
     */
    public static <T extends TreeNode> List<T> getTreeList(List<T> list, Comparator<T> comparator) {
        if(comparator != null){
            list = list.stream().sorted(comparator).collect(Collectors.toList());
        }
        Map<Object,T> nodeMap = new HashMap<>((int)(list.size() * 0.7));
        List<T> rootNodes = new ArrayList<>();
        for (T t : list) {
            nodeMap.put(t.getId(),t);
        }
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
