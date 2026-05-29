# BeanUtils 修复计划 — 日志迁移 & BeanDesc API 优化

**创建时间**: 2026-05-29 14:40
**最后更新**: 2026-05-29 14:45
**状态**: 已完成

## 1. 问题分析报告

### 问题1：日志改用 SLF4J

| 维度 | 当前状态 | 目标状态 |
|------|---------|---------|
| 使用框架 | `commons-logging` (Log/LogFactory) | SLF4J (Lombok @Slf4j) |
| 是否已获依赖 | pom.xml 已声明 `slf4j-log4j12 1.7.25` (provided) | 无需新增依赖 |
| Lombok 支持 | pom.xml 已声明 `lombok 1.18.24` (provided) | 无需新增依赖 |

**涉及文件**：
- `BeanUtils.java` — 第 9-10 行导入 `org.apache.commons.logging.Log`、`LogFactory`；第 38 行定义 `private static final Log logger = ...`
- `BeanCopier.java` — 第 7-8 行导入同上；第 28 行定义 `private static final Log logger = ...`

### 问题2：通过 Lombok @Slf4j 替代手写日志

当前两个文件都手写了 `private static final Log logger = LogFactory.getLog(Xxx.class)`。改用 `@Slf4j` 后，Lombok 自动生成 `private static final Logger log = LoggerFactory.getLogger(Xxx.class)`。

**兼容性检查**：SLF4J 的 `Logger.debug(String, Throwable)` 签名与现有的 `log.debug("msg", e)` 调用完全兼容，无需修改日志调用语句。

### 问题3：BeanDesc 属性访问返回结果而非抛异常

**现状分析**：

```java
// BeanDesc.java 第 175 行
public Object getPropertyValue(Object bean, String name) {
    // ... 尝试 getter ...
    // ... 尝试 field ...
    throw new BaseException("Property not found: " + name);  // ← 属性不存在时抛异常
}

// BeanDesc.java 第 206 行
public void setPropertyValue(Object bean, String name, Object value) {
    // ... 尝试 setter ...
    // ... 尝试 field ...
    throw new BaseException("Property not found: " + name);  // ← 属性不存在时抛异常
}
```

**问题**：使用异常（Exception）来控制流程（property not found 是预期可能发生的场景，不是异常情况）。所有调用者都需要 try-catch 来绕过。

**解决方案**：
- 新增 `PropertyAccessMode` 枚举表示访问方式
- 新增 `PropertyResult<T>` 类封装存在性、值和访问方式
- `getPropertyValue` 改为返回 `PropertyResult<T>`，属性不存在时返回 `exists=false` 而不是抛异常
- `setPropertyValue` 在属性不存在时静默跳过（不再抛异常）

### 问题4：deepCopyBean 异常处理优化

**现状**：
```java
try {
    Object value = desc.getPropertyValue(source, propName);
    Object copiedValue = deepCopy(value, visited);
    desc.setPropertyValue(target, propName, copiedValue);
} catch (BaseException e) {
    if (logger.isDebugEnabled()) {
        logger.debug("Failed to deep copy property: " + propName + ", skipping", e);
    }
}
```

**修复后**：
- `getPropertyValue` 不再抛异常 → 移除 try-catch
- 使用 `PropertyResult` 检查属性是否存在 → 不存在则 `continue`
- `setPropertyValue` 在属性不存在时静默跳过 → 无需处理
- 极少数 IllegalAccessException 情况向上传播（让调用方知晓）

## 2. 调用点分析

### getPropertyValue 调用点（共 5 处）

| # | 文件 | 行号 | 当前处理方式 | 修复方案 |
|---|------|------|-------------|---------|
| 1 | BeanCopier.java:107 | copy() | try-catch catching BaseException → continue | 改为 PropertyResult.exists 判断 |
| 2 | BeanCopier.java:378 | toMap() | try-catch catching BaseException → continue | 改为 PropertyResult.exists 判断 |
| 3 | BeanUtils.java:392 | isEmpty() | 无 try-catch（期望属性都存在） | 改为 PropertyResult.exists 判断 |
| 4 | BeanUtils.java:599 | deepCopyBean() | try-catch catching BaseException | 移除 try-catch，改为 PropertyResult 判断 |
| 5 | BeanUtils.java:630 | hasNullField() | 无 try-catch（期望属性都存在） | 改为 PropertyResult.exists 判断 |

### setPropertyValue 调用点（共 3 处）

