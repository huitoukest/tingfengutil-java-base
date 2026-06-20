# BeanUtils 功能完善 — 方案设计

- 创建时间: 2026-06-01 11:00
- 最后更新: 2026-06-01 17:10
- 状态: 已完成

---

## 1. 整体目标

基于已确认的 Story 文档，完成三个需求方向的设计：

| 需求 | 范围 | 实现计划 |
|------|------|---------|
| **需求1** 自定义 Converter 临时传参 | CopyOptions 新增 customConverters 字段 +  BeanCopier 集成查找逻辑 | 已完成 |
| **需求2** 注释规范化 | BeanCopier, CopyOptions, BeanDesc, ValueProvider, MapValueProvider Javadoc 补充 | 已完成 |
| **需求3** V5 优化建议分级清单 | Must/Should/Could 三级建议整理 | 已完成，用户审核后确认实现 Must 级别 |
| **需求4** Must缺陷修复 — 嵌套转换 | 嵌套 Map → Bean 递归转换 + 嵌套 List 元素类型转换 | 本次实现 |
| **需求5** Must缺陷修复 — 错误信息增强 | 类型转换失败时附加源/目标属性名和值信息 | 本次实现 |
| **需求6** Must缺陷修复 — 废弃 API 清理 | 标记旧可变参数风格方法为 @Deprecated，提供替代方案 | 本次实现 |

---

## 2. 需求1：自定义 Converter 临时传参

### 2.1 设计决策回顾（Story 已确认）

| 决策项 | 选择 | 说明 |
|--------|------|------|
| 优先级规则 | **临时优先** | 先找临时 Converter，找不到再 fallback 到全局 |
| Converter 类型 | **泛型 `Converter<?, ?>`** | 可注册任意类型对，调用时按需查找 |
| 生命周期 | **CopyOptions 生命周期** | 在 CopyOptions 有效期内有效，可多次调用 |
| 与全局 Converter 关系 | **额外补充模式** | 临时 Converter 找不到时，仍会 fallback 到全局 Converter |

### 2.2 修改清单

#### 2.2.1 CopyOptions.java

**新增字段：**

```java
/** 自定义转换器列表（临时，优先级高于全局） */
private List<Converter<?, ?>> customConverters;
```

**新增方法：**

| 方法 | 签名 | 说明 |
|------|------|------|
| `setCustomConverters` | `CopyOptions setCustomConverters(Converter<?, ?>... converters)` | 设置临时转换器，支持可变参数传入多个。如果 converters 为 null 或空，清空当前列表。返回 this 支持链式调用。 |
| `getCustomConverters` | `List<Converter<?, ?>> getCustomConverters()` | 获取临时转换器列表的不可变视图。返回空集合时不会为 null。 |

**新增 import：**
- `java.util.List`
- `java.util.Arrays`
- `java.util.Collections`（已有）
- `com.tingfeng.util.java.base.bean.converter.Converter`

**影响范围：**
- `equals()` / `hashCode()` 需要更新以包含 `customConverters` 字段

#### 2.2.2 BeanCopier.java

**修改位置：** `copyProperties()` 私有方法，类型转换阶段（现有第 202-231 行）。

**新逻辑流程（伪代码）：**

```
类型转换阶段（当前值 value 非 null，类型不匹配，且 useConverter=true）:

  Step 1 — 尝试临时转换器
    IF options.getCustomConverters() 非空
      FOR EACH converter IN options.getCustomConverters()
        IF converter.getSourceType().isAssignableFrom(value.getClass())      // 源类型匹配（含子类）
          AND converter.getTargetType().equals(targetPropertyType)            // 目标类型精确匹配
        THEN
          IF converter instanceof ConditionConverter
            AND ((ConditionConverter) converter).matches(value) == false
          THEN
            CONTINUE  // 条件不满足，尝试下一个
          END IF
          convertedValue = converter.convert(value)
          value = convertedValue
          MARK as converted
          BREAK
        END IF
      END FOR
    END IF

  Step 2 — 临时转换器未命中，fallback 到全局
    IF NOT converted
      执行原有全局 Converter 查找逻辑（现有第 206-231 行保持不变）
    END IF
```

**关键设计点：**

| 设计点 | 方案 | 理由 |
|--------|------|------|
| 源类型匹配 | `sourceType.isAssignableFrom(value.getClass())` | 支持子类多态，与 DefaultConverterRegistry 的 `findConverterInSourceHierarchy` 逻辑一致 |
| 目标类型匹配 | `targetType.equals(targetPropertyType)` | 精确匹配，不支持冒泡。原因：临时 Converter 是用户明确指定的类型对，不应自动冒泡 |
| ConditionConverter | 调用 `matches(value)` 过滤 | 与全局 Converter 行为一致 |
| 转换后赋值 | 直接覆盖 value 变量 | 与现有全局 Converter 的处理方式一致 |
| 异常处理 | 不捕获临时 Converter 的异常 | 临时 Converter 是用户明确指定的，转换失败应直接抛异常，让用户感知 |

### 2.3 API 设计

#### CopyOptions 扩展

```java
// ——— setter ———
/**
 * 设置临时转换器（仅在本次 CopyOptions 生命周期内有效，优先级高于全局）。
 * <p>
 * 临时转换器查找规则：按传入顺序遍历，找到第一个源类型可赋值、目标类型精确匹配的转换器。
 * ConditionConverter 额外检查 matches() 条件。
 * <p>
 * 所有临时 Converter 找不到匹配时，自动 fallback 到全局 ConverterRegistry。
 *
 * @param converters 临时转换器，支持传入多个。传入 null 或空数组时清空临时转换器列表
 * @return this（链式调用）
 */
public CopyOptions setCustomConverters(Converter<?, ?>... converters)

// ——— getter ———
/**
 * 获取临时转换器列表（不可变视图）。
 * <p>
 * 返回的列表按传入顺序排列，遍历时优先匹配前面的转换器。
 *
 * @return 临时转换器列表，永不为 null（无临时转换器时返回空列表）
 */
public List<Converter<?, ?>> getCustomConverters()
```

