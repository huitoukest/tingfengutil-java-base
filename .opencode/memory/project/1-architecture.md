# 项目架构 — tingfengutil-java-base

## 项目性质

纯 Java SE 工具库（非 Web 项目），提供通用 Java 工具类集合。
打包方式：jar，无外部框架依赖（仅依赖 JDK 8 + 少量第三方）。

## 技术栈

| 框架/工具           | 版本      | 用途                                         |
| --------------- | ------- | ------------------------------------------ |
| Java            | 1.8     | 编译/源码级别                                    |
| Maven           | 3.x     | 构建管理                                       |
| JUnit           | 4.12    | 单元测试                                       |
| SLF4J + Log4j   | 1.7.25  | 日志门面（provided）                             |
| Lombok          | 1.18.24 | 代码生成（provided）                             |
| commons-logging | 1.2     | 废弃，后面统一使用SLF4J |
| fastjson        | 1.2.49  | 仅测试使用                                      |

## 物理结构

包根路径：`com.tingfeng.util.java.base`

```
com.tingfeng.util.java.base/
├── LogUtils.java                     ← 日志工具（顶层快捷入口）
├── array/                            ← 数组操作
├── bean/                             ← Bean 操作 + 类型转换器引擎
│   ├── BeanUtils.java
│   ├── base/                         ← 底层接口/基类
│   ├── converter/                    ← 类型转换器框架（SPI 式注册）
│   │   └── defaults/                 ← 内建类型转换器
├── cache/                            ← 简单内存缓存
│   └── base/                         ← 缓存底层数据结构
├── collection/                       ← 集合操作 + 树结构 + 组合
│   ├── base/                         ← 自定义集合结构（FastMap, TreeNode 等）
│   └── support/tree/                 ← 树的遍历策略
├── common/                           ← 公共基础设施
│   ├── constant/                     ← 常量、枚举、类型定义
│   │   └── ...Constants.java         ← 按领域分组的常量集合
│   ├── collection/                   ← 线程安全集合
│   └── utils/                        ← 通用测试/线程工具
├── concurrent/                       ← 并发工具
│   └── base/                         ← NamedThreadFactory
├── crypto/                           ← 加密/哈希工具
├── database/                         ← 数据库连接工具（hbase/mysql）
├── datetime/                         ← 日期时间工具
├── db/                               ← JDBC 连接工厂/封装
├── file/                             ← 文件操作
├── format/                           ← 格式化
├── function/                         ← 函数式接口（Consumer1~5, Function1~5）
├── gis/                              ← 地理信息工具
├── io/                               ← IO/CSV 操作
│   └── base/                         ← CSV 参数封装
├── lang/                             ← 核心语言工具（最大包）
│   ├── base/                         ← 基础数据结构/接口（Tuple, Trie, IEnum 等）
│   ├── ex/                           ← ⚠️ 遗留异常包（已废弃，使用 exception/）
│   ├── exception/                    ← 业务异常体系
│   ├── fun/                          ← CacheSupplier
│   ├── inter/                        ← 回调/函数接口体系
│   │   ├── consumer/                 ← 多参 Consumer（2~10 参数）
│   │   ├── returnfunction/           ← 多参 Function（1~10 参数，含 R 变体）
│   │   └── voidfunction/            ← 多参 Void 函数
│   └── support/                      ← 反射/泛型工具
├── math/                             ← 数学/分数/随机数
│   └── base/                         ← 分数（Fraction）体系
├── net/                              ← HTTP 工具
│   └── base/
├── pool/                             ← 对象池
│   └── base/
├── text/                             ← HTML/正则/字符串模板
├── validate/                         ← 判空工具
└── web/                              ← WebService 工具
    └── utils/
```

## 模块职责表

| 路径                 | 职责                                        | 命名风格                                                         |
| ------------------ | ----------------------------------------- | ------------------------------------------------------------ |
| `lang/base/`       | 通用数据结构、基础接口（Tuple, Trie, IEnum, ConvertI） | 接口：`I` 前缀/后缀（遗留）                                             |
| `lang/exception/`  | 业务异常体系（BaseException, InfoException 等）    | `XxxException`                                               |
| `lang/inter/`      | 函数式接口（Consumer/Function 各参数变体）            | `Consumer[N]`, `Function[N]`, `FunctionV[N]`, `FunctionR[N]` |
| `lang/support/`    | 反射/泛型辅助工具                                 | `XxxUtils`                                                   |
| `bean/converter/`  | 类型转换器 SPI 框架                              | `Converter`, `XxxConverter`                                  |
| `collection/`      | 集合工具 + 树结构 + 组合                           | `XxxUtils` / `XxxHelper`                                     |
| `common/constant/` | 常量接口（遗留风格，保持不动）                           | 接口名 + 子接口分组                                                  |

## 逻辑架构

纯工具库，无分层调用链。调用关系：

```
Controller（无，纯库无 Web）
  → 业务层（无）
    → Utils 静态方法 / Helper 实例方法
      → JDK 原生 API
```

工具类内部调用链示例：

```
StringUtils.getInteger(value)
  → StringUtils.getValue(value, emptyValue, defaultValue, convert)
    → FunctionROne.run(str) → Integer.parseInt(str)
```

转换器调用链：

```
BeanUtils.convert(source, targetType)
  → ConverterRegistry.findConverter(sourceType, targetType)
    → Converter.convert(source)
      → 各 DefaultConverters 按类型匹配
```
