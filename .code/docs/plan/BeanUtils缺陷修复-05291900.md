---
创建时间: 2026-05-29 19:00
最后更新: 2026-05-29 19:00
状态: 已完成
---

# Plan — BeanUtils 缺陷修复

## 1. 背景

基于 BeanUtils 日志收尾审查报告（`.code/docs/review/BeanUtils日志收尾-05291830.md`）及后续分析，梳理出 10 个待处理问题，
涵盖 BeanCopier、BeanDesc、BeanUtils、StringConverters 四个核心文件。

**本次方案不包括（用户已确认）:**
- Converter 触发值不可能为 null，无需修改 Optional 转换器
- deepCopy 递归深度不直接限制，而是支持传入参数控制（待后续设计）
- 大范围→小范围数值转换静默失败 → 注释中说明即可（本次暂不处理）
- FieldAttribute 已删除 → 无需处理
- SimpleCacheHelper evict 策略先不动

## 2. 问题清单与决策

| # | 严重度 | 模块 | 文件:行号 | 问题简述 | 处理方式 |
|---|--------|------|-----------|---------|---------|
| 1 | Critical | BeanDesc | BeanDesc.java:179,211 | catch (Exception e) 过宽，吞异常无日志 | ✅ 修复 |
| 2 | High | StringConverters | StringConverters.java:143 | 异常包装使用 RuntimeException 丢失语义 | ✅ 修复 |
| 3 | High | BeanCopier | BeanCopier.java:125-126,250-251 | findConverters() 循环内重复调用 | ✅ 修复 |
| 4 | High | BeanCopier | BeanCopier.java:43-280 | copy()/copyFromProvider() ~80% 代码重复 | ✅ 修复 |
| 5 | Medium | BeanUtils | BeanUtils.java:86-97 | 缺少参数化构造函数支持 | ✅ 修复（新增重载） |
| 6 | Medium | BeanUtils | BeanUtils.java:540-551 | deepCopy 丢失集合具体类型 | ✅ 修复 |
| 7 | Medium | BeanCopier | BeanCopier.java:377-380 | toMap() 固定忽略 null，与 copy() 行为不一致 | ✅ 文档说明 |
| 8 | Medium | BeanDesc | BeanDesc.java:94-99 | getPropertyNames() 每次新建 HashSet | ✅ 修复 |
| 9 | Medium | BeanCopier | BeanCopier.java:418-461 | resolveSourceFieldName() 圈复杂度过高 | ✅ 修复（拆分方法） |
| 10 | Medium | BeanUtils | BeanUtils.java:91,118,146,173,582 | Class.newInstance() Java 9 废弃 | ✅ 修复 |

## 3. 详细修复方案

### 3.1 [Critical] BeanDesc 异常吞没 — 行179/211

**现状:**
```java
// 行179 (getPropertyValue): catch (Exception e) { /* 降级到field方式 */ }
// 行211 (setPropertyValue): catch (Exception e) { /* 降级到field方式 */ }
```

**修复方案:**
两个 catch 块各增一条 `log.debug()`，记录降级原因但不阻断流程：
- `log.debug("get/set property via method failed: {}, fallback to field", name, e)`
- 保持降级逻辑不变

**涉及:**
- BeanDesc.java:179 — getPropertyValue 的 catch
- BeanDesc.java:211 — setPropertyValue 的 catch

**风险:** 极低。仅写入日志，不改变业务逻辑。
**影响范围:** 仅 BeanDesc.java

---

### 3.2 [High] StringConverters 异常包装 — 行143

**现状:**
```java
// String -> URL 转换
throw new RuntimeException(e);
```

**修复方案:**
使用 `ConverterException`（已有且继承 `BaseException`）替换 `RuntimeException`：
```java
throw new ConverterException("Failed to parse URL: " + s.trim(), e);
```

**ConverterException 可用构造器:** `ConverterException(String message, Throwable cause)` — 已存在，可保留完整堆栈。

**涉及:**
- StringConverters.java:143

