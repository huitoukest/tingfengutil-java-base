package com.tingfeng.util.java.base.file.strategy;

import com.tingfeng.util.java.base.file.FileUtils;
import com.tingfeng.util.java.base.lang.exception.BaseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * FileChannel高效拷贝策略
 * 使用 NIO FileChannel.transferTo() 进行文件拷贝
 *
 * 特点：
 * 1. 高效，利用系统级 DMA 传输
 * 2. 适合大文件拷贝
 * 3. 支持进度回调
 */
public class ChannelCopyStrategy implements FileCopyStrategy {

    /**
     * 默认缓冲区大小（与 FileUtils.BUFFER_SIZE 保持一致）
     */
    private final int bufferSize;

    /**
     * 创建Channel拷贝策略（使用默认缓冲区大小）
     */
    public ChannelCopyStrategy() {
        this(FileUtils.BUFFER_SIZE);
    }

    /**
     * 创建Channel拷贝策略（指定缓冲区大小）
     * @param bufferSize 缓冲区大小（字节）
     */
    public ChannelCopyStrategy(int bufferSize) {
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

        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;

        try {
            // 确保目标父目录存在
            File parent = dest.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            inStream = new FileInputStream(src);
            outStream = new FileOutputStream(dest);
            in = inStream.getChannel();
            out = outStream.getChannel();

            long fileSize = in.size();

            if (callback == null || callback == ProgressCallback.NONE) {
                // 无回调时使用高效的系统级 transferTo
                in.transferTo(0, fileSize, out);
            } else {
                // 有回调时分块拷贝并通知进度
                int lengthPerTime;
                long lengthReadSum = 0;
                ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

                while ((lengthPerTime = in.read(buffer)) != -1) {
                    buffer.flip();
                    out.write(buffer);
                    buffer.clear();
                    lengthReadSum += lengthPerTime;
                    callback.onProgress(lengthReadSum, fileSize);
                }
            }

            out.force(true);

            if (callback != null && callback != ProgressCallback.NONE) {
                callback.onComplete(fileSize);
            }
        } catch (IOException e) {
            if (callback != null && callback != ProgressCallback.NONE) {
                callback.onError(e);
            }
            throw new BaseException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException ignored) {
                }
            }
            if (outStream != null) {
                try {
                    outStream.flush();
                    outStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
