# TreeUtils / TreeHelper 设计原则

## 架构设计

### 双组件模式

```
TreeUtils     <- 简单静态方法（无状态）
TreeHelper    <- 复杂操作助手（不可变设计）
```

### TreeUtils 职责

- 遍历方法（traverse, traverseAndCollect, traverseUntil）
- 简单转换（depthFirst, breadthFirst）
- 简单统计（count, depth）
- 工厂方法（helper）

### TreeHelper 职责

- 索引构建（nodeIndex, parentIndex, childrenIndex）
- 查询操作（findById, findOne, findAll, getChildren, getParent）
- 变换操作（map, filter）
- 集合运算（merge, subtract, add, intersect）

## 不可变设计

```java
// TreeHelper 所有字段都是 final
private final List<T> allNodes;
private final Map<ID, T> nodeIndex;
private final Function<T, T> copier;

// 变换操作返回新实例
public TreeHelper<T, ID> map(...) { ... return new TreeHelper<>(...); }
```

## 函数接口选用

| 场景 | 接口 | 签名 |
|------|------|------|
| 子节点获取 | `Function<T, List<T>>` | `childrenGetter.apply(node)` |
| 节点匹配 | `BiPredicate<T, T>` | `isSameNode.test(node1, node2)` |
| 节点变换 | `Function<T, T>` | `nodeMapper.apply(node)` |
| 子节点处理 | `BiFunction<T, List<T>, List<T>>` | `childHandler.apply(parent, children)` |

## 设计约束

1. **O(1) 查询** - 通过索引实现
2. **线程安全** - 不可变 + 无共享可变状态
3. **用户控制拷贝** - copier 函数由用户提供
