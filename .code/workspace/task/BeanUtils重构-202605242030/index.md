# index — BeanUtils重构

**创建时间**：2026-05-24 22:10
**最后更新**：2026-05-24 22:10
**Story 路径**：`.code/docs/story/BeanUtils重构-202605242030.md`
**Plan 路径**：`.code/docs/plan/BeanUtils重构-202605242030.md`

## SubStory 索引

| # | SubStory | 路径 | 范围 | 验收标准 | 状态 |
|---|----------|------|------|---------|------|
| 01 | BeanCopier 底层组件 | `subStory-01-BeanCopier底层组件.md` | BeanDesc、CopyOptions、BeanCopier 引擎、缓存 | BeanDesc 缓存正确；Copier 支持 PD/Field 双模式 | ⏳ |
| 02 | ConverterRegistry 全局注册表 | `subStory-02-ConverterRegistry全局注册表.md` | 已有冒泡注册机制已验证 | 冒泡注册已可用 | ⏳ |
| 03 | BeanUtil 门面层 | `subStory-03-BeanUtil门面层.md` | copyProperties、toBean、toMap、toList | API 一致；6 个场景验收通过 | ⏳ |
| 04 | 向后兼容层 | `subStory-04-向后兼容层.md` | BeanUtils @Deprecated 委托 | 原 API 编译警告非错误；委托逻辑正确 | ⏳ |
| 05 | ValueProvider 支持 | `subStory-05-ValueProvider支持.md` | ValueProvider 接口、MapValueProvider | 支持 Map/任意源 → Bean | ⏳ |
