# 简介
tingfengutil-java-base属于tingfengutil工具集合中的java工具包，并且以base为命名表示是只依赖于jdk的基础工具包。


# 包结构和类简介

## common包

### exception
- ReturnException
  带有返回值的异常
  
### helper 
- PropertyHelper
  读取Property属性文件
- PoolHelper
  缓冲池/资源池等通用池处理工具
    
### utils

#### datetime
- DateUtils 日期处理，时间处理工具

#### reflect 反射工具包
- ClassUtil 类工具
- GenericsUtils 泛型工具
- ReflectJudgeUtils 反射判断工具
- ReflectUtils 反射工具

#### string 字符串处理工具
- CharSetUtil 编码处理工具
- StringConvertUtils 字符串转换工具
- StringUtils 字符串常用判断，处理工具

### verificationCode 验证码包
- VerificationCodeUtil 生成简单的验证码图片和信息

### 其他

- ArrayUtils.java 数组工具类
- BeanUtils 工具类
- CollectionUtils 集合工具类
- CurrencyUtils 货币工具类
- Encrypt 加密解密工具类
- EnumUtils 枚举工具类
- HtmlUtils html处理工具类
- ObjectTypeUtils 对象类型判断处理工具类
- ObjectUtils 对象处理工具类
- ProxyByJDKUtils jdk代理工具类
- RandomUtils 随机数工具类
- StreamUtils  数据流工具类
- TreeUtils list和tree结构互转工具类
- UdpGetClientMacAddr 获取点名mac ip等信息工具类

## database 包


## file包
- FileUtils
 文件工具类
-  CSVUtil
 读写csv格式文件
 
 
## Serialization包

## web包



# 约定
- 基于jdk1.8以上
- 以UTF-8作为编译和存储时的编码
- 骆驼命名法则，静态方法的工具类以Utils/Util(推荐)为名称结尾，如SpringUtils.java；
- 实例方法的工具类以Helper为名称结尾，如SpringHelper.java(如果构造方法无参数，且一般用spring单例注入的模式运行可以是xxxmanage.java命名)
- 所有接口命名结尾以大写字母I为名称开头(推荐)或结尾，如BaseServiceI.java;
- 在common中尽量写一些枚举，接口，抽象类，在其余包中写实现方法，这样方便扩展；
- 抽象类等编写的命名规则是：当前使用点+功能描述：比如FieldSerializationProperty-->适用于Field，功能是序列化属性描述
- 方法命名
将A转换为B的方法:AUtils.toB(XXX);
从B得到A的方法:AUtils.getAbyB(XXX);或者AUtils.getA(XXX);
toString,toInteger等常用 和非自己当前项目代码写在A类中
不常用的或者同一项目中的实现方法,都尽量写在B类中.

