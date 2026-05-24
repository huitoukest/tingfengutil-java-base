---
subStory: 向后兼容层
subStory_id: 04
wave: 3
状态: 待开始
plan_path: .code/docs/plan/BeanUtils重构-202605242030.md
story_path: .code/docs/story/BeanUtils重构-202605242030.md
创建时间: 2026-05-24 22:10
最后更新: 2026-05-24 22:10
---

# subStory-04-向后兼容层

## 上下文摘要

将现有 BeanUtils 类标记 @Deprecated，方法体委托到 BeanUtil 新实现。保留 `getFieldNameByGetter()`（LambdaUtils 使用）和 `createBeanConverter()`（CSVUtil 使用）原实现。

## 核心实体

- `BeanUtils` — 现有工具类（修改）

## 验收标准

- [ ] BeanUtils 类级 + 方法级标记 @Deprecated
- [ ] `BeanUtils.copyProperties()` 委托到 `BeanUtil` 后行为一致
- [ ] `BeanUtils.toMap()` 委托后行为一致
- [ ] `BeanUtils.getBeanByMap()` 委托后行为一致
- [ ] `BeanUtils.copyListProperties()` 委托 `BeanUtil.toList()` 后行为一致
- [ ] `BeanUtils.getFieldNameByGetter()` 保留原实现
- [ ] `BeanUtils.createBeanConverter()` 保留原实现
- [ ] 原 BeanUtilsTest 全部测试通过
- [ ] HttpUtils 未使用的 import 移除

## 涉及文件

| 文件 | 用途 | 类型 |
|------|------|------|
| `bean/BeanUtils.java` | @Deprecated + 委托 | modify |
| `net/HttpUtils.java` | 移除未使用 import | modify |
| `test/.../bean/BeanUtilsTest.java` | 追加兼容性测试 | modify |

## 子任务

### Task 1：BeanUtils @Deprecated 改造
- **类型**：coding
- **状态**：pending
- **依赖**：SubStory-03 (BeanUtil 全部功能)
- **涉及文件**：`bean/BeanUtils.java`
- **上下文**：
  - 类级别添加 `@Deprecated` 注解 + Javadoc：@deprecated 请使用 BeanUtil
  - 逐方法添加 `@Deprecated` 注解
  - 方法体委托到 BeanUtil / BeanCopier：
    - `copyProperties(T, O, String...)` → `BeanUtil.copyProperties(source, target, CopyOptions.create().setIgnoreProperties(exceptFields))`
    - `copyProperties(T, O, boolean, Collection<String>)` → 根据 boolean 决定 mode（通过 CopyOptions.forceFieldAccess）
    - `copyProperties(T, O)` → `BeanUtil.copyProperties(source, target)`
    - `copyProperties(T, O, Predicate, Function, String...)` → 通过 CopyOptions + 简化逻辑
    - `copyPropertiesNotStrict(...)` → `BeanUtil.copyProperties(source, target, CopyOptions.create().setForceFieldAccess(true))`
    - `copyListProperties(List, Class, String...)` → `BeanUtil.toList(sources, targetClass, Options.setIgnoreProperties(...))`
    - `toMap(T, PropertyFunction...)` → `BeanUtil.toMap(obj, fieldNames...)`（PropertyFunction 转 fieldName）
    - `getBeanByMap(Class, Map)` → `BeanUtil.toBean(new MapValueProvider(map), cls)`
    - `getBeanByMap(T, Map)` → `BeanCopier.copyFromProvider(new MapValueProvider(map), t, null)`
  - 以下方法保留原实现（不委托，不标记 @Deprecated 或保留实现）：
    - `getFieldNameByGetter(String)` — LambdaUtils 使用
    - `createBeanConverter(...)` — CSVUtil 使用
    - `getBeanInfo(Class)` — 纯代理 Introspector
  - 以下方法标记 @Deprecated 但保留原实现（无新等价方法）：
    - `getBeanCopyMethodMap(...)` — 低使用率
    - `getBeanCopyFieldMap(...)` — 低使用率
    - `getBeanCopyFunMap(...)` — 低使用率
- **查找指引**：现有 `BeanUtils.java` 全文件 663 行，逐方法审查

### Task 2：HttpUtils 清理 + 兼容测试
- **类型**：coding
- **状态**：pending
- **依赖**：Task 1
- **涉及文件**：`net/HttpUtils.java`, `test/.../bean/BeanUtilsTest.java`
- **上下文**：
  - `HttpUtils.java` 第6行：移除 `import com.tingfeng.util.java.base.bean.BeanUtils;`
  - `BeanUtilsTest.java` 追加兼容性测试：
    - 确认 `BeanUtils.copyProperties()` 调用 `@Deprecated` 方法产生编译警告（测试编译期行为）
    - 运行时确认委托到 BeanUtil 后结果一致
- **查找指引**：现有 `BeanUtilsTest.java`、`HttpUtils.java`
