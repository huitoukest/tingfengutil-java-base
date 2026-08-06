package com.tingfeng.util.java.base.io;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * 基于文件系统的块缓存实现（CacheReader/CacheWriter 默认实现）
 *
 * - 块文件命名: {blockIndex}{suffix}，默认后缀 .cache（例：2.cache）
 * - cacheKey 非空时按 cacheDir/{cacheKey}/ 子目录隔离，不同源身份天然不串数据
 * - 写采用临时文件 + ATOMIC_MOVE 原子替换，并发写同一块不产生半写块
 * - 文件层损坏校验：仅当文件存在且 0 < 长度 <= blockSize 才返回数据，
 *   空文件/超长文件视为 miss；精确命中判定由流层期望块长校验完成
 * - 缓存无 TTL：同一 cacheKey 下的块文件视为同一 source 的持久块；
 *   cacheKey 为空时调用方必须保证 cacheDir 不被多个不同 source 共用，
 *   否则由流层精确期望块长校验兜底（end<=0 仅满块命中，陈旧部分块永不过校验）
 * - 失效入口：clear() 清空全部块、deleteBlock() 删除单块
 *
 * 线程安全：原子写保证并发写同一块不损坏；多实例共享安全。
 *
 * @author huitoukest
 */
public class FileRangeCache implements CacheReader, CacheWriter {

    /**
     * 默认块大小：1MB
     */
    public static final long DEFAULT_BLOCK_SIZE = 1024L * 1024L;

    /**
     * 默认块文件后缀
     */
    public static final String DEFAULT_SUFFIX = ".cache";

    private final Path cacheDir;
    private final String suffix;
    private final long blockSize;
    private String cacheKey;

    /**
     * 创建文件块缓存（默认后缀 .cache、块大小 1MB、无身份绑定）
     * @param cacheDir 缓存目录（不存在自动创建）
     */
    public FileRangeCache(Path cacheDir) {
        this(cacheDir, DEFAULT_SUFFIX, DEFAULT_BLOCK_SIZE, null);
    }

    /**
     * 创建文件块缓存（默认块大小 1MB、无身份绑定）
     * @param cacheDir 缓存目录（不存在自动创建）
     * @param suffix 块文件后缀
     */
    public FileRangeCache(Path cacheDir, String suffix) {
        this(cacheDir, suffix, DEFAULT_BLOCK_SIZE, null);
    }

    /**
     * 创建文件块缓存（无身份绑定）
     * @param cacheDir 缓存目录（不存在自动创建）
     * @param suffix 块文件后缀
     * @param blockSize 块大小（字节），必须为正数
     */
    public FileRangeCache(Path cacheDir, String suffix, long blockSize) {
        this(cacheDir, suffix, blockSize, null);
    }

    /**
     * 创建文件块缓存
     * @param cacheDir 缓存目录（不存在自动创建）
     * @param suffix 块文件后缀
     * @param blockSize 块大小（字节），必须为正数
     * @param cacheKey 源身份标识，非空时按 cacheDir/{cacheKey}/ 子目录隔离，可为 null
     */
    public FileRangeCache(Path cacheDir, String suffix, long blockSize, String cacheKey) {
        if (cacheDir == null) {
            throw new IllegalArgumentException("cacheDir must not be null");
        }
        if (suffix == null) {
            throw new IllegalArgumentException("suffix must not be null");
        }
        if (blockSize <= 0) {
            throw new IllegalArgumentException("blockSize must be positive");
        }
        this.cacheDir = cacheDir;
        this.suffix = suffix;
        this.blockSize = blockSize;
        this.cacheKey = cacheKey;
        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("create cache dir failed: " + cacheDir, e);
        }
    }

    /**
     * 读取指定索引的缓存块
     * 文件不存在、空文件或长度超过 blockSize（损坏）均返回 null（miss）；
     * 精确命中判定由流层期望块长校验完成
     * @param blockIndex 块索引
     * @return 缓存块字节数据；不存在或损坏返回 null
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 缓存读取失败
     */
    @Override
    public byte[] read(long blockIndex) {
        Path file = blockFile(blockIndex);
        try {
            if (!Files.isRegularFile(file)) {
                return null;
            }
            long length = Files.size(file);
            if (length <= 0 || length > blockSize) {
                return null;
            }
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("read cache block failed: " + file, e);
        }
    }

    /**
     * 写入指定索引的缓存块
     * 临时文件 + ATOMIC_MOVE 原子替换；文件系统不支持原子移动时降级为普通替换
     * @param blockIndex 块索引
     * @param data 块数据，不允许为 null
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 缓存写入失败
     */
    @Override
    public void write(long blockIndex, byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null");
        }
        Path target = blockFile(blockIndex);
        Path temp = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            Files.createDirectories(target.getParent());
            Files.write(temp, data);
            try {
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("write cache block failed: " + target, e);
        }
    }

    /**
     * 清除全部缓存块文件（缓存失效入口）
     * cacheKey 非空时清除 cacheDir/{cacheKey}/ 子目录全部内容；
     * cacheKey 为空时清除 cacheDir 下全部内容（目录本身保留）
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 清除失败
     */
    public void clear() {
        Path dir = cacheKey != null ? cacheDir.resolve(cacheKey) : cacheDir;
        deleteContents(dir);
    }

    /**
     * 删除指定索引的缓存块（损坏块淘汰入口）
     * 块不存在时无操作
     * @param blockIndex 块索引
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 删除失败
     */
    public void deleteBlock(long blockIndex) {
        try {
            Files.deleteIfExists(blockFile(blockIndex));
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("delete cache block failed", e);
        }
    }

    /**
     * 获取源身份标识（未绑定返回 null）
     * @return cacheKey
     */
    public String getCacheKey() {
        return cacheKey;
    }

    /**
     * 绑定源身份标识（子目录隔离）
     * 已绑定不同身份 → IllegalArgumentException；已绑定相同身份或无操作
     * 动态绑定仅建议在实例未被并发使用前调用
     * @param cacheKey 源身份标识
     */
    public void bindCacheKey(String cacheKey) {
        if (cacheKey == null) {
            return;
        }
        if (this.cacheKey != null && !this.cacheKey.equals(cacheKey)) {
            throw new IllegalArgumentException("cacheKey already bound: " + this.cacheKey);
        }
        if (this.cacheKey == null) {
            this.cacheKey = cacheKey;
        }
    }

    /**
     * 计算块文件路径
     * cacheKey 非空时位于 cacheDir/{cacheKey}/{index}{suffix}
     * @param blockIndex 块索引
     * @return 块文件路径
     */
    private Path blockFile(long blockIndex) {
        Path dir = cacheDir;
        if (cacheKey != null) {
            dir = dir.resolve(cacheKey);
        }
        return dir.resolve(blockIndex + suffix);
    }

    /**
     * 递归删除目录下全部内容（目录本身保留）
     * @param dir 目标目录
     */
    private static void deleteContents(Path dir) {
        if (!Files.isDirectory(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                .filter(path -> !path.equals(dir))
                .forEach(FileRangeCache::deleteQuietly);
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("clear cache failed: " + dir, e);
        }
    }

    /**
     * 安静删除单个路径（失败抛项目 IOException 终止遍历）
     * @param path 待删除路径
     */
    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("delete cache file failed: " + path, e);
        }
    }
}
