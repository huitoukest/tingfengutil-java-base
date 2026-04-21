# Converter 模块设计文档

> 创建时间：2026-04-21
> 更新时间：2026-04-21（补充分类原则）

---

## 核心分类原则

### 分类规则（最高规范）

**转换器的归类由源类型（Source Type）决定，与目标类型无关。**

> 原文：「只有 src 的类型是 xxx 时，对应的 Converter 的功能才归类到 xxx 类」

**规则**：以 `Converter<S, T>` 中的 `S`（源类型）作为分类依据。
- `String → BigDecimal` → 属于 `StringConverters`
- `BigDecimal → BigInteger` → 属于 `BigNumberConverters`
- `Long → BigDecimal` → 属于 `NumberConverters`（Long 是 Number 子类）
- `Integer → BigInteger` → 属于 `NumberConverters`
- `BigInteger → String` → 属于 `BigNumberConverters`
- `byte[] → String` → 属于 `ByteArrayConverters`

### 已有分类的源类型对应关系

| 分类文件 | 源类型（必须） | 转换器数量 |
|---------|--------------|-----------|
| `StringConverters` | `String` | ~16 |
| `NumberConverters` | `Number` 及其子类 | 6 |
| `DateTimeConverters` | `Date`/`LocalDateTime`/`LocalDate` | 6 |
| `ByteArrayConverters` | `byte[]` | 1 |
| `CollectionConverters` | `Object[]`/`List`/`primitive array` | 9 |
| `UrlConverters` | `String` | 2 |
| `BigNumberConverters` | `BigDecimal`/`BigInteger` | 当前含错误分类 |

### 归类错误清单（BigNumberConverters 中）

| 转换器 | 当前分类 | 正确分类 |
|-------|---------|---------|
| `String → BigDecimal` | BigNumberConverters | StringConverters |
| `String → BigInteger` | BigNumberConverters | StringConverters |
| `BigDecimal → BigInteger` | BigNumberConverters | BigNumberConverters（正确） |
| `BigInteger → BigDecimal` | BigNumberConverters | BigNumberConverters（正确） |
| `BigDecimal → String` | BigNumberConverters | BigNumberConverters（正确） |
| `BigInteger → String` | BigNumberConverters | BigNumberConverters（正确） |
| `Long → BigDecimal` | BigNumberConverters | NumberConverters |
| `Long → BigInteger` | BigNumberConverters | NumberConverters |
| `Integer → BigDecimal` | BigNumberConverters | NumberConverters |
| `Integer → BigInteger` | BigNumberConverters | NumberConverters |
| `Short → BigDecimal` | BigNumberConverters | NumberConverters |
| `Short → BigInteger` | BigNumberConverters | NumberConverters |
| `Byte → BigDecimal` | BigNumberConverters | NumberConverters |
| `Byte → BigInteger` | BigNumberConverters | NumberConverters |
| `Double → BigDecimal` | BigNumberConverters | NumberConverters |
| `Double → BigInteger` | BigNumberConverters | NumberConverters |
| `Float → BigDecimal` | BigNumberConverters | NumberConverters |
| `Float → BigInteger` | BigNumberConverters | NumberConverters |
| `Number → BigDecimal` | BigNumberConverters | NumberConverters |
| `Number → BigInteger` | BigNumberConverters | NumberConverters |

## 正确的 BigNumberConverters 应只保留

- `BigDecimal → BigInteger`
- `BigInteger → BigDecimal`
- `BigDecimal → String`
- `BigInteger → String`

## 正确的 NumberConverters 应补充

所有 `Number 子类 → BigDecimal/BigInteger` 的转换器。

---

## 核心接口

### Converter<S, T>

最基础的转换器接口。

```java
public interface Converter<S, T> {
    T convert(S source);
    Class<S> getSourceType();
    Class<T> getTargetType();
}
```

### ConditionConverter<S, T> extends Converter<S, T>

支持条件判断的转换器。

```java
public interface ConditionConverter<S, T> extends Converter<S, T> {
    boolean matches(S source);
    default int order() { return 0; }
}
```

### ConverterRegistry

转换器注册中心。

```java
public interface ConverterRegistry {
    void register(Converter<?, ?> converter);
    boolean unregister(Converter<?, ?> converter);
    List<Converter<?, ?>> findAll(Class<?> source, Class<?> target);
    ConverterSearchResult findConverters(Class<?> source, Class<?> target);
    <T> T convert(Object source, Class<T> target);
    <T> T convert(Object source, Class<T> target, T defaultValue);
    void clear();
    void resetConverter();
}
```

## 已知限制

### Number 转换泛型擦除问题

`NumberConverters` 注册了 `Number → Integer/Long/Double/...`，但由于 Java 泛型类型擦除，`findAll(Double.class, Integer.class)` 只能精确匹配 `UnionKey(Double, Integer)`，无法找到 `UnionKey(Number, Integer)`。

### 日期格式固定

String ↔ Date/LocalDateTime/LocalDate 使用固定格式，不支持自定义格式。