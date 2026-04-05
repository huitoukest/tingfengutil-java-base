# Process 进程工具开发规范

## 一、概述

本项目进程工具封装 `ProcessUtils`，基于 Java 8 原生 `ProcessBuilder` + `Process` 实现，提供跨平台进程创建、执行、管理能力。

---

## 二、Java 8 兼容性

| Java 9+ 功能 | Java 8 替代方案 |
|-------------|----------------|
| `ProcessHandle.current().pid()` | `ManagementFactory.getRuntimeMXBean().getName().split("@")[0]` |
| `ProcessHandle.allProcesses()` | 无法直接获取，需维护子进程集合 |
| `Process.info()` | `process.exitValue()` 同步检查，`process.isAlive()` |
| `Process.destroyForcibly()` | `process.destroy()` + 检测 `isAlive()` 后再次 destroy |

---

## 三、包结构

```
common/utils/process/
  ├── constant/
  │     └── ProcessExitCode.java           // 退出码常量接口
  │
  ├── model/
  │     ├── ProcessConfig.java            // 配置类（建造者模式）
  │     ├── ProcessResult.java            // 执行结果
  │     └── ProcessInfo.java              // 进程信息
  │
  ├── exception/
  │     ├── ProcessException.java          // 基础异常
  │     ├── ProcessStartException.java    // 启动失败
  │     ├── ProcessTimeoutException.java   // 超时
  │     └── ProcessExitCodeException.java // 退出码异常
  │
  ├── function/
  │     ├── LineProcessor.java            // 函数式接口：行处理器
  │     ├── IndexedLineProcessor.java     // 函数式接口：带行号行处理器
  │     └── TypedLineProcessor.java      // 函数式接口：带类型行处理器
  │
  ├── ProcessWrapper.java                  // 资源封装（AutoCloseable）
  └── ProcessUtils.java                   // 核心工具类
```

---

## 四、常量定义

### 4.1 退出码常量

```java
public interface ProcessExitCode {
    int SUCCESS           = 0;     // 正常退出
    int GENERAL_ERROR     = 1;     // 一般错误
    int MISUSE_OF_SHELL   = 2;     // 命令使用错误
    int EXECUTE_ERROR     = 126;   // 命令不可执行
    int NOT_FOUND         = 127;   // 命令未找到
    int SIGNAL_TERMINATE  = 128;   // 被信号终止的基准码
    
    // 常用信号
    int SIGKILL           = 9;     // SIGKILL（强制终止）
    int SIGTERM           = 15;    // SIGTERM（优雅终止）
}
```

### 4.2 流类型枚举

```java
public enum OutputStreamType {
    STDOUT,  // 标准输出
    STDERR   // 错误输出
}
```

---

## 五、异常体系

```
ProcessException (RuntimeException)
  ├── ProcessStartException     // 进程启动失败
  │       reason: "command not found" | "permission denied" | "io error"
  │
  ├── ProcessTimeoutException   // 执行超时
  │       timeoutMs: long
  │
  └── ProcessExitCodeException  // 非0退出码
          exitCode: int
          isSignaled(): boolean  // 是否被信号终止
          getSignal(): int      // 获取信号编号
```

### 设计原则

| 原则 | 说明 |
|------|------|
| 继承 RuntimeException | 调用方无需强制 try-catch，符合快速失败原则 |
| 包含完整上下文 | 每个异常携带 command / exitCode / timeoutMs 等诊断信息 |
| 不吞没异常 | 异常必须传播，不允许空 catch |

---

## 六、函数式接口

### 6.1 LineProcessor

```java
@FunctionalInterface
public interface LineProcessor {
    void process(String line);
}
```

### 6.2 IndexedLineProcessor

```java
@FunctionalInterface
public interface IndexedLineProcessor {
    void process(int lineNumber, String line);
}
```

### 6.3 TypedLineProcessor

```java
@FunctionalInterface
public interface TypedLineProcessor {
    void process(int lineNumber, String line, OutputStreamType type);
}
```

### 使用示例

