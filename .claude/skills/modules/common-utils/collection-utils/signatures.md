# CollectionUtils 方法签名

## 位置

`com.tingfeng.util.java.base.common.utils.CollectionUtils`

## 核心方法签名

### 集合判断

```java
// 判断是否为空
public static boolean isEmpty(Collection<?> collection)
public static boolean isNotEmpty(Collection<?> collection)

// 判断是否包含任意元素
public static boolean isNullOrEmpty(Collection<?> collection)
```

### 集合转换

```java
// List 转换
public static <T, R> List<R> toList(T[] array, FunctionROne<R, T> mapper)
public static <T, R> List<R> toList(Collection<T> collection, FunctionROne<R, T> mapper)

// Set 转换
public static <T, R> Set<R> toSet(Collection<T> collection, FunctionROne<R, T> mapper)
```

### 集合过滤

```java
public static <T> List<T> filter(Collection<T> collection, Predicate<T> predicate)
public static <T> List<T> filterNull(List<T> list)
```

### 集合分页

```java
public static <T> List<T> subList(List<T> list, int start, int end)
```
