# 单元测试规范

## 核心原则

1. **每个公开方法都需要测试** - 公共API必须有对应的测试用例
2. **AAA模式** - Arrange（准备）、Act（执行）、Assert（断言）
3. **测试单一职责** - 每个测试只验证一个行为
4. **测试命名规范** - `test方法名_测试场景_预期结果`

## 测试结构

```java
@Test
public void testMethodName_scenario_expectedResult() {
    // Arrange - 准备测试数据和对象
    InputType input = createTestInput();

    // Act - 执行被测试的方法
    OutputType result = ClassName.methodName(input);

    // Assert - 验证结果
    Assert.assertEquals(expected, result);
}
```

## 命名规范

| 测试类型 | 命名模式 | 示例 |
|---------|---------|------|
| 正常流程 | `test方法名_正常输入_预期结果` | `testToString_normalInput_success` |
| 边界条件 | `test方法名_边界值_预期结果` | `testDivide_zeroDenominator_throwsException` |
| 异常处理 | `test方法名_非法输入_抛出异常` | `testReadFile_notFound_throwsFileNotFoundException` |
| 空值处理 | `test方法名_null输入_预期行为` | `testCopy_nullInput_handledGracefully` |

## 测试覆盖要求

### 必须覆盖的场景

1. **正常输入** - 有效数据的标准流程
2. **边界值** - 0、空字符串、最大值、最小值
3. **null值** - 参数为空时的行为
4. **异常情况** - 抛出预期的异常
5. **资源清理** - finally块中的清理逻辑

### 示例

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

## 资源管理

### 使用 try-finally 确保清理

```java
@Test
public void testFileOperation_cleanupFinally() throws IOException {
    File tempFile = createTempFile();
    try {
        // 测试代码
        byte[] result = FileUtils.readFileToByteArray(tempFile);
        Assert.assertNotNull(result);
    } finally {
        deleteTempFile(tempFile);
    }
}
```

### 临时文件处理

```java
private File createTempFile(String content) throws IOException {
    File tempDir = Files.createTempDirectory("test").toFile();
    File tempFile = new File(tempDir, "test.txt");
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
```

## 断言使用规范

| 场景 | 推荐断言 |
|------|---------|
| 相等比较 | `Assert.assertEquals(expected, actual)` |
| 布尔条件 | `Assert.assertTrue(condition)` / `Assert.assertFalse(condition)` |
| 非空检查 | `Assert.assertNotNull(object)` |
| 异常验证 | `@Test(expected = Exception.class)` 或 `Assert.assertThrows()` |
| 数组相等 | `Assert.assertArrayEquals(expected, actual)` |

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

## 模板

```java
package com.tingfeng.util.java.base.[module];

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * [ClassName] 单元测试
 */
public class [ClassName]Test {

    private static final String TEST_CONTENT = "Test content";

    // ==================== 正常流程测试 ====================

    @Test
    public void testMethod_normalInput_expectedResult() throws Exception {
        // Arrange
        Input input = createInput();

        // Act
        Output result = ClassName.method(input);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(expected, result.getValue());
    }

    // ==================== 边界条件测试 ====================

    @Test
    public void testMethod_emptyInput_handledGracefully() throws Exception {
        // Arrange
        Input input = createEmptyInput();

        // Act
        Output result = ClassName.method(input);

        // Assert
        Assert.assertNull(result);
    }

    // ==================== 异常测试 ====================

    @Test(expected = SpecificException.class)
    public void testMethod_invalidInput_throwsException() {
        // Arrange
        Input input = createInvalidInput();

        // Act
        ClassName.method(input);
    }

    // ==================== 异步测试 ====================

    @Test
    public void testAsyncMethod_completesSuccessfully() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            CancellationToken token = new CancellationToken();

            CompletableFuture<Result> future = ClassName.asyncMethod(
                input, executor, callback, token);

            Result result = future.get(5, TimeUnit.SECONDS);
            Assert.assertNotNull(result);
        } finally {
            executor.shutdown();
        }
    }

    // ==================== 辅助方法 ====================

    private Input createInput() {
        return new Input();
    }

    private void cleanup(File file) {
        // 清理逻辑
    }
}
```

## 运行测试

```bash
# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=ClassNameTest

# 运行特定测试方法
mvn test -Dtest=ClassNameTest#testMethod

# 运行多个测试类
mvn test -Dtest=ClassNameTest1,ClassNameTest2
```