**风险:** 低。仅替换异常类型，调用方 catch 逻辑不受影响（均为 BaseException 子类）。

---

### 3.3 [High] BeanCopier findConverters() 循环内重复 — 行125/250

**现状:**
```java
// 行118-142 (copy) 和 行243-267 (copyFromProvider):
catch (Exception e) {
    boolean hasConverter = !ConverterRegistry.getInstance()
            .findConverters(value.getClass(), targetType).isEmpty();
    // ...
}
```

每次转换失败时都重新调用 `findConverters()` 查询注册表，性能浪费。

**修复方案:**
在 try 块前预查询 `findConverters` 结果：
```java
boolean converterExists = useConverter && !ConverterRegistry.getInstance()
        .findConverters(value.getClass(), targetType).isEmpty();
try {
    // ... convert ...
} catch (Exception e) {
    if (converterExists) {
        throw new BaseException(...);
    }
    // ignoreNoMatchConverterError handling
}
```

注意：此修复与 3.4（公共逻辑提取）高度耦合，应合并实现。

---

### 3.4 [High] BeanCopier copy()/copyFromProvider() 代码重复 — 行43-280

**现状:**
`copy()` 和 `copyFromProvider()` 两个方法的属性遍历循环体高度相似（~80% 相同），仅在取值来源处差异：
- `copy()`: 从 `sourceDesc.getPropertyValue()` 取值 + `resolveSourceFieldName()` 匹配
- `copyFromProvider()`: 从 `provider.value()` 取值 + `provider.containsKey()` 检查

**修复方案:**
提取公共的内循环处理逻辑为私有方法，两个公开方法只做前置准备和取值差异部分：

```
copy(source, target, options)                           copyFromProvider(provider, target, options)
  │                                                         │
  ├─ null check                                             ├─ null check
  ├─ init options                                           ├─ init options
  ├─ get sourceDesc                                         ├─ (无 sourceDesc)
  ├─ get targetDesc                                         ├─ get targetDesc
  ├─ get settings                                           ├─ get settings
  └─ iterate properties ──▶ forEachProp(sourceInfo, ──◀─── └─ iterate properties
                            targetDesc, options) {               (provider instead of source)
      extractSourceFieldName()                                 provider.containsKey()
      getValue()                                               provider.value()
      nullCheck()
      typeConversion()
      setValue()
  }
```

**提取的公共方法签名:**
```java
private static void copyProperties(
        ValueProvider<Object> valueProvider,  // 取值函数接口
        CopyOptions options,
        BeanDesc targetDesc,
        Set<String> targetPropertyNames,
        Class<?> sourceType  // 仅用于类型检测
);
```

或者采用更简单的函数式回调方式，提取内循环，将"取值"步骤作为参数传入。

**同时包含的修复:**
- 3.3 (findConverters 预缓存) 集成到公共方法的类型转换步骤中
- 现有 `getOrCreateBeanDesc` / `getTargetPropertyType` / `buildLowerCaseNameMap` 保持不动

**涉及:**
- BeanCopier.java:43-155 (copy 方法体)
- BeanCopier.java:176-280 (copyFromProvider 方法体)

**风险:** 中。重构后需确保两种方法的差异逻辑正确保留（取值源不同、fieldName 解析方式不同）。

---

### 3.5 [Medium] BeanUtils 缺少参数化构造器支持 — 行86-97

**现状:**
`toBean()` 系列方法固定使用 `targetClass.newInstance()`，要求目标类有无参构造器。

**修复方案:**
新增 2 个重载方法，支持传入构造器和参数：

```java
// 新增：指定构造器 + 参数
public static <T> T toBean(Object source, Constructor<T> constructor, Object... args) {
    // target = constructor.newInstance(args);
    // BeanCopier.copy(source, target, null);
}

// 新增：指定构造器 + 参数 + CopyOptions
public static <T> T toBean(Object source, Constructor<T> constructor, CopyOptions options, Object... args) {
    // target = constructor.newInstance(args);
    // BeanCopier.copy(source, target, options);
}
```

