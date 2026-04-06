# ConcurrentUtils 方法签名

## 位置

`com.tingfeng.util.java.base.common.concurrent.ThreadLocalUtils`

## ThreadLocal 操作

```java
// 移除 ThreadLocal 的值并返回
public static <T> T remove(ThreadLocal<T> threadLocal)

// 清除当前线程的所有 ThreadLocal（通过反射实现）
public static int clearAllThreadLocals()

// 获取当前线程的 ThreadLocal 数量
public static int threadLocalCount()
```