```java
// 场景1：简单行处理
ProcessUtils.executeWithCallback("ping -n 4 127.0.0.1",
    line -> System.out.println("[OUT] " + line),
    line -> System.err.println("[ERR] " + line)
);

// 场景2：带行号
ProcessUtils.executeWithCallback("cat file.txt",
    (lineNum, line) -> System.out.println(lineNum + ": " + line)
);

// 场景3：带类型（最通用）
ProcessUtils.executeWithCallback("python app.py",
    (lineNum, line, type) -> {
        if (type == OutputStreamType.STDOUT) {
            logger.info(line);
        } else {
            logger.error(line);
        }
    }
);

// 场景4：收集到List
List<String> out = new CopyOnWriteArrayList<>();
ProcessUtils.executeWithCallback("ls",
    out::add,    // stdout 行收集
    err::add     // stderr 行收集
);
```

---

## 七、结果模型

### 7.1 ProcessResult

```java
public class ProcessResult {
    private int exitCode;              // 退出码
    private String standardOutput;       // 标准输出
    private String errorOutput;         // 错误输出
    private boolean timedOut;          // 是否超时
    private long durationMs;            // 执行耗时
    private String command;             // 执行的命令
    private boolean started;            // 进程是否成功启动
    private String startFailureReason;  // 启动失败原因
    
    // 计算属性
    public boolean isSuccess() { 
        return started && exitCode == 0 && !timedOut; 
    }
    
    public ErrorType getErrorType() {
        if (!started) return ErrorType.START_FAILED;
        if (timedOut) return ErrorType.TIMEOUT;
        if (exitCode != 0) return ErrorType.EXIT_CODE;
        return ErrorType.NONE;
    }
}

public enum ErrorType {
    NONE,
    START_FAILED,
    TIMEOUT,
    EXIT_CODE
}
```

### 7.2 ProcessInfo

```java
public class ProcessInfo {
    private long pid;          // 进程ID
    private String command;     // 命令
    private boolean isAlive;    // 是否存活
    private Process process;    // 原始Process对象
}
```

### 7.3 ProcessConfig

```java
public class ProcessConfig {
    private String command;
    private long timeoutMs = 120_000;          // 默认120秒
    private Charset charset;                    // 默认系统编码
    private boolean inheritEnv = true;          // 继承环境变量
    private Map<String, String> extraEnv;      // 追加环境变量
    private File workingDir;                   // 工作目录
    private TypedLineProcessor stdoutProcessor; // stdout回调
    private TypedLineProcessor stderrProcessor; // stderr回调
    
    // 建造者模式
    public static Builder builder() { return new Builder(); }
}
```

---

## 八、核心实现要点

### 8.1 流式读取（防止死锁）

**问题**：stdout/stderr 缓冲区有限（约64KB），若主线程顺序读取任一stream，另一stream可能因缓冲区满而阻塞，导致死锁。

**解决方案**：双线程异步读取 + CompletableFuture 并行等待。

```java
public static ProcessResult execute(String command, long timeoutMs, Charset charset) {
    Process process = startProcess(command);
    
    // 双线程异步读取
    CompletableFuture<String> stdoutFuture = CompletableFuture.supplyAsync(() -> 
        readStream(process.getInputStream(), charset));
    
    CompletableFuture<String> stderrFuture = CompletableFuture.supplyAsync(() -> 
        readStream(process.getErrorStream(), charset));
    
    // 等待进程结束或超时
    boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
    
    if (!finished) {
        process.destroy();
        return buildResult(command, -1, "", "", true, 0);
    }
    
    // join 等待读取完成
    String stdout = stdoutFuture.join();
    String stderr = stderrFuture.join();
    return buildResult(command, process.exitValue(), stdout, stderr, false, duration);
}
```

### 8.2 跨平台命令适配

```java
private static String[] prepareCommand(String command) {
    if (isWindows()) {
        return new String[]{"cmd", "/c", command};
    } else {
        return new String[]{"sh", "-c", command};
    }
}

private static boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().contains("windows");
}

private static Charset getSystemCharset() {
    return isWindows() ? Charset.forName("GBK") : StandardCharsets.UTF_8;
}
```

### 8.3 梯度终止策略

```
收到关闭信号
    ↓
SIGTERM (软终止) → 等待 5 秒
    ↓
若仍存活
    ↓
SIGTERM 再次发送 → 等待 3 秒
    ↓
若仍存活
    ↓
SIGKILL (硬终止) → 立即
```

