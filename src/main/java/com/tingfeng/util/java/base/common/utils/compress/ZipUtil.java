package com.tingfeng.util.java.base.common.utils.compress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * zip工具类
 */
public class ZipUtil {
    /**
     * 默认的压缩级别：经过测试压缩1级的压缩率和其余的级别差别不是很大，但是速度优先很多
     */
    public static final int DEFAULT_LEVEL = 1;

    private static final int BUFFER_SIZE = 4096;
    /**
     * 压缩数据
     * @param input
     * @param level 压缩的级别;可选的级别有0（不压缩），以及1(快速压缩)到9（慢速压缩）,这里使用的是以速度为优先。
     * @return
     */
    public static byte[] compress(byte input[],int level) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Deflater compressor = new Deflater(level);
        byte[] re = null;
        try {
            compressor.setInput(input);
            compressor.finish();
            final byte[] buf = new byte[BUFFER_SIZE];
            while (!compressor.finished()) {
                int count = compressor.deflate(buf);
                bos.write(buf, 0, count);
            }
            re = bos.toByteArray();
        } finally {
            compressor.end();
            try {
                bos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return re;
    }

    /**
     * 压缩数据,默认使用级别2的数据压缩
     * @param input
     * @return
     */
    public static byte[] compress(byte input[]) {
        return compress(input,DEFAULT_LEVEL);
    }

    /**
     * 解压数据
     * @param input
     * @return
     * @throws DataFormatException
     */
    public static byte[] uncompress(byte[] input) throws DataFormatException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Inflater deCompressor = new Inflater();
        byte[] re = null;
        try {
            deCompressor.setInput(input);
            final byte[] buf = new byte[BUFFER_SIZE];
            while (!deCompressor.finished()) {
                int count = deCompressor.inflate(buf);
                bos.write(buf, 0, count);
            }
            re = bos.toByteArray();
        } finally {
            deCompressor.end();
            try {
                bos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return re;
    }
}