#### BeanCopier 类型转换逻辑

核心转换逻辑无新增 public API，修改 `copyProperties()` 内部实现。添加一个辅助方法：

```java
/**
 * 在临时转换器列表中查找匹配的转换器。
 * <p>
 * 查找规则：
 * <ol>
 *   <li>源类型匹配：converter.getSourceType().isAssignableFrom(value.getClass())</li>
 *   <li>目标类型匹配：converter.getTargetType().equals(targetPropertyType)</li>
 *   <li>ConditionConverter 额外检查 matches(value)</li>
 * </ol>
 * 返回第一个同时满足所有条件的转换器。
 *
 * @param customConverters 临时转换器列表
 * @param value            当前属性值
 * @param targetPropertyType 目标属性类型
 * @return 匹配的 Converter，或 null
 */
private static Converter<?, ?> findCustomConverter(
    List<Converter<?, ?>> customConverters, Object value, Class<?> targetPropertyType)
```

### 2.4 边界条件与异常处理

| 场景 | 行为 |
|------|------|
| `setCustomConverters(null)` | 清空临时转换器列表 |
| `setCustomConverters()`（空 varargs） | 清空临时转换器列表 |
| 临时 Converter 的 getSourceType() 返回 null | 跳过该 Converter（不抛异常） |
| 临时 Converter 的 getTargetType() 返回 null | 跳过该 Converter（不抛异常） |
| 临时 Converter 的 convert() 抛异常 | 不捕获，直接向上传播（用户明确指定的 Converter 应预知行为） |
| 临时 ConditionConverter.matches() 抛异常 | 不捕获，直接向上传播 |
| 临时 Converter 未命中 + 全局 Converter 也找不到 | 受现有 `ignoreNoMatchConverterError` 控制 |
| equals/hashCode 更新 | customConverters 需参与比较（但 List<Converter> 引用比较即可） |

### 2.5 验收标准

| 验收项 | 标准 |
|--------|------|
| 临时 Converter 优先级 | 匹配成功时，一定使用临时 Converter 而非全局 |
| Fallback 行为 | 临时 Converter 找不到时，fallback 到全局 |
| 生命周期 | 临时 Converter 仅在设置它的 CopyOptions 内有效 |
| 条件转换器支持 | ConditionConverter 的 matches() 正确生效 |
| 不影响全局 | 使用临时 Converter 后，全局 Converter 状态不变 |
| equals/hashCode | customConverters 更新后 equals/hashCode 一致性 |

---

## 3. 需求2：注释规范化

### 3.1 规范要求

根据项目偏好（`4-preferences.md`）和编码规范：
- 所有 public 方法必须有 Javadoc
- `@param` 描述参数含义
- `@return` 描述返回值（特殊行为如返回 null、空集合等情况说明）
- `@throws` 描述可能抛出的异常及触发条件
- 注释中禁止使用 HTML 标签 `<>`，使用 Markdown 语法

### 3.2 逐文件分析

#### 3.2.1 BeanCopier.java

当前 `getOrCreateBeanDesc()` 已有 Javadoc（第 288-303 行），但可以进一步补充 `@param` 的 null 约束说明。

| 方法 | 当前状态 | 需要补充 |
|------|---------|---------|
| `copy()` | 已有完整 Javadoc | 无变更 |
| `copyFromProvider()` | 已有完整 Javadoc | 无变更 |
| `toMap()` | 已有完整 Javadoc | 无变更 |
| `getOrCreateBeanDesc()` | 已有 Javadoc，但 `@param` 缺少 null 约束 | 补充 `@param beanClass 不能为null` |
| `getFieldMapKeySet()` | private | 无需 |
| `getTargetPropertyType()` | private | 无需 |
| `resolveSourceFieldName()` | private | 无需 |
| `buildLowerCaseNameMap()` | private | 无需 |

**实际变更量：** 仅 `getOrCreateBeanDesc()` 补充 `@param` 说明，说明 beanClass 为 null 时的行为（会抛 NullPointerException）。

#### 3.2.2 CopyOptions.java

所有 setter 已有 `@param`，所有 getter 已有 `@return`。当前状态已接近完整。

| 方法 | 当前状态 | 需要补充 |
|------|---------|---------|
| `create()` | 已有 `@return` | 无变更 |
| 全部 setter | 已有 `@param` + `@return this` | 无变更 |
| 全部 getter | 已有 `@return` | 无变更 |
| `getFieldMapping()` | 已有 `@return`（说明可能返回 null） | 无变更 |
| `getIgnoreProperties()` | 已有 `@return`（说明永不为 null） | 无变更 |
| `equals()` / `hashCode()` | Override 方法 | 无需补充（标准方法） |

**实际变更量：** 极少。关注 `getFieldMapping()` 的 null 返回说明是否清晰，`getIgnoreProperties()` 的"永不为 null" 说明是否准确。

#### 3.2.3 BeanDesc.java

当前缺失 Javadoc 的方法：

| 方法 | 当前 Javadoc | 需要补充 |
|------|-------------|---------|
| `getPropertyNameIgnoreCase(String name)` | 已有（第 184-208 行） | 但需检查 `<p>` 标签和 `<code>` 标签是否符合规范 |
| `getField(String propName)` | 简单 Javadoc（第 272-278 行） | `@param` + `@return` 已基本完整，检查标签规范 |
| `hasPropertyDescriptor(String name)` | 已有完整 Javadoc | 无变更 |
| `getPropertyType(String propName)` | 已有完整 Javadoc | 无变更 |
| `getPropertyValue(Object bean, String name)` | 已有完整 Javadoc | 无变更 |
| `setPropertyValue(Object bean, String name, Object value)` | 已有完整 Javadoc | 无变更 |
| `getInstance(Class<?> clazz)` | 已有 Javadoc | 无变更 |

**实际变更量：** 主要检查已存在 Javadoc 是否包含 HTML 标签（`<>`），若有则替换为 Markdown。

#### 3.2.4 ValueProvider.java

接口方法 Javadoc：

