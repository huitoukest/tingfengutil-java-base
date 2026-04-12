# IOUtils 方法签名

## 流创建（输入）

| 方法 | 说明 |
|------|------|
| `static InputStream toInputStream(String)` | 字符串 → InputStream (UTF-8) |
| `static InputStream toInputStream(String, Charset)` | 字符串 → InputStream (指定编码) |
| `static InputStream toInputStream(byte[])` | 字节数组 → InputStream |

## 流创建（输出）

| 方法 | 说明 |
|------|------|
| `static ByteArrayOutputStream createByteArrayOutputStream()` | 创建 ByteArrayOutputStream |

## 流转换

| 方法 | 说明 |
|------|------|
| `static byte[] toByteArray(InputStream)` | InputStream → byte[] |
| `static String toString(InputStream)` | InputStream → String (UTF-8) |
| `static String toString(InputStream, Charset)` | InputStream → String (指定编码) |
| `static Reader toReader(InputStream, Charset)` | InputStream → Reader |
| `static Writer toWriter(OutputStream)` | OutputStream → Writer |

## 流拷贝

| 方法 | 说明 |
|------|------|
| `static long copy(OutputStream, InputStream)` | 同步拷贝（自动关闭） |
| `static long copy(OutputStream, InputStream, boolean)` | 同步拷贝（可配置关闭） |
| `static long copy(OutputStream, InputStream, Consumer<Long>)` | 同步拷贝（带进度回调） |
| `static long copy(OutputStream, InputStream, int, boolean, Consumer<Long>)` | 完整参数版本 |

## 流读取

| 方法 | 说明 |
|------|------|
| `static List<String> readLines(InputStream, Consumer<String>)` | 按行读取（UTF-8） |
| `static List<String> readLines(InputStream, Charset, Consumer<String>)` | 按行读取（指定编码） |

## 资源关闭

| 方法 | 说明 |
|------|------|
| `static void closeQuietly(Closeable)` | 安全关闭单个资源 |
| `static void closeQuietly(Closeable...)` | 安全关闭多个资源 |

## 管道连接

| 方法 | 说明 |
|------|------|
| `static void pipe(OutputStream, InputStream)` | 同步管道连接 |
| `static void pipe(OutputStream, InputStream, int)` | 同步管道连接（指定缓冲区） |
| `static void pipe(Writer, Reader)` | 字符流管道连接 |
| `static void pipe(Writer, Reader, int)` | 字符流管道连接（指定缓冲区） |
| `static void joinStreams(OutputStream, InputStream...)` | 合并多个输入流到单一输出 |

## 缓冲包装

| 方法 | 说明 |
|------|------|
| `static BufferedReader toBufferedReader(InputStream, Charset)` | 包装为 BufferedReader |
| `static BufferedOutputStream toBufferedOutputStream(OutputStream)` | 包装为 BufferedOutputStream |
| `static BufferedInputStream toBufferedInputStream(InputStream)` | 包装为 BufferedInputStream |

## 异步流处理

| 方法 | 说明 |
|------|------|
| `static CompletableFuture<Long> copyAsync(...)` | 异步流拷贝（带进度+背压） |
| `static void readLinesAsync(...)` | 异步按行读取（支持背压） |
| `static void readStreamWithBackpressure(...)` | 批处理模式（累积N行后回调） |
| `static void transmitStream(...)` | 流水线传输（读→处理→写） |
| `static ExecutorService toExecutorService(Object)` | Thread/Runnable/ExecutorService 统一转换 |
