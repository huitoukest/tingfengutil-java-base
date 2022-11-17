package com.tingfeng.util.java.base.common.utils.support.tree;

import com.tingfeng.util.java.base.common.bean.TreeTraverseContext;
import com.tingfeng.util.java.base.common.constant.DataNodeTraversePolicy;
import com.tingfeng.util.java.base.common.utils.ObjectUtils;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class DataFirstTraverse extends AbstractTraverse{
    public DataFirstTraverse(boolean isReverse) {
        super(isReverse);
    }

    @Override
    public <T> void traverse(List<T> treeList, int level, T parent, Function<T, List<T>> childrenGetter, Predicate<TreeTraverseContext<T>> traverseF, DataNodeTraversePolicy dataNodeTraversePolicy) {
        traverse(treeList,level,parent,childrenGetter,traverseF,null);
    }

    /**
     * 遍历 tree， 具体的顺序需要子类实现
     * @param treeList tree 结构的List
     * @param level 当前遍历的层级
     * @param parent 当前遍历的 treeList 的父节点,默认为 null
     * @param childrenGetter 获取当前节点的所有子节点
     * @param traverseF 每次遍历一个数据节点时调用此方法,其中第一次父节点的值为 为null; 返回结果 true = 继续遍历, false = 停止遍历
     */
    public <T> void traverse(List<T> treeList, int level, T parent, Function<T,List<T>> childrenGetter,
                      Predicate<TreeTraverseContext<T>> traverseF){
        int size = treeList.size();
        boolean isContinueTraverse = true;
        if (isReverse()) {
            for (int i = size - 1; i >= 0 ; i--) {
                isContinueTraverse = isContinueTraverse && dataFirstTraverse(treeList,level,parent,childrenGetter,traverseF);
                if(!isContinueTraverse){
                    break;
                }
            }
        }else {
            for (int i = 0; i < size; i++) {
                isContinueTraverse = isContinueTraverse && dataFirstTraverse(treeList,level,parent,childrenGetter,traverseF);
                if(!isContinueTraverse){
                    break;
                }
            }
        }
    }

    /**
     *
     * @param treeList tree 结构的List
     * @param level 当前遍历的层级
     * @param parent 当前遍历的 treeList 的父节点,默认为 null
     * @param childrenGetter 获取当前节点的所有子节点
     * @param traverseF 每次遍历一个数据节点时调用此方法,其中第一次父节点的值为 为null; 返回结果 true = 继续遍历, false = 停止遍历
     * @return 是否继续遍历 true = 继续, false = 停止
     * @param <T>
     */
    protected  <T> boolean dataFirstTraverse(List<T> treeList, int level, T parent, Function<T, List<T>> childrenGetter,
                                    Predicate<TreeTraverseContext<T>> traverseF) {
        if(ObjectUtils.isEmpty(treeList)){
            return true;
        }
        boolean isContinueTraverse = true;
        int size = treeList.size();
        if (isReverse()) {
            for (int i = size - 1; i >= 0 ; i--) {
                isContinueTraverse = isContinueTraverse && traverse(level,treeList,i,parent,childrenGetter,traverseF);
                if(!isContinueTraverse){
                    return false;
                }
            }
        }else {
            //遍历左侧
            for (int i = 0; i < size; i++) {
                isContinueTraverse = isContinueTraverse && traverse(level,treeList,i,parent,childrenGetter,traverseF);
                if(!isContinueTraverse){
                    return false;
                }
            }
        }
        return isContinueTraverse;
    }


    /**
     * @param level 当前遍历的层级
     * @param parent 当前遍历的 treeList 的父节点,默认为 null
     * @param childrenGetter 获取当前节点的所有子节点
     * @param traverseF 每次遍历一个数据节点时调用此方法,其中第一次父节点的值为 为null; 返回结果 true = 继续遍历, false = 停止遍历
     * @param brothers 当前parent的兄弟节点
     * @param indexInBrother 当前parent在兄弟节点中的索引
     * @return
     * @param <T>
     */
    protected  <T> boolean traverse(int level,List<T> brothers,int indexInBrother,T parent, Function<T, List<T>> childrenGetter,
                                    Predicate<TreeTraverseContext<T>> traverseF) {
        boolean isContinueTraverse = true;
        T node = brothers.get(indexInBrother);
        List<T> children = childrenGetter.apply(node);
        TreeTraverseContext<T> currentContext = new TreeTraverseContext<>(isReverse(), node, parent,  + 1, indexInBrother, children);
        isContinueTraverse = isContinueTraverse && traverseF.test(currentContext);
        if(!isContinueTraverse){
            return false;
        }
        isContinueTraverse = isContinueTraverse && dataFirstTraverse(children,level + 1,node,childrenGetter,traverseF);
        if(!isContinueTraverse){
            return false;
        }

        return true;
    }
}
