# BeanUtils 完善优化 V4 设计方案

- 创建时间: 2026-05-29 20:30
- 最后更新: 2026-05-29 20:31
- 状态: 已完成

## 1. 需求概要

基于用户确认的 V4 五项需求：

| # | 需求 | 说明 |
|---|------|------|
| 1 | 全面优化 | 性能 + 功能 + API 综合提升 |
| 2 | 深拷贝跳过 transient 字段 | `deepCopyBean()` 中跳过 transient 修饰的属性 |
| 3 | 明确跳过 static 字段 | 显式跳过 static 修饰的属性（即使 pdMap 已过滤，add 显式检查） |
| 4 | CommonType 接口映射枚举 | 通用的接口→实现类映射枚举，用于反射创建场景 |
| 5 | 不清理无用 API | 保持所有现有 API 不动 |

## 2. 现状分析

### 2.1 BeanDesc 现有字段过滤

```java
// BeanDesc.introspect() 第 149 行
java.util.List<Field> fields = ReflectUtils.getFields(clazz, false, false, true, true);
// isContainsStatic=false → static 字段已被过滤出 fieldMap
// isFinal=false → final 字段已被过滤出 fieldMap
```

- **fieldMap**：已排除 static、final 字段
- **pdMap**：来自 `Introspector.getBeanInfo()`，通常不含静态属性，但 `transient` 字段若存在 getter/setter 仍会进入 pdMap
- **`getPropertyNames()`** 返回 `pdMap ∪ fieldMap`，`deepCopyBean()` 遍历此集合

### 2.2 当前 deepCopyBean 不检查 transient/static

`deepCopyBean()` 第 686-723 行：遍历 `getPropertyNames()`，对 `"class"` 属性进行过滤，但未对 transient/static 字段做显式检查。

### 2.3 currentClassPropertyNames 读取

`collectCurrentClassPropertyNames()` 第 91-111 行从 pdMap 和 fieldMap 筛选声明类为当前类的属性。

### 2.4 BeanCopier 热点

`copyProperties()` 第 179 行和第 204 行两次调用 `getTargetPropertyType(targetDesc, propName)`，可提取为局部变量。

## 3. 实体关系图

```
BeanUtils (门面类)
├── deepCopy(T source)
├── deepCopy(T source, int maxDepth)
├── deepCopy(T source, int remainingDepth, IdentityHashMap visited)  [内部]
├── deepCopyBean(...)                   ← 增加 transient/static 检查 + CommonType 支持
├── deepCopyCollection(...)             ← 增加 CommonType 回退
├── deepCopyMap(...)                    ← 增加 CommonType 回退
├── deepCopyArray(...)                  ← 不变
└── isImmutableType(Object)

BeanDesc (属性描述缓存)
├── pdMap: Map<String, PropertyDescriptor>   ← 有 getter/setter 的属性
├── fieldMap: Map<String, Field>             ← 所有字段（已过滤 static/final）
├── getPropertyNames()                       ← pdMap ∪ fieldMap
├── getPropertyType(String)                  ← 属性类型
└── getField(String) [NEW]                   ← 新增：根据属性名获取 Field

CommonType [NEW] (接口→实现类映射枚举)
├── LIST, SET, MAP, COLLECTION, ...
├── resolveImplementation(Class<?>)         ← 根据接口获取实现类
└── resolveImplementationOrDefault(Class<?>, Class<?>)  ← 带默认值

BeanCopier
└── copyProperties(...)                      ← 性能优化：外提 getTargetPropertyType
```

## 4. 核心设计

### 4.1 CommonType 枚举设计

位于 `bean/base/CommonType.java`，提供通用接口→实现类映射。

```java
public enum CommonType {
    LIST(List.class, ArrayList.class),
    SET(Set.class, HashSet.class),
    MAP(Map.class, HashMap.class),
    COLLECTION(Collection.class, ArrayList.class),
    ITERABLE(Iterable.class, ArrayList.class),
    QUEUE(Queue.class, LinkedList.class),
    DEQUE(Deque.class, ArrayDeque.class),
    SORTED_SET(SortedSet.class, TreeSet.class),
    NAVIGABLE_SET(NavigableSet.class, TreeSet.class),
    SORTED_MAP(SortedMap.class, TreeMap.class),
    NAVIGABLE_MAP(NavigableMap.class, TreeMap.class),
    ;

    private final Class<?> interfaceType;
    private final Class<?> implementationClass;

    // 构造器、getter

    /**
     * 根据类型查找对应的实现类。
     * 支持接口类型和抽象类的映射查找，使用 isAssignableFrom 匹配。
     */
    public static Class<?> resolveImplementation(Class<?> type) { ... }

    /**
     * 根据类型查找对应的实现类，带默认值。
     */
    public static Class<?> resolveImplementationOrDefault(Class<?> type, Class<?> defaultImpl) { ... }
}
```

