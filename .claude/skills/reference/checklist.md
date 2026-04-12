# 代码检查清单

## 编码规范

- [ ] 命名符合规范（Utils/Helper/I后缀）
- [ ] 无缩写（除URL/ID/DTO/VO等业界通用）
- [ ] 布尔返回值用`is`/`has`/`contains`，不用`get`
- [ ] 无魔法数字/字符串（使用常量）
- [ ] 无无用import
- [ ] 4空格缩进，禁止Tab

## 异常处理

- [ ] 使用自定义RuntimeException
- [ ] 不抛检查型异常
- [ ] 精确捕获具体异常类型
- [ ] 无空catch
- [ ] 无`catch(Exception/Throwable)`（顶层除外）

## 资源管理

- [ ] 使用try-with-resources
- [ ] finally中关闭可能为null的资源已做null检查
- [ ] close顺序正确（先输入流，后输出流）
- [ ] flush在close之前

## 并发

- [ ] ThreadLocal使用后remove()
- [ ] 线程池拒绝策略已设置
- [ ] 共享变量线程安全

## 安全

- [ ] 所有外部输入校验
- [ ] SQL参数化，无拼接
- [ ] 无硬编码密码/密钥/Token
- [ ] 敏感信息不打印日志

## IOUtils 专用

- [ ] 无File参数（文件操作归FileUtils）
- [ ] CancellationToken是public static class
- [ ] 背压机制正确实现

## FileUtils 专用

- [ ] 文件操作前检查存在性
- [ ] 父目录不存在时创建
- [ ] 大文件分块处理
- [ ] 委托流操作给IOUtils

## ProcessUtils 专用

- [ ] 流式读取用双线程防止死锁
- [ ] 跨平台命令适配（Windows/Linux）
- [ ] 默认超时120秒
- [ ] 梯度终止策略

## 测试

- [ ] 每个公开方法有测试
- [ ] AAA模式（Arrange/Act/Assert）
- [ ] 边界值有测试
- [ ] 异常情况有测试
- [ ] 资源清理有finally
