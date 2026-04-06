# TreeUtils / TreeHelper 方法签名

## TreeUtils

### 位置

`com.tingfeng.util.java.base.common.utils.TreeUtils`

### 遍历方法

```java
// 基础遍历
public static <T> void traverse(List<T> treeList, TraversalPolicy traversalPolicy,
                                 Function<T, List<T>> childrenGetter,
                                 Predicate<TreeTraverseContext<T>> traverseF)

// 遍历并收集结果
public static <T, R> List<R> traverseAndCollect(List<T> roots,
                                                  Function<T, List<T>> childrenGetter,
                                                  BiFunction<T, Integer, R> collector)

// 遍历直到找到目标
public static <T> Optional<T> traverseUntil(List<T> roots,
                                             Function<T, List<T>> childrenGetter,
                                             Predicate<T> stopPredicate)

// 深度优先
public static <T> List<T> depthFirst(List<T> roots, Function<T, List<T>> childrenGetter)

// 广度优先
public static <T> List<T> breadthFirst(List<T> roots, Function<T, List<T>> childrenGetter)
```

### 工厂方法

```java
public static <T, ID> TreeHelper<T, ID> helper(List<T> nodes,
                                                 Function<T, ID> idGetter,
                                                 Function<T, ID> parentIdGetter)
```

## TreeHelper

### 位置

`com.tingfeng.util.java.base.common.helper.TreeHelper`

### 工厂方法

```java
public static <T, ID> TreeHelper<T, ID> of(List<T> nodes,
                                             Function<T, ID> idGetter,
                                             Function<T, ID> parentIdGetter)
```

### 查询操作

```java
public Optional<T> findById(ID id)
public Optional<T> findOne(Predicate<T> predicate)
public List<T> findAll(Predicate<T> predicate)
public List<T> getRoots()
public List<T> getChildren(T node)
public Optional<T> getParent(T node)
public List<T> getLeaves()
public List<T> getPathToRoot(T node)
public int getLevel(T node)
public int getDepth()
```

### 变换操作

```java
public TreeHelper<T, ID> map(Function<T, T> nodeMapper,
                             Function<List<T>, List<T>> childrenMapper)
public TreeHelper<T, ID> filter(Predicate<T> predicate,
                                 BiFunction<T, List<T>, List<T>> childHandler)
```

### 集合运算

```java
public TreeHelper<T, ID> merge(TreeHelper<T, ID> other,
                                BiPredicate<T, T> isSameNode,
                                BinaryOperator<T> mergeNode,
                                BiFunction<T, T, T> conflictHandler)
public TreeHelper<T, ID> subtract(TreeHelper<T, ID> other, BiPredicate<T, T> isSameNode)
public TreeHelper<T, ID> add(TreeHelper<T, ID> other)
public TreeHelper<T, ID> intersect(TreeHelper<T, ID> other,
                                   BiPredicate<T, T> isSameNode,
                                   BinaryOperator<T> mergeNode)
```
