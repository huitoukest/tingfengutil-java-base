# Converter 冒泡注册机制 - 详细设计

| 项目 | 内容 |
|------|------|
| 创建时间 | 2026-05-23 |
| 最后更新 | 2026-05-23 |
| 状态 | 已实现 |

## 决策记录
| 日期 | 决策 | 来源 |
|------|------|------|
| 2026-05-23 | Converter 存储从单个改为 List，支持冒泡副本共存 | myPlan 设计确认 |
| 2026-05-23 | bubbleLevel 默认=1，向后兼容 | myPlan 设计确认 |
| 2026-05-23 | 排序在注册时完成（而非查找时），简化查找路径 | myPlan 设计确认 |
| 2026-05-23 | ReadWriteArrayList.removeIf() 无效（iterator快照），改为索引遍历+remove(int) | FEEDBACK:myBuild 修复 |
| 2026-05-23 | 新增 BubbleRegistrationTest 12个场景覆盖冒泡注册机制 | FEEDBACK:myBuild 修复 |
| 相关文档 | `.code/docs/story/converter-bubble-registration.md`、`.code/docs/design/converter-bubble-registration.md` |

## 1. 实体关系

```
Converter<S, T>  (接口)
  ├── bubbleLevel()        default 1     // 可冒泡层数
  ├── registrationOrder()  default 0     // 注册顺序（内部自动计算）
  ├── order()              default 0     // 用户排序值
  ├── convert(S)                         // 执行转换
  ├── getSourceType()                    // 源类型
  └── getTargetType()                    // 目标类型
        ↑
        │  extends
ConditionConverter<S, T>  (接口)          // 已有 order() 覆盖 Converter.order()
  ├── matches(S)                         // 条件判断
  └── order()               default 0    // 覆盖 Converter.order()

ConverterRegistry  (接口)
  ├── register(Converter)
  ├── unregister(Converter)
  ├── findAll(source, target)
  ├── findConverters(source, target)
  ├── convert(source, target)
  ├── convert(source, target, defaultValue)
  ├── clear()
  └── resetConverter()

DefaultConverterRegistry  (实现)
  ├── converters: Map<UnionKey, ReadWriteArrayList<Converter<?, ?>>>     // ★ 改为 List
  ├── conditionConverters: Map<UnionKey, ReadWriteArrayList<ConditionConverter<?, ?>>>
  ├── registerWithBubble()                   // ★ 新增：冒泡注册入口
  ├── bubbleToHierarchy()                    // ★ 新增：递归冒泡
  └── originalToBubbled: Map<Converter, List<UnionKey>>   // ★ 新增：追踪冒泡副本

ConverterSearchResult<S, T>
  ├── conditionConverters: List<ConditionConverter<S, T>>
  ├── converters: List<Converter<S, T>>                  // ★ 改为 List（之前是单个）
  ├── getConverters()                    // ★ 新增
  ├── getConverter()                     // 返回 converters 中第一个（或 null）
  └── getAllConverters()                 // 返回合并排序后的完整列表

UnionKey
  └── keys: Object[]                     // (sourceType, targetType) 联合键
```

## 2. 接口设计 — Converter.java

### 新增方法（均为 default，非破坏性变更）

```java
/**
 * 可冒泡层数。注册时生效，控制向上注册到第几层父类。
 * <ul>
 *   <li>0 = 不冒泡，仅注册自身</li>
 *   <li>1 = 注册自身 + 直接父类（含实现的接口）</li>
 *   <li>n = 注册自身 + 向上 n 层父类链/接口链</li>
 *   <li>-1 = 无限制，冒泡到 Object 为止</li>
 * </ul>
 * 默认值 1，保持向后兼容的同时让大多数转换器获得自动冒泡能力。
 *
 * @return 冒泡层数
 */
default int bubbleLevel() {
    return 1;
}

/**
 * 注册顺序，由注册工具在冒泡时自动计算。
 * <ul>
 *   <li>原始注册的 Converter 为 0</li>
 *   <li>每冒泡一层 +1</li>
 *   <li>超过 {@link Integer#MAX_VALUE} 时取最大值</li>
 * </ul>
 * 用于排序：优先使用更精确（即原始注册）的转换器。
 *
 * @return 注册顺序，值越小越优先
 */
default int registrationOrder() {
    return 0;
}

/**
 * 用户定义的同级排序值。
 * 当 {@link #registrationOrder()} 相同时，此值小的优先。
 * 配合 {@link #registrationOrder()} 实现"先精确匹配，再用户排序"的查找规则。
 *
 * @return 排序值，值越小越优先
 */
default int order() {
    return 0;
}
```

