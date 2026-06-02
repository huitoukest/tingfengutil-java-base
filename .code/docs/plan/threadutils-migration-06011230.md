# ThreadUtils 迁移方案设计

- 创建时间: 2026-06-01 12:30
- 最后更新: 2026-06-01 12:35
- 状态: 已完成

---

## 整体目标

基于已完成的 story，将 `ThreadUtils`（common/utils）迁移到 `concurrent` 包并完成优化，最终在验证通过后删除旧类。

**用户确认决策：**
- Q1: 主动排查 P0 缺陷 ✅ 已执行，发现 4 个 P0 缺陷
- Q2: TestUtils 保持在 common/utils ✅ 确认
- Q3: 删除条件为无 P0/P1 问题 ✅ 确认
- Q4: P1 健壮性审查 ✅ 采用默认
- Q5: P2 性能基线 ✅ 采用默认

---

## P0 缺陷主动排查结果

### 缺陷清单

| 缺陷ID | 文件 | 缺陷类型 | 问题描述 | 修复方案 |
|--------|------|---------|---------|---------|
| P0-1 | `BaseFrequencyHelper` | 死锁风险 | synchronized 方法内调用 `Thread.sleep()` + 递归调用，锁长期持有 | 重构为非阻塞频率控制，使用 `AtomicInteger` + 时间戳判断 |
| P0-2 | `ThreadPoolUtils.newCachedThreadPool` | 资源耗尽 | 最大线程数 `Integer.MAX_VALUE` 无限制 | 设置合理上限（如 256） |
| P0-3 | `ThreadPoolUtils.newFixedThreadPool` | OOM风险 | `LinkedBlockingQueue<>()` 无容量限制 | 必须传入 queueSize 参数，默认值使用有界队列 |
| P0-4 | `ThreadPoolUtils`（所有线程池） | 拒绝策略缺失 | 未显式设置拒绝策略 | 添加 `CallerRunsPolicy` 作为默认策略，并文档说明 |

### 缺陷分析详述

#### P0-1: BaseFrequencyHelper 死锁风险

**位置**: `BaseFrequencyHelper.checkAndLimitFrequency()`

**问题代码**:
```java
public synchronized void checkAndLimitFrequency(){
    if(incrementalCount++ >= secondMaxCount){
        long sleepTime = getSleepTimeWhileOverMaxCount(...);
        if(sleepTime > 0) {
            Thread.sleep(sleepTime);  // 在 synchronized 方法中 sleep
        }
        checkAndLimitFrequency();  // 递归调用
    }
}
```

**风险**:
1. `synchronized` 方法持有锁时调用 `Thread.sleep()`，导致其他线程被阻塞
2. 递归调用深度不可控，高并发下可能引发 StackOverflowError
3. 不符合并发编程规范（禁止在同步块中执行阻塞操作）

**修复方案**:
- 移除 synchronized，改用 `AtomicInteger` 计数
- 移除递归，改用循环 + 时间戳判断
- sleep 操作移至调用方控制

#### P0-2: newCachedThreadPool 资源耗尽

**位置**: `ThreadPoolUtils.newCachedThreadPool()`

**问题代码**:
```java
return new java.util.concurrent.ThreadPoolExecutor(
    0, Integer.MAX_VALUE,  // 最大线程数无限制
    60L, TimeUnit.SECONDS,
    new SynchronousQueue<>(),
    ThreadFactoryUtils.newNamedThreadFactory(namePrefix + "-", false)
);
```

**风险**: 高负载场景下可能创建超过系统承受能力的线程数

**修复方案**: 设置最大线程数上限为 256（根据 CPU 核心数和负载调整）

#### P0-3: newFixedThreadPool OOM 风险

**位置**: `ThreadPoolUtils.newFixedThreadPool(int, String)`

**问题代码**:
```java
return new java.util.concurrent.ThreadPoolExecutor(
    nThreads, nThreads,
    0L, TimeUnit.MILLISECONDS,
    new LinkedBlockingQueue<>(),  // 无界队列
    ThreadFactoryUtils.newNamedThreadFactory(namePrefix + "-", false)
);
```

**风险**: 任务堆积时可能耗尽内存

**修复方案**: 保留原方法但文档声明必须传入 queueSize；新增重载方法强制要求 queueSize

#### P0-4: 线程池拒绝策略缺失

**位置**: 所有 `ThreadPoolUtils` 线程池创建方法

**问题**: 未显式设置拒绝策略

**修复方案**: 所有线程池添加 `CallerRunsPolicy` 作为默认拒绝策略，并在 Javadoc 中说明

---

## 优化计划（P0-P3）

