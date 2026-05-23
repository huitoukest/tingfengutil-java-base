# Converter 冒泡注册机制 - 设计文档

## 基本信息

| 项目 | 内容 |
|------|------|
| 创建时间 | 2026-05-23 |
| 最后更新 | 2026-05-23 |
| 状态 | 已确认 |
| 相关文档 | `.code/docs/story/converter-bubble-registration.md` |

## 1. 接口变更：Converter.java

新增三个默认方法：

```java
public interface Converter<S, T> {
    T convert(S source);
    Class<S> getSourceType();
    Class<T> getTargetType();
    
    /**
     * 可冒泡层数。注册时生效，控制向上注册到第几层父类
     * 0 = 不冒泡，仅注册自身
     * 1 = 注册自身 + 直接父类
     * n = 注册自身 + 向上 n 层父类
     * -1 = 无限制，冒泡到 Object 为止，且冒泡产生的副本也保持 -1
     * @return 冒泡层数
     */
    default int bubbleLevel() {
        return 1;
    }
    
    /**
     * 注册顺序。内部使用，由注册工具自动计算
     * 原始注册的 Converter 为 0，每冒泡一层 +1
     * @return 注册顺序
     */
    default int registrationOrder() {
        return 0;
    }
    
    /**
     * 用户定义的排序顺序。registrationOrder 相同时用于排序
     * @return 排序值，越小越优先
     */
    default int order() {
        return 0;
    }
}
```

## 2. 冒泡注册逻辑

### 核心流程

```
register(S→T Converter):
  1. 记录所有受影响的 UnionKey（Set<UnionKey>）
  
  2. 注册原始 Converter（自身）
     - 若为 ConditionConverter → 加入 conditionConverters 列表
     - 若为普通 Converter → 加入 converters 列表
     
  3. 如果 bubbleLevel != 0，处理冒泡：
     a. 复制 bubbleLevel 为 remainingBubbleLevel
     b. 收集当前目标类的父类型（先父类链，后接口链）
     c. 对每个父类型：
        - 计算副本的 remainingBubbleLevel：
          * 原始 = -1 时 → 副本 = -1
          * 原始 > 0 时 → 副本 = remainingBubbleLevel - 1
        - 计算副本 registrationOrder = 原始 registrationOrder + 1
        - 创建 BubbleConverterWrapper(delegate, targetType, registrationOrder, remainingBubbleLevel)
        - 加入对应列表
        - 记录 key 到 affectedKeys
     d. 递归处理每个父类型的父类型（若 remainingBubbleLevel ≠ 0）

  4. 对所有 affectedKeys 执行 sortConverters(key)
```

### BubbleConverterWrapper

```java
class BubbleConverterWrapper<S, T> implements Converter<S, T> {
    final Converter<S, T> delegate;       // 原始 Converter（包可见，供 Comparator 访问）
    private final Class<T> targetType;     // 冒泡副本的目标类型
    private final int registrationOrder;
    private final int bubbleLevel;
    
    @Override public T convert(S source) { return delegate.convert(source); }
    @Override public Class<S> getSourceType() { return delegate.getSourceType(); }
    @Override public Class<T> getTargetType() { return targetType; }
    @Override public int registrationOrder() { return registrationOrder; }
    @Override public int bubbleLevel() { return bubbleLevel; }
    @Override public int order() { return delegate.order(); }
}
```

### bubbleLevel 递减规则

| 场景 | 原始 bubbleLevel | 冒泡副本 bubbleLevel |
|------|-----------------|---------------------|
| 有限冒泡 | 2 | 第1层: 1, 第2层: 0 |
| 无限制 | -1 | 各层: -1(保持不变) |

### 冒泡停止条件

- 当前类到达 Object（getSuperclass 返回 null）
- 当前接口无父接口
- bubbleLevel 减到 0
- 这三者任一满足则停止

## 3. 排序机制

### 排序链

```java
private static final Comparator<Converter<?, ?>> CONVERTER_COMPARATOR = Comparator
    .comparingInt(Converter::registrationOrder)                          // 1. 注册顺序（升序）
    .thenComparingInt(Converter::order)                                  // 2. 用户定义顺序（升序）
    .thenComparing(c -> c.getSourceType().getName())                     // 3. 源类名（字母升序）
    .thenComparing(DefaultConverterRegistry::getOriginalTargetName);     // 4. 原始目标类名（字母升序）

// 获取用于排序的原始目标类名
private static String getOriginalTargetName(Converter<?, ?> c) {
    if (c instanceof BubbleConverterWrapper) {
        return ((BubbleConverterWrapper<?, ?>) c).delegate.getTargetType().getName();
    }
    return c.getTargetType().getName();
}
```

