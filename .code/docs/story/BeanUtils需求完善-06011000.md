# BeanUtils 功能需求文档

- 创建时间: 2026-06-01 10:00
- 最后更新: 2026-06-01 10:15
- 状态: 已确认

---

## 整体目标

完善 BeanUtils 工具类的三个需求方向：**自定义 Converter 临时传参**、**注释规范化**、**V5 版本优化建议整理**，确保 BeanUtils 在功能性、可维护性、易用性方面达到生产级标准。

---

## 项目上下文

### 现有 BeanUtils 架构

```
BeanUtils（门面类）
├── copyProperties(source, target)
├── copyProperties(source, target, CopyOptions)
├── toBean(source, targetClass)
├── toBean(source, targetClass, CopyOptions)
├── toBean(source, constructor, args)
├── toBean(source, constructor, options, args)
├── toBean(provider, targetClass)
├── toBean(provider, targetClass, CopyOptions)
├── toBean(map, targetClass)
├── toBean(map, targetClass, CopyOptions)
├── toMap(bean)
├── toMap(bean, ignoreProperties)
├── toList(sources, targetClass)
├── toList(sources, targetClass, options)
├── toList(sources, typeRef)
├── deepCopy(source)
├── deepCopy(source, maxDepth)
├── isEmpty(bean)
├── isNotEmpty(bean)
└── hasNullField(bean)

BeanCopier（核心拷贝引擎）
├── copy(source, target, CopyOptions)
├── copyFromProvider(provider, target, CopyOptions)
└── toMap(bean, CopyOptions, ignoreFields)

CopyOptions（配置选项）
├── ignoreNull
├── ignoreProperties
├── fieldMapping
├── forceFieldAccess
├── useConverter
├── ignoreCase
├── ignoreNoMatchConverterError
└── copySuperclassProperties

ConverterRegistry（全局转换器注册中心）
├── register(Converter)
├── unregister(Converter)
├── findAll(source, target)
├── findConverters(source, target)
├── convert(source, target)
└── convert(source, target, defaultValue)

Converter<S, T>（转换器接口）
├── convert(source)
├── getSourceType()
├── getTargetType()
├── bubbleLevel()
├── registrationOrder()
└── order()

ConditionConverter<S, T>（条件转换器接口）
└── matches(source)
```

### 当前转换调用路径

```
BeanCopier.copyProperties()
  → 发现类型不匹配时
    → ConverterRegistry.getInstance().findConverters(value.getClass(), targetPropertyType)
    → ConverterRegistry.getInstance().convert(value, targetPropertyType, value)
```

---

## 需求 1：自定义 Converter 支持（临时传参）

### 1.1 背景

当前 BeanUtils 通过 `ConverterRegistry.getInstance().register(converter)` 注册全局 Converter，所有类型转换都会使用全局 Converter。但在以下场景中，用户需要临时使用特定 Converter，不想影响全局注册：

- 本次调用需要特殊转换逻辑（如日期格式自定义）
- 一次性转换需求，不想污染全局 Converter 池
- 测试场景下使用 mock Converter

### 1.2 设计决策（已确认）

| 决策项 | 选择 | 说明 |
|--------|------|------|
| 优先级规则 | **临时优先** | 先找临时 Converter，找不到再 fallback 到全局 |
| Converter 类型 | **泛型 `Converter<?, ?>`** | 可注册任意类型对，调用时按需查找 |
| 生命周期 | **CopyOptions 生命周期** | 在 CopyOptions 有效期内有效，可多次调用 |
| 与全局 Converter 关系 | **额外补充模式** | 临时 Converter 找不到时，仍会 fallback 到全局 Converter |

### 1.3 设计方案

#### 1.3.1 CopyOptions 扩展

新增字段：
```java
/** 自定义转换器（临时，优先级高于全局） */
private List<Converter<?, ?>> customConverters;
```

新增方法：
```java
/**
 * 设置临时转换器（仅在本次 CopyOptions 生命周期内有效）
 * @param converters 临时转换器，可传入多个
 * @return this
 */
public CopyOptions setCustomConverters(Converter<?, ?>... converters) {
    this.customConverters = Arrays.asList(converters);
    return this;
}

/**
 * 获取临时转换器列表
 * @return 临时转换器列表，可能为空
 */
public List<Converter<?, ?>> getCustomConverters() {
    return customConverters != null ? Collections.unmodifiableList(customConverters) : Collections.emptyList();
}
```

