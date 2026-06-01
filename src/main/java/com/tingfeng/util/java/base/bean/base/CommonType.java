package com.tingfeng.util.java.base.bean.base;

import java.util.*;

/**
 * 通用接口类型与默认实现类映射枚举
 *
 * 提供常见集合类型接口到其默认实现类的映射关系，用于反射创建实例场景。
 * 使用 isAssignableFrom 进行类型匹配，支持接口和抽象类的查找。
 */
public enum CommonType {
    /**
     * List 接口映射
     */
    LIST(List.class, ArrayList.class),
    /**
     * Set 接口映射
     */
    SET(Set.class, HashSet.class),
    /**
     * Map 接口映射
     */
    MAP(Map.class, HashMap.class),
    /**
     * Collection 接口映射
     */
    COLLECTION(Collection.class, ArrayList.class),
    /**
     * Iterable 接口映射
     */
    ITERABLE(Iterable.class, ArrayList.class),
    /**
     * Queue 接口映射
     */
    QUEUE(Queue.class, LinkedList.class),
    /**
     * Deque 接口映射
     */
    DEQUE(Deque.class, ArrayDeque.class),
    /**
     * SortedSet 接口映射
     */
    SORTED_SET(SortedSet.class, TreeSet.class),
    /**
     * NavigableSet 接口映射
     */
    NAVIGABLE_SET(NavigableSet.class, TreeSet.class),
    /**
     * SortedMap 接口映射
     */
    SORTED_MAP(SortedMap.class, TreeMap.class),
    /**
     * NavigableMap 接口映射
     */
    NAVIGABLE_MAP(NavigableMap.class, TreeMap.class),
    ;

    private final Class<?> interfaceType;
    private final Class<?> implementationClass;

    CommonType(Class<?> interfaceType, Class<?> implementationClass) {
        this.interfaceType = interfaceType;
        this.implementationClass = implementationClass;
    }

    /**
     * 获取对应的接口类型
     *
     * @return 接口类型 Class
     */
    public Class<?> getInterfaceType() {
        return interfaceType;
    }

    /**
     * 获取对应的默认实现类
     *
     * @return 实现类 Class
     */
    public Class<?> getImplementationClass() {
        return implementationClass;
    }

    /**
     * 根据类型查找对应的实现类
     *
     * 遍历枚举成员，使用 isAssignableFrom 匹配接口类型，返回第一个匹配的实现类
     *
     * @param type 待查找的类型（通常是接口或抽象类）
     * @return 匹配的实现类，未找到返回 null
     */
    public static Class<?> resolveImplementation(Class<?> type) {
        if (type == null) {
            return null;
        }
        for (CommonType commonType : values()) {
            if (commonType.interfaceType.isAssignableFrom(type)) {
                return commonType.implementationClass;
            }
        }
        return null;
    }

    /**
     * 根据类型查找对应的实现类，带默认值
     *
     * 当无法通过 CommonType 找到对应实现时，返回指定的默认值
     *
     * @param type        待查找的类型
     * @param defaultImpl 默认实现类
     * @return 匹配的实现类，未找到返回 defaultImpl
     */
    public static Class<?> resolveImplementationOrDefault(Class<?> type, Class<?> defaultImpl) {
        Class<?> result = resolveImplementation(type);
        return result != null ? result : defaultImpl;
    }
}