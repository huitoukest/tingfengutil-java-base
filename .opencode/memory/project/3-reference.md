# 项目资料 — tingfengutil-java-base

## 命令手册

| 用途 | 命令 | 说明 |
|------|------|------|
| 编译打包 | `mvn clean package -DskipTests` | 编译跳过测试 |
| 运行测试 | `mvn test` | 运行全部单元测试 |
| 编译 | `mvn compile` | 编译源码 |
| 安装到本地 | `mvn clean install -DskipTests` | 本地 jar 安装 |
| 打包源码 | `mvn source:jar-no-fork` | 生成源码 jar |

## ⚠️ 注意事项 / 已知陷阱

- 🔴 **`lang/ex/` 目录已不存在（2026-08 确认）** — 勿再引用；`lang/exception/` 为现行异常包
- 🔴 **TestTimeoutException 位于 `lang/exception/test/`** — 现行异常包的 test 子包中，可直接引用
- 🔴 **Constants.java 接口常量** — 遗留代码，不要新增接口常量。新常量使用 `final class + private 构造器`
- 🔴 **部分接口保留 I 前缀/后缀** — 如 IEnum, ConvertI。新接口统一不要 `I`
- 🔴 **部分文件使用 Tab 缩进** — 新旧文件混用。修改时统一改为 4 空格
- 🔴 **pom.xml 版本锁定** — Java 1.8、Lombok 1.18.24、SLF4J 1.7.25 等关键版本不可随意升级
- 🔴 **commons-logging 1.2 仅 WebServiceUtils 使用** — 新增代码应避免引入更多第三方依赖
- 🔴 **WebServiceUtils 有重试逻辑** — postToWebService 递归重试，注意递归深度

- 🔴 **`ReadWriteArrayList.removeIf()` 无效** — 因其 `iterator()` 返回 `new ArrayList<>(list)` 快照副本，继承自 `Collection` 默认实现的 `removeIf()` 操作的是副本而非底层列表。应使用索引逆序遍历 + `remove(int)` 替代
- 🔴 **`ReadWriteArrayList.sort()` 可用** — 已显式重写 `sort()`，可安全使用

## 修改红线

| 文件 | 约束 |
|------|------|
| `pom.xml` | 不改版本号（Java 1.8、Lombok、SLF4J、JUnit 等） |
| `pom.xml` | 不改 packaging=jar |
| `src/main/java/com/tingfeng/util/java/base/common/constant/Constants.java` | 不新增接口常量，保持遗留不动 |

## 关键文件索引

| 文件 | 用途 |
|------|------|
| `pom.xml` | 项目构建配置 |
| `src/main/java/com/tingfeng/util/java/base/LogUtils.java` | 统一日志入口 |
| `src/main/java/com/tingfeng/util/java/base/lang/StringUtils.java` | 核心字符串工具 |
| `src/main/java/com/tingfeng/util/java/base/lang/exception/BaseException.java` | 异常基类 |
| `src/main/java/com/tingfeng/util/java/base/common/constant/Constants.java` | 全局常量 |
| `src/main/java/com/tingfeng/util/java/base/bean/converter/Converter.java` | 类型转换器接口 |
| `src/main/java/com/tingfeng/util/java/base/common/ReadMe.java` | 开发约定文档 |
| `CLAUDE.md` | AI 协作规范 |

## 环境要求

| 项 | 要求 |
|----|------|
| JDK | 1.8+ |
| Maven | 3.x |
| 编码 | UTF-8 |
| 内存（测试） | -Xmx512m（见 surefire 配置） |
