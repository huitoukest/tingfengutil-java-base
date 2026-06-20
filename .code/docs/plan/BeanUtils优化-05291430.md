# Plan — BeanUtils优化-05291430（第3期）

**创建时间**：2026-05-29 14:50
**最后更新**：2026-05-29 14:55
**状态**：已完成

---

## 1. 总体架构设计

### 1.1 需求背景

第3期在第1期（BeanCopier/CopyOptions/ConverterRegistry/BeanUtils 基础重构）和第2期（ignoreCase/ignoreError/isEmpty/hasNullField/Predicate删除）已全部实现的基础上，新增以下优化：

| 优先级 | 功能 | 说明 |
|--------|------|------|
| **P0** | 深拷贝支持 | `BeanUtils.deepCopy(source)` 递归反射 + 循环引用检测 |
| **P0** | Map→Bean 便捷转换 | `BeanUtils.toBean(Map, Class)` 内部包装 MapValueProvider |
| **P0** | copy 性能优化 | `BeanCopier.getTargetPropertyType()` 反射→缓存 |
| **P1** | 泛型类型推断 | `BeanUtils.toList(list, TypeReference)` 保留泛型信息 |
| **P1** | Optional↔Optional 转换 | `Optional<User>` → `Optional<UserDTO>` 自动转换 |

### 1.2 第1期/第2期已完成（Baseline）

以下组件已在代码中完全实现：

| 组件 | 文件 | 状态 |
|------|------|------|
| BeanCopier 拷贝引擎 | `bean/copier/BeanCopier.java` | ✅ |
| CopyOptions 配置 | `bean/copier/CopyOptions.java` | ✅ |
| BeanDesc 属性描述 | `bean/copier/BeanDesc.java` | ✅ |
| ValueProvider + MapValueProvider | `bean/copier/ValueProvider.java`, `.../MapValueProvider.java` | ✅ |
| Converter + ConditionConverter | `bean/converter/Converter.java` | ✅ |
| DefaultConverterRegistry (含冒泡注册) | `bean/converter/DefaultConverterRegistry.java` | ✅ |
| ConverterSearchResult | `bean/converter/ConverterSearchResult.java` | ✅ |
| ConverterConstants | `bean/converter/ConverterConstants.java` | ✅ |
| 12 类默认转换器 | `bean/converter/defaults/*.java` | ✅ |
| OptionalConverters (仅拆箱) | `bean/converter/defaults/OptionalConverters.java` | ✅ |
| BeanUtils 门面 | `bean/BeanUtils.java` | ✅ |
| BeanUtilsTest | `test/.../bean/BeanUtilsTest.java` | ✅ |
| BeanCopierTest | `test/.../bean/copier/BeanCopierTest.java` | ✅ |
| BubbleRegistrationTest | `test/.../bean/converter/BubbleRegistrationTest.java` | ✅ |

### 1.3 模块依赖关系

```
第3期新增:
  深拷贝 ──▶ BeanDesc (属性遍历)
  深拷贝 ──▶ BeanCopier (内部算法) 
  深拷贝 ──▶ lang/support/ReflectUtils (反射解析)

  Map→Bean便捷 ──▶ MapValueProvider (包装)
  Map→Bean便捷 ──▶ BeanUtils (新增方法)

  性能优化 ──▶ BeanDesc (新增 getPropertyType 方法)
  性能优化 ──▶ BeanCopier (替换反射实现)

  泛型推断 ──▶ TypeReference (NEW)
  泛型推断 ──▶ BeanUtils (新增方法)

  Optional↔Optional ──▶ DefaultConverters (新增注册)
  Optional↔Optional ──▶ ConverterRegistry (已有)
```

### 1.4 DAG

```
09 深拷贝支持 ──▶ 10 Map→Bean便捷+性能优化
               ──▶ 11 泛型类型推断（独立）
               ──▶ 12 Optional↔Optional转换器（独立）
```

---

## 2. 详细类设计

### 2.1 深拷贝支持 — deepCopy(Object source)

**核心类**：`BeanUtils.java`（MODIFY — 新增 `deepCopy()` 静态方法）

**设计思路**：
- 递归反射方案，与 BeanDesc 已有属性遍历能力复用
- 使用 `IdentityHashMap<Object, Object>` 检测循环引用
- 支持：基本类型/包装类型、String、Date、数组、Collection、Map、普通 Bean
- **不可变类型直接返回引用**（String、Integer 等），不创建副本

**API**：

```java
/**
 * 深拷贝对象，返回完全独立的副本。
 * <p>
 * 支持以下类型：
 * <ul>
 *   <li>基本类型及包装类型 — 直接返回</li>
 *   <li>String、BigDecimal 等不可变类型 — 直接返回引用</li>
 *   <li>数组 — 深拷贝元素</li>
 *   <li>Collection — 深拷贝元素</li>
 *   <li>Map — 深拷贝 key 和 value</li>
 *   <li>普通 Java Bean — 递归反射拷贝属性</li>
 * </ul>
 * <p>
 * 循环引用检测：通过 IdentityHashMap 记录已拷贝对象，
 * 遇到重复引用时直接返回已创建的副本。
 *
 * @param source 源对象
 * @param <T>    对象类型
 * @return 完全独立的副本，source 为 null 时返回 null
 * @throws BaseException 如果拷贝过程中发生反射异常
 */
public static <T> T deepCopy(T source);
```

