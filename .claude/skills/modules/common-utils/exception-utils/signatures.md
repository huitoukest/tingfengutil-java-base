# ExceptionUtils 方法签名

## 位置

`com.tingfeng.util.java.base.common.utils.ThrowableUtils`

## 消息获取

```java
// 获取异常消息，无则返回默认值
public static String getMessage(Throwable throwable, String defaultMsg)

// 获取异常消息，无则返回空字符串
public static String getMessage(Throwable throwable)
```

## 根因获取

```java
public static Throwable getRootCause(Throwable throwable)
```

## 异常链处理

```java
// 从异常链中获取指定类型的异常
public static <T extends Throwable> T getThrowable(Throwable throwable, Class<T> clazz)

// 获取异常链上所有异常
public static List<Throwable> getCauseChain(Throwable throwable)

// 检查是否包含指定类型的异常
public static boolean isCauseOf(Throwable throwable, Class<? extends Throwable> clazz)
```

## 栈追踪

```java
// 完整栈追踪字符串
public static String getStackTraceAsString(Throwable throwable)

// 简化栈追踪（仅类名和方法名）
public static String getSimpleStackTrace(Throwable throwable)
```

## 解包

```java
// 解包 InvocationTargetException, ExecutionException 等
public static Throwable unwrap(Throwable throwable)
```

## 类型判断

```java
public static boolean isCheckedException(Throwable throwable)
public static boolean isUncheckedException(Throwable throwable)
```

## 被压制的异常

```java
public static List<Throwable> getSuppressed(Throwable throwable)
```
