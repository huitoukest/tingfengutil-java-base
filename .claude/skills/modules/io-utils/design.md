# IOUtils 设计原则

## 职责定位

IOUtils 是 **纯流处理工具类**，专注于输入输出流之间的转换与处理。

## 核心原则

1. **不直接操作文件** - 文件操作全部委托给 FileUtils
2. **统一运行时异常** - 抛出项目自定义 IOException，不抛检查型异常
3. **try-with-resources** - 所有资源使用自动关闭
4. **线程池外部注入** - 异步方法支持 ExecutorService / Thread / Runnable

## CancellationToken 设计

```java
public static class CancellationToken {
    volatile boolean cancelled
    long timeoutMillis  // 存储截止时间点，非时长

    void cancel()
    boolean isCancelled()
    boolean isTimeout()
    boolean shouldInterrupt()  // cancelled || isTimeout()
    void reset()
}
```

## 背压机制

```
Semaphore(permits = backPressureLimit / bufferSize)
读取 → 缓冲区 → 写入 → release()
                    ↑
              acquire() 阻塞
```

## 常量定义

| 常量 | 值 | 说明 |
|------|-----|------|
| `BUFFER_SIZE` | 4096 | 默认缓冲区大小 |
| `DEFAULT_BACK_PRESSURE_BUFFER_SIZE` | 8MB | 默认背压缓冲区上限 |

## 注意事项

1. **禁止在 IOUtils 中出现 File 参数** - 文件操作归 FileUtils
2. **所有流必须关闭** - 使用 try-with-resources 或 closeQuietly
3. **flush 在 close 之前** - 确保数据写出
4. **大文件避免一次性加载** - 使用流式处理 + 分块
5. **CancellationToken 必须是 public static class** - 供 FileUtils 引用
