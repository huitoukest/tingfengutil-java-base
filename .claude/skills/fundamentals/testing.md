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
| 异常处理 | `test方法名_非法输入_抛出异常` | `testReadFile_notFound_throwsException` |
| 空值处理 | `test方法名_null输入_预期行为` | `testCopy_nullInput_handledGracefully` |

## 测试覆盖要求

### 必须覆盖的场景

1. **正常输入** - 有效数据的标准流程
2. **边界值** - 0、空字符串、最大值、最小值
3. **null值** - 参数为空时的行为
4. **异常情况** - 抛出预期的异常
5. **资源清理** - finally块中的清理逻辑

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

## 运行测试

```bash
# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=ClassNameTest

# 运行特定测试方法
mvn test -Dtest=ClassNameTest#testMethod
```
