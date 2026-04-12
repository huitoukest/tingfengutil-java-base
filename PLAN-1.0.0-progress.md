# 1.0.0 版本架构升级规划

## 最后更新时间: 2026-04-12
## Git 提交记录:
- `cc06dab` chore: 准备 1.0.0-SNAPSHOT 版本
- `8431aa1` feat: 添加 LogUtils 和 Fun1~Fun5 函数接口
- `617fc42` refactor: 包迁移进行中 - 第一阶段 (156 files changed)

---

## 核心原则

1. **依赖**：生产代码只依赖 JDK + slf4j-api（lombok 辅助可用，fastjson 仅测试用）
2. **日志**：统一使用 LogUtils（自动选择 slf4j 或 System.out）
3. **包结构**：按功能分类，基础/支撑类放入各包的 base/ 或 support/
4. **函数接口**：Function1~5, Consumer1~5（超过5个参数建议封装为bean）
5. **循环依赖**：通过包级注解防止

---

## 已完成工作

### 1. 版本准备
- [x] pom.xml 版本改为 1.0.0-SNAPSHOT
- [x] 提交: `chore: 准备 1.0.0-SNAPSHOT 版本`

### 2. LogUtils 创建
- [x] 创建 `src/main/java/com/tingfeng/util/java/base/LogUtils.java`
- [x] 支持 slf4j 和 System.out 自动选择
- [x] 场景化日志方法: methodEnter, methodReturn, biz, performance, debug(tag)
- [x] 提交: `feat: 添加 LogUtils 和 Fun1~Fun5 函数接口`

### 3. 函数接口创建
- [x] 创建 `function/Consumer1~5`
- [x] 创建 `function/Function1~5`
- [x] Function1 继承 JDK Function
- [x] Function2 继承 JDK BiFunction
- [x] Consumer1 继承 JDK Consumer
- [x] Consumer2 继承 JDK BiConsumer

### 4. 包结构创建
- [x] 创建目录: lang/, collection/, array/, math/, datetime/, gis/, db/, io/, crypto/, concurrent/, text/, net/, pool/, cache/, validate/, bean/, format/
- [x] 各包下创建 base/, support/ 子目录
- [x] 创建 `function/` 包

### 5. 包迁移（进行中）
- [x] lang/: StringUtils, ObjectUtils, EnumUtils, Base64Utils, ThrowableUtils, LambdaUtils, FunctionUtils, JDKProxyUtils, RuntimeUtils, CharSetUtils
- [x] lang/base/: UnionKey, EntryBean, Tuple2-4, TrieNode, JvmSystemInfo, OsSystemInfo, RuntimeStatus, UserSystemInfo
- [x] lang/annotation/: FieldDescribe, FieldSortDescribe
- [x] lang/constant/: AlgorithmType, CacheType, Constants, DataNodeTraversePolicy, ObjectType, ObjectTypeString, RandomType, SystemConstants, TraversalPolicy
- [x] lang/exception/: BaseException, InfoException, ReturnException, OverPoolWaitSizeException, OverPoolWaitTimeException, io/, test/
- [x] lang/support/: PropertyHelper, test beans
- [x] array/: ArrayUtils
- [x] collection/: CollectionUtils, MapUtils, TreeUtils, CombinationUtils
- [x] collection/base/: TreeNode, DefaultTreeNode, GenericTreeNode, TreeTraverseContext, FastMap, TimeBufferConsumerList, BaseTimeBufferConsumerCollection
- [x] collection/support/: TreeHelper
- [x] math/: MathUtils, RandomUtils, CurrencyUtils, BinaryOperationUtils
- [x] math/base/: Fraction相关类
- [x] io/: IOUtils, StreamUtils, CSVBatchReadParam
- [x] crypto/: MessageDigestUtils, HashEncryptionHelper
- [x] text/: RegExpUtils, HtmlUtils, TokenHelper, StringTemplateHelper
- [x] gis/: GisUtils
- [x] bean/: BeanUtils, ConverterUtil, BeanCopyFun, ConverterInfo, FieldAttribute
- [x] concurrent/: ThreadUtils, ThreadPoolUtils, ThreadLocalUtils, ThreadFactoryUtils, ThreadGroupUtils, BaseFrequencyHelper, NamedThreadFactory
- [x] net/: HttpUtils, HttpResponseInfo
- [x] pool/: PoolHelper, FixedPoolHelper, SimplePoolHelper, PoolMember, PoolBaseInfo
- [x] cache/: SimpleCacheHelper, CacheVO, CacheItem, SimpleCacheMember
- [x] validate/: JudgeEmptyHelper

