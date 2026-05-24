---
subStory: ConverterRegistry 全局注册表
subStory_id: 02
wave: 1
状态: 待开始
plan_path: .code/docs/plan/BeanUtils重构-202605242030.md
story_path: .code/docs/story/BeanUtils重构-202605242030.md
创建时间: 2026-05-24 22:10
最后更新: 2026-05-24 22:10
---

# subStory-02-ConverterRegistry全局注册表

## 上下文摘要

ConverterRegistry 的冒泡注册机制已在 [converter-bubble-registration] Story 中完成。本 SubStory 为验证确认，确保 BeanCopier 跨类型转换时能正确调用已注册的 ConverterRegistry。

## 核心实体

- `ConverterRegistry`（现有）
- `DefaultConverterRegistry`（现有）
- `ConverterUtils`（现有）

## 验收标准

- [ ] ConverterRegistry.getConverterByValue 能正确找到匹配转换器
- [ ] BeanCopier 跨类型转换时调用 ConverterRegistry.getInstance().convert()
- [ ] 冒泡注册的 Converter 在跨类型转换中生效

## 涉及文件

| 文件 | 用途 | 类型 |
|------|------|------|
| `bean/converter/DefaultConverterRegistry.java` | 验证现有实现 | read_only |
| `bean/converter/ConverterRegistry.java` | 验证接口 | read_only |

## 子任务

### Task 1：验证 ConverterRegistry 冒泡注册机制可用
- **类型**：analysis
- **状态**：pending
- **依赖**：无
- **涉及文件**：`bean/converter/DefaultConverterRegistry.java`, `bean/converter/ConverterRegistry.java`
- **上下文**：
  - 确认 `DefaultConverterRegistry` 的 `register()` 方法已正确实现冒泡注册
  - 确认 `convert()` 方法能正确遍历 ConditionConverter 和普通 Converter
  - 确认 `BubbleRegistrationTest` 测试通过
  - 如有缺陷，记录并修复
- **查找指引**：参考已完成的 `converter-bubble-registration.md` Story 文档和 `BubbleRegistrationTest.java`
