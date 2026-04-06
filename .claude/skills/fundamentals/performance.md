# 性能工具选用规范

## 核心原则

**所有与项目现有工具重叠的系统功能，都应进行性能对比分析，不能一概而论。**

## 决策流程

```
1. 查项目工具 → 2. 性能对比 → 3. 决策
```

**差异小的判断标准：**
- 执行时间差异 < 5%
- 内存占用差异可忽略
- 功能完全等价

**不适用"项目优先"的例外（须记录原因）：**
1. 项目工具未覆盖的场景
2. 系统API有明显性能优势
3. 涉及JDK底层优化

## 工具对比表

| 场景 | 项目工具 | 系统API | 推荐 |
|------|----------|---------|------|
| 字符串拼接(<1KB) | `StringUtils.append()` | `StringBuilder` | 项目工具（池化复用） |
| 循环内拼接 | `StringUtils.append()` | `+` | 项目工具 |
| 集合连接字符串 | `CollectionUtils.join()` | `String.join()` | 项目工具（池化） |
| 日期格式化 | `DateUtils.format()` | `SimpleDateFormat` | 项目工具（线程安全） |
| 数组反转 | `ArrayUtils.reverse()` | `Collections.reverse()` | 项目工具（原地反转） |
| 数组打乱 | `ArrayUtils.shuffle()` | `Collections.shuffle()` | 项目工具（可控次数） |
| GCD | `MathUtils.gcd()` | `BigInteger.gcd()` | 小数用项目工具 |
| LCM防溢出 | `MathUtils.lcm()` | 手写 | 项目工具 |

## 项目关键工具性能说明

| 类别 | 工具类 | 优化点 |
|------|--------|--------|
| 字符串 | `StringUtils` | `FixedPoolHelper<StringBuilder>` 池化复用，KMP搜索 |
| 集合 | `CollectionUtils` | 池化StringBuilder，自定义洗牌 |
| 日期 | `DateUtils` | `ConcurrentHashMap`+`FixedPoolHelper`缓存SimpleDateFormat |
| 数组 | `ArrayUtils` | 双指针原地反转，池化StringBuilder |
| 数学 | `MathUtils` | 防溢出LCM，任意进制(2-256)转换 |
| 随机 | `RandomUtils` | `ThreadLocalRandom`，可配置字符集 |
| 反射 | `ReflectUtils` | 反射结果缓存 |

## 典型错误

```java
// 禁止：循环内用 + 拼接
for (String s : list) {
    result = result + s;  // 每次创建新String对象
}

// 推荐：使用项目工具
String result = StringUtils.append("a", "b", "c");

// 或使用池化StringBuilder
String result = StringUtils.doAppend(sb -> {
    for (String s : list) sb.append(s);
    return sb.toString();
});
```

## 性能分析模板

```markdown
## 性能分析记录

**场景**：[具体操作]

**项目工具**：[工具名和方法名]

**系统API**：[类名.方法名]

**对比分析**：
- 时间复杂度：
- 内存占用：
- 并发安全：
- 功能差异：

**结论**：[选用系统API的理由]

**记录人**：XXX
**日期**：YYYY-MM-DD
```
