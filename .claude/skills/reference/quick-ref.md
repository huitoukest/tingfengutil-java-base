# 快速索引

## 方法 -> 文件映射

| 类别 | 工具类 | 文档位置 |
|------|--------|----------|
| **流处理** | | |
| InputStream → byte[] | `IOUtils.toByteArray()` | `modules/io-utils/signatures.md` |
| InputStream → String | `IOUtils.toString()` | `modules/io-utils/signatures.md` |
| 异步拷贝 | `IOUtils.copyAsync()` | `modules/io-utils/signatures.md` |
| 背压读取 | `IOUtils.readStreamWithBackpressure()` | `modules/io-utils/signatures.md` |
| **文件操作** | | |
| 读文件 | `FileUtils.readFileToByteArray()` | `modules/file-utils/signatures.md` |
| 写文件 | `FileUtils.writeStringToFile()` | `modules/file-utils/signatures.md` |
| 写行 | `FileUtils.writeLine()` | `modules/file-utils/signatures.md` |
| 异步读文件 | `FileUtils.readFileAsync()` | `modules/file-utils/signatures.md` |
| 异步拷贝 | `FileUtils.copyFileAsync()` | `modules/file-utils/signatures.md` |
| **进程管理** | | |
| 执行命令 | `ProcessUtils.execute()` | `modules/process-utils/signatures.md` |
| 异步执行 | `ProcessUtils.executeAsync()` | `modules/process-utils/signatures.md` |
| 流式回调 | `ProcessUtils.executeWithCallback()` | `modules/process-utils/signatures.md` |
| 进程管理 | `ProcessUtils.destroy()` | `modules/process-utils/signatures.md` |
| **通用工具** | | |
| 字符串 | `StringUtils` | `common/utils/string/` |
| 日期 | `DateUtils` | `common/utils/datetime/` |
| 集合 | `CollectionUtils` | `common/utils/` |
| 数组 | `ArrayUtils` | `common/utils/` |
| 数学 | `MathUtils` | `common/utils/` |
| 反射 | `ReflectUtils` | `common/utils/reflect/` |

## 规范速查

| 需求 | 查看文档 |
|------|----------|
| 怎么命名类/方法？ | `fundamentals/coding.md` |
| 用项目工具还是系统API？ | `fundamentals/performance.md` |
| 怎么写单元测试？ | `fundamentals/testing.md` |
| 工具类设计原则？ | `architecture/principles.md` |
| 包怎么组织？ | `architecture/package-structure.md` |
| IOUtils 怎么用？ | `modules/io-utils/design.md` |
| FileUtils 怎么用？ | `modules/file-utils/design.md` |
| ProcessUtils 怎么用？ | `modules/process-utils/design.md` |

## 常用常量

| 常量 | 值 | 用于 |
|------|-----|------|
| `BUFFER_SIZE` | 4096 | IOUtils/FileUtils |
| `DEFAULT_BACK_PRESSURE_BUFFER_SIZE` | 8MB | 异步操作背压限制 |
| `DEFAULT_LINE_SEPARATOR` | "\n" | 行写入 |