**映射规则**：遍历枚举成员，使用 `interfaceType.isAssignableFrom(type)` 判断，返回第一个匹配的实现类。

### 4.2 deepCopyBean 字段过滤

在 `deepCopyBean()` 中，对每个属性名获取对应的 Field 并检查修饰符：

```java
for (String propName : desc.getPropertyNames()) {
    if ("class".equals(propName)) continue;

    // --- 新增：跳过 transient / static 字段 ---
    Class<?> propType = desc.getPropertyType(propName);
    if (propType == null) continue;

    // 前序步骤：获取 Field 并检查修饰符
    // 使用 BeanDesc.getField(propName) 获取字段信息
    // 如果是 transient 或 static → continue
    // --- 新增结束 ---

    PropertyResult<Object> propResult = desc.getPropertyValue(source, propName);
    ...
}
```

新增 `BeanDesc.getField(String propName)` 方法，返回字段信息（从 fieldMap 获取），供 `deepCopyBean` 检查修饰符。

### 4.3 CommonType 在 deepCopy 中的使用

**场景 A：deepCopyCollection 回退**

当 `source.getClass().getDeclaredConstructor().newInstance()` 失败时（如不可变集合），尝试 CommonType：

```java
try {
    Collection<Object> instance = source.getClass().getDeclaredConstructor().newInstance();
    result = instance;
} catch (Exception e) {
    // 新增：使用 CommonType 回退
    Class<?> implClass = CommonType.resolveImplementation(source.getClass());
    if (implClass != null && Collection.class.isAssignableFrom(implClass)) {
        result = (Collection<Object>) implClass.getDeclaredConstructor().newInstance();
    } else {
        result = new ArrayList<>(source.size());
    }
}
```

**场景 B：deepCopyMap 回退**

类似 Collection，Map 回退时先尝试 CommonType：

```java
try {
    Map<Object, Object> instance = source.getClass().getDeclaredConstructor().newInstance();
    result = instance;
} catch (Exception e) {
    Class<?> implClass = CommonType.resolveImplementation(source.getClass());
    if (implClass != null && Map.class.isAssignableFrom(implClass)) {
        result = (Map<Object, Object>) implClass.getDeclaredConstructor().newInstance();
    } else {
        result = new java.util.HashMap<>(source.size());
    }
}
```

**场景 C：deepCopyBean 接口属性初始化**

当 Bean 属性的值为 null，且属性类型是 Collection/Map 等接口时，使用 CommonType 创建空实例：

```java
Object value = propResult.getValue();
if (value == null) {
    // 新增：接口属性使用 CommonType 创建空实例
    Class<?> propType = desc.getPropertyType(propName);
    if (propType != null && propType.isInterface()) {
        Class<?> implClass = CommonType.resolveImplementation(propType);
        if (implClass != null) {
            try {
                value = implClass.getDeclaredConstructor().newInstance();
            } catch (Exception ignored) {}
        }
    }
    if (value == null) continue; // 仍为 null 则跳过
}
```

### 4.4 BeanCopier 性能优化

`copyProperties()` 中 `getTargetPropertyType` 被重复调用：

```java
// 第 179 行（外循环）：用于 resolveSourceFieldName 的同类型匹配
Class<?> propertyType = getTargetPropertyType(targetDesc, propName);

// 第 204 行（内部 if 块）：用于类型转换判断
Class<?> targetType = getTargetPropertyType(targetDesc, propName);
```

两次调用参数完全相同，提取为循环变量即可消除重复查询：

```java
Class<?> targetPropertyType = getTargetPropertyType(targetDesc, propName);
// 使用 targetPropertyType 替换所有 getTargetPropertyType(targetDesc, propName) 调用
```

此外，Converter 查询也可做微小优化：`findConverters` 结果可缓存复用，避免重复查询 registry。

