# ProcessUtils 使用示例

## 基本使用

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
```

## 流式回调

```java
// 实时处理输出
ProcessUtils.executeWithCallback("python app.py",
    (lineNum, line, type) -> {
        if (type == OutputStreamType.STDOUT) {
            System.out.println("[OUT] " + line);
        } else {
            System.err.println("[ERR] " + line);
        }
    }
);

// 收集到列表
List<String> out = new CopyOnWriteArrayList<>();
List<String> err = new CopyOnWriteArrayList<>();
ProcessUtils.executeWithCallback("cat file.txt",
    out::add,    // stdout 行收集
    err::add     // stderr 行收集
);
```

## 配置化使用

```java
ProcessConfig config = ProcessConfig.builder()
    .command("python script.py")
    .timeoutMs(60_000)
    .workingDir(new File("/tmp"))
    .stdoutProcessor((lineNum, line, type) -> log.info(line))
    .stderrProcessor((lineNum, line, type) -> log.error(line))
    .build();

ProcessResult result = ProcessUtils.execute(config);
```

## 进程管理

```java
Process p = new ProcessBuilder("long-running.sh").start();

// 检查存活
if (ProcessUtils.isAlive(p)) {
    System.out.println("Process is running");
}

// 优雅关闭（梯度终止）
boolean stopped = ProcessUtils.gracefulShutdown(p, 10_000);
if (!stopped) {
    // 强制终止
    ProcessUtils.destroyForcibly(p);
}
```

## ProcessWrapper 资源管理

```java
// 自动清理进程
try (ProcessWrapper pw = ProcessUtils.executeAsync("long-running.sh")) {
    long pid = pw.getPid();
    // 业务逻辑
} // 退出时自动清理进程
```

## 异步执行

```java
CompletableFuture<ProcessResult> future = ProcessUtils.executeAsync("python app.py");

// 继续其他工作
doOtherThing();

try {
    ProcessResult result = future.get(60, TimeUnit.SECONDS);
} catch (Exception e) {
    // 处理失败
}
```

## 跨平台注意

```java
// Windows: 自动转换为 cmd /c
// Linux: 自动转换为 sh -c
ProcessResult r = ProcessUtils.execute("echo Hello");  // 跨平台兼容
```