### 排序时机

每次 `register()` 完成后，对 affectedKeys 中所有 UnionKey 对应的列表执行一次排序，**不按新增元素逐次排序**。

## 4. 存储变更：普通 Converter 列表化

```java
// 原：Map<UnionKey, Converter<?, ?>> 单个
// 改：Map<UnionKey, ReadWriteArrayList<Converter<?, ?>>> 列表
private final Map<UnionKey, ReadWriteArrayList<Converter<?, ?>>> converters;

// 原：registerOne 中 put(key, converter)
// 改：registerOne 中 add 到列表
private void registerOne(Converter<?, ?> converter, Class<?> srcType, Class<?> targetType) {
    UnionKey key = new UnionKey(srcType, targetType);
    if (converter instanceof ConditionConverter) {
        conditionConverters.computeIfAbsent(key, k -> new ReadWriteArrayList<>())
                .add((ConditionConverter<?, ?>) converter);
    } else {
        converters.computeIfAbsent(key, k -> new ReadWriteArrayList<>())
                .add(converter);
    }
}
```

## 5. ConverterSearchResult 调整

```java
public class ConverterSearchResult<S, T> {
    // 原: Converter<S, T> converter（单个）
    // 改: List<Converter<S, T>> converters（列表）
    private final List<ConditionConverter<S, T>> conditionConverters;
    private final List<Converter<S, T>> converters;
    
    public ConverterSearchResult(
            List<ConditionConverter<S, T>> conditionConverters,
            List<Converter<S, T>> converters) {
        this.conditionConverters = conditionConverters != null ? conditionConverters : Collections.emptyList();
        this.converters = converters != null ? converters : Collections.emptyList();
    }
    
    public List<Converter<S, T>> getConverters() { return converters; }
    
    // getAllConverters() 合并两个列表并排序
    public List<Converter<S, T>> getAllConverters() {
        List<Converter<S, T>> result = new ArrayList<>(
            conditionConverters.size() + converters.size());
        result.addAll(conditionConverters);
        result.addAll(converters);
        result.sort(CONVERTER_COMPARATOR);  // 使用排序好的列表（已缓存）
        return result;
    }
}
```

## 6. 查找逻辑调整

```java
// 原有：返回唯一的普通 Converter
// 改为：返回排序后列表的第一个（或 null）
public <S, T> Converter<S, T> getConverter(Class<S> sourceType, Class<T> target) {
    ReadWriteArrayList<Converter<?, ?>> list = converters.get(new UnionKey(sourceType, target));
    if (list != null && !list.isEmpty()) {
        return (Converter<S, T>) list.get(0);  // 已排序，第一个即最优
    }
    return null;
}

// findAll() / findConverters() 返回时已包含多个 Converter
public <S, T> ConverterSearchResult<S, T> findConverters(Class<S> source, Class<T> target) {
    UnionKey key = new UnionKey(source, target);
    List<ConditionConverter<?, ?>> ccList = conditionConverters.get(key);
    List<Converter<?, ?>> cvList = converters.get(key);
    return new ConverterSearchResult<>(
        (List) (ccList != null ? new ArrayList<>(ccList) : Collections.emptyList()),
        (List) (cvList != null ? new ArrayList<>(cvList) : Collections.emptyList())
    );
}
```

## 7. 影响范围

| 文件 | 变更类型 | 说明 |
|------|---------|------|
| `Converter.java` | 新增 3 个方法 | `bubbleLevel()`、`registrationOrder()`、`order()` |
| `DefaultConverterRegistry.java` | 主要修改 | 注册冒泡逻辑、Converter 列表化、排序 |
| `BubbleConverterWrapper.java` (新) | 新建 | 冒泡副本包装类 |
| `ConverterSearchResult.java` | 修改 | 支持多个普通 Converter |
| `ConverterUtils.java` | 轻微修改 | 注意 `of()` 创建的无名 Converter 默认 bubbleLevel=1 |
| `ConverterConstants.java` | 可选新增 | 可新增常量（如默认 bubbleLevel 常量） |

## 8. 边界处理

| 场景 | 处理 |
|------|------|
| `bubbleLevel = 0` | 仅注册自身，不冒泡 |
| `bubbleLevel = -1` | 冒泡到 Object 为止，副本保持 -1 |
| `registrationOrder > Integer.MAX_VALUE` | 取 Integer.MAX_VALUE |
| 数组类型 | `getSuperclass()` 返回 null，`getInterfaces()` 可能空 |
| 目标类 = Object | 无父类，无接口，停止冒泡 |
| unregister() | 同时移除原始 + 冒泡副本 |
| 同一列表内多个相同 Converter（去重） | 注册时检查是否已存在相同实例 |