| 方法 | 当前 Javadoc | 需要补充 |
|------|-------------|---------|
| `value(String key, Class<?> type)` | 已有（第 14-20 行） | `@param` 说明 type 当前用途（暂不用于过滤），`@return` 说明不存在时返回 null |
| `containsKey(String key)` | 已有（第 22-28 行） | 基本完整 |

**实际变更量：** 极少，对 `value()` 的 `type` 参数补充说明。

#### 3.2.5 MapValueProvider.java

| 元素 | 当前 Javadoc | 需要补充 |
|------|-------------|---------|
| 类级 | 已有（第 5-10 行），说明暂不支持 a.b.c 嵌套路径 | 无变更 |
| 构造器 | 无 | 补充 `@param map 用于提供属性值的源Map，不能为null` |
| `value()` | Override，继承接口 Javadoc | 无变更 |
| `containsKey()` | Override，继承接口 Javadoc | 无变更 |

**实际变更量：** 补充构造器的 Javadoc。

### 3.3 验收标准

| 文件 | 验收标准 |
|------|---------|
| BeanCopier.java | 所有 public 方法有完整 Javadoc，`@param` 包含 null 约束，无 HTML 标签 |
| CopyOptions.java | 所有 setter 有 `@param`，所有 getter 有 `@return`，无 HTML 标签 |
| BeanDesc.java | 所有 public 方法有 Javadoc，无 HTML 标签 |
| ValueProvider.java | 接口方法有 Javadoc，说明方法契约 |
| MapValueProvider.java | 构造器有 Javadoc，类级 Javadoc 完整 |

---

## 4. 需求3：V5 优化建议分级清单

### 4.1 背景

以资深 BeanUtils 使用者角度，整理 V5 版本的优化建议，供用户审核决定是否实现。

### 4.2 分级清单（用户审核确认）

> **用户审核决策（2026-06-01）：**
> - ✅ **实现** Must 级别所有 4 项（P0 + P1）
> - ❌ **暂不实现** 属性路径拷贝支持（Should/P2）
> - ❌ **暂不实现** ASM 字节码优化（Could/P4）
> - ❌ **暂不实现** MethodHandle 替代 Method（Could/P4）
> - ⏳ **待定** 其余 Should / Could 项后续评估

#### Must（已确认实现）

| 方向 | 优化项 | 说明 | 优先级 |
|------|--------|------|--------|
| 功能增强 | 嵌套 Map → Bean 递归转换 | 当前 `toBean(map, Target.class)` 时，Map 的 value 如果也是 Map，不会递归转换为嵌套 Bean | P0 |
| 功能增强 | 嵌套 List 元素类型转换 | 当前 `toBean(map, Target.class)` 时，如果 map 中某个 key 对应的 value 是 `List<Map>`，不会递归转换为 `List<TargetElement>` | P0 |
| 易用性改进 | 错误信息增强 | 当前类型转换失败时，错误信息不包含源属性名和目标属性名 | P0 |
| 兼容性 | 废弃 API 清理 | 部分旧方法使用可变参数风格，应标记 `@Deprecated` 并提供替代方案 | P1 |

#### Should（已确认暂不实现）

| 方向 | 优化项 | 说明 | 优先级 |
|------|--------|------|--------|
| 性能优化 | 反射缓存优化 | 当前 `BeanDesc` 使用 `SimpleCacheHelper` 缓存，但没有大小淘汰机制 | P1 |
| 功能增强 | 字段级 Converter | 为特定字段指定专属 Converter | P2 |
| 易用性改进 | 链式 API 增强 | 提供流式 API：`BeanUtils.from(source).to(TargetClass.class).withOptions(opts).execute()` | P3 |
| 易用性改进 | 类型推断增强 | 减少泛型显式声明 | P3 |

#### Could（已确认暂不实现）

| 方向 | 优化项 | 说明 | 优先级 |
|------|--------|------|--------|
| 功能增强 | 转换回调钩子 | 支持在属性拷贝前后插入自定义逻辑 | P4 |
| 功能增强 | 自定义跳过策略 | 支持 `PropertySkipStrategy` | P4 |
| 兼容性 | 迁移工具 | 提供从 V4 到 V5 的迁移指南 | P4 |

---

## 5. 实体关系图

### 5.1 自定义 Converter 相关实体关系

```
CopyOptions
  ├── customConverters: List<Converter<?, ?>>
  │     ├── Converter<S, T>           ← 普通转换器接口
  │     │     ├── getSourceType()
  │     │     ├── getTargetType()
  │     │     └── convert(S source)
  │     └── ConditionConverter<S, T>  ← 条件转换器（extends Converter）
  │           └── matches(S source)
  │
  └── (其他现有配置字段: ignoreNull, fieldMapping, useConverter...)

BeanCopier.copyProperties()
  ├── 读取 options.getCustomConverters()
  ├── 遍历匹配 → 使用临时 Converter
  └── 未匹配 → fallback → ConverterRegistry.getInstance()
```

### 5.2 转换查找优先级

```
查找临时 Converter（customConverters）
  ├── 源类型可赋值匹配（isAssignableFrom）
  ├── 目标类型精确匹配（equals）
  ├── ConditionConverter 额外 matches() 检查
  └── 找到 → 使用临时 Converter

  ↓ 未找到

查找全局 Converter（ConverterRegistry）
  ├── findConverters(value.getClass(), targetPropertyType)
  ├── ConditionConverter / 普通 Converter 分类查找
  └── 找到 → 使用全局 Converter
  
  ↓ 未找到

受 ignoreNoMatchConverterError 控制
  ├── true  → 跳过该属性（log.debug）
  └── false → 抛异常
```

---

## 6. 子任务分解

### 6.1 功能评分与分组

#### SubStory 02：临时Converter实现

| 功能 | 参数 | 逻辑 | 依赖 | 边界 | 产出 | 状态 | 异常 | 外部 | 总分 | 分级 |
|------|------|------|------|------|------|------|------|------|------|------|
| F1: CopyOptions customConverters 字段+setter+getter | 2 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | **9** | 极简 |
| F2: BeanCopier 临时Converter查找集成 | 1 | 3 | 2 | 2 | 1 | 1 | 2 | 1 | **13** | 极简 |

