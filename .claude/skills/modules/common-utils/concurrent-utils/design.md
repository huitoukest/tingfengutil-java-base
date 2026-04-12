# ConcurrentUtils 设计原则

## 设计目标

提供线程相关工具方法，包括 ThreadLocal 管理和线程上下文处理。

## 核心能力

1. **ThreadLocal 管理** - 安全移除值，防止内存泄漏
2. **线程上下文** - 获取/设置线程上下文信息
3. **线程操作** - sleep, interrupt 等

## ThreadLocal 内存泄漏防护

```java
// 正确做法：使用后立即 remove
ThreadLocal<String> tl = new ThreadLocal<>();
tl.set("value");
try {
    // 使用 value
} finally {
    tl.remove();  // 防止内存泄漏
}

// 错误做法：仅 set 不 remove
ThreadLocal<String> tl = new ThreadLocal<>();
tl.set("value");
// ... 长时间持有导致内存泄漏
```

## 方法命名规范

| 操作 | 命名 | 示例 |
|------|------|------|
| 移除值 | `remove(ThreadLocal)` | 返回原值并移除 |
| 清除所有 | `clearAllThreadLocals()` | 反射清除当前线程所有 ThreadLocal |
| 线程数 | `threadLocalCount()` | 获取当前线程 ThreadLocal 数量 |
