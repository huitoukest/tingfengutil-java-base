package com.tingfeng.util.java.base.file;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 基于文件魔数（Magic Number / 文件签名）的文件类型检测工具类。
 *
 * <p>通过读取文件头部字节（通常前 32 字节），与内置的魔数签名进行匹配，
 * 从而检测文件的真实类型，不受文件扩展名影响。</p>
 *
 * <p>内置常见文件类型的魔数（JPEG、PNG、GIF、BMP、PDF、ZIP、RAR、GZIP、CLASS），
 * 并支持通过 {@link #registerDetector(byte[], String, String)} 扩展自定义类型。</p>
 *
 * <p>魔数匹配规则：优先匹配更长的魔数（避免短魔数误判），内置类型优先于自定义类型。</p>
 *
 * <p>使用示例：
 * <pre>{@code
 *   FileType type = FileTypeUtils.detect(new File("photo.jpg"));
 *   if (type == FileTypeUtils.FileType.JPEG) { ... }
 *
 *   FileType type2 = FileTypeUtils.detect(fileHeaderBytes);
 *   boolean match = FileTypeUtils.isExtensionMatch(file);
 * }</pre>
 * </p>
 *
 * @author huitoukest
 */
public final class FileTypeUtils {

    private FileTypeUtils() {
    }

    /** 读取文件头部的最大字节数 */
    private static final int MAX_HEADER_BYTES = 32;

    /** 自定义检测器列表（线程安全） */
    private static final List<CustomDetector> customDetectors = new CopyOnWriteArrayList<>();

    // ==================== 文件类型定义 ====================

    /**
     * 已知文件类型（魔数签名）。
     *
     * <p>每个实例包含：魔数字节数组、对应扩展名、MIME 类型。
     * 内置类型包括 JPEG、PNG、GIF、BMP、PDF、ZIP、RAR、GZIP、CLASS，
     * 以及兜底的 UNKNOWN。</p>
     *
     * <p>可通过 {@link FileTypeUtils#registerDetector(byte[], String, String)}
     * 扩展自定义文件类型。</p>
     */
    public static final class FileType {

        // ==================== 内置文件类型常量 ====================

        /** JPEG 图片 (FF D8 FF) */
        public static final FileType JPEG = new FileType(
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
                "jpg", "image/jpeg");

        /** PNG 图片 (89 50 4E 47 0D 0A 1A 0A) */
        public static final FileType PNG = new FileType(
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A},
                "png", "image/png");

        /** GIF 图片 (47 49 46 38，同时匹配 GIF87a 和 GIF89a) */
        public static final FileType GIF = new FileType(
                new byte[]{0x47, 0x49, 0x46, 0x38},
                "gif", "image/gif");

        /** BMP 图片 (42 4D) */
        public static final FileType BMP = new FileType(
                new byte[]{0x42, 0x4D},
                "bmp", "image/bmp");

        /** PDF 文档 (25 50 44 46) */
        public static final FileType PDF = new FileType(
                new byte[]{0x25, 0x50, 0x44, 0x46},
                "pdf", "application/pdf");

        /** ZIP 压缩包 (50 4B 03 04，也匹配 DOCX/XLSX 等基于 ZIP 的格式) */
        public static final FileType ZIP = new FileType(
                new byte[]{0x50, 0x4B, 0x03, 0x04},
                "zip", "application/zip");

        /** RAR 压缩包 (52 61 72 21) */
        public static final FileType RAR = new FileType(
                new byte[]{0x52, 0x61, 0x72, 0x21},
                "rar", "application/vnd.rar");

        /** GZIP 压缩文件 (1F 8B) */
        public static final FileType GZIP = new FileType(
                new byte[]{0x1F, (byte) 0x8B},
                "gz", "application/gzip");

        /** Java Class 文件 (CA FE BA BE) */
        public static final FileType CLASS = new FileType(
                new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE},
                "class", "application/java-vm");

        /** 未知文件类型 */
        public static final FileType UNKNOWN = new FileType(
                new byte[]{}, "unknown", "application/octet-stream");

        /** 魔数字节数组 */
        private final byte[] magicBytes;

        /** 文件扩展名（不含点） */
        private final String extension;

        /** MIME 类型 */
        private final String mimeType;

        /**
         * 私有构造器（仅供内置常量和静态工厂使用）。
         *
         * @param magicBytes 魔数字节数组
         * @param extension  文件扩展名
         * @param mimeType   MIME 类型
         */
        private FileType(byte[] magicBytes, String extension, String mimeType) {
            this.magicBytes = magicBytes;
            this.extension = extension;
            this.mimeType = mimeType;
        }

        /**
         * 创建自定义 FileType 实例（由 registerDetector 使用）。
         *
         * @param magicBytes 魔数字节数组
         * @param extension  文件扩展名
         * @param mimeType   MIME 类型
         * @return 新的 FileType 实例
         */
        static FileType createCustom(byte[] magicBytes, String extension, String mimeType) {
            return new FileType(magicBytes.clone(), extension, mimeType);
        }

        // ==================== Getter ====================

        /**
         * 返回魔数字节数组。
         *
         * @return 魔数字节数组（防御性拷贝）
         */
        public byte[] getMagicBytes() {
            return magicBytes.clone();
        }

        /**
         * 返回文件扩展名（不含点）。
         *
         * @return 扩展名
         */
        public String getExtension() {
            return extension;
        }

        /**
         * 返回 MIME 类型。
         *
         * @return MIME 类型
         */
        public String getMimeType() {
            return mimeType;
        }

        // ==================== equals / hashCode / toString ====================

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof FileType)) {
                return false;
            }
            FileType fileType = (FileType) o;
            return extension.equals(fileType.extension)
                    && mimeType.equals(fileType.mimeType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(extension, mimeType);
        }

        @Override
        public String toString() {
            return extension.toUpperCase(Locale.ROOT) + " (" + mimeType + ")";
        }

        // ==================== 内置类型列表（按魔数长度降序） ====================

        /** 内置文件类型列表，按魔数长度降序排列（长魔数优先匹配） */
        private static final List<FileType> BUILTIN_TYPES;

        static {
            List<FileType> types = new ArrayList<>();
            types.add(JPEG);
            types.add(PNG);
            types.add(GIF);
            types.add(BMP);
            types.add(PDF);
            types.add(ZIP);
            types.add(RAR);
            types.add(GZIP);
            types.add(CLASS);
            // 按魔数长度降序排序（长魔数优先匹配）
            types.sort((a, b) -> Integer.compare(b.magicBytes.length, a.magicBytes.length));
            BUILTIN_TYPES = Collections.unmodifiableList(types);
        }

        /**
         * 从文件头部字节数组中检测文件类型。
         *
         * <p>匹配规则：先匹配内置类型（按魔数长度降序），再匹配自定义注册类型。
         * 短魔数（如 BMP 的 42 4D）不会误判长魔数（如 CLASS 的 CA FE BA BE）。</p>
         *
         * @param fileHeader 文件头部字节数组
         * @return 匹配的 FileType；无匹配时返回 {@link #UNKNOWN}
         */
        static FileType fromMagic(byte[] fileHeader) {
            if (null == fileHeader || fileHeader.length == 0) {
                return UNKNOWN;
            }
            // 检查内置类型（按魔数长度降序，长魔数优先）
            for (FileType type : BUILTIN_TYPES) {
                if (startsWith(fileHeader, type.magicBytes)) {
                    return type;
                }
            }
            // 检查自定义注册类型
            for (CustomDetector detector : customDetectors) {
                if (startsWith(fileHeader, detector.magicBytes)) {
                    return detector.fileType;
                }
            }
            return UNKNOWN;
        }

        /**
         * 判断字节数组是否以指定前缀开头。
         *
         * @param data   待检测字节数组
         * @param prefix 前缀字节数组
         * @return true 如果 data 以 prefix 开头
         */
        private static boolean startsWith(byte[] data, byte[] prefix) {
            if (data.length < prefix.length) {
                return false;
            }
            for (int i = 0; i < prefix.length; i++) {
                if (data[i] != prefix[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    // ==================== 自定义检测器 ====================

    /**
     * 自定义检测器，存储用户注册的魔数和对应的文件类型信息。
     */
    private static final class CustomDetector {
        final byte[] magicBytes;
        final FileType fileType;

        CustomDetector(byte[] magicBytes, String extension, String mimeType) {
            this.magicBytes = magicBytes.clone();
            this.fileType = FileType.createCustom(magicBytes, extension, mimeType);
        }
    }

    // ==================== 核心检测方法 ====================

    /**
     * 从文件检测文件类型。
     *
     * <p>读取文件头部字节并与魔数签名匹配。匹配失败或发生 IO 异常时返回 UNKNOWN，
     * 不会抛出异常。</p>
     *
     * @param file 目标文件
     * @return 检测到的 FileType；文件为 null、不存在、目录或 IO 异常时返回 {@link FileType#UNKNOWN}
     */
    public static FileType detect(File file) {
        if (null == file || !file.exists() || file.isDirectory()) {
            return FileType.UNKNOWN;
        }
        // 使用 BufferedInputStream 以支持 mark/reset
        try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
            return detect(input);
        } catch (IOException e) {
            return FileType.UNKNOWN;
        }
    }

    /**
     * 从字节数组检测文件类型。
     *
     * <p>传入文件头部 N 字节（通常前 32 字节即可），与魔数签名匹配。</p>
     *
     * @param fileHeader 文件头部字节数组
     * @return 检测到的 FileType；参数为 null 或空数组时返回 {@link FileType#UNKNOWN}
     */
    public static FileType detect(byte[] fileHeader) {
        return FileType.fromMagic(fileHeader);
    }

    /**
     * 从输入流检测文件类型（不关闭流）。
     *
     * <p>如果输入流支持 {@link InputStream#markSupported mark/reset}，则读取头部字节后
     * 重置流位置；如果不支持，则读取全部流内容到字节数组副本进行检测（流会被消费）。</p>
     *
     * @param input 输入流
     * @return 检测到的 FileType；参数为 null 时返回 {@link FileType#UNKNOWN}
     */
    public static FileType detect(InputStream input) {
        if (null == input) {
            return FileType.UNKNOWN;
        }
        byte[] header = readHeader(input);
        return FileType.fromMagic(header);
    }

    // ==================== 扩展匹配 ====================

    /**
     * 校验文件的魔数类型与扩展名是否一致（双重校验）。
     *
     * <p>先通过魔数检测真实类型，再与文件扩展名比较。可用于检测伪装扩展名的文件。</p>
     *
     * @param file 目标文件
     * @return true=魔数类型与扩展名一致；false=不一致、文件为 null、不存在或类型未知
     */
    public static boolean isExtensionMatch(File file) {
        if (null == file || !file.exists() || file.isDirectory()) {
            return false;
        }
        FileType detected = detect(file);
        if (detected == FileType.UNKNOWN) {
            return false;
        }
        String extension = getExtension(file.getName());
        return detected.extension.equalsIgnoreCase(extension);
    }

    // ==================== 扩展注册 ====================

    /**
     * 注册自定义文件类型检测器。
     *
     * <p>注册的魔数会参与魔数匹配，按注册顺序检测（在内置类型之后）。
     * 注册的检测器存储在 {@link CopyOnWriteArrayList} 中，保证线程安全。</p>
     *
     * <p>注意：已注册的检测器无法移除，如需覆盖请先确认注册顺序。</p>
     *
     * @param magicBytes 魔数字节数组（不能为 null 或空）
     * @param extension  文件扩展名（不含点，不能为 null 或空）
     * @param mimeType   MIME 类型（不能为 null 或空）
     * @throws IllegalArgumentException 任意参数为 null 或空时抛出
     */
    public static void registerDetector(byte[] magicBytes, String extension, String mimeType) {
        if (null == magicBytes || magicBytes.length == 0) {
            throw new IllegalArgumentException("Magic bytes must not be null or empty");
        }
        if (null == extension || extension.isEmpty()) {
            throw new IllegalArgumentException("Extension must not be null or empty");
        }
        if (null == mimeType || mimeType.isEmpty()) {
            throw new IllegalArgumentException("MIME type must not be null or empty");
        }
        customDetectors.add(new CustomDetector(magicBytes, extension, mimeType));
    }

    // ==================== 内部工具方法 ====================

    /**
     * 从输入流读取头部字节数组。
     *
     * <p>最多读取 {@value #MAX_HEADER_BYTES} 字节。
     * 如果流支持 mark/reset，则读取后重置流位置；
     * 如果不支持，则读取全部流内容到字节数组副本。</p>
     *
     * @param input 输入流
     * @return 头部字节数组（可能为空数组，不会为 null）
     */
    private static byte[] readHeader(InputStream input) {
        if (input.markSupported()) {
            // mark/reset 模式：读取头部后重置流
            input.mark(MAX_HEADER_BYTES);
            try {
                byte[] header = new byte[MAX_HEADER_BYTES];
                int bytesRead = 0;
                int read;
                while (bytesRead < MAX_HEADER_BYTES
                        && (read = input.read(header, bytesRead, MAX_HEADER_BYTES - bytesRead)) != -1) {
                    bytesRead += read;
                }
                input.reset();
                if (bytesRead == 0) {
                    return new byte[0];
                }
                return Arrays.copyOf(header, bytesRead);
            } catch (IOException e) {
                // 读取或重置失败，返回空数组
                return new byte[0];
            }
        } else {
            // 不支持 mark/reset：仅读取前 MAX_HEADER_BYTES 字节
            try {
                byte[] header = new byte[MAX_HEADER_BYTES];
                int totalRead = 0;
                while (totalRead < MAX_HEADER_BYTES) {
                    int nRead = input.read(header, totalRead, MAX_HEADER_BYTES - totalRead);
                    if (nRead == -1) break;
                    totalRead += nRead;
                }
                if (totalRead == 0) {
                    return new byte[0];
                }
                return Arrays.copyOf(header, totalRead);
            } catch (IOException e) {
                return new byte[0];
            }
        }
    }

    /**
     * 从文件名中提取扩展名（不含点，小写）。
     *
     * @param fileName 文件名
     * @return 扩展名；无扩展名或文件名为空时返回空字符串
     */
    private static String getExtension(String fileName) {
        if (null == fileName || fileName.isEmpty()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
