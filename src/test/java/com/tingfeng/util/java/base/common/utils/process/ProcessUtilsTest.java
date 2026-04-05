package com.tingfeng.util.java.base.common.utils.process;

import com.tingfeng.util.java.base.common.utils.process.exception.ProcessExitCodeException;
import com.tingfeng.util.java.base.common.utils.process.exception.ProcessTimeoutException;
import com.tingfeng.util.java.base.common.utils.process.function.OutputStreamType;
import com.tingfeng.util.java.base.common.utils.process.model.ProcessConfig;
import com.tingfeng.util.java.base.common.utils.process.model.ProcessResult;
import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertTrue(result.getExitCode() == 0);
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
        // 不存在的命令应该抛出启动异常
        try {
            ProcessUtils.execute("nonexistent_command_12345");
            Assert.fail("Should throw exception");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("not found") || e.getMessage().contains("not recognized"));
        }
    }

    @Test
    public void testExecuteTimeout() {
        // sleep命令在Windows是timeout，在Linux是sleep
        String cmd = ProcessUtils.isWindows() ? "ping -n 10 127.0.0.1" : "sleep 10";
        try {
            ProcessUtils.execute(cmd, 1000);
            Assert.fail("Should throw timeout exception");
        } catch (ProcessTimeoutException e) {
            Assert.assertEquals(1000, e.getTimeoutMs());
        }
    }

    @Test
    public void testExecuteNonZeroExitCode() {
        // false命令在Windows返回1
        String cmd = ProcessUtils.isWindows() ? "cmd /c exit 1" : "false";
        try {
            ProcessUtils.execute(cmd, 5000);
            Assert.fail("Should throw exit code exception");
        } catch (ProcessExitCodeException e) {
            Assert.assertEquals(1, e.getExitCode());
        }
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
        // mvn -version 成功返回0
        Assert.assertEquals(0, result.getExitCode());
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

        Assert.assertTrue(stdoutLines.size() > 0);
        Assert.assertTrue(stderrLines.size() > 0);
    }

    @Test
    public void testExecuteWithTypedCallback() {
        CopyOnWriteArrayList<String> lines = new CopyOnWriteArrayList<>();

        String cmd = ProcessUtils.isWindows() ? "echo hello" : "echo hello";
        ProcessUtils.executeWithCallback(cmd, 5000,
                (lineNum, line, type) -> {
                    Assert.assertTrue(lineNum > 0);
                    Assert.assertNotNull(type);
                    lines.add("[" + type + "] " + line);
                });

        Assert.assertTrue(lines.size() > 0);
    }

    // ==================== 异步执行测试 ====================

    @Test
    public void testExecuteAsync() throws Exception {
        String cmd = ProcessUtils.isWindows() ? "echo async" : "echo async";
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
                .command(ProcessUtils.isWindows() ? "echo config" : "echo config")
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

        Assert.assertTrue(result.getExitCode() == 0);
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
    public void testIsAlive() {
        java.lang.Process process = new ProcessBuilder(
                ProcessUtils.isWindows() ? new String[]{"cmd", "/c", "ping -n 10 127.0.0.1"} : new String[]{"sleep", "10"}
        ).start();

        Assert.assertTrue(ProcessUtils.isAlive(process));
        ProcessUtils.destroy(process);
        ProcessUtils.waitFor(process, 15000);
        Assert.assertFalse(ProcessUtils.isAlive(process));
    }

    @Test
    public void testDestroyForcibly() {
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
