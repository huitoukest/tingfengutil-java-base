# Story — BeanUtils优化-05291430

**创建时间**：2026-05-29 14:30
**最后更新**：2026-05-29 14:30
**需求提出者**：架构师/开发者视角
**关联 tracking**：`.code/workspace/task/BeanUtils优化-05291430/tracking.md`

---

## 需求背景

### 业务场景

工具框架 `BeanUtils` 是 Java 工程中最常用的 Bean 处理库，贯穿以下典型场景：

- **DTO 映射**：将 Service 层 DO 转成 API 层 DTO，避免手动 get/set
- **配置对象复制**：将默认配置拷贝到实际运行环境
- **缓存对象克隆**：将缓存对象副本用于业务修改
- **对象池/原型模式**：通过拷贝创建新对象而非重新构建

### 当前痛点

| 痛点 | 影响 |
|------|------|
| 仅浅拷贝 | 嵌套对象共享引用，修改拷贝对象影响原对象 |
| Map 互转繁琐 | `toBean(Map)` 需手动 `new MapValueProvider(map)` |
| 泛型丢失 | `toList(List<UserDTO>, Class<User>)` 无法保证元素类型安全 |
| 性能一般 | 每次属性拷贝都反射，`getTargetPropertyType` 未缓存 |
| 无深拷贝 | 无法满足缓存克隆、对象池等场景 |

---

## 需求清单

### P0 — 必须实现

| # | 需求 | 验收标准 | 优先级 |
|---|------|---------|--------|
| S1 | **深拷贝支持** | `deepCopy(source)` 返回完全独立副本，循环引用检测通过 | 高 |
| S2 | **Map→Bean 直接转换** | `toBean(Map, Class)` 无需手动 new MapValueProvider | 高 |
| S3 | **Bean→Map 直接转换** | `toMap(Bean)` 一行调用，支持排除属性 | 高 |
| S4 | **copy 性能优化** | 超过 Spring 4.3.x 和 Hutool 5.x 默认性能（ JMH 基准测试） | 高 |

### P1 — 功能增强

| # | 需求 | 验收标准 | 优先级 |
|---|------|---------|--------|
| S5 | **泛型类型推断** | `toList(list, TypeReference<List<User>>>)` 保留泛型信息 | 中 |
| S6 | **Optional→Optional 转换** | `Optional<User>` → `Optional<UserDTO>` 转换器存在 | 中 |

### P2 — 未来规划（本次不实现）

| # | 需求 | 备注 |
|---|------|------|
| S7 | 链式属性拷贝 | `copyProperty(source, "user.address.city", target, "city")` 低优先级 |
| S8 | JSON ↔ Bean 转换 | 已有 fastjson，但非核心场景 |

---

## 设计约束

### 技术约束

| 约束 | 说明 |
|------|------|
| JDK 1.8+ | 不可使用 Java 15+ 的 Record/Sealed Class |
| 不改 pom.xml | 版本锁定，不升级 Lombok/SLF4J 等依赖 |
| 不新增外部依赖 | 已有 fastjson 可复用，不引入新依赖 |
| 不破坏现有 API | 兼容 `copyProperties(source, target)` 等已有方法 |

### 性能约束

| 指标 | 目标 |
|------|------|
| 浅拷贝性能 | 超越 Spring BeanUtils 20% 以上 |
| 深拷贝性能 | 不超过浅拷贝 3 倍 |
| 内存占用 | 不超过浅拷贝 2 倍 |

---

## 设计决策

| # | 决策 | 理由 |
|---|------|------|
| D1 | 深拷贝采用递归反射方案 | 可控性强、类型安全、循环引用易检测 |
| D2 | 循环引用用 IdentityHashMap 检测 | 相比序列化方案性能更优 |
| D3 | Map.toBean 支持递归嵌套转换 | 当 Map value 也是 Map 时，递归转为对应 Bean |
| D4 | 名称匹配默认大小写敏感 | 与 Java Bean 规范一致，可通过 CopyOptions 改为忽略 |

---

## 已知约束

- `lang/ex/` 包已废弃，使用 `lang/exception/` 替代
- 部分文件使用 Tab 缩进，新文件统一 4 空格
- 接口命名统一不使用 I 前缀/后缀（遗留如 IEnum/ConvertI 暂不改）
- `ReadWriteArrayList.removeIf()` 无效，使用索引逆序遍历替代

---

## 决策记录

| 日期 | 决策 | 结果 |
|------|------|------|
| 2026-05-29 | 深拷贝实现方案 | 递归反射 + IdentityHashMap 循环检测 |
| 2026-05-29 | Map.toBean 设计 | 支持递归嵌套，Map key→Bean 自动转换 |
| 2026-05-29 | 名称大小写 | 默认 ignoreCase=false，需精确匹配 |