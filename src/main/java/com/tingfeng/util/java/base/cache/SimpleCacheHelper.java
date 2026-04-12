package com.tingfeng.util.java.base.common.helper;

import com.tingfeng.util.java.base.common.bean.SimpleCacheMember;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 基于访问频率的简单缓存实现
 * <p>
 * 特性：
 * <ul>
 *   <li>读多写少场景下使用 ReadWriteLock，读读并发，写写/读写互斥</li>
 *   <li>使用 TreeMap + LinkedList 维护权重索引，快速获取 min/max 权重</li>
 *   <li>权重增量（actualWeight = globalBase + entry.weight），通过 globalOffset 防止溢出</li>
 * </ul>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author huitoukest
 */
public class SimpleCacheHelper<K, V> {
    /**
     * 权重接近溢出阈值时，触发全局偏移
     * actualWeight = globalBase + entry.weight，当 entry.weight > 此值时需要偏移
     */
    private static final long WEIGHT_OVERFLOW_THRESHOLD = Integer.MAX_VALUE >> 1;

    private final int maxSize;
    private int currentSize = 0;

    /**
     * 主缓存 Map：key → 缓存成员
     */
    private final Map<K, SimpleCacheMember<V>> map;

    /**
     * 权重索引：actualWeight → 该权重下的所有成员列表
     * actualWeight = globalBase + entry.weight
     * TreeMap 保证 firstKey() = minWeight, lastKey() = maxWeight
     */
    private final TreeMap<Integer, LinkedList<SimpleCacheMember<V>>> weightIndex;

    /**
     * 全局基准偏移量
     * 每次 globalOffset 时增加，所有成员的 actualWeight 减去 minWeight
     * 这样可以防止 entry.weight 无限递增溢出
     */
    private long globalBase = 0;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public SimpleCacheHelper(int maxSize) {
        this.maxSize = maxSize;
        this.map = new HashMap<>();
        this.weightIndex = new TreeMap<>();
    }

