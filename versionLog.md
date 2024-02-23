# 版本日志
maven 引用:
```xml
        <dependency>
          <groupId>com.tingfeng</groupId>
          <artifactId>tingfengutil-java-base</artifactId>
          <version>0.2.3</version>
        </dependency>
```

## 0.2.4
- bugfix
  - 修复 LocalDateUtils#getDateString bug
## 0.2.3
20240105
- 增强
  - 新增 ArrayUtils.join，支持各种类型以指定连接符拼接为字符串
  - 新增 ArrayUtils/CollectionUtils 新增shuffle方法，可打乱数组或List集合中内容的顺序
- bugfix
  - 修复 TimeBufferConsumerList 只有第一个实例会正常运行
## 0.2.2
20231106
- 增强
  - add CacheSupplier：带有缓存功能的 Supplier
  - LocalDateUtils 新增大量工具方法
  - 日期工具类增强：新增 Date 与 LocalDate/LocalDateTime的转换，他们与字符串、毫秒的相互转换工具
  - 新增StringUtils.unescape 反转义工具
  - CSVUtil优化: 支持反转义header头（header头多一个一层引号的情况）
- bugfix
  - EnumUtils 的 getEnum 与 getEnumByValue方法，通过枚举值获取枚举对象本身时，默认使用的缓存只会以第一次获取值的枚举方法（例如枚举有A、B两个属性，一次通过A属性方法getA取值得到枚举，第二次通过B属性方法取的枚举值时，此时仍会通过A属性的取值与传入的值作比，等同时获取A属性值对应的枚举类造成bug）
- 不兼容
  - 日期格式化常量从 DateUtils.FORMATE_xxxx  移动到 DateFormat.FORMAT_xxx

## 0.2.1
20230810
- TimeBufferConsumerList 性能优化
- bugfix
  - CollectionUtils.eq bug