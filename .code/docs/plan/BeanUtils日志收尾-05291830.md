# Plan — BeanUtils 日志收尾（最终未完成项）

**创建时间**：2026-05-29 18:30
**最后更新**：2026-05-29 18:30
**状态**：已完成

---

## 1. 背景与目标

### 1.1 综合状态总览

经过对过往全部 4 个 Story + 5 个 Plan 的源码逐一验证，本项目所有 BeanUtils 相关需求已基本实现，**仅剩一项未完成**：

| 阶段 | 状态 | 来源 |
|------|------|------|
| 第1期：BeanUtils重构（BeanCopier/CopyOptions/BeanDesc/BeanUtil/ValueProvider/Converter） | ✅ 全部完成 | `BeanUtils重构-202605242030` |
| 第2期：需求完善（ignoreCase/ignoreError/isEmpty/hasNullField/Predicate删除/文档） | ✅ 全部完成 | `BeanUtil需求完善-202605281130` |
| 第3期：优化（deepCopy/toBean(Map)/getPropertyType/TypeReference/Optional装箱） | ✅ 全部完成 | `BeanUtils优化-05291430` |
| 修复：日志迁移 + BeanDesc API 优化 | ⚠️ **BeanCopier.java 日志迁移未完成** | `BeanUtils修复-05291440` Task 2 |

### 1.2 本轮目标

完成 **BeanCopier.java** 的日志框架迁移：从 commons-logging 迁移到 Lombok `@Slf4j`，与 `BeanUtils.java` 保持一致。

---

## 2. 变更分析

### 2.1 涉及文件

| 文件 | 操作 | 说明 |
|------|------|------|
| `bean/copier/BeanCopier.java` | MODIFY | 日志框架迁移 |

### 2.2 变更前后对比

```diff
- import org.apache.commons.logging.Log;
- import org.apache.commons.logging.LogFactory;
+ import lombok.extern.slf4j.Slf4j;

- private static final Log logger = LogFactory.getLog(BeanCopier.class);
+ // 由 @Slf4j 类注解自动生成

- logger.debug("...");
- logger.isDebugEnabled()
+ log.debug("...");
+ log.isDebugEnabled()
```

### 2.3 变更明细

| 行号 | 当前代码 | 目标代码 |
|------|---------|---------|
| 7 | `import org.apache.commons.logging.Log;` | **移除** |
| 8 | `import org.apache.commons.logging.LogFactory;` | **移除** |
| — | — | **新增** `import lombok.extern.slf4j.Slf4j;` |
| 25 | `public class BeanCopier {` | `@Slf4j` + `public class BeanCopier {` |
| 27-28 | `/** 日志 */ private static final Log logger = ...` | **移除** |
| 141 | `if (logger.isDebugEnabled())` | `if (log.isDebugEnabled())` |
| 142 | `logger.debug(...)` | `log.debug(...)` |
| 153 | `if (logger.isDebugEnabled())` | `if (log.isDebugEnabled())` |
| 154 | `logger.debug(...)` | `log.debug(...)` |
| 266 | `if (logger.isDebugEnabled())` | `if (log.isDebugEnabled())` |
| 267 | `logger.debug(...)` | `log.debug(...)` |
| 278 | `if (logger.isDebugEnabled())` | `if (log.isDebugEnabled())` |
| 279 | `logger.debug(...)` | `log.debug(...)` |

### 2.4 兼容性分析

| 检查项 | 结果 |
|--------|------|
| SLF4J 已在 pom.xml 中声明 (provided) | ✅ `slf4j-log4j12 1.7.25` |
| Lombok 已在 pom.xml 中声明 (provided) | ✅ `lombok 1.18.24` |
| BeanUtils.java 已成功使用 `@Slf4j` 的先例 | ✅ 第10行 `import lombok.extern.slf4j.Slf4j;` + 第35行 `@Slf4j` |
| `log.debug("msg", e)` 签名兼容 | ✅ SLF4J 与 commons-logging API 签名一致 |
| commons-logging 依赖是否可移除 | ❌ **不可移除** — WebServiceUtils 仍使用 `commons-logging 1.2` |

---

## 3. 核心边界条件与异常处理

| 场景 | 处理策略 |
|------|---------|
| pom.xml 中 SLF4J/Lombok 版本 | 不动已有声明 |
| commons-logging pom.xml 依赖 | 不动（WebServiceUtils 仍使用） |
| BeanUtils.java 已用 `@Slf4j` 验证通过 | 与 BeanCopier 使用完全相同的模式 |
| `log.isDebugEnabled()` 性能 | SLF4J 的 isDebugEnabled + 参数化日志优于手动拼接 |

