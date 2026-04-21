# Converter 模块待实现任务

> 更新：2026-04-21 — BigNumberConverters 已重新分类

## 已完成的重新分类

**分类原则（2026-04-21 确立）**：「只有 src 的类型是 xxx 时，对应的 Converter 的功能才归类到 xxx 类」

| 分类文件 | 源类型 | 状态 |
|---------|--------|------|
| `StringConverters` | `String` | ✅ 已补充 String→BigDecimal/Integer |
| `NumberConverters` | `Number` 及其子类 | ✅ 已补充 Number→BigDecimal/BigInteger |
| `BigNumberConverters` | `BigDecimal`/`BigInteger` | ✅ 已修正为只保留源为 BigXxx 的转换 |
| `DateTimeConverters` | `Date`/`LocalDateTime`/`LocalDate` | 待检查 |
| `ByteArrayConverters` | `byte[]` | 待检查 |
| `CollectionConverters` | `Object[]`/`List`/primitive array | 待检查 |
| `UrlConverters` | `String` | 待检查 |



> 创建时间：2026-04-21
> 优先级说明：P1=高频必需，P2=常用，P3=补充

---

## P1 - 高频基础类型

### 1. BigNumberConverters（BigDecimal / BigInteger 转换）

**文件路径**：`src/main/java/com/tingfeng/util/java/base/bean/converter/defaults/BigNumberConverters.java`

**待实现转换**：

| 源类型 | 目标类型 | 条件 |
|-------|---------|------|
| String | BigDecimal | 合法数字格式 |
| String | BigInteger | 合法整型格式 |
| BigDecimal | BigInteger | 四舍五入 |
| BigDecimal | String | toPlainString() |
| BigInteger | BigDecimal | valueOf() |
| BigInteger | String | toString() |
| Number | BigDecimal | 精度转换 |
| BigDecimal | BigDecimal | 深度克隆（备选） |
| String | BigInteger | 进制支持（2/8/10/16）|

**设计要点**：
- `BigDecimal` 的 `String → BigDecimal` 需处理科学计数法（如 `"1.23e-5"`）
- `String → BigInteger` 需支持 `0x` 前缀（十六进制）
- 需新增 `BigNumberConverters.register(registry)` 到 `DefaultConverters.registerDefaults()`

---

### 2. EnumConverters（枚举转换）

**文件路径**：`src/main/java/com/tingfeng/util/java/base/bean/converter/defaults/EnumConverters.java`

**待实现转换**：

| 源类型 | 目标类型 | 条件/实现 |
|-------|---------|---------|
| String | Enum | `Enum.valueOf(clazz, name)`，不区分大小写 |
| String | Enum | `Enum.valueOf(clazz, name)`，区分大小写 |
| Integer | Enum | `Enum.values()[ordinal]` |
| Enum | String | `enum.name()` |
| Enum | Integer | `enum.ordinal()` |
| String | Enum | 支持自定义 value 字段（备选，需泛型） |

**设计要点**：
- String → Enum 需区分 `valueOf`（大小写敏感）和宽松匹配（忽略大小写）两种策略
- 可通过 `ConditionConverter` 实现：先尝试精确匹配，失败再尝试忽略大小写匹配
- 由于 Java 泛型擦除，`Enum<E extends Enum<E>>` 的类型推断受限，需在运行时利用 `target.asSubclass(Enum.class)` 获取具体枚举类

---

## P2 - 常用集合与数组

### 3. CollectionElementConverters（集合元素互转）

**文件路径**：`src/main/java/com/tingfeng/util/java/base/bean/converter/defaults/CollectionElementConverters.java`

**待实现转换**：

| 源类型 | 目标类型 | 实现 |
|-------|---------|------|
| List | Set | `new HashSet<>(list)` |
| Set | List | `new ArrayList<>(set)` |
| Collection | Object[] | `collection.toArray()` |
| Object[] | Set | `new HashSet<>(Arrays.asList(arr))` |
| List | LinkedList | `new LinkedList<>(list)` |
| Set | LinkedHashSet | `new LinkedHashSet<>(set)` |
| SortedSet | List | `new ArrayList<>(sortedSet)` |

---

### 4. PrimitiveArrayConverters（primitive array ↔ String）

**文件路径**：`src/main/java/com/tingfeng/util/java/base/bean/converter/defaults/PrimitiveArrayConverters.java`

**待实现转换**：

| 源类型 | 目标类型 | 实现 |
|-------|---------|------|
| int[] | String | `"1,2,3"` |
| long[] | String | `"1,2,3"` |
| double[] | String | `"1.0,2.0,3.0"` |
| String | int[] | `split(",") → parseInt` |
| String | long[] | `split(",") → parseLong` |
| String | double[] | `split(",") → parseDouble` |
| String | int[] | 支持十六进制 `0xFF,0x00`（需条件判断） |

**设计要点**：
- 使用 `ConditionConverter` 区分十进制和十六进制字符串
- 分隔符默认为 `,`，可扩展支持空格、分号等

---

## P3 - 字符序列与 IO

### 5. CharSequenceConverters（字符序列互转）

**文件路径**：`src/main/java/com/tingfeng/util/java/base/bean/converter/defaults/CharSequenceConverters.java`

**待实现转换**：

| 源类型 | 目标类型 | 实现 |
|-------|---------|------|
| StringBuilder | String | `toString()` |
| StringBuffer | String | `toString()` |
| String | StringBuilder | `new StringBuilder(s)` |
| String | StringBuffer | `new StringBuffer(s)` |
| CharSequence | StringBuilder | `new StringBuilder(cs)` |
| CharSequence | StringBuffer | `new StringBuffer(cs)` |
| char[] | String | `String.valueOf(arr)` |
| String | char[] | `toCharArray()` |