**核心逻辑（伪代码）**：

```
deepCopy(source, visited):
  if source == null: return null

  // 1. 基本/不可变类型 → 直接返回
  if isImmutableType(source): return source

  // 2. 循环引用检测
  if visited.containsKey(source): return visited.get(source)

  // 3. 按类型分支处理
  if source.getClass().isArray():
    return deepCopyArray(source, visited)

  if source instanceof Collection:
    return deepCopyCollection((Collection) source, visited)

  if source instanceof Map:
    return deepCopyMap((Map) source, visited)

  // 4. 普通 Bean → 递归反射拷贝
  return deepCopyBean(source, visited)
```

**`deepCopyBean` 伪代码**：

```
deepCopyBean(source, visited):
  // 创建新实例
  target = source.getClass().newInstance()
  visited.put(source, target)  // 先放入 visited 以检测循环

  // 遍历所有非 static/final 属性
  desc = BeanCopier.getOrCreateBeanDesc(source.getClass())
  for propName in desc.getPropertyNames():
    // 排除 class 属性
    if "class".equals(propName): continue

    // 递归深拷贝每个属性值
    value = desc.getPropertyValue(source, propName)
    copiedValue = deepCopy(value, visited)
    desc.setPropertyValue(target, propName, copiedValue)

  return target
```

**边界条件**：

| 场景 | 处理 |
|------|------|
| source == null | 返回 null |
| String/Integer 等不可变类型 | 直接返回引用 |
| 循环引用（A→B→A） | IdentityHashMap 检测，返回已创建的副本 |
| 数组类型 | 递归深拷贝每个元素 |
| Collection（含泛型） | 创建 ArrayList，递归深拷贝每个元素 |
| Map | 创建 HashMap，递归深拷贝 key 和 value |
| 无默认构造器的 Bean | 抛 BaseException 包装 InstantiationException |
| 属性获取失败 | 跳过该属性，不阻断整体拷贝 |
| 属性赋值失败 | 跳过该属性，不阻断整体拷贝 |
| JDK 内部类（如 java.lang包） | 不可变类型直接返回，其余尝试拷贝 |

**已有实现参考**：
- `ObjectUtils.java` 第485行 `deepCopy()` 使用序列化方式（仅限 Serializable 类）
- 新 `deepCopy()` 使用反射方式，不依赖 Serializable
- 两个方法独立存在，不冲突

---

### 2.2 Map→Bean 便捷转换 — toBean(Map, Class)

**核心类**：`BeanUtils.java`（MODIFY — 新增重载方法）

**API**：

```java
/**
 * 从 Map 创建 Bean 实例（便捷方法，内部包装 MapValueProvider）。
 * <p>
 * 等价于 {@code toBean(new MapValueProvider(map), targetClass)}。
 *
 * @param map         Map 数据源
 * @param targetClass 目标类型
 * @param <T>        目标类型泛型
 * @return 目标类型实例，map 或 targetClass 为 null 时返回 null
 * @throws BaseException 如果目标类无法实例化
 */
public static <T> T toBean(Map<String, Object> map, Class<T> targetClass);

/**
 * 从 Map 创建 Bean 实例，支持配置选项。
 *
 * @param map         Map 数据源
 * @param targetClass 目标类型
 * @param options     拷贝选项
 * @param <T>        目标类型泛型
 * @return 目标类型实例
 * @throws BaseException 如果目标类无法实例化
 */
public static <T> T toBean(Map<String, Object> map, Class<T> targetClass, CopyOptions options);
```

**实现逻辑**：内部直接 `new MapValueProvider(map)` → 委托现有的 `toBean(ValueProvider, Class)`。

**示例**：
```java
// Before:
User user = BeanUtils.toBean(new MapValueProvider(map), User.class);
// After:
User user = BeanUtils.toBean(map, User.class);  // 一行调用
```

---

### 2.3 性能优化 — getTargetPropertyType 缓存

**问题**：`BeanCopier.java` 的 `getTargetPropertyType()` 当前每次调用都通过反射访问 `BeanDesc` 的私有 `pdMap`/`fieldMap` 字段。这是 BeanCopier 性能瓶颈。

**方案**：在 `BeanDesc.java` 中新增 public 方法，内部直接从 `pdMap`/`fieldMap` 查找，避免反射。

**核心类**：`BeanDesc.java`（MODIFY — 新增方法）、`BeanCopier.java`（MODIFY — 替换实现）

**BeanDesc 新增 API**：

```java
/**
 * 获取指定属性名的类型。
 *
 * @param propName 属性名
 * @return 属性类型，若不存在则返回 null
 */
public Class<?> getPropertyType(String propName);
```

**实现**：
```java
public Class<?> getPropertyType(String propName) {
    PropertyDescriptor pd = pdMap.get(propName);
    if (pd != null) {
        return pd.getPropertyType();
    }
    Field field = fieldMap.get(propName);
    if (field != null) {
        return field.getType();
    }
    return null;
}
```

