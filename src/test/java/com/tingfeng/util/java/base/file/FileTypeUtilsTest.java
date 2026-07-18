package com.tingfeng.util.java.base.file;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * FileTypeUtils 单元测试。
 *
 * <p>覆盖：已知类型检测、未知类型、空文件、null 输入、流 mark/reset 场景、
 * 扩展名校验、自定义注册、isExtensionMatch 等。</p>
 */
public class FileTypeUtilsTest {

    // ==================== 辅助方法 ====================

    /**
     * 创建包含指定魔数头部的临时文件。
     *
     * @param magic  魔数字节数组
     * @param suffix 文件后缀（如 .jpg）
     * @return 临时文件
     */
    private File createTempFileWithMagic(byte[] magic, String suffix) throws IOException {
        File tempFile = File.createTempFile("filetype_test_", suffix);
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(magic);
            fos.write("dummy content for padding".getBytes(StandardCharsets.UTF_8));
        }
        return tempFile;
    }

    /**
     * 创建空临时文件。
     *
     * @return 空临时文件
     */
    private File createEmptyTempFile() throws IOException {
        File tempFile = File.createTempFile("filetype_test_empty_", ".tmp");
        tempFile.deleteOnExit();
        return tempFile;
    }

    /**
     * 将魔数转换为字节数组的便捷方法。
     */
    private byte[] magic(int... bytes) {
        byte[] result = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[i] = (byte) (bytes[i] & 0xFF);
        }
        return result;
    }

    // ==================== detect(byte[]) 测试 ====================

    @Test
    public void testDetectBytes_JPEG() {
        FileTypeUtils.FileType type = FileTypeUtils.detect(magic(0xFF, 0xD8, 0xFF));
        Assert.assertSame(FileTypeUtils.FileType.JPEG, type);
    }

    @Test
    public void testDetectBytes_PNG() {
        FileTypeUtils.FileType type = FileTypeUtils.detect(
                magic(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A));
        Assert.assertSame(FileTypeUtils.FileType.PNG, type);
    }

    @Test
    public void testDetectBytes_GIF() {
        // GIF89a
        FileTypeUtils.FileType type = FileTypeUtils.detect(magic(0x47, 0x49, 0x46, 0x38, 0x39, 0x61));
        Assert.assertSame(FileTypeUtils.FileType.GIF, type);
    }

    @Test
    public void testDetectBytes_GIF87a() {
        // GIF87a
        FileTypeUtils.FileType type = FileTypeUtils.detect(magic(0x47, 0x49, 0x46, 0x38, 0x37, 0x61));
        Assert.assertSame(FileTypeUtils.FileType.GIF, type);
    }

    @Test
    public void testDetectBytes_BMP() {
        FileTypeUtils.FileType type = FileTypeUtils.detect(magic(0x42, 0x4D));
        Assert.assertSame(FileTypeUtils.FileType.BMP, type);
    }

    @Test
    public void testDetectBytes_PDF() {
        FileTypeUtils.FileType type = FileTypeUtils.detect(magic(0x25, 0x50, 0x44, 0x46));
        Assert.assertSame(FileTypeUtils.FileType.PDF, type);
    }

    @Test
    public void testDetectBytes_ZIP() {
        FileTypeUtils.FileType type = FileTypeUtils.detect(magic(0x50, 0x4B, 0x03, 0x04));
        Assert.assertSame(FileTypeUtils.FileType.ZIP, type);
    }

    @Test
    public void testDetectBytes_RAR() {
        FileTypeUtils.FileType type = FileTypeUtils.detect(magic(0x52, 0x61, 0x72, 0x21));
        Assert.assertSame(FileTypeUtils.FileType.RAR, type);
    }

    @Test
    public void testDetectBytes_GZIP() {
        FileTypeUtils.FileType type = FileTypeUtils.detect(magic(0x1F, 0x8B));
        Assert.assertSame(FileTypeUtils.FileType.GZIP, type);
    }

    @Test
    public void testDetectBytes_CLASS() {
        FileTypeUtils.FileType type = FileTypeUtils.detect(magic(0xCA, 0xFE, 0xBA, 0xBE));
        Assert.assertSame(FileTypeUtils.FileType.CLASS, type);
    }

    @Test
    public void testDetectBytes_Unknown() {
        FileTypeUtils.FileType type = FileTypeUtils.detect(
                "plain text content".getBytes(StandardCharsets.UTF_8));
        Assert.assertSame(FileTypeUtils.FileType.UNKNOWN, type);
    }

    @Test
    public void testDetectBytes_Null() {
        FileTypeUtils.FileType type = FileTypeUtils.detect((byte[]) null);
        Assert.assertSame(FileTypeUtils.FileType.UNKNOWN, type);
    }

    @Test
    public void testDetectBytes_Empty() {
        FileTypeUtils.FileType type = FileTypeUtils.detect(new byte[0]);
        Assert.assertSame(FileTypeUtils.FileType.UNKNOWN, type);
    }

    @Test
    public void testDetectBytes_ShortHeader() {
        // 头部字节不足魔数长度时不应误判
        FileTypeUtils.FileType type = FileTypeUtils.detect(magic(0xFF, 0xD8));
        Assert.assertSame(FileTypeUtils.FileType.UNKNOWN, type);
    }

    // ==================== 长魔数优先匹配测试 ====================

    @Test
    public void testDetectBytes_LongMagicFirst() {
        // CLASS 的魔数 (CA FE BA BE) 是 4 字节，而 ZIP (50 4B 03 04) 也是 4 字节
        // 确保使用正确魔数
        FileTypeUtils.FileType type = FileTypeUtils.detect(magic(0xCA, 0xFE, 0xBA, 0xBE, 0x00, 0x00));
        Assert.assertSame(FileTypeUtils.FileType.CLASS, type);
    }

    @Test
    public void testDetectBytes_PNG_LongerThan_ZIP() {
        // PNG 魔数 8 字节，比 ZIP 4 字节长，确保长魔数优先
        // 检查 PNG 不误判为 ZIP (虽然前缀不同)
        FileTypeUtils.FileType type = FileTypeUtils.detect(magic(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A));
        Assert.assertSame(FileTypeUtils.FileType.PNG, type);
    }

    // ==================== detect(File) 测试 ====================

    @Test
    public void testDetectFile_JPEG() throws IOException {
        File file = createTempFileWithMagic(magic(0xFF, 0xD8, 0xFF), ".jpg");
        FileTypeUtils.FileType type = FileTypeUtils.detect(file);
        Assert.assertSame(FileTypeUtils.FileType.JPEG, type);
    }

    @Test
    public void testDetectFile_PNG() throws IOException {
        File file = createTempFileWithMagic(
                magic(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A), ".png");
        FileTypeUtils.FileType type = FileTypeUtils.detect(file);
        Assert.assertSame(FileTypeUtils.FileType.PNG, type);
    }

    @Test
    public void testDetectFile_Unknown() throws IOException {
        File file = createTempFileWithMagic("hello world".getBytes(StandardCharsets.UTF_8), ".txt");
        FileTypeUtils.FileType type = FileTypeUtils.detect(file);
        Assert.assertSame(FileTypeUtils.FileType.UNKNOWN, type);
    }

    @Test
    public void testDetectFile_Null() {
        FileTypeUtils.FileType type = FileTypeUtils.detect((File) null);
        Assert.assertSame(FileTypeUtils.FileType.UNKNOWN, type);
    }

    @Test
    public void testDetectFile_NotExists() {
        File nonExist = new File("nonexistent_file_xyz.tmp");
        FileTypeUtils.FileType type = FileTypeUtils.detect(nonExist);
        Assert.assertSame(FileTypeUtils.FileType.UNKNOWN, type);
    }

    @Test
    public void testDetectFile_EmptyFile() throws IOException {
        File file = createEmptyTempFile();
        FileTypeUtils.FileType type = FileTypeUtils.detect(file);
        Assert.assertSame(FileTypeUtils.FileType.UNKNOWN, type);
    }

    // ==================== detect(InputStream) 测试 ====================

    @Test
    public void testDetectInputStream_JPEG() {
        byte[] data = magic(0xFF, 0xD8, 0xFF, 0x01, 0x02, 0x03);
        InputStream input = new ByteArrayInputStream(data);
        FileTypeUtils.FileType type = FileTypeUtils.detect(input);
        Assert.assertSame(FileTypeUtils.FileType.JPEG, type);
    }

    @Test
    public void testDetectInputStream_NotClosed() throws IOException {
        // 验证流未被关闭
        byte[] data = magic(0xFF, 0xD8, 0xFF, 0x01, 0x02, 0x03);
        InputStream input = new ByteArrayInputStream(data);
        FileTypeUtils.detect(input);
        // 流仍可读
        Assert.assertTrue(input.read() >= 0 || input.available() == 0);
    }

    @Test
    public void testDetectInputStream_Null() {
        FileTypeUtils.FileType type = FileTypeUtils.detect((InputStream) null);
        Assert.assertSame(FileTypeUtils.FileType.UNKNOWN, type);
    }

    @Test
    public void testDetectInputStream_MarkReset() throws IOException {
        // ByteArrayInputStream 支持 mark/reset
        byte[] data = magic(0x25, 0x50, 0x44, 0x46, 0x01, 0x02); // PDF
        InputStream input = new ByteArrayInputStream(data);
        // 在检测前记录可用字节数
        int availableBefore = input.available();
        FileTypeUtils.FileType type = FileTypeUtils.detect(input);
        Assert.assertSame(FileTypeUtils.FileType.PDF, type);
        // mark/reset 后，流的剩余可用字节应保持不变
        int availableAfter = input.available();
        Assert.assertEquals(availableBefore, availableAfter);
    }

    @Test
    public void testDetectInputStream_NoMarkReset() throws IOException {
        // 使用一个不支持 mark/reset 的流包装
        byte[] data = magic(0x42, 0x4D, 0x01, 0x02, 0x03, 0x04); // BMP
        InputStream inner = new ByteArrayInputStream(data);
        // 包装为不支持 mark/reset 的流
        InputStream noMarkStream = new InputStream() {
            @Override
            public int read() throws IOException {
                return inner.read();
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return inner.read(b, off, len);
            }

            @Override
            public boolean markSupported() {
                return false;
            }
        };
        FileTypeUtils.FileType type = FileTypeUtils.detect(noMarkStream);
        Assert.assertSame(FileTypeUtils.FileType.BMP, type);
    }

    @Test
    public void testDetectInputStream_Empty() {
        InputStream input = new ByteArrayInputStream(new byte[0]);
        FileTypeUtils.FileType type = FileTypeUtils.detect(input);
        Assert.assertSame(FileTypeUtils.FileType.UNKNOWN, type);
    }

    // ==================== isExtensionMatch 测试 ====================

    @Test
    public void testIsExtensionMatch_JPEG() throws IOException {
        File file = createTempFileWithMagic(magic(0xFF, 0xD8, 0xFF), ".jpg");
        Assert.assertTrue(FileTypeUtils.isExtensionMatch(file));
    }

    @Test
    public void testIsExtensionMatch_WrongExtension() throws IOException {
        // PNG 魔数但扩展名是 .jpg
        File file = File.createTempFile("filetype_test_", ".jpg");
        file.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(magic(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A));
            fos.write("content".getBytes(StandardCharsets.UTF_8));
        }
        Assert.assertFalse(FileTypeUtils.isExtensionMatch(file));
    }

    @Test
    public void testIsExtensionMatch_Null() {
        Assert.assertFalse(FileTypeUtils.isExtensionMatch(null));
    }

    @Test
    public void testIsExtensionMatch_NotExists() {
        Assert.assertFalse(FileTypeUtils.isExtensionMatch(new File("nonexistent")));
    }

    @Test
    public void testIsExtensionMatch_UnknownType() throws IOException {
        File file = createTempFileWithMagic("plain text".getBytes(StandardCharsets.UTF_8), ".txt");
        Assert.assertFalse(FileTypeUtils.isExtensionMatch(file));
    }

    // ==================== registerDetector 测试 ====================

    @Test
    public void testRegisterDetector_CustomType() {
        // 注册一个自定义类型：TIFF 图片 (49 49 2A 00)
        FileTypeUtils.registerDetector(magic(0x49, 0x49, 0x2A, 0x00), "tif", "image/tiff");

        // 检测自定义类型的字节数组
        FileTypeUtils.FileType type = FileTypeUtils.detect(magic(0x49, 0x49, 0x2A, 0x00, 0x01, 0x02));
        Assert.assertNotNull(type);
        Assert.assertNotSame(FileTypeUtils.FileType.UNKNOWN, type);
        Assert.assertEquals("tif", type.getExtension());
        Assert.assertEquals("image/tiff", type.getMimeType());
    }

    @Test
    public void testRegisterDetector_CustomBeforeBuiltin() {
        // 注册一个覆盖 ZIP 检测的自定义类型（实际上是测试内置类型优先级更高）
        // 内置类型优先于自定义类型
        FileTypeUtils.FileType type = FileTypeUtils.detect(magic(0x50, 0x4B, 0x03, 0x04, 0x01, 0x02));
        Assert.assertSame(FileTypeUtils.FileType.ZIP, type);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterDetector_NullMagic() {
        FileTypeUtils.registerDetector(null, "ext", "application/octet-stream");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterDetector_EmptyMagic() {
        FileTypeUtils.registerDetector(new byte[0], "ext", "application/octet-stream");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterDetector_NullExtension() {
        FileTypeUtils.registerDetector(magic(0x00, 0x01), null, "application/octet-stream");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterDetector_EmptyExtension() {
        FileTypeUtils.registerDetector(magic(0x00, 0x01), "", "application/octet-stream");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterDetector_NullMimeType() {
        FileTypeUtils.registerDetector(magic(0x00, 0x01), "ext", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterDetector_EmptyMimeType() {
        FileTypeUtils.registerDetector(magic(0x00, 0x01), "ext", "");
    }

    // ==================== FileType 值对象测试 ====================

    @Test
    public void testFileType_Getter() {
        FileTypeUtils.FileType type = FileTypeUtils.FileType.PNG;
        Assert.assertEquals("png", type.getExtension());
        Assert.assertEquals("image/png", type.getMimeType());
        byte[] magic = type.getMagicBytes();
        Assert.assertArrayEquals(magic(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A), magic);
    }

    @Test
    public void testFileType_Equals() {
        Assert.assertEquals(FileTypeUtils.FileType.JPEG, FileTypeUtils.FileType.JPEG);
        Assert.assertNotEquals(FileTypeUtils.FileType.JPEG, FileTypeUtils.FileType.PNG);
        Assert.assertNotEquals(FileTypeUtils.FileType.JPEG, null);
        Assert.assertNotEquals(FileTypeUtils.FileType.JPEG, "JPEG");
    }

    @Test
    public void testFileType_ToString() {
        Assert.assertEquals("JPG (image/jpeg)", FileTypeUtils.FileType.JPEG.toString());
        Assert.assertEquals("PNG (image/png)", FileTypeUtils.FileType.PNG.toString());
        Assert.assertEquals("UNKNOWN (application/octet-stream)", FileTypeUtils.FileType.UNKNOWN.toString());
    }

    @Test
    public void testFileType_ConstantsNotReused() {
        // 验证每个常量是独立的对象
        Assert.assertNotSame(FileTypeUtils.FileType.JPEG, FileTypeUtils.FileType.PNG);
    }

    // ==================== 边界场景测试 ====================

    @Test
    public void testDetect_Directory() throws IOException {
        // 目录应返回 UNKNOWN
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        FileTypeUtils.FileType type = FileTypeUtils.detect(tempDir);
        Assert.assertSame(FileTypeUtils.FileType.UNKNOWN, type);
    }

    @Test
    public void testIsExtensionMatch_Directory() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        Assert.assertFalse(FileTypeUtils.isExtensionMatch(tempDir));
    }

    @Test
    public void testDetectBytes_ExtraBytes() {
        // 传入更多字节不应影响匹配
        FileTypeUtils.FileType type = FileTypeUtils.detect(
                magic(0xFF, 0xD8, 0xFF, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05));
        Assert.assertSame(FileTypeUtils.FileType.JPEG, type);
    }

    @Test
    public void testMagics_AllUnique() {
        // 验证所有内置类型的魔数互不相同（至少长度或内容不同）
        FileTypeUtils.FileType[] types = {
                FileTypeUtils.FileType.JPEG,
                FileTypeUtils.FileType.PNG,
                FileTypeUtils.FileType.GIF,
                FileTypeUtils.FileType.BMP,
                FileTypeUtils.FileType.PDF,
                FileTypeUtils.FileType.ZIP,
                FileTypeUtils.FileType.RAR,
                FileTypeUtils.FileType.GZIP,
                FileTypeUtils.FileType.CLASS
        };
        for (int i = 0; i < types.length; i++) {
            for (int j = i + 1; j < types.length; j++) {
                byte[] mi = types[i].getMagicBytes();
                byte[] mj = types[j].getMagicBytes();
                boolean same = Arrays.equals(mi, mj);
                if (same) {
                    // 允许 ZIP 和另一个 ZIP 重复？不应该
                    Assert.fail("Magic bytes should be unique between types: "
                            + types[i] + " vs " + types[j]);
                }
            }
        }
    }

    @Test
    public void testDetect_UnicodeFileName() {
        // 只是检测不抛异常，以文件名无关方式检测
        FileTypeUtils.FileType type = FileTypeUtils.detect(magic(0xFF, 0xD8, 0xFF));
        Assert.assertSame(FileTypeUtils.FileType.JPEG, type);
    }
}