**注意:** 现有 4 个 `toBean` 方法的无参构造器调用也需升级（见 3.10）。

**涉及:**
- BeanUtils.java: 新增 2 个 public 方法

**风险:** 低。新增方法不影响现有 API。

---

### 3.6 [Medium] deepCopy 丢失集合具体类型 — BeanUtils:540-551

**现状:**
```java
Collection<Object> result = new ArrayList<>(source.size());
// 应尝试保留 source 的具体类型 (LinkedList, TreeSet, etc.)
```

**修复方案:**
通过反射尝试实例化 source 的实际 Collection 类型：
```java
Collection<Object> result;
try {
    @SuppressWarnings("unchecked")
    Collection<Object> instance = source.getClass().getDeclaredConstructor().newInstance();
    result = instance;
} catch (Exception e) {
    // 降级到 ArrayList
    result = new ArrayList<>(source.size());
}
result.addAll(...);  // 使用 addAll 批量添加深拷贝元素
```

注意：`deepCopyMap()` 也存在类似问题（HashMap 写死），同样处理：
```java
// deepCopyMap 中:
Map<Object, Object> result;
try {
    @SuppressWarnings("unchecked")
    Map<Object, Object> instance = source.getClass().getDeclaredConstructor().newInstance();
    result = instance;
} catch (Exception e) {
    result = new HashMap<>(source.size());
}
```

**涉及:**
- BeanUtils.java:540-551 (deepCopyCollection)
- BeanUtils.java:557-568 (deepCopyMap)

**风险:** 低。反射失败时降级到默认类型（ArrayList/HashMap），与现有行为一致。

---

### 3.7 [Medium] toMap() 固定忽略 null 的文档说明 — BeanCopier:377-380

**现状:**
`toMap()` 始终跳过 null 值，无论 `CopyOptions.isIgnoreNull()` 设置。

**修复方案:**
在 `toMap()` 的 Javadoc 中明确说明此行为差异（仅修改注释）：

```java
/**
 * ...
 * <p>
 * 注意：与 {@link #copy(Object, Object, CopyOptions)} 不同，
 * toMap() 始终忽略 null 属性值，不受 CopyOptions.isIgnoreNull() 控制。
 */
```

**涉及:**
- BeanCopier.java:331-386 (toMap 方法 Javadoc)

**风险:** 极低。仅注释修改。

---

### 3.8 [Medium] BeanDesc getPropertyNames() 缓存 — 行94-99

**现状:**
`getPropertyNames()` 每次被调用都创建新的 `HashSet` 合并 pdMap.keySet() + fieldMap.keySet()。

**修复方案:**
在构造时计算一次并缓存到字段：

```java
// 新增字段
private final Set<String> allPropertyNames;

// 构造器中计算
this.allPropertyNames = Collections.unmodifiableSet(allNames);  // 或 new HashSet<>(...)
```

同时，`getFieldNames()` 同样每次都新建 HashSet，一并缓存：

```java
// 新增字段
private final Set<String> allFieldNames;
```

**注意:** 使用 `Collections.unmodifiableSet()` 包裹后返回，防止外部修改。

**涉及:**
- BeanDesc.java:30 (新增字段)
- BeanDesc.java:55-60 (构造器初始化缓存)
- BeanDesc.java:94-99 (getPropertyNames 改为返回缓存)
- BeanDesc.java:107-109 (getFieldNames 改为返回缓存)

**风险:** 低。BeanDesc 构造后属性映射不可变，缓存安全。

---

### 3.9 [Medium] resolveSourceFieldName() 圈复杂度过高 — BeanCopier:418-461

**现状:**
单一方法实现 4 级优先级匹配，圈复杂度约 8+。

**修复方案:**
按优先级拆分为 3 个私有方法：

