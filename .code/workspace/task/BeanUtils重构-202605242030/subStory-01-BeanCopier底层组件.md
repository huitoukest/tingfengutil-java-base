---
subStory: BeanCopier 底层组件
subStory_id: 01
wave: 1
状态: 待开始
plan_path: .code/docs/plan/BeanUtils重构-202605242030.md
story_path: .code/docs/story/BeanUtils重构-202605242030.md
创建时间: 2026-05-24 22:10
最后更新: 2026-05-24 22:10
---

# subStory-01-BeanCopier底层组件

## 上下文摘要

实现 BeanCopier 底层拷贝子系统，包含 BeanDesc（属性描述缓存）、CopyOptions（拷贝配置）、BeanCopier（拷贝执行引擎）。属性描述使用 SimpleCacheHelper 缓存，支持 PropertyDescriptor / Field 双模式自动检测。

## 核心实体

- `BeanDesc` — 属性描述缓存（包内可见，非 public）
- `CopyOptions` — 拷贝配置选项
- `BeanCopier` — 拷贝执行引擎

## 验收标准

- [ ] BeanDesc 能正确内省 Java Bean，缓存 PropertyDescriptor 和 Field
- [ ] CopyOptions 支持 ignoreNull、ignoreProperties、fieldMapping、forceFieldAccess、useConverter
- [ ] BeanCopier 正确执行 source → target 的属性拷贝
- [ ] 模式自动检测：PropertyDescriptor 优先，不存在时降级 Field
- [ ] forceFieldAccess=true 时跳过 PropertyDescriptor
- [ ] 跨类型拷贝时调用 ConverterRegistry 进行类型转换
- [ ] BeanDesc 通过 SimpleCacheHelper 缓存，第二次调用不重复内省
- [ ] BeanCopier 单元测试覆盖所有模式

## 涉及文件

| 文件 | 用途 | 类型 |
|------|------|------|
| `bean/copier/BeanDesc.java` | 属性描述缓存 | create |
| `bean/copier/CopyOptions.java` | 拷贝配置 | create |
| `bean/copier/BeanCopier.java` | 拷贝执行引擎 | create |
| `test/.../bean/copier/BeanCopierTest.java` | BeanCopier 单元测试 | create |

## 子任务

### Task 1：创建 BeanDesc 类
- **类型**：coding
- **状态**：pending
- **依赖**：无
- **涉及文件**：`bean/copier/BeanDesc.java`
- **上下文**：
  - 对 Class 执行 Introspector.getBeanInfo() 获取 PropertyDescriptor[]
  - 通过 ReflectUtils.getFields() 获取 Field[]
  - 缓存 pdMap (name→PropertyDescriptor) 和 fieldMap (name→Field)
  - 排除 static/final 字段，排除 class 属性
  - 提供 getPropertyValue(bean, name) / setPropertyValue(bean, name, value)
  - 先尝试 PropertyDescriptor 读写，不存在则降级 Field
  - 包内可见（非 public）
- **查找指引**：参考 `BeanUtils.getBeanCopyPropertyDescriptorMap()` (L480-500)、`ReflectUtils.getFields()`、`ReflectUtils.getSetterName()`

### Task 2：创建 CopyOptions 类
- **类型**：coding
- **状态**：pending
- **依赖**：无
- **涉及文件**：`bean/copier/CopyOptions.java`
- **上下文**：
  - 字段：ignoreNull(boolean), ignoreProperties(Set<String>), fieldMapping(Map<String,String>), forceFieldAccess(boolean), useConverter(boolean, default true)
  - 静态工厂 CopyOptions.create()
  - 链式 setter 返回 this
  - 必须正确实现 equals() 和 hashCode()（用于缓存 key 的潜在用途）
- **查找指引**：参考 Story 场景 4（带条件过滤）、场景 2（字段映射）

### Task 3：创建 BeanCopier 核心拷贝方法
- **类型**：coding
- **状态**：pending
- **依赖**：Task 1 (BeanDesc), Task 2 (CopyOptions)
- **涉及文件**：`bean/copier/BeanCopier.java`
- **上下文**：
  - 核心方法 `copy(Object source, Object target, CopyOptions options)`
  - 获取 sourceDesc / targetDesc（通过 SimpleCacheHelper 缓存）
  - 遍历 target 可写属性，忽略列表检查 → 字段映射 → source 取值 → null 判断 → 类型转换 → 赋值
  - 模式自动检测：forceFieldAccess=false 时先 PD，属性不存在则 Field
  - 跨类型转换：value.getClass() != propertyType 时调用 ConverterRegistry
  - 转换失败跳过该属性，不抛异常
- **查找指引**：参考 `BeanUtils.copyProperties()` 第92-171行（Field模式+PD模式）、`DefaultConverterRegistry.convert()` 第188-228行

### Task 4：BeanCopier 辅助方法 + 缓存集成
- **类型**：coding
- **状态**：pending
- **依赖**：Task 3 (BeanCopier 核心)
- **涉及文件**：`bean/copier/BeanCopier.java`（追加）
- **上下文**：
  - 定义 `SOURCE_DESC_CACHE` 和 `TARGET_DESC_CACHE`：`SimpleCacheHelper<UnionKey, BeanDesc>` 容量512
  - 实现 `getOrCreateBeanDesc(Class<?>)` 缓存方法
  - 实现 `toMap(Object bean, CopyOptions options, String... ignoreFields)`
  - toMap: 遍历 BeanDesc 可读属性，过滤 null + ignoreFields，返回 HashMap
- **查找指引**：参考 `BeanUtils.toMap()` 第510-532行、`SimpleCacheHelper` 使用示例

### Task 5：BeanCopier 单元测试
- **类型**：coding
- **状态**：pending
- **依赖**：Task 3, Task 4
- **涉及文件**：`test/.../bean/copier/BeanCopierTest.java`
- **上下文**：
  - 测试同类型拷贝：source→target 属性值一致
  - 测试跨类型拷贝：涉及 Converter 调用
  - 测试 ignoreNull / ignoreProperties / fieldMapping / forceFieldAccess 各配置
  - 测试 null source / null target 边界
  - 测试 PropertyDescriptor 模式 vs Field 模式
  - 测试 BeanDesc 缓存（第二次调用验证）
  - 测试 toMap 输出正确性
- **查找指引**：参考 `BeanUtilsTest.java` 现有测试模式（Assert 断言、TestUtils.printTime）
