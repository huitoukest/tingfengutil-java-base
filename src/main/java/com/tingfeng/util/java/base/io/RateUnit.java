package com.tingfeng.util.java.base.io;

/**
 * 限速单位：KB/MB/GB（1024 进制，字节/秒）
 *
 * 换算因子与项目既有字节容量常量一致（FileRangeCache/IOUtils 等均为 1024 进制）：
 * KB = 1024、MB = 1024²、GB = 1024³
 *
 * @author huitoukest
 */
public enum RateUnit {

    /** 千字节每秒：1024 字节 */
    KB(1024L),

    /** 兆字节每秒：1024 × 1024 字节 */
    MB(1024L * 1024L),

    /** 吉字节每秒：1024³ 字节 */
    GB(1024L * 1024L * 1024L);

    private final long bytes;

    RateUnit(long bytes) {
        this.bytes = bytes;
    }

    /**
     * 数量换算为字节数
     * 换算溢出时抛 ArithmeticException（调用方应捕获并包装为业务异常）
     * @param value 数量
     * @return 字节数
     * @throws ArithmeticException 换算结果超出 long 范围
     */
    public long toBytes(long value) {
        return Math.multiplyExact(value, bytes);
    }
}
