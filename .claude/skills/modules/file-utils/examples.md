# FileUtils 使用示例

## 基本文件读写

```java
// 读取文件为字节数组
byte[] data = FileUtils.readFileToByteArray(new File("data.bin"));

// 读取文件为字符串
String content = FileUtils.readFileToString(new File("data.txt"));
String contentWithCharset = FileUtils.readFileToString(new File("data.txt"), StandardCharsets.UTF_8);

// 写入字节数组
FileUtils.writeByteArrayToFile(new File("data.bin"), data);

// 写入字符串
FileUtils.writeStringToFile(new File("data.txt"), "Hello World");
```

## 行写入

```java
// 追加单行（自动换行）
FileUtils.writeLine(new File("log.txt"), "2024-01-01 INFO: Started");

// 指定编码追加
FileUtils.writeLine(new File("log.txt"), "Error occurred", StandardCharsets.UTF_8);

// 覆盖模式
FileUtils.writeLine(new File("log.txt"), "New content", StandardCharsets.UTF_8, false);

// 追加多行
List<String> lines = Arrays.asList("Line 1", "Line 2", "Line 3");
FileUtils.writeLines(new File("log.txt"), lines);

// 指定编码+追加模式
FileUtils.writeLines(new File("log.txt"), lines, StandardCharsets.UTF_8, true);
```

## 异步文件操作

```java
ExecutorService executor = Executors.newSingleThreadExecutor();
try {
    IOUtils.CancellationToken token = new IOUtils.CancellationToken();

    // 异步读取
    CompletableFuture<byte[]> readFuture = FileUtils.readFileAsync(
        new File("largefile.bin"),
        executor,
        (read, total) -> {
            System.out.printf("Read: %d/%d%n", read, total);
            return true;  // 返回true继续读取
        },
        token
    );

    byte[] data = readFuture.get(60, TimeUnit.SECONDS);

    // 异步写入
    CompletableFuture<Boolean> writeFuture = FileUtils.writeFileAsync(
        new File("output.bin"),
        data,
        executor,
        (written, total) -> {
            System.out.printf("Written: %d/%d%n", written, total);
        },
        token
    );

    writeFuture.get(60, TimeUnit.SECONDS);

} finally {
    executor.shutdown();
}
```

## 带取消的异步拷贝

```java
ExecutorService executor = Executors.newSingleThreadExecutor();
File srcFile = new File("source.bin");
File destFile = new File("dest.bin");

try {
    IOUtils.CancellationToken token = new IOUtils.CancellationToken(30000); // 30秒超时

    CompletableFuture<Long> copyFuture = FileUtils.copyFileAsync(
        destFile,
        srcFile,
        executor,
        total -> {
            System.out.printf("Progress: %d bytes%n", total);
            return true;
        },
        FileUtils.DEFAULT_BACK_PRESSURE_BUFFER_SIZE,
        token
    );

    Long bytesCopied = copyFuture.get();

    // 取消执行
    // token.cancel();

} finally {
    executor.shutdown();
}
```
