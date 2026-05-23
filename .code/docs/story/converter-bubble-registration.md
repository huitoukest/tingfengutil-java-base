# Converter 冒泡注册机制

## 基本信息

| 项目 | 内容 |
|------|------|
| 创建时间 | 2026-05-23 |
| 最后更新 | 2026-05-23 |
| 状态 | 已完成 |
| 相关文档 | - |

## 背景与目标

### 背景

在面向对象编程中，子类继承父类或实现接口是基本特性。如果 K → A 的转换器已存在，那么 K → parent(A) 的转换也应该自动成立（任何 K 对象都是 A 的子类/实现类实例）。

当前 `ConverterRegistry` 的 `register(Converter<S, T>)` 方法只注册精确的 (S, T) 类型对，不支持自动向上冒泡注册。

### 目标

在 `Converter` 接口增加冒泡属性，注册时自动分析目标类的父类链（类父类 + 实现接口），支持向上冒泡注册。

## 用户角色

- 工具库使用者：注册自定义 Converter 时自动获得父类转换能力
- 框架开发者：利用冒泡机制减少重复 Converter 定义

## 功能列表

### MoSCoW

| 优先级 | 功能 | 说明 |
|--------|------|------|
| Must | Converter 接口新增冒泡属性 | `bubbleLevel()`、`registrationOrder()`、`order()` |
| Must | 注册时自动冒泡 | 修改 `register()` 实现父类链 + 接口链遍历 |
| Must | 冒泡排序机制 | `registrationOrder` + `order` 联合排序 |
| Must | 查找时统一排序遍历 | 所有 Converter 参与排序，第一个匹配的被采用 |
| Should | 支持接口冒泡 | 接口的父接口递归遍历 |
| Should | `bubbleLevel = -1` 无限制冒泡 | 冒泡到 Object 为止 |

## 冒泡行为（核心规则）

### bubbleLevel 处理

```
原始 Converter: bubbleLevel = 2
  ↓ 注册自身（bubbleLevel 不变）:
String → Number, bubbleLevel = 2

  ↓ 冒泡一层，bubbleLevel - 1:
String → Object, bubbleLevel = 1

  ↓ bubbleLevel 减到 0，停止冒泡
```

### 特殊情况

- `bubbleLevel = -1`：冒泡到 Object（含Object）为止，且冒泡产生的 Converter 仍保持 `-1`
- `bubbleLevel > 0`：每冒泡一次 -1，直到达到 Object 或值变为 0 则停止
- `registrationOrder`：原始为 0，每冒泡一层 +1，超过 `Integer.MAX_VALUE` 时取 `Integer.MAX_VALUE`

### 冒泡范围与遍历顺序

**遍历顺序：先父类链，再接口链**

1. **父类链**：`Class.getSuperclass()` 向上遍历
2. **接口链**：`Class.getInterfaces()` 向上遍历，再递归遍历每个接口的父接口

### 完整冒泡示例

注册 `String → Number(bubbleLevel=3, order=5)`：

```
原始注册：
String → Number     registrationOrder=0, order=5, bubbleLevel=3

第一层（bubbleLevel=2）：
  类父类链：
    String → Object     registrationOrder=1, order=5, bubbleLevel=2
  
  接口链（Number 实现的接口）：
    String → Serializable     registrationOrder=1, order=5, bubbleLevel=2
    String → Comparable        registrationOrder=1, order=5, bubbleLevel=2

第二层（bubbleLevel=1）：
  类父类链：Object 无父类，停止
  
  接口链冒泡（Comparable 的父接口）：
    String → Object           registrationOrder=2, order=5, bubbleLevel=1

第三层（bubbleLevel=0，停止）：
  接口链：Object 无父接口，停止
```

## 排序机制

**比较规则：**
1. 先比较 `registrationOrder`，值小优先
2. `registrationOrder` 相同时比较 `order`，值小优先

**实现：**
```java
Comparator<Converter<?, ?>> COMPOSITE_ORDER = 
    Comparator.comparingInt(Converter::registrationOrder)
              .thenComparingInt(Converter::order);
```

## 查找行为

- 所有 Converter（精确匹配 + 冒泡产生）统一参与排序
- 遍历排序后的列表，第一个 `matches(source)=true` 的被采用
- 无 `matches()` 方法的普通 Converter 直接使用

## 接口设计

### Converter.java 新增方法

```java
public interface Converter<S, T> {
    T convert(S source);
    Class<S> getSourceType();
    Class<T> getTargetType();
    
    /**
     * 可冒泡层数。注册时生效，控制向上注册到第几层父类
     * 0 = 不冒泡，仅注册自身
     * 1 = 注册自身 + 直接父类
     * n = 注册自身 + 向上 n 层父类
     * -1 = 无限制，冒泡到 Object 为止
     * @return 冒泡层数
     */
    default int bubbleLevel() {
        return 1;
    }
    
    /**
     * 注册顺序。内部使用，由注册工具自动计算
     * 原始注册的 Converter 为 0，每冒泡一层 +1
     * @return 注册顺序
     */
    default int registrationOrder() {
        return 0;
    }
    
    /**
     * 用户定义的排序顺序。registrationOrder 相同时用于排序
     * @return 排序值，越小越优先
     */
    default int order() {
        return 0;
    }
}
```

## 影响范围

| 文件 | 变更 |
|------|------|
| `Converter.java` | 新增三个方法：`bubbleLevel()`、`registrationOrder()`、`order()` |
| `DefaultConverterRegistry.java` | 修改 `register()` 实现冒泡逻辑（父类链+接口链），更新排序比较逻辑 |
| `ConverterSearchResult.java` | 调整排序 |
| `ConverterConstants.java` | 可能新增常量 |

## 异常流程与边界

| 场景 | 处理 |
|------|------|
| `bubbleLevel = 0` | 仅注册自身，不冒泡 |
| `bubbleLevel = -1` | 冒泡到 Object 为止（含 Object），冒泡产生的也保持 -1 |
| registrationOrder 超过 Integer.MAX_VALUE | 取 Integer.MAX_VALUE |
| Object 是接口（如 Cloneable） | 同样停止冒泡 |
| 接口无父接口 | 停止 |
| 数组类型 | getSuperclass 返回 null，getInterfaces 返回空数组 |

## 待确认问题

- [x] 冒泡方向：仅目标类向上冒泡
- [x] 冒泡范围：类父类 + 接口父类
- [x] 遍历顺序：先父类链，再接口链
- [x] 多个接口顺序：按 `Class.getInterfaces()` 返回顺序
- [x] 冒泡停止点：包含 Object
- [x] bubbleLevel = -1：冒泡到 Object 为止，冒泡产生的也保持 -1
- [x] 排序规则：registrationOrder + order 联合升序
- [x] 查找行为：所有 Converter 统一排序遍历

## 决策记录

| 日期 | 决策 | 说明 |
|------|------|------|
| 2026-05-23 | 冒泡属性放到 Converter 接口 | 非破坏性变更，默认值兼容现有 Converter |
| 2026-05-23 | registrationOrder 代替原 bubbleOrder | 避免与优先级概念混淆 |
| 2026-05-23 | 支持接口冒泡 | 接口的父接口递归遍历 |