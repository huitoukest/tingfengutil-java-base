# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Ignored (Non-Source) Directories

These directories contain build artifacts, IDE files, or generated content and should typically be ignored:

- `/target` - Maven build output (compiled classes, jars)
- `.idea/` - IntelliJ IDEA project files
- `*.iml` - IntelliJ IDEA module files
- `bin/`, `gen/` - Generated/build directories
- `.gradle/`, `build/` - Gradle build directories
- `local.properties` - SDK configuration
- `proguard/` - ProGuard generated files
- `annotation/` - Generated annotation files
- `/src/target/` - Maven target in source tree

## Build & Test Commands

```bash
# Compile
mvn clean compile

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=ClassName

# Run a specific test method
mvn test -Dtest=ClassName#methodName

# Package (build jar)
mvn package

# Skip tests during build
mvn package -DskipTests
```

## Architecture Overview

**tingfengutil-java-base** is a JDK 1.8 utility library with minimal dependencies (only JDK + slf4j-log4j12 provided scope). The "base" naming indicates it contains only pure JDK-based utilities with no JavaEE or third-party dependencies.

### Package Structure

```
src/main/java/com/tingfeng/util/java/base/
├── common/          # Core utilities (no external dependencies)
│   ├── utils/      # Static utility classes (StringUtils, DateUtils, etc.)
│   ├── bean/       # Data structures (TreeNode, Tuple2-4, GenericTreeNode)
│   ├── exception/ # Custom exceptions
│   ├── helper/     # Instance-based helpers (PoolHelper, PropertyHelper)
│   ├── constant/   # Enums and constants
│   ├── inter/      # Functional interfaces and callbacks
│   └── annotation/ # Annotations
├── database/       # DB utilities (DBConnectFactory, SqlUtils, HbaseConnectUtils)
├── file/           # File handling (FileUtils, CSVReader/Writer)
├── web/            # Web utilities (WebServiceUtils)
└── Serialization/  # JSON serialization support
```

### Naming Conventions

- **Utils/Util** suffix: Static method utility classes (e.g., `StringUtils`, `DateUtils`)
- **Helper** suffix: Instance method classes, often stateful (e.g., `PoolHelper`, `PropertyHelper`)
- **Interface naming**: End with uppercase `I` (e.g., `PoolMemberActionI`, `ConvertI`)
- **Conversion methods**: `AUtils.toB(obj)` for type conversion, `AUtils.getAbyB(obj)` for reverse lookup

### Key Utilities

- `common/utils/reflect/*` - Reflection utilities (ClassUtil, ReflectUtils, GenericsUtils)
- `common/utils/string/*` - String processing (StringUtils, CharSetUtil)
- `common/utils/datetime/*` - Date/time handling (DateUtils, LocalDateUtils)
- `common/bean/collection/TimeBufferConsumerList` - Buffered collection with flush timing
- `common/bean/TreeNode` / `GenericTreeNode` - Tree structure for list-tree conversions
- `common/helper/PoolHelper` - Generic resource pool management
- `file/csv/*` - CSV reading/writing utilities

### Test Structure

Tests are in `src/test/java` mirroring the main source structure. Use `mvn test -Dtest=TestClassName` to run individual test classes.

## Coding Standards Skill

When writing or optimizing Java code, use the skill: `/skill java-code-quality`

This skill enforces these priorities (1 > 2 > 3 > 4 > 5):
1. **Standards & Quality** - Follow Java standards, complete tool classes, add tests
2. **Reuse First** - Always use existing project utilities before implementing new code; no magic numbers
3. **Code Cleanup** - Remove dead code and unused imports
4. **Java 8 Features** - Use Lambda/try-with-resources where performance is not impacted
5. **Design First** - Design method signatures before implementation; no TDD

## Maven Coordinates

```xml
<dependency>
  <groupId>com.tingfeng</groupId>
  <artifactId>tingfengutil-java-base</artifactId>
  <version>0.2.6</version>
</dependency>
```