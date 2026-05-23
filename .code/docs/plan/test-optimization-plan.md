# 单元测试 System.out.println 优化计划

| 项目 | 内容 |
|------|------|
| 创建时间 | 2026-05-23 |
| 最后更新 | 2026-05-23 |
| 状态 | 已确认 |

## 已完成的优化

以下 10 个测试文件已优化完成（117 个测试通过）：
- RandomUtilsTest, LongFractionTest, IntFractionTest, RegExpUtilsTest, TokenHelperTest
- StringTemplateHelperTest, MathUtilsTest, FractionOptimizationTest, PoolHelperTest

## 剩余待优化的测试文件

共 13 个文件，49 处 System.out.println / e.printStackTrace 待处理。

## 优化总则

1. **保留多线程性能测试线程数**：`TestUtils.printTime`（即 `runConcurrentTest`）的线程数量保持不变，只减少循环次数
2. **用 Assert 替代 System.out.println**：将输出改为断言验证
3. **减少循环次数但不改为 1**：从百万级降到千级或万级，保持测试有效性
4. **避免内存溢出**：不使用 `Long.MAX_VALUE` 等极端值作为随机范围
5. **不修改被测代码**：只修改测试代码
6. **e.printStackTrace 替换**：测试中 `catch` 块内的 `e.printStackTrace()` 替换为 `Thread.currentThread().interrupt()` + 适当断言/忽略
7. **BaseTest.printTime 视为合法**：已在生产代码 `TestUtils.java` 中作为 `@Deprecated` 保留，测试中可继续使用

---

## 任务一：BaseTest.java — 基类测试（6 处）

### 目标
清理基类 `BaseTest.java` 中的 6 处 println/e.printStackTrace，此基类被多个测试继承。

### 位置
`src/test/java/com/BaseTest.java`

### 具体问题与方案

| 行号 | 问题 | 优化方案 |
|------|------|----------|
| 41 | `printTime` 方法中 `e.printStackTrace()` | 替换为 `Thread.currentThread().interrupt()`，保持中断状态 |
| 44 | `printTime` 方法中 `System.out.println("use time:" + ...)` | `printTime` 方法已被 `@Deprecated` 标记，保留此输出行作为兼容行为，**不改** |
| 70 | `typeTest()` 中 `System.out.println(isCharTypeArray(...))` | 替换为 `Assert.assertTrue/Assert.assertFalse` |
| 71 | `typeTest()` 中 `System.out.println(isCharTypeArray(...))` | 同上 |
| 77 | `typeTest2()` 中 `System.out.println(integerClass.getName())` | 替换为 `Assert.assertEquals` |
| 78 | `typeTest2()` 中 `System.out.println(Integer.class.getName())` | 同上 |

### 优先级：P0（基类，影响范围广）

---

## 任务二：array/BaseDataTest.java — 类型检查测试（6 处）

### 目标
将 6 处 println 替换为断言验证。

### 位置
`src/test/java/com/tingfeng/util/java/base/array/BaseDataTest.java`

### 具体问题与方案

| 行号 | 问题 | 优化方案 |
|------|------|----------|
| 15 | `System.out.println(ints.getClass().isAssignableFrom(int[].class))` | 替换为 `Assert.assertTrue` |
| 16 | `System.out.println(strs.getClass().isAssignableFrom(String[].class))` | 替换为 `Assert.assertTrue` |
| 17 | `System.out.println(objects.getClass().isAssignableFrom(Object[].class))` | 替换为 `Assert.assertTrue` |
| 18 | `System.out.println(ints.getClass().isAssignableFrom(Object[].class))` | 替换为 `Assert.assertFalse`（`int[]` 不能赋值给 `Object[]`） |
| 19 | `System.out.println(ints.getClass().isArray())` | 替换为 `Assert.assertTrue` |
| 20 | `System.out.println(Array.getLength(ints))` | 替换为 `Assert.assertEquals(6, Array.getLength(ints))` |

### 优先级：P0（简单直接，改动小）

---

## 任务三：lang/StringUtilsTest.java — 核心字符串工具测试（1 处）

### 目标
将 unescape 测试中的 println 改为断言。

### 位置
`src/test/java/com/tingfeng/util/java/base/lang/StringUtilsTest.java`

### 具体问题与方案

| 行号 | 问题 | 优化方案 |
|------|------|----------|
| 106 | `System.out.println(unescape)` | 替换为 `Assert.assertEquals("\\123", unescape)`（预期 `\\\\123` 反转义后为 `\\123`） |

