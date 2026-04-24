---
name: windows-command-spec
description: Windows命令执行规范，避免路径/参数解析错误
type: reference
originSessionId: f7263902-384a-4af5-b3d9-90d4a8714f50
---
# Windows命令执行规范

## 核心规则
**所有Windows命令统一使用 `cmd //c "命令"` 执行**

## 常见问题
- Git Bash直接调用Windows命令时，参数会被错误解析
- 路径中的斜杠/反斜杠问题

## 日期格式
- Windows使用 `yyyy/mm/dd` 格式
- 错误格式示例：`04/24/2026`、`2026-04-24`

## 示例
```bash
# 错误
schtasks /create ...

# 正确
cmd //c "schtasks /create /tn TaskName /tr ... /sc once /st 19:00 /sd 2026/04/24"
```
