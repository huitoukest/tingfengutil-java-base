# Java Code Review 规范

## 一、命名规范

### 1.1 包名
- 全小写，使用单数名词
- `com.company.project.module`
- 禁止：下划线、混合大小写、数字开头

### 1.2 类名
- UpperCamelCase
- Utils/Util 结尾：静态工具类（如 `StringUtils`）
- Helper 结尾：实例化帮助类（如 `PoolHelper`）
- I 结尾：接口（如 `PoolMemberActionI`）
- DO/BO/DTO/VO：对应领域对象命名
- 禁止：缩写（除业界通用如 `URL`/`ID`/`DTO`/`VO`）

### 1.3 方法名
- lowerCamelCase
- 获取：`getXxx()` / `findXxx()` / `queryXxx()`
- 判断：`isXxx()` / `hasXxx()` / `containsXxx()`
- 设置：`setXxx()`
- 转换：`toXxx()` / `convertXxx()`
- 布尔返回值禁止用 `get` 开头

### 1.4 变量名
- lowerCamelCase
- 集合加复数或类型后缀：`users` / `userList` / `userMap`
- 常量全大写下划线分隔：`MAX_BUFFER_SIZE`
- 临时变量避免单字母（循环除外：`i`/`j`/`k`）

### 1.5 常量接口
- 接口命名：`XxxConstants`
- 内部接口/类分组：`大类别小写名称`（如 `JVMSystem`）
- 同类型常量放同一分组，用空行分隔

---

## 二、代码风格

### 2.1 缩进与空格
- 4 空格缩进，禁止 Tab
- 二元运算符两侧空格：`a + b`
- 一元运算符与操作数紧邻：`i++`
- 逗号后空格：`method(a, b, c)`
- 禁止行尾多余空格

### 2.2 大括号
- K&R 风格（不换行）：
```java
if (condition) {
    doSomething();
}
```
- else/catch 与前括号同行：
```java
if (condition) {
    doSomething();
} else {
    doOther();
}
```

### 2.3 行长度
- 单行不超过 120 字符
- 链式调用可换行：
```java
List<String> result = users.stream()
        .filter(User::isActive)
        .map(User::getName)
        .collect(Collectors.toList());
```

### 2.4 import
- 顺序：java → javax → 第三方 → 项目内部
- 同包内不需要 import
- 禁止通配符 import（`import java.util.*`）

---

## 三、注释规范

### 3.1 类/接口注释
```java
/**
 * 类功能描述
 * @author authorName
 * @date YYYY-MM-DD
 */
public class XxxUtils {
```

### 3.2 方法注释
```java
/**
 * 方法功能描述
 * @param paramName 参数说明（无参数可不写）
 * @return 返回值说明（无返回值可不写）
 * @throws XxxException 异常说明（无throws可不写）
 */
public Xxx doSomething(String param) {
```

### 3.3 行内注释
- 注释在代码上方或行尾
- 行尾注释与代码至少一个空格分隔
- 禁止无意义注释：`// 循环` `// 设置值`

### 3.4 TODO/FIXME
- 必须带负责人或日期：`// TODO[huitoukest 2026-04-05]: 优化为批量处理`

---

## 四、异常处理

### 4.1 异常捕获
- 禁止 `catch(Exception e)` / `catch(Throwable t)`，除非顶层统一处理
- 精确捕获具体异常类型
- 禁止吞掉异常（空 catch 或仅打印日志）：
```java
// 禁止
} catch (Exception e) {
}

// 允许（须有明确理由）
} catch (Exception e) {
    log.error("xxx failed", e);
    throw new BusinessException("xxx failed", e);
}
```

### 4.2 异常抛出
- 业务异常用 `BusinessException` / 自定义异常
- 禁止抛出 `RuntimeException` / `Exception`
- 禁止用异常做流程控制