### ConditionConverter 的 order() 处理

`ConditionConverter` 已存在 `order()` 方法（默认返回 `ConverterConstants.ORDER_DEFAULT` = 0）。
新增的 `Converter.order()` 默认返回 0。由于 `ConditionConverter extends Converter`，
`ConditionConverter.order()` 覆盖 `Converter.order()`，返回相同值，**无冲突、无行为变更**。

```java
public interface ConditionConverter<S, T> extends Converter<S, T> {
    // 已有的 order() 会覆盖 Converter.order()，默认值均为 0，无需修改
    default int order() {
        return ConverterConstants.ORDER_DEFAULT;  // 保持为 0
    }
}
```

## 3. 冒泡注册算法

### 3.1 整体流程

```
register(converter)
  │
  ├─ 1. 获取 srcType, targetType, bubbleLevel
  │
  ├─ 2. 初始化 affectedKeys = new HashSet<>()
  │
  ├─ 3. 注册原始 Converter（registrationOrder=0）
  │     └─ registerOne(converter, srcType, targetType, 0)
  │     └─ affectedKeys.add(new UnionKey(srcType, targetType))
  │
  ├─ 4. 如果 bubbleLevel == 0 → 不冒泡，直接跳到第 6 步
  │
  ├─ 5. 冒泡注册（记录所有受影响的 UnionKey）
  │     └─ bubbleToHierarchy(converter, srcType, targetType, bubbleLevel, 0, affectedKeys)
  │
  └─ 6. ★ 对所有 affectedKeys 统一执行排序
        for each key in affectedKeys:
          sortConverterList(key, isCondition)
```

### 3.2 核心伪代码

```java
// ★ register() 方法变更
@Override
public <S, T> void register(Converter<S, T> converter) {
    if (converter == null) return;
    Class<?> srcType = converter.getSourceType();
    Class<?> targetType = converter.getTargetType();
    if (srcType == null || targetType == null) return;

    int bubbleLevel = converter.bubbleLevel();

    // 初始化 affectedKeys（记录本次注册影响的所有 UnionKey）
    Set<UnionKey> affectedKeys = new HashSet<>();

    // 步骤1：注册原始 Converter（registrationOrder = 0）
    registerOne(converter, srcType, targetType, 0);
    affectedKeys.add(new UnionKey(srcType, targetType));

    // 步骤2：需要冒泡
    if (bubbleLevel != 0) {
        Set<UnionKey> bubbledKeys = new HashSet<>();
        bubbledKeys.add(new UnionKey(srcType, targetType)); // 原始跳过
        
        // 递归冒泡，affectedKeys 同时记录
        bubbleToHierarchy(converter, srcType, targetType, 
                bubbleLevel, 0, bubbledKeys, affectedKeys);
    }

    // 步骤3：★ 本次注册所有操作完成后，统一排序所有受影响的 key
    for (UnionKey key : affectedKeys) {
        sortConverterList(key);
    }
}

/**
 * 递归冒泡：遍历目标类型的目标类链 + 接口链
 */
private void bubbleToHierarchy(
        Converter<?, ?> converter,
        Class<?> srcType,
        Class<?> targetType,
        int bubbleLevel,
        int currentRegOrder,
        Set<UnionKey> bubbledKeys,
        Set<UnionKey> affectedKeys) {

    if (bubbleLevel == 0) return;

    int nextBubbleLevel = (bubbleLevel == -1) ? -1 : bubbleLevel - 1;
    int nextRegOrder = safeIncrementRegOrder(currentRegOrder);

    // 1️⃣ 处理父类链（先父类，再接口）
    Class<?> superClass = targetType.getSuperclass();
    if (superClass != null) {
        registerBubbledCopyIfAbsent(converter, srcType, superClass,
                nextBubbleLevel, nextRegOrder, bubbledKeys, affectedKeys);
        bubbleToHierarchy(converter, srcType, superClass,
                nextBubbleLevel, nextRegOrder, bubbledKeys, affectedKeys);
    }

    // 2️⃣ 处理接口链
    for (Class<?> iface : targetType.getInterfaces()) {
        registerBubbledCopyIfAbsent(converter, srcType, iface,
                nextBubbleLevel, nextRegOrder, bubbledKeys, affectedKeys);
        bubbleToHierarchy(converter, srcType, iface,
                nextBubbleLevel, nextRegOrder, bubbledKeys, affectedKeys);
    }
}

/**
 * 安全递增 registrationOrder（防溢出）
 */
private int safeIncrementRegOrder(int regOrder) {
    if (regOrder >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
    return regOrder + 1;
}

/**
 * 创建冒泡副本并注册（仅当该 UnionKey 尚未被本次冒泡产生时）
 */
private void registerBubbledCopyIfAbsent(
        Converter<?, ?> original,
        Class<?> srcType,
        Class<?> newTargetType,
        int nextBubbleLevel,
        int nextRegOrder,
        Set<UnionKey> bubbledKeys,
        Set<UnionKey> affectedKeys) {

    UnionKey key = new UnionKey(srcType, newTargetType);
    if (bubbledKeys.contains(key)) return;  // 去重

    bubbledKeys.add(key);
    affectedKeys.add(key); // ★ 记录受影响的 key

    // 创建包裹 Converter（委托给 original，覆盖 registrationOrder/bubbleLevel/targetType）
    Converter<?, ?> bubbled = createBubbledCopy(original,
            srcType, newTargetType, nextRegOrder, nextBubbleLevel);

    registerOne(bubbled, srcType, newTargetType, nextRegOrder);

    // 记录追踪信息，供 unregister 使用
    trackBubbledCopy(original, key);
}
```

