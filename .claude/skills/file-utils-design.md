# FileUtils 设计规范

## 职责定位

FileUtils 是 **文件直接操作工具类**，所有对文件的读写、拷贝、删除、创建等操作都放在此类。

## 核心原则

1. **文件直接操作** - 文件的 CRUD 操作
2. **底层调用 IOUtils** - 流操作委托给 IOUtils
3. **统一运行时异常** - 抛出项目自定义异常
4. **线程池外部注入** - 异步方法支持 ExecutorService / Thread / Runnable

## 方法分类

### 1. 文件读写（简洁封装）

基于 IOUtils 的流操作，提供文件到字节数组/字符串的便捷转换。

| 方法 | 说明 |
|------|------|
| `readFileToByteArray(File)` | 读取文件为字节数组 |
| `readFileToString(File)` | 读取文件为 String (UTF-8) |
| `readFileToString(File, Charset)` | 读取文件为 String (指定编码) |
| `writeByteArrayToFile(File, byte[])` | 字节数组写入文件 |
| `writeStringToFile(File, String)` | String 写入文件 (UTF-8) |
| `writeStringToFile(File, String, Charset, boolean)` | String 写入文件（指定编码+追加模式） |

### 2. 行写入操作

追加模式的行写入，自动处理换行符。

| 方法 | 说明 |
|------|------|
| `writeLine(File, String)` | 追加一行（UTF-8，自动换行） |
| `writeLine(File, String, Charset)` | 追加一行（指定编码） |
| `writeLine(File, String, Charset, boolean)` | 写入一行（追加/覆盖） |
| `writeLines(File, List<String>)` | 追加多行（UTF-8） |
| `writeLines(File, List<String>, Charset, boolean)` | 追加多行（指定编码+模式） |

**设计要点：**
- 自动添加 `\n` 换行符
- 使用 BufferedWriter 缓存
- 关闭时自动 flush

### 3. 异步文件读写

支持线程池注入、背压控制、取消令牌。

| 方法 | 说明 |
|------|------|
| `readFileAsync(File, Object executor, BiConsumer, CancellationToken)` | 异步读取文件 |
| `writeFileAsync(File, byte[], Object, BiConsumer, CancellationToken)` | 异步写入字节数组 |
| `writeFileAsync(File, String, Charset, boolean, Object, CancellationToken)` | 异步写入字符串 |
| `writeLineAsync(File, String, Charset, boolean, Object, CancellationToken)` | 异步追加一行 |
| `copyFileAsync(File dest, File src, Object, Consumer, int, CancellationToken)` | 异步文件拷贝 |

### 4. 现有方法（已实现）

来自原始 FileUtils 的功能，保持不变：

| 方法 | 说明 |
|------|------|
| `uploadFile(...)` | 文件上传到 HTTP URL |
| `downFile(...)` | 从 URL 下载文件 |
| `deleteFile(path)` | 删除文件 |
| `deleteFolder(File, boolean, boolean)` | 删除文件夹 |
| `createFolder(path)` | 创建文件夹 |
| `createFile(path)` | 创建文件 |
| `copyFile(srcPath, destPath)` | 文件拷贝（channel 方式） |
| `copyFileByFileChannel(...)` | 带进度的文件拷贝 |
| `copyDirectory(...)` | 拷贝整个目录 |
| `writeFile(File, OutputStream, callback)` | 用输出流写出文件 |
| `getAllFilesByAFolders(...)` | 递归获取目录下所有文件 |
| `getFileNoExtensionName(path)` | 获取无扩展名的文件名 |
| `getFileExtension(path)` | 获取文件扩展名 |
| `getFileNameByPath(path)` | 通过路径获取文件名 |
| `transFileToString(File, Base64ConvertToStringI)` | 文件转 Base64 字符串 |

## 常量定义

| 常量 | 值 | 说明 |
|------|-----|------|
| `BUFFER_SIZE` | 4096 | 缓存字节数 |
| `BUFFER_SIZE_MIN` | 4096 | 分片/进度的最小缓存 |
| `DEFAULT_BACK_PRESSURE_BUFFER_SIZE` | 8MB | 默认背压缓冲区 |
| `DEFAULT_LINE_SEPARATOR` | "\n" | 默认行分隔符 |

## CancellationToken 复用

FileUtils 的异步方法使用 IOUtils.CancellationToken：

```java
// FileUtils 中引用
import com.tingfeng.util.java.base.common.utils.IOUtils;

public static CompletableFuture<byte[]> readFileAsync(
    File file,
    Object executor,
    BiConsumer<Long, Long> readCallback,
    IOUtils.CancellationToken token) {
    // 实现
}
```

## 实现要点

### readFileToByteArray
```java
public static byte[] readFileToByteArray(File file) {
    try (InputStream is = new FileInputStream(file)) {
        return IOUtils.toByteArray(is);
    } catch (IOException e) {
        throw new IOException(e);
    }
}
```

### writeLine 实现
```java
public static void writeLine(File file, String line, Charset charset, boolean append) {
    try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(file, append), charset))) {
        writer.write(line);
        writer.newLine();  // 或 writer.write("\n")
        writer.flush();
    } catch (IOException e) {
        throw new IOException(e);
    }
}
```

### writeLines 高效实现（使用 JDK NIO）
```java
public static void writeLines(File file, List<String> lines, Charset charset, boolean append) {
    try {
        OpenOption[] options = append
            ? new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND}
            : new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
        Files.write(file.toPath(), lines, charset, options);
    } catch (IOException e) {
        throw new IOException(e);
    }
}
```

### copyFileAsync
```java
public static CompletableFuture<Long> copyFileAsync(
        File dest, File src,
        Object executor,
        Consumer<Long> progressCallback,
        int backPressureLimit,
        IOUtils.CancellationToken token) {
    return CompletableFuture.supplyAsync(() -> {
        long total = 0;
        try (FileInputStream fis = new FileInputStream(src);
             FileOutputStream fos = new FileOutputStream(dest);
             FileChannel in = fis.getChannel();
             FileChannel out = fos.getChannel()) {
            // 使用 transferTo 高效拷贝
            // 每块拷贝后检查 token.shouldInterrupt()
            // 调用 progressCallback.accept(total)
        }
        return total;
    }, IOUtils.toExecutorService(executor));
}
```

## 注意事项

1. **文件操作前检查存在性** - File.exists() / File.canWrite()
2. **父目录不存在时创建** - 使用 mkdirs()
3. **覆盖前备份** - 重要文件操作建议先备份
4. **大文件分块处理** - 避免 OOM
5. **close 顺序** - 先关闭输入流，再关闭输出流
6. **finally 中关闭** - 确保资源释放
7. **DEFAULT_LINE_SEPARATOR 必须是 public static final** - 常量定义规范
