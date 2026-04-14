package com.tingfeng.util.java.base.file;

import com.tingfeng.util.java.base.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * FileUtils 单元测试
 */
public class FileUtilsTest {

    private static final String TEST_CONTENT = "Hello, World! 你好，世界！";
    private static final String TEST_FILE_NAME = "test_io_utils.txt";

    // ==================== 文件读写测试 ====================

    @Test
    public void testReadFileToByteArray() throws IOException {
        File tempFile = createTempFile(TEST_CONTENT);
        try {
            byte[] bytes = FileUtils.readFileToByteArray(tempFile);
            Assert.assertNotNull(bytes);
            Assert.assertEquals(TEST_CONTENT, new String(bytes, StandardCharsets.UTF_8));
        } finally {
            deleteTempFile(tempFile);
        }
    }

    @Test(expected = com.tingfeng.util.java.base.lang.exception.FileNotFoundException.class)
    public void testReadFileToByteArrayNotFound() {
        FileUtils.readFileToByteArray(new File("non_existent_file.txt"));
    }

    @Test
    public void testReadFileToString() throws IOException {
        File tempFile = createTempFile(TEST_CONTENT);
        try {
            String content = FileUtils.readFileToString(tempFile);
            Assert.assertEquals(TEST_CONTENT, content);
        } finally {
            deleteTempFile(tempFile);
        }
    }

    @Test
    public void testReadFileToStringWithCharset() throws IOException {
        File tempFile = createTempFile(TEST_CONTENT);
        try {
            String content = FileUtils.readFileToString(tempFile, StandardCharsets.UTF_8);
            Assert.assertEquals(TEST_CONTENT, content);
        } finally {
            deleteTempFile(tempFile);
        }
    }

    @Test
    public void testWriteByteArrayToFile() throws IOException {
        File tempFile = createTempFile("");
        try {
            byte[] data = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
            FileUtils.writeByteArrayToFile(tempFile, data);

            byte[] readBack = FileUtils.readFileToByteArray(tempFile);
            Assert.assertEquals(TEST_CONTENT, new String(readBack, StandardCharsets.UTF_8));
        } finally {
            deleteTempFile(tempFile);
        }
    }

    @Test
    public void testWriteStringToFile() throws IOException {
        File tempFile = createTempFile("");
        try {
            FileUtils.writeStringToFile(tempFile, TEST_CONTENT);

            String content = FileUtils.readFileToString(tempFile);
            Assert.assertEquals(TEST_CONTENT, content);
        } finally {
            deleteTempFile(tempFile);
        }
    }

    @Test
    public void testWriteStringToFileAppend() throws IOException {
        File tempFile = createTempFile("Initial");
        try {
            FileUtils.writeStringToFile(tempFile, TEST_CONTENT, StandardCharsets.UTF_8, true);

            String content = FileUtils.readFileToString(tempFile);
            Assert.assertEquals("Initial" + TEST_CONTENT, content);
        } finally {
            deleteTempFile(tempFile);
        }
    }

    @Test
    public void testWriteStringToFileOverwrite() throws IOException {
        File tempFile = createTempFile("Initial");
        try {
            FileUtils.writeStringToFile(tempFile, TEST_CONTENT, StandardCharsets.UTF_8, false);

            String content = FileUtils.readFileToString(tempFile);
            Assert.assertEquals(TEST_CONTENT, content);
        } finally {
            deleteTempFile(tempFile);
        }
    }

    // ==================== 行写入测试 ====================

    @Test
    public void testWriteLine() throws IOException {
        File tempFile = createTempFile("");
        try {
            FileUtils.writeLine(tempFile, "Line 1");
            FileUtils.writeLine(tempFile, "Line 2");

            List<String> lines = Files.readAllLines(tempFile.toPath(), StandardCharsets.UTF_8);
            Assert.assertEquals(2, lines.size());
            Assert.assertEquals("Line 1", lines.get(0));
            Assert.assertEquals("Line 2", lines.get(1));
        } finally {
            deleteTempFile(tempFile);
        }
    }

    @Test
    public void testWriteLineWithCharset() throws IOException {
        File tempFile = createTempFile("");
        try {
            FileUtils.writeLine(tempFile, "Line 1", StandardCharsets.UTF_8);
            FileUtils.writeLine(tempFile, "Line 2", StandardCharsets.UTF_8);

            List<String> lines = Files.readAllLines(tempFile.toPath(), StandardCharsets.UTF_8);
            Assert.assertEquals(2, lines.size());
        } finally {
            deleteTempFile(tempFile);
        }
    }

    @Test
    public void testWriteLineOverwrite() throws IOException {
        File tempFile = createTempFile("Initial Line");
        try {
            FileUtils.writeLine(tempFile, "New Line", StandardCharsets.UTF_8, false);

            List<String> lines = Files.readAllLines(tempFile.toPath(), StandardCharsets.UTF_8);
            Assert.assertEquals(1, lines.size());
            Assert.assertEquals("New Line", lines.get(0));
        } finally {
            deleteTempFile(tempFile);
        }
    }

    @Test
    public void testWriteLines() throws IOException {
        File tempFile = createTempFile("");
        try {
            List<String> lines = Arrays.asList("Line 1", "Line 2", "Line 3");
            FileUtils.writeLines(tempFile, lines);

            List<String> readBack = Files.readAllLines(tempFile.toPath(), StandardCharsets.UTF_8);
            Assert.assertEquals(3, readBack.size());
            Assert.assertEquals("Line 1", readBack.get(0));
            Assert.assertEquals("Line 2", readBack.get(1));
            Assert.assertEquals("Line 3", readBack.get(2));
        } finally {
            deleteTempFile(tempFile);
        }
    }