| 优先级 | 范围 | 目标 | 依赖 |
|--------|------|------|------|
| **P0** | 缺陷修复 | 修复 4 个 P0 缺陷 | 无 |
| **P1** | 健壮性审查 | 审查所有方法的边界处理、异常处理 | P0 |
| **P2** | 性能基线 | 建立核心方法性能基线（可选） | P1 |
| **P3** | 注释规范 | 审查 Javadoc 规范性，移除 HTML 标签 | P1 |
| **P4** | 删除确认 | 用户确认后删除旧 ThreadUtils | P0-P3 |

---

## 核心实体关系

```
concurrent/
├── ThreadUtils              ← 线程操作（sleep/join/interrupt）
├── ThreadFactoryUtils      ← 线程工厂创建
│   └── base/NamedThreadFactory  ← 命名线程工厂实现
├── ThreadPoolUtils          ← 线程池管理 [有P0缺陷]
├── ThreadLocalUtils        ← ThreadLocal 清理 [有P1反射问题]
├── ThreadGroupUtils        ← 线程组操作
├── BaseFrequencyHelper     ← 频率控制 [有P0缺陷]
└── TestUtils (保留在common/utils)
```

---

## API 设计

### ThreadPoolUtils 新增/修改方法

| 方法名 | 参数 | 返回值 | 说明 |
|--------|------|--------|------|
| `newCachedThreadPool(String, int)` | namePrefix, maxThreads | ExecutorService | 新增带最大线程数限制的缓存池 |
| `newFixedThreadPool(int, int, String)` | nThreads, queueSize, namePrefix | ExecutorService | 新增必须指定队列大小的固定池 |
| `shutdownAndAwait(ExecutorService, long, TimeUnit, Runnable)` | executor, timeout, unit, fallbackTask | boolean | 新增带 fallback 的关闭方法 |

### BaseFrequencyHelper 重构

| 方法名 | 参数 | 返回值 | 说明 |
|--------|------|--------|------|
| `checkAndLimitFrequency()` | void | boolean | 返回是否被限流（非阻塞） |
| `isOverLimit()` | void | boolean | 检查是否超过频率限制 |
| `getSleepTime()` | void | long | 获取需睡眠时间（供调用方决定是否 sleep） |

---

## 异常处理策略

| 边界 | 处理方式 |
|------|---------|
| Thread.sleep 负数参数 | 抛出 IllegalArgumentException |
| ThreadPoolUtils queueSize ≤ 0 | 使用 SynchronousQueue |
| ThreadLocalUtils 反射失败 | 抛出 RuntimeException 并包含原异常 |
| BaseFrequencyHelper secondMaxCount ≤ 0 | 禁用频率限制，直接返回 |

---

## 子任务分解清单

### 功能清单

| 功能 | 涉及文件 | 依赖 | 查找指引 |
|------|---------|------|---------|
| **P0-1: 修复 BaseFrequencyHelper 死锁** | `concurrent/BaseFrequencyHelper.java` | 无 | 参考 java-basic-coding 并发规范 |
| **P0-2: 修复 newCachedThreadPool 无限线程** | `concurrent/ThreadPoolUtils.java` | 无 | 参考 ConcurrentHashMap 线程数上限设计 |
| **P0-3: 修复 newFixedThreadPool 无界队列** | `concurrent/ThreadPoolUtils.java` | 无 | 新增 queueSize 必需参数重载 |
| **P0-4: 添加显式拒绝策略** | `concurrent/ThreadPoolUtils.java` | 无 | 使用 CallerRunsPolicy |
| **P1-1: ThreadLocalUtils 反射安全处理** | `concurrent/ThreadLocalUtils.java` | P0 | 添加 SecurityManager 检查 |
| **P1-2: ThreadUtils 边界检查增强** | `concurrent/ThreadUtils.java` | P0 | sleep 负数检查 |
| **P3-1: Javadoc 规范审查** | 所有 concurrent 文件 | P1 | 移除 HTML 标签，确认纯文本首行 |
| **P4-1: 删除旧 ThreadUtils** | `common/utils/ThreadUtils.java` | P0-P3 | 用户人工确认后执行 |

---

## DAG

```
P0-1 ──┬─▶ P1-1 ──▶ P3-1 ──▶ P4-1
P0-2 ──┤
P0-3 ──┤
P0-4 ──┘
       └─▶ P1-2 ──────────┘
                      ↓
                    [删除确认]
```

---

## 删除旧 ThreadUtils 前置条件

| 条件 | 验证方式 |
|------|---------|
| concurrent 包所有 P0 缺陷已修复 | 代码审查 + 编译通过 |
| concurrent 包所有 P1 健壮性问题已处理 | 边界测试通过 |
| ThreadUtils 迁移代理功能验证通过 | 单元测试通过 |
| 用户人工确认 | 用户明确回复"确认删除" |

---

## 验收标准

- [ ] P0 缺陷全部修复
- [ ] P1 健壮性审查完成
- [ ] Javadoc 无 HTML 标签
- [ ] 所有 concurrent 测试通过
- [ ] 用户确认删除旧 ThreadUtils