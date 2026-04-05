package com.tingfeng.util.java.base.common.utils.process;

import com.tingfeng.util.java.base.common.utils.process.constant.ProcessExitCode;
import com.tingfeng.util.java.base.common.utils.process.exception.ProcessExitCodeException;
import com.tingfeng.util.java.base.common.utils.process.exception.ProcessStartException;
import com.tingfeng.util.java.base.common.utils.process.exception.ProcessTimeoutException;
import com.tingfeng.util.java.base.common.utils.process.function.OutputStreamType;
import com.tingfeng.util.java.base.common.utils.process.function.TypedLineProcessor;
import com.tingfeng.util.java.base.common.utils.process.model.ProcessConfig;
import com.tingfeng.util.java.base.common.utils.process.model.ProcessInfo;
import com.tingfeng.util.java.base.common.utils.process.model.ProcessResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 进程工具类，提供跨平台进程创建、执行、管理能力
 * @author huitoukest
 */
public class ProcessUtils {

    /** 默认超时时间：120秒 */
    public static final long DEFAULT_TIMEOUT_MS = 120_000;

    private ProcessUtils() {
    }

    // ==================== 同步执行（阻塞） ====================

    /**
     * 执行命令（默认120秒超时）
     * @param command 命令
     * @return 执行结果
     */
    public static ProcessResult execute(String command) {
        return execute(command, DEFAULT_TIMEOUT_MS);
    }

    /**
     * 执行命令
     * @param command 命令
     * @param timeoutMs 超时时间（毫秒）
     * @return 执行结果
     */
    public static ProcessResult execute(String command, long timeoutMs) {
        return execute(command, timeoutMs, getSystemCharset());
    }

    /**
     * 执行命令
     * @param command 命令
     * @param timeoutMs 超时时间（毫秒）
     * @param charset 字符集
     * @return 执行结果
     */
    public static ProcessResult execute(String command, long timeoutMs, Charset charset) {
        return execute(buildConfig(command, timeoutMs, charset));
    }

