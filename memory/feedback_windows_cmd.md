---
name: windows-cmd-preference
description: Windows系统Maven/Java命令通过bash PATH配置执行，Linux使用默认bash
type: feedback
---

**规则：** 对话开始时先查询系统环境，Windows 下 Maven/Java 相关命令通过 bash PATH 配置执行，其余命令直接用 bash。

**Why:** Windows 和 bash 的环境变量不共享，bash 无法直接访问 Windows CMD 中配置的 Maven/Java PATH。需要在 bash 中单独配置。

**How to apply:**
- Windows 下：首次执行 Maven/Java 前设置 PATH（后续会话需重新设置）
  ```bash
  export PATH="/d/soft/apache-maven-3.5.2/bin:/d/soft/jdk8/bin:$PATH"
  export JAVA_HOME="/d/soft/jdk8"
  export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
  export MAVEN_OPTS="-Dfile.encoding=UTF-8"
  ```
- Linux 系统直接使用 bash，无需额外配置
- 其余命令（git, 文件操作等）直接使用 bash

**注意：** 以上 PATH 路径是当前机器的配置，如换电脑需根据实际安装路径调整。或者将 Maven/Java 添加到 Windows 系统 PATH 中（bash 也可共享）。