### 3.3 包裹 Converter 创建

```java
/**
 * 创建冒泡副本 Converter，所有逻辑委托给 original，
 * 仅覆盖 registrationOrder()、bubbleLevel()、getTargetType()
 */
private static <S, T> Converter<S, T> createBubbledCopy(
        Converter<S, T> original,
        Class<S> srcType,
        Class<T> bubbledTargetType,
        int regOrder,
        int bl) {

    if (original instanceof ConditionConverter) {
        ConditionConverter<S, T> cc = (ConditionConverter<S, T>) original;
        return new BubbledConditionConverter<>(cc, srcType, bubbledTargetType, regOrder, bl);
    } else {
        return new BubbledConverter<>(original, srcType, bubbledTargetType, regOrder, bl);
    }
}

/**
 * 普通转换器冒泡副本
 */
static class BubbledConverter<S, T> implements Converter<S, T> {
    private final Converter<S, T> delegate;
    private final Class<S> sourceType;
    private final Class<T> targetType;
    private final int registrationOrder;
    private final int bubbleLevel;

    BubbledConverter(Converter<S, T> delegate, Class<S> sourceType,
                     Class<T> targetType, int registrationOrder, int bubbleLevel) {
        this.delegate = delegate;
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.registrationOrder = registrationOrder;
        this.bubbleLevel = bubbleLevel;
    }

    @Override public T convert(S source) { return delegate.convert(source); }
    @Override public Class<S> getSourceType() { return sourceType; }
    @Override public Class<T> getTargetType() { return targetType; }
    @Override public int registrationOrder() { return registrationOrder; }
    @Override public int bubbleLevel() { return bubbleLevel; }
    @Override public int order() { return delegate.order(); }  // 保持用户 order

    // 用于 unregister 的 identity 比对
    public Converter<S, T> getDelegate() { return delegate; }
}

/**
 * ConditionConverter 冒泡副本
 */
static class BubbledConditionConverter<S, T> extends BubbledConverter<S, T>
        implements ConditionConverter<S, T> {

    private final ConditionConverter<S, T> delegate;

    BubbledConditionConverter(ConditionConverter<S, T> delegate, Class<S> sourceType,
                              Class<T> targetType, int registrationOrder, int bubbleLevel) {
        super(delegate, sourceType, targetType, registrationOrder, bubbleLevel);
        this.delegate = delegate;
    }

    @Override public boolean matches(S source) { return delegate.matches(source); }
}
```

