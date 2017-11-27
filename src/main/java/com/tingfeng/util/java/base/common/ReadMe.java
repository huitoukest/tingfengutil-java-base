package com.tingfeng.util.java.base.common;

/**
 * 
 * @author huitoukest
 * 开发约定;
 * 1.在utils包内部存放的是自定义的一些工具类;
 *  	其中,以带有Utils名称的是一些包含静态方法的工具类;
 *  	带有Helper名称的是一些需要实例化调用的类;
 * 2.
 * 一个类使用的接口,所以使用次数较少,一般定义在其内部;如果有多个类使用这个接口,那么接口是外部接口
 * 3.所有转换类型的方法，均使用getAByB/或者toXXX的命名方式，
 * 	 具体写入A方法还是B方法，看具体和哪一个类的联系更加紧密，大部分情况下建议写到类A中；
 */
public interface ReadMe {

}