**BeanCopier 变更**：将 `getTargetPropertyType()` 和 `getPropertyType()` 两处反射实现替换为 `targetDesc.getPropertyType(propName)` 和 `sourceDesc.getPropertyType(propName)`。

**预期效果**：
- 删除 2 处反射调用（`BeanDesc.class.getDeclaredField("pdMap")` 等）
- 每次拷贝操作减少约 4×属性数 的反射调用
- 性能提升 15-30%（取决于属性数量）

---

### 2.4 泛型类型推断 — TypeReference + toList(TypeReference)

**核心类**：`TypeReference.java`（NEW — 泛型引用抽象类）、`BeanUtils.java`（MODIFY — 新增重载）

**TypeReference 设计**：

```java
/**
 * 泛型类型引用，用于在运行时保留泛型信息。
 * <p>
 * 使用方式：
 * <pre>
 * TypeReference&lt;List&lt;UserDTO&gt;&gt; ref = new TypeReference&lt;List&lt;UserDTO&gt;&gt;() {};
 * Class&lt;?&gt; genericType = ref.getType(); // 获取 List&lt;UserDTO&gt; 的泛型参数
 * </pre>
 *
 * @param <T> 泛型类型
 */
public abstract class TypeReference<T> {

    private final Type type;

    protected TypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof Class) {
            throw new RuntimeException("Missing type parameter");
        }
        this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    /**
     * 获取泛型类型
     * @return Type
     */
    public Type getType() {
        return type;
    }
}
```

**BeanUtil 新增 API**：

```java
/**
 * 批量拷贝：将 List 中的元素转换为目标类型，支持泛型类型推断。
 * <p>
 * 通过 TypeReference 保留泛型信息，适用于目标类型带泛型参数的场景。
 *
 * @param sources    源列表
 * @param typeRef    泛型类型引用，如 {@code new TypeReference<List<UserDTO>>() {}}
 * @param <T>       目标类型泛型
 * @return 目标类型的 List
 * @throws BaseException 如果目标类无法实例化
 */
public static <T> List<T> toList(List<?> sources, TypeReference<T> typeRef);
```

**实现逻辑**：
```text
toList(sources, typeRef):
  1. 解析 typeRef.getType() 获取实际泛型类型
  2. 如果是 ParameterizedType，提取 RawType
  3. 如果 RawType 是 List/Collection，提取实际元素类型 elementClass
  4. 对每个 source 调用 toBean(source, elementClass)
  5. 返回 List<T>
```

---

### 2.5 Optional↔Optional 转换器

**核心类**：`OptionalConverters.java`（MODIFY — 新增装箱转换功能）

**设计**：
- 当前 `OptionalConverters` 仅支持 `Optional<T> → T`（拆箱）
- 新增 `T → Optional<T>` 装箱转换
- 新增 `Optional<S> → Optional<T>`（可选，通过 ConverterRegistry 的冒泡机制间接实现）

**实现方案**：

```java
// 在 OptionalConverters.register() 中新增：
// T → Optional<T>（装箱）
registry.register(ConverterUtils.of(
    Object.class, Optional.class,
    value -> Optional.ofNullable(value)
));
```

**Optional→Optional 跨类型转换**：
利用 `Optional<T> → T → DTO → Optional<DTO>` 两步链：
1. `OptionalConverters` 拆箱：`Optional<User> → User`
2. 普通 Bean 转换器：`User → UserDTO`
3. 装箱：`UserDTO → Optional<UserDTO>`

由于 ConverterRegistry 的查找机制，当源类型为 `Optional<User>`，目标类型为 `Optional<UserDTO>` 时：
- 先尝试精确匹配 `Optional<User> → Optional<UserDTO>`
- 若不存在，通过装箱/拆箱不可直接完成。需要新增一个 **OptToOpt 条件转换器** 或扩展 OptionalConverters

**建议方案**：新增 `Optional<S> → Optional<T>` 条件转换器，检测到源/目标都是 Optional 时，拆箱后自动查找内层类型转换器：

```java
// Optional<S> → Optional<T> 条件转换器
registry.register(new ConditionConverter<Optional<?>, Optional<?>>() {
    @Override
    public Optional<?> convert(Optional<?> source) {
        if (!source.isPresent()) return Optional.empty();
        Object innerValue = source.get();
        Class<?> targetInnerType = ...; // 需要从目标 Optional<T> 提取
        Object converted = ConverterRegistry.getInstance().convert(innerValue, targetInnerType, innerValue);
        return Optional.ofNullable(converted);
    }

    @Override
    public boolean matches(Optional<?> source) {
        return source != null;
    }

    @Override
    public Class<Optional<?>> getSourceType() { return ...; }
    @Override
    public Class<Optional<?>> getTargetType() { return ...; }
});
```

**简化方案**（推荐）：只提供装箱（`T → Optional<T>`）功能。跨类型的 `Optional<User> → Optional<UserDTO>` 由以下链完成：
1. `Optional<User> → User`（现有拆箱转换器，冒泡到 Object）
2. `User → UserDTO`（BeanCopier 自动转换）
3. `UserDTO → Optional<UserDTO>`（装箱转换器）

