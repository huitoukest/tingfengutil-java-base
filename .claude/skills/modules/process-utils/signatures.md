# ProcessUtils 方法签名

## 同步执行（阻塞）

| 方法 | 说明 |
|------|------|
| `static ProcessResult execute(String command)` | 默认120秒超时 |
| `static ProcessResult execute(String command, long timeoutMs)` | 自定义超时 |
| `static ProcessResult execute(String command, long timeoutMs, Charset)` | 自定义超时和编码 |
| `static ProcessResult execute(ProcessConfig config)` | 配置化执行 |

## 流式回调执行（阻塞）

| 方法 | 说明 |
|------|------|
| `static void executeWithCallback(String, LineProcessor, LineProcessor)` | 简单行处理 |
| `static void executeWithCallback(String, long, IndexedLineProcessor)` | 带行号 |
| `static void executeWithCallback(String, long, TypedLineProcessor)` | 带类型（最通用） |

## 异步执行（非阻塞）

| 方法 | 说明 |
|------|------|
| `static CompletableFuture<ProcessResult> executeAsync(String)` | 返回 CompletableFuture |
| `static void executeAsyncWithCallback(String, TypedLineProcessor)` | 异步流式 |

## 进程管理

| 方法 | 说明 |
|------|------|
| `static boolean isAlive(Process)` | 检查进程是否存活 |
| `static void destroy(Process)` | 优雅终止 |
| `static boolean destroyForcibly(Process)` | 强制终止 |
| `static boolean gracefulShutdown(Process, long timeoutMs)` | 梯度终止 |
| `static boolean waitFor(Process, long timeoutMs)` | 等待进程结束 |

## 进程信息

| 方法 | 说明 |
|------|------|
| `static long currentPid()` | 当前JVM进程ID |
| `static long getPid(Process)` | 子进程PID |
| `static ProcessInfo getProcessInfo(Process)` | 获取进程信息 |

## 工具方法

| 方法 | 说明 |
|------|------|
| `static boolean isWindows()` | 判断是否为Windows |
| `static boolean isLinux()` | 判断是否为Linux |
| `static Charset getSystemCharset()` | 获取系统编码 |

## 函数式接口

```java
@FunctionalInterface
interface LineProcessor {
    void process(String line);
}

@FunctionalInterface
interface IndexedLineProcessor {
    void process(int lineNumber, String line);
}

@FunctionalInterface
interface TypedLineProcessor {
    void process(int lineNumber, String line, OutputStreamType type);
}

enum OutputStreamType {
    STDOUT,  // 标准输出
    STDERR   // 错误输出
}
```

## 结果模型

```java
class ProcessResult {
    int exitCode;
    String standardOutput;
    String errorOutput;
    boolean timedOut;
    long durationMs;
    String command;
    boolean started;
    String startFailureReason;

    boolean isSuccess();
    ErrorType getErrorType();
}

enum ErrorType {
    NONE, START_FAILED, TIMEOUT, EXIT_CODE
}
```