```java
public boolean gracefulShutdown(Process process, long timeoutMs) {
    // 1. 发送 SIGTERM
    process.destroy();
    
    if (waitFor(process, timeoutMs)) {
        return true;
    }
    
    // 2. 再次发送 SIGTERM
    process.destroy();
    if (waitFor(process, timeoutMs / 2)) {
        return true;
    }
    
    // 3. 强制终止（Java 8 兼容）
    return process.destroyForcibly();
}

private boolean waitFor(Process process, long timeoutMs) {
    long deadline = System.currentTimeMillis() + timeoutMs;
    while (System.currentTimeMillis() < deadline) {
        if (!process.isAlive()) {
            return true;
        }
        LockSupport.parkNanos(10_000_000); // 10ms
    }
    return false;
}
```

### 8.4 Java 8 兼容 destroyForcibly

```java
public boolean destroyForcibly(Process process) {
    // Java 9+ 直接用
    // process.destroyForcibly();
    
    // Java 8 兼容：destroy + 检测
    process.destroy();
    if (process.isAlive()) {
        // 在某些平台需要再调一次
        process.destroy();
    }
    return !process.isAlive();
}
```

### 8.5 子进程 PID 获取（Java 8 兼容）

```java
public static long getPid(Process process) {
    // Java 9+: ProcessHandle.of(process).get().pid()
    
    // Java 8: 通过反射获取 pid 字段
    try {
        Field pidField = process.getClass().getDeclaredField("pid");
        pidField.setAccessible(true);
        return (long) pidField.get(process);
    } catch (Exception e) {
        throw new RuntimeException("Cannot get PID", e);
    }
}
```

---

## 九、安全规范

### 9.1 命令注入防护

| 禁止 | 推荐 |
|------|------|
| `exec(userInput)` 直接执行 | 使用参数数组 `exec("grep", userInput)` |
| 字符串拼接命令 | 白名单校验 + 输入过滤 |

```java
// 白名单校验
private static final Set<String> ALLOWED_COMMANDS = Set.of("ls", "cat", "grep", "ping");

// 输入过滤（禁止 shell 元字符）
if (userInput.matches("[;|&$`\\\\]")) {
    throw new IllegalArgumentException("Invalid characters in input");
}
```

### 9.2 敏感信息

- 禁止在日志中打印完整命令（可能含密码）
- 错误信息中若包含命令，需脱敏处理

---

## 十、API 设计

### 10.1 方法清单

```
ProcessUtils
  │
  ├── 同步执行（阻塞）
  │     execute(String command)                              // 默认120秒超时
  │     execute(String command, long timeoutMs)
  │     execute(String command, long timeoutMs, Charset)
  │     execute(ProcessConfig config)
  │
  ├── 流式回调执行（阻塞）
  │     executeWithCallback(String command, LineProcessor stdout, LineProcessor stderr)
  │     executeWithCallback(String command, long timeoutMs, IndexedLineProcessor)
  │     executeWithCallback(String command, long timeoutMs, TypedLineProcessor)
  │
  ├── 异步执行（非阻塞）
  │     executeAsync(String command)                                 // 返回 CompletableFuture<ProcessResult>
  │     executeAsyncWithCallback(String command, TypedLineProcessor) // 异步流式
  │
  ├── 进程管理
  │     isAlive(Process)
  │     destroy(Process)                    // 优雅终止
  │     destroyForcibly(Process)           // 强制终止
  │     gracefulShutdown(Process, timeoutMs) // 梯度终止
  │     waitFor(Process, timeoutMs)
  │
  ├── 进程信息
  │     currentPid()                       // 当前JVM进程ID
  │     getPid(Process)                   // 子进程PID
  │     getProcessInfo(Process)
  │
  └── 工具
        isWindows()
        isLinux()
        getSystemCharset()
```

### 10.2 使用示例

```java
// 最简单使用
ProcessResult r = ProcessUtils.execute("ls");
if (r.isSuccess()) {
    System.out.println(r.getStandardOutput());
}

// 带超时
try {
    ProcessResult r = ProcessUtils.execute("sleep 10", 3_000);
} catch (ProcessTimeoutException e) {
    System.out.println("执行超时: " + e.getTimeoutMs() + "ms");
}

