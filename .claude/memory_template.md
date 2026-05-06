# 项目记忆库模板

## 技术栈快照
- 语言/版本：
- 主框架/版本：
- 持久层/版本：

## 项目结构约定
- 包结构：
- 命名规范：

## 当前迭代需求列表
| ID | 断言描述 | 依赖 | 状态 |

## 用户习惯与偏好
- [Active] 描述 (记录日期)
- [Deprecated] 描述 (记录日期)

## 项目级架构约束
- [Active] 接口统一返回格式等

## 模块级特殊准则
- 模块名：[准则描述]

## Converter 转换器规范
- **分类原则**：同一源类型类别 → 转为其它类型
- **转换方向限制**：
  - Long/Integer → 时间类型 → `NumberConverters`
  - Date/LocalDateTime/Duration/Period → 其它类型 → `DateTimeConverters`
- **时间转字符串默认格式**：`yyyy-MM-dd HH:mm:ss`
- **Optional 转换器**：仅提供拆箱功能（`Optional<T> → T`）

## 可复用组件清单
| 组件/类 | 功能 | 位置 |

## 遗留问题与待办
- [ ]
