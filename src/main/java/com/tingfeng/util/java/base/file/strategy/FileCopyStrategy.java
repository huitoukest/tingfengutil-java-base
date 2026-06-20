package com.tingfeng.util.java.base.file.strategy;

import java.io.File;

/**
 * 文件拷贝策略接口
 * 抽象文件拷贝的不同实现方式（流拷贝、Channel拷贝、异步拷贝等）
 *
 * 设计原则：
 * 1. 策略接口统一抽象，支持多种拷贝实现
 * 2. 进度回调通过 ProgressCallback 传递
 * 3. 实现类负责具体的拷贝逻辑
 */
public interface FileCopyStrategy {

    /**
     * 拷贝文件（无进度回调）
     * @param src 源文件
     * @param dest 目标文件
     * @throws IllegalArgumentException 如果源文件不存在或目标路径无效
     */
    void copyFile(File src, File dest);

    /**
     * 拷贝文件（带进度回调）
     * @param src 源文件
     * @param dest 目标文件
     * @param callback 进度回调，传递已拷贝字节数和总字节数
     * @throws IllegalArgumentException 如果源文件不存在或目标路径无效
     */
    void copyFile(File src, File dest, ProgressCallback callback);
}
