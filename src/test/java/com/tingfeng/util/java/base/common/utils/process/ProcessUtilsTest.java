package com.tingfeng.util.java.base.common.utils.process;

import com.tingfeng.util.java.base.common.utils.process.model.ProcessConfig;
import com.tingfeng.util.java.base.common.utils.process.model.ProcessResult;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.ProcessBuilder;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ProcessUtils单元测试
 * @author huitoukest
 */
public class ProcessUtilsTest {

    // ==================== 基础执行测试 ====================

    @Test
    public void testExecuteSimple() {
        String cmd = ProcessUtils.isWindows() ? "dir" : "ls";
        ProcessResult result = ProcessUtils.execute(cmd);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.getExitCode());
        Assert.assertNotNull(result.getStandardOutput());
    }

    @Test
    public void testExecuteWithExitCode() {
        ProcessResult result = ProcessUtils.execute(ProcessUtils.isWindows() ? "cd ." : "pwd");
        Assert.assertTrue(result.isSuccess());
        Assert.assertFalse(result.isTimedOut());
    }

    @Test
    public void testExecuteFailedCommand() {
        // 不存在的命令会启动cmd.exe成功，但执行失败返回非0退出码
        ProcessResult result = ProcessUtils.execute("nonexistent_command_12345");
        // 检查执行失败（不是启动失败，而是命令执行失败）
        Assert.assertFalse(result.isSuccess());
    }

    @Test
    public void testExecuteTimeout() {
        // execute()方法超时时不抛异常，返回timedOut=true的ProcessResult
        String cmd = ProcessUtils.isWindows() ? "ping -n 10 127.0.0.1" : "sleep 10";
        ProcessResult result = ProcessUtils.execute(cmd, 1000);
        Assert.assertTrue(result.isTimedOut());
    }

    @Test
    public void testExecuteNonZeroExitCode() {
        // execute()方法非0退出码不抛异常，返回包含错误信息的ProcessResult
        String cmd = ProcessUtils.isWindows() ? "cmd /c exit 1" : "false";
        ProcessResult result = ProcessUtils.execute(cmd, 5000);
        Assert.assertFalse(result.isSuccess());
        Assert.assertEquals(ProcessResult.ErrorType.EXIT_CODE, result.getErrorType());
    }

    // ==================== 系统工具测试 ====================

    @Test
    public void testCurrentPid() {
        long pid = ProcessUtils.currentPid();
        Assert.assertTrue(pid > 0);
    }

    @Test
    public void testIsWindows() {
        boolean isWindows = ProcessUtils.isWindows();
        boolean isLinux = ProcessUtils.isLinux();
        Assert.assertTrue(isWindows || isLinux);
        Assert.assertFalse(isWindows && isLinux);
    }

    @Test
    public void testGetSystemCharset() {
        Assert.assertNotNull(ProcessUtils.getSystemCharset());
    }

    // ==================== 快捷命令测试 ====================

    @Test
    public void testMvn() {
        ProcessResult result = ProcessUtils.mvn("-version");
        Assert.assertNotNull(result);
        // mvn可能未安装或不在PATH中，跳过
        if (result.getExitCode() != 0) {
            return;
        }
        String output = result.getStandardOutput();
        Assert.assertTrue(output.contains("Apache") || output.contains("Maven"));
    }

    @Test
    public void testDocker() {
        ProcessResult result = ProcessUtils.docker("--version");
        // docker可能未安装，跳过
        if (result.getExitCode() != 0) {
            return;
        }
        Assert.assertTrue(result.getStandardOutput().contains("Docker"));
    }

    @Test
    public void testPython() {
        String cmd = ProcessUtils.isWindows() ? "python --version" : "python3 --version";
        ProcessResult result = ProcessUtils.execute(cmd, 5000);
        // python可能未安装
        if (result.getExitCode() != 0) {
            return;
        }
        Assert.assertTrue(result.getStandardOutput().contains("Python"));
    }

    @Test
    public void testNode() {
        ProcessResult result = ProcessUtils.node("--version");
        // node可能未安装
        if (result.getExitCode() != 0) {
            return;
        }
        Assert.assertTrue(result.getStandardOutput().contains("v"));
    }

    // ==================== curl测试 ====================

    @Test
    public void testCurlHelp() {
        ProcessResult result = ProcessUtils.curl("--help");
        // curl可能未安装
        if (result.getExitCode() != 0) {
            return;
        }
        Assert.assertTrue(result.getStandardOutput().contains("curl"));
    }

    // ==================== 流式回调测试 ====================

    @Test
    public void testExecuteWithCallback() {
        CopyOnWriteArrayList<String> stdoutLines = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<String> stderrLines = new CopyOnWriteArrayList<>();

        String cmd = ProcessUtils.isWindows() ? "echo test && echo error 1>&2" : "echo test && echo error >&2";
        ProcessUtils.executeWithCallback(cmd,
                line -> stdoutLines.add(line),
                line -> stderrLines.add(line)
        );

        Assert.assertFalse(stdoutLines.isEmpty());
        Assert.assertFalse(stderrLines.isEmpty());
    }

    @Test
    public void testExecuteWithTypedCallback() {
        CopyOnWriteArrayList<String> lines = new CopyOnWriteArrayList<>();

        String cmd = "echo hello";
        ProcessUtils.executeWithCallback(cmd, 5000,
                (lineNum, line, type) -> {
                    Assert.assertTrue(lineNum > 0);
                    Assert.assertNotNull(type);
                    lines.add("[" + type + "] " + line);
                });

        Assert.assertFalse(lines.isEmpty());
    }

    // ==================== 异步执行测试 ====================

    @Test
    public void testExecuteAsync() throws Exception {
        String cmd = "echo async";
        ProcessResult result = ProcessUtils.executeAsync(cmd).get(5, java.util.concurrent.TimeUnit.SECONDS);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
    }

    // ==================== 配置测试 ====================

    @Test
    public void testProcessConfigBuilder() {
        ProcessConfig config = ProcessConfig.builder()
                .command("echo test")
                .timeoutMs(5000)
                .build();

        Assert.assertEquals("echo test", config.getCommand());
        Assert.assertEquals(5000, config.getTimeoutMs());
    }

    @Test
    public void testExecuteWithConfig() {
        ProcessConfig config = ProcessConfig.builder()
                .command("echo config")
                .timeoutMs(5000)
                .build();

        ProcessResult result = ProcessUtils.execute(config);
        Assert.assertTrue(result.isSuccess());
    }

    // ==================== ProcessInfo测试 ====================

    @Test
    public void testProcessInfo() {
        String cmd = ProcessUtils.isWindows() ? "ping -n 2 127.0.0.1" : "sleep 1";
        ProcessResult result = ProcessUtils.execute(cmd, 5000);

        Assert.assertEquals(0, result.getExitCode());
    }

    // ==================== 错误类型测试 ====================

    @Test
    public void testErrorType() {
        ProcessResult result = ProcessUtils.execute(
                ProcessUtils.isWindows() ? "cmd /c exit 1" : "exit 1", 5000);
        Assert.assertEquals(ProcessResult.ErrorType.EXIT_CODE, result.getErrorType());
    }

    // ==================== 进程管理测试 ====================

    @Test
    public void testIsAlive() throws IOException {
        java.lang.Process process = new ProcessBuilder(
                ProcessUtils.isWindows() ? new String[]{"cmd", "/c", "ping -n 10 127.0.0.1"} : new String[]{"sleep", "10"}
        ).start();

        Assert.assertTrue(ProcessUtils.isAlive(process));
        ProcessUtils.destroy(process);
        ProcessUtils.waitFor(process, 15000);
        Assert.assertFalse(ProcessUtils.isAlive(process));
    }

    @Test
    public void testDestroyForcibly() throws IOException {
        java.lang.Process process = new ProcessBuilder(
                ProcessUtils.isWindows() ? new String[]{"cmd", "/c", "ping -n 100 127.0.0.1"} : new String[]{"sleep", "100"}
        ).start();

        Assert.assertTrue(ProcessUtils.isAlive(process));
        boolean destroyed = ProcessUtils.destroyForcibly(process);
        Assert.assertTrue(destroyed || !ProcessUtils.isAlive(process));
    }

    // ==================== grep/find测试 ====================

    @Test
    public void testGrep() {
        ProcessResult result = ProcessUtils.grep("test", "-");
        Assert.assertNotNull(result);
        // grep从stdin读取，返回码取决于是否有匹配
        Assert.assertTrue(result.getExitCode() == 0 || result.getExitCode() == 1);
    }

    @Test
    public void testFind() {
        String path = ProcessUtils.isWindows() ? "." : "/tmp";
        ProcessResult result = ProcessUtils.find(path, "-maxdepth", "1");
        // find可能无权限或无结果，但不报错
        Assert.assertNotNull(result);
    }

    @Test
    public void testNslookup() {
        ProcessResult result = ProcessUtils.nslookup("localhost");
        if (result.getExitCode() != 0) {
            return;
        }
        Assert.assertTrue(result.getStandardOutput().contains("localhost") ||
                result.getStandardOutput().contains("Address"));
    }
}