---

## 待完成工作

### 包迁移（未完成）

#### 待迁移文件
| 文件 | 原位置 | 目标位置 |
|------|--------|---------|
| ReflectUtils | common/utils/reflect/ | lang/support/ (需创建) |
| TreeUtils相关 | common/utils/support/tree/ | collection/support/ |

#### 待清理的空目录
- common/utils/support/
- common/utils/compress/
- common/utils/process/
- common/utils/reflect/
- common/utils/verificationCode/
- common/utils/cache/
- common/utils/datetime/
- common/utils/string/
- common/bean/ (空)
- common/helper/ (空)
- common/annotation/ (空)
- common/abs/ (空)
- common/inter/ (保留，暂未处理)
- common/constant/ (空)

### 编译修复

需要更新以下文件的 import 语句：
```
src/main/java/com/tingfeng/util/java/base/bean/BeanUtils.java
src/main/java/com/tingfeng/util/java/base/lang/StringUtils.java
src/main/java/com/tingfeng/util/java/base/collection/TreeUtils.java
src/main/java/com/tingfeng/util/java/base/common/constant/TraversalPolicy.java
```

修复内容：
1. `com.tingfeng.util.java.base.common.utils.reflect.ReflectUtils` → 移至 `lang/support/` 后更新路径
2. `com.tingfeng.util.java.base.common.utils.support.tree` → 移至 `collection/support/` 后更新路径

### 冗余代码清理

| 项目 | 状态 |
|------|------|
| 移除 `inter/` 下冗余函数式接口 | 待处理 |
| ConverterI, IEnum 确认删除 | 待处理 |
| 移除 commons-logging 引用 | 待处理 |

### 包级依赖注解

- [ ] 创建 `lang/package-info.java`
- [ ] 创建其他包的 package-info.java
- [ ] 定义包依赖规则

---

## 目标包结构

```
com.tingfeng.util.java.base/
├── LogUtils               # 日志工具（统一入口）
├── function/              # 函数接口 (Function1~5, Consumer1~5)
├── lang/
│   ├── base/             # 基础结构（UnionKey, Tuple2-4, EntryBean, TrieNode...）
│   ├── support/          # 支撑类
│   ├── annotation/       # 注解
│   ├── constant/         # 常量
│   ├── exception/        # 异常
│   ├── StringUtils
│   ├── ObjectUtils
│   └── ...
├── collection/
│   ├── base/             # 集合基础结构
│   ├── support/          # 支撑类
│   ├── CollectionUtils
│   ├── ListUtils
│   ├── MapUtils
│   └── TreeUtils
├── array/
│   └── ArrayUtils
├── math/
│   ├── base/             # Fraction等
│   └── MathUtils
├── datetime/
│   └── DateUtils
├── gis/
│   └── GisUtils
├── db/
│   └── DBUtils
├── io/
│   ├── base/
│   ├── IOUtils
│   └── CSVUtils
├── crypto/
│   └── MessageDigestUtils
├── concurrent/
│   ├── base/
│   ├── ThreadUtils
│   └── ThreadPoolUtils
├── text/
│   ├── RegExpUtils
│   └── HtmlUtils
├── net/
│   └── HttpUtils
├── pool/
│   ├── base/
│   └── PoolHelper
├── cache/
│   ├── base/
│   └── CacheUtils
├── validate/
│   └── ValidateUtils
├── bean/
│   ├── base/
│   └── BeanUtils
└── format/
    └── NumberFormatUtils
```

---

## 验证方式

1. `mvn clean compile` - 编译通过
2. `mvn test` - 全量测试通过
3. 无 commons-logging 引用
4. 包结构符合规划
5. `.claude/skills/` 包含完整规范
