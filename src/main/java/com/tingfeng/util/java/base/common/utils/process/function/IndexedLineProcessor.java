package com.tingfeng.util.java.base.common.utils.process.function;

/**
 * 函数式接口：带行号的行处理器
 * @author huitoukest
 */
@FunctionalInterface
public interface IndexedLineProcessor {
    /**
     * 处理一行输出
     * @param lineNumber 行号（从1开始）
     * @param line 一行文本
     */
    void process(int lineNumber, String line);
}
