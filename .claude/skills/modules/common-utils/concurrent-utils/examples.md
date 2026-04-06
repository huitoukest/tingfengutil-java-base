# ConcurrentUtils 使用示例

## ThreadLocal 安全使用

```java
ThreadLocal<String> userContext = new ThreadLocal<>();

// 设置值
userContext.set("user-123");

try {
    // 在线程中使用
    String user = userContext.get();
    // ... 业务逻辑
} finally {
    // 始终移除，防止内存泄漏
    ThreadLocalUtils.remove(userContext);
}
```

## 线程隔离验证

```java
@Test
public void testThreadIsolation() throws InterruptedException {
    ThreadLocal<Integer> tl = new ThreadLocal<>();
    tl.set(100);

    Thread thread = new Thread(() -> {
        // 子线程看不到主线程的值
        assertNull(tl.get());
        tl.set(200);
        assertEquals(200, tl.get());
    });

    thread.start();
    thread.join();

    // 主线程值不受影响
    assertEquals(100, tl.get());
}
```

## 内存泄漏防护

```java
// 大对象通过 ThreadLocal 持有时，使用后必须移除
ThreadLocal<byte[]> buffer = new ThreadLocal<>();
buffer.set(new byte[1024 * 1024]);

try {
    // 使用 buffer
} finally {
    ThreadLocalUtils.remove(buffer);  // 释放引用，防止内存泄漏
}
```