### 3.4 状态转换：冒泡示例

注册 `String → Number(bubbleLevel=3, order=5)`：

```
bubbleLevel=3, registrationOrder=0 (原始注册)
  │
  ├─ 父类链: Number.getSuperclass() = Object
  │   └─ String → Object    regOrder=1, bl=2, order=5
  │       └─ Object.getSuperclass() = null → 停止
  │
  ├─ 接口链: Number.getInterfaces() = [Serializable, Comparable]
  │   ├─ String → Serializable  regOrder=1, bl=2, order=5
  │   │   └─ Serializable.getInterfaces() = [] → 停止
  │   │
  │   └─ String → Comparable    regOrder=1, bl=2, order=5
  │       └─ Comparable.getInterfaces() = [] → 停止
  │
  └─ bl 减到 0 → 不再继续扩展（但已递归到的路径继续走完）
```

### 3.5 边界情况处理

| 场景 | 处理方式 |
|------|----------|
| `bubbleLevel = 0` | 仅注册自身，不冒泡 |
| `bubbleLevel = -1` | 无限冒泡直到 Object。冒泡副本也保持 -1。Object 的 `getSuperclass()=null`, `getInterfaces()=[]`，自然终止 |
| `bubbleLevel = 1` | 注册自身 + 直接父类 + 直接接口，不递归 |
| 目标类型为 `Object.class` | 接口链上 `Object` 的 `getInterfaces()` 返回空数组；Object 本身作为目标时从 `getSuperclass()` 开始 — 但 `Object.getSuperclass()=null`，不继续冒泡 |
| 目标类型为接口 | `getSuperclass()` 返回 `null`；在接口链中递归 `getInterfaces()` 遍历父接口 |
| 数组类型 | `getSuperclass()` 返回 `Object`；`getInterfaces()` 可能返回 `Cloneable`, `Serializable` |
| `registrationOrder` 溢出 | 使用 `safeIncrementRegOrder()`：达到 `Integer.MAX_VALUE` 时保持 |
| 去重：同一 `UnionKey` 从不同路径到达 | 使用 `bubbledKeys` 集合跟踪，首次遇到时注册并记录，后续跳过 |
| 递归深度过大 | 最坏情况：类继承深度+接口深度有限的（一般不超过 20 层），递归栈安全 |

## 4. 存储结构变更 — DefaultConverterRegistry

### 4.1 数据结构变更

```java
// ★ 旧：Map<UnionKey, Converter<?, ?>>   — 每个类型对一个，后注册覆盖先注册
// ★ 新：Map<UnionKey, ReadWriteArrayList<Converter<?, ?>>> — 支持多个 Converter（因冒泡产生）
private final Map<UnionKey, ReadWriteArrayList<Converter<?, ?>>> converters;

// ★ 不变：Map<UnionKey, ReadWriteArrayList<ConditionConverter<?, ?>>>
private final Map<UnionKey, ReadWriteArrayList<ConditionConverter<?, ?>>> conditionConverters;

// ★ 新增：追踪原始 Converter → 其冒泡副本的 UnionKey 列表
// 用于 unregister 时同时移除全部冒泡副本
private final Map<Converter<?, ?>, List<UnionKey>> originalToBubbledKeys;
```

### 4.2 registerOne() 变更

```java
private void registerOne(Converter<?, ?> converter, Class<?> srcType,
                         Class<?> targetType, int registrationOrder) {
    UnionKey key = new UnionKey(srcType, targetType);

    if (converter instanceof ConditionConverter) {
        conditionConverters.computeIfAbsent(key, k -> new ReadWriteArrayList<>())
                .add((ConditionConverter<?, ?>) converter);
        sortConverterList(key, true);  // ★ 用新排序器
    } else {
        // ★ 旧：converters.put(key, converter)
        // ★ 新：加入列表
        converters.computeIfAbsent(key, k -> new ReadWriteArrayList<>())
                .add(converter);
        sortConverterList(key, false);  // ★ 用新排序器
    }
}
```

### 4.3 排序算法

