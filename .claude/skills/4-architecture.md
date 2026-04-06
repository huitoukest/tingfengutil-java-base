# 架构原则

## 核心约束

1. **最小依赖** - 禁止引入JavaEE/第三方库
2. **统一异常** - 抛出自定义RuntimeException
3. **职责分离** - IOUtils管流，FileUtils管文件
4. **线程池外部注入** - 异步方法支持ExecutorService/Thread/Runnable

## 包结构

```
src/main/java/com/tingfeng/util/java/base/
├── common/
│   ├── utils/        # 静态工具类（Utils后缀）
│   ├── bean/         # 数据结构（TreeNode, Tuple）
│   ├── exception/    # 自定义异常
│   ├── helper/       # 实例类（Helper后缀）
│   ├── constant/     # 常量枚举
│   ├── inter/        # 函数接口（I后缀）
│   └── annotation/   # 注解
├── file/             # 文件工具
├── database/         # 数据库工具
└── web/             # Web工具
```

## 命名规范

| 类型 | 规则 | 示例 |
|------|------|------|
| 工具类 | Utils/Util后缀 | `StringUtils`, `DateUtils` |
| 实例类 | Helper后缀 | `PoolHelper`, `PropertyHelper` |
| 接口 | I后缀 | `ConvertI`, `PoolMemberActionI` |
| 转换方法 | `toB()` / `getAbyB()` | `IOUtils.toByteArray()` |

## 工具类设计

```java
public final class XxxUtils {  // final防止继承

    private static final int BUFFER_SIZE = 4096;

    private XxxUtils() {  // 私有构造器，禁止实例化
    }

    public static Result doSomething(Input input) {
        // 静态工具方法
    }
}
```

## 实例类设计

```java
public class XxxHelper {  // 非final，允许继承

    private final ExecutorService executor;

    public XxxHelper(ExecutorService executor) {
        this.executor = executor;  // 外部注入
    }

    public void doSomething() {
        // 实例方法，可持有状态
    }
}
```

## 异步设计

### 线程池统一
```java
static ExecutorService toExecutorService(Object executor) {
    if (executor instanceof ExecutorService) return (ExecutorService) executor;
    else if (executor instanceof Thread) { /* start and wrap */ }
    else if (executor instanceof Runnable) { /* wrap */ }
    throw new IllegalArgumentException("Unsupported executor type");
}
```

### 背压机制
```
Semaphore(permits = backPressureLimit / bufferSize)
读取 → 缓冲区 → 写入 → release()
                    ↑
              acquire() 阻塞
```

### 取消令牌
```java
class CancellationToken {
    volatile boolean cancelled
    long timeoutMillis  // 截止时间点，不是时长

    boolean shouldInterrupt() { return cancelled || isTimeout(); }
}
```

## 异常设计

```java
// 自定义异常
public class IOException extends RuntimeException {
    public IOException() {}
    public IOException(String message) { super(message); }
    public IOException(Throwable cause) { super(cause); }
    public IOException(String message, Throwable cause) { super(message, cause); }
}

// 使用
throw new com.tingfeng.util.java.base.common.exception.io.IOException("message", e);
```

## 工具方法位置索引

| 类别 | 工具类 | 位置 |
|------|--------|------|
| 字符串 | `StringUtils` | `common/utils/string/` |
| 日期 | `DateUtils`, `LocalDateUtils` | `common/utils/datetime/` |
| 集合 | `CollectionUtils` | `common/utils/` |
| 数组 | `ArrayUtils` | `common/utils/` |
| 数学 | `MathUtils` | `common/utils/` |
| 反射 | `ReflectUtils`, `ClassUtils` | `common/utils/reflect/` |
| 文件 | `FileUtils` | `file/` |
| 树结构 | `TreeNode`, `GenericTreeNode` | `common/bean/` |
| 池化 | `PoolHelper` | `common/helper/` |