### 4.3 资源关闭
- 必须使用 try-with-resources：
```java
try (InputStream is = new FileInputStream(file)) {
    // use stream
}
```
- 禁止在 finally 中关闭可能为 null 的资源

---

## 五、并发与线程

### 5.1 线程安全
- `ThreadLocal` 使用后必须 `remove()`
- 共享可变对象必须同步
- 集合类：`ConcurrentHashMap` / `CopyOnWriteArrayList`（写多读少）
- 禁止在 Servlet/Controller 中创建线程

### 5.2 线程池
- 禁止 `Executors.newFixedThreadPool(n)` / `newCachedThreadPool()`（OOM风险）
- 使用 `ThreadPoolExecutor` 显式配置参数
- 拒绝策略：`CallerRunsPolicy` / `AbortPolicy`

### 5.3 synchronized
- 优先使用 `Lock` 或 `ConcurrentHashMap`
- 同步块尽量小（锁定对象需外部可访问时除外）
- 禁止在同步块外操作共享变量

---

## 六、性能规范

### 6.0 工具选用通用原则

**核心思路**：所有与项目现有工具重叠的系统功能，都应进行性能对比分析，不能一概而论。

**分析步骤**：

| 步骤 | 操作 | 说明 |
|------|------|------|
| 1 | 查项目工具 | 先查 `common/utils/` 下是否有对应工具 |
| 2 | 性能对比 | 分析时间复杂度、内存占用、并发安全 |
| 3 | 决策 | 差异小 → 用项目工具；项目工具明显更优 → 用项目工具；系统 API 明显更优 → 记录原因后用系统 API |

**差异小的判断标准**：
- 执行时间差异 < 5%（经验值）
- 内存占用差异可忽略
- 功能完全等价

**不适用"项目优先"的例外**（须记录原因）：
- 项目工具未覆盖的场景（如 `java.nio.file.Files.walkFileTree()`）
- 系统 API 有重大性能优势（如 `String.getBytes(Charset)` 底层直接调用）
- 涉及 JDK 底层优化（如 `Collections.unmodifiableMap` 内部直接返回固定对象）

---

### 6.1 字符串拼接

**性能分析**：

| 场景 | 项目工具 | 系统 API | 推荐 |
|------|----------|----------|------|
| 普通拼接（<1KB） | `StringUtils.append()` — **StringBuilder 池化复用，多线程无锁** | `StringBuilder` 每次新建 | 项目工具 |
| 模板/超长字符串（≥1KB） | `StringUtils.replaceByTemplate()` | `StringBuilder` | 系统 API 更灵活 |
| 循环内拼接 | `StringUtils.append()` | `StringBuilder` | 项目工具（池化复用） |
| 固定分隔符连接集合 | `CollectionUtils.join()` — 内部用 `StringUtils.doAppend()` | `String.join()` | 项目工具 |

**分析结论**：
- 项目 `StringUtils.append()` 使用 `FixedPoolHelper<StringBuilder>` 池化复用，`StringBuilder` 初始容量 128，最大容量 512，多线程并发时无锁化
- 系统 `StringBuilder` 每次创建新对象，有 GC 压力
- 普通拼接（<1KB）结果差异不明显，但项目工具在高频调用场景下有显著优势
- 循环内拼接禁止用 `+`，因为编译器会每次创建新的 String 对象

```java
// 推荐：使用项目工具
String result = StringUtils.append("a", "b", "c", 123);

// 循环内拼接：必须使用 StringBuilder 或项目工具，禁止直接 +
// 禁止：
for (String s : list) {
    result = result + s;  // 每次创建新String对象
}

// 推荐：
String result = StringUtils.doAppend(sb -> {
    for (String s : list) {
        sb.append(s);
    }
    return sb.toString();
});
```

---

### 6.2 集合操作

**性能分析**：