```java
/**
 * 获取 Converter 的原始目标类型名称用于排序
 * - BubbledConverter: 通过 delegate.getTargetType() 获取原始目标类名
 * - 普通 Converter: 直接用 getTargetType()
 */
private static String getOriginalTargetTypeName(Converter<?, ?> c) {
    if (c instanceof BubbledConverter) {
        return ((BubbledConverter<?, ?>) c).getDelegate().getTargetType().getName();
    }
    return c.getTargetType().getName();
}

// ★ 统一排序比较器
private static final Comparator<Converter<?, ?>> CONVERTER_ORDER_COMPARATOR = Comparator
    .comparingInt(Converter::registrationOrder)                           // 1. 注册顺序（升序）
    .thenComparingInt(Converter::order)                                   // 2. 用户定义顺序（升序）
    .thenComparing(c -> c.getSourceType().getName())                      // 3. 源类名（字母升序）
    .thenComparing(DefaultConverterRegistry::getOriginalTargetTypeName);  // 4. 原始目标类名（字母升序）

/**
 * 对指定 key 的所有列表排序（condition + regular 统一处理）
 */
private void sortConverterList(UnionKey key) {
    // 排序 ConditionConverter 列表
    ReadWriteArrayList<ConditionConverter<?, ?>> ccList = conditionConverters.get(key);
    if (ccList != null && ccList.size() > 1) {
        ccList.sort((Comparator) CONVERTER_ORDER_COMPARATOR);
    }
    // 排序普通 Converter 列表
    ReadWriteArrayList<Converter<?, ?>> cvList = converters.get(key);
    if (cvList != null && cvList.size() > 1) {
        cvList.sort((Comparator) CONVERTER_ORDER_COMPARATOR);
    }
}
```

**排序规则（完整链）：**
1. `registrationOrder()` 升序 — 精确注册(=0)优先，冒泡层级越高(=1,2...)越靠后
2. `order()` 升序 — 用户自定义优先级细化
3. `getSourceType().getName()` 字母升序 — 稳定排序，同一 key 内所有项相同，无实际区分效果
4. **原始目标类名** 字母升序 — 通过 delegate 获取冒泡前的原始目标类名，区分不同来源的冒泡副本

**示例：** `(String, Number)` 键下有 8 个冒泡副本：
```
String→Integer 冒泡副本: regOrder=1, order=0, source="String", original="Integer"
String→Long    冒泡副本: regOrder=1, order=0, source="String", original="Long"
String→BigDecimal 冒泡副本: regOrder=1, order=0, source="String", original="BigDecimal"
...
```
排序后顺序：BigDecimal < Integer < Long（字母升序）

### 4.4 findConverters() 变更

```java
@Override
public <S, T> ConverterSearchResult<S, T> findConverters(Class<S> source, Class<T> target) {
    if (source == null || target == null) {
        return new ConverterSearchResult<>(Collections.emptyList(), Collections.emptyList());
    }

    UnionKey key = new UnionKey(source, target);

    // 获取 ConditionConverter 列表
    List<ConditionConverter<?, ?>> conditionList = conditionConverters.get(key);

    // ★ 获取普通 Converter 列表（之前是单个 Converter）
    List<Converter<?, ?>> converterList = converters.get(key);

    return new ConverterSearchResult<>(
        (List) (conditionList != null ? conditionList : Collections.emptyList()),
        (List) (converterList != null ? converterList : Collections.emptyList())
    );
}
```

### 4.5 getConverterByValue() 变更

```java
@Override
public <S, T> Converter<S, T> getConverterByValue(
        S source, Class<S> sourceType, Class<T> target) {
    if (source == null || sourceType == null || target == null) return null;

    ConverterSearchResult<S, T> result = findConverters(sourceType, target);

    // 1. 遍历 ConditionConverter（已按 registrationOrder+order 排序）
    for (ConditionConverter<S, T> cc : result.getConditionConverters()) {
        if (cc.matches(source)) {
            return cc;
        }
    }

    // 2. 返回普通 Converter 列表的第一个（已按 registrationOrder+order 排序）
    List<Converter<S, T>> converters = result.getConverters();
    if (!converters.isEmpty()) {
        return converters.get(0);
    }

    return null;
}
```

### 4.6 unregister() 变更

