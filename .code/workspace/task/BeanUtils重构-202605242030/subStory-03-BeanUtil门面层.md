---
subStory: BeanUtil 门面层
subStory_id: 03
wave: 2
状态: 待开始
plan_path: .code/docs/plan/BeanUtils重构-202605242030.md
story_path: .code/docs/story/BeanUtils重构-202605242030.md
创建时间: 2026-05-24 22:10
最后更新: 2026-05-24 22:10
---

# subStory-03-BeanUtil门面层

## 上下文摘要

创建顶层门面类 BeanUtil，提供 copyProperties、toBean、toMap、toList 等静态方法。内部委托 BeanCopier 执行实际拷贝，通过 CopyOptions 配置化驱动。

## 核心实体

- `BeanUtil` — 门面类（public final，私有构造器）
- `BeanCopier` — 底层引擎（依赖）
- `CopyOptions` — 配置选项（依赖）
- `ValueProvider` — 值提供者（依赖）

## 验收标准

- [ ] `BeanUtil.copyProperties(source, target)` 简单拷贝工作正常
- [ ] `BeanUtil.copyProperties(source, target, CopyOptions)` 带配置拷贝工作正常
- [ ] `BeanUtil.toBean(source, TargetClass)` 返回新实例
- [ ] `BeanUtil.toBean(valueProvider, TargetClass)` 从 ValueProvider 创建
- [ ] `BeanUtil.toMap(obj)` 返回 `Map<String, Object>` 含所有非 null 属性
- [ ] `BeanUtil.toMap(obj, "field1", "field2")` 忽略指定属性
- [ ] `BeanUtil.toList(sources, TargetClass)` 批量拷贝
- [ ] 性能测试：同类型拷贝不劣化，批量拷贝提升 30%+

## 涉及文件

| 文件 | 用途 | 类型 |
|------|------|------|
| `bean/BeanUtil.java` | 门面类 | create |
| `test/.../bean/BeanUtilTest.java` | 单元测试 | create |

## 子任务

### Task 1：创建 BeanUtil 类（copyProperties + toBean）
- **类型**：coding
- **状态**：pending
- **依赖**：SubStory-01 (BeanCopier), SubStory-02 (ConverterRegistry), SubStory-05 (ValueProvider)
- **涉及文件**：`bean/BeanUtil.java`
- **上下文**：
  - public final class，private 构造器
  - `copyProperties(Object source, Object target)` → `BeanCopier.copy(source, target, null)`
  - `copyProperties(Object source, Object target, CopyOptions options)` → `BeanCopier.copy(source, target, options)`
  - `toBean(Object source, Class<T> targetClass)` → 先 `targetClass.newInstance()` 再 copy
  - `toBean(Object source, Class<T> targetClass, CopyOptions options)` → 同上 + options
  - `toBean(ValueProvider provider, Class<T> targetClass)` → 先 newInstance 再 `BeanCopier.copyFromProvider()`
  - `toBean(ValueProvider provider, Class<T> targetClass, CopyOptions options)` → 同上 + options
  - 异常包装：反射异常统一包装为 BaseException
- **查找指引**：参考 `BeanUtils.getBeanByMap()` (L315-351) targetClass.newInstance() 模式、`BeanUtils.copyProperties()` 重载链

### Task 2：实现 toMap + toList
- **类型**：coding
- **状态**：pending
- **依赖**：Task 1 (BeanUtil基础)
- **涉及文件**：`bean/BeanUtil.java`（追加方法）
- **上下文**：
  - `toMap(Object bean)` → `BeanCopier.toMap(bean, null)`
  - `toMap(Object bean, String... ignoreProperties)` → `BeanCopier.toMap(bean, options, ignoreProperties)`
  - `toList(List<S> sources, Class<T> targetClass)` → 预分配 ArrayList(sources.size())，循环 toBean
  - `toList(List<S> sources, Class<T> targetClass, CopyOptions options)` → 同上 + options
  - toMap 自动过滤 null 值
  - toList 预分配容量避免扩容
- **查找指引**：参考 `BeanUtils.copyListProperties()` (L70-82)、`BeanUtils.toMap()` (L510-532)

### Task 3：BeanUtil 单元测试
- **类型**：coding
- **状态**：pending
- **依赖**：Task 1, Task 2
- **涉及文件**：`test/.../bean/BeanUtilTest.java`
- **上下文**：
  - 覆盖 Story 6 个核心场景测试：
    1. 简单对象拷贝（同类型）
    2. 跨类型对象映射（涉及 Converter）
    3. 列表批量拷贝
    4. 带条件过滤的拷贝（ignoreProperties + ignoreNull）
    5. 对象转 Map
    6. Map/ValueProvider 转对象
  - 边界测试：null source、null target、空对象
  - 性能测试：10万次拷贝时间，与现有 BeanUtils 对比
- **查找指引**：参考 `BeanUtilsTest.java`、Story 核心场景 L65-L161
