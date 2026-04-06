# IOUtils 设计规范

## 职责定位

IOUtils 是 **纯流处理工具类**，专注于输入输出流之间的转换与处理。

## 核心原则

1. **不直接操作文件** - 文件操作全部委托给 FileUtils
2. **统一运行时异常** - 抛出项目自定义 IOException，不抛检查型异常
3. **try-with-resources** - 所有资源使用自动关闭
4. **线程池外部注入** - 异步方法支持 ExecutorService / Thread / Runnable

## 方法分类（已实现）

### 1. 流创建（输入）✅

| 方法 | 说明 | 状态 |
|------|------|------|
| `toInputStream(String)` | 字符串 → InputStream (UTF-8) | ✅ |
| `toInputStream(String, Charset)` | 字符串 → InputStream (指定编码) | ✅ |
| `toInputStream(byte[])` | 字节数组 → InputStream | ✅ |

### 2. 流创建（输出）✅

| 方法 | 说明 | 状态 |
|------|------|------|
| `createByteArrayOutputStream()` | 创建 ByteArrayOutputStream | ✅ |

### 3. 流转换 ✅

| 方法 | 说明 | 状态 |
|------|------|------|
| `toByteArray(InputStream)` | InputStream → byte[] | ✅ |
| `toString(InputStream)` | InputStream → String (UTF-8) | ✅ |
| `toString(InputStream, Charset)` | InputStream → String (指定编码) | ✅ |
| `toReader(InputStream, Charset)` | InputStream → Reader | ✅ |
| `toWriter(OutputStream)` | OutputStream → Writer | ✅ |

### 4. 流拷贝 ✅

| 方法 | 说明 | 状态 |
|------|------|------|
| `copy(OutputStream, InputStream)` | 同步拷贝（自动关闭） | ✅ |
| `copy(OutputStream, InputStream, boolean)` | 同步拷贝（可配置关闭） | ✅ |
| `copy(OutputStream, InputStream, Consumer<Long>)` | 同步拷贝（带进度回调） | ✅ |
| `copy(OutputStream, InputStream, int, boolean, Consumer<Long>)` | 完整参数版本 | ✅ |

### 5. 流读取 ✅

| 方法 | 说明 | 状态 |
|------|------|------|
| `readLines(InputStream, Consumer<String>)` | 按行读取（UTF-8） | ✅ |
| `readLines(InputStream, Charset, Consumer<String>)` | 按行读取（指定编码） | ✅ |

### 6. 资源关闭 ✅

| 方法 | 说明 | 状态 |
|------|------|------|
| `closeQuietly(Closeable)` | 安全关闭单个资源 | ✅ |
| `closeQuietly(Closeable...)` | 安全关闭多个资源 | ✅ |

### 7. 管道连接 ✅

| 方法 | 说明 | 状态 |
|------|------|------|
| `pipe(OutputStream, InputStream)` | 同步管道连接 | ✅ |
| `pipe(OutputStream, InputStream, int)` | 同步管道连接（指定缓冲区） | ✅ |
| `pipe(Writer, Reader)` | 字符流管道连接 | ✅ |
| `pipe(Writer, Reader, int)` | 字符流管道连接（指定缓冲区） | ✅ |
| `joinStreams(OutputStream, InputStream...)` | 合并多个输入流到单一输出 | ✅ |

### 8. 缓冲包装 ✅

| 方法 | 说明 | 状态 |
|------|------|------|
| `toBufferedReader(InputStream, Charset)` | 包装为 BufferedReader | ✅ |
| `toBufferedOutputStream(OutputStream)` | 包装为 BufferedOutputStream | ✅ |
| `toBufferedInputStream(InputStream)` | 包装为 BufferedInputStream | ✅ |

### 9. 异步流处理 ✅

**CancellationToken 设计：**
```java
public static class CancellationToken {
    volatile boolean cancelled
    long timeoutMillis

    void cancel()
    boolean isCancelled()
    boolean isTimeout()
    boolean shouldInterrupt()  // cancelled || isTimeout()
    void reset()
}
```

| 方法 | 说明 | 状态 |
|------|------|------|
| `copyAsync(...)` | 异步流拷贝（带进度+背压） | ✅ |
| `readLinesAsync(...)` | 异步按行读取（支持背压） | ✅ |
| `readLinesAsync(...)` (无背压) | 异步按行读取（无背压） | ✅ |
| `readStreamWithBackpressure(...)` | 批处理模式（累积N行后回调） | ✅ |
| `transmitStream(...)` | 流水线传输（读→处理→写） | ✅ |
| `toExecutorService(Object)` | Thread/Runnable/ExecutorService 统一转换 | ✅ |

**背压机制：**
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