    /**
     * 执行命令（使用配置）
     * @param config 配置
     * @return 执行结果
     */
    public static ProcessResult execute(ProcessConfig config) {
        Charset charset = config.getCharset() != null ? config.getCharset() : getSystemCharset();
        java.lang.Process process = startProcess(config.getCommand());

        long startTime = System.currentTimeMillis();

        // 双线程异步读取stdout和stderr
        CompletableFuture<String> stdoutFuture = CompletableFuture.supplyAsync(() ->
                readStream(process.getInputStream(), charset));

        CompletableFuture<String> stderrFuture = CompletableFuture.supplyAsync(() ->
                readStream(process.getErrorStream(), charset));

        // 等待进程结束或超时
        boolean finished;
        try {
            finished = process.waitFor(config.getTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            destroyForcibly(process);
            return buildErrorResult(config.getCommand(), -1, "Interrupted", 0);
        }

        long duration = System.currentTimeMillis() - startTime;

        if (!finished) {
            process.destroy();
            return buildResult(config.getCommand(), -1, "", "", true, duration);
        }

        String stdout = stdoutFuture.join();
        String stderr = stderrFuture.join();

        return buildResult(config.getCommand(), process.exitValue(), stdout, stderr, false, duration);
    }

    // ==================== 流式回调执行（阻塞） ====================

    /**
     * 执行命令并实时处理输出流
     * @param command 命令
     * @param stdoutProcessor stdout处理回调
     * @param stderrProcessor stderr处理回调
     */
    public static void executeWithCallback(String command,
                                          Consumer<String> stdoutProcessor,
                                          Consumer<String> stderrProcessor) {
        executeWithCallback(command, DEFAULT_TIMEOUT_MS,
                (lineNum, line, type) -> {
                    if (type == OutputStreamType.STDOUT && stdoutProcessor != null) {
                        stdoutProcessor.accept(line);
                    } else if (type == OutputStreamType.STDERR && stderrProcessor != null) {
                        stderrProcessor.accept(line);
                    }
                });
    }

    /**
     * 执行命令并实时处理输出流
     * @param command 命令
     * @param timeoutMs 超时时间
     * @param processor 行处理器
     */
    public static void executeWithCallback(String command, long timeoutMs, TypedLineProcessor processor) {
        executeWithCallback(command, timeoutMs, getSystemCharset(), processor);
    }

    /**
     * 执行命令并实时处理输出流
     * @param command 命令
     * @param timeoutMs 超时时间
     * @param charset 字符集
     * @param processor 行处理器
     */
    public static void executeWithCallback(String command, long timeoutMs, Charset charset, TypedLineProcessor processor) {
        java.lang.Process process = startProcess(command);

        Charset actualCharset = charset != null ? charset : getSystemCharset();

        // 异步读取并处理流
        CompletableFuture<Void> stdoutFuture = CompletableFuture.runAsync(() ->
                readStreamWithCallback(process.getInputStream(), actualCharset, OutputStreamType.STDOUT, processor));

        CompletableFuture<Void> stderrFuture = CompletableFuture.runAsync(() ->
                readStreamWithCallback(process.getErrorStream(), actualCharset, OutputStreamType.STDERR, processor));

        boolean finished;
        try {
            finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            destroyForcibly(process);
            return;
        }

        if (!finished) {
            process.destroy();
            throw new ProcessTimeoutException("Process execution timed out", command, timeoutMs);
        }

        // 等待流读取完成
        CompletableFuture.allOf(stdoutFuture, stderrFuture).join();

        int exitCode = process.exitValue();
        if (exitCode != ProcessExitCode.SUCCESS) {
            throw new ProcessExitCodeException("Process exited with non-zero code", command, exitCode);
        }
    }

    // ==================== 异步执行 ====================

    /**
     * 异步执行命令
     * @param command 命令
     * @return CompletableFuture
     */
    public static CompletableFuture<ProcessResult> executeAsync(String command) {
        return CompletableFuture.supplyAsync(() -> execute(command));
    }

    /**
     * 异步执行命令
     * @param command 命令
     * @param timeoutMs 超时时间
     * @return CompletableFuture
     */
    public static CompletableFuture<ProcessResult> executeAsync(String command, long timeoutMs) {
        return CompletableFuture.supplyAsync(() -> execute(command, timeoutMs));
    }

    // ==================== 快捷命令封装 ====================

    /**
     * 执行curl请求
     * @param args curl参数，如 "-X GET" 或 "-X POST -d '{}'"
     * @return 执行结果
     */
    public static ProcessResult curl(String... args) {
        StringBuilder cmd = new StringBuilder("curl");
        for (String arg : args) {
            cmd.append(" ").append(arg);
        }
        return execute(cmd.toString());
    }

    /**
     * 执行nslookup查询
     * @param domain 域名
     * @return DNS查询结果
     */
    public static ProcessResult nslookup(String domain) {
        return execute("nslookup " + domain);
    }

    /**
     * 执行grep搜索
     * @param pattern 正则表达式
     * @param paths 文件路径
     * @return 匹配结果
     */
    public static ProcessResult grep(String pattern, String... paths) {
        StringBuilder cmd = new StringBuilder("grep ");
        cmd.append("\"").append(pattern).append("\"");
        for (String path : paths) {
            cmd.append(" ").append(path);
        }
        return execute(cmd.toString());
    }

    /**
     * 执行find搜索
     * @param path 搜索路径
     * @param args find参数，如 "-name '*.java'"
     * @return 搜索结果
     */
    public static ProcessResult find(String path, String... args) {
        StringBuilder cmd = new StringBuilder("find ").append(path);
        for (String arg : args) {
            cmd.append(" ").append(arg);
        }
        return execute(cmd.toString());
    }

    /**
     * 执行Maven命令
     * @param args mvn参数，如 "clean package -DskipTests"
     * @return 执行结果
     */
    public static ProcessResult mvn(String... args) {
        StringBuilder cmd = new StringBuilder("mvn");
        for (String arg : args) {
            cmd.append(" ").append(arg);
        }
        return execute(cmd.toString());
    }

    /**
     * 执行Docker命令
     * @param args docker参数
     * @return 执行结果
     */
    public static ProcessResult docker(String... args) {
        StringBuilder cmd = new StringBuilder("docker");
        for (String arg : args) {
            cmd.append(" ").append(arg);
        }
        return execute(cmd.toString());
    }

    /**
     * 执行kubectl命令
     * @param args kubectl参数
     * @return 执行结果
     */
    public static ProcessResult kubectl(String... args) {
        StringBuilder cmd = new StringBuilder("kubectl");
        for (String arg : args) {
            cmd.append(" ").append(arg);
        }
        return execute(cmd.toString());
    }

    /**
     * 执行Python脚本
     * @param scriptPath 脚本路径
     * @param args 脚本参数
     * @return 执行结果
     */
    public static ProcessResult python(String scriptPath, String... args) {
        StringBuilder cmd = new StringBuilder("python ");
        cmd.append("\"").append(scriptPath).append("\"");
        for (String arg : args) {
            cmd.append(" ").append(arg);
        }
        return execute(cmd.toString());
    }

    /**
     * 执行Node脚本
     * @param scriptPath 脚本路径
     * @param args 脚本参数
     * @return 执行结果
     */
    public static ProcessResult node(String scriptPath, String... args) {
        StringBuilder cmd = new StringBuilder("node ");
        cmd.append("\"").append(scriptPath).append("\"");
        for (String arg : args) {
            cmd.append(" ").append(arg);
        }
        return execute(cmd.toString());
    }

    // ==================== 进程管理 ====================

    /**
     * 检测进程是否存活
     */
    public static boolean isAlive(java.lang.Process process) {
        return process != null && process.isAlive();
    }

    /**
     * 终止进程（优雅）
     * Java 8: destroy() 返回 Process; Java 9+: destroy() 返回 boolean
     */
    public static boolean destroy(java.lang.Process process) {
        if (process == null || !process.isAlive()) {
            return true;
        }
        process.destroy();
        return !process.isAlive();
    }

    /**
     * 强制终止进程（Java 8兼容）
     */
    public static boolean destroyForcibly(java.lang.Process process) {
        if (process == null || !process.isAlive()) {
            return true;
        }
        process.destroy();
        if (process.isAlive()) {
            // Java 9+ 才有 destroyForcibly()，Java 8 兼容处理
            try {
                java.lang.reflect.Method method = process.getClass().getMethod("destroyForcibly");
                return (boolean) method.invoke(process);
            } catch (Exception e) {
                return !process.isAlive();
            }
        }
        return true;
    }

    /**
     * 梯度终止进程
     * @param process 进程
     * @param timeoutMs 总超时时间
     * @return true if terminated
     */
    public static boolean gracefulShutdown(java.lang.Process process, long timeoutMs) {
        if (process == null || !process.isAlive()) {
            return true;
        }
        process.destroy();
        if (waitForExit(process, timeoutMs)) {
            return true;
        }
        process.destroy();
        if (waitForExit(process, timeoutMs / 2)) {
            return true;
        }
        return destroyForcibly(process);
    }

    /**
     * 等待进程结束
     */
    public static boolean waitFor(java.lang.Process process, long timeoutMs) {
        try {
            return process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    // ==================== 进程信息 ====================

    /**
     * 获取当前JVM进程ID
     */
    public static long currentPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int atIndex = name.indexOf('@');
        if (atIndex <= 0) {
            throw new RuntimeException("Cannot parse PID from: " + name);
        }
        return Long.parseLong(name.substring(0, atIndex));
    }

    /**
     * 获取子进程PID（Java 8兼容）
     */
    public static long getPid(java.lang.Process process) {
        return ProcessWrapper.getPid(process);
    }

    /**
     * 获取进程信息
     */
    public static ProcessInfo getProcessInfo(java.lang.Process process) {
        ProcessInfo info = new ProcessInfo();
        info.setProcess(process);
        info.setPid(getPid(process));
        info.setAlive(process.isAlive());
        return info;
    }

    // ==================== 工具方法 ====================

    /**
     * 判断是否为Windows系统
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * 判断是否为Linux系统
     */
    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    /**
     * 获取系统控制台编码
     */
    public static Charset getSystemCharset() {
        return isWindows() ? Charset.forName("GBK") : StandardCharsets.UTF_8;
    }

    // ==================== 内部方法 ====================

    /**
     * 启动进程
     */
    private static java.lang.Process startProcess(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(prepareCommand(command));
            pb.redirectErrorStream(false);
            return pb.start();
        } catch (Exception e) {
            String reason = getStartFailureReason(e);
            throw new ProcessStartException("Failed to start process: " + reason, command, reason, e);
        }
    }

    /**
     * 准备跨平台命令
     */
    private static String[] prepareCommand(String command) {
        if (isWindows()) {
            return new String[]{"cmd", "/c", command};
        } else {
            return new String[]{"sh", "-c", command};
        }
    }

    /**
     * 获取启动失败原因
     */
    private static String getStartFailureReason(Exception e) {
        String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (message.contains("cannot run program") && message.contains("no such file")) {
            return "command not found";
        }
        if (message.contains("permission denied")) {
            return "permission denied";
        }
        return "io error";
    }

    /**
     * 读取流内容
     */
    private static String readStream(java.io.InputStream inputStream, Charset charset) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception e) {
            // 读取流时的异常不影响整体结果，仅记录
        }
        return sb.toString();
    }

    /**
     * 读取流并回调处理
     */
    private static void readStreamWithCallback(java.io.InputStream inputStream, Charset charset,
                                              OutputStreamType type, TypedLineProcessor processor) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                if (processor != null) {
                    processor.process(lineNum, line, type);
                }
                lineNum++;
            }
        } catch (Exception e) {
            // 读取流时的异常不影响整体回调
        }
    }

    /**
     * 等待进程退出
     */
    private static boolean waitForExit(java.lang.Process process, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (!process.isAlive()) {
                return true;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * 构建结果
     */
    private static ProcessResult buildResult(String command, int exitCode, String stdout,
                                             String stderr, boolean timedOut, long duration) {
        ProcessResult result = new ProcessResult();
        result.setCommand(command);
        result.setExitCode(exitCode);
        result.setStandardOutput(stdout);
        result.setErrorOutput(stderr);
        result.setTimedOut(timedOut);
        result.setDurationMs(duration);
        result.setStarted(true);
        return result;
    }

    /**
     * 构建错误结果
     */
    private static ProcessResult buildErrorResult(String command, int exitCode, String reason, long duration) {
        ProcessResult result = new ProcessResult();
        result.setCommand(command);
        result.setExitCode(exitCode);
        result.setStandardOutput("");
        result.setErrorOutput(reason);
        result.setTimedOut(false);
        result.setDurationMs(duration);
        result.setStarted(false);
        result.setStartFailureReason(reason);
        return result;
    }

    /**
     * 构建配置
     */
    private static ProcessConfig buildConfig(String command, long timeoutMs, Charset charset) {
        return ProcessConfig.builder()
                .command(command)
                .timeoutMs(timeoutMs)
                .charset(charset)
                .build();
    }
}
