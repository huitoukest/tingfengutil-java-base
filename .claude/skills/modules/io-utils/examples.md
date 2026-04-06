# IOUtils 使用示例

## 基本流转换

```java
// InputStream -> byte[]
try (InputStream is = new FileInputStream("data.txt")) {
    byte[] data = IOUtils.toByteArray(is);
}

// InputStream -> String
try (InputStream is = new FileInputStream("data.txt")) {
    String content = IOUtils.toString(is, StandardCharsets.UTF_8);
}

// String -> InputStream
InputStream is = IOUtils.toInputStream("Hello World");

// byte[] -> InputStream
InputStream is = IOUtils.toInputStream(new byte[]{1, 2, 3});
```

## 流拷贝

```java
// 基本拷贝
try (InputStream in = new FileInputStream("source.txt");
     OutputStream out = new FileOutputStream("dest.txt")) {
    IOUtils.copy(out, in);
}

// 带进度回调
try (InputStream in = new FileInputStream("source.txt");
     OutputStream out = new FileOutputStream("dest.txt")) {
    IOUtils.copy(out, in, (written, total) -> {
        System.out.printf("Progress: %d/%d%n", written, total);
    });
}
```

## 异步拷贝

```java
ExecutorService executor = Executors.newSingleThreadExecutor();
try {
    IOUtils.CancellationToken token = new IOUtils.CancellationToken();

    CompletableFuture<Long> future = IOUtils.copyAsync(
        outputStream, inputStream,
        executor,
        (written, total) -> true,  // 进度回调，返回true继续
        8 * 1024 * 1024,           // 8MB 背压限制
        token
    );

    Long bytesWritten = future.get(30, TimeUnit.SECONDS);
} finally {
    executor.shutdown();
}
```

## 取消操作

```java
IOUtils.CancellationToken token = new IOUtils.CancellationToken(5000); // 5秒超时

CompletableFuture<Long> future = IOUtils.copyAsync(
    outputStream, inputStream, executor, null, 8 * 1024 * 1024, token);

// 取消执行
token.cancel();

// 或等待超时自动取消
```

## 背压机制

```java
// 使用默认8MB背压
IOUtils.copyAsync(out, in, executor, callback,
    IOUtils.DEFAULT_BACK_PRESSURE_BUFFER_SIZE, token);

// 自定义背压限制
IOUtils.copyAsync(out, in, executor, callback,
    16 * 1024 * 1024,  // 16MB
    token);
```

## 线程池转换

```java
// ExecutorService 直接使用
ExecutorService executor = Executors.newFixedThreadPool(4);
IOUtils.copyAsync(out, in, executor, ...);

// Thread 对象自动启动
Thread thread = new Thread(() -> { ... });
IOUtils.toExecutorService(thread); // 自动 start()

// Runnable 对象自动包装
Runnable task = () -> { ... };
IOUtils.toExecutorService(task);
```
