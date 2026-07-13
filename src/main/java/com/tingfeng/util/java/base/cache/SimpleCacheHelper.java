package com.tingfeng.util.java.base.cache;

import com.tingfeng.util.java.base.cache.base.WeightCacheItem;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 基于访问频率的简单缓存实现
 *
 * 特性：
 * - 读多写少场景下使用 ReadWriteLock，读读并发，写写/读写互斥
 * - 使用 TreeMap + LinkedList 维护权重索引，快速获取 min/max 权重
 * - TreeMap key 直接使用 member.weight，通过 globalOffset 防止溢出
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class SimpleCacheHelper<K, V> {
    /**
     * 权重接近溢出阈值时，触发全局偏移
     * member.weight > 此值时需要偏移
     */
    private static final long WEIGHT_OVERFLOW_THRESHOLD = Integer.MAX_VALUE >> 1;

    private final int maxSize;
    private int currentSize = 0;

    /**
     * 主缓存 Map：key → 缓存成员
     */
    private final Map<K, WeightCacheItem<V>> map;

    /**
     * 权重索引：member.getWeight() → 该权重下的所有成员列表
     * TreeMap 保证 firstKey() = minWeight, lastKey() = maxWeight
     */
    private final TreeMap<Integer, LinkedList<WeightCacheItem<V>>> weightIndex;


    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public SimpleCacheHelper(int maxSize) {
        this.maxSize = maxSize;
        this.map = new HashMap<>();
        this.weightIndex = new TreeMap<>();
    }

    /**
     * 从缓存中取值，命中时权重 +1
     *
     * 惰性删除：获取时发现已过期的 entry 会立即移除并返回 null
     *
     * 读多写少场景优化：
     * 1. 读锁下快速获取数据（读读并发）
     * 2. 释放读锁后，判断是否需要更新权重（容量未满时跳过写锁）
     * 3. 需要时获取写锁进行权重更新（re-check 保证一致性）
     *
     * @param key 键
     * @return 值，不存在或已过期则返回 null
     */
    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        // Phase 1: 读锁下获取数据
        readWriteLock.readLock().lock();
        WeightCacheItem<V> member;
        V value;
        try {
            member = map.get(key);
            if (member == null) {
                return null;
            }
            // 惰性删除：已过期则移除
            if (member.isExpired()) {
                map.remove(key);
                currentSize--;
                removeFromWeightIndex(member, getActualWeight(member));
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
            // re-check：entry 可能已被其他线程驱逐或过期
            member = map.get(key);
            if (member == null || member.isExpired()) {
                return value;  // 已驱逐或过期仍返回原值（权重更新丢失）
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
    private void doUpdateWeight(WeightCacheItem<V> member) {
        if ((int) member.getWeight() > WEIGHT_OVERFLOW_THRESHOLD) {
            globalOffset();
        }
        int oldWeight = getActualWeight(member);
        removeFromWeightIndex(member, oldWeight);
        member.setWeight(member.getWeight() + 1);
        addToWeightIndex(member, getActualWeight(member));
    }

    /**
     * 设置缓存值（永不过期）
     *
     * @param key   键
     * @param value 值
     */
    public void set(K key, V value) {
        set(key, value, 0);
    }

    /**
     * 设置缓存值并指定过期时间
     *
     * @param key             键
     * @param value           值
     * @param expireTimeMillis 过期时间戳（毫秒），0 表示不过期
     */
    public void set(K key, V value, long expireTimeMillis) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        readWriteLock.writeLock().lock();
        try {
            WeightCacheItem<V> member = map.get(key);
            if (member != null) {
                // 已存在：更新值，权重 +1
                doUpdateWeight(member);
                member.setValue(value);
                member.setExpireTime(expireTimeMillis);
            } else {
                // 不存在：驱逐（溢出检查由 doUpdateWeight 统一处理）
                // 新成员初始权重 = 当前最小权重 + 1，避免刚加入就被驱逐
                int initialWeight = getCurrentMinWeight() + 1;
                member = new WeightCacheItem<>(initialWeight, value, expireTimeMillis);
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
     * 主动清理所有已过期的 entry
     *
     * @return 清理的 entry 数量
     */
    public int cleanExpired() {
        readWriteLock.writeLock().lock();
        try {
            int cleanedCount = 0;
            long now = System.currentTimeMillis();
            Iterator<Map.Entry<K, WeightCacheItem<V>>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<K, WeightCacheItem<V>> entry = iterator.next();
                WeightCacheItem<V> member = entry.getValue();
                if (member.getExpireTime() > 0 && now > member.getExpireTime()) {
                    removeFromWeightIndex(member, getActualWeight(member));
                    iterator.remove();
                    currentSize--;
                    cleanedCount++;
                }
            }
            return cleanedCount;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * 是否包含键
     */
    public boolean containsKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
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

    /**
     * 批量获取缓存值
     *
     * 边界条件：
     * - keys 为 null/空：返回空 Map
     * - 内部元素过期：自动过滤（不返回）
     *
     * @param keys 键集合
     * @return 存在的键值对Map（不包含过期/不存在的键）
     */
    public Map<K, V> getAll(Collection<K> keys) {
        if (keys == null || keys.isEmpty()) {
            return new HashMap<>();
        }
        Map<K, V> result = new HashMap<>();
        for (K key : keys) {
            V value = get(key);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * 批量插入缓存值（永不过期）
     *
     * 边界条件：
     * - map 为 null/空：NOP
     *
     * @param map 键值对Map
     */
    public void putAll(Map<K, V> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        for (Map.Entry<K, V> entry : map.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }

    // ==================== 内部方法 ====================

    /**
     * 获取成员的权重（直接取 member.weight）
     */
    private int getActualWeight(WeightCacheItem<V> member) {
        return (int) member.getWeight();
    }

    /**
     * 获取当前最小实际权重
     */
    private int getCurrentMinWeight() {
        Map.Entry<Integer, LinkedList<WeightCacheItem<V>>> first = weightIndex.firstEntry();
        return first == null ? 0 : first.getKey();
    }

    /**
     * 获取当前最大实际权重
     */
    private int getCurrentMaxWeight() {
        Map.Entry<Integer, LinkedList<WeightCacheItem<V>>> last = weightIndex.lastEntry();
        return last == null ? 0 : last.getKey();
    }

    /**
     * 从权重索引中移除成员
     */
    private void removeFromWeightIndex(WeightCacheItem<V> member, int weight) {
        LinkedList<WeightCacheItem<V>> bucket = weightIndex.get(weight);
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
    private void addToWeightIndex(WeightCacheItem<V> member, int weight) {
        weightIndex.computeIfAbsent(weight, k -> new LinkedList<>()).add(member);
    }

    /**
     * 驱逐权重最低的成员
     */
    private void evict() {
        while (currentSize > maxSize) {
            Map.Entry<Integer, LinkedList<WeightCacheItem<V>>> minEntry = weightIndex.pollFirstEntry();
            if (minEntry == null) {
                break;
            }
            LinkedList<WeightCacheItem<V>> minBucket = minEntry.getValue();
            WeightCacheItem<V> toEvict = minBucket.pollFirst();
            if (toEvict == null) {
                continue;
            }
            // 从 map 中移除（需要遍历，因为 HashMap 不支持通过 value 反查 key）
            // 由于 currentSize 远大于平均每个 weight 的 entry 数，此处 O(n) 可接受
            // 如需 O(1)，需在 SimpleCacheMember 中保存 key引用
            // 使用引用比较（==）而非 equals，避免误删所有 equals 的成员
            if (toEvict != null) {
                Iterator<Map.Entry<K, WeightCacheItem<V>>> iterator = map.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<K, WeightCacheItem<V>> entry = iterator.next();
                    if (entry.getValue() == toEvict) {
                        iterator.remove();
                        break; // 只删除第一个匹配的
                    }
                }
            }
            currentSize--;
            // 桶中剩余元素需要放回（如果不为空）
            if (!minBucket.isEmpty()) {
                weightIndex.put(minEntry.getKey(), minBucket);
            }
        }
    }

    /**
     * 全局偏移：所有成员权重减去当前最小权重
     *
     * 相对权重关系不变，防止 member.weight 无限递增溢出
     * 此操作仅在权重即将溢出时触发，频率极低
     */
    private void globalOffset() {
        int minWeight = getCurrentMinWeight();
        if (minWeight > 0) {
            Map<Integer, LinkedList<WeightCacheItem<V>>> newIndex = new TreeMap<>();
            for (Map.Entry<Integer, LinkedList<WeightCacheItem<V>>> entry : weightIndex.entrySet()) {
                int newWeight = entry.getKey() - minWeight;
                LinkedList<WeightCacheItem<V>> newBucket = newIndex.computeIfAbsent(newWeight, k -> new LinkedList<>());
                for (WeightCacheItem<V> member : entry.getValue()) {
                    member.setWeight(member.getWeight() - minWeight);
                    newBucket.add(member);
                }
            }
            this.weightIndex.clear();
            this.weightIndex.putAll(newIndex);
        }
    }
}
