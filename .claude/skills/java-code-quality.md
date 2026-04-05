# Java 代码优化 Skill

## 优先级顺序（1 > 2 > 3 > 4 > 5）

### 1. 规范与质量
- 整理并遵循此方法可能涉及的 Java 规范及功能相关标准
- 完善工具类（补全场景、修复 Bug/性能）
- 添加注释及完备单元测试

### 2. 复用优先
- 优先使用项目现有工具类/常量
- 禁止重复实现或使用魔法数字/字符串
- 先查看 `src/main/java/com/tingfeng/util/java/base/common/utils/` 下是否有可复用工具

### 3. 代码清理
- 移除无用引入/代码
- 修正语法错误

### 4. 特性优化
- 在性能不降且不违反第 2 点（现有工具优先）前提下
- 使用 Java 8 特性（Lambda/try-with-resources）简化冗余代码

### 5. 设计先行
- 禁止面向测试编程（TDD）
- 须先设计方法签名（参数/返回/异常），再实现逻辑，最后编写测试

## 工具选用分析规范（重要）

所有与项目现有工具重叠的系统功能，都应进行性能对比分析，不能一概而论。

### 分析步骤

| 步骤 | 操作 | 说明 |
|------|------|------|
| 1 | 查项目工具 | 先查 `common/utils/` 下是否有对应工具 |
| 2 | 性能对比 | 分析时间复杂度、内存占用、并发安全 |
| 3 | 决策 | 差异小 → 用项目工具；项目工具明显更优 → 用项目工具；系统 API 明显更优 → 记录原因后用系统 API |

### 差异小的判断标准
- 执行时间差异 < 5%（经验值）
- 内存占用差异可忽略
- 功能完全等价

### 不适用"项目优先"的例外（须记录原因）
- 项目工具未覆盖的场景（如 `java.nio.file.Files.walkFileTree()`）
- 系统 API 有重大性能优势（如 `String.getBytes(Charset)` 底层直接调用）
- 涉及 JDK 底层优化（如 `Collections.unmodifiableMap` 内部直接返回固定对象）

### 关键工具性能说明

| 类别 | 工具类 | 主要优化点 |
|------|--------|-----------|
| **字符串** | `StringUtils` | `FixedPoolHelper<StringBuilder>` 池化复用（16个实例循环使用），KMP搜索，字符宽度计算 |
| **集合** | `CollectionUtils` | 池化 StringBuilder 连接，自定义洗牌算法 |
| **日期** | `DateUtils` | `ConcurrentHashMap` + `FixedPoolHelper` 缓存 SimpleDateFormat，每种格式8-N个实例，线程安全 |
| **数组** | `ArrayUtils` | 双指针原地反转，池化 StringBuilder 连接，对所有基础类型提供 shuffle/reverse |
| **数学** | `MathUtils` | 防溢出 LCM，GCD/Euclid 算法，任意进制(2-256)转换 |
| **随机** | `RandomUtils` | 底层用 `ThreadLocalRandom`，可配置字符集随机字符串，独有洗牌偏移量算法 |
| **反射** | `ReflectUtils` / `ClassUtils` / `MethodUtils` | 反射结果缓存，方法调用优化 |
| **线程** | `ThreadUtils` | `NamedThreadFactory` 线程工厂 |

### 典型场景对比

| 场景 | 项目工具 | 系统 API | 推荐 |
|------|----------|----------|------|
| 字符串拼接(<1KB) | `StringUtils.append()` | `StringBuilder` | 项目工具（池化复用） |
| 日期格式化 | `DateUtils.format()` | `SimpleDateFormat` | 项目工具（线程安全） |
| 集合连接字符串 | `CollectionUtils.join()` | `String.join()` | 项目工具（池化） |
| 数组反转 | `ArrayUtils.reverse()` | `Collections.reverse()` 需转List | 项目工具（原地反转） |
| 数组打乱 | `ArrayUtils.shuffle()` | `Collections.shuffle()` | 项目工具（可控次数） |
| 基础数学 | `Math` | `java.lang.Math` | 系统 API（无优化空间） |

## 使用方法

在代码优化任务中引用此 skill，或在实现新功能前加载此规则集。

优先使用项目工具，但在以下情况可选用系统 API（须记录原因）：
1. 项目工具未覆盖该场景
2. 系统 API 有明显性能优势
3. 涉及 JDK 底层优化

## 项目关键工具类位置

- 字符串工具: `common/utils/string/StringUtils.java`
- 日期工具: `common/utils/datetime/DateUtils.java`, `LocalDateUtils.java`
- 集合工具: `common/utils/CollectionUtils.java`, `ArrayUtils.java`
- 反射工具: `common/utils/reflect/ReflectUtils.java`, `ClassUtils.java`, `GenericsUtils.java`
- 文件工具: `file/FileUtils.java`, `file/csv/`
- 树结构: `common/bean/TreeNode.java`, `common/bean/GenericTreeNode.java`
- 池化帮助: `common/helper/PoolHelper.java`
- 数学工具: `common/utils/MathUtils.java`
- 随机工具: `common/utils/RandomUtils.java`
- 线程工具: `common/utils/ThreadUtils.java`