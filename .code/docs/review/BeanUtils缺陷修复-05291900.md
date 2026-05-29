---
验收文档: BeanUtils缺陷修复-05291900
创建时间: 2026-05-29 15:10
最后更新: 2026-05-29 15:29
story路径: .code/docs/review/BeanUtils日志收尾-05291830.md
plan路径: .code/docs/plan/BeanUtils缺陷修复-05291900.md
状态: 进行中
---

# 验收文档 - BeanUtils缺陷修复

## 需求概述

- 整体目标: 修复 BeanUtils/BeanDesc/BeanCopier/StringConverters 的缺陷
- SubStory数量: 4
- 功能点总数: 10（Plan 问题清单）
- 当前审查: SubStory-02 BeanCopier核心重构

## SubStory 验收明细

### SubStory-02: BeanCopier核心重构

**验收状态**: ❌ 未通过

| # | 功能点 | 验收用例 | 验收标准 | 覆盖维度 | 状态 | 测试结果 |
|---|-------|---------|---------|---------|------|---------|
| 1 | F3: copy()/copyFromProvider() 提取公共方法 | 检查 copyProperties() 方法 | 存在公共方法，消除重复内循环代码 | 功能✓ | ✅ | `copyProperties()` 已存在（行137），两个公开方法均委托调用 |
| 2 | F3: copyProperties() 含 Object target 参数 | 检查方法签名 | 参数包含 Object target | 功能✓ | ✅ | 第3参数为 `Object target`（行140）|
| 3 | F3: copy() 传入 target | 检查 copy() 调用 | 调用 copyProperties 时传入 target | 功能✓ | ✅ | 行72传入 `target` |
| 4 | F3: copyFromProvider() 传入 target | 检查 copyFromProvider() 调用 | 调用 copyProperties 时传入 target | 功能✓ | ✅ | 行122传入 `target` |
| 5 | F4: findConverters 循环前预查询 | 检查代码 | try前预查询，catch内不重复调用 | 功能✓ 性能✓ | ✅ | 行199-200 try前预查询，行208 catch内使用预缓存变量 |
| 6 | F5: resolveSourceFieldName 拆分 | 检查方法 | 拆分为 3+ 子方法 | 功能✓ | ❌ | 4个方法，但 resolveByExactMatch() 同类型比较语义失效（见遗留问题） |
| 7 | F6: toMap() Javadoc | 检查注释 | 说明"始终忽略 null，不受 ignoreNull 控制" | 文档✓ | ✅ | 行308-309 注释正确 |
| 8 | 编译通过 | `mvn compile` | BUILD SUCCESS | 编译✓ | ✅ | BUILD SUCCESS |
| 9 | BeanCopierTest 通过 | `mvn test -Dtest=BeanCopierTest` | 21项全部通过 | 功能✓ 回归✓ 边界✓ | ❌ | **20/21 通过，1 ERROR** — `testCopyFromProviderEmptyMap` 失败 |

**功能点验收**: 6/9 通过，2 ❌ 失败

---

### SubStory-03: BeanUtils修复与增强

**验收状态**: ✅ 通过

| # | 功能点 | 验收用例 | 验收标准 | 覆盖维度 | 状态 | 测试结果 |
|---|-------|---------|---------|---------|------|---------|
| 1 | newInstance() 替换 (F7) | grep 确认无 Class.newInstance() | 5处全部替换为 getDeclaredConstructor().newInstance() | 功能✓ 边界✓ | ✅ | 7处 newInstance() 均为合法调用（5处 getDeclaredConstructor() + 1处 Constructor + 1处 Array），0处遗留 Class.newInstance() |
| 2 | deepCopy 集合类型保留 (F8) | deepCopyCollection + deepCopyMap | 反射实例化实际类型，失败降级 ArrayList/HashMap | 功能✓ 边界✓ 异常✓ | ✅ | 反射 ± 降级兜底，异常分支完整 |
| 3 | 参数化构造器 toBean (F9) | 2个新重载方法 | 支持 Constructor + args 参数 | 功能✓ 边界✓ 异常✓ | ✅ | 正确捕获 InstantiationException / IllegalAccessException / InvocationTargetException |
| 4 | 编译通过 | `mvn clean compile -T 4` | BUILD SUCCESS | 编译✓ | ✅ | 230源文件编译通过，0错误 |
| 5 | 定向测试 | `mvn test -Dtest=BeanUtilsTest` | 全部通过 | 功能✓ 回归✓ 边界✓ | ✅ | **53/53 通过**，0失败，0错误，0跳过 |