**分组：F1 + F2 → Task 1**

合并检查：
- 条件A: 同属一个业务流程（临时Converter实现）✓
- 条件B: 引用同一 Converter<?,?> 类型 ✓
- 条件C: 总文件数=2 ≤6 ✓；估算行数≈90 ≤500 ✓

#### SubStory 03：注释规范化

| 功能 | 参数 | 逻辑 | 依赖 | 边界 | 产出 | 状态 | 异常 | 外部 | 总分 | 分级 |
|------|------|------|------|------|------|------|------|------|------|------|
| F3: BeanCopier.getOrCreateBeanDesc() Javadoc | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | **8** | 极简 |
| F4: CopyOptions setter/getter Javadoc | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | **8** | 极简 |
| F5: BeanDesc.getField() Javadoc | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | **8** | 极简 |
| F6: ValueProvider 接口 Javadoc | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | **8** | 极简 |
| F7: MapValueProvider 构造器 Javadoc | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | **8** | 极简 |

**分组：**
- F3 + F4 → Task 2（BeanCopier + CopyOptions，同为拷贝操作配置相关）
- 条件A: 同属一个流程（注释规范化）✓
- 条件B: 引用相关文件 ✓
- 条件C: 文件数=2 ≤6 ✓；估算行数≈20 ≤500 ✓

- F5 + F6 + F7 → Task 3（BeanDesc + ValueProvider + MapValueProvider，同为 Bean 描述/值提供相关）
- 条件A: 同属一个流程（注释规范化）✓
- 条件B: 引用相关文件 ✓
- 条件C: 文件数=3 ≤6 ✓；估算行数≈30 ≤500 ✓

#### SubStory 04：V5优化建议整理

单个功能 → Task 4（类型：analysis，独立不可合并）

### 6.2 功能清单

#### Task 1：CopyOptions customConverters 字段 + BeanCopier 集成

| 功能 | 涉及文件 | 依赖 | 查找指引 |
|------|---------|------|---------|
| CopyOptions 新增 customConverters 字段、setter、getter | `CopyOptions.java` | 无 | 参考现有 setIgnoreProperties/getIgnoreProperties 模式 |
| CopyOptions equals/hashCode 更新 | `CopyOptions.java` | 同上 | 参考现有 equals/hashCode 模式 |
| BeanCopier 临时 Converter 查找逻辑 | `BeanCopier.java` | CopyOptions 扩展完成 | 参考 `copyProperties()` 现有类型转换逻辑（第202-231行） |
| 新增辅助方法 `findCustomConverter()` | `BeanCopier.java` | CopyOptions 扩展完成 | 参考 `DefaultConverterRegistry.getConverterByValue()` 的匹配逻辑 |

#### Task 2：BeanCopier + CopyOptions Javadoc 补充

| 功能 | 涉及文件 | 依赖 | 查找指引 |
|------|---------|------|---------|
| BeanCopier.getOrCreateBeanDesc() Javadoc 补充 | `BeanCopier.java` | 无 | 查看方法现有 Javadoc（第294行），补充 @param null 约束 |
| CopyOptions existing Javadoc 检查 | `CopyOptions.java` | 无 | 逐方法检查 @param/@return 完整性，移除 HTML 标签 |

#### Task 3：BeanDesc + ValueProvider + MapValueProvider Javadoc 补充

| 功能 | 涉及文件 | 依赖 | 查找指引 |
|------|---------|------|---------|
| BeanDesc.getField() Javadoc | `BeanDesc.java` | 无 | 查看方法现有 Javadoc（第276行） |
| ValueProvider 接口方法 Javadoc | `ValueProvider.java` | 无 | 检查 value() 和 containsKey() 的 @param/@return |
| MapValueProvider 构造器 Javadoc | `MapValueProvider.java` | 无 | 补充 @param map 说明 |

#### Task 4：V5 优化建议整理（analysis）

| 功能 | 涉及文件 | 依赖 | 查找指引 |
|------|---------|------|---------|
| Must 级别优化建议整理 | 无（纯文档） | 无 | Story 文档 §3.2 已有完整清单 |
| Should 级别优化建议整理 | 无（纯文档） | 无 | Story 文档 §3.2 |
| Could 级别优化建议整理 | 无（纯文档） | 无 | Story 文档 §3.2 |

### 6.3 依赖拓扑

```
Task 1 (CopyOptions + BeanCopier 实现) ── 独立无依赖
Task 2 (BeanCopier + CopyOptions Javadoc) ── 独立无依赖（可与 Task 1 并行）
Task 3 (BeanDesc + ValueProvider + MapValueProvider Javadoc) ── 独立无依赖
Task 4 (V5优化建议整理) ── 独立无依赖
```

所有 Task 可并行执行。

---

## 7. 确认门控

### 7.1 覆盖检验

- [x] **需求1（自定义Converter）** → SubStory 02 → Task 1
- [x] **需求2（注释规范化）** → SubStory 03 → Task 2 + Task 3
- [x] **需求3（V5优化建议）** → SubStory 04 → Task 4（analysis 类型）

### 7.2 目标对齐

- [x] 所有 Task 与 Story 文档中 SubStory 分解一致
- [x] 临时 Converter 设计完全遵循 Story 已确认的决策（临时优先、泛型、CopyOptions 生命周期、额外补充模式）
- [x] V5 建议不实现，仅整理到设计文档供用户审核

### 7.3 可行检验

- [x] CopyOptions 扩展不涉及新增第三方依赖
- [x] BeanCopier 修改仅在现有转换阶段增加前置判断逻辑，不影响现有流程
- [x] 辅助方法 `findCustomConverter()` 逻辑清晰，复用 Converter/ConditionConverter 现有接口
- [x] 注释规范化不改变任何运行时行为

### 7.4 完整性检验