```java
@Override
public <S, T> boolean unregister(Converter<S, T> converter) {
    if (converter == null) return false;
    Class<?> srcType = converter.getSourceType();
    Class<?> targetType = converter.getTargetType();
    if (srcType == null || targetType == null) return false;

    boolean removed = unregisterOne(converter, srcType, targetType);

    // ★ 如果移除的是原始 Converter，同时移除所有冒泡副本
    List<UnionKey> bubbledKeys = originalToBubbledKeys.remove(converter);
    if (bubbledKeys != null) {
        for (UnionKey key : bubbledKeys) {
            // 从列表中移除关联的冒泡副本
            removeBubbledCopies(converter, key);
        }
    }

    return removed;
}

private boolean unregisterOne(Converter<?, ?> converter,
                              Class<?> srcType, Class<?> targetType) {
    UnionKey key = new UnionKey(srcType, targetType);
    boolean removed = false;

    if (converter instanceof ConditionConverter) {
        List<ConditionConverter<?, ?>> list = conditionConverters.get(key);
        if (list != null) {
            removed = list.remove(converter);
        }
    } else {
        // ★ 旧：converters.remove(key)
        // ★ 新：从列表中移除
        List<Converter<?, ?>> list = converters.get(key);
        if (list != null) {
            removed = list.remove(converter);
            // 如果列表为空，清理条目
            if (list.isEmpty()) {
                converters.remove(key);
            }
        }
    }
    return removed;
}

/**
 * 移除指定原始 Converter 在目标 UnionKey 下的所有冒泡副本
 */
private void removeBubbledCopies(Converter<?, ?> original, UnionKey key) {
    List<Converter<?, ?>> list = converters.get(key);
    if (list == null) {
        list = (List) conditionConverters.get(key);
    }
    if (list == null) return;

    list.removeIf(c -> isBubbledCopyOf(c, original));
    if (list.isEmpty()) {
        converters.remove(key);
    }
}

/**
 * 判断 converter 是否是 original 的冒泡副本
 */
private boolean isBubbledCopyOf(Converter<?, ?> converter, Converter<?, ?> original) {
    if (converter instanceof BubbledConverter) {
        return ((BubbledConverter<?, ?>) converter).getDelegate() == original;
    }
    return false;
}
```

### 4.7 clear() 变更

```java
@Override
public void clear() {
    conditionConverters.clear();
    converters.clear();
    originalToBubbledKeys.clear();  // ★ 新增
}
```

## 5. ConverterSearchResult 变更

### 5.1 结构调整

```java
public class ConverterSearchResult<S, T> {

    private final List<ConditionConverter<S, T>> conditionConverters;
    // ★ 改为 List（之前是单个 Converter）
    private final List<Converter<S, T>> converters;

    public ConverterSearchResult(
            List<ConditionConverter<S, T>> conditionConverters,
            List<Converter<S, T>> converters) {
        this.conditionConverters = conditionConverters != null ? conditionConverters : Collections.emptyList();
        this.converters = converters != null ? converters : Collections.emptyList();
    }

    public List<ConditionConverter<S, T>> getConditionConverters() {
        return conditionConverters;
    }

    // ★ 新增：返回完整列表
    public List<Converter<S, T>> getConverters() {
        return converters;
    }

    // ★ 变更：返回列表中第一个，或 null
    public Converter<S, T> getConverter() {
        return converters.isEmpty() ? null : converters.get(0);
    }

    /**
     * ★ 变更：返回合并排序后的 List（ConditionConverter在前，普通Converter在后）
     * 两者内部均已按 (registrationOrder, order) 排序
     */
    public List<Converter<S, T>> getAllConverters() {
        List<Converter<S, T>> result = new ArrayList<>(
                conditionConverters.size() + converters.size());
        result.addAll(conditionConverters);
        result.addAll(converters);
        return result;
    }

    public boolean isEmpty() {
        return conditionConverters.isEmpty() && converters.isEmpty();
    }

    public int size() {
        return conditionConverters.size() + converters.size();
    }
}
```

### 5.2 getConverter() 的调用方影响

`DefaultConverterRegistry.getConverter()` 当前使用：

