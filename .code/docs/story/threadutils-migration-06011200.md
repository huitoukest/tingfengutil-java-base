# ThreadUtils 迁移需求文档

## 元数据

- 创建时间: 2026-06-01 12:00
- 最后更新: 2026-06-01 12:00
- 状态: 待确认

---

## 整体目标

将 `ThreadUtils`（common/utils）完整迁移到 `concurrent` 包，并制定 concurrent 包的优化计划，最终在验证通过后删除旧的 ThreadUtils。

---

## 项目上下文

### 关键约束（来自 4-preferences.md）
- 接口命名统一不使用 I 前缀/后缀，采用 UpperCamelCase
- tool 类优先用 Utils 命名（静态方法），有上下文的用 Helper
- lang/ex/ 包已废弃，使用 lang/exception/ 替代
- 缩进统一使用 4 空格，禁用 Tab

### 架构信息（来自 1-architecture.md）
- 包根路径：`com.tingfeng.util.java.base`
- concurrent 包职责：并发工具

---

## 现状分析

### 旧 ThreadUtils（common/utils/ThreadUtils.java）

```java
@Deprecated
public class ThreadUtils {
    public static void sleep(long mills) {
        com.tingfeng.util.java.base.concurrent.ThreadUtils.sleep(mills);
    }
    public static ThreadFactory newNamedThreadFactory(String namePrefix, boolean isDaemon) {
        return com.tingfeng.util.java.base.concurrent.ThreadFactoryUtils.newNamedThreadFactory(namePrefix, isDaemon);
    }
}
```

**功能**: 2 个方法（代理到 concurrent 包）

### concurrent 包现有文件

| 文件 | 功能 | 方法数 |
|------|------|--------|
| ThreadUtils.java | sleep/join/interrupt/状态检查 | 9 |
| ThreadFactoryUtils.java | 线程工厂创建 | 5 |
| ThreadPoolUtils.java | 线程池创建/关闭/状态 | 10 |
| ThreadLocalUtils.java | ThreadLocal 清理/计数 | 4 |
| ThreadGroupUtils.java | 线程组获取/信息 | 4 |
| BaseFrequencyHelper.java | 频率控制抽象类 | 3 |
| base/NamedThreadFactory.java | 命名线程工厂实现 | 1 |

### 功能迁移对照

| 旧 ThreadUtils 方法 | concurrent 包对应 | 状态 |
|-------------------|------------------|------|
| sleep(long) | ThreadUtils.sleep(long) | ✅ 已迁移 |
| newNamedThreadFactory(String, boolean) | ThreadFactoryUtils.newNamedThreadFactory(String, boolean) | ✅ 已迁移 |

**结论**: ThreadUtils 的所有功能均已迁移到 concurrent 包，旧类仅作为代理存在。

---

## 确认门控 — 疑问确认

根据"疑问确认原则"，以下问题需要用户确认后才能推进：

### Q1: concurrent 包 P0 缺陷检查

用户要求 P0 缺陷修复，但未指明具体缺陷。请确认：
- concurrent 包中是否存在已知的 P0 缺陷需要修复？
- 还是仅需按规范进行代码审查后确认无缺陷？

### Q2: TestUtils 是否需要迁移？

`common/utils/TestUtils.java` 包含大量并发测试功能（runConcurrentTest、testForDeadlock、findConcurrencyLimit 等）。

请确认：
- A: TestUtils 属于测试工具，保持在 common/utils 不迁移
- B: TestUtils 应迁移到 concurrent 包作为并发测试支持

### Q3: 删除 ThreadUtils 的前置条件

用户要求"完成迁移、完善、验证后，人工确认并删除 ThreadUtils"。

请确认删除前置条件：
- A: 验证 concurrent 包测试覆盖率 ≥ 80% 且全部通过
- B: 验证 concurrent 包无 P0/P1 问题
- C: 其他条件（请说明）

### Q4: P1 功能完善的具体范围

用户要求 P1 功能完善，但 concurrent 包现有功能较为完整。请确认：
- 是否有具体功能缺失需要补充？
- 还是仅需审查现有功能的健壮性（边界处理、异常处理）？

### Q5: P2 性能评估 的具体需求

用户要求 P2 性能评估。请确认：
- 是否有具体方法需要性能优化？
- 还是仅需建立性能基线？

---

## SubStory 分解

### 功能覆盖矩阵

| 功能点 | 对应 SubStory | 验收标准 |
|--------|-------------|---------|
| ThreadUtils 迁移 | 01-迁移验证 | 旧 ThreadUtils 的 2 个方法已正确代理到 concurrent 包 |
| P0 缺陷修复 | 02-缺陷修复 | 无 P0 级别缺陷 |
| P1 功能完善 | 03-功能完善 | 现有功能边界处理完整 |
| P2 性能评估 | 04-性能评估 | 建立性能基线（可选） |
| P3 规范注释 | 05-注释规范 | Javadoc 完整，无 HTML 标签 |
| 删除旧 ThreadUtils | 06-删除确认 | 用户人工确认后删除 |

### SubStory 列表

| 序号 | 名称 | 范围 | 验收标准 | 依赖 |
|------|------|------|---------|------|
| 01 | 迁移验证 | 验证 ThreadUtils 2 个方法正确代理 | 编译通过，测试通过 | 无 |
| 02 | 缺陷修复 | 检查并修复 P0 缺陷 | 无 P0 缺陷 | 01 |
| 03 | 功能完善 | 审查功能健壮性 | 边界/异常处理完整 | 02 |
| 04 | 性能评估 | 建立性能基线 | 记录性能数据（可选） | 03 |
| 05 | 注释规范 | 审查注释规范性 | 无 HTML 标签，Javadoc 完整 | 03 |
| 06 | 删除确认 | 等待用户确认后删除旧类 | 用户确认 | 01-05 |

### DAG

```
01 → 02 → 03 → 04
           ↓
          05
           ↓
          06
```

---

## [DECISIONS]

等待用户确认以下决策点：

- **迁移策略**: ThreadUtils 2 个方法已通过代理迁移到 concurrent 包，无需重复迁移
- **缺失功能**: 经扫描，未发现缺失功能
- **删除时机**: 用户人工确认后删除 ThreadUtils
- **待确认问题**: Q1-Q5（见上文）

---

## [ROUTE_TO:myPlan]

等待用户回答上述 Q1-Q5 确认问题后，进入 myPlan 阶段制定具体实施计划。

---

## 备注

- common/utils/TestUtils.java 属于测试工具类，与 concurrent 业务工具职责不同，建议保持在原位置
- concurrent 包现有功能覆盖较为完整，主要是验证性工作
