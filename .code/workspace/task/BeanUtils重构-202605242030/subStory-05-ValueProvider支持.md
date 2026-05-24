---
subStory: ValueProvider 支持
subStory_id: 05
wave: 2
状态: 待开始
plan_path: .code/docs/plan/BeanUtils重构-202605242030.md
story_path: .code/docs/story/BeanUtils重构-202605242030.md
创建时间: 2026-05-24 22:10
最后更新: 2026-05-24 22:10
---

# subStory-05-ValueProvider支持

## 上下文摘要

实现 ValueProvider 接口及其 MapValueProvider 实现，增强 BeanCopier 支持从任意值源（Map、Http 参数等）拷贝到 Bean 的能力。

## 核心实体

- `ValueProvider` — 值提供者接口
- `MapValueProvider` — Map 适配实现
- `BeanCopier` — 引擎集成

## 验收标准

- [ ] ValueProvider 接口定义正确（value + containsKey 方法）
- [ ] MapValueProvider 正确包装 Map 取值
- [ ] MapValueProvider key 支持 a.b.c 点号路径
- [ ] BeanCopier.copyFromProvider() 正确实现从 ValueProvider 到 Bean 的拷贝
- [ ] BeanUtil.toBean(ValueProvider, Class) 可正常使用

## 涉及文件

| 文件 | 用途 | 类型 |
|------|------|------|
| `bean/copier/ValueProvider.java` | 值提供者接口 | create |
| `bean/copier/MapValueProvider.java` | Map 值提供者实现 | create |
| `bean/copier/BeanCopier.java` | 追加 copyFromProvider 方法 | modify |
| `test/.../bean/copier/BeanCopierTest.java` | 追加 ValueProvider 测试 | modify |

## 子任务

### Task 1：创建 ValueProvider 接口 + MapValueProvider
- **类型**：coding
- **状态**：pending
- **依赖**：无
- **涉及文件**：`bean/copier/ValueProvider.java`, `bean/copier/MapValueProvider.java`
- **上下文**：
  - ValueProvider<T> 接口：
    - `Object value(String key, Class<?> type)` — 获取指定 key 的值
    - `boolean containsKey(String key)` — 检查 key 是否存在
  - MapValueProvider 实现：
    - 构造器接收 `Map<String, ?>`
    - `value(key, type)` → `map.get(key)`
    - `containsKey(key)` → `map.containsKey(key)`
    - key 支持 a.b.c 点号路径 → 通过 `ReflectUtils` 或递归拆解
  - 类型参数 type 暂不用于 Map 查找，保留给未来扩展
- **查找指引**：参考 `BeanUtils.getBeanByMap()` (L315-351) 中 map 取值逻辑、Hutool 5 ValueProvider 设计

### Task 2：BeanCopier 集成 ValueProvider
- **类型**：coding
- **状态**：pending
- **依赖**：Task 1 (ValueProvider)
- **涉及文件**：`bean/copier/BeanCopier.java`（追加 copyFromProvider 方法）
- **上下文**：
  - `copyFromProvider(ValueProvider provider, Object target, CopyOptions options)`：
    1. 获取 targetDesc
    2. 遍历 target 可写属性（经 ignoreProperties / fieldMapping 过滤）
    3. 从 provider.value(propName, propertyType) 取值
    4. ignoreNull 检查
    5. 类型转换（useConverter=true 时）
    6. 赋值
  - 与 `copy(Object, Object, CopyOptions)` 的核心区别：取值来源不同
  - 建议提取公共的 "属性遍历 + 配置过滤 + 类型转换" 逻辑为内部方法，避免重复
- **查找指引**：参考 `BeanCopier.copy()` 核心逻辑（Task 3）、`BeanUtils.getBeanByMap()` (L332-351)

### Task 3：ValueProvider 单元测试
- **类型**：coding
- **状态**：pending
- **依赖**：Task 1, Task 2
- **涉及文件**：`test/.../bean/copier/BeanCopierTest.java`（追加测试方法）
- **上下文**：
  - 测试 MapValueProvider 基本取值
  - 测试 MapValueProvider a.b.c 点号路径取值
  - 测试 copyFromProvider 从 Map 到 Bean 的完整拷贝
  - 测试 ignoreNull / ignoreProperties / fieldMapping 在 ValueProvider 场景下的生效
  - 测试空 Map、null provider 边界
- **查找指引**：参考 `BeanUtilsTest.mapToBeanTest()` (L48-57)