#### 1.3.2 转换查找逻辑修改

在 `BeanCopier.copyProperties()` 的类型转换阶段，修改查找逻辑：

```
原有逻辑：
  1. ConverterRegistry.getInstance().findConverters(value.getClass(), targetPropertyType)
  2. ConverterRegistry.getInstance().convert(value, targetPropertyType, value)

新逻辑：
  1. 如果 CopyOptions 中有 customConverters
     → 先在 customConverters 中查找匹配的 Converter
     → 找到则使用，找不到则继续
  2. 如果 useConverter=true
     → 使用 ConverterRegistry.getInstance() 的全局 Converter
     → 找不到且 ignoreNoMatchConverterError=true 则跳过，否则抛异常
```

#### 1.3.3 自定义 Converter 注册机制

临时 Converter 支持：
- 通过 `ConverterUtils.of(src, target, function)` 创建
- 支持 ConditionConverter（带 matches 条件）
- 支持多个临时 Converter，按注册顺序匹配

调用示例：
```java
// 示例 1：使用临时 Converter（CopyOptions 方式）
Converter<String, Date> dateConverter = ConverterUtils.of(
    String.class, Date.class,
    s -> new SimpleDateFormat("yyyy-MM-dd").parse(s)
);
CopyOptions options = CopyOptions.create()
    .setCustomConverters(dateConverter);
User target = BeanUtils.toBean(source, User.class, options);

// 示例 2：使用条件转换器
ConditionConverter<String, Integer> conditionConverter = ConverterUtils.of(
    String.class, Integer.class,
    0,
    s -> s.startsWith("ID:"),  // matches 条件
    s -> Integer.parseInt(s.replace("ID:", ""))
);
CopyOptions options = CopyOptions.create()
    .setCustomConverters(conditionConverter);
User target = BeanUtils.toBean(source, User.class, options);
```

### 1.4 约束条件

1. 临时 Converter 仅在设置了 `setCustomConverters()` 的 CopyOptions 中生效
2. 临时 Converter 找不到匹配时，自动 fallback 到全局 Converter
3. 临时 Converter 不影响 `ConverterRegistry.getInstance()` 的全局注册状态
4. 临时 Converter 支持 `ConditionConverter`，按 matches 条件匹配

### 1.5 验收标准

| 验收项 | 标准 |
|--------|------|
| 临时 Converter 优先级 | 临时 Converter 匹配成功时，一定使用临时 Converter |
| Fallback 行为 | 临时 Converter 找不到时，fallback 到全局 Converter |
| 生命周期 | 临时 Converter 在 CopyOptions 有效期内可多次使用 |
| 条件转换器支持 | ConditionConverter 的 matches 条件正确生效 |
| 不影响全局 | 使用临时 Converter 后，全局 Converter 状态不变 |

---

## 需求 2：注释规范化

### 2.1 背景

BeanUtils.java 的 Javadoc 注释比较规范，但 `bean/copier/` 包下的辅助类存在注释不完整的问题：

- 部分 public 方法缺少 Javadoc
- 参数和返回值说明缺失
- 异常说明不完整

### 2.2 规范要求

根据项目偏好（`4-preferences.md`）和编码规范：
- 所有 public 方法必须有 Javadoc
- `@param` 描述参数含义（不为 null 时说明）
- `@return` 描述返回值（特殊行为如返回 null、空集合等情况说明）
- `@throws` 描述可能抛出的异常及触发条件
- 注释中禁止使用 HTML 标签 `<>`，使用 Markdown 语法

### 2.3 涉及文件及需要补充的方法

| 文件 | 需要补充 Javadoc 的方法 | 说明 |
|------|------------------------|------|
| BeanCopier.java | `getOrCreateBeanDesc()` | 补充 `@param`、`@return`、`@throws` |
| CopyOptions.java | 所有 setter 和 getter | 补充 `@param`、`@return` |
| BeanDesc.java | `getPropertyNameIgnoreCase()`、`getField()` | 补充缺失的 Javadoc |
| ValueProvider.java | `value()`、`containsKey()` | 接口方法补充 Javadoc |
| MapValueProvider.java | 类级 Javadoc 补充 | 补充类和构造器的 Javadoc |