**功能点验收**: 5/5 通过

---

### SubStory-01: BeanDesc异常与缓存优化

**验收状态**: ❌ 未通过

| # | 功能点 | 验收用例 | 验收标准 | 覆盖维度 | 状态 | 测试结果 |
|---|-------|---------|---------|---------|------|---------|
| 1 | F1: getPropertyValue catch log (行191) | 检查 BeanDesc.java:191 | catch 块有 log.debug() | 功能✓ | ✅ | `log.debug("get property via method failed: {}, fallback to field", name, e)` 正确 |
| 2 | F1: setPropertyValue catch log (行223) | 检查 BeanDesc.java:223 | catch 块有 log.debug() | 功能✓ | ✅ | `log.debug("set property via method failed: {}, fallback to field", name, e)` 正确 |
| 3 | F2: allPropertyNames 字段声明 | 检查 BeanDesc.java:42 | 有 `Set<String> allPropertyNames` 声明 | 功能✓ | ✅ | 字段声明正确，有 Javadoc |
| 4 | F2: allFieldNames 字段声明 | 检查 BeanDesc.java:45 | 有 `Set<String> allFieldNames` 声明 | 功能✓ | ✅ | 字段声明正确，有 Javadoc |
| 5 | F2: 构造器初始化 allPropertyNames | 检查 BeanDesc.java:73 | 构造器中初始化缓存 | 功能✓ | ⚠️ | 已初始化，但仅包含 `pdMap.keySet()`，未按要求合并 `fieldMap.keySet()`（见遗留问题）|
| 6 | F2: 构造器初始化 allFieldNames | 检查 BeanDesc.java:74 | 构造器中初始化缓存 | 功能✓ | ✅ | `Collections.unmodifiableSet(fieldMap.keySet())` 正确 |
| 7 | F2: getPropertyNames() 返回缓存 | 检查 BeanDesc.java:109-111 | 返回缓存的 Set | 功能✓ | ✅ | 返回 `allPropertyNames` |
| 8 | F2: getFieldNames() 返回缓存 | 检查 BeanDesc.java:118-120 | 返回缓存的 Set | 功能✓ | ✅ | 返回 `allFieldNames` |
| 9 | 编译通过 | `mvn compile -T 4` | BUILD SUCCESS | 编译✓ | ❌ | 编译失败 — BeanCopier.java:226 `target` 未定义 |
| 10 | 定向测试 | `mvn test -Dtest=BeanCopierTest` | 全部通过 | 功能✓ 回归✓ | ❌ | 无法执行（编译未通过）|

**功能点验收**: 6/10 通过，1 ⚠️ 警告，3 ❌ 失败

---

## 验收状态总览

| SubStory | 状态 | 功能点 | 通过 | 失败 | 待验证 |
|----------|------|-------|------|------|--------|
| subStory-01 | ❌ | 10 | 6 | 3 | 1 |
| subStory-02 | ❌ | 9 | 6 | 2 | 1 |
| subStory-03 | ✅ | 5 | 5 | 0 | 0 |
| **汇总** | ❌ | **24** | **17** | **5** | **2** |

---

## 遗留问题与优化点

### 高严重度（阻塞验收）

