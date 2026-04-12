package com.tingfeng.util.java.base.common.utils;

import java.util.*;

/**
 * 排列组合工具
 * @author huitoukest
 */
public class CombinationUtils {
    /**
     * 将集合中的元素进行组合，返回所有可能的组合结果
     * @param source 源集合
     * @param selectCount 选择的元素个数
     * @param <T> 元素类型
     * @return 组合结果列表，每个元素是一个组合
     */
    public static  <T> List<Collection<T>> combination(List<T> source, int selectCount){
        if(source == null || source.isEmpty()){
            return Collections.emptyList();
        }
        if(selectCount <= 0 || selectCount > source.size()){
            return Collections.emptyList();
        }
        List<Collection<T>> list = new ArrayList<>();
        combination(list,null,source,0,selectCount,0);
        return list;
    }

    /**
     * 递归生成组合结果
     * @param list 结果列表
     * @param oneCollection 当前组合
     * @param source 源集合
     * @param startIndex 开始索引
     * @param selectCount 选择的元素个数
     * @param currentCount 当前已选择的元素个数
     * @param <T> 元素类型
     */
    public static  <T> void combination(List<Collection<T>> list, Set<T> oneCollection, List<T> source, int startIndex, int selectCount, int currentCount){
        if(oneCollection == null){
            oneCollection = new HashSet<>();
        }
        if(currentCount < selectCount){
            for (int i = startIndex; i < source.size(); i++) {
                Set<T> current = new HashSet<>(oneCollection);
                current.add(source.get(i));
                combination(list,current,source,i + 1,selectCount,currentCount + 1);
            }
        }else if(currentCount == selectCount && !oneCollection.isEmpty()){
            list.add(oneCollection);
        }
    }
}