- [x] 每个 Task 涉及文件明确
- [x] 每个 Task 查找指引明确（具体到行号/方法名）
- [x] 依赖关系清晰（无循环依赖，可并行）
- [x] 验收标准可量化

**置信度**：99%（所有边界已确认，设计方案与 Story 完全对齐，无未解决问题）

---

## 14. 新增 SubStory 确认门控

### 14.1 覆盖检验

- [x] **Must缺陷1（嵌套Map→Bean）** → SubStory 05 → Task 5（嵌套转换支持）
- [x] **Must缺陷2（嵌套List元素类型转换）** → SubStory 05 → Task 5（同上）
- [x] **Must缺陷3（错误信息增强）** → SubStory 06 → Task 6
- [x] **Must缺陷4（废弃API清理）** → SubStory 07 → Task 7
- [x] 用户排除项（属性路径拷贝、ASM、MethodHandle）已从清单中移除，且不在实现计划内

### 14.2 目标对齐

- [x] 所有 Task 与 Story 文档中 Must 级别优化建议一一对应
- [x] 嵌套转换优先级（临时→嵌套→全局）与已有设计一致
- [x] 错误信息增强不改变运行时行为，只改进异常信息质量
- [x] 废弃 API 标记不删除任何功能，保持完全向后兼容

### 14.3 可行检验

- [x] 嵌套转换不新增第三方依赖，仅依赖 BeanUtils 现有反射能力
- [x] List 元素类型解析通过现有 BeanDesc.getField() + Field.getGenericType() 实现
- [x] 错误信息增强只在现有异常处理位置修改格式字符串
- [x] 废弃 API @Deprecated 是编译期注解，不影响运行时

### 14.4 完整性检验

- [x] Task 5 涉及文件明确（BeanCopier.java + BeanCopierTest.java）
- [x] Task 6 涉及文件明确（BeanCopier.java）
- [x] Task 7 涉及文件明确（BeanUtils.java + BeanCopier.java）
- [x] 每个 Task 查找指引明确到具体行号
- [x] Task 间无循环依赖（全部并行）
- [x] 验收标准可量化

**置信度**：99%（嵌套转换边界已明确，List 泛型解析方案已确认，废弃清单已确定）

---

## 8. 核心边界条件与异常处理策略

| 场景 | 策略 | 类型 |
|------|------|------|
| CopyOptions.setCustomConverters(null) | 清空列表 | 正常行为 |
| 临时 Converter.getSourceType() 为 null | 跳过该 Converter | 防御性 |
| 临时 Converter.getTargetType() 为 null | 跳过该 Converter | 防御性 |
| 临时 Converter 的 convert() 抛异常 | 不捕获，向上传播 | 异常 |
| ConditionConverter.matches() 抛异常 | 不捕获，向上传播 | 异常 |
| 临时 + 全局都找不到匹配 Converter | 按现有 ignoreNoMatchConverterError 处理 | 正常行为 |
| 临时 Converter 与全局注册的 Converter 类型相同 | 临时优先 | 正常行为 |
| equals/hashCode 与 customConverters 一致性 | 更新 equals/hashCode | 正确性 |

---

## 9. 需求4：嵌套转换支持（Must P0 缺陷修复）

### 9.1 问题分析

当前 `toBean(map, Target.class)` 时有两个问题：

**问题1：嵌套 Map → 不会递归转换为嵌套 Bean**
```java
// 现有行为：innerMap 保持为 Map，不会转换为 Address
Map<String, Object> map = new HashMap<>();
map.put("name", "张三");
map.put("address", new HashMap<String, Object>() {{ put("city", "北京"); put("street", "长安街"); }});
User user = BeanUtils.toBean(map, User.class);
// user.address 类型为 HashMap，不是 Address
```

**问题2：嵌套 List<Map> → 不会递归转换为 List<Bean>**
```java
// 现有行为：每个 Map 元素保持为 Map，不会转换为 Order
Map<String, Object> map = new HashMap<>();
map.put("orders", Arrays.asList(
    new HashMap<String, Object>() {{ put("id", 1L); put("amount", 100.0); }},
    new HashMap<String, Object>() {{ put("id", 2L); put("amount", 200.0); }}
));
User user = BeanUtils.toBean(map, User.class);
// user.orders 类型为 List<HashMap>，不是 List<Order>
```

### 9.2 修改方案概览

| 修改点 | 位置 | 说明 |
|--------|------|------|
| 嵌套 Map→Bean 转换 | `BeanCopier.copyProperties()` 类型转换阶段 | 检测 value 是 Map 且目标不是 Map→递归 toBean |
| 嵌套 List→List<Bean> 转换 | `BeanCopier.copyProperties()` 类型转换阶段 | 检测 value 是 List<Map>→递归转换元素 |
| List 元素类型解析 | `BeanCopier` 新增辅助方法 | 通过 Field.getGenericType() 提取 List<E> 中的 E |
| 递归深度控制 | `BeanCopier` 新增常量 | 默认 MAX_NESTED_DEPTH = 10，防止栈溢出 |

### 9.3 修改清单

#### 9.3.1 BeanCopier.java — 类型转换逻辑扩展

**修改位置：** `copyProperties()` 类型转换阶段（当前第 196-237 行）。

**新逻辑流程**（在临时 Converter 查找之后、全局 Converter 查找之前插入嵌套转换）：

```
类型转换阶段流程图：

value 不为 null，类型不匹配，且 useConverter=true

  Step 1 — 临时 Converter 查找（已有逻辑，保持不变）
    IF options.getCustomConverters() 非空 → 遍历匹配

  Step 2 — 嵌套递归转换（新增）
    IF 临时转换器未命中 AND 嵌套递归未关闭
      IF value 是 Map 且 targetPropertyType 不是 Map/Collection/基本类型/String/枚举
        → 递归调用 BeanUtils.toBean((Map)value, targetPropertyType, options)
        → 限制递归深度 ≤ MAX_NESTED_DEPTH（默认 10）
        → 结果标记为已转换

      ELSE IF value 是 List 且 targetPropertyType 是 List/Collection
        → 解析 targetPropertyType 的元素类型（通过 Field 泛型参数）
        → 解析成功且元素不是基本类型/Map/不可变类型
        → 对 List 中每个 Map 元素递归 toBean
        → 结果标记为已转换

  Step 3 — 全局 Converter 查找（已有逻辑，保持不变）
    IF 未转换 → 执行 ConverterRegistry.getInstance() 查找
```

