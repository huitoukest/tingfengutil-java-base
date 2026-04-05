package com.tingfeng.util.java.base.common.utils.process.function;

/**
 * 函数式接口：带行号和流类型的行处理器
 * @author huitoukest
 */
@FunctionalInterface
public interface TypedLineProcessor {
    /**
     * 处理一行输出
     * @param lineNumber 行号（从1开始）
     * @param line 一行文本
     * @param type 流类型（stdout/stderr）
     */
    void process(int lineNumber, String line, OutputStreamType type);
}