    @Test
    public void testWriteLinesAppend() throws IOException {
        // 使用换行符结尾的初始内容
        File tempFile = createTempFile("Existing\n");
        try {
            List<String> lines = Arrays.asList("Line 1", "Line 2");
            FileUtils.writeLines(tempFile, lines, StandardCharsets.UTF_8, true);

            List<String> readBack = Files.readAllLines(tempFile.toPath(), StandardCharsets.UTF_8);
            // 追加后应该有3行: "Existing", "Line 1", "Line 2"
            Assert.assertEquals(3, readBack.size());
        } finally {
            deleteTempFile(tempFile);
        }
    }

    // ==================== 异步文件操作测试 ====================

    @Test
    public void testReadFileAsync() throws Exception {
        File tempFile = createTempFile(TEST_CONTENT);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            byte[] result = FileUtils.readFileAsync(tempFile, executor).get();
            Assert.assertEquals(TEST_CONTENT, new String(result, StandardCharsets.UTF_8));
        } finally {
            executor.shutdown();
            deleteTempFile(tempFile);
        }
    }

    @Test
    public void testReadFileAsyncWithProgress() throws Exception {
        File tempFile = createTempFile(TEST_CONTENT);
        AtomicInteger progressCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            byte[] result = FileUtils.readFileAsync(tempFile, executor,
                (read, total) -> {
                    progressCount.incrementAndGet();
                    return true;
                }, null).get();

            Assert.assertEquals(TEST_CONTENT, new String(result, StandardCharsets.UTF_8));
            Assert.assertTrue(progressCount.get() > 0);
        } finally {
            executor.shutdown();
            deleteTempFile(tempFile);
        }
    }

    @Test
    public void testWriteFileAsync() throws Exception {
        File tempFile = createTempFile("");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            byte[] data = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
            Boolean result = FileUtils.writeFileAsync(tempFile, data, executor, null, null).get();

            Assert.assertTrue(result);
            String content = FileUtils.readFileToString(tempFile);
            Assert.assertEquals(TEST_CONTENT, content);
        } finally {
            executor.shutdown();
            deleteTempFile(tempFile);
        }
    }

    @Test
    public void testWriteFileAsyncWithProgress() throws Exception {
        File tempFile = createTempFile("");
        AtomicInteger progressCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            byte[] data = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
            Boolean result = FileUtils.writeFileAsync(tempFile, data, executor,
                (written, total) -> {
                    progressCount.incrementAndGet();
                }, null).get();

            Assert.assertTrue(result);
        } finally {
            executor.shutdown();
            deleteTempFile(tempFile);
        }
    }

    @Test
    public void testWriteLineAsync() throws Exception {
        File tempFile = createTempFile("");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Boolean result = FileUtils.writeLineAsync(tempFile, "Async Line",
                StandardCharsets.UTF_8, true, executor, null).get();

            Assert.assertTrue(result);
            List<String> lines = Files.readAllLines(tempFile.toPath(), StandardCharsets.UTF_8);
            Assert.assertEquals(1, lines.size());
            Assert.assertEquals("Async Line", lines.get(0));
        } finally {
            executor.shutdown();
            deleteTempFile(tempFile);
        }
    }

    @Test
    public void testCopyFileAsync() throws Exception {
        File srcFile = createTempFile(TEST_CONTENT);
        File destFile = new File(srcFile.getParent(), "copy_dest.txt");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            AtomicLong progress = new AtomicLong(0);

            Long result = FileUtils.copyFileAsync(destFile, srcFile, executor,
                progress::set, FileUtils.DEFAULT_BACK_PRESSURE_BUFFER_SIZE, null).get();

            Assert.assertTrue(result > 0);
            String content = FileUtils.readFileToString(destFile);
            Assert.assertEquals(TEST_CONTENT, content);
        } finally {
            executor.shutdown();
            deleteTempFile(srcFile);
            deleteTempFile(destFile);
        }
    }

    @Test
    public void testCopyFileAsyncWithCancellation() throws Exception {
        // 创建一个较大的源文件
        File srcFile = createTempFile(new String(new byte[1024 * 1024])); // 1MB
        File destFile = new File(srcFile.getParent(), "cancel_copy_dest.txt");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            IOUtils.CancellationToken token = new IOUtils.CancellationToken();
            token.cancel(); // 立即取消

            Long result = FileUtils.copyFileAsync(destFile, srcFile, executor,
                total -> {}, FileUtils.DEFAULT_BACK_PRESSURE_BUFFER_SIZE, token).get();

            // 取消后应该停止拷贝
            Assert.assertTrue(result < srcFile.length());
        } finally {
            executor.shutdown();
            deleteTempFile(srcFile);
            deleteTempFile(destFile);
        }
    }

    // ==================== 辅助方法 ====================

    private File createTempFile(String content) throws IOException {
        File tempDir = Files.createTempDirectory("io_utils_test").toFile();
        File tempFile = new File(tempDir, TEST_FILE_NAME);
        Files.write(tempFile.toPath(), content.getBytes(StandardCharsets.UTF_8));
        return tempFile;
    }

    private void deleteTempFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
            File parent = file.getParentFile();
            if (parent != null && parent.exists()) {
                parent.delete();
            }
        }
    }
}