| 场景 | 项目工具 | 系统 API | 推荐 |
|------|----------|----------|------|
| 集合判空/取首尾元素 | `CollectionUtils.getFirst()` / `getLast()` | `list.isEmpty()` / `list.get(0)` | 均可 |
| 集合连接为字符串 | `CollectionUtils.join()` — **池化 StringBuilder** | `String.join()` | 项目工具（性能略优） |
| 集合分组成子集合 | `CollectionUtils.split()` | 手写循环 | 项目工具 |
| 集合打乱顺序 | `CollectionUtils.shuffle()` — **自定义洗牌算法** | `Collections.shuffle()` | 项目工具（可控洗牌次数） |
| List/Set 内容比较 | `CollectionUtils.eq()` | 手写循环或 Stream | 项目工具（更简洁） |
| 查找符合条件的元素 | `CollectionUtils.findFirst()` / `findAny()` | Stream API | 功能等价，用任一均可 |

**分析结论**：
- 项目工具在集合连接、打乱算法上有优化
- 判空、取元素等基础操作两者差异极小（<1%），按代码可读性选择
- Stream API 用于复杂链式操作场景更清晰

---

### 6.3 日期时间

**性能分析**：

| 场景 | 项目工具 | 系统 API | 推荐 |
|------|----------|----------|------|
| 日期格式化/解析 | `DateUtils.format()` / `parse()` — **SimpleDateFormat 池化复用** | `SimpleDateFormat` 每次新建 | 项目工具 |
| 线程不安全日期操作 | `DateUtils` | `Calendar` / `Date` | 项目工具（已做线程安全处理） |
| LocalDateTime 互转 | `DateUtils.toLocalDate()` / `toDate()` | `Date.from()` / `date.toInstant()` | 功能等价 |
| 日期计算（加减天数等） | `DateUtils.getDateAdd()` | `Calendar.add()` | 均可 |
| 获取日期区间 | `DateUtils.getDatesBetweenTwoDate()` | 手写循环 | 项目工具 |

**分析结论**：
- `SimpleDateFormat` 是线程不安全的，项目 `DateUtils` 使用 `FixedPoolHelper` 池化 + `ConcurrentHashMap` 缓存，每种格式默认 8-N 个实例复用
- 系统 `ThreadLocal<SimpleDateFormat>` 方案：每个线程一个实例，有内存开销
- 高并发格式化场景（QPS>1000），项目工具比每次 `new SimpleDateFormat` 快 3-5 倍

---

### 6.4 数组操作

**性能分析**：

| 场景 | 项目工具 | 系统 API | 推荐 |
|------|----------|----------|------|
| 数组判空/判包含 | `ArrayUtils.isContain()` | `Arrays.stream()` | 均可 |
| 数组反转 | `ArrayUtils.reverse()` — **双指针原地反转** | `Collections.reverse()` 需转 List | 项目工具 |
| 数组打乱 | `ArrayUtils.shuffle()` — **洗牌算法，支持指定次数** | `Collections.shuffle()` | 项目工具 |
| 数组连接为字符串 | `ArrayUtils.join()` — **内部用池化 StringBuilder** | `Arrays.toString()` | 项目工具（格式可控） |
| 类型转换 | `ArrayUtils.getArray()` | Stream `map()` | 均可 |

**分析结论**：
- 项目工具对所有基础类型（int/long/char/boolean/byte）都提供了 `shuffle` 和 `reverse` 重载
- `ArrayUtils.join()` 使用池化 StringBuilder，无多余对象创建

---

### 6.5 数学运算

**性能分析**：

| 场景 | 项目工具 | 系统 API | 推荐 |
|------|----------|----------|------|
| GCD 最大公约数 | `MathUtils.gcd()` | `BigInteger.gcd()` | 小数用项目工具，大数用系统 API |
| LCM 最小公倍数 | `MathUtils.lcm()` — **防溢出设计** | 手写 `a/gcd(a,b)*b` | 项目工具（有溢出检测） |
| 任意进制转换 | `MathUtils.toRadix()` — **支持 2-256 进制** | 无对应功能 | 项目工具（独有功能） |
| 基础数学（绝对值/最大/最小等） | 无增强 | `java.lang.Math` | 系统 API |