**新增辅助方法：**

```java
/**
 * 尝试执行嵌套转换（Map→Bean, List<Map>→List<Bean>）。
 * <p>
 * 返回转换后的值，若无需嵌套转换则返回原始 value。
 * 内部通过 depth 参数控制递归深度，超过 MAX_NESTED_DEPTH 时直接返回原始值。
 *
 * @param value              当前属性值
 * @param targetPropertyType 目标属性类型
 * @param propName           属性名（用于获取 Field 泛型类型）
 * @param targetDesc         目标 BeanDesc
 * @param options            拷贝选项（传递到递归调用）
 * @param depth              当前递归深度（外部首次传入 1）
 * @return 转换后的值，或原始 value（无需转换时）
 */
private static Object tryNestedConversion(
    Object value, Class<?> targetPropertyType,
    String propName, BeanDesc targetDesc,
    CopyOptions options, int depth)
```

```java
/**
 * 判断目标类型是否为「简单类型」—— 不需要嵌套递归的。
 * <p>
 * 包括：基本类型包装类、String、BigDecimal/BigInteger、枚举、Map、Collection、
 * Date、URI、URL、UUID、Class 等。
 *
 * @param type 目标属性类型
 * @return true 表示是简单类型，不需要嵌套转换
 */
private static boolean isSimpleType(Class<?> type)
```

```java
/**
 * 解析 List 类型属性的元素类型（通过 Field 的泛型参数）。
 * <p>
 * 例如 List<User> → User.class；List<String> → String.class。
 * 支持多层嵌套泛型（如 List<List<User>> → List.class 作为元素类型）。
 *
 * @param targetDesc 目标 BeanDesc
 * @param propName   属性名
 * @return 元素类型，或 null（无法解析时）
 */
private static Class<?> resolveListElementType(BeanDesc targetDesc, String propName)
```

**所需新增 import：**
- `java.lang.reflect.ParameterizedType`
- `java.lang.reflect.Type`
- `java.util.Collection`
- `java.util.ArrayList`
- `com.tingfeng.util.java.base.bean.BeanUtils`（可能已有）

### 9.4 关键设计决策

| 决策点 | 方案 | 理由 |
|--------|------|------|
| 嵌套转换在转换链中的位置 | 临时 Converter → 嵌套转换 → 全局 Converter | 用户显式指定的转换器优先；嵌套转换是自动行为，介于显式指定和全局注册之间 |
| 嵌套深度限制 | 10 层 | 防止因循环嵌套导致的栈溢出。BeanUtils.deepCopy 已有类似模式 |
| 深度超限处理 | 返回原始 value（不转换），后续走正常类型转换 | 降级行为，不会抛异常 |
| List 元素类型解析 | 通过 Field.getGenericType() 走泛型参数 | 兼容现有 BeanDesc.getField() 接口 |
| 泛型类型解析失败 | 跳过嵌套转换，返回原始 List | 降级行为，用户感知不到 |
| CopyOptions 传递 | 递归调用时传递同一 options 实例 | 确保 customConverters 等在递归中生效 |
| 循环引用检测 | 不专门检测（Map→Bean 不会产生 Java 对象循环引用） | Map 是值对象，递归只沿 Map→Bean→Map 链向下 |

### 9.5 单元测试设计

| 测试场景 | 输入 | 验证点 |
|----------|------|--------|
| 简单 Map→Bean 嵌套 | Map<String, Map<String, Object>> | 内层 Map 正确转换为嵌套 Bean |
| 多层嵌套 | 3 层 Map 嵌套 | 每层递归正确转换 |
| List<Map>→List<Bean> | Map 中含 List<Map> 属性 | List 中元素正确转换 |
| 混合嵌套 | Map 中既有 Map 又有 List<Map> | 两种转换同时生效 |
| 深度超限 | 超过 10 层的嵌套 | 超限层返回原始值 |
| 简单类型不转换 | Map<String, String> 的目标 | 不触发嵌套转换 |
| 与临时 Converter 共存 | 同属性既有临时 Converter 又触发嵌套 | 临时 Converter 优先 |
| 与全局 Converter 共存 | 嵌套转换未触发，fallback 到全局 | 链式查找正确 |
| 空 Map/空 List | Map 值为空 Map / List 为空 | 不抛异常 |

---

## 10. 需求5：错误信息增强（Must P0 缺陷修复）

### 10.1 问题分析

当前类型转换失败时的异常信息只包含属性名，缺少源类型、目标类型和实际值信息：

```java
// 当前：Property conversion failed: age
// 期望：Property 'age' conversion failed: String → Integer, value='25abc'
```

这会使得在批量转换或复杂对象转换时，定位问题属性需要额外调试。

### 10.2 修改方案

**修改位置：** `BeanCopier.copyProperties()` 第 218-233 行（两个 throw BaseException 位置 + debug 日志）

**当前代码片段：**
```java
// 场景A：有Converter但转换失败
throw new BaseException("Property conversion failed: " + propName, e);
// 场景B：无Converter类型不匹配
throw new BaseException("Property conversion failed: " + propName, e);
// debug日志
log.debug("Property conversion failed: " + propName + ", skipping", e);
```

**修改后格式：**
```
Property '<propName>' conversion failed: <sourceType> → <targetType>, value='<value>'
```

### 10.3 修改清单

**BeanCopier.java 修改点：**

| 位置 | 当前代码 | 修改为 |
|------|---------|--------|
| 第 221-223 行 | `throw new BaseException("Property conversion failed: " + propName, e);` | `throw new BaseException(buildConversionErrorMessage(propName, value, targetPropertyType), e);` |
| 第 226-228 行 | `throw new BaseException("Property conversion failed: " + propName, e);` | `throw new BaseException(buildConversionErrorMessage(propName, value, targetPropertyType), e);` |
| 第 231-234 行 | `log.debug("Property conversion failed: " + propName + ", skipping", e);` | `log.debug(buildConversionErrorMessage(propName, value, targetPropertyType) + ", skipping", e);` |

