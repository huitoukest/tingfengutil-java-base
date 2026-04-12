# 测试模式

## 异步测试规范

### 异步方法测试

```java
@Test
public void testAsyncMethod_completesSuccessfully() throws Exception {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
        IOUtils.CancellationToken token = new IOUtils.CancellationToken();

        CompletableFuture<Type> future = ClassName.asyncMethod(
            param1, param2, executor, callback, token);

        Type result = future.get(5, TimeUnit.SECONDS);
        Assert.assertNotNull(result);
    } finally {
        executor.shutdown();
    }
}
```

### 取消/超时测试

```java
@Test
public void testAsyncMethod_withCancellation_stopsEarly() throws Exception {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
        IOUtils.CancellationToken token = new IOUtils.CancellationToken();
        token.cancel(); // 立即取消

        CompletableFuture<Type> future = ClassName.asyncMethod(
            largeData, executor, callback, token);

        Type result = future.get();
        // 取消后结果应该小于原始大小
        Assert.assertTrue(result < originalSize);
    } finally {
        executor.shutdown();
    }
}
```

## 常见错误

### 1. 异步方法返回void但调用.get()

```java
// 错误 - readStreamWithBackpressure 返回 void
IOUtils.readStreamWithBackpressure(...).get();

// 正确 - 使用 Thread.sleep 等待异步完成
IOUtils.readStreamWithBackpressure(...);
Thread.sleep(500);
```

### 2. Lambda 返回类型不匹配

```java
// 错误 - BiConsumer 不返回 Boolean
callback -> { progress.set(1); return true; }  // 编译错误

// 正确 - 使用正确的函数式接口
Function<T, Boolean> callback = t -> { return true; };
Consumer<T> callback = t -> { /* do something */ };
```

### 3. 忽略受检异常

```java
// 错误 - UnsupportedEncodingException 在 Java 8 中必须处理
Assert.assertEquals("text", os.toString("UTF-8"));

// 正确 - 添加 throws 声明
public void testMethod() throws Exception {
    Assert.assertEquals("text", os.toString("UTF-8"));
}
```

## 性能注意事项

1. **避免在测试中创建过大数据** - 1MB 通常足够测试大文件场景
2. **异步测试增加超时** - 使用足够长的超时避免CI环境不稳定
3. **独立测试原则** - 测试间不应有依赖

## 示例

```java
// 正常输入
@Test
public void testToByteArray_normalInput_success() {
    InputStream is = IOUtils.toInputStream("Hello");
    byte[] result = IOUtils.toByteArray(is);
    Assert.assertNotNull(result);
}

// 边界值 - 空输入
@Test
public void testToByteArray_nullInput_returnsEmptyArray() {
    byte[] result = IOUtils.toByteArray(null);
    Assert.assertNotNull(result);
    Assert.assertEquals(0, result.length);
}

// 异常情况
@Test(expected = FileNotFoundException.class)
public void testReadFile_notFound_throwsException() {
    FileUtils.readFileToByteArray(new File("non_existent.txt"));
}
```
