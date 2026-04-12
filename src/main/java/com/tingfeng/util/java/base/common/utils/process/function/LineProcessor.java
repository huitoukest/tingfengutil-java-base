package com.tingfeng.util.java.base.common.utils.process.function;

/**
 * 函数式接口：行处理器
 * @author huitoukest
 */
@FunctionalInterface
public interface LineProcessor {
    /**
     * 处理一行输出
     * @param line 一行文本
     */
    void process(String line);
}