- **`BeanCopier.java:179-182` — `containsKey` 检查在 `sourceDesc != null` 块内，`copyFromProvider()` 路径跳过此检查**
  问题：`containsKey` 检查位于 `if (sourceDesc != null)` 块内（行179-182），当 `copyFromProvider()` 调用 `copyProperties()` 并传入 `sourceDesc = null` 时，整个块跳过。provider 中不存在的属性（如空 Map）不经过 `containsKey` 过滤，直接取值（null）并尝试赋给 `int` 等基本类型字段，导致 `IllegalArgumentException`。
  表现：`testCopyFromProviderEmptyMap` 测试失败（21项测试中1项 ERROR）
  修复建议：将 `containsKey` 检查移出 `if (sourceDesc != null)` 块，使其对两条路径均生效。Plan 设计（3.4节流程图）明确 `copyFromProvider()` 需要 `provider.containsKey()` 检查。
  - 来源: [FEEDBACK:myExec] - 影响: subStory-02 (阻塞)

### 中严重度（影响质量）

- **`BeanCopier.java:437-438` — `resolveByExactMatch()` 中"同类型检查"语义失效**
  问题：`srcType` 和 `sourcePropType` 均来自 `sourceDesc.getPropertyType(sourceFieldName)`（相同来源），比较始终为 true。Plan 3.9节设计签名要求 `resolveByExactMatch(BeanDesc, String, Class<?>)`（含 target 类型参数），但实现仅 `resolveByExactMatch(BeanDesc, String)`，缺少 target 类型参数，导致"同类型"比较丧失语义。
  - 来源: [FEEDBACK:myExec] - 影响: subStory-02

- **`BeanDesc.java:73` — `allPropertyNames` 仅包含 pdMap.keySet()，未合并 fieldMap.keySet()**
  问题：Javadoc（行105）声明为"pdMap ∪ fieldMap的key集合"，Plan 3.8节和 Task 描述均要求"合并 pdMap.keySet() + fieldMap.keySet()"，但实现仅包含 pdMap 的 key。
  - 来源: [FEEDBACK:myExec] - 影响: subStory-01

### 低风险发现（优化建议）

- `BeanUtils.java:129,145` — 新增的两个 Constructor 重载方法的 Javadoc 第一行沿用 "将source对象转换为targetClass类型的实例"，但参数是 `Constructor<T>` 而非 `Class<T>`。建议调整描述。

---

## 打回记录

| 时间 | SubStory | 轮次 | 来自 | 问题摘要 | 修复状态 |
|------|----------|------|------|---------|---------|
| 2026-05-29 15:15 | subStory-01 | 1 | myExec | ① `allPropertyNames` 仅含 pdMap.keySet() 未合并 fieldMap<br>② BeanCopier.java:226 编译失败（target 未定义） | ⏳ 待修复 |
| 2026-05-29 15:29 | subStory-02 | 1 | myExec | ① `containsKey` 检查在 sourceDesc!=null 块内，copyFromProvider 路径跳过<br>② `resolveByExactMatch()` 同类型比较语义失效 | ⏳ 待修复 |

---

## 验收结论

- **整体验收**: ❌ 未通过（subStory-02 验收失败）
- **验收人**: myReview
- **验收时间**: 2026-05-29 15:29

### SubStory-02 验证结果汇总

| 验证项 | 结果 |
|--------|------|
| **F3 — copyProperties() 存在** | ✅ 方法已提取，含 Object target 参数 |
| **F3 — copy() 传入 target** | ✅ 行72: `copyProperties(..., target, ...)` |
| **F3 — copyFromProvider() 传入 target** | ✅ 行122: `copyProperties(..., target, ...)` |
| **F4 — findConverters 预缓存** | ✅ 行199-200 try前预查询，行208 catch内用预缓存变量 |
| **F5 — resolveSourceFieldName 拆分** | ⚠️ 拆分为4方法，但 resolveByExactMatch 类型比较语义失效 |
| **F6 — toMap() Javadoc** | ✅ 行308-309 注释正确 |
| **编译 (mvn compile)** | ✅ **BUILD SUCCESS** |
| **定向测试 BeanCopierTest** | ❌ **20/21 通过，1 ERROR** — `testCopyFromProviderEmptyMap` 失败 |

### 变更范围确认

- ✅ 仅修改 BeanCopier.java 一个文件
- ✅ 未修改 pom.xml
- ✅ 未新增第三方依赖
- ✅ 未修改公共 API 签名
- ❌ 测试失败（`copyFromProvider` 的 `containsKey` 检查遗漏）