```java
// 当前调用方式：
protected Converter<S, T> getConverter(Class<S> sourceType, Class<T> target) {
    ...
    ConverterSearchResult<S, T> result = findConverters(sourceType, target);
    return result.getConverter();  // 返回列表第一个
}
```

行为一致（返回第一个），但语义变化：
- 旧：返回唯一的精确匹配 Converter
- 新：返回排序后的列表中的第一个（可能是原始注册或冒泡副本，取决于 order + registrationOrder）

## 6. ConverterConstants 变更

```java
public class ConverterConstants {
    private ConverterConstants() {}

    /** 默认 order 值 */
    public static final int ORDER_DEFAULT = 0;

    /** 最高优先级 */
    public static final int ORDER_HIGHEST = Integer.MIN_VALUE;

    /** 最低优先级 */
    public static final int ORDER_LOWEST = Integer.MAX_VALUE;

    // ======== ★ 新增 ========

    /** 默认冒泡层数（向上冒泡 1 层：父类 + 直接接口） */
    public static final int BUBBLE_DEFAULT = 1;

    /** 无限冒泡标记 */
    public static final int BUBBLE_UNLIMITED = -1;

    /** 不冒泡 */
    public static final int BUBBLE_NONE = 0;

    /** 原始注册的默认 registrationOrder */
    public static final int REGISTRATION_ORDER_DEFAULT = 0;
}
```

## 7. 改动清单

### 7.1 Converter.java — 接口变更

| 改动类型 | 方法 | 说明 |
|----------|------|------|
| 新增 default 方法 | `bubbleLevel()` | 返回 int，默认 1 |
| 新增 default 方法 | `registrationOrder()` | 返回 int，默认 0 |
| 新增 default 方法 | `order()` | 返回 int，默认 0 |
| 已有方法 | `convert()`, `getSourceType()`, `getTargetType()` | 不变 |

### 7.2 ConditionConverter.java — 无变更

`order()` 已存在，默认值 0，与 `Converter.order()` 默认值一致，自然覆盖。

### 7.3 DefaultConverterRegistry.java — 核心变更

| 组件 | 变更类型 | 说明 |
|------|----------|------|
| `converters` 字段 | 类型变更 | `Map<UnionKey,Converter>` → `Map<UnionKey,ReadWriteArrayList<Converter>>` |
| `originalToBubbledKeys` 字段 | 新增 | `Map<Converter, List<UnionKey>>` 关联追踪 |
| `register()` | 逻辑增强 | 注册原始 + 调用 `registerWithBubble()` |
| `registerWithBubble()` | 新增 | 冒泡注册入口 |
| `bubbleToHierarchy()` | 新增 | 递归遍历父类链 + 接口链 |
| `registerBubbledCopyIfAbsent()` | 新增 | 创建包裹 Converter + 去重检查 |
| `createBubbledCopy()` | 新增 | 工厂方法：创建 BubbledConverter/BubbledConditionConverter |
| `safeIncrementRegOrder()` | 新增 | 安全递增 registrationOrder |
| `registerOne()` | 修改 | 适配新存储结构（List），接收 registrationOrder 参数 |
| `sortConverterList()` | 新增 | 统一排序（替换原 `sortConditionConverters()`） |
| `findConverters()` | 修改 | 返回 List 格式的转换器查询结果 |
| `getConverterByValue()` | 修改 | 适配列表查询：排序后的 List 取第一个匹配 |
| `getConverter()` | 微调 | 返回 `result.getConverter()`（列表第一个） |
| `unregister()` | 增强 | 同时移除原始 + 冒泡副本 |
| `unregisterOne()` | 修改 | 从 List 中移除，非 Map 直接 remove |
| `removeBubbledCopies()` | 新增 | 批量移除冒泡副本 |
| `clear()` | 增强 | 新增 `originalToBubbledKeys.clear()` |

### 7.4 ConverterSearchResult.java — 结构调整

| 组件 | 变更类型 | 说明 |
|------|----------|------|
| `converters` 字段 | 类型变更 | `Converter<T>` → `List<Converter<T>>` |
| 构造器 | 参数变更 | 接受 `List<Converter<T>>` 而非单个 |
| `getConverter()` | 语义变更 | 返回列表中第一个（或 null） |
| `getConverters()` | 新增 | 返回完整列表 |
| `getAllConverters()` | 行为变更 | 合并 List（condition + regular），均为已排序状态 |
| `isEmpty()` / `size()` | 微调 | 适配新结构 |

