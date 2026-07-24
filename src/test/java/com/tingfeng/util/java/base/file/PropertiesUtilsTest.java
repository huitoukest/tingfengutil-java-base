package com.tingfeng.util.java.base.file;

import com.tingfeng.util.java.base.lang.exception.BaseException;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

/**
 * PropertiesUtils 单元测试
 */
public class PropertiesUtilsTest {

    // ==================== 辅助方法 ====================

    private File createTempDir() throws IOException {
        return Files.createTempDirectory("properties_utils_test").toFile();
    }

    private File createTempPropertiesFile(String content, Charset charset) throws IOException {
        File tempDir = createTempDir();
        File file = new File(tempDir, "test.properties");
        Files.write(file.toPath(), content.getBytes(charset));
        return file;
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

    // ==================== load(File) 测试 ====================

    @Test
    public void testLoadFile() throws IOException {
        File file = createTempPropertiesFile("name=hello\nage=25", StandardCharsets.UTF_8);
        try {
            Properties props = PropertiesUtils.load(file);
            Assert.assertEquals("hello", props.getProperty("name"));
            Assert.assertEquals("25", props.getProperty("age"));
            Assert.assertEquals(2, props.size());
        } finally {
            deleteTempFile(file);
        }
    }

    @Test
    public void testLoadFileWithCharset() throws IOException {
        // 使用 UTF-8 编码的中文内容
        File file = createTempPropertiesFile("name=你好\nkey=value", StandardCharsets.UTF_8);
        try {
            Properties props = PropertiesUtils.load(file, StandardCharsets.UTF_8);
            Assert.assertEquals("你好", props.getProperty("name"));
            Assert.assertEquals("value", props.getProperty("key"));
        } finally {
            deleteTempFile(file);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadFileNull() {
        PropertiesUtils.load((File) null);
    }

    @Test
    public void testLoadFileNotExists() {
        File nonExist = new File("non_existent.properties");
        Properties props = PropertiesUtils.load(nonExist);
        Assert.assertNotNull(props);
        Assert.assertTrue(props.isEmpty());
    }

    // ==================== load(InputStream) 测试 ====================

    @Test
    public void testLoadInputStream() throws IOException {
        String content = "key1=value1\nkey2=value2";
        InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        Properties props = PropertiesUtils.load(input);
        Assert.assertEquals("value1", props.getProperty("key1"));
        Assert.assertEquals("value2", props.getProperty("key2"));
        Assert.assertEquals(2, props.size());
        // 确认流未被关闭（spec：不关闭流）
        // ByteArrayInputStream 被 Reader 消费后 read() 返回 -1 但流仍处于开启状态
        Assert.assertEquals(-1, input.read());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadInputStreamNull() {
        PropertiesUtils.load((InputStream) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadInputStreamWithNullCharset() {
        InputStream input = new ByteArrayInputStream("key=value".getBytes());
        PropertiesUtils.load(input, null);
    }

    // ==================== loadFromClasspath 测试 ====================

    @Test(expected = IllegalArgumentException.class)
    public void testLoadFromClasspathNull() {
        PropertiesUtils.loadFromClasspath(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadFromClasspathEmpty() {
        PropertiesUtils.loadFromClasspath("");
    }

    @Test(expected = BaseException.class)
    public void testLoadFromClasspathNotFound() {
        PropertiesUtils.loadFromClasspath("nonexistent/resource.properties");
    }

    // ==================== store 测试 ====================

    @Test
    public void testStoreFile() throws IOException {
        Properties props = new Properties();
        props.setProperty("name", "test");
        props.setProperty("value", "123");

        File tempDir = createTempDir();
        File file = new File(tempDir, "output.properties");
        try {
            PropertiesUtils.store(props, file, "test comments");
            Assert.assertTrue(file.exists());

            Properties loaded = PropertiesUtils.load(file);
            Assert.assertEquals("test", loaded.getProperty("name"));
            Assert.assertEquals("123", loaded.getProperty("value"));
        } finally {
            deleteTempFile(file);
        }
    }

    @Test
    public void testStoreFileAutoCreateParentDir() throws IOException {
        Properties props = new Properties();
        props.setProperty("key", "value");

        File tempDir = createTempDir();
        File nestedFile = new File(tempDir, "subdir/nested.properties");
        try {
            PropertiesUtils.store(props, nestedFile, null);
            Assert.assertTrue(nestedFile.exists());

            Properties loaded = PropertiesUtils.load(nestedFile);
            Assert.assertEquals("value", loaded.getProperty("key"));
        } finally {
            deleteTempFile(nestedFile);
            // 清理子目录
            File subdir = new File(tempDir, "subdir");
            if (subdir.exists()) {
                subdir.delete();
            }
            deleteTempFile(tempDir);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStoreNullProperties() {
        PropertiesUtils.store(null, new File("dummy"), "comments");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStoreNullFile() {
        PropertiesUtils.store(new Properties(), (File) null, "comments");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStoreNullPropertiesWithOutputStream() {
        PropertiesUtils.store((Properties) null, new ByteArrayOutputStream(), "comments");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStoreNullOutputStream() {
        PropertiesUtils.store(new Properties(), (OutputStream) null, "comments");
    }

    // ==================== getString 测试 ====================

    @Test
    public void testGetString() {
        Properties props = new Properties();
        props.setProperty("name", "hello");

        Assert.assertEquals("hello", PropertiesUtils.getString(props, "name", "default"));
        Assert.assertEquals("default", PropertiesUtils.getString(props, "notexist", "default"));
    }

    @Test
    public void testGetStringWithNullProps() {
        Assert.assertEquals("default", PropertiesUtils.getString(null, "key", "default"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStringNullKey() {
        PropertiesUtils.getString(new Properties(), null, "default");
    }

    // ==================== getInt 测试 ====================

    @Test
    public void testGetInt() {
        Properties props = new Properties();
        props.setProperty("count", "42");
        props.setProperty("invalid", "notanumber");

        Assert.assertEquals(42, PropertiesUtils.getInt(props, "count", 0));
        Assert.assertEquals(0, PropertiesUtils.getInt(props, "invalid", 0));
        Assert.assertEquals(99, PropertiesUtils.getInt(props, "notexist", 99));
    }

    @Test
    public void testGetIntWithNullProps() {
        Assert.assertEquals(10, PropertiesUtils.getInt(null, "key", 10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetIntNullKey() {
        PropertiesUtils.getInt(new Properties(), null, 0);
    }

    // ==================== getBoolean 测试 ====================

    @Test
    public void testGetBoolean() {
        Properties props = new Properties();
        props.setProperty("flag1", "true");
        props.setProperty("flag2", "TRUE");
        props.setProperty("flag3", "yes");
        props.setProperty("flag4", "YES");
        props.setProperty("flag5", "on");
        props.setProperty("flag6", "1");
        props.setProperty("flag7", "false");

        Assert.assertTrue(PropertiesUtils.getBoolean(props, "flag1", false));
        Assert.assertTrue(PropertiesUtils.getBoolean(props, "flag2", false));
        Assert.assertTrue(PropertiesUtils.getBoolean(props, "flag3", false));
        Assert.assertTrue(PropertiesUtils.getBoolean(props, "flag4", false));
        Assert.assertTrue(PropertiesUtils.getBoolean(props, "flag5", false));
        Assert.assertTrue(PropertiesUtils.getBoolean(props, "flag6", false));
        Assert.assertFalse(PropertiesUtils.getBoolean(props, "flag7", true));
        Assert.assertTrue(PropertiesUtils.getBoolean(props, "notexist", true));
    }

    @Test
    public void testGetBooleanWithNullProps() {
        Assert.assertTrue(PropertiesUtils.getBoolean(null, "key", true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBooleanNullKey() {
        PropertiesUtils.getBoolean(new Properties(), null, false);
    }

    // ==================== append 测试 ====================

    @Test
    public void testAppendNewKeys() throws IOException {
        File file = createTempPropertiesFile("existing.key=old", StandardCharsets.UTF_8);
        try {
            Properties toAppend = new Properties();
            toAppend.setProperty("new.key", "newvalue");
            toAppend.setProperty("another.key", "anothervalue");

            PropertiesUtils.append(file, toAppend);

            Properties result = PropertiesUtils.load(file);
            Assert.assertEquals("old", result.getProperty("existing.key"));
            Assert.assertEquals("newvalue", result.getProperty("new.key"));
            Assert.assertEquals("anothervalue", result.getProperty("another.key"));
            Assert.assertEquals(3, result.size());
        } finally {
            deleteTempFile(file);
        }
    }

    @Test
    public void testAppendDoesNotOverwriteExistingKeys() throws IOException {
        File file = createTempPropertiesFile("key1=original", StandardCharsets.UTF_8);
        try {
            Properties toAppend = new Properties();
            toAppend.setProperty("key1", "overwrite");
            toAppend.setProperty("key2", "new");

            PropertiesUtils.append(file, toAppend);

            Properties result = PropertiesUtils.load(file);
            // 已有 key 不被覆盖
            Assert.assertEquals("original", result.getProperty("key1"));
            // 新 key 被添加
            Assert.assertEquals("new", result.getProperty("key2"));
        } finally {
            deleteTempFile(file);
        }
    }

    @Test
    public void testAppendToNewFile() throws IOException {
        File tempDir = createTempDir();
        File file = new File(tempDir, "new.properties");
        try {
            Properties toAppend = new Properties();
            toAppend.setProperty("key", "value");

            PropertiesUtils.append(file, toAppend);

            Assert.assertTrue(file.exists());
            Properties result = PropertiesUtils.load(file);
            Assert.assertEquals("value", result.getProperty("key"));
        } finally {
            deleteTempFile(file);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullFile() {
        PropertiesUtils.append(null, new Properties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullProps() {
        PropertiesUtils.append(new File("dummy"), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullCharset() {
        PropertiesUtils.append(new File("dummy"), new Properties(), null);
    }

    // ==================== 编码测试 ====================

    @Test
    public void testChineseCharactersWithUtf8() throws IOException {
        // 写入中文
        Properties props = new Properties();
        props.setProperty("name", "你好世界");

        File tempDir = createTempDir();
        File file = new File(tempDir, "chinese.properties");
        try {
            PropertiesUtils.store(props, file, "中文注释", StandardCharsets.UTF_8);

            // 读取回来验证
            Properties loaded = PropertiesUtils.load(file, StandardCharsets.UTF_8);
            Assert.assertEquals("你好世界", loaded.getProperty("name"));
        } finally {
            deleteTempFile(file);
        }
    }

    // ==================== 边界场景 ====================

    @Test
    public void testLoadEmptyFile() throws IOException {
        File file = createTempPropertiesFile("", StandardCharsets.UTF_8);
        try {
            Properties props = PropertiesUtils.load(file);
            Assert.assertNotNull(props);
            Assert.assertTrue(props.isEmpty());
        } finally {
            deleteTempFile(file);
        }
    }

    @Test
    public void testStoreEmptyProperties() throws IOException {
        File tempDir = createTempDir();
        File file = new File(tempDir, "empty.properties");
        try {
            PropertiesUtils.store(new Properties(), file, null);
            Assert.assertTrue(file.exists());
            Properties loaded = PropertiesUtils.load(file);
            Assert.assertTrue(loaded.isEmpty());
        } finally {
            deleteTempFile(file);
        }
    }

    @Test
    public void testLoadIso88591File() throws IOException {
        // ISO 8859-1 编码文件（不含中文字符）
        String content = "key1=value1\nkey2=value2";
        File file = createTempPropertiesFile(content, StandardCharsets.ISO_8859_1);
        try {
            // 使用 ISO 8859-1 编码读取
            Properties props = PropertiesUtils.load(file, StandardCharsets.ISO_8859_1);
            Assert.assertEquals("value1", props.getProperty("key1"));
            Assert.assertEquals("value2", props.getProperty("key2"));

            // 使用 UTF-8 编码读取也应当正确（ASCII 字符两种编码一致）
            Properties propsUtf8 = PropertiesUtils.load(file, StandardCharsets.UTF_8);
            Assert.assertEquals("value1", propsUtf8.getProperty("key1"));
        } finally {
            deleteTempFile(file);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadFileNullCharset() {
        PropertiesUtils.load(new File("dummy"), null);
    }

    @Test
    public void testStoreWithCharset() throws IOException {
        Properties props = new Properties();
        props.setProperty("key", "value");

        File tempDir = createTempDir();
        File file = new File(tempDir, "charset_test.properties");
        try {
            PropertiesUtils.store(props, file, null, StandardCharsets.ISO_8859_1);
            Assert.assertTrue(file.exists());

            Properties loaded = PropertiesUtils.load(file, StandardCharsets.ISO_8859_1);
            Assert.assertEquals("value", loaded.getProperty("key"));
        } finally {
            deleteTempFile(file);
        }
    }

    @Test
    public void testAppendWithCharset() throws IOException {
        File file = createTempPropertiesFile("key1=original", StandardCharsets.UTF_8);
        try {
            Properties toAppend = new Properties();
            toAppend.setProperty("key2", "added");

            PropertiesUtils.append(file, toAppend, StandardCharsets.UTF_8);

            Properties result = PropertiesUtils.load(file, StandardCharsets.UTF_8);
            Assert.assertEquals("original", result.getProperty("key1"));
            Assert.assertEquals("added", result.getProperty("key2"));
        } finally {
            deleteTempFile(file);
        }
    }

    @Test
    public void testGetIntWithInvalidFormatReturnsDefault() {
        Properties props = new Properties();
        props.setProperty("bad", "12.5");
        Assert.assertEquals(0, PropertiesUtils.getInt(props, "bad", 0));
    }

    @Test
    public void testGetBooleanWithWeirdValues() {
        Properties props = new Properties();
        props.setProperty("weird", "maybe");
        props.setProperty("empty", "");

        Assert.assertFalse(PropertiesUtils.getBoolean(props, "weird", false));
        Assert.assertFalse(PropertiesUtils.getBoolean(props, "empty", false));
        Assert.assertTrue(PropertiesUtils.getBoolean(props, "weird", true));
    }
}