## 5. API 设计

### 5.1 CommonType （新增文件）

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `resolveImplementation(Class<?>)` | type | Class<?> / null | 根据类型查找实现类 |
| `resolveImplementationOrDefault(Class<?>, Class<?>)` | type, defaultImpl | Class<?> | 带默认值的查找 |
| `getInterfaceType()` | — | Class<?> | 枚举成员对应的接口类型 |
| `getImplementationClass()` | — | Class<?> | 枚举成员对应的实现类 |

### 5.2 BeanDesc （新增方法）

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `getField(String propName)` | propName | Field / null | 根据属性名获取 Field |

### 5.3 BeanUtils.deepCopy （内部修改，接口不变）

| 方法 | 变更 | 说明 |
|------|------|------|
| `deepCopy(T)` | 无变更 | 委托给三参数版本 |
| `deepCopy(T, int)` | 无变更 | 委托给三参数版本 |
| `deepCopyBean(...)` | 增强 | 跳过 transient/static + CommonType 接口实例化 |
| `deepCopyCollection(...)` | 增强 | CommonType 回退 |
| `deepCopyMap(...)` | 增强 | CommonType 回退 |

### 5.4 BeanCopier （性能优化）

| 方法 | 变更 | 说明 |
|------|------|------|
| `copyProperties(...)` | 优化 | getTargetPropertyType 从 2 次减为 1 次 |

## 6. 状态转换表

deepCopyBean 属性处理状态：

```
PropName → "class" 吗？
  ├─ Yes → continue（跳过）
  └─ No  → 检查 Field 修饰符
              ├─ transient 或 static → continue（新增跳过）
              └─ 非 transient 且 非 static
                   → 获取属性值
                   → 值是否为 null？
                       ├─ Yes → 属性类型是接口吗？
                       │    ├─ Yes → CommonType 解析 → 创建空实例
                       │    └─ No  → continue
                       └─ No  → deepCopy 递归
```

## 7. 核心边界条件与异常处理

| 边界 | 处理方式 |
|------|---------|
| transient 字段 | `deepCopyBean` 中显式跳过 |
| static 字段 | `deepCopyBean` 中显式跳过（双重保障） |
| fieldMap 中无对应 Field（仅 pdMap 属性） | 不检查修饰符，不跳过（兼容虚拟属性） |
| 接口属性值为 null | 使用 CommonType 创建空实例（如 List→ArrayList） |
| 接口属性在 CommonType 中无映射 | 保持 null 不变 |
| CommonType 映射的实现类无默认构造器 | 捕获异常，跳过该属性 |
| deepCopyCollection 实例化失败（不可变集合） | CommonType 回退 → ArrayList 兜底 |
| deepCopyMap 实例化失败 | CommonType 回退 → HashMap 兜底 |
| 已有 API 不受影响 | 新增逻辑不改变现有方法签名和行为 |

## 8. 涉及文件清单

| 文件 | 用途 | 变更类型 |
|------|------|---------|
| `bean/base/CommonType.java` | 接口→实现类映射枚举 | **create** |
| `bean/copier/BeanDesc.java` | 新增 getField() 方法 | modify |
| `bean/BeanUtils.java` | deepCopy 增强 | modify |
| `bean/copier/BeanCopier.java` | 性能优化 | modify |
| `bean/copier/CopyOptions.java` | 无变更 | — |

## 9. 子任务分解清单

### Task 评分

| 功能 | 参数 | 逻辑 | 依赖 | 边界 | 产出 | 状态 | 异常 | 外部 | 总分 | 级别 |
|------|------|------|------|------|------|------|------|------|------|------|
| **SubStory 1: CommonType 枚举** |
| 1.1 CommonType 枚举定义 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 8 | 极简 |
| 1.2 resolveImplementation 方法 | 1 | 2 | 1 | 1 | 1 | 1 | 1 | 1 | 9 | 极简 |
| **SubStory 2: BeanUtils deepCopy 增强** |
| 2.1 BeanDesc.getField 新增 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 8 | 极简 |
| 2.2 deepCopyBean transient/static 跳过 | 1 | 2 | 1 | 2 | 1 | 1 | 1 | 1 | 10 | 极简 |
| 2.3 deepCopyBean CommonType 集成 | 1 | 2 | 1 | 2 | 1 | 1 | 1 | 1 | 10 | 极简 |
| 2.4 deepCopyCollection CommonType 回退 | 1 | 2 | 1 | 1 | 1 | 1 | 1 | 1 | 9 | 极简 |
| 2.5 deepCopyMap CommonType 回退 | 1 | 2 | 1 | 1 | 1 | 1 | 1 | 1 | 9 | 极简 |
| **SubStory 3: BeanCopier 性能优化** |
| 3.1 getTargetPropertyType 外提 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 8 | 极简 |

