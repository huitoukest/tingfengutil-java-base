package com.tingfeng.util.java.base.file.strategy;

import com.tingfeng.util.java.base.file.FileUtils;
import com.tingfeng.util.java.base.lang.exception.BaseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * IO流拷贝策略
 * 使用传统的 InputStream/OutputStream 进行文件拷贝
 *
 * 特点：
 * 1. 实现简单，兼容性好
 * 2. 适合中小文件拷贝
 * 3. 可配置缓冲区大小
 */
public class StreamCopyStrategy implements FileCopyStrategy {

    /**
     * 默认缓冲区大小（与 FileUtils.BUFFER_SIZE 保持一致）
     */
    private final int bufferSize;

    /**
     * 创建流拷贝策略（使用默认缓冲区大小）
     */
    public StreamCopyStrategy() {
        this(FileUtils.BUFFER_SIZE);
    }

    /**
     * 创建流拷贝策略（指定缓冲区大小）
     * @param bufferSize 缓冲区大小（字节）
     */
    public StreamCopyStrategy(int bufferSize) {
        this.bufferSize = bufferSize > 0 ? bufferSize : FileUtils.BUFFER_SIZE;
    }

    @Override
    public void copyFile(File src, File dest) {
        copyFile(src, dest, ProgressCallback.NONE);
    }

    @Override
    public void copyFile(File src, File dest, ProgressCallback callback) {
        if (src == null || !src.exists()) {
            throw new IllegalArgumentException("Source file does not exist: " + src);
        }
        if (dest == null) {
            throw new IllegalArgumentException("Destination file must not be null");
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            // 确保目标父目录存在
            File parent = dest.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            fis = new FileInputStream(src);
            fos = new FileOutputStream(dest);

            byte[] buffer = new byte[bufferSize];
            long total = 0;
            long fileSize = src.length();
            int len;

            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                total += len;

                if (callback != null && callback != ProgressCallback.NONE) {
                    callback.onProgress(total, fileSize);
                }
            }

            fos.flush();

            if (callback != null && callback != ProgressCallback.NONE) {
                callback.onComplete(total);
            }
        } catch (IOException e) {
            if (callback != null && callback != ProgressCallback.NONE) {
                callback.onError(e);
            }
            throw new BaseException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignored) {
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
