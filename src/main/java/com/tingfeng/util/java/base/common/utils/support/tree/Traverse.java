package com.tingfeng.util.java.base.common.utils.support.tree;

import com.tingfeng.util.java.base.common.bean.TreeTraverseContext;
import com.tingfeng.util.java.base.common.constant.DataNodeTraversePolicy;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Traverse{

    /**
     * 遍历 tree， 具体的顺序需要子类实现
     * @param treeList tree 结构的List
     * @param level 当前遍历的层级
     * @param parent 当前遍历的 treeList 的父节点,默认为 null
     * @param childrenGetter 获取当前节点的所有子节点
     * @param traverseF 每次遍历一个数据节点时调用此方法,其中第一次父节点的值为 为null; 返回结果 true = 继续遍历, false = 停止遍历
     * @param dataNodeTraversePolicy 接下来是否遍历数据节点（也就是当前中序遍历的父节点，仅当中序遍历生效）; 其它遍历策略不生效，传入 null 即可
     */
    <T> void traverse(List<T> treeList, int level, T parent, Function<T,List<T>> childrenGetter,
                      Predicate<TreeTraverseContext<T>> traverseF, DataNodeTraversePolicy dataNodeTraversePolicy);

    /**
     * 遍历 tree， 具体的顺序需要子类实现
     * @param treeList tree 结构的List
     * @param level 当前遍历的层级
     * @param parent 当前遍历的 treeList 的父节点,默认为 null
     * @param childrenGetter 获取当前节点的所有子节点
     * @param traverseF 每次遍历一个数据节点时调用此方法,其中第一次父节点的值为 为null; 返回结果 true = 继续遍历, false = 停止遍历
     */
    default <T> void traverse(List<T> treeList,int level,T parent, Function<T,List<T>> childrenGetter,
                      Predicate<TreeTraverseContext<T>> traverseF){
        traverse(treeList,level,parent,childrenGetter,traverseF,null);
    }
}
