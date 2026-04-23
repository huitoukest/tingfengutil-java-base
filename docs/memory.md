# 项目记忆库

## 技术栈快照
- 语言/版本：Java 8+
- 主框架/版本：无外部框架，仅JDK + slf4j-log4j12
- 持久层/版本：无

## 项目结构约定
- 包结构：按功能模块划分（array/bean/cache/collection/common/concurrent/crypto/database/datetime/db/file/function/gis/io/lang/math/net/pool/text/validate/web）
- 命名规范：Utils后缀=静态工具类；Helper后缀=实例类；I后缀=接口

## 当前迭代需求列表
| ID | 断言描述 | 依赖 | 状态 |

## 用户习惯与偏好
- [Active] 200行可简至50行则重写 (2026/04/23)
- [Active] 只改必须改的，禁止顺手优化 (2026/04/23)
- [Active] 最小依赖，禁止引入JavaEE/第三方库 (2026/04/23)

## 项目级架构约束
- [Active] 统一抛出自定义RuntimeException，不抛检查型异常 (2026/04/23)
- [Active] 资源管理必须使用try-with-resources (2026/04/23)
- [Active] 上层不可依赖下层（common可被file/web依赖），禁止循环依赖 (2026/04/23)
- [Active] 工具类封闭：构造器私有，静态方法 (2026/04/23)
- [Active] 禁止自动提交，提交须用户手动触发 (2026/04/23)

## 模块级特殊准则
- FixedPoolHelper：DateUtils/RegexUtils/StringUtils等依赖此实现线程安全资源复用
- ConverterUtils：支持条件转换（ConditionConverter）和order优先级
- FunctionROne/FunctionRTwo：自定义函数接口，替代java.util.function支持多参数

## 可复用组件清单
| 组件/类 | 功能 | 位置 |
|---------|------|------|
| FixedPoolHelper | 对象池，clear时回调清理 | pool/base/FixedPoolHelper.java |
| StringTemplateHelper | 字符串模板替换 | text/StringTemplateHelper.java |
| ConverterUtils | 类型转换工厂 | bean/converter/ConverterUtils.java |
| FunctionROne/FunctionRTwo | 自定义函数接口 | common/inter/ |
| CollectionUtils | 集合转换/Join/拆分/打乱 | collection/CollectionUtils.java |
| StringUtils | 字符串处理/KMP/模板替换 | lang/StringUtils.java |
| DateUtils | 日期格式化，线程安全 | datetime/DateUtils.java |
| IOUtils | 流读写/拷贝 | io/IOUtils.java |
| FileUtils | 文件读写/异步拷贝 | file/FileUtils.java |
| RegExpUtils | 正则匹配，Pattern自动缓存 | text/RegExpUtils.java |

## 遗留问题与待办
- [ ]
