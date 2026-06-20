# BeanUtils 深度拷贝优化方案

- 创建时间: 2026-06-01 10:40
- 最后更新: 2026-06-01 10:40
- 状态: 设计中

## 1. 整体目标

修正 BeanUtils.deepCopyBean 中 null 接口属性的语义处理逻辑，使其在深/浅拷贝后保持 null 而非自动创建实例；同时合并浅拷贝与深拷贝分支的重复代码，消除冗余。

## 2. 核心设计

### 2.1 影响范围

| 文件 | 用途 | 变更类型 |
|------|------|---------|
| `src/main/java/com/tingfeng/util/java/base/bean/BeanUtils.java` | 修改 deepCopyBean 方法 | read_write |
| `src/test/java/com/tingfeng/util/java/base/bean/BeanUtilsTest.java` | 删除冲突测试，添加新测试 | read_write |

### 2.2 变更概要

#### deepCopyBean 方法（lines 718-792）

**当前结构：**

```
deepCopyBean(T source, int remainingDepth, IdentityHashMap visited)
  ├── 创建 target 实例 + 放入 visited
  ├── 遍历属性
  │   ├── 跳过 class/transient/static
  │   ├── 获取属性值 value
  │   ├── if (remainingDepth <= 0):
  │   │   ├── (删除) null 检查 + CommonType 实例化  ← 不再创建实例
  │   │   └── desc.setPropertyValue(target, propName, value)
  │   └── if (remainingDepth > 0):
  │       ├── copiedValue = deepCopy(value, remainingDepth-1, visited)
  │       ├── (删除) null 检查 + CommonType 实例化  ← 不再创建实例
  │       └── desc.setPropertyValue(target, propName, copiedValue)
  └── return target
```

**优化后结构：**

```
deepCopyBean(T source, int remainingDepth, IdentityHashMap visited)
  ├── 创建 target 实例 + 放入 visited
  ├── 遍历属性
  │   ├── 跳过 class/transient/static
  │   ├── 获取属性值 value（null 保持 null，无实例化）
  │   ├── 合并分支:
  │   │   finalValue = (remainingDepth <= 0) ? value : deepCopy(value, ...)
  │   │   desc.setPropertyValue(target, propName, finalValue)
  │   └── ← 无 null→instance 逻辑
  └── return target
```

#### 核心变化点

1. **删除浅拷贝分支**的 null→instance 逻辑（原 lines 756-768）
2. **删除深拷贝分支**的 null→instance 逻辑（原 lines 774-787）
3. **合并两个分支**为条件表达式：
   - `remainingDepth <= 0`：直接使用 value（null 保持 null）
   - `remainingDepth > 0`：使用 `deepCopy(value, remainingDepth-1, visited)`（null 传入 deepCopy 返回 null）
4. 循环引用检测、transient/static 跳过、BeanDesc 获取等外围逻辑保持不变

### 2.3 变更后方法伪代码

```java
private static <T> T deepCopyBean(T source, int remainingDepth,
                                   IdentityHashMap<Object, Object> visited) {
    // 1. 创建目标实例（保持不变）
    T target = ...; // 反射创建实例

    // 2. 放入 visited（保持不变）
    visited.put(source, target);

    // 3. 遍历属性（保持不变）
    BeanDesc desc = BeanCopier.getOrCreateBeanDesc(clazz);
    for (String propName : desc.getPropertyNames()) {
        // 跳过 class/transient/static（保持不变）

        // 获取属性值（保持不变）
        Object value = ...; // 从 source 读取

        // [变更] 合并分支 + 移除 null→instance 逻辑
        Object finalValue = (remainingDepth <= 0)
                ? value                    // 浅拷贝：直接引用
                : deepCopy(value, remainingDepth - 1, visited); // 深拷贝：递归
        desc.setPropertyValue(target, propName, finalValue);
    }
    return target;
}
```

### 2.4 边界条件与异常处理

| 边界场景 | 策略 | 说明 |
|---------|------|------|
| null 源对象 | 已在 `deepCopy()` 入口处理（返回 null） | deepCopyBean 不会被 null 源调用 |
| null 属性值 | 保持 null，不做任何实例化 | `deepCopy(null, ...)` 返回 null |
| remainingDepth = 0 | 浅拷贝，所有值直接引用 | 避免递归，null 保持 null |
| remainingDepth < 0 | 已由 `deepCopy(T, int)` 校验（抛 IllegalArgumentException） | deepCopyBean 内部不会被调用 |
| 接口类型属性为 null | null 保持 null | 不再通过 CommonType 创建实例 |
| Collection/Map 属性为 null | null 保持 null | 原 deepCopyCollection/deepCopyMap 逻辑不变 |
| 循环引用 | visited 映射照常工作 | 辅助感知 |
| transient/static 属性 | 跳过，保持不变 | 辅助感知 |