### 7.5 ConverterConstants.java — 新增常量

| 常量 | 值 | 说明 |
|------|-----|------|
| `BUBBLE_DEFAULT` | 1 | 默认冒泡层数 |
| `BUBBLE_UNLIMITED` | -1 | 无限冒泡标志 |
| `BUBBLE_NONE` | 0 | 不冒泡 |
| `REGISTRATION_ORDER_DEFAULT` | 0 | 原始注册的默认 registrationOrder |

### 7.6 ConverterUtils.java — 无变更

匿名 Converter 通过 `ConverterUtils.of()` 创建，默认继承 `bubbleLevel=1`、`registrationOrder=0`、`order=0`。
此行为是期望的：默认向上冒泡一层无需用户额外配置。

### 7.7 DefaultConverters.java 和各 default 转换器 — 无变更

默认转换器不重写 `bubbleLevel()`，初始 `order=0`，`registrationOrder=0`。
继承默认行为 `bubbleLevel=1`，即在默认转换器注册时自动向上冒泡一层。

## 8. 查找排序完整流程

### 8.1 convert(S source, Class<T> target) 的查找流程

```
convert(source, target)
  │
  ├─ 1. null/identity 检查
  │
  ├─ 2. 目标为基础类型 → 先找基础类型转换器，再转包装类型
  │     └─ 最终调用 getConverterByValue()
  │
  ├─ 3. 目标为包装类型/源为基础类型 → 同上
  │
  ├─ 4. 自动转换
  │     └─ 调用 getConverterByValue()
  │
  └─ 5. 全部返回 null → 抛 ConverterException
```

### 8.2 getConverterByValue() 的查找排序

```
getConverterByValue(source, sourceType, target)
  │
  ├─ findConverters(sourceType, target)
  │     ├─ 从 conditionConverters 获取 ConditionConverter 列表（已排序）
  │     └─ 从 converters 获取普通 Converter 列表（已排序）
  │
  ├─ 排序依据：registrationOrder ASC → order ASC
  │
  ├─ 1. 遍历 ConditionConverter（按排序后顺序）
  │     └─ cc.matches(source) == true → 返回该 cc
  │
  ├─ 2. ConditionConverter 无匹配 → 遍历普通 Converter（按排序后顺序）
  │     └─ 返回列表第一个（普通 Converter 无条件，直接可用）
  │
  └─ 3. 均无匹配 → 返回 null
```

### 8.3 排序规则验证示例

注册以下三个 Converter（均实现 `String → Number`）：

| Converter | registrationOrder | order |
|-----------|------------------|-------|
| A（用户自定义，精确注册） | 0 | 5 |
| B（冒泡自其他 Converter） | 1 | 2 |
| C（冒泡自其他 Converter） | 1 | 5 |

排序后：A → B → C

查找时：
1. 先检查 A（非 ConditionConverter）
2. 发现 A 是普通 Converter → 直接使用 A
3. B 和 C 不会被检查到

若 A 不存在：
1. 检查 B（第 1 顺序）→ 使用 B

若没有精确匹配（registrationOrder=0）的 Converter，按 registrationOrder=1 中 order 最优先的取。

## 9. 未解决的问题与后续考虑

| 问题 | 思考 | 状态 |
|------|------|------|
| `unregister()` 移除冒泡副本的完整性 | 目前通过 `originalToBubbledKeys` 追踪，但若手动注册了同 UnionKey 的另一个 Converter，移除时可能误删 | 不影响当前版本，后续可增加引用计数 |
| 默认 `bubbleLevel=1` 的兼容性 | 现有 Converter 之前不冒泡，升级后自动获得 1 层冒泡。对用户透明，需要测试保障 | 需在构建时确认 |
| 并发安全 | `bubbleToHierarchy` 在 `register()` 中执行，多个线程同时注册时 `bubbledKeys` 是局部变量，线程安全 | 正确 |
| 冒泡副本序列化 | `BubbledConverter` 持有 delegate 引用，`getTargetType()` 返回的是新目标类型 | 非 Web 工具库，暂不涉及序列化 |
