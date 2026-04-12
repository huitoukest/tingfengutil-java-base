# ExceptionUtils 设计原则

## 设计目标

提供异常信息的获取、异常链处理、栈追踪转换等功能。

## 核心能力

1. **消息获取** - 遍历异常链获取有效消息
2. **根因查找** - 获取异常链末端
3. **类型匹配** - 在异常链中查找指定类型
4. **栈追踪转换** - 异常堆栈转为字符串
5. **解包处理** - 处理 InvocationTargetException, ExecutionException 等包装异常

## 方法分类

| 类别 | 方法 | 说明 |
|------|------|------|
| 消息 | `getMessage()` | 遍历异常链返回第一个有效消息 |
| 根因 | `getRootCause()` | 获取最底层异常 |
| 查找 | `getThrowable()` | 在异常链中查找指定类型 |
| 链 | `getCauseChain()` | 获取完整异常链列表 |
| 判断 | `isCauseOf()` | 检查是否包含某类型异常 |
| 栈 | `getStackTraceAsString()` | 完整栈追踪字符串 |
| 栈 | `getSimpleStackTrace()` | 简化栈追踪（仅类名方法名） |
| 解包 | `unwrap()` | 解包 InvocationTargetException 等 |
| 类型 | `isCheckedException()` | 判断是否检查型异常 |
| 类型 | `isUncheckedException()` | 判断是否非检查型异常 |
| 压制 | `getSuppressed()` | 获取被压制的异常列表 |