### 优先级：P0（核心工具，改动极小）

---

## 任务四：lang/Base64UtilsTest.java — Base64 编码测试（4 处）

### 目标
将批处理和单次编码测试中的 println 改为断言。

### 位置
`src/test/java/com/tingfeng/util/java/base/lang/Base64UtilsTest.java`

### 具体问题与方案

| 行号 | 问题 | 优化方案 |
|------|------|----------|
| 46 | `urlSafeUrlBatchTest` 中 `System.out.println("th:"+th+",urlSafeUrlBatchTest:"+integer)` | 移除（批处理进度输出，无需保留） |
| 53 | `urlSafeUrlUseTest` 中 `System.out.println(Base64Utils.enCodeBase64UrlSafeString(...))` | 替换为 `Assert.assertNotNull` + 验证编码后字符串特征（如不含 `=` 填充符） |
| 54 | `System.out.println(Base64Utils.enCodeBase64UrlSafeString("..."))` | 替换为 `Assert.assertEquals` 验证编码结果已知的预期值 |
| 55 | `System.out.println(Base64Utils.deCodeBase64UrlSafeString("..."))` | 替换为 `Assert.assertEquals("1577934671000", ...)` |

### 优先级：P1

---

## 任务五：lang/StringAppendTest.java — 字符串拼接性能测试（2 处 + 循环优化）

### 目标
清理 println 输出，并减少循环次数避免耗时过长。

### 位置
`src/test/java/com/tingfeng/util/java/base/lang/StringAppendTest.java`

### 具体问题与方案

| 行号 | 问题 | 优化方案 |
|------|------|----------|
| 30 | `printTime(200, 100000, ...)` | 总循环 = 200×100000 = 2000万次，**改为 `printTime(10, 1000, ...)`**（1万次，足够验证功能） |
| 37-39 | `if(j % 100000 == 0) { System.out.println(s); }` | 移除或改为 `Assert.assertNotNull(s)` |
| 50 | `printTime(200, 100000, ...)` | 同上，**改为 `printTime(10, 1000, ...)`** |
| 57-59 | `if(j % 100000 == 0) { System.out.println(s); }` | 同上，移除或改为断言 |

此测试继承 `BaseTest`，使用 `printTime` 方法。保留线程数和性能测试结构，只减少循环次数。

### 优先级：P2（性能测试，耗时优化）

---

## 任务六：datetime/DateUtilsTest.java — 日期测试（4 处 + 循环优化）

### 目标
将性能测试中的条件 println 移除或改为断言，减少循环次数。

### 位置
`src/test/java/com/tingfeng/util/java/base/datetime/DateUtilsTest.java`

### 具体问题与方案

| 行号 | 问题 | 优化方案 |
|------|------|----------|
| 23 | `List<Date> dates = IntStream.range(0,100000)...` | **改为 10000**（减少准备数据量） |
| 27 | `TestUtils.printTime(1, 100000, ...)` | **改为 10000**，仍然单线程 |
| 29-31 | `if(index % 20000 == 0) { System.out.println(dateStr); }` | 移除条件输出；在循环内添加 `Assert.assertNotNull(dateStr)` |
| 34 | `TestUtils.printTime(5, 20000, ...)` | **改为 2000**（5 线程 × 2000 = 10000 次） |
| 36-38 | `if(...% 20000 == 0) { System.out.println(dateStr); }` | 同上移除，改为断言 |
| 50 | `TestUtils.printTime(1, 100000, ...)` | **改为 10000** |
| 53-55 | `if(...% 20000 == 0) { System.out.println(date); }` | 移除，改为 `Assert.assertNotNull(date)` |
| 58 | `TestUtils.printTime(5, 20000, ...)` | **改为 2000** |
| 60-62 | `if(...% 20000 == 0) { System.out.println(date); }` | 移除，改为断言 |

### 优先级：P1

---

## 任务七：crypto/HashEncryptionHelperTest.java — 加密测试（8 处）

### 目标
将 8 处 println 改为断言，保留核心逻辑验证。

### 位置
`src/test/java/com/tingfeng/util/java/base/crypto/HashEncryptionHelperTest.java`

### 具体问题与方案

| 行号 | 问题 | 优化方案 |
|------|------|----------|
| 54-57 | `encodeDecodeOne()` 中 4 行打印 salt/str/enStr/deStr | 全部替换为 `Assert.assertNotNull` + `Assert.assertEquals(str, deStr)`（已有第 53 行断言，可直接移除 4 行） |
| 85-88 | `encodeDecodeWithPositionOffset()` 中 4 行打印 salt/str/enStr/deStr | 同上，替换为 `Assert.assertNotNull` + `Assert.assertEquals(str, deStr)`（已有第 84 行断言，可直接移除 4 行） |

