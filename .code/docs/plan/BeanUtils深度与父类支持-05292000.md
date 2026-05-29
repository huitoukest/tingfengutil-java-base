# BeanUtils 深度拷贝深度控制与父类属性支持

- 创建时间: 2026-05-29 15:46
- 最后更新: 2026-05-29 15:46
- 状态: 已完成

## 1. 需求概要

基于用户确认的两个需求：

### 需求1：deepCopy 递归深度控制

| maxDepth | 语义 |
|----------|------|
| > 0 | 允许向下递归 maxDepth 层 |
| = 0 | 只拷贝当前层（不递归） |
| < 0 | 非法，抛 IllegalArgumentException |

### 需求2：父类属性拷贝

- 修复 BeanDesc.introspect() 中 pdMap 不含父类 PropertyDescriptor 的 bug
- CopyOptions 新增 `copySuperclassProperties` 布尔开关（默认 true）
- 默认行为：拷贝父类属性
- 可通过 `CopyOptions.copySuperclassProperties(false)` 关闭

## 2. 实体关系图

```
BeanUtils (门面类)
├── deepCopy(source)                      # 现有，不变
├── deepCopy(source, maxDepth)            # 新增
└── deepCopy(source, remainingDepth, visited)  # 内部方法，新增深度参数

BeanCopier
├── copy(source, target, options)
├── copyFromProvider(provider, target, options)
├── copyProperties(...)                   # 根据 copySuperclassProperties 选择属性集
└── getOrCreateBeanDesc(clazz)            # 缓存获取 BeanDesc

BeanDesc
├── PropertyDescriptor pdMap              # 修复：遍历类层级，含父类
├── Field fieldMap                        # 已有：含父类私有字段
├── allPropertyNames                      # pdMap ∪ fieldMap
├── allFieldNames                         # fieldMap keys
└── currentClassPropertyNames [NEW]       # 仅当前类属性名

CopyOptions
├── copySuperclassProperties [NEW]        # boolean, 默认 true
└── ... 其他现有选项
```

## 3. 类层级遍历顺序

```
User.class (当前类)
  ├── pdMap: age, c, userName, user, isOk, otherNames, homeNames, map, childList, parentUserName, updateDateTime, interval, a
  └── fieldMap: a(String), isOk, userName, updateDateTime, age, interval, user, c, otherNames, homeNames, map, childList, parentUserName
      └── (静态字段 b 被过滤)
      
BaseUser.class (父类)
  ├── pdMap: parentFiled
  └── fieldMap: parentFiled
      └── (通过 ReflectUtils.getFields(cls, false, false, true, true) 递归获取)
```

## 4. API 设计

### 4.1 BeanUtils (门面类)

| 方法 | 参数 | 返回值 | 异常 | 说明 |
|------|------|--------|------|------|
| `deepCopy(T source, int maxDepth)` | source=T, maxDepth=int | T | IllegalArgumentException (maxDepth<0) | 新增，深度受控的深拷贝 |
| `deepCopy(T source)` | source=T | T | — | 保持不变，委托 `deepCopy(source, Integer.MAX_VALUE)` |

### 4.2 CopyOptions

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `setCopySuperclassProperties(boolean)` | boolean | CopyOptions | 设置是否拷贝父类属性 |
| `isCopySuperclassProperties()` | — | boolean | 获取，默认 true |

### 4.3 BeanDesc

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `getCurrentClassPropertyNames()` | — | Set\<String\> | 仅当前类定义的属性名（不含父类） |

## 5. 核心逻辑设计

### 5.1 deepCopy 深度控制 (BeanUtils.java)

```
deepCopy(source)  →  deepCopy(source, Integer.MAX_VALUE, new IdentityHashMap<>())

deepCopy(source, maxDepth)
  ├── maxDepth < 0  →  throw IllegalArgumentException
  └── 委托 deepCopy(source, maxDepth, new IdentityHashMap<>())

deepCopy(source, remainingDepth, visited)
  流程：
  ├── source == null → return null
  ├── if isImmutableType(source) → return source (不可变类型，不消耗深度)
  ├── if visited.containsKey(source) → return visited.get(source) (循环引用)
  ├── if clazz.isArray() → deepCopyArray(source, remainingDepth, visited)
  ├── if source instanceof Collection → deepCopyCollection(source, remainingDepth, visited)
  ├── if source instanceof Map → deepCopyMap(source, remainingDepth, visited)
  └── else → deepCopyBean(source, remainingDepth, visited)

深度控制规则：
  remainingDepth <= 0 时：
  - 数组：创建新数组，元素不递归（直接引用原元素）
  - Collection：创建新集合，元素不递归
  - Map：创建新 Map，key/value 不递归
  - Bean：创建新实例，属性值不递归（浅拷贝）
  
  remainingDepth > 0 时：
  - 递归调用，传递 remainingDepth - 1
```

