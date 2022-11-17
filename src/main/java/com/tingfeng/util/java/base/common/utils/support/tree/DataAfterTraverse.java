package com.tingfeng.util.java.base.common.utils.support.tree;

import com.tingfeng.util.java.base.common.bean.TreeTraverseContext;
import com.tingfeng.util.java.base.common.constant.DataNodeTraversePolicy;
import com.tingfeng.util.java.base.common.utils.ObjectUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 中序遍历
 * 部分子节点优先遍历,而后遍历父节点,再依次遍历其它子节点
 */
public class DataAfterTraverse extends AbstractTraverse{
    public DataAfterTraverse(boolean isReverse) {
        super(isReverse);
    }
    @Override
    public <T> void traverse(List<T> treeList, int level, T parent, Function<T, List<T>> childrenGetter,
                             Predicate<TreeTraverseContext<T>> traverseF, DataNodeTraversePolicy dataNodeTraversePolicy) {
        DataNodeTraversePolicy fixedDataNodeTraversePolicy = Optional.ofNullable(dataNodeTraversePolicy).orElse(DataNodeTraversePolicy.AFTER_ONE_CHILD_NODE);
        traverse(treeList,level,parent,null,childrenGetter,traverseF, fixedDataNodeTraversePolicy);
    }

    /**
     *
     * @param treeList 当前节点列表
     * @param level 当前层级
     * @param parent treeList 对应的 parent 节点
     * @param grandParent treeList 对应的 grandParent 节点
     * @param childrenGetter 输入T类型数，返回其children 数据的方法
     * @param traverseF 遍历数据，输入当前节点/父节点的数据 并返回是否继续遍历
     * @param dataNodeTraversePolicy 数据节点的遍历方式
     * @return 是否继续遍历 true = 继续, false = 停止
     * @param <T>
     */
    protected  <T> boolean traverse(List<T> treeList, int level, T parent,T grandParent, Function<T, List<T>> childrenGetter,
                             Predicate<TreeTraverseContext<T>> traverseF, DataNodeTraversePolicy dataNodeTraversePolicy) {
        if(ObjectUtils.isEmpty(treeList)){
            return true;
        }
        boolean isContinueTraverse = true;
        int size = treeList.size();
        if (isReverse()) {
            for (int i = size - 1; i >= 0 ; i--) {
                isContinueTraverse = isContinueTraverse && traverse(level,treeList,i,parent,grandParent,childrenGetter,traverseF, dataNodeTraversePolicy);
                if(!isContinueTraverse){
                    return false;
                }
            }
        }else {
            //遍历左侧
            for (int i = 0; i < size; i++) {
                isContinueTraverse = isContinueTraverse && traverse(level,treeList,i,parent,grandParent,childrenGetter,traverseF, dataNodeTraversePolicy);
                if(!isContinueTraverse){
                    return false;
                }
            }
        }
        return isContinueTraverse;
    }


    /**
     *
     * @param parentLevel 父节点的层级
     * @param brothers 当前节点的兄弟节点
     * @param indexInBrother  当前节点在兄弟节点中的索引值
     * @param parent treeList 对应的 parent 节点
     * @param grandParent treeList 对应的 grandParent 节点
     * @param childrenGetter 输入T类型数，返回其children 数据的方法
     * @param traverseF 遍历数据，输入当前节点/父节点的数据 并返回是否继续遍历
     * @param dataNodeTraversePolicy 数据节点的遍历方式
     * @return 是否继续遍历 true = 继续, false = 停止
     * @return
     * @param <T>
     */
    protected  <T> boolean traverse(int parentLevel,List<T> brothers,int indexInBrother,T parent,T grandParent, Function<T, List<T>> childrenGetter,
                                 Predicate<TreeTraverseContext<T>> traverseF, DataNodeTraversePolicy dataNodeTraversePolicy) {
        boolean isContinueTraverse = true;
        T node = brothers.get(indexInBrother);
        List<T> children = childrenGetter.apply(node);
        isContinueTraverse = isContinueTraverse && traverse(children,parentLevel + 1,node,parent,childrenGetter,traverseF, dataNodeTraversePolicy);
        if(!isContinueTraverse){
           return false;
        }
        TreeTraverseContext<T> currentContext = new TreeTraverseContext<>(isReverse(), node, parent,  + 1, indexInBrother, brothers);
        //因为在遍历子节点的方法中 会传入条件从而遍历(子节点的)父节点，也就是当前节点，
        // 所以只要当前节点有下级，则一定代表遍历过，所以这里判断无需再次遍历
        if(ObjectUtils.isEmpty(children)) {
            isContinueTraverse = isContinueTraverse && traverseF.test(currentContext);
        }
        if(!isContinueTraverse){
            return false;
        }
        if(parent != null && dataNodeTraversePolicy.getPredicate().test(currentContext)){
            TreeTraverseContext<T> parentContext = new TreeTraverseContext<>(isReverse(), parent,grandParent ,parentLevel, indexInBrother, brothers);
            isContinueTraverse = isContinueTraverse && traverseF.test(parentContext);
            if(!isContinueTraverse){
                return false;
            }
        }
        return true;
    }
}