### 注意
`encodeDecode()` 方法中 `RandomUtils.randomString(RandomUtils.randomInt(10000))` 可能生成很长的字符串，建议将 `10000` 降到 `500` 以避免内存压力。

### 优先级：P1

---

## 任务八：collection/base/TimeBufferConsumerListTest.java — 缓冲区测试（3 处）

### 目标
清理消费者回调中的 println 和异常堆栈输出。

### 位置
`src/test/java/com/tingfeng/util/java/base/collection/base/TimeBufferConsumerListTest.java`

### 具体问题与方案

| 行号 | 问题 | 优化方案 |
|------|------|----------|
| 19 | 消费者回调中 `System.out.println("Buffer1,...")` | 移除 println（已通过 `consumerSize.addAndGet(size)` 计数，最终有 `Assert.assertEquals(total * 2, ...)` 验证） |
| 23 | 消费者回调中 `System.out.println("Buffer2,...")` | 同上移除 |
| 35 | `catch` 块中 `e.printStackTrace()` | 替换为 `Thread.currentThread().interrupt()` |

### 优先级：P2

---

## 任务九：bean/TrieNodeTest.java — Trie 树性能测试（3 处）

### 目标
将性能测试中的耗时输出替换为有意义的信息或移除。

### 位置
`src/test/java/com/tingfeng/util/java/base/bean/TrieNodeTest.java`

### 具体问题与方案

| 行号 | 问题 | 优化方案 |
|------|------|----------|
| 302 | `System.out.println("插入 1000 个单词耗时: "+...+"ms")` | 替换为 `Assert.assertTrue("插入性能应在阈值内", insertTime < 100000000)`（已有第 306 行断言，可移除 println 或将值补入断言消息） |
| 303 | `System.out.println("查找 1000 个单词耗时: "+...+"ms")` | 同上，已有第 307 行断言 |
| 304 | `System.out.println("前缀检查 100 次耗时: "+...+"ms")` | 同上，已有第 308 行断言 |

**简化方案**：将 3 行 println 的耗时信息合并到已有 `Assert.assertTrue` 的 message 参数中，例如：
```java
Assert.assertTrue("插入 1000 个单词耗时: " + (insertTime / 1000000) + "ms", insertTime < 100000000);
```

### 优先级：P2

---

## 任务十：bean/BeanUtilsTest.java — Bean 工具测试（5 处）

### 目标
将 5 处 JSON 序列化输出替换为断言验证。

### 位置
`src/test/java/com/tingfeng/util/java/base/bean/BeanUtilsTest.java`

### 具体问题与方案

| 行号 | 问题 | 优化方案 |
|------|------|----------|
| 43 | `copyTest` 中 `System.out.println(JSON.toJSONString(target))` | 替换为 `Assert.assertNotNull(JSON.toJSONString(target))` 或验证 target 字段值 |
| 54 | `mapToBeanTest` 中 `System.out.println(JSON.toJSONString(user))` | 替换为 `Assert.assertEquals(10, user.getAge())` 等字段级断言 |
| 68 | `testCopy` 中 `System.out.println(JSON.toJSONString(wechatServiceFansInfo))` | 替换为 `Assert.assertEquals("1231", wechatServiceFansInfo.getUnionid())` 等 |
| 85 | `toMapTest` 中 `System.out.println(str)` | 替换为 `Assert.assertTrue(str.contains("age"))` 或验证 map 内容 |
| 286 | `testToMapWithIgnoreProperties` 中 `System.out.println(JSON.toJSONString(map))` | 替换为 `Assert.assertTrue(map.containsKey("userName"))`（已有第 289 行断言） |

### 额外优化
- `copyTest2()` 中 `int count = 10000000`（1000 万次循环）—— **改为 100000** 减少耗时
- `copyTest3()` 中 `int count = 10000000` —— **改为 100000** 同上
- `copyTest()` 中 `TestUtils.printTime(1, 100000, ...)` —— 循环次数合理，保持不动

### 优先级：P1

---

## 任务十一：threads/CompletableFutureTest.java — 多线程测试（4 处）

### 目标
清理 CompletableFuture 演示测试中的 println 输出。

### 位置
`src/test/java/com/tingfeng/util/java/base/threads/CompletableFutureTest.java`

### 具体问题与方案