**新增辅助方法：**

```java
/**
 * 构建类型转换异常信息，格式：Property '<propName>' conversion failed: <sourceType> → <targetType>, value='<value>'
 * <p>
 * value 的 toString() 超过 200 字符时会截断并追加 "...({length})"。
 *
 * @param propName           属性名
 * @param value              源值
 * @param targetPropertyType 目标属性类型
 * @return 格式化的异常信息字符串
 */
private static String buildConversionErrorMessage(
    String propName, Object value, Class<?> targetPropertyType)
```

### 10.4 边界条件

| 场景 | 行为 |
|------|------|
| value 为 null | 显示 null |
| value.toString() 超长（>200 字符） | 截断并追加 "...(length)" |
| targetPropertyType 为 null | 显示 "Unknown" |
| value 为数组或集合 | 显示数组/集合类型名 + size |

---

## 11. 需求6：废弃 API 清理（Must P1 缺陷修复）

### 11.1 问题分析

部分 API 使用可变参数风格，不够类型安全。应标记 `@Deprecated` 并推荐用户使用更安全的替代方案。

### 11.2 废弃清单

| 方法 | 废弃原因 | 替代方案 |
|------|---------|---------|
| `BeanUtils.toMap(Object bean, String... ignoreProperties)` | 可变参数不够类型安全 | 使用 `BeanUtils.toMap(bean)` + `CopyOptions.setIgnoreProperties(Collection)` |
| `BeanCopier.toMap(Object bean, CopyOptions options, String... ignoreFields)` | 可变参数不够类型安全 | 使用 `BeanCopier.toMap(bean, CopyOptions)` 并在 options 中通过 `setIgnoreProperties()` 指定忽略字段 |

### 11.3 保留不变的方法

| 方法 | 说明 |
|------|------|
| `BeanUtils.toBean(source, constructor, Object... args)` | 构造器参数天然可变，保留 |
| `CopyOptions.setIgnoreProperties(String... ignoreProperties)` | 便捷方法，已存在 Collection 替代 |
| `CopyOptions.setCustomConverters(Converter<?, ?>... converters)` | 新增 API，保留 |
| `BeanUtils.toBean(source, constructor, CopyOptions options, Object... args)` | 构造器参数天然可变，保留 |

> 注意：`CopyOptions.setIgnoreProperties(String...)` 虽为可变参数，但已存在 `setIgnoreProperties(Collection)` 作为类型安全替代，不需额外标记。文档中推荐使用 Collection 版本即可。

### 11.4 修改清单

#### BeanUtils.java

```java
/**
 * ...(现有注释)
 * @deprecated 从 V5 开始废弃。推荐使用 {@link #toMap(Object)} 配合
 *             {@link CopyOptions#setIgnoreProperties(Collection)} 的方式：
 *             <pre>{@code
 *             CopyOptions options = CopyOptions.create()
 *                 .setIgnoreProperties(Arrays.asList("field1", "field2"));
 *             Map<String, Object> map = BeanUtils.toMap(bean);
 *             }</pre>
 *             或直接使用 {@link #toMap(Object)} 后手动过滤。
 */
@Deprecated
public static Map<String, Object> toMap(Object bean, String... ignoreProperties)
```

`@Deprecated` 注解添加，Javadoc 追加 `@deprecated` 标签，方法体保持不变（保持向后兼容）。

#### BeanCopier.java

```java
/**
 * ...(现有注释)
 * @deprecated 从 V5 开始废弃。推荐在 CopyOptions 中使用
 *             {@link CopyOptions#setIgnoreProperties(Collection)} 替代 ignoreFields
 *             可变参数：
 *             <pre>{@code
 *             CopyOptions options = CopyOptions.create()
 *                 .setIgnoreProperties(Arrays.asList("field1", "field2"));
 *             Map<String, Object> map = BeanCopier.toMap(bean, options);
 *             }</pre>
 */
@Deprecated
public static Map<String, Object> toMap(Object bean, CopyOptions options, String... ignoreFields)
```

### 11.5 验证标准

| 验证项 | 标准 |
|--------|------|
| 编译通过 | 添加 @Deprecated 后项目正常编译 |
| 不影响功能 | 废弃方法功能不受影响 |
| 替代方案可用 | 用户可通过推荐方式实现相同功能 |

---

## 12. 新增 SubStory 子任务分解

### 12.1 功能评分与分组

#### SubStory 05：嵌套转换支持

| 功能 | 参数 | 逻辑 | 依赖 | 边界 | 产出 | 状态 | 异常 | 外部 | 总分 | 分级 |
|------|------|------|------|------|------|------|------|------|------|------|
| F1: 嵌套 Map→Bean 递归转换 | 3 | 5 | 3 | 4 | 2 | 2 | 3 | 2 | **24** | 中等 |
| F2: 嵌套 List<Map>→List<Bean> 转换 | 3 | 4 | 4 | 4 | 2 | 2 | 3 | 2 | **24** | 中等 |
| F3: isSimpleType + resolveListElementType 辅助方法 | 1 | 2 | 1 | 2 | 1 | 1 | 1 | 1 | **10** | 极简 |
| F4: 单元测试 | 2 | 3 | 2 | 2 | 2 | 1 | 1 | 1 | **14** | 极简 |

**分组：F1 + F2 + F3 + F4 → Task 5**
- 合并检查：
  - 条件A: 同属嵌套转换功能，修改同一文件 BeanCopier.java ✓
  - 条件B: 都引用 BeanCopier / BeanDesc / CopyOptions ✓
  - 条件C: 总文件数 ≈ 2（BeanCopier.java + BeanCopierTest.java）≤ 6 ✓；估算行数 ≈ 250 ≤ 500 ✓

#### SubStory 06：错误信息增强