### 2.4 验收标准

| 验收项 | 标准 |
|--------|------|
| BeanCopier.java | 所有 public 方法有 Javadoc，包含 `@param`、`@return`、`@throws` |
| CopyOptions.java | 所有 setter 有 `@param`，所有 getter 有 `@return` |
| BeanDesc.java | 所有 public 方法有 Javadoc，无 HTML 标签 |
| ValueProvider.java | 接口方法有 Javadoc，说明方法契约 |
| MapValueProvider.java | 类级 Javadoc 完整，说明用途和示例 |

---

## 需求 3：V5 版本优化建议

### 3.1 背景

用户希望以资深 BeanUtils 使用者角度，整理 V5 版本的优化建议，整理后供用户审核，再决定是否实现。

### 3.2 优化建议分级清单

#### Must（有明显缺陷，必须优化）

| 方向 | 优化项 | 说明 | 优先级 |
|------|--------|------|--------|
| 功能增强 | 嵌套 Map → Bean 递归转换 | 当前 `toBean(map, Target.class)` 时，Map 的 value 如果也是 Map，不会递归转换为嵌套 Bean。这导致 `Map<String, Map<String, Object>>` 无法正确转换为嵌套 Bean 结构 | P0 |
| 功能增强 | 嵌套 List 元素类型转换 | 当前 `toBean(map, Target.class)` 时，如果 map 中某个 key 对应的 value 是 `List<Map>`，不会递归转换为 `List<TargetElement>` | P0 |
| 易用性改进 | 错误信息增强 | 当前类型转换失败时，错误信息不包含源属性名和目标属性名，难以定位问题。期望：`Property 'xxx' conversion failed: String -> Integer, value='abc'` | P0 |
| 兼容性 | 废弃 API 清理 | 部分旧方法（如 `copyProperties(source, target, ignoreFields)`）使用可变参数风格，应标记为 `@Deprecated` 并提供替代方案 | P1 |

#### Should（有改进空间，强烈建议）

| 方向 | 优化项 | 说明 | 优先级 |
|------|--------|------|--------|
| 性能优化 | 反射缓存优化 | 当前 `BeanDesc` 使用 `SimpleCacheHelper` 缓存，但没有大小淘汰机制。在处理大量不同类型时可能导致内存压力。建议：引入 LRU 淘汰策略或 LinkedHashMap-based LRU | P1 |
| 功能增强 | 属性路径拷贝支持 | 支持 `copyProperties(source, target, "user.address.city")` 形式，拷贝嵌套属性。当前需要手动逐层拷贝 | P2 |
| 功能增强 | 字段级 Converter | 为特定字段指定专属 Converter，如 `fieldMapping("createTime", "createTime").converter(dateConverter)` | P2 |
| 易用性改进 | 链式 API 增强 | 提供流式 API：`BeanUtils.from(source).to(TargetClass.class).withOptions(opts).execute()` | P3 |
| 易用性改进 | 类型推断增强 | 减少泛型显式声明，如 `BeanUtils.toList(sources, User.class)` 可改为 `BeanUtils.toList(sources)` 自动推断 | P3 |

#### Could（有则更好）

| 方向 | 优化项 | 说明 | 优先级 |
|------|--------|------|--------|
| 性能优化 | ASM 字节码优化 | 对于高频拷贝场景（如批量导入），考虑使用 ASM 生成字节码进行属性拷贝，替代反射 | P4 |
| 性能优化 | MethodHandle 替代 Method | MethodHandle 比 Reflection 更快，考虑在 BeanDesc 中缓存 MethodHandle | P4 |
| 功能增强 | 转换回调钩子 | 支持在属性拷贝前后插入自定义逻辑，如日志记录、值验证 | P4 |
| 功能增强 | 自定义跳过策略 | 支持 `PropertySkipStrategy`，如跳过值为空的 String 属性 | P4 |
| 兼容性 | 迁移工具 | 提供从 V4 到 V5 的迁移指南，包括 API 变更说明 | P4 |

### 3.3 验收标准

