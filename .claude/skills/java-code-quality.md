# Java 代码优化 Skill

## 优先级顺序（1 > 2 > 3 > 4 > 5）

### 1. 规范与质量
- 整理并遵循此方法可能涉及的 Java 规范及功能相关标准
- 完善工具类（补全场景、修复 Bug/性能）
- 添加注释及完备单元测试

### 2. 复用优先
- 优先使用项目现有工具类/常量
- 禁止重复实现或使用魔法数字/字符串
- 先查看 `src/main/java/com/tingfeng/util/java/base/common/utils/` 下是否有可复用工具

### 3. 代码清理
- 移除无用引入/代码
- 修正语法错误

### 4. 特性优化
- 在性能不降且不违反第 2 点（现有工具优先）前提下
- 使用 Java 8 特性（Lambda/try-with-resources）简化冗余代码

### 5. 设计先行
- 禁止面向测试编程（TDD）
- 须先设计方法签名（参数/返回/异常），再实现逻辑，最后编写测试

## 使用方法

在代码优化任务中引用此 skill，或在实现新功能前加载此规则集。

## 项目关键工具类位置

- 字符串工具: `common/utils/string/StringUtils.java`
- 日期工具: `common/utils/datetime/DateUtils.java`, `LocalDateUtils.java`
- 集合工具: `common/utils/CollectionUtils.java`, `ArrayUtils.java`
- 反射工具: `common/utils/reflect/ReflectUtils.java`, `ClassUtils.java`, `GenericsUtils.java`
- 文件工具: `file/FileUtils.java`, `file/csv/`
- 树结构: `common/bean/TreeNode.java`, `common/bean/GenericTreeNode.java`
- 池化帮助: `common/helper/PoolHelper.java`