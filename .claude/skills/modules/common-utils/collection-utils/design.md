# CollectionUtils 设计原则

## 设计目标

提供集合操作的工具方法，保持 JDK 集合框架的补充而非替代。

## 核心原则

1. **最小依赖** - 仅依赖 JDK
2. **null 安全** - 方法需处理 null 输入
3. **不可变优先** - 返回新集合而非修改原集合
4. **性能高效** - 避免不必要的拷贝

## 函数接口复用

| 操作 | 推荐接口 | 说明 |
|------|----------|------|
| 单参数转换 | `FunctionROne<R, P1>` | 一进一出 |
| 双参数转换 | `FunctionRTwo<R, P1, P2>` | 二进一出 |
| 无返回值操作 | `FunctionVTwo<P1, P2>` | 二进零出 |
| 多参数 Consumer | `ConsumerThree`~`ConsumerTen` | 多参数消费 |

## 方法命名规范

| 操作 | 命名 | 示例 |
|------|------|------|
| 转换 | `toXxx()` / `mapXxx()` | `toList()`, `mapList()` |
| 过滤 | `filterXxx()` | `filterList()` |
| 判断 | `isXxx()` / `hasXxx()` | `isEmpty()`, `hasNull()` |
| 获取 | `getXxx()` / `findXxx()` | `getFirst()`, `findOne()` |