| 验收项 | 标准 |
|--------|------|
| 分级完整性 | Must/Should/Could 各层级都有具体条目 |
| 覆盖全面性 | 包含功能增强、性能优化、易用性改进、兼容性四个方向 |
| 优先级一致性 | P0 = Must，P1 = Should，P2-P3 = Should/Could，P4 = Could |
| 描述清晰度 | 每条建议有明确的问题描述和优化说明 |

---

## SubStory 分解

| 序号 | 名称 | 需求范围 | 验收标准 | 依赖 |
|------|------|---------|---------|------|
| 01 | 临时Converter设计 | 确定临时 Converter 的设计方案（优先级、类型、生命周期、与 CopyOptions 关系） | 输出设计方案供用户确认 | 无 |
| 02 | 临时Converter实现 | 在 CopyOptions / BeanCopier 中支持临时 Converter 传参 | 临时 Converter 优先级高于全局，fallback 正确，CopyOptions 生命周期有效 | 01 |
| 03 | 注释规范化 | 补充 BeanCopier、CopyOptions、BeanDesc、ValueProvider、MapValueProvider 的 Javadoc | 所有 public 方法有完整 Javadoc，无 HTML 标签 | 无 |
| 04 | V5优化建议整理 | 整理功能增强、性能优化、易用性改进、兼容性四个方向的具体建议 | 输出优化建议清单，Must/Should/Could 分级清晰 | 无 |

### 功能覆盖矩阵

| 功能点 | 对应 SubStory | 验收标准 |
|--------|-------------|---------|
| 临时 Converter 设计方案 | 01-临时Converter设计 | 设计方案完整，优先级/类型/生命周期/补充模式明确 |
| 临时 Converter 实现 | 02-临时Converter实现 | 单元测试通过，优先级正确，fallback 正确 |
| 注释规范化 | 03-注释规范化 | 所有 public 方法 Javadoc 完整 |
| V5 优化建议分级清单 | 04-V5优化建议整理 | Must/Should/Could 分级，每条有描述 |

---

## 确认门控

### 第一级 — 子任务检验（≥95%）

1. **目标检验**：四个 SubStory 是否覆盖所有需求？
   - [x] 需求1（自定义Converter）→ SubStory 01 + 02
   - [x] 需求2（注释规范化）→ SubStory 03
   - [x] 需求3（V5优化建议）→ SubStory 04

2. **边界检验**：临时 Converter 设计边界是否清晰？
   - [x] 优先级：临时优先，找不到 fallback 到全局
   - [x] 类型：泛型 Converter<?, ?>
   - [x] 生命周期：CopyOptions 生命周期
   - [x] 补充模式：额外补充，不替代全局

3. **一致性检验**：SubStory 分解是否符合 high-cohesion/low-coupling？
   - [x] 01 + 02 强耦合（实现依赖设计）→ 同一需求聚合
   - [x] 03 独立（仅文档/Javadoc 工作）
   - [x] 04 独立（仅整理优化建议清单）

### 第二级 — 总体目标检验（≥98%）

4. **整体目标验证**：整体目标是否明确？
   - [x] 三个需求方向已明确
   - [x] V5 建议整理为分级清单，用户审核后决定是否实现

**置信度**：98%（所有设计边界已确认，SubStory 覆盖完整）

---

## [DECISIONS]

- Story 文档: `.code/docs/story/BeanUtils需求完善-06011000.md`
- 需求范围：
  - 需求1：自定义 Converter 临时传参（临时优先 + fallback 到全局，CopyOptions 生命周期，额外补充模式）
  - 需求2：注释规范化（BeanCopier、CopyOptions、BeanDesc、ValueProvider、MapValueProvider）
  - 需求3：V5 优化建议分级清单（Must/Should/Could）
- 确认的边界：
  - 临时 Converter 优先级：临时优先，找不到再 fallback 到全局
  - 临时 Converter 类型：泛型 Converter<?, ?>
  - 生命周期：CopyOptions 生命周期
  - 与全局 Converter 关系：额外补充模式（找不到时 fallback 到全局）
  - V5 优化建议粒度：需要按 Must/Should/Could 分级
- 用户指定的特殊要求：
  - V5 建议先整理后审核，不直接实现
  - 注释禁用 HTML 标签，使用 Markdown 语法
- 未解决的问题：无（所有边界已确认）

---

[ROUTE_TO:myPlan]