// 实时处理日志
ProcessUtils.executeWithCallback("python app.py",
    (lineNum, line, type) -> logger.info(line),
    (lineNum, line, type) -> logger.error(line)
);

// 收集输出到列表
List<String> out = new CopyOnWriteArrayList<>();
ProcessUtils.executeWithCallback("cat file.txt",
    out::add,
    err::add
);

// 配置化使用
ProcessConfig config = ProcessConfig.builder()
    .command("python script.py")
    .timeoutMs(60_000)
    .workingDir(new File("/tmp"))
    .stdoutProcessor((lineNum, line, type) -> log.info(line))
    .stderrProcessor((lineNum, line, type) -> log.error(line))
    .build();
ProcessResult result = ProcessUtils.execute(config);
```

---

## 十一、ProcessWrapper（资源管理）

```java
public class ProcessWrapper implements AutoCloseable {
    private final Process process;
    private final long pid;
    
    public ProcessWrapper(Process process) {
        this.process = process;
        this.pid = getPid(process);
    }
    
    public boolean isAlive() { return process.isAlive(); }
    public long getPid() { return pid; }
    public Process getProcess() { return process; }
    
    @Override
    public void close() {
        if (process.isAlive()) {
            gracefulShutdown(process, 10_000);
        }
    }
}

// 使用
try (ProcessWrapper pw = ProcessUtils.executeAsync("long-running.sh")) {
    // 业务逻辑
} // 退出时自动清理进程
```

---

## 十二、进程组管理（Linux）

### 12.1 问题

单独 kill 子进程时，其 fork 的衍生进程可能成为孤儿进程。

### 12.2 解决方案

```java
// Linux: kill -PGID pid（负数PID表示进程组）
// Windows: taskkill /PID xxx /T

if (isLinux()) {
    // 获取进程组ID并杀死整个进程组
    String[] pgidCmd = {"sh", "-c", "ps -o pgid= " + pid + " | tr -d ' '"};
    Process pgidProcess = new ProcessBuilder(pgidCmd).start();
    String pgid = readLine(pgidProcess.getInputStream());
    Runtime.getRuntime().exec("kill -" + ProcessExitCode.SIGKILL + " -" + pgid);
} else {
    process.destroyForcibly();
}
```

---

## 十三、开发注意事项

### 13.1 必须避免的问题

| 问题 | 后果 | 解决方案 |
|------|------|----------|
| 顺序读取 stdout/stderr | 死锁 | 双线程异步 + CompletableFuture |
| 不检测 process.isAlive() | 僵尸进程 | destroy 后必须检测 |
| 硬编码 GBK | Linux 乱码 | `isWindows()` 判断 |
| 不设置 timeout | 进程僵死 | 默认 120 秒，必须可配置 |
| 空 catch | 异常吞没 | 异常必须封装传播 |

### 13.2 测试要点

| 测试场景 | 验证点 |
|----------|--------|
| 正常执行 | exitCode=0, stdout有值 |
| 命令不存在 | 抛出 ProcessStartException |
| 超时 | 抛出 ProcessTimeoutException |
| 非0退出码 | 抛出 ProcessExitCodeException |
| 大输出 | 不死锁，完整读取 |
| 并发执行 | 无死锁/资源竞争 |

---

## 十四、相关文件索引

| 文件 | 说明 |
|------|------|
| `common/utils/process/ProcessUtils.java` | 核心工具类 |
| `common/utils/process/ProcessWrapper.java` | 资源封装 |
| `common/utils/process/ProcessConfig.java` | 配置类 |
| `common/utils/process/model/ProcessResult.java` | 结果模型 |
| `common/utils/process/model/ProcessInfo.java` | 进程信息 |
| `common/utils/process/exception/*.java` | 异常体系 |
| `common/utils/process/function/*.java` | 函数式接口 |
| `common/utils/process/constant/ProcessExitCode.java` | 退出码常量 |

---

## 十五、参考

- Java `ProcessBuilder` 文档
- Java `Process` 文档
- [Shell 退出码规范](https://www.gnu.org/software/bash/manual/html_node/Exit-Status.html)
- `apache commons-exec`（进阶参考）