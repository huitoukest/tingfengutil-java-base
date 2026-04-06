---
name: windows-cmd-preference
description: Windows系统优先使用cmd执行命令，Linux使用默认bash
type: feedback
---

**规则：** 对话开始时先查询系统环境，Windows 下 Maven/Java 相关命令使用 cmd 执行，其余命令用 bash。

**Why:** Windows 和 bash 的环境变量不共享，bash 中无法访问 Windows CMD 中配置的 Maven/Java PATH。

**How to apply:**
- Windows 下：Maven (`mvn`) 和 Java (`java`, `javac`) 相关命令使用 `cmd /c "命令"`
- 其余命令（git, 文件操作等）直接使用 bash
- Linux 系统直接使用 bash