    /**
     * 从缓存中取值，命中时权重 +1
     * <p>
     * 读多写少场景优化：
     * 1. 读锁下快速获取数据（读读并发）
     * 2. 释放读锁后，判断是否需要更新权重（容量未满时跳过写锁）
     * 3. 需要时获取写锁进行权重更新（re-check 保证一致性）
     *
     * @param key 键
     * @return 值，不存在则返回 null
     */
    public V get(K key) {
        // Phase 1: 读锁下获取数据
        readWriteLock.readLock().lock();
        SimpleCacheMember<V> member;
        V value;
        try {
            member = map.get(key);
            if (member == null) {
                return null;
            }
            value = member.getValue();
        } finally {
            readWriteLock.readLock().unlock();
        }

        // Phase 2: 判断是否需要获取写锁更新权重
        // 容量未满时跳过写锁（权重更新延迟或丢失对缓存淘汰策略影响可控）
        // 容量已满时必须写锁保证 weightIndex 一致性
        if (currentSize < maxSize) {
            return value;
        }

        // 容量已满：获取写锁更新权重
        readWriteLock.writeLock().lock();
        try {
            // re-check：entry 可能已被其他线程驱逐
            member = map.get(key);
            if (member == null) {
                return value;  // 已驱逐仍返回原值（权重更新丢失）
            }
            doUpdateWeight(member);
            return member.getValue();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * 内部权重更新操作（需要持有写锁）
     */
    private void doUpdateWeight(SimpleCacheMember<V> member) {
        int oldWeight = getActualWeight(member);
        removeFromWeightIndex(member, oldWeight);
        member.setWeight(member.getWeight() + 1);
        addToWeightIndex(member, getActualWeight(member));
    }

    /**
     * 设置缓存值
     *
     * @param key   键
     * @param value 值
     */
    public void set(K key, V value) {
        readWriteLock.writeLock().lock();
        try {
            SimpleCacheMember<V> member = map.get(key);
            if (member != null) {
                // 已存在：更新值，权重 +1
                doUpdateWeight(member);
                member.setValue(value);
            } else {
                // 不存在：检查溢出 + 驱逐
                int currentMaxWeight = getCurrentMaxWeight();
                if (currentMaxWeight > WEIGHT_OVERFLOW_THRESHOLD) {
                    globalOffset();
                }

                // 新成员初始权重 = 当前最小权重 + 1，避免刚加入就被驱逐
                int initialWeight = getCurrentMinWeight() + 1;
                member = new SimpleCacheMember<>(initialWeight, value);
                map.put(key, member);
                addToWeightIndex(member, initialWeight);
                currentSize++;

                // 容量超限时驱逐
                if (currentSize > maxSize) {
                    evict();
                }
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * 是否包含键
     */
    public boolean containsKey(K key) {
        readWriteLock.readLock().lock();
        try {
            return map.containsKey(key);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * 返回当前缓存的所有键集合（复制一份，避免外部修改）
     */
    public Set<K> keySet() {
        readWriteLock.readLock().lock();
        try {
            return new HashSet<>(map.keySet());
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public int size() {
        readWriteLock.readLock().lock();
        try {
            return currentSize;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public int getMaxSize() {
        return maxSize;
    }

    // ==================== 内部方法 ====================

    /**
     * 获取成员的实际权重（globalBase + entry.weight）
     */
    private int getActualWeight(SimpleCacheMember<V> member) {
        return (int) (globalBase + member.getWeight());
    }

    /**
     * 获取当前最小实际权重
     */
    private int getCurrentMinWeight() {
        Map.Entry<Integer, LinkedList<SimpleCacheMember<V>>> first = weightIndex.firstEntry();
        return first == null ? 0 : first.getKey();
    }

    /**
     * 获取当前最大实际权重
     */
    private int getCurrentMaxWeight() {
        Map.Entry<Integer, LinkedList<SimpleCacheMember<V>>> last = weightIndex.lastEntry();
        return last == null ? 0 : last.getKey();
    }

    /**
     * 从权重索引中移除成员
     */
    private void removeFromWeightIndex(SimpleCacheMember<V> member, int weight) {
        LinkedList<SimpleCacheMember<V>> bucket = weightIndex.get(weight);
        if (bucket != null) {
            bucket.remove(member);
            if (bucket.isEmpty()) {
                weightIndex.remove(weight);
            }
        }
    }

    /**
     * 将成员加入权重索引
     */
    private void addToWeightIndex(SimpleCacheMember<V> member, int weight) {
        weightIndex.computeIfAbsent(weight, k -> new LinkedList<>()).add(member);
    }

    /**
     * 驱逐权重最低的成员
     */
    private void evict() {
        while (currentSize > maxSize) {
            Map.Entry<Integer, LinkedList<SimpleCacheMember<V>>> minEntry = weightIndex.pollFirstEntry();
            if (minEntry == null) {
                break;
            }
            LinkedList<SimpleCacheMember<V>> minBucket = minEntry.getValue();
            SimpleCacheMember<V> toEvict = minBucket.pollFirst();
            if (toEvict == null) {
                continue;
            }
            // 从 map 中移除（需要遍历，因为 HashMap 不支持通过 value 反查 key）
            // 由于 currentSize 远大于平均每个 weight 的 entry 数，此处 O(n) 可接受
            // 如需 O(1)，需在 SimpleCacheMember 中保存 key引用
            map.values().remove(toEvict);
            currentSize--;
            // 桶中剩余元素需要放回（如果不为空）
            if (!minBucket.isEmpty()) {
                weightIndex.put(minEntry.getKey(), minBucket);
            }
        }
    }

    /**
     * 全局偏移：所有实际权重减去当前最小权重
     * <p>
     * 这使得 globalBase 增加，而各 entry.weight 减少
     * 相对权重关系不变，防止 entry.weight 无限递增溢出
     * <p>
     * 此操作仅在权重即将溢出时触发，频率极低
     */
    private void globalOffset() {
        int minWeight = getCurrentMinWeight();
        globalBase += minWeight;
        if (minWeight > 0) {
            Map<Integer, LinkedList<SimpleCacheMember<V>>> newIndex = new TreeMap<>();
            for (Map.Entry<Integer, LinkedList<SimpleCacheMember<V>>> entry : weightIndex.entrySet()) {
                int newWeight = entry.getKey() - (int) minWeight;
                LinkedList<SimpleCacheMember<V>> newBucket = newIndex.computeIfAbsent(newWeight, k -> new LinkedList<>());
                for (SimpleCacheMember<V> member : entry.getValue()) {
                    member.setWeight(member.getWeight() - minWeight);
                    newBucket.add(member);
                }
            }
            this.weightIndex.clear();
            this.weightIndex.putAll(newIndex);
        }
    }
}
