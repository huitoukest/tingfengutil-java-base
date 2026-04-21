# Converter 实现模板技能

## 概述

本模板用于实现 `com.tingfeng.util.java.base.bean.converter.defaults` 包下的类型转换器。

## 核心接口

### ConverterUtils.of() 重载

| 方法签名 | 用途 |
|---------|------|
| `of(Class<S> src, Class<T> target, Function<S,T> converter)` | 无条件转换，matcher 始终返回 true |
| `of(Class<S> src, Class<T> target, int order, Predicate<S> matcher, Function<S,T> converter)` | 条件转换，matcher 返回 true 时触发 |

### ConverterRegistry.register()

```java
registry.register(ConverterUtils.of(src, target, converter));           // 无条件
registry.register(ConverterUtils.of(src, target, order, matcher, converter)); // 条件
```

## 设计模式

### 1. 整数检测辅助方法

```java
private static boolean isInteger(BigDecimal bd) {
    return bd.remainder(BigDecimal.ONE).signum() == 0;
}
```

**原理**：`bd.remainder(BigDecimal.ONE)` 获取小数部分，结果符号为0表示整数。

**适用场景**：BigDecimal 转换为整数类型（byte/short/int/long/AtomicInteger/AtomicLong）时，先检测是否为整数再委托给 BigInteger 转换器。

### 2. 整数类型委托模式

**目的**：复用 BigInteger 的范围检查，保证语义一致性。

```java
// BigDecimal -> byte
registry.register(ConverterUtils.of(
    BigDecimal.class, byte.class,
    ConverterConstants.ORDER_DEFAULT,
    bd -> isInteger(bd) && bd.toBigInteger().compareTo(BIG_INTEGER_BYTE_MIN) >= 0
            && bd.toBigInteger().compareTo(BIG_INTEGER_BYTE_MAX) <= 0,
    bd -> bd.toBigInteger().byteValue()
));
```

**模式**：
```
匹配条件: isInteger(bd) && bd.toBigInteger() 在目标类型范围内
转换函数: bd.toBigInteger().xxxValue()
```

### 3. 浮点类型范围保护模式

**目的**：防止溢出。

```java
registry.register(ConverterUtils.of(
    BigDecimal.class, float.class,
    ConverterConstants.ORDER_DEFAULT,
    bd -> bd.compareTo(BIG_DECIMAL_FLOAT_MIN) >= 0
            && bd.compareTo(BIG_DECIMAL_FLOAT_MAX) <= 0,
    bd -> bd.floatValue()
));
```

**常量定义**（BigNumberConstants.java）：
```java
BigDecimal BIG_DECIMAL_FLOAT_MIN = BigDecimal.valueOf(-Float.MAX_VALUE);
BigDecimal BIG_DECIMAL_FLOAT_MAX = BigDecimal.valueOf(Float.MAX_VALUE);
BigDecimal BIG_DECIMAL_DOUBLE_MIN = BigDecimal.valueOf(-Double.MAX_VALUE);
BigDecimal BIG_DECIMAL_DOUBLE_MAX = BigDecimal.valueOf(Double.MAX_VALUE);
```

### 4. 单向互转设计原则

| 转换方向 | 设计决策 |
|---------|---------|
| BigDecimal → BigInteger | 仅整数可转（非整数会丢失小数部分） |
| BigInteger → BigDecimal | 始终可转（无精度丢失） |

### 5. 原子类型转换模式

```java
registry.register(ConverterUtils.of(
    BigDecimal.class, AtomicLong.class,
    ConverterConstants.ORDER_DEFAULT,
    bd -> isInteger(bd) && bd.toBigInteger().compareTo(BIG_INTEGER_LONG_MIN) >= 0
            && bd.toBigInteger().compareTo(BIG_INTEGER_LONG_MAX) <= 0,
    bd -> new AtomicLong(bd.toBigInteger().longValue())
));
```

## 注册入口

所有转换器在 `public static void register(ConverterRegistry registry)` 中注册：

```java
public static void register(ConverterRegistry registry) {
    registerBigIntegerToPrimitiveAndWrapper(registry);
    registerBigIntegerToAtomic(registry);
    registerBigDecimalToPrimitiveAndWrapper(registry);
    registerBigDecimalToAtomic(registry);
    registerBigFraction(registry);
    registerBigNumberInterchange(registry);
}
```

## 范围常量约定

| 类型 | 常量前缀 | 示例 |
|------|---------|------|
| byte | `BIG_INTEGER_BYTE_*` / `BIG_DECIMAL_BYTE_*` | `BIG_INTEGER_BYTE_MAX = BigInteger.valueOf(Byte.MAX_VALUE)` |
| short | `BIG_INTEGER_SHORT_*` / `BIG_DECIMAL_SHORT_*` | `BIG_INTEGER_SHORT_MAX = BigInteger.valueOf(Short.MAX_VALUE)` |
| int | `BIG_INTEGER_INT_*` / `BIG_DECIMAL_INT_*` | `BIG_INTEGER_INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE)` |
| long | `BIG_INTEGER_LONG_*` / `BIG_DECIMAL_LONG_*` | `BIG_INTEGER_LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE)` |
| float | `BIG_DECIMAL_FLOAT_*` | `BIG_DECIMAL_FLOAT_MAX = BigDecimal.valueOf(Float.MAX_VALUE)` |
| double | `BIG_DECIMAL_DOUBLE_*` | `BIG_DECIMAL_DOUBLE_MAX = BigDecimal.valueOf(Double.MAX_VALUE)` |

## 注意事项

1. **不判空**：假设外部传入非 null 值，matcher 和 converter 均不处理 null 情况
2. **单向转换**：只做 A→B 转换，不考虑反向
3. **按序匹配**：多个转换器匹配同一类型对时，按注册顺序依次尝试条件匹配
4. **ORDER_DEFAULT**：通常使用 `ConverterConstants.ORDER_DEFAULT`（值为0）