**分析结论**：
- 项目 `MathUtils.lcm()` 对 long 类型使用 `Math.multiplyExact()` 防溢出，对 int 类型先升级 long 计算再检测范围
- 进制转换工具是项目独有功能，无系统对应 API

---

### 6.6 随机数

**性能分析**：

| 场景 | 项目工具 | 系统 API | 推荐 |
|------|----------|----------|------|
| 基础随机数 | `RandomUtils.randomInt()` 等 | `ThreadLocalRandom.current().nextInt()` | 项目工具（更简洁） |
| 随机字符串 | `RandomUtils.randomString()` — **支持自定义字符集** | 无对应功能 | 项目工具（独有功能） |
| UUID | `RandomUtils.randomUUid()` | `UUID.randomUUID()` | 功能等价 |
| 自定义洗牌偏移量 | `RandomUtils.getDefaultExchangeOffsetValues()` | 无对应功能 | 项目工具（独有功能） |

**分析结论**：
- 项目 `RandomUtils` 底层也是 `ThreadLocalRandom`，性能无差异
- 项目优势在于：API 更简洁、字符集可配置、独有的洗牌算法

---

### 6.7 反射

| 场景 | 项目工具 | 系统 API | 推荐 |
|------|----------|----------|------|
| 获取/设置字段 | `ReflectUtils` / `ClassUtils` | `Field.get()` / `set()` | 项目工具（缓存优化） |
| 调用方法 | `MethodUtils.invoke()` | `Method.invoke()` | 项目工具（参数类型自动转换） |
| 获取类信息 | `ClassUtils.getClass()` | `Class.forName()` | 均可 |
| 泛型类型解析 | `GenericsUtils` | 无对应功能 | 项目工具（独有功能） |

---

### 6.8 I/O 与文件

| 场景 | 项目工具 | 系统 API | 推荐 |
|------|----------|----------|------|
| InputStream → String | `StringUtils.getStringByStream()` | 手写 byte[] 循环 | 项目工具 |
| InputStream → byte[] | `ArrayUtils.getBytesByInputStream()` — **try-with-resources** | 手写循环 | 项目工具 |
| 文件复制/移动 | `FileUtils` | `Files.copy()` / `move()` | 功能等价 |
| 遍历文件树 | 无增强 | `Files.walkFileTree()` | 系统 API |

---

### 6.9 循环

- 避免在循环内创建对象
- 嵌套循环注意顺序（外大内小）
- 循环内拼接必须用 `StringBuilder` 或项目工具

---

## 七、安全规范

### 7.1 输入校验
- 所有外部输入必须校验（参数、请求体、环境变量）
- SQL 禁止拼接，必须用参数化查询
- 禁止 `eval()` / `new ScriptEngineManager().getEngineByName("JS")`

### 7.2 敏感信息
- 禁止硬编码密码/密钥/Token
- 密码必须加密存储或使用配置中心
- 日志禁止打印密码/银行卡号/身份证号

### 7.3 序列化
- `Serializable` 类禁止 `transient` + 初始化赋值（反序列化不触发）
- 敏感字段用 `transient` 修饰

---

## 八、测试规范

### 8.1 测试覆盖率
- 核心业务逻辑：覆盖率 ≥ 80%
- 工具类：每方法至少一个测试
- 分支/异常路径必须覆盖

### 8.2 单元测试
- 测试类命名：`XxxUtilsTest` / `XxxServiceTest`
- 测试方法命名：`testMethodName_Scenario_ExpectedResult`
- 禁止测试间依赖（执行顺序无关）
- 禁止 `Thread.sleep()` 等待异步

### 8.3 断言
```java
// 禁止
Assert.assertTrue(list.size() > 0);

// 允许
Assert.assertFalse(list.isEmpty());
Assert.assertEquals(5, list.size());
```