### 2.5 测试变更

**删除的测试用例：**

| 测试方法 | 行号 | 删除原因 |
|---------|------|---------|
| `testDeepCopyCollectionNullToArrayList` | lines 1565-1584 | 验证 null→ArrayList 行为，与需求冲突 |
| `testDeepCopyMapNullToHashMap` | lines 1586-1608 | 验证 null→HashMap 行为，与需求冲突 |

**新增的测试用例：**

| 测试方法 | 验证目标 |
|---------|---------|
| `testDeepCopyCollectionNullRemainsNull` | Bean 的 Collection 属性为 null，deepCopy 后保持 null |
| `testDeepCopyMapNullRemainsNull` | Bean 的 Map 属性为 null，deepCopy 后保持 null |
| `testDeepCopyShallowNullRemainsNull` | 浅拷贝（maxDepth=0）时 null 属性保持 null |

> 注：新增测试使用现有的 `CollectionNullBean` 和 `MapNullBean` 内部类，不新增测试实体类。

## 3. SubStory 分解

### 3.1 功能评分

| SubStory | 功能 | 参数 | 逻辑 | 依赖 | 边界 | 产出 | 状态 | 异常 | 外部 | 总分 | 级别 |
|----------|------|------|------|------|------|------|------|------|------|------|------|
| 01 | 移除 null→instance 逻辑 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 8 | 极简 |
| 02 | 合并浅/深拷贝分支 | 1 | 2 | 1 | 2 | 1 | 1 | 1 | 1 | 10 | 极简 |
| 03 | 测试：删除+新增 | 1 | 2 | 1 | 2 | 1 | 1 | 1 | 1 | 10 | 极简 |
| 04 | 编译验证 | — | — | — | — | — | — | — | — | — | 非编码 |

合并前提检查（01+02）：
- ✅ 条件A：同一实体（deepCopyBean 方法），同属一个流程（先移除后合并）
- ✅ 条件B：引用相同文件（BeanUtils.java），参数/上下文相同
- ✅ 条件C：总文件数=1 ≤6，估算修改行数 < 500
- ✅ 合并策略：根据 Q3 确认「合并优化」

### 3.2 SubStory 列表

| # | 名称 | 范围 | 依赖 | 验收标准 |
|---|------|------|------|---------|
| 01 | 移除null实例化并合并分支 | 删除两处 null→instance 逻辑；合并两个分支为条件表达式 | 无 | deepCopyBean 无 null→instance 代码，浅/深拷贝通过条件分支合并 |
| 02 | 更新单元测试 | 删除冲突测试；新增 null 保持验证测试 | 01 | 旧测试删除，新测试验证 null 保持 null |
| 03 | 编译验证 | mvn compile + mvn test | 01, 02 | 编译通过，全部测试通过 |

### 3.3 DAG

```
01 ──▶ 02 ──▶ 03
```

### 3.4 Wave 规划

| Wave | SubStory | 说明 |
|------|----------|------|
| 1 | 01-修复并合并 | 编码：修改 BeanUtils.java |
| 2 | 02-更新测试 | 编码：修改 BeanUtilsTest.java |
| 3 | 03-编译验证 | 验证：编译+测试 |

## 4. 子任务分解

### SubStory 01：移除null实例化并合并分支

#### Task 1：移除 null→instance 逻辑并合并分支

| 维度 | 内容 |
|------|------|
| **类型** | coding |
| **状态** | pending |
| **依赖** | 无 |
| **涉及文件** | `src/main/java/com/tingfeng/util/java/base/bean/BeanUtils.java` |
| **上下文** | deepCopyBean 方法（lines 718-792），需修改 lines 754-789 |
| **具体修改** | 1. 删除 lines 756-768（浅拷贝分支的 null 检测和实例化）<br>2. 删除 lines 774-787（深拷贝分支的 null 检测和实例化）<br>3. 将剩余两个分支合并为条件表达式 |
| **查找指引** | 参考 BeanUtils.java lines 754-789 的现有结构 |

