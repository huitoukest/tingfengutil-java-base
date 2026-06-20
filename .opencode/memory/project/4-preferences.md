# 用户偏好
> tingfengutil-java-base
> 最后更新: 2026-05-23 11:01

## 记录规则
此文件记录用户在与 AI 协作中表达的、与项目相关的偏好/约定。
当用户说「记住」「注意」「以后都这样」「我喜欢」时，应询问用户是否记录到此文件。

## 命名偏好
- 接口命名统一不使用 I 前缀/后缀，采用 UpperCamelCase 名词/形容词
- 新接口不应添加 I 前缀或 I 后缀。遗留接口（IEnum, ConvertI）暂不改

## 设计偏好
- tool 类优先用 Utils 命名（静态方法），有上下文的用 Helper（需实例化）
- 转换方法命名用 `getAByB()` 或 `toXXX()`

## 特殊约定
- `lang/ex/` 包已废弃，使用 `lang/exception/` 替代
- `Constants.java` 的接口常量风格保持不动，新常量用 final class + private 构造器
- 缩进统一使用 4 空格，禁用 Tab

## 验证规范
- 任何提到 bug 或性能低的结论，必须通过时间复杂度分析 + 单元测试双重验证后才能给出
- 有高性能标记的方法/类（已验证），后续记录到项目资料 3-reference.md，不重复分析

## 注释
java注释中不允许使用 \<\> 这种HTML标签，可以使用markdown语法
## 历史记录
| 日期 | 内容 | 来源（上下文） |
|------|------|--------------|
| 2026-05-23 | lang/ex/ 是遗留重复包，以 lang/exception/ 为准 | myInit 扫描确认 |
| 2026-05-23 | 接口统一不使用 I 前缀/后缀，用 UpperCamelCase 名词/形容词 | myInit 扫描确认 |
| 2026-05-23 | Constants.java 接口常量保持现状；缩进用 4 空格 | myInit 扫描确认 |
| 2026-06-01 | StringUtils.doAppend 是优化过的高性能方法（FixedPoolHelper 复用 StringBuilder），无需性能分析 | 用户确认 |