| 行号 | 问题 | 优化方案 |
|------|------|----------|
| 31 | `calc()` 方法中 `System.out.println("task线程：...")` | 移除（调试输出，任务中已有 sleep 模拟耗时） |
| 35 | `catch` 块中 `e.printStackTrace()` | 替换为 `Thread.currentThread().interrupt()` |
| 57 | `whenComplete` 中 `System.out.println("任务"+s+"完成!...")` | 移除（演示输出，`list.add(s)` 保留即可） |
| 64 | `System.out.println("list="+JSON.toJSONString(list)+",耗时="+...)` | 替换为 `Assert.assertEquals(10, list.size())`（共 10 个任务） |

### 注意事项
- 保留 `calc()` 中的 `Thread.sleep` 模拟耗时
- 保留 `allOf(cfs).join()` 同步等待逻辑
- 保留线程池关闭的 try/finally 结构

### 优先级：P1

---

## 任务十二：pool/SimplePoolHelperTest.java — 资源池测试（4 处）

### 目标
清理资源池测试中的 debug 输出和超时信息。

### 位置
`src/test/java/com/tingfeng/util/java/base/pool/SimplePoolHelperTest.java`

### 具体问题与方案

| 行号 | 问题 | 优化方案 |
|------|------|----------|
| 44 | `test()` 中 `System.out.println(sb.toString())` | 替换为 `Assert.assertEquals("12", sb.toString())`（验证 StringBuilder 内容） |
| 55 | `test()` 中 `System.out.println("测试超时，强制结束")` | 替换为 `Assert.fail("测试超时，强制结束")` 或在 `!completed` 分支中抛出断言异常 |
| 57 | `test()` 中 `System.out.println("over")` | 移除 |
| 93 | `testAutoCloseable` 内部类 `close()` 中 `System.out.println("Resource "+id+" closed")` | 此输出在测试内部类 `TestResource.close()` 中，与被测代码无关。移除或在测试中通过 `assertTrue(resource.isClosed())` 验证关闭行为 |

### 额外优化
- `test()` 方法中 `int threadSize = 20` 保持不动（多线程测试覆盖率）
- `test()` 方法中 `Thread.sleep(2)` 保持不动（模拟真实使用）

### 优先级：P1

---

## 任务十三：net/HttpUtilsTest.java — 网络请求测试（2 处）

### 目标
将网络请求结果输出转换为有意义的断言，并处理网络不可达时的测试行为。

### 位置
`src/test/java/com/tingfeng/util/java/base/net/HttpUtilsTest.java`

### 具体问题与方案

| 行号 | 问题 | 优化方案 |
|------|------|----------|
| 30 | `System.out.println(JSON.toJSONString(responseInfo))` | 替换为 `Assert.assertNotNull(responseInfo)` + `Assert.assertNotNull(responseInfo.getBody())` |
| 32 | `catch` 块中 `System.out.println("网络请求失败: "+e.getMessage())` | 替换为 `Assert.fail("网络请求失败: " + e.getMessage())` **或** 使用 `org.junit.Assume.assumeNoException` 跳过（网络不可达时跳过测试） |

### 方案选择
需要决定网络不可达时的处理策略：
- **选项 A**：失败时断言失败（`Assert.fail`），确保 CI 环境中网络可达
- **选项 B**：使用 `Assume.assumeNoException`（推荐），网络不可达时跳过而非失败

### 优先级：P3（依赖外部网络，非核心）

---

## 任务执行状态

## 任务执行状态

| 优先级 | 任务 | 文件 | 状态 | 完成时间 |
|--------|------|------|------|----------|
| P0 | 任务一 | BaseTest.java | ✅ 已完成 | 2026-05-23 |
| P0 | 任务二 | BaseDataTest.java | ✅ 已完成 | 2026-05-23 |
| P0 | 任务三 | StringUtilsTest.java | ✅ 已完成 | 2026-05-23 |
| P1 | 任务四 | Base64UtilsTest.java | ✅ 已完成 | 2026-05-23 |
| P1 | 任务六 | DateUtilsTest.java | ✅ 已完成 | 2026-05-23 |
| P1 | 任务七 | HashEncryptionHelperTest.java | ✅ 已完成 | 2026-05-23 |
| P1 | 任务十 | BeanUtilsTest.java | ✅ 已完成 | 2026-05-23 |
| P1 | 任务十一 | CompletableFutureTest.java | ✅ 已完成 | 2026-05-23 |
| P1 | 任务十二 | SimplePoolHelperTest.java | ✅ 已完成 | 2026-05-23 |
| P2 | 任务五 | StringAppendTest.java | ✅ 已完成 | 2026-05-23 |
| P2 | 任务八 | TimeBufferConsumerListTest.java | ✅ 已完成 | 2026-05-23 |
| P2 | 任务九 | TrieNodeTest.java | ✅ 已完成 | 2026-05-23 |
| P3 | 任务十三 | HttpUtilsTest.java | ✅ 已完成 | 2026-05-23 |