| # | 文件 | 行号 | 当前处理方式 | 修复方案 |
|---|------|------|-------------|---------|
| 1 | BeanCopier.java:152 | copy() | try-catch catching BaseException → log debug | setPropertyValue 不再抛PropertyNotFound，保留catch处理真实异常 |
| 2 | BeanCopier.java:278 | copyFromProvider() | try-catch catching BaseException → log debug | 同上 |
| 3 | BeanUtils.java:601 | deepCopyBean() | 在 try-catch 块内 | 移除外层 try-catch（setPropertyValue 不再抛PropertyNotFound） |

## 3. 设计文档

### 3.1 实体关系

```
PropertyAccessMode (enum)
├── GETTER_METHOD  — 通过 getter 方法访问
├── FIELD_ACCESS   — 直接字段访问
└── SETTER_METHOD  — 通过 setter 方法访问

PropertyResult<T>
├── exists: boolean        — 属性是否存在
├── value: T               — 属性值
└── mode: PropertyAccessMode — 访问方式

BeanDesc (uses)
├── → PropertyResult<T> (getPropertyValue 返回)
├── → PropertyAccessMode (标记访问方式)
└── → setPropertyValue 不再抛出 PropertyNotFound
```

### 3.2 API 设计变更

#### PropertyAccessMode（新建，`bean/copier/` 包）

```java
public enum PropertyAccessMode {
    GETTER_METHOD,  // 通过 getter 方法读取
    FIELD_ACCESS,   // 通过字段直接读取/写入
    SETTER_METHOD   // 通过 setter 方法写入
}
```

#### PropertyResult<T>（新建，`bean/copier/` 包）

```java
public class PropertyResult<T> {
    private final boolean exists;
    private final T value;
    private final PropertyAccessMode mode;

    // 静态工厂方法
    public static <T> PropertyResult<T> found(T value, PropertyAccessMode mode);
    public static <T> PropertyResult<T> notFound();

    // getter
    public boolean isExists();
    public T getValue();
    public PropertyAccessMode getMode();
}
```

#### BeanDesc API 变更

| 方法 | 原签名 | 新签名 | 行为变化 |
|------|--------|--------|---------|
| getPropertyValue | `Object getPropertyValue(Object bean, String name)` throws BaseException | `<T> PropertyResult<T> getPropertyValue(Object bean, String name)` | 属性不存在时返回 `PropertyResult.notFound()` 而非抛异常；新增访问方式追踪 |
| setPropertyValue | `void setPropertyValue(Object bean, String name, Object value)` throws BaseException | `void setPropertyValue(Object bean, String name, Object value)` (签名不变) | 属性不存在时静默跳过，不再抛 `BaseException("Property not found")`；`IllegalAccessException` 仍包装为 BaseException |

### 3.3 调用方伪代码变更

#### BeanCopier.copy() — property 读取
```
// 原代码
try {
    value = sourceDesc.getPropertyValue(source, sourceFieldName);
} catch (BaseException e) {
    continue;
}

// 新代码
PropertyResult<?> result = sourceDesc.getPropertyValue(source, sourceFieldName);
if (!result.isExists()) {
    continue;
}
value = result.getValue();
```

#### BeanCopier.toMap() — property 读取
```
// 原代码
try {
    value = desc.getPropertyValue(bean, propName);
} catch (BaseException e) {
    continue;
}

// 新代码
PropertyResult<?> result = desc.getPropertyValue(bean, propName);
if (!result.isExists()) {
    continue;
}
value = result.getValue();
```

#### BeanUtils.isEmpty() / hasNullField()
```
// 原代码
Object value = BeanCopier.getOrCreateBeanDesc(bean.getClass()).getPropertyValue(bean, name);

// 新代码
PropertyResult<?> result = BeanCopier.getOrCreateBeanDesc(bean.getClass()).getPropertyValue(bean, name);
Object value = result.isExists() ? result.getValue() : null;
```

#### BeanUtils.deepCopyBean()
```
// 原代码
try {
    Object value = desc.getPropertyValue(source, propName);
    Object copiedValue = deepCopy(value, visited);
    desc.setPropertyValue(target, propName, copiedValue);
} catch (BaseException e) {
    if (logger.isDebugEnabled()) {
        logger.debug("Failed to deep copy property: " + propName + ", skipping", e);
    }
}

// 新代码
PropertyResult<?> propResult = desc.getPropertyValue(source, propName);
if (!propResult.isExists()) {
    continue;
}
Object value = propResult.getValue();
Object copiedValue = deepCopy(value, visited);
desc.setPropertyValue(target, propName, copiedValue);
```

### 3.4 日志迁移

