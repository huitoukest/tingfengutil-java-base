# 包结构规范

## 顶层包

```
com.tingfeng.util.java.base
```

## 模块划分

```
com.tingfeng.util.java.base/
├── common/                    # 通用工具
│   ├── utils/                 # 静态工具类
│   ├── bean/                  # 数据结构
│   ├── exception/             # 自定义异常
│   ├── helper/                # 实例类
│   ├── constant/              # 常量枚举
│   ├── inter/                 # 函数接口
│   └── annotation/            # 注解
│
├── file/                      # 文件工具
├── database/                  # 数据库工具
└── web/                       # Web工具
```

## common/utils 细分

| 目录 | 内容 | 示例 |
|------|------|------|
| `utils/` | 通用工具 | `CollectionUtils`, `ArrayUtils` |
| `utils/string/` | 字符串工具 | `StringUtils` |
| `utils/datetime/` | 日期工具 | `DateUtils`, `LocalDateUtils` |
| `utils/reflect/` | 反射工具 | `ReflectUtils`, `ClassUtils` |
| `utils/process/` | 进程工具 | `ProcessUtils` |

## 异常包结构

```
common/exception/
├── io/                        # IO相关异常
│   ├── IOException.java
│   └── FileNotFoundException.java
├── process/                   # 进程相关异常
│   ├── ProcessException.java
│   ├── ProcessStartException.java
│   ├── ProcessTimeoutException.java
│   └── ProcessExitCodeException.java
└── 数据库/Web等
```

## 依赖规则

1. **禁止上层依赖下层** - common 可被 file/web 依赖，反之不行
2. **禁止循环依赖** - common/utils 之间不可循环
3. **最小依赖** - 仅依赖 JDK + slf4j-log4j12

## 工具类 vs 实例类位置

| 类型 | 位置 | 说明 |
|------|------|------|
| Utils | `common/utils/` | 静态方法，无状态 |
| Helper | `common/helper/` | 实例方法，可有状态 |
| I接口 | `common/inter/` | 函数式接口 |
| Bean | `common/bean/` | 数据结构 |