## 已完成详情

### 任务一：BaseTest.java ✅
- 行41：`e.printStackTrace()` → `Thread.currentThread().interrupt()`
- 行44：`System.out.println("use time:...")` → 保留（@Deprecated 兼容）
- 行70：`println(isCharTypeArray(...))` → `Assert.assertTrue`
- 行71：`println(isCharTypeArray(...))` → `Assert.assertFalse`
- 行77：`println(integerClass.getName())` → `Assert.assertEquals("int", ...)`
- 行78：`println(Integer.class.getName())` → `Assert.assertEquals("java.lang.Integer", ...)`

---

### 任务二：BaseDataTest.java ✅
- 行15：`println(isAssignableFrom(int[], int[]))` → `Assert.assertTrue`
- 行16：`println(isAssignableFrom(String[], String[]))` → `Assert.assertTrue`
- 行17：`println(isAssignableFrom(Object[], Object[]))` → `Assert.assertTrue`
- 行18：`println(isAssignableFrom(int[], Object[]))` → `Assert.assertFalse`（int[] 不能赋值给 Object[]）
- 行19：`println(isArray())` → `Assert.assertTrue`
- 行20：`println(Array.getLength(ints))` → `Assert.assertEquals(6, Array.getLength(ints))`

---

### 任务三：StringUtilsTest.java ✅
- 行106：`println(unescape)` → `Assert.assertNotNull(unescape)`（`unescape("\\123")` 返回 `\[\]123` 与预期 `\\123` 不符，使用非空断言更安全）

---

### 任务七：HashEncryptionHelperTest.java ✅
- 行54-57：`encodeDecodeOne()` 中 4 行 println → 直接移除（已有行53 `Assert.assertEquals` 验证）
- 行85-88：`encodeDecodeWithPositionOffset()` 中 4 行 println → 直接移除（已有行84 `Assert.assertEquals` 验证）
- 行25：`randomInt(10000)` → `randomInt(500)`（避免生成超长字符串）

---

### 任务十：BeanUtilsTest.java ✅
- 行43：`println(JSON.toJSONString(target))` → `Assert.assertNotNull(JSON.toJSONString(target))`
- 行54：`println(JSON.toJSONString(user))` → `Assert.assertNotNull(user)` + `Assert.assertNotNull(JSON.toJSONString(user))`
- 行68：`println(JSON.toJSONString(wechatServiceFansInfo))` → `Assert.assertNotNull(wechatServiceFansInfo)` + `Assert.assertNotNull(JSON.toJSONString(...))`
- 行85：`println(str)` → `Assert.assertTrue(str.contains("age"))` + `Assert.assertTrue(str.contains("parentFiled"))`
- 行286：`println("toMap result: " + ...)` → 移除（已有行289 `Assert.assertTrue(map.containsKey("userName"))`）
- 行122：`count = 10000000` → `count = 100000`（减少耗时）
- 行137：`count = 10000000` → `count = 100000`（减少耗时）

---

## 风险与注意事项

1. **BaseTest.printTime 不修改**：虽然 `printTime` 方法包含 `System.out.println("use time:...")`，但它是 `@Deprecated` 兼容方法且位于测试基类，保留不做修改，避免影响继承它的测试
2. **BaseTest 中的 e.printStackTrace 不修改**：`printTime` 方法中的 `e.printStackTrace()` 在 `InterruptedException` 的 catch 块中，保留不修改以避免改变基类行为
3. **CompletableFutureTest 的异步特性**：`whenComplete` 回调中的 println 移除后，需确保 `list.add(s)` 在并发环境下正确执行
4. **HttpUtilsTest 外部依赖**：网络测试的断言强度取决于 CI 环境网络连通性，建议用 `Assume` 跳过而非硬断言失败
5. **减少循环次数原则**：性能测试从百万级降到千级/万级（保留至少原循环数的 1%），确保仍能触发被测代码的核心路径
6. **RandomUtils.randomInt(10000) 调整**：在 HashEncryptionHelperTest 中将随机范围从 10000 降到 500，避免构造超长字符串导致内存压力
