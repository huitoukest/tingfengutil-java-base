# FileUtils 设计原则

## 职责定位

FileUtils 是 **文件直接操作工具类**，所有对文件的读写、拷贝、删除、创建等操作都放在此类。

## 核心原则

1. **文件直接操作** - 文件的 CRUD 操作
2. **底层调用 IOUtils** - 流操作委托给 IOUtils
3. **统一运行时异常** - 抛出项目自定义异常
4. **线程池外部注入** - 异步方法支持 ExecutorService / Thread / Runnable

## CancellationToken 复用

FileUtils 的异步方法使用 IOUtils.CancellationToken：

```java
import com.tingfeng.util.java.base.common.utils.IOUtils;

public static CompletableFuture<byte[]> readFileAsync(
    File file,
    Object executor,
    BiConsumer<Long, Long> readCallback,
    IOUtils.CancellationToken token) {
    // 实现
}
```

## 常量定义

| 常量 | 值 | 说明 |
|------|-----|------|
| `BUFFER_SIZE` | 4096 | 缓存字节数 |
| `BUFFER_SIZE_MIN` | 4096 | 分片/进度的最小缓存 |
| `DEFAULT_BACK_PRESSURE_BUFFER_SIZE` | 8MB | 默认背压缓冲区 |
| `DEFAULT_LINE_SEPARATOR` | "\n" | 默认行分隔符 |

## 注意事项

1. **文件操作前检查存在性** - File.exists() / File.canWrite()
2. **父目录不存在时创建** - 使用 mkdirs()
3. **覆盖前备份** - 重要文件操作建议先备份
4. **大文件分块处理** - 避免 OOM
5. **close 顺序** - 先关闭输入流，再关闭输出流
6. **finally 中关闭** - 确保资源释放