---

## 4. 子任务评分

| 功能 | 参数 | 逻辑 | 依赖 | 边界 | 产出 | 状态 | 异常 | 外部 | 总分 | 级别 |
|------|------|------|------|------|------|------|------|------|------|------|
| F1: BeanCopier @Slf4j 迁移 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | **8** | 极简 |

**分组**：极简，独立 Task，无需合并。

**DAG**：
```
Task 1-1 (BeanCopier @Slf4j 日志迁移) — 独立，无依赖
```

---

## 5. 子任务分解清单

### SubStory-01：日志框架迁移

---

#### Task 1-1：BeanCopier @Slf4j 日志迁移

**功能**：
- 将 BeanCopier.java 的 commons-logging 日志替换为 Lombok `@Slf4j`
- 移除 `import org.apache.commons.logging.Log;` / `import org.apache.commons.logging.LogFactory;`
- 类级别添加 `@Slf4j` 注解
- 所有 `logger.xxx()` 改为 `log.xxx()`
- 移除手写 `private static final Log logger = ...` 字段

**涉及文件**：

| 文件 | 类型 | 用途 |
|------|------|------|
| `bean/copier/BeanCopier.java` | MODIFY | 日志框架从 commons-logging 迁移到 @Slf4j |

**依赖**：无

**上下文**：
- `@Slf4j` 加在类级别，Lombok 自动生成 `private static final Logger log = LoggerFactory.getLogger(BeanCopier.class);`
- BeanUtils.java 已有完全相同的模式可作为参考
- `isDebugEnabled()` 和 `debug()` 签名 SLF4J 与 commons-logging 完全一致
- commons-logging 的 pom.xml 依赖不动（WebServiceUtils 仍使用）

**查找指引**：
- BeanUtils.java 第10行 `import lombok.extern.slf4j.Slf4j;` 和第35行 `@Slf4j` — 同模式参考
- BeanCopier.java 第7-8行 — 要移除的 import
- BeanCopier.java 第28行 — 要移除的 logger 字段
- BeanCopier.java 第141/142/153/154/266/267/278/279行 — 要替换的日志调用

---

## 6. 验收标准

| # | 验收项 | 验证方式 |
|---|--------|---------|
| 1 | BeanCopier.java 没有 `org.apache.commons.logging` 导入 | grep 检查 |
| 2 | BeanCopier.java 存在 `import lombok.extern.slf4j.Slf4j;` | 文件检查 |
| 3 | BeanCopier.java 存在 `@Slf4j` 注解 | 文件检查 |
| 4 | BeanCopier.java 没有 `private static final Log logger` 定义 | 文件检查 |
| 5 | 所有 `logger.xxx()` 改为 `log.xxx()` | grep 检查 `logger.` 无结果（注释除外）|
| 6 | 编译通过 | `mvn compile` |
| 7 | BeanCopier 测试通过 | `mvn test -Dtest=BeanCopierTest` |
| 8 | 全量测试通过 | `mvn test` |

---

## 7. 三阶段自检

### ✅ 覆盖检验

| 来源 Story/Plan | 功能点 | 方案 |
|-----------------|--------|------|
| `BeanUtils修复-05291440` Task 2 | BeanCopier 日志迁移 | ✅ Task 1-1: commons-logging → @Slf4j |

### ✅ 目标对齐

- 仅完成日志统一目标中 BeanCopier 的未完成部分
- 不修改 BeanCopier 业务逻辑，仅修改日志框架
- 不修改 pom.xml（commons-logging 依赖保留）

### ✅ 可行检验

- BeanUtils.java 已成功使用 `@Slf4j`，方式完全一致
- 所有依赖已在 pom.xml 中声明
- commons-logging 保留不动（WebServiceUtils 仍使用）

### ✅ 完整性检验

- Task 涉及文件、无额外依赖、查找指引明确
- 测试验证全量覆盖

---

## 8. 关键设计决策

```
[DECISIONS]
- 任务范围: 仅 BeanCopier.java 一个文件的日志迁移
- 迁移方式: commons-logging → Lombok @Slf4j（与 BeanUtils.java 一致）
- pom.xml 不变: commons-logging 依赖保留（WebServiceUtils 仍使用）
- 不涉及任何业务逻辑变更
- 无共享数据类（无需 Task 0）
- DAG: 单 Task，无依赖
[/DECISIONS]
```

---

> **确认门控说明**：覆盖检验、目标对齐、可行检验、完整性检验全部通过。确定性 ≥99%。
