# 代码风格 — tingfengutil-java-base

## 命名规范

| 元素 | 规范 | 示例 | 备注 |
|------|------|------|------|
| 类名 | UpperCamelCase | StringUtils, LogUtils, BeanUtils | 工具类命名优先用 Utils/Helper |
| 方法名 | lowerCamelCase | getValue, isEmpty, toLowerFirstChar | 转换方法用 `getAByB`/`toXXX` |
| 常量 | UPPER_SNAKE_CASE | MAX_EXPORTCOUNT, KEY_HTTP | 见 ReadMe.java 第 13 行 |
| 变量 | lowerCamelCase | srcString, tempList | - |
| 枚举 | UpperCamelCase | CacheType, AlgorithmType | 枚举成员全大写 |
| 异常 | XxxException | BaseException, InfoException | 见 lang/exception/ |
| 接口 | UpperCamelCase（无 I 前缀/后缀） | Converter, Traverse | 遗留如 IEnum/ConvertI 暂不改 |
| 抽象类 | AbstractXxx / BaseXxx | AbstractFraction, BaseException | - |
| 测试类 | XxxTest | StringUtilsTest, BeanUtilsTest | 包级匹配 |

## 代码格式

| 项目 | 规则 |
|------|------|
| 缩进 | 4 空格（部分遗留文件用 Tab，优先 4 空格） |
| 大括号 | K&R 风格（左大括号前不换行，左大括号后换行） |
| 行宽 | 不超过 120 字符 |
| 编码 | UTF-8 |
| import 分组 | 先项目内 > 再 JDK > 再第三方 |
| 注释 | Javadoc `/** ... */`，禁止 `//` 作为类/方法注释 |

## 各层编写规范

### 工具类（Utils）— 静态方法模式

```java
public final class StringUtils {
    private StringUtils() {}  // 私有构造器禁止实例化

    public static boolean isEmpty(String value) {
        return value == null || value.trim().length() < 1;
    }
}
```

### 辅助类（Helper）— 实例化调用模式

```java
public class JudgeEmptyHelper {
    private final boolean recursive;
    private final boolean trim;

    private JudgeEmptyHelper(boolean recursive, boolean trim) {
        this.recursive = recursive;
        this.trim = trim;
    }

    public static JudgeEmptyHelper newInstance(boolean recursive, boolean trim) {
        return new JudgeEmptyHelper(recursive, trim);
    }

    public boolean dealMap(Map<?, ?> map) {
        // ...
    }
}
```

### 转换器（Converter）— SPI 接口

```java
public interface Converter<S, T> {
    T convert(S source);
    Class<S> getSourceType();
    Class<T> getTargetType();
}
```

### 异常定义 — 继承 BaseException

```java
public class InfoException extends BaseException {
    public InfoException(String message) {
        super(message);
    }
}
```

### 常量定义 — 接口反模式（仅限 legacy）

```java
// 遗留风格，保持不动。新常量推荐用 final class + private 构造器
public interface Constants {
    interface Symbol {
        char dot = '.';
        String comma = ",";
    }
}
```

## 特殊约定

| 约定 | 说明 |
|------|------|
| Utils vs Helper | Utils = 纯静态方法；Helper = 需实例化的上下文类 |
| 返回值风格 | `getXXX` 返回解析结果，解析失败/空值返回 defaultValue |
| 方法参数顺序 | `value, emptyValue, defaultValue` 三参数模式 |
| Rabbit 模式 | 三参 -> 二参 -> 一参重载链 |
| 转换方法命名 | `getAByB()` 或 `toXXX()`，视与哪个类更紧密 |
| StringBuilder 池 | 用 `FixedPoolHelper` 复用 StringBuilder，避免频繁创建 |
| 日志 | 统一通过 `LogUtils`，SLF4J 可用时委托，否则 fallback 到 System.out |
| 私有构造器 | 所有工具类必须有 `private XxxUtils() {}` |
| 判空 | 优先用 `null == obj`（左侧常量）防御风格 |

## 已知待清理项

- `Constants.java` 接口常量风格遗留，保持不动
- 部分接口保留 `I` 前缀/后缀（IEnum, ConvertI），新接口不添加