```java
// 公共入口：仍为 resolveSourceFieldName
private static String resolveSourceFieldName(...) {
    // 1) 精确+同类型
    String result = resolveByExactMatch(sourceDesc, sourceFieldName, sourcePropType);
    if (result != null) return result;
    // 2) 忽略大小写+同类型
    result = resolveByIgnoreCaseMatch(sourceDesc, sourceFieldName, sourcePropType, sourceLowerNameMap);
    if (result != null) return result;
    // 3) 精确+不同类型
    if (propertyNames.contains(sourceFieldName)) return sourceFieldName;
    // 4) 忽略大小写+不同类型
    return resolveByIgnoreCaseOnly(sourceFieldName, sourceLowerNameMap);
}

private static String resolveByExactMatch(...) { ... }
private static String resolveByIgnoreCaseMatch(...) { ... }
private static String resolveByIgnoreCaseOnly(...) { ... }
```

**涉及:**
- BeanCopier.java:418-461 (原方法，拆分为 4 个小方法)

**风险:** 低。纯方法提取，不改变逻辑。

---

### 3.10 [Medium] Class.newInstance() Java 9 废弃 — BeanUtils 多处

**现状:**
5 处使用 `targetClass.newInstance()` / `clazz.newInstance()`：
- toBean(Object, Class) 行91
- toBean(Object, Class, CopyOptions) 行118
- toBean(ValueProvider, Class) 行146
- toBean(ValueProvider, Class, CopyOptions) 行173
- deepCopyBean 行582

**修复方案:**
统一替换为：
```java
targetClass.getDeclaredConstructor().newInstance()
```

`getDeclaredConstructor()` 可能抛 `NoSuchMethodException`，`newInstance()` 可能抛 `InvocationTargetException`。
需要在现有的 catch 分支中增加这两个异常类型。

**现有 catch:**
```java
} catch (InstantiationException | IllegalAccessException e) {
    throw new BaseException(e);
}
```

**修改后:**
```java
} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
    throw new BaseException(e);
}
```

`deepCopyBean` 行582 同理。

**涉及:**
- BeanUtils.java:91, 118, 146, 173, 582

**风险:** 低。Java 8+ 均支持 `getDeclaredConstructor()`，与 `newInstance()` 语义一致但避免了废弃警告。

---

## 4. 子任务分解

### 4.1 功能评分

| # | 功能 | 参数 | 逻辑 | 依赖 | 边界 | 产出 | 状态 | 异常 | 外部 | 总分 | 级别 |
|---|------|------|------|------|------|------|------|------|------|------|------|
| F1 | BeanDesc catch 异常日志 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 8 | 极简 |
| F2 | BeanDesc getPropertyNames 缓存 | 1 | 2 | 1 | 1 | 2 | 1 | 1 | 1 | 10 | 极简 |
| F3 | BeanCopier 公共逻辑提取 | 3 | 4 | 2 | 3 | 1 | 1 | 3 | 2 | 19 | 简单 |
| F4 | BeanCopier findConverters 预缓存 | 1 | 2 | 1 | 1 | 1 | 1 | 1 | 1 | 9 | 极简 |
| F5 | BeanCopier resolveSourceFieldName 拆分 | 2 | 3 | 1 | 1 | 1 | 1 | 1 | 1 | 11 | 极简 |
| F6 | BeanCopier toMap Javadoc | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 8 | 极简 |
| F7 | BeanUtils newInstance 替换 | 1 | 2 | 1 | 1 | 1 | 1 | 2 | 1 | 10 | 极简 |
| F8 | BeanUtils deepCopy 集合类型保留 | 1 | 3 | 1 | 2 | 1 | 1 | 2 | 1 | 12 | 极简 |
| F9 | BeanUtils 参数化构造器支持 | 2 | 3 | 1 | 2 | 1 | 1 | 2 | 1 | 13 | 极简 |
| F10 | StringConverters ConverterException | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 8 | 极简 |

### 4.2 分组（合并条件检查）

