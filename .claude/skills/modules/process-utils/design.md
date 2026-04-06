# ProcessUtils 设计原则

## 职责定位

ProcessUtils 是 **进程管理工具类**，封装 Java 原生 ProcessBuilder + Process，提供跨平台进程创建、执行、管理能力。

## Java 8 兼容性

| Java 9+ 功能 | Java 8 替代方案 |
|-------------|----------------|
| `ProcessHandle.current().pid()` | `ManagementFactory.getRuntimeMXBean().getName().split("@")[0]` |
| `ProcessHandle.allProcesses()` | 无法直接获取，需维护子进程集合 |
| `Process.info()` | `process.exitValue()` 同步检查 |
| `Process.destroyForcibly()` | `process.destroy()` + 检测 `isAlive()` 后再次 destroy |

## 包结构

```
common/utils/process/
├── constant/
│   └── ProcessExitCode.java       // 退出码常量
├── model/
│   ├── ProcessConfig.java        // 配置类（建造者模式）
│   ├── ProcessResult.java        // 执行结果
│   └── ProcessInfo.java          // 进程信息
├── exception/
│   ├── ProcessException.java
│   ├── ProcessStartException.java
│   ├── ProcessTimeoutException.java
│   └── ProcessExitCodeException.java
├── function/
│   ├── LineProcessor.java
│   ├── IndexedLineProcessor.java
│   └── TypedLineProcessor.java
├── ProcessWrapper.java           // 资源封装（AutoCloseable）
└── ProcessUtils.java
```

## 异常体系

```
ProcessException (RuntimeException)
  ├── ProcessStartException     // 进程启动失败
  ├── ProcessTimeoutException   // 执行超时
  └── ProcessExitCodeException // 非0退出码
```

## 梯度终止策略

```
收到关闭信号
    ↓
SIGTERM (软终止) → 等待 5 秒
    ↓
若仍存活 → SIGTERM 再次发送 → 等待 3 秒
    ↓
若仍存活 → SIGKILL (硬终止) → 立即
```

## 安全规范

| 禁止 | 推荐 |
|------|------|
| `exec(userInput)` 直接执行 | 使用参数数组 `exec("grep", userInput)` |
| 字符串拼接命令 | 白名单校验 + 输入过滤 |

## 注意事项

1. **流式读取防止死锁** - 双线程异步读取 stdout/stderr
2. **跨平台命令适配** - Windows 用 `cmd /c`，Linux 用 `sh -c`
3. **默认120秒超时** - 必须可配置
4. **禁止空catch** - 异常必须传播
