package com.tingfeng.util.java.base.common.utils;

import java.util.*;

/**
 * 排列组合工具
 * @author huitoukest
 */
public class CombinationUtils {
    /**
     * 将两个集合组合,对于包含的元素则去重
     * @param source
     * @param selectCount
     * @param <T> 返回的是集合的组合结果
     */
    public static  <T> List<Collection<T>> combination(List<T> source, int selectCount){
        if(selectCount <= 0 || selectCount > source.size()){
            return Collections.EMPTY_LIST;
        }
        List<Collection<T>> list = new ArrayList<>();
        combination(list,null,source,0,selectCount,0);
        return list;
    }

    /**
     * 将两个集合组合,对于包含的元素则去重
     * @param list result data
     * @param source
     * @param oneCollection
     * @param selectCount
     * @param <T> 返回的是集合的组合结果
     */
    public static  <T> void combination(List<Collection<T>> list, Set<T> oneCollection, List<T> source, int startIndex, int selectCount, int currentCount){
        if(oneCollection == null){
            oneCollection = new HashSet<>();
        }
        if(currentCount < selectCount){
            for (int i = startIndex; i < source.size(); i++) {
                Set<T> current = new HashSet<>();
                current.addAll(oneCollection);
                current.add(source.get(i));
                combination(list,current,source,i + 1,selectCount,currentCount + 1);
            }
        }else if(currentCount == selectCount && !oneCollection.isEmpty()){
            list.add(oneCollection);
        }
    }
}