变更前：
```java
if (remainingDepth <= 0) {
    // 浅拷贝 + null→instance 逻辑 (lines 756-768)  ← 删除
    desc.setPropertyValue(target, propName, value);
} else {
    Object copiedValue = deepCopy(value, remainingDepth - 1, visited);
    // null→instance 逻辑 (lines 774-787)  ← 删除
    desc.setPropertyValue(target, propName, copiedValue);
}
```

变更后：
```java
Object finalValue = (remainingDepth <= 0)
        ? value
        : deepCopy(value, remainingDepth - 1, visited);
desc.setPropertyValue(target, propName, finalValue);
```

### SubStory 02：更新单元测试

#### Task 1：删除冲突测试 + 新增 null 保持验证测试

| 维度 | 内容 |
|------|------|
| **类型** | coding |
| **状态** | pending |
| **依赖** | SubStory 01 Task 1 |
| **涉及文件** | `src/test/java/com/tingfeng/util/java/base/bean/BeanUtilsTest.java` |
| **具体修改** | 1. 删除 testDeepCopyCollectionNullToArrayList（lines 1565-1584）<br>2. 删除 testDeepCopyMapNullToHashMap（lines 1586-1608）<br>3. 在原位置（或末尾）新增 3 个测试方法（见下方） |
| **查找指引** | 测试文件现有 `CollectionNullBean`（line 1774）和 `MapNullBean`（line 1807）内部类可直接复用 |

新增测试方法伪代码：

```java
@Test
public void testDeepCopyCollectionNullRemainsNull() {
    CollectionNullBean source = new CollectionNullBean();
    source.setAge(25);
    source.setName("Test");
    // collectionProp 值为 null
    
    CollectionNullBean result = BeanUtils.deepCopy(source);
    
    // 基本属性正常拷贝
    Assert.assertEquals(source.getAge(), result.getAge());
    Assert.assertEquals(source.getName(), result.getName());
    // null 属性保持 null（核心验证）
    Assert.assertNull(result.getCollectionProp());
}

@Test
public void testDeepCopyMapNullRemainsNull() {
    MapNullBean source = new MapNullBean();
    source.setAge(25);
    source.setName("Test");
    // mapProp 值为 null
    
    MapNullBean result = BeanUtils.deepCopy(source);
    
    Assert.assertEquals(source.getAge(), result.getAge());
    Assert.assertEquals(source.getName(), result.getName());
    Assert.assertNull(result.getMapProp());
}

@Test
public void testDeepCopyShallowNullRemainsNull() {
    // 浅拷贝场景：maxDepth = 0
    CollectionNullBean source = new CollectionNullBean();
    source.setAge(25);
    source.setName("Test");
    // collectionProp 值为 null
    
    CollectionNullBean result = BeanUtils.deepCopy(source, 0);
    
    Assert.assertEquals(source.getAge(), result.getAge());
    Assert.assertEquals(source.getName(), result.getName());
    Assert.assertNull(result.getCollectionProp());
}
```

### SubStory 03：编译验证

#### Task 1：编译并运行全部测试

| 维度 | 内容 |
|------|------|
| **类型** | analysis |
| **状态** | pending |
| **依赖** | SubStory 01 Task 1, SubStory 02 Task 1 |
| **涉及文件** | 无 |
| **具体操作** | 1. `mvn compile -Dmaven.compiler.useIncrementalCompilation=simple`<br>2. `mvn test` |

## 5. 确认门控自检

### 覆盖检验

| 需求点 | 设计方案 | 状态 |
|--------|---------|------|
| null 语义修正：所有接口类型的 null 属性保持 null | deepCopyBean 删除 null→instance 逻辑 | ✅ |
| 代码优化：合并两个分支 | 条件表达式合并两条分支 | ✅ |
| 删除冲突测试 | SubStory 02 Task 1 | ✅ |
| 新增 null 语义验证测试 | 3 个新测试方法 | ✅ |

### 目标对齐

- 所有设计与「修正 null 语义 + 优化代码结构」整体目标一致 ✅
- 无偏离或遗漏 ✅

### 可行检验

- 技术方案简单可靠，仅做删除和合并，无新依赖引入 ✅
- 边界场景覆盖完整（null 值、浅/深拷贝、循环引用） ✅

### 完整性检验

- 每个 Task 的涉及文件、查找指引、上下文明确 ✅
- 依赖关系通过 DAG 清晰表达 ✅

### 确定性评估：≥ 98%

无不确定点：story 中 4 个边界问题均已确认，技术方案明确。

---

## 元数据更新

- 创建时间: 2026-06-01 10:40
- 最后更新: 2026-06-01 10:45
- 状态: 已完成
