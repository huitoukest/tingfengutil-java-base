# TreeUtils / TreeHelper 使用示例

## TreeUtils 遍历示例

```java
// 深度优先遍历
List<Node> roots = ...;
List<Node> allNodes = TreeUtils.depthFirst(roots, Node::getChildren);

// 广度优先遍历
List<Node> levelOrder = TreeUtils.breadthFirst(roots, Node::getChildren);

// 遍历并收集结果
List<String> names = TreeUtils.traverseAndCollect(roots,
    Node::getChildren,
    (node, level) -> node.getName() + "(" + level + ")");

// 遍历直到找到目标
Optional<Node> found = TreeUtils.traverseUntil(roots,
    Node::getChildren,
    node -> node.getId().equals(targetId));
```

## TreeHelper 查询示例

```java
// 构建 Helper
TreeHelper<Node, String> helper = TreeHelper.of(nodes,
    Node::getId,
    Node::getParentId);

// 查询
Optional<Node> node = helper.findById("id123");
List<Node> children = helper.getChildren(node);
List<Node> path = helper.getPathToRoot(node);
int level = helper.getLevel(node);
```

## TreeHelper 变换示例

```java
// 过滤节点（被过滤节点的子节点提升到父节点）
TreeHelper<Node, String> filtered = helper.filter(
    node -> !node.isDeleted(),
    (removed, children) -> children  // 子节点保留
);

// 添加节点后重建
TreeHelper<Node, String> merged = original.add(newHelper);
```

##向后兼容方法

```java
// 使用 FunctionVTwo, FunctionRTwo, FunctionROne
List<Node> tree = TreeUtils.getTreeList(nodeList,
    (child, parent) -> parent.getChildren().add(child),      // FunctionVTwo
    (child, parent) -> parent.getId().equals(child.getParentId()), // FunctionRTwo
    Node::getId);  // FunctionROne

// 使用 Comparator
List<Node> sortedTree = TreeUtils.getTreeList(nodeList,
    Comparator.comparingInt(Node::getOrder));

// 展平树
List<Node> flat = TreeUtils.flatList(tree,
    Node::getChildren,
    Function.identity());
```