### 5.2 BeanDesc.introspect() 父类支持修复

```
introspect(clazz):
  1. pdMap 收集（遍历类层级）：
     for each currentClass from clazz up to Object (exclusive):
       BeanInfo = Introspector.getBeanInfo(currentClass, currentClass.getSuperclass())
       for each PropertyDescriptor pd:
         if !"class".equals(pd.getName()) && !pdMap.containsKey(pd.getName()):
           pdMap.put(pd.getName(), pd)
           if isFirstLevel: currentClassPropertyNames.add(pd.getName())
     
     isFirstLevel:
       同时遍历 clazz.getDeclaredFields()，非 static 的字段名加入 currentClassPropertyNames

  2. fieldMap 收集（已有，保持不变）：
     ReflectUtils.getFields(clazz, false, false, true, true)
     // 非static、非final字段，含父类私有字段
     
  3. allPropertyNames = pdMap.keySet() ∪ fieldMap.keySet()
     allFieldNames = fieldMap.keySet()
     currentClassPropertyNames 独立维护
```

### 5.3 BeanCopier.copy() 使用 copySuperclassProperties 选项

```
在 copy(Object source, Object target, CopyOptions options) 中：

// 获取 target 可写属性名集合
Set<String> targetPropertyNames;
if (!options.isCopySuperclassProperties()) {
    // 仅拷贝当前类属性（不含父类）
    targetPropertyNames = targetDesc.getCurrentClassPropertyNames();
} else if (options.isForceFieldAccess()) {
    // 仅使用 fieldMap
    targetPropertyNames = getFieldMapKeySet(targetDesc);
} else {
    // 使用所有属性名（pdMap ∪ fieldMap）
    targetPropertyNames = targetDesc.getPropertyNames();
}

copyFromProvider() 同样处理。
```

## 6. 状态转换表

deepCopy 递归层级变化：

```
调用层     remainingDepth    行为
0（顶层）   maxDepth         拷贝所有直接属性
1           maxDepth-1       拷贝属性的属性
...
maxDepth    0                只创建容器，不递归内部元素
maxDepth+1  -1              不产生调用
```

## 7. 核心边界条件与异常处理

| 边界 | 处理方式 |
|------|---------|
| maxDepth < 0 | 抛 IllegalArgumentException("maxDepth must be >= 0") |
| maxDepth = 0 | 创建新实例，属性浅拷贝（不可变类型直接引用，bean 属性共享引用） |
| maxDepth = 1 | 属性直接拷贝（属性如果是 bean 则创建新实例，但 bean 的属性不递归） |
| maxDepth = Integer.MAX_VALUE | 等同于无限制递归 |
| copySuperclassProperties = true | 拷贝父类属性（默认） |
| copySuperclassProperties = false | 仅拷贝当前类属性 |
| source/target 为 null | 不抛异常，直接 return（copyProperties 现有行为） |
| 循环引用 | IdentityHashMap 检测，不栈溢出（现有行为） |
| maxDepth 与循环引用共存 | remainingDepth 先到 0 则停止递归，visited 检测也同时生效 |

## 8. 涉及文件清单

| 文件 | 用途 | 变更类型 | 涉及功能 |
|------|------|---------|---------|
| BeanUtils.java | 新增 deepCopy 深度方法 | modify | deepCopy 深度控制 |
| BeanDesc.java | 修复 pdMap 父类支持 | modify | 父类属性支持 |
| CopyOptions.java | 新增 copySuperclassProperties | modify | 父类属性支持 |
| BeanCopier.java | 使用 copySuperclassProperties 选项 | modify | 父类属性支持 |
| BeanUtilsTest.java | deepCopy 深度测试 | modify | deepCopy 深度控制 |
| BeanCopierTest.java | 父类属性拷贝测试 | modify | 父类属性支持 |

## 9. 子任务分解清单

### Task 评分与分组

