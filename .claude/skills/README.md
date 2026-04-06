# Skills 文档索引

## 项目知识地图

```
skills/
├── fundamentals/          # 编码基础（必读）
│   ├── coding.md          # 编码标准 + 命名 + 异常
│   ├── performance.md      # 性能工具选用
│   └── testing.md         # 单元测试规范
│
├── architecture/          # 架构设计
│   ├── principles.md      # 核心原则
│   └── package-structure.md
│
├── modules/               # 工具模块
│   ├── io-utils/
│   │   ├── design.md      # 设计原则
│   │   ├── signatures.md # 方法签名
│   │   └── examples.md   # 使用示例
│   ├── file-utils/
│   │   ├── design.md
│   │   ├── signatures.md
│   │   └── examples.md
│   └── process-utils/
│       ├── design.md
│       ├── signatures.md
│       └── examples.md
│
└── reference/              # 参考索引
    ├── quick-ref.md       # 方法->文件映射
    └── checklist.md       # 代码检查清单
```

## 加载顺序

| 优先级 | 文档 | 时机 |
|--------|------|------|
| 1 | fundamentals/* | 编码前必读 |
| 2 | architecture/* | 设计决策时读 |
| 3 | modules/* | 实现具体模块时读 |
| 4 | reference/* | 调试/检查时查 |

## 核心约束

1. **最小依赖** - 禁止引入 JavaEE/第三方库
2. **统一异常** - 抛出自定义 RuntimeException
3. **职责分离** - IOUtils 管流，FileUtils 管文件
4. **线程池外部注入** - 异步方法支持 ExecutorService/Thread/Runnable
