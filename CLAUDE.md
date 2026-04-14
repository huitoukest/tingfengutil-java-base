# CLAUDE.md

## 项目定位

**tingfengutil-java-base** 是 JDK 1.8 工具库，仅依赖 JDK + slf4j-log4j12，无第三方库。

## 构建

```bash
mvn clean compile
mvn test -Dtest=ClassName
mvn test -Dtest=ClassName#methodName
mvn package -DskipTests
```

## 包结构

```
src/main/java/com/tingfeng/util/java/base/
├── common/
│   ├── utils/        # 静态工具类（Utils后缀）
│   ├── bean/         # 数据结构
│   ├── exception/    # 自定义异常
│   ├── helper/       # 实例类（Helper后缀）
│   ├── constant/     # 常量枚举
│   ├── inter/        # 函数接口（I后缀）
│   └── annotation/   # 注解
├── file/             # 文件工具
├── database/         # 数据库工具
└── web/             # Web工具
```

## 命名规范

| 类型 | 规则 | 示例 |
|------|------|------|
| 工具类 | Utils/Util 后缀 | `StringUtils`, `DateUtils` |
| 实例类 | Helper 后缀 | `PoolHelper`, `PropertyHelper` |
| 接口 | I 后缀 | `ConvertI`, `PoolMemberActionI` |
| 转换方法 | `toB()` / `getAbyB()` | `IOUtils.toByteArray()` |

## 规范引用

- **编码标准**：`/skill coding-standards` - 优先级、设计原则
- **测试规范**：`/skill testing-guide` - 单元测试模式
- **架构原则**：`/skill architecture` - 类设计、模块划分
- **模块设计**：按需查阅 `/skill io-utils-design` 等

## 关键约束

1. **最小依赖** - 禁止引入 JavaEE/第三方库
2. **统一异常** - 抛出自定义 RuntimeException，不抛检查型异常
3. **资源管理** - 必须使用 try-with-resources
4. **工具类封闭** - 构造器私有，静态方法
5. **禁止自动提交** - AI自动修改代码后，禁止自动提交。提交代码必须由用户手动输入要求后触发
6. **1.0.0版本提交规范** - plan 1.0.0完成后，将之前1.0.0开始的AI提交记录合并为**一个**提交记录，提交信息为"1.0.0初始化"

## Maven

```xml
<dependency>
  <groupId>com.tingfeng</groupId>
  <artifactId>tingfengutil-java-base</artifactId>
  <version>0.2.6</version>
</dependency>
```