| 功能 | 参数 | 逻辑 | 依赖 | 边界 | 产出 | 状态 | 异常 | 外部 | 总分 | 分级 |
|------|------|------|------|------|------|------|------|------|------|------|
| F5: 异常信息格式增强 + buildConversionErrorMessage 辅助方法 | 1 | 2 | 2 | 2 | 1 | 1 | 2 | 1 | **12** | 极简 |

**独立为 Task 6**（单一功能，极简级别不可再拆分，也不需与其他合并——不同域的缺陷修复）

#### SubStory 07：废弃API清理

| 功能 | 参数 | 逻辑 | 依赖 | 边界 | 产出 | 状态 | 异常 | 外部 | 总分 | 分级 |
|------|------|------|------|------|------|------|------|------|------|------|
| F6: BeanUtils.toMap @Deprecated + Javadoc | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | **8** | 极简 |
| F7: BeanCopier.toMap @Deprecated + Javadoc | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | **8** | 极简 |

**分组：F6 + F7 → Task 7**
- 条件A: 同为废弃 API 标记，功能相关 ✓
- 条件B: 同为 @Deprecated + Javadoc 修改 ✓
- 条件C: 文件数 = 2 ≤ 6 ✓；估算行数 ≈ 30 ≤ 500 ✓

### 12.2 功能清单

#### Task 5：嵌套 Map/List 递归转换

| 功能 | 涉及文件 | 依赖 | 查找指引 |
|------|---------|------|---------|
| 嵌套 Map→Bean 递归转换 (`tryNestedConversion`) | `BeanCopier.java` | 无 | `copyProperties()` 类型转换阶段（第 196-237 行） |
| 嵌套 List<Map>→List<Bean> 转换 | `BeanCopier.java` | F3 辅助方法完成 | `copyProperties()` 类型转换阶段 |
| `isSimpleType()` 辅助方法 | `BeanCopier.java` | 无 | 参考现有的 `BeanUtils.isImmutableType()`（第 558-579 行） |
| `resolveListElementType()` 辅助方法 | `BeanCopier.java` | 无 | 通过 `BeanDesc.getField(propName)` → `field.getGenericType()` |
| 单元测试 | `BeanCopierTest.java` / `BeanUtilsTest.java` | F1+F2 实现完成 | 参考现有 `testCopyFromProvider` 和 `testCopyCrossType` 模式 |

#### Task 6：错误信息增强

| 功能 | 涉及文件 | 依赖 | 查找指引 |
|------|---------|------|---------|
| `buildConversionErrorMessage()` 辅助方法 | `BeanCopier.java` | 无 | 新增方法，参考现有 `findCustomConverter()` 模式（第 500-537 行） |
| 异常信息修改（3 处 throw + 1 处 debug） | `BeanCopier.java` | `buildConversionErrorMessage()` 完成 | `copyProperties()` 第 218-233 行 |

#### Task 7：废弃 API 清理

| 功能 | 涉及文件 | 依赖 | 查找指引 |
|------|---------|------|---------|
| `BeanUtils.toMap(Object bean, String... ignoreProperties)` @Deprecated | `BeanUtils.java` | 无 | 第 290-296 行 |
| `BeanCopier.toMap(Object bean, CopyOptions options, String... ignoreFields)` @Deprecated | `BeanCopier.java` | 无 | 第 326-381 行 |

### 12.3 依赖拓扑

```
Task 5 (嵌套转换支持) ──┐
                        ├── 可并行
Task 6 (错误信息增强) ──┤
                        │
Task 7 (废弃API清理) ──┘
（三个 Task 彼此独立，可并行执行）
```

### 12.4 与现有 Task 关系

| 现 Task | 对应 SubStory | 状态 | 与新增 Task 关系 |
|---------|--------------|------|-----------------|
| Task 1 | SubStory 02（临时Converter实现） | 已完成 | 无依赖 |
| Task 2 | SubStory 03（BeanCopier+CopOptions Javadoc） | 已完成 | 无依赖 |
| Task 3 | SubStory 03（BeanDesc+ValueProvider+MapValueProvider Javadoc） | 已完成 | 无依赖 |
| Task 4 | SubStory 04（V5优化建议整理） | 已完成 | 无依赖 |
| Task 5 | SubStory 05（嵌套转换支持） | 新增 | 无依赖 |
| Task 6 | SubStory 06（错误信息增强） | 新增 | 无依赖 |
| Task 7 | SubStory 07（废弃API清理） | 新增 | 无依赖 |

---

## 13. 实体关系图（V5 嵌套转换扩展）

### 13.1 嵌套转换调用关系

```
copyProperties() 原有流程:
  value → 临时Converter → (未命中) → 全局Converter → 类型匹配失败 → 抛异常

copyProperties() 新流程:
  value → 临时Converter → (未命中) → 嵌套递归转换 → (仍不匹配) → 全局Converter → (失败) → 抛异常
                                        │
                                        ├── Map → BeanUtils.toBean(map, targetType, options)
                                        │        └── BeanCopier.copy() [递归调用]
                                        │
                                        └── List<Map> → 逐元素 toBean(elementMap, elementType, options)
                                                 └── BeanCopier.copy() [递归调用]
```

### 13.2 类型检测流程

```
tryNestedConversion(value, targetPropertyType, options, depth)

  1. depth > MAX_NESTED_DEPTH(10)?
     ├─ Yes → return value (不转换，降级)
     └─ No  → 继续

  2. isSimpleType(targetPropertyType)?
     ├─ Yes → return value (基础类型/不可变类型不做嵌套)
     └─ No  → 继续

  3. value instanceof Map?
     ├─ Yes → BeanUtils.toBean((Map)value, targetPropertyType, options)
     └─ No  → 继续

  4. value instanceof List?
     ├─ Yes → resolveListElementType(targetDesc, propName)
     │         ├─ 解析成功 且 元素类型不是简单类型?
     │         │   ├─ Yes → 逐元素递归: Map→Bean
     │         │   └─ No  → return value
     │         └─ 解析失败 → return value
     └─ No  → return value
```