| 分组 | 功能 | 总分 | 级别 | 合并条件 |
|------|------|------|------|---------|
| Task 1-1 | F1(极简) + F2(极简) | 18 | 简单(3-4个) | ✅ 同文件BeanDesc.java，均处理属性描述缓存逻辑 |
| Task 2-1 | F3(简单) + F4(极简) | 28 | 中等(2-3个) | ✅ 同文件BeanCopier.java，均为core核心流程（提取公共 + 预缓存集成）|
| Task 2-2 | F5(极简) + F6(极简) | 19 | 简单(3-4个) | ✅ 同文件BeanCopier.java，均为方法级优化 |
| Task 3-1 | F7(极简) + F8(极简) | 22 | 简单(3-4个) | ✅ 同文件BeanUtils.java，均为修复现有代码 |
| Task 3-2 | F9(极简) | 13 | 极简(5-6个) | standalone（但独立为Task更清晰，与Task3-1不同逻辑类型） |
| Task 4-1 | F10(极简) | 8 | 极简(5-6个) | standalone，独立文件StringConverters.java |

### 4.3 DAG（所有 SubStory 之间无依赖）

```
SubStory-01 (BeanDesc)    ──▶ 独立
SubStory-02 (BeanCopier)  ──▶ 独立（Task 2-1 → Task 2-2 建议按序）
SubStory-03 (BeanUtils)   ──▶ 独立（Task 3-1 与 Task 3-2 无依赖）
SubStory-04 (StringConverters) ─▶ 独立
```

### 4.4 SubStory 分配

| SubStory | Task | 功能 | 涉及文件 | 等级 |
|----------|------|------|---------|------|
| 01-BeanDesc优化 | 1-1 | F1 + F2 | BeanDesc.java | 简单 |
| 02-BeanCopier重构 | 2-1 | F3 + F4 | BeanCopier.java | 中等 |
| 02-BeanCopier重构 | 2-2 | F5 + F6 | BeanCopier.java | 简单 |
| 03-BeanUtils修复 | 3-1 | F7 + F8 | BeanUtils.java | 简单 |
| 03-BeanUtils修复 | 3-2 | F9 | BeanUtils.java | 极简 |
| 04-类型转换异常 | 4-1 | F10 | StringConverters.java | 极简 |

## 5. 边界条件与异常处理

| 场景 | 处理策略 |
|------|---------|
| BeanDesc 日志不影响业务 | catch 块内仅加 log.debug，不改业务逻辑 |
| StringConverters 替换异常类型 | ConverterException extends BaseException，调用方 catch 不受影响 |
| BeanCopier 公共逻辑提取 | 保留 copy() 和 copyFromProvider() 的 public 签名不变，仅内部提取 |
| deepCopy 反射实例化失败 | 降级到 ArrayList/HashMap，行为与现有一致 |
| newInstance 替换后新增异常类型 | NoSuchMethodException + InvocationTargetException 加入 catch 分支，兜底 BaseException |
| 参数化构造器 args 为 null | 传入 null 时等同于无参构造器（Constructor.newInstance((Object[]) null)） |
| 多处文件同时修改 | SubStory-02 (BeanCopier.java) 的 2 个 Task 建议顺序执行避免冲突 |

## 6. 涉及文件汇总

| 文件 | 操作 | 涉及的 SubStory |
|------|------|----------------|
| `bean/copier/BeanDesc.java` | MODIFY | 01 |
| `bean/copier/BeanCopier.java` | MODIFY | 02 |
| `bean/BeanUtils.java` | MODIFY | 03 |
| `bean/converter/defaults/StringConverters.java` | MODIFY | 04 |

## 7. 总体自检

### ✅ 覆盖检验
story/ 审查报告中所有 10 个遗留问题均有设计方案。

### ✅ 目标对齐
所有修复方案以最小修改为原则，不引入新框架，不改动外部依赖。

### ✅ 可行检验
- 所有修复均为单文件修改，无跨包依赖
- 无新增第三方依赖
- 所有修改可增量测试（`mvn compile` + 定向测试）

### ✅ 完整性检验
所有 Task 涉及文件、查找指引、风险分析明确。

**确定性评估:** 98%（对 deepCopy 集合反射实例化的边界场景有 1 个假设——所有标准 Collection 实现均有无参构造器，非标准实现降级安全）