| 功能 | 参数 | 逻辑 | 依赖 | 边界 | 产出 | 状态 | 异常 | 外部 | 总分 | 级别 |
|------|------|------|------|------|------|------|------|------|------|------|
| **SubStory 1: deepCopy 深度控制** |
| 1.1 BeanUtils.deepCopy 新增 maxDepth 方法 | 1 | 2 | 1 | 2 | 1 | 1 | 1 | 1 | 10 | 极简 |
| 1.2 修改内部递归方法支持深度参数 | 1 | 3 | 1 | 3 | 1 | 1 | 1 | 1 | 12 | 极简 |
| 1.3 修改 deepCopyArray/Collection/Map/Bean 传递深度 | 1 | 2 | 1 | 2 | 1 | 1 | 1 | 1 | 10 | 极简 |
| 1.4 测试用例 | 1 | 2 | 1 | 2 | 1 | 1 | 1 | 1 | 10 | 极简 |
| **SubStory 2: 父类属性支持** |
| 2.1 BeanDesc.introspect() pdMap 父类修复 | 1 | 3 | 2 | 2 | 1 | 1 | 1 | 1 | 12 | 极简 |
| 2.2 CopyOptions 新增 copySuperclassProperties | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 8 | 极简 |
| 2.3 BeanCopier 适配选项 | 1 | 2 | 1 | 2 | 1 | 1 | 1 | 1 | 10 | 极简 |
| 2.4 测试用例 | 1 | 2 | 1 | 2 | 1 | 1 | 1 | 1 | 10 | 极简 |

**合并判断：**
- **SubStory 1**: 所有功能处理同一实体 BeanUtils.deepCopy，引用相同文件（BeanUtils.java, BeanUtilsTest.java），合并后总文件数 ≤ 6，估算行数 ≤ 300 → 合并为 1 个 Task
- **SubStory 2**: 所有功能属于同一流程（父类属性拷贝），引用相同文件（BeanDesc.java, CopyOptions.java, BeanCopier.java, BeanCopierTest.java），合并后总文件数 ≤ 6，估算行数 ≤ 400 → 合并为 1 个 Task

### 功能清单

#### Task 1: deepCopy 深度控制 (SubStory 1)
- **功能名**: deepCopy 深度控制实现 + 测试
- **模块文件**: 
  - api: `src/main/java/com/tingfeng/util/java/base/bean/BeanUtils.java`
  - test: `src/test/java/com/tingfeng/util/java/base/bean/BeanUtilsTest.java`
- **依赖**: 无
- **查找指引**: 参考 BeanUtils.java 中现有 deepCopy 系列方法（第 477-665 行），按三参数模式新增

#### Task 2: 父类属性支持 (SubStory 2)
- **功能名**: 父类属性拷贝修复 + 选项 + 测试
- **模块文件**:
  - api: `src/main/java/com/tingfeng/util/java/base/bean/copier/BeanDesc.java`
  - api: `src/main/java/com/tingfeng/util/java/base/bean/copier/CopyOptions.java`
  - api: `src/main/java/com/tingfeng/util/java/base/bean/copier/BeanCopier.java`
  - test: `src/test/java/com/tingfeng/util/java/base/bean/copier/BeanCopierTest.java`
- **依赖**: 无
- **查找指引**: 参考 BeanDesc.java 第 83-105 行 introspect() 方法；CopyOptions.java 现有 setter/getter 模式；BeanCopier.java 第 54-74 行 copy() 方法

## 10. 验收标准

### SubStory 1: deepCopy 深度控制
- [ ] `deepCopy(source, -1)` 抛 IllegalArgumentException
- [ ] `deepCopy(source, 0)` 只拷贝当前层（bean 属性浅拷贝）
- [ ] `deepCopy(source, 1)` 允许向下递归 1 层
- [ ] `deepCopy(source)` 与 `deepCopy(source, Integer.MAX_VALUE)` 行为一致
- [ ] 循环引用 + 深度控制共存，不栈溢出
- [ ] 不可变类型在所有深度下仍直接返回引用

### SubStory 2: 父类属性支持
- [ ] BeanDesc.getPropertyNames() 包含父类属性（如 User 包含 parentFiled）
- [ ] BeanDesc.getCurrentClassPropertyNames() 仅含当前类属性
- [ ] `deepCopy` / `copyProperties` 默认拷贝父类属性
- [ ] `CopyOptions.create().copySuperclassProperties(false)` 不拷贝父类属性
- [ ] 父类属性优先通过 getter/setter 访问
- [ ] BeanCopier 测试通过，已有测试不受影响