| 文件 | 当前 | 替换为 |
|------|------|--------|
| BeanUtils.java | `import org.apache.commons.commons.logging.Log;` + `import org.apache.commons.commons.logging.LogFactory;` + `private static final Log logger = LogFactory.getLog(BeanUtils.class);` | `import lombok.extern.slf4j.Slf4j;` + `@Slf4j` 类注解 |
| BeanCopier.java | 同上 | 同上 |

所有 `logger.xxx()` 调用改为 `log.xxx()`（Lombok @Slf4j 默认字段名为 `log`）。

## 4. 核心边界条件与异常处理策略

| 场景 | 处理策略 |
|------|---------|
| `getPropertyValue(null, name)` | 保留现有行为：NPE 由反射 API 抛出（调用方负责传非空 bean） |
| `getPropertyValue(bean, null)` | 保留现有行为：NPE（调用方负责传非空 name） |
| `getPropertyValue(bean, "nonexistent")` | 返回 `PropertyResult.notFound()`，不再抛异常 |
| `setPropertyValue(bean, "nonexistent", value)` | 静默跳过，不再抛异常 |
| `setPropertyValue(bean, name, value)` 字段访问被拒绝 | 仍然包装为 `BaseException("Failed to set property value: ...")` 抛出 |
| `getPropertyValue` getter 调用失败降级到 field | 保持现有降级行为不变 |
| `deepCopyBean` 中 setPropertyValue 失败 | 移除 try-catch 后，异常向上传播（视为严重错误） |

## 5. 子任务分解

### 评分表

| 功能 | 参数 | 逻辑 | 依赖 | 边界 | 产出 | 状态 | 异常 | 外部 | 总分 | 级别 |
|------|------|------|------|------|------|------|------|------|------|------|
| F1: @Slf4j BeanUtils | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 8 | 极简 |
| F2: @Slf4j BeanCopier | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 8 | 极简 |
| F3: PropertyAccessMode | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 8 | 极简 |
| F4: PropertyResult<T> | 2 | 1 | 2 | 2 | 1 | 1 | 1 | 1 | 11 | 极简 |
| F5: getPropertyValue 返回 PropertyResult | 2 | 4 | 2 | 3 | 2 | 1 | 2 | 1 | 17 | 简单 |
| F6: setPropertyValue 不再抛PropertyNotFound | 2 | 3 | 1 | 3 | 1 | 1 | 2 | 1 | 14 | 极简 |
| F7: BeanCopier.copy() 适配 | 2 | 2 | 3 | 2 | 2 | 1 | 2 | 1 | 15 | 极简 |
| F8: BeanCopier.toMap() 适配 | 1 | 2 | 3 | 2 | 1 | 1 | 2 | 1 | 13 | 极简 |
| F9: BeanUtils 调用点适配 | 2 | 2 | 3 | 2 | 1 | 1 | 1 | 1 | 13 | 极简 |
| F10: deepCopyBean 简化 | 1 | 2 | 3 | 2 | 2 | 1 | 2 | 1 | 14 | 极简 |

### 合并分组

| Task | 包含功能 | 级别 | 文件数 | 估算行数 | 依赖 |
|------|---------|------|--------|---------|------|
| Task 0 | F3 + F4 (PropertyAccessMode + PropertyResult) | 共享类 | 2 (新建) | ~80 | 无 |
| Task 1 | F5 + F6 (BeanDesc API 变更) | 极简 | 1 (修改) | ~50 | Task 0 |
| Task 2 | F2 + F7 + F8 (BeanCopier 日志 + 适配) | 极简 | 1 (修改) | ~80 | Task 1 |
| Task 3 | F1 + F9 + F10 (BeanUtils 日志 + 适配 + 简化) | 极简 | 1 (修改) | ~80 | Task 1 |

**合并验证：**
- Task 0: 共享数据类规则 → 自动提取，不评分
- Task 1: 同一文件 BeanDesc.java，条件A✓ 条件B✓ 条件C(1文件≤6, ~50行≤500)✓
- Task 2: 同一文件 BeanCopier.java，条件A✓ 条件B✓ 条件C(1文件≤6, ~80行≤500)✓
- Task 3: 同一文件 BeanUtils.java，条件A✓ 条件B✓ 条件C(1文件≤6, ~80行≤500)✓

### DAG

```
Task 0 (PropertyAccessMode + PropertyResult)
  ↓
Task 1 (BeanDesc API 变更)
  ├──→ Task 2 (BeanCopier 适配)
  └──→ Task 3 (BeanUtils 适配)
```

### 功能清单

#### Task 0：共享数据类
- **类型**: coding
- **涉及文件**:
  - `src/main/java/com/tingfeng/util/java/base/bean/copier/PropertyAccessMode.java` (新建)
  - `src/main/java/com/tingfeng/util/java/base/bean/copier/PropertyResult.java` (新建)
