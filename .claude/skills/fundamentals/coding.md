# 编码标准

## 优先级

1. **规范与质量** - 补全工具类、修复Bug、添加测试
2. **复用优先** - 优先使用项目现有工具，禁止魔法数字
3. **代码清理** - 移除无用代码/导入
4. **Java 8特性** - Lambda/try-with-resources
5. **设计先行** - 先设计方法签名，再实现

## 命名规范

| 类型 | 规则 | 示例 |
|------|------|------|
| 包名 | 全小写单数名词 | `com.company.utils` |
| 类名 | UpperCamelCase，Utils/Helper/I后缀 | `StringUtils`, `PoolHelper`, `ConvertI` |
| 方法名 | lowerCamelCase | `toByteArray`, `readLines` |
| 变量名 | lowerCamelCase，集合加复数 | `users`, `userList` |
| 常量 | 全大写下划线 | `MAX_BUFFER_SIZE` |

**禁止：**
- 缩写（除URL/ID/DTO/VO等业界通用）
- 布尔返回值用`get`开头（用`is`/`has`/`contains`）

## 代码风格

### 缩进与空格
- 4空格缩进，禁止Tab
- 二元运算符两侧空格：`a + b`
- 逗号后空格：`method(a, b, c)`

### 大括号
```java
if (condition) {
    doSomething();
} else {
    doOther();
}
```

### import顺序
1. `java` → `javax` → 第三方 → 项目内部
2. 禁止通配符：`import java.util.*`

## 异常处理

### 必须
- 使用自定义RuntimeException，不抛检查型异常
- 精确捕获具体异常类型
- 必须使用try-with-resources

### 禁止
```java
// 禁止：空catch或仅打印日志
} catch (Exception e) {
}

// 禁止：catch(Exception/Throwable)
// 除非顶层统一处理

// 禁止：用异常做流程控制
```

## 资源管理

```java
// 必须：try-with-resources
try (InputStream is = new FileInputStream(file)) {
    // use stream
}

// 禁止：finally中关闭可能为null的资源
```

## 并发

- `ThreadLocal`使用后必须`remove()`
- 禁止在Servlet/Controller中创建线程
- 线程池使用`ThreadPoolExecutor`，拒绝策略`CallerRunsPolicy`/`AbortPolicy`

## 注释规范

```java
/**
 * 类功能描述
 * @author authorName
 */
public class XxxUtils {

    /**
     * 方法功能描述
     * @param paramName 参数说明
     * @return 返回值说明
     * @throws XxxException 异常说明
     */
}

/**
 * TODO[huitoukest 2026-04-06]: 优化为批量处理
 */
```

## 安全

- 所有外部输入必须校验
- SQL禁止拼接，必须参数化
- 禁止硬编码密码/密钥/Token
- 敏感信息禁止日志打印

## CheckList

- [ ] 命名符合规范
- [ ] 无魔法数字/字符串
- [ ] 无无用import
- [ ] 复用现有工具类
- [ ] 参数校验
- [ ] 资源try-with-resources
- [ ] 共享变量线程安全
- [ ] 输入校验