**合并判断：**
- **SubStory 1**（极简 × 2）：同属一个新文件创建，总分 8+9=17 → 简单级别，3-4 个功能可合。但这是新增文件（Task 0 共享类类型），自动作为独立子任务，不合并。
- **SubStory 2**（极简 × 4 + 1 个 BeanDesc 修改）：所有功能处理同一实体 BeanUtils.deepCopy，引用相同文件（BeanUtils.java + BeanDesc.java），合并后总文件数 ≤ 6，估算行数 ≤ 200 → 合并为 1 个 Task
- **SubStory 3**（极简 × 1）：独立文件 BeanCopier.java，独立 Task

### 功能清单

#### Task 0: CommonType 枚举（共享数据类）
- **功能名**: CommonType 枚举定义与实现
- **模块文件**: `src/main/java/com/tingfeng/util/java/base/bean/base/CommonType.java`
- **依赖**: 无
- **查找指引**: 参考现有枚举如 `bean/converter/ConverterConstants.java`、`common/constant/` 下的常量模式

#### Task 1: deepCopy 增强（transient/static 跳过 + CommonType 集成）
- **功能名**: deepCopyBean 字段过滤 + CommonType 集成 + BeanDesc.getField 新增
- **模块文件**:
  - `src/main/java/com/tingfeng/util/java/base/bean/copier/BeanDesc.java`
  - `src/main/java/com/tingfeng/util/java/base/bean/BeanUtils.java`
- **依赖**: Task 0 (CommonType)
- **查找指引**: 
  - BeanDesc.java 第 227-238 行 `getPropertyType()` 方法模式，新增 `getField(String)`
  - BeanUtils.java 第 686-723 行 `deepCopyBean()` 方法
  - BeanUtils.java 第 618-646 行 `deepCopyCollection()` 方法
  - BeanUtils.java 第 652-681 行 `deepCopyMap()` 方法

#### Task 2: BeanCopier 性能优化
- **功能名**: getTargetPropertyType 外提
- **模块文件**: `src/main/java/com/tingfeng/util/java/base/bean/copier/BeanCopier.java`
- **依赖**: 无
- **查找指引**: BeanCopier.java 第 166-243 行 `copyProperties()` 方法，第 179 和 204 行

## 10. 验收标准

### SubStory 1: CommonType 枚举
- [ ] CommonType 正确映射 List→ArrayList, Set→HashSet, Map→HashMap 等接口→实现关系
- [ ] `resolveImplementation(null)` 返回 null
- [ ] `resolveImplementation(ArrayList.class)` 返回 ArrayList.class（LIST 匹配，因为 List.isAssignableFrom(ArrayList)）
- [ ] `resolveImplementation(UnknownInterface.class)` 返回 null
- [ ] 没有新增外部依赖
- [ ] 编译通过

### SubStory 2: deepCopy 增强
- [ ] `deepCopyBean` 跳过 transient 字段（目标对象的 transient 属性值不变）
- [ ] `deepCopyBean` 跳过 static 字段（目标对象的 static 属性值不变）
- [ ] `deepCopyBean` 中 Collection 类型接口属性为 null 时，自动创建空 ArrayList 实例
- [ ] `deepCopyBean` 中 Map 类型接口属性为 null 时，自动创建空 HashMap 实例
- [ ] `deepCopyCollection` 遇到无默认构造器集合时，能通过 CommonType 回退创建实例
- [ ] `deepCopyMap` 遇到无默认构造器 Map 时，能通过 CommonType 回退创建实例
- [ ] CommonType 中未映射的接口类型属性保持 null 不变
- [ ] 已有测试全部通过

### SubStory 3: BeanCopier 性能优化
- [ ] `copyProperties()` 中 `getTargetPropertyType` 只调用 1 次
- [ ] 功能正确，已有测试全部通过
