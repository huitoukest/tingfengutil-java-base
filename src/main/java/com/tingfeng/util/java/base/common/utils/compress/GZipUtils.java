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
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        byte[] re = null;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(srcBytes);
            re = out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                out.close();
                if (gzip != null) {
                    gzip.close();
                }
            }catch (Throwable e){
                throw new RuntimeException(e);
            }

        }
        return re;
    }

    public static byte[] uncompress(byte[] bytes) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        GZIPInputStream unGZip = null;
        byte[] re = null;
        try {
            unGZip = new GZIPInputStream(in);
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while ((n = unGZip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            re = out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                out.close();
                if (unGZip != null) {
                    unGZip.close();
                }
            }catch (Throwable e){
                throw new RuntimeException(e);
            }

        }

        return re;
    }
}