---

## 九、Git 规范

### 9.1 Commit 消息
```
<type>: <subject>

<body>

<footer>
```
- type: `feat`/`fix`/`docs`/`style`/`refactor`/`test`/`chore`
- subject: 简短描述（不超过 50 字符）
- 禁止中文 subject

### 9.2 分支命名
- `feature/xxx`
- `bugfix/xxx`
- `hotfix/xxx`
- 禁止中文

### 9.3 PR 规范
- 单次 PR 不超过 300 行变更
- 每次 commit 需有测试
- 必须通过 CI

---

## 十、CheckList（Review 时逐项检查）

### 通用
- [ ] 命名符合规范
- [ ] 无硬编码魔法数字/字符串（应使用项目常量或枚举）
- [ ] 无 TODO 未完成项（除非标注计划版本）
- [ ] 无无用 import
- [ ] 无重复代码（复用现有工具类）
- [ ] Lombok 使用正确（`@Data` 慎用）
- [ ] 选用了系统 API 而非项目工具时，是否记录了原因

### 方法
- [ ] 参数校验（非空、范围、格式）
- [ ] 返回值不为 null（除非明确文档说明）
- [ ] 异常处理规范
- [ ] 资源正确关闭（try-with-resources）

### 并发
- [ ] 共享变量访问安全
- [ ] ThreadLocal 使用后清理
- [ ] 线程池配置合理

### 性能工具选用
- [ ] 字符串拼接：使用了 `StringUtils.append()` 或 `StringBuilder`（禁止循环内 `+`）
- [ ] 集合连接：使用了 `CollectionUtils.join()` 或项目工具
- [ ] 日期格式化：使用了 `DateUtils`（线程安全）
- [ ] 数组反转/打乱：使用了 `ArrayUtils` 对应方法
- [ ] 数学运算（大数/进制）：使用了 `MathUtils` 对应方法
- [ ] 反射调用：已缓存 `Method`/`Field` 对象

### 安全
- [ ] 输入校验
- [ ] 无敏感信息硬编码
- [ ] SQL 参数化

---

## 十一、项目关键工具索引

| 类别 | 工具类 | 主要优化点 |
|------|--------|-----------|
| 字符串 | `StringUtils` | `FixedPoolHelper<StringBuilder>` 池化复用，KMP 搜索，字符宽度计算 |
| 集合 | `CollectionUtils` | 池化 StringBuilder 连接，自定义洗牌算法 |
| 日期 | `DateUtils` | `ConcurrentHashMap` + `FixedPoolHelper` 缓存 SimpleDateFormat，线程安全 |
| 数组 | `ArrayUtils` | 双指针原地反转，池化 StringBuilder 连接 |
| 数学 | `MathUtils` | 防溢出 LCM，GCD/Euclid 算法，任意进制转换 |
| 随机 | `RandomUtils` | `ThreadLocalRandom`，可配置字符集随机字符串 |
| 反射 | `ReflectUtils` / `ClassUtils` / `MethodUtils` | 反射结果缓存，方法调用优化 |
| 线程 | `ThreadUtils` | `NamedThreadFactory` 线程工厂 |

---

## 使用方法

1. **实现前**：对照第六节工具选用原则，先查项目工具，再分析对比
2. **Review 时**：逐项对照第十节 CheckList 检查
3. **使用系统 API 时**：必须在代码注释或 PR 中记录原因

---

## 性能分析模板（选用系统 API 时填写）

```markdown
## 性能分析记录

**场景**：[具体操作，如：日期格式化]

**项目工具**：[工具名和方法名]

**系统 API**：[类名.方法名]

**对比分析**：
- 时间复杂度：
- 内存占用：
- 并发安全：
- 功能差异：

**结论**：[选用系统 API 的理由]

**记录人**：XXX
**日期**：YYYY-MM-DD
```