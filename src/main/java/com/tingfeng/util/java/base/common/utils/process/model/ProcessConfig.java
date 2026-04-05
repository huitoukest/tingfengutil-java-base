package com.tingfeng.util.java.base.common.utils.process.model;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * 进程执行配置
 * @author huitoukest
 */
@Getter
@Setter
public class ProcessConfig {
    /** 命令 */
    private String command;

    /** 超时时间（毫秒），默认120秒 */
    private long timeoutMs = 120_000;

    /** 字符集，默认系统编码 */
    private Charset charset = null;  // null表示自动检测

    /** 是否继承环境变量 */
    private boolean inheritEnv = true;

    /** 追加的环境变量 */
    private Map<String, String> extraEnv;

    /** 工作目录 */
    private File workingDir;

    /** stdout回调 */
    private com.tingfeng.util.java.base.common.utils.process.function.TypedLineProcessor stdoutProcessor;

    /** stderr回调 */
    private com.tingfeng.util.java.base.common.utils.process.function.TypedLineProcessor stderrProcessor;

    private ProcessConfig() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ProcessConfig config = new ProcessConfig();

        public Builder command(String command) {
            config.command = command;
            return this;
        }

        public Builder timeoutMs(long timeoutMs) {
            config.timeoutMs = timeoutMs;
            return this;
        }

        public Builder charset(Charset charset) {
            config.charset = charset;
            return this;
        }

        public Builder inheritEnv(boolean inheritEnv) {
            config.inheritEnv = inheritEnv;
            return this;
        }

        public Builder extraEnv(Map<String, String> extraEnv) {
            config.extraEnv = extraEnv;
            return this;
        }

        public Builder workingDir(File workingDir) {
            config.workingDir = workingDir;
            return this;
        }

        public Builder stdoutProcessor(com.tingfeng.util.java.base.common.utils.process.function.TypedLineProcessor processor) {
            config.stdoutProcessor = processor;
            return this;
        }

        public Builder stderrProcessor(com.tingfeng.util.java.base.common.utils.process.function.TypedLineProcessor processor) {
            config.stderrProcessor = processor;
            return this;
        }

        public ProcessConfig build() {
            return config;
        }
    }
}