---

### 6. StreamConverters（IO 流转换）

**文件路径**：`src/main/java/com/tingfeng/util/java/base/bean/converter/defaults/StreamConverters.java`

**待实现转换**：

| 源类型 | 目标类型 | 实现 |
|-------|---------|------|
| InputStream | byte[] | `IOUtils.toByteArray()` |
| InputStream | String | `IOUtils.toString()` |
| byte[] | InputStream | `new ByteArrayInputStream(bytes)` |
| String | InputStream | `new ByteArrayInputStream(bytes)` |
| Reader | String | `IOUtils.toString()` |
| File | String | `FileUtils.readFileToString()` |
| String | File | `FileUtils.writeStringToFile()` |
| URI | URL | `uri.toURL()` |
| URL | URI | `uri.toURI()` |

**设计要点**：
- 必须使用 `try-with-resources` 管理流
- 所有 IO 操作可能抛 `ConverterException`（将检查型异常包装为运行时异常）

---

## P4 - 其他常用类型

### 7. SqlDateTimeConverters（SQL 日期类型转换）

**文件路径**：`src/main/java/com/tingfeng/util/java/base/bean/converter/defaults/SqlDateTimeConverters.java`

**待实现转换**：

| 源类型 | 目标类型 | 实现 |
|-------|---------|------|
| String | java.sql.Date | `Date.valueOf(localDate)` |
| java.sql.Date | String | `date.toString()` |
| String | java.sql.Time | `Time.valueOf()` |
| java.sql.Time | String | `time.toString()` |
| String | java.sql.Timestamp | `Timestamp.valueOf()` |
| java.sql.Timestamp | String | `timestamp.toString()` |
| java.util.Date | java.sql.Date | `new Date(date.getTime())` |
| java.util.Date | java.sql.Timestamp | `new Timestamp(date.getTime())` |
| java.sql.Date | java.util.Date | `new Date(date.getTime())` |

---

### 8. LocaleConverters（国际化类型转换）

**文件路径**：`src/main/java/com/tingfeng/util/java/base/bean/converter/defaults/LocaleConverters.java`

**待实现转换**：

| 源类型 | 目标类型 | 实现 |
|-------|---------|------|
| String | Locale | `new Locale(String)` 或 `Locale.forLanguageTag()` |
| Locale | String | `locale.toString()` |
| String | TimeZone | `TimeZone.getTimeZone(zoneId)` |
| TimeZone | String | `timeZone.getID()` |

---

### 9. UriConverters（URI/URL 转换）

**文件路径**：`src/main/java/com/tingfeng/util/java/base/bean/converter/defaults/UriConverters.java`

**待实现转换**：

| 源类型 | 目标类型 | 实现 |
|-------|---------|------|
| String | URI | `URI.create(str)` |
| URI | String | `uri.toString()` |
| String | URL | `new URL(str)` |
| URL | String | `url.toString()` |
| URL | URI | `url.toURI()` |
| URI | URL | `uri.toURL()` |

---

### 10. InstantConverters（Java 8 新日期类型）

**文件路径**：`src/main/java/com/tingfeng/util/java/base/bean/converter/defaults/InstantConverters.java`

**待实现转换**：

| 源类型 | 目标类型 | 实现 |
|-------|---------|------|
| Date | Instant | `date.toInstant()` |
| Instant | Date | `Date.from(instant)` |
| Long | Instant | `Instant.ofEpochMilli(long)` |
| Instant | Long | `instant.toEpochMilli()` |
| String | Instant | `Instant.parse(str)` |
| Instant | String | `instant.toString()` |
| LocalDateTime | ZonedDateTime | `ldt.atZone(ZoneId.systemDefault())` |
| ZonedDateTime | LocalDateTime | `zdt.toLocalDateTime()` |

---

## 架构优化任务

### A. Number 类型层次查找优化

**问题**：当前 `findAll(Double.class, Integer.class)` 只能精确匹配 `UnionKey(Double, Integer)`，无法找到注册的 `UnionKey(Number, Integer)`。

**方案**：在 `findAll()` 或 `findConverters()` 中加入类型层次遍历——查找不到时，遍历 `source` 的父类/接口链，再查找是否有注册的转换器。

**影响文件**：`DefaultConverterRegistry.java`

---

### B. 自定义日期格式支持

**问题**：String ↔ Date/LocalDateTime/LocalDate 只能使用固定格式。

**方案**：通过 `ThreadLocal<DateFormat>` 或 `ThreadLocal<DateTimeFormatter>` 支持用户注册自定义格式的转换器（通过条件转换器匹配特定格式）。

**影响文件**：`DateTimeConverters.java`，新增 `CustomDateTimeConverters.java`

---

## 实现顺序建议

1. **BigNumberConverters** → P1，最常用
2. **EnumConverters** → P1，枚举是 Java 核心类型
3. **CollectionElementConverters** → P2，集合互转是常见需求
4. **CharSequenceConverters** → P3，简单且无依赖
5. **PrimitiveArrayConverters** → P2
6. **StreamConverters** → P3，需注意资源管理
7. **SqlDateTimeConverters** → P4，JDBC 用户常用
8. **LocaleConverters** → P4
9. **UriConverters** → P4
10. **InstantConverters** → P4，Java 8 日期补充

---

## 注意事项

1. 每个分类文件需在 `DefaultConverters.registerDefaults()` 中注册
2. 所有转换方法需做 null 输入检查
3. IO 相关转换必须使用 `try-with-resources`
4. 异常统一包装为 `ConverterException`（非检查型）
5. 新增分类需补充单元测试到 `DefaultConvertersTest.java`
