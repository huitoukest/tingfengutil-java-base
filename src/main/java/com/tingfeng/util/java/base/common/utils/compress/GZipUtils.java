package com.tingfeng.util.java.base.common.utils.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * gzip工具类
 */
public class GZipUtils {
    private static final int BUFFER_SIZE = 4096;

    public static byte[] compress(byte srcBytes[]) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(srcBytes);
            // 必须调用 finish() 方法完成压缩，确保所有数据都被写入
            gzip.finish();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] uncompress(byte[] bytes) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ByteArrayInputStream in = new ByteArrayInputStream(bytes);
             GZIPInputStream unGZip = new GZIPInputStream(in)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while ((n = unGZip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}