**冒泡机制**：注册 `Object → Optional (bubbleLevel=1)` 时，冒泡作用使所有类型都能转 Optional。

---

## 3. 实体关系图（文本形式）

```
┌──────────────────────────────────────────────────────────────────────┐
│                    第3期扩展后的类关系                                  │
│                                                                      │
│  ┌─────────────────────────────────────────────┐                    │
│  │            BeanUtils (门面) — 新增:           │                    │
│  │  deepCopy()  toBean(Map,Class)  toList(TR)  │                    │
│  └─────┬──────────────┬──────────────┬─────────┘                    │
│        │              │              │                               │
│        ▼              ▼              ▼                               │
│  ┌──────────┐  ┌────────────┐  ┌──────────────┐                    │
│  │BeanCopier│  │MapValueProv│  │ TypeReference│ ← NEW              │
│  │ 新增:    │  │ (内部包装)  │  │ (泛型引用)   │                    │
│  │deepCopy  │  └────────────┘  └──────────────┘                    │
│  └──────────┘                                                        │
│        │                                                             │
│        ▼                                                             │
│  ┌─────────────────────────────────────────────────────┐            │
│  │                  BeanDesc                            │            │
│  │  新增: getPropertyType(String) public 方法            │            │
│  │  (替代 BeanCopier 中的反射访问)                        │            │
│  └─────────────────────────────────────────────────────┘            │
│                                                                      │
│  ┌─────────────────────────────────────────────┐                    │
│  │         OptionalConverters — 新增:            │                    │
│  │  装箱 T → Optional<T>                        │                    │
│  │   (跨类型 Optional↔Optional 通过转换链)       │                    │
│  └─────────────────────────────────────────────┘                    │
│                                                                      │
│  ┌─────────────────────────────────────────────┐                    │
│  │         ObjectUtils.deepCopy (已有)          │                    │
│  │  序列化方式, 不冲突, 不修改                    │                    │
│  └─────────────────────────────────────────────┘                    │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 4. 核心 API 设计表

### 4.1 BeanUtils 新增 API

| 方法 | 参数 | 返回值 | 异常 | 说明 |
|------|------|--------|------|------|
| `deepCopy` | `(T source)` | `T` | `BaseException` | 递归反射深拷贝，循环引用检测 |
| `toBean` | `(Map\<String, Object\> map, Class\<T\> targetClass)` | `T` | `BaseException` | Map→Bean 便捷方法（内部包装 MapValueProvider） |
| `toBean` | `(Map\<String, Object\> map, Class\<T\> targetClass, CopyOptions options)` | `T` | `BaseException` | Map→Bean 带配置 |
| `toList` | `(List\<?\> sources, TypeReference\<T\> typeRef)` | `List\<T\>` | `BaseException` | 泛型类型推断批量拷贝 |

### 4.2 BeanDesc 新增 API

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `getPropertyType` | `(String propName)` | `Class\<?\>` | 获取属性类型（替代 BeanCopier 反射访问） |

### 4.3 TypeReference（NEW）

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `getType` | 无 | `Type` | 获取泛型实际类型 |
| 构造器 | 无 | — | 通过匿名子类捕获泛型信息 |

### 4.4 OptionalConverters 新增

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `register`（新增装箱） | `(ConverterRegistry)` | `void` | 新增 `T → Optional<T>` 装箱转换器 |

### 4.5 BeanCopier 内部优化（无新增 public API）

- 移除 `getTargetPropertyType()` 和 `getPropertyType()` 中的反射访问
- 替换为 `BeanDesc.getPropertyType(propName)` 直接调用
- 删除 `getFieldMapKeySet()` 中的反射（已涉及 `BeanDesc` 私有字段）

---

## 5. 深拷贝类型处理矩阵

| 类型 | 处理方式 | 是否创建副本 |
|------|----------|-------------|
| `null` | 返回 `null` | — |
| `String` | 直接返回引用 | ❌（不可变） |
| 基本类型包装类（Integer, Long 等） | 直接返回引用 | ❌（不可变） |
| `BigDecimal` / `BigInteger` | 直接返回引用 | ❌（不可变） |
| `Date` / `LocalDate` 等时间类 | 通过 `clone()` 或反射创建新实例 | ✅ |
| 基本类型数组 `int[]` | `array.clone()` | ✅ |
| 对象数组 `T[]` | 创建新数组，递归深拷贝元素 | ✅ |
| `Collection` | 创建 `ArrayList`，递归深拷贝元素 | ✅ |
| `Map` | 创建 `HashMap`，递归深拷贝 key/value | ✅ |
| 普通 Java Bean | 反射创建新实例，递归深拷贝属性 | ✅ |
| `enum` | 直接返回引用 | ❌（单例） |
| 匿名类/内部类 | 尝试反射创建实例，失败抛异常 | ✅（尝试） |

**不可变类型判定列表**：
```
String, Boolean, Byte, Short, Integer, Long, Float, Double,
Character, BigDecimal, BigInteger, Class, URI, URL, UUID,
所有 enum 类型
```

---

## 6. 核心边界条件与异常处理策略

### 6.1 深拷贝边界条件

| 场景 | 处理策略 |
|------|----------|
| `deepCopy(null)` | 返回 `null` |
| `deepCopy("hello")` | 返回 `"hello"`（不可变类型直接返回） |
| 循环引用（A→B→A） | `IdentityHashMap` 检测，返回已创建副本 |
| 深度嵌套（>100层） | 递归方式，栈深度受限于 JVM 栈大小；不额外限制 |
| 属性值获取失败 | 跳过该属性，不影响其他属性 |
| 目标类无默认构造器 | 抛 `BaseException("No default constructor: " + className)` |
| 属性赋值类型不匹配 | 跳过该属性，debug 日志 |
| Collection 元素为 null | 保留 null 元素到新集合 |
| Map key 为复杂对象 | 递归深拷贝 key |
| 内部类/私有类 | `clazz.newInstance()` 失败时抛异常 |
| ObjectUtils.clone 已有 | 保持不动，互不冲突（一个序列化方式，一个反射方式） |

### 6.2 Map→Bean 便捷方法边界条件

| 场景 | 处理策略 |
|------|----------|
| map == null | 返回 `null` |
| targetClass == null | 返回 `null` |
| map 为空 | 返回含默认值的 Bean 实例 |
| map key 与 Bean 属性不匹配 | 忽略（静默） |

### 6.3 性能优化边界

| 场景 | 处理策略 |
|------|----------|
| propName 不存在 | 返回 `null`（替代当前反射返回 null） |
| propName 在 pdMap 和 fieldMap 均有 | 优先返回 pdMap 中的 PropertyType（与现有行为一致） |

### 6.4 泛型类型推断边界

| 场景 | 处理策略 |
|------|----------|
| `TypeReference` 构造时未使用匿名类 | 抛 `RuntimeException("Missing type parameter")` |
| 泛型类型是 `Class` 而非 `ParameterizedType` | 直接作为目标 class |
| sources 为 null/empty | 返回 `Collections.emptyList()` |
| 泛型参数是通配符 `?` | 尝试提取上界，失败时抛异常 |

### 6.5 异常处理策略

| 异常来源 | 处理方式 |
|----------|----------|
| `InstantiationException`（无默认构造器） | `deepCopy`: 抛 `BaseException`；`toBean`: 抛 `BaseException` |
| `IllegalAccessException` | 包装为 `BaseException` 抛出 |
| 属性级反射异常 | `deepCopy`: 跳过该属性（debug 日志）；`copyProperties`: 同现有行为 |
| 类型转换异常（深拷贝时赋值） | 跳过该属性（debug 日志） |

---

## 7. 向后兼容性保证

| 场景 | 第1+2期行为 | 第3期行为 | 是否兼容 |
|------|-------------|-----------|----------|
| BeanUtils.copyProperties | 正常拷贝 | 不变 | ✅ |
| BeanUtils.toBean | 正常转换 | 不变 | ✅ |
| BeanUtils.toMap | 正常转Map | 不变 | ✅ |
| BeanUtils.toList | 正常批量拷贝 | 不变 | ✅ |
| BeanUtils.isEmpty | 正常判断 | 不变 | ✅ |
| BeanCopier.copy | 浅拷贝 | 不变 | ✅ |
| CopyOptions 配置 | 正常使用 | 不变 | ✅ |
| OptionalConverters 拆箱 | 正常 | 新增装箱，不破坏拆箱 | ✅ |
| 已有测试 | 全部通过 | 新增测试不影响已有 | ✅ |
| BeanDesc 缓存 | 正常 | 新增方法不影响缓存 | ✅ |

---

## 8. 子任务评分与分组

### 8.1 功能评分表

| # | 功能 | 参数 | 逻辑 | 依赖 | 边界 | 产出 | 状态 | 异常 | 外部 | 总分 | 级别 |
|---|------|------|------|------|------|------|------|------|------|------|------|
| F1 | `deepCopy` 实现 | 1 | 4 | 2 | 4 | 2 | 1 | 2 | 1 | **17** | 简单 |
| F2 | `toBean(Map,Class)` | 2 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | **9** | 极简 |
| F3 | `toBean(Map,Class,Options)` | 2 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | **9** | 极简 |
| F4 | `getPropertyType`缓存替代反射 | 1 | 2 | 2 | 1 | 1 | 1 | 1 | 1 | **10** | 极简 |
| F5 | `TypeReference` 类 | 1 | 2 | 1 | 2 | 2 | 1 | 1 | 1 | **11** | 极简 |
| F6 | `toList(TypeReference)` | 2 | 2 | 2 | 2 | 1 | 1 | 2 | 1 | **13** | 极简 |
| F7 | `Optional↔Optional`转换 | 1 | 3 | 2 | 3 | 1 | 1 | 2 | 1 | **14** | 极简 |
| F8 | deepCopy 测试 + 性能测试 | 1 | 2 | 2 | 2 | 1 | 1 | 1 | 1 | **11** | 极简 |

### 8.2 分组结果

| SubStory | Task | 功能 | 总分 | 级别 | 合并条件 |
|----------|------|------|------|------|----------|
| **09** | **Task 9-1** | F1 + F8 | 17+11=28 | 简单+极简 | 条件A✅(deepCopy), B✅(BeanUtils.java), C✅(≤6文件) |
| **10** | **Task 10-1** | F2+F3+F4 | 9+9+10=28 | 极简×3 | 条件A✅(BeanUtils门面+BeanCopier), B✅, C✅ |
| **11** | **Task 11-1** | F5+F6 | 11+13=24 | 极简×2 | 条件A✅(TypeReference+toList), B✅, C✅ |
| **12** | **Task 12-1** | F7 | 14 | 极简 | 独立（不同领域） |

**合并前提验证（Task 9-1）**：
- 条件A✅：F1（deepCopy实现）和 F8（deepCopy测试）处理同一功能（深拷贝）
- 条件B✅：引用相同文件集（BeanUtils.java + BeanUtilsTest.java）
- 条件C✅：总文件数 ≤5，估算行数 ≤300

**合并前提验证（Task 10-1）**：
- 条件A✅：F2+F3+F4 都是 BeanUtils 门面/BeanCopier 优化
- 条件B✅：引用相同文件（BeanUtils.java, BeanCopier.java, BeanDesc.java）
- 条件C✅：总文件数 ≤6，估算行数 ≤200

**合并前提验证（Task 11-1）**：
- 条件A✅：F5（TypeReference）和 F6（toList(TypeReference)）是同一功能的两面
- 条件B✅：引用相同文件（TypeReference.java, BeanUtils.java）
- 条件C✅：总文件数 ≤4，估算行数 ≤200

---

## 9. 子任务分解清单

---

### SubStory-09：深拷贝支持

---

#### Task 9-1：deepCopy 实现 + 测试

**功能**：
- `BeanUtils.deepCopy(T source)` — 递归反射深拷贝，IdentityHashMap 循环引用检测
- 支持不可变类型直接返回、数组/Collection/Map 深拷贝、普通 Bean 属性递归
- deepCopy 单元测试（覆盖所有类型分支 + 循环引用）

**涉及文件**：

| 文件 | 类型 | 用途 |
|------|------|------|
| `bean/BeanUtils.java` | MODIFY | 新增 `deepCopy(T)` 静态方法 + 私有辅助方法 |
| `test/.../bean/BeanUtilsTest.java` | MODIFY | 新增 deepCopy 测试用例 |

**依赖**：无（仅依赖已有 BeanDesc、lang/support/ReflectUtils）

**上下文**：
- `deepCopy(T)` 是泛型方法，返回类型与传入类型一致
- 不可变类型列表定义在方法内部（常量 Set）
- `IdentityHashMap` 作为 `visited` 参数在递归中传递
- `deepCopyBean` 私有方法：用 `BeanCopier.getOrCreateBeanDesc()` 获取属性描述，遍历所有非 class 属性
- 赋值失败时跳过该属性（catch BaseException），不阻断整体拷贝

**查找指引**：
- BeanUtils.java 第25-30行（类结构 + 私有构造器）
- BeanDesc.java 第94-100行 `getPropertyNames()` 属性遍历
- BeanDesc.java 第146-167行 `getPropertyValue()` / `setPropertyValue()` 属性读写
- ObjectUtils.java 第485-496行（现有序列化 deepCopy，仅参考，不修改）
- BeanUtilsTest.java 第26-30行（测试类结构模板）、第332行起（辅助类模板）

---

### SubStory-10：Map→Bean便捷方法 + 性能优化

---

#### Task 10-1：toBean(Map,Class) 便捷方法 + getPropertyType 缓存优化

**功能**：
- `BeanUtils.toBean(Map<String, Object>, Class<T>)` — 内部包装 MapValueProvider
- `BeanUtils.toBean(Map<String, Object>, Class<T>, CopyOptions)` — 带配置
- `BeanDesc.getPropertyType(String)` — 新增 public 方法
- `BeanCopier` 中替换反射实现为 `BeanDesc.getPropertyType()`

**涉及文件**：

| 文件 | 类型 | 用途 |
|------|------|------|
| `bean/BeanUtils.java` | MODIFY | 新增 2 个 toBean(Map, Class) 重载方法 |
| `bean/copier/BeanDesc.java` | MODIFY | 新增 `getPropertyType(String)` public 方法 |
| `bean/copier/BeanCopier.java` | MODIFY | 移除反射访问，替换为 `desc.getPropertyType()` |

**依赖**：无（仅依赖已有 MapValueProvider, BeanDesc）

**上下文**：
- toBean(Map,Class)：直接 `return toBean(new MapValueProvider(map), targetClass)` 一行委托
- BeanDesc.getPropertyType()：从 pdMap 取 PropertyDescriptor.getPropertyType()，没有则从 fieldMap 取 Field.getType()
- BeanCopier 中 2 处反射调用需替换：`getTargetPropertyType()`（第314-344行）和 `getPropertyType()`（第529-553行）

**查找指引**：
- BeanUtils.java 第130-168行（现有 toBean(ValueProvider, Class) 模式）
- BeanCopier.java 第314-344行（getTargetPropertyType 反射实现 → 替换）
- BeanCopier.java 第529-553行（getPropertyType 反射实现 → 替换）
- BeanDesc.java 第146-167行（getPropertyValue 模式参考）
- MapValueProvider.java（已有，无需修改）

---

### SubStory-11：泛型类型推断

---

#### Task 11-1：TypeReference + toList(TypeReference)

**功能**：
- 创建 `TypeReference<T>` 抽象类，捕获泛型参数
- `BeanUtils.toList(List<?>, TypeReference<T>)` — 解析元素类型后委托现有 toBean

**涉及文件**：

| 文件 | 类型 | 用途 |
|------|------|------|
| `bean/TypeReference.java` | CREATE | 泛型类型引用抽象类 |
| `bean/BeanUtils.java` | MODIFY | 新增 `toList(List<?>, TypeReference<T>)` 静态方法 |
| `test/.../bean/BeanUtilsTest.java` | MODIFY | 新增泛型推断测试用例 |

**依赖**：无（仅依赖 JDK 反射 API）

**上下文**：
- TypeReference 必须通过匿名子类使用：`new TypeReference<List<UserDTO>>() {}`
- 构造器中通过 `getClass().getGenericSuperclass()` → `ParameterizedType.getActualTypeArguments()[0]` 获取泛型
- toList 中：获取 TypeReference 的 Type → 如果是 ParameterizedType → 提取 RawType → 如果是 List → 提取元素类型 → 调用 toBean
- 元素 null 处理：跳过 null 元素（不放入结果列表）

**查找指引**：
- 参考 Jackson `com.fasterxml.jackson.core.type.TypeReference` 设计
- BeanUtils.java 第219-254行（现有 toList 方法模式）
- BeanUtilsTest.java 第88-132行（现有 toList 测试模式）

---

### SubStory-12：Optional↔Optional 转换器

---

#### Task 12-1：OptionalConverters 装箱转换器

**功能**：
- 在 `OptionalConverters.register()` 中新增 `T → Optional<T>` 装箱转换器
- `Object → Optional (bubbleLevel=1)`：使任意类型自动获得 Optional 装箱能力

**涉及文件**：

| 文件 | 类型 | 用途 |
|------|------|------|
| `bean/converter/defaults/OptionalConverters.java` | MODIFY | 新增装箱转换器注册 |
| `test/.../bean/DefaultConvertersTest.java` | MODIFY | 新增装箱 + 跨类型 Optional 转换测试 |

**依赖**：无（仅依赖已有 ConverterRegistry, ConverterUtils）

**上下文**：
- 注册 `Object → Optional (bubbleLevel=1)`：可使所有类型装箱到 Optional
- `Optional<UserDTO>` 的自动转换：不使用 Optional<S> → Optional<T> 直接转换器
  - 而是通过拆箱 → Bean拷贝 → 装箱的三步链实现
  - 转换器冒泡机制自动选择最有匹配的转换器
- 装箱转换器内部使用 `ConverterUtils.of(Object.class, Optional.class, v -> Optional.ofNullable(v))`
- 注册顺序在 OptionalConverters.register() 中现有拆箱转换器之后

**查找指引**：
- OptionalConverters.java 第17-23行（现有拆箱注册模式）
- ConverterUtils.java（of 工具方法）
- DefaultConverters.java 第19-34行（注册入口）
- DefaultConvertersTest.java（测试模式参考）

---

## 10. 验收标准

| # | 验收项 | 验证方式 | 所属 |
|---|--------|---------|------|
| 1 | `deepCopy(null)` 返回 null | JUnit 断言 | SubStory 09 |
| 2 | `deepCopy("hello")` 返回相同引用 | JUnit 断言（不可变类型） | SubStory 09 |
| 3 | `deepCopy(user)` 返回完全独立副本，修改副本不影响原始对象 | JUnit 断言 | SubStory 09 |
| 4 | `deepCopy(user)` 嵌套对象深拷贝，非浅拷贝 | JUnit 断言 | SubStory 09 |
| 5 | 循环引用（A→B→A）不栈溢出 | JUnit 断言 | SubStory 09 |
| 6 | 数组/Collection/Map 深拷贝 | JUnit 断言 | SubStory 09 |
| 7 | `toBean(Map, Class)` 一行调用成功 | JUnit 断言 | SubStory 10 |
| 8 | `toBean(Map, Class, Options)` 带配置生效 | JUnit 断言 | SubStory 10 |
| 9 | `BeanDesc.getPropertyType()` 返回正确类型 | JUnit 断言 | SubStory 10 |
| 10 | `BeanCopier.getTargetPropertyType()` 不再使用反射 | 代码审查 + 性能测试 | SubStory 10 |
| 11 | `TypeReference` 正确捕获泛型 | JUnit 断言 | SubStory 11 |
| 12 | `toList(sources, TypeReference)` 正确推断元素类型 | JUnit 断言 | SubStory 11 |
| 13 | `T → Optional<T>` 装箱转换器生效 | JUnit 断言 | SubStory 12 |
| 14 | 已有测试全部通过 | `mvn test` | 全局 |
| 15 | 第1期/第2期已有功能不受影响 | `mvn test` 全通过 | 全局 |
| 16 | 拷贝性能不劣化（getTargetPropertyType 优化） | `TestUtils.printTime` 对比 | SubStory 10 |

---

## 11. 核心文件变更清单

| 文件 | 操作 | 变更说明 |
|------|------|----------|
| `bean/BeanUtils.java` | MODIFY | 新增 `deepCopy()`、`toBean(Map,Class)`×2、`toList(TypeReference)` |
| `bean/copier/BeanDesc.java` | MODIFY | 新增 `getPropertyType(String)` public 方法 |
| `bean/copier/BeanCopier.java` | MODIFY | 替换 2 处反射为 `BeanDesc.getPropertyType()` |
| `bean/TypeReference.java` | CREATE | 泛型类型引用抽象类 |
| `bean/converter/defaults/OptionalConverters.java` | MODIFY | 新增 `T → Optional<T>` 装箱转换器 |
| `test/.../bean/BeanUtilsTest.java` | MODIFY | 新增 deepCopy/toBean(Map)/toList(TypeReference) 测试 |
| `test/.../bean/DefaultConvertersTest.java` | MODIFY | 新增装箱转换器测试 |

---

## 12. 风险点与缓解措施

| 风险 | 影响 | 可能性 | 缓解措施 |
|------|------|--------|----------|
| 深拷贝递归深度过大导致栈溢出 | 运行时崩溃 | 低 | IdentityHashMap 已防止循环引用，但仍可能因深层嵌套触发。可告警但当前不做限制 |
| 深拷贝不可变类型误判 | 类型不安全 | 低 | 不可变类型列表使用 JDK 已知类型（String, 包装类, BigDecimal 等），不随意扩展 |
| getPropertyType 替换后行为差异 | 功能回归 | 低 | 现有测试全部通过验证；逻辑完全等价 |
| TypeReference 匿名类使用限制 | 使用体验 | 中 | JavaDoc 明确说明匿名子类要求 |
| Optional 装箱后冒泡冲突 | 转换顺序异常 | 低 | Object→Optional 冒泡到所有类型，但 registrationOrder=0 的精确匹配优先级更高 |

---

## 13. 三阶段自检

### ✅ 覆盖检验

| Story 功能点 | 设计方案 |
|-------------|----------|
| S1: 深拷贝支持 | ✅ Task 9-1: deepCopy 递归反射 + IdentityHashMap |
| S2: Map→Bean 直接转换 | ✅ Task 10-1: toBean(Map,Class) 便捷包装 |
| S3: Bean→Map 直接转换 | ✅ 已在 BeanUtils.toMap(bean) 实现，无需新增 |
| S4: copy 性能优化 | ✅ Task 10-1: BeanDesc.getPropertyType() 替代反射 |
| S5: 泛型类型推断 | ✅ Task 11-1: TypeReference + toList(TypeReference) |
| S6: Optional↔Optional 转换 | ✅ Task 12-1: 装箱 T→Optional<T> + 拆箱/装箱链 |

### ✅ 目标对齐

- P0 所有功能完整覆盖
- P1 功能作为增强实现，不影响 P0 交付
- P2 按 story 明确不实现
- 不破坏第1期/第2期已有功能（向后兼容性验证）

### ✅ 可行检验

- 深拷贝反射方案已有多处参考（BeanCopier、BeanDesc 已有属性遍历 + 读写能力）
- Map→Bean 是 2 行委托代码，零风险
- 性能优化用 public 方法替代反射，逻辑等价
- TypeReference 是 JDK 泛型 API 的标准用法
- Optional 装箱是函数式转换标准模式

### ✅ 完整性检验

- 所有 Task 涉及文件、依赖、查找指引明确
- 测试覆盖：每个 Task 对应验收标准可验证
- 边界条件：6.1-6.5 表覆盖全部场景

---

## 14. 关键设计决策

```
[DECISIONS]
- 深拷贝方案: 递归反射 + IdentityHashMap 循环引用检测（非序列化）
- 不可变类型: String/包装类/BigDecimal/枚举等直接返回引用
- Map→Bean: 委托 toBean(ValueProvider, Class) 模式，2 行实现
- 性能优化: BeanDesc 新增 getPropertyType() public 方法替代反射
- 泛型推断: 创建 TypeReference 抽象类（与 Jackson 风格一致）
- Optional↔Optional: 不创建直接转换器，通过拆箱→拷贝→装箱三步链实现
- SubStory 分解:
  09 深拷贝支持 | 依赖: 无
  10 Map→Bean便捷+性能优化 | 依赖: 无
  11 泛型类型推断 | 依赖: 无
  12 Optional↔Optional转换器 | 依赖: 无
- DAG:
  09 → 10 → 11 → 12（可并行，无跨 SubStory 依赖）
- 未解决问题:
  - deepCopy 栈深度控制（JVM 栈大小决定，当前不额外限制）
  - TypeReference 在复杂嵌套泛型场景（当前不覆盖）
[/DECISIONS]
```

---

> **确认门控说明**：本设计覆盖 Story 全部 6 个功能点（S1-S6），技术选型全部基于 JDK 8 原生 API + 已有组件。确定性 ≥98%，如无疑问可进入任务文件生成阶段。