- **依赖**: 无
- **查找指引**: 参考 `common/constant/` 中已有枚举风格，参考 POJO 风格

#### Task 1：BeanDesc API 变更
- **类型**: coding
- **涉及文件**:
  - `src/main/java/com/tingfeng/util/java/base/bean/copier/BeanDesc.java` (修改)
- **依赖**: Task 0
- **查找指引**: 参考现有 BeanDesc.java 第 175-196 行 (getPropertyValue)、第 206-229 行 (setPropertyValue)

#### Task 2：BeanCopier 日志迁移 + API 适配
- **类型**: coding
- **涉及文件**:
  - `src/main/java/com/tingfeng/util/java/base/bean/copier/BeanCopier.java` (修改)
- **依赖**: Task 1
- **查找指引**: 参考现有 BeanCopier.java 第 7-8 行 (commons-logging 导入)、第 28 行 (logger 定义)、第 106-111 行 (copy getPropertyValue)、第 151-158 行 (copy setPropertyValue)、第 277-284 行 (copyFromProvider setPropertyValue)、第 377-382 行 (toMap getPropertyValue)

#### Task 3：BeanUtils 日志迁移 + API 适配 + deepCopyBean 简化
- **类型**: coding
- **涉及文件**:
  - `src/main/java/com/tingfeng/util/java/base/bean/BeanUtils.java` (修改)
- **依赖**: Task 1
- **查找指引**: 参考现有 BeanUtils.java 第 9-10 行 (commons-logging 导入)、第 38 行 (logger 定义)、第 392 行 (isEmpty)、第 599-607 行 (deepCopyBean)、第 630 行 (hasNullField)

## 6. 门控标准

### Task 0 门控（共享数据类）
- [ ] 编译通过 (`mvn compile`)
- [ ] PropertyAccessMode 包含 3 个枚举常量：GETTER_METHOD, FIELD_ACCESS, SETTER_METHOD
- [ ] PropertyResult 包含 exists/value/mode 三个字段
- [ ] PropertyResult 提供 found()/notFound() 静态工厂方法
- [ ] 私有构造器 + getter 方法（不可变对象）

### Task 1 门控
- [ ] 编译通过
- [ ] `getPropertyValue` 签名改为 `<T> PropertyResult<T> getPropertyValue(Object bean, String name)`
- [ ] 属性存在时返回 `PropertyResult.found(value, mode)`，mode 正确标记 GETTER_METHOD 或 FIELD_ACCESS
- [ ] 属性不存在时返回 `PropertyResult.notFound()`，不抛异常
- [ ] `setPropertyValue` 在属性不存在时静默跳过，不再抛 `BaseException("Property not found")`
- [ ] `setPropertyValue` 在 `IllegalAccessException` 时仍包装为 `BaseException` 抛出
- [ ] 原有功能测试通过 (`mvn test -Dtest=BeanCopierTest`)

### Task 2 门控
- [ ] 编译通过
- [ ] `BeanCopier.java` 移除 `commons-logging` 导入，使用 `@Slf4j` 注解
- [ ] 所有 `logger.xxx()` 改为 `log.xxx()`
- [ ] `copy()` 中的 `getPropertyValue` try-catch 替换为 `PropertyResult.exists` 判断
- [ ] `toMap()` 中的 `getPropertyValue` try-catch 替换为 `PropertyResult.exists` 判断
- [ ] `setPropertyValue` 的 try-catch 保留（处理真实异常如 IllegalAccessException）
- [ ] BeanCopier 测试通过 (`mvn test -Dtest=BeanCopierTest`)

### Task 3 门控
- [ ] 编译通过
- [ ] `BeanUtils.java` 移除 `commons-logging` 导入，使用 `@Slf4j` 注解
- [ ] 所有 `logger.xxx()` 改为 `log.xxx()`
- [ ] `isEmpty()` 中的 `getPropertyValue` 改为 `PropertyResult.exists` 判断
- [ ] `hasNullField()` 中的 `getPropertyValue` 改为 `PropertyResult.exists` 判断
- [ ] `deepCopyBean()` 移除 try-catch，改为 `PropertyResult.exists` 判断
- [ ] 全部测试通过 (`mvn test`)

### 总体门控（≥98%）
- [ ] **覆盖检验**：4 个问题全部有对应的设计方案
- [ ] **目标对齐**：所有变更服务于「日志统一 + 异常流优化」目标
- [ ] **可行检验**：Lombok + SLF4J 已在 pom.xml 中；API 变更类型安全；所有调用点已排查
- [ ] **完整性检验**：4 个 Task 的涉及文件、查找指引、依赖关系均明确
