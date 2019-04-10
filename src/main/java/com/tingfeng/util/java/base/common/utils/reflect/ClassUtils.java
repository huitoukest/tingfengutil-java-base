package com.tingfeng.util.java.base.common.utils.reflect;

import com.tingfeng.util.java.base.common.utils.BeanUtils;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author huitoukest
 * class工具类
 */
public class ClassUtils {

    private static final Log logger = LogFactory.getLog(ClassUtils.class);

    /**
     * 取得某个类的所有子类或者实现类类
     * @param c Class对象，否则返回Empty List
     * */
    public static List<Class<?>> getAllClassByInterface(Class<?> c) {
            List<Class<?>>  returnClassList = Collections.EMPTY_LIST;
            if(c.isInterface()) {
                // 获取当前的包名
                String packageName = c.getPackage().getName();
                // 获取当前包下以及子包下所以的类
                List<Class<?>> allClass = getClasses(packageName);
                if(allClass != null) {
                    returnClassList = new ArrayList<Class<?>>();
                    for(Class<?> classes : allClass) {
                        // 判断是否是同一个接口
                        if(c.isAssignableFrom(classes)) {
                            // 本身不加入进去
                            if(!c.equals(classes)) {
                                returnClassList.add(classes);        
                            }
                        }
                    }
                }
            }
            
            return returnClassList;
        }

    /**
     * 从包package中获取所有的Class,自动查找当前classpath下的文件或者jar包中的class
     * 默认递归查找包下面的子文件夹
     * @param packageName
     * @return
     */
    public static List<Class<?>> getClasses(String packageName){
        //是否循环迭代
        boolean recursive = true;
        return getClasses(packageName,recursive);
    }

    /**
     * 从包package中获取所有的Class,自动查找当前classpath下的文件或者jar包中的class
     * @param packageName
     * @param recursive 是否递归查找包下面的子文件夹
     * @return
     */
    public static List<Class<?>> getClasses(String packageName,boolean recursive){
        //第一个class类的集合
        List<Class<?>> classes = new ArrayList<Class<?>>();
        //获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        //定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            //循环迭代下去
            while (dirs.hasMoreElements()){
                //获取下一个元素
                URL url = dirs.nextElement();
                //得到协议的名称
                String protocol = url.getProtocol();
                //如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    //获取包的物理路径
                    String rootFilePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    //以文件的方式扫描整个包下的文件 并添加到集合中
                    classes.addAll(findClassesByPath(packageName, rootFilePath, recursive));
                } else if ("jar".equals(protocol)){
                    //获取jar
                   JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
                   classes.addAll(findClassesByJar(jar,packageName,recursive));
                }
            }
        } catch (IOException e) {
            logger.info("getClasses error",e);
        }
       
        return classes;
    }

    /**
     * 查找一个指定jarFile指定packageName下的class文件
     * @param jarFile
     * @param packageName
     * @param recursive 是否运行递归查找子包
     * @return EMPTY_LIST or 这classes List
     */
    public static List<Class<?>> findClassesByJar(JarFile jarFile,String packageName,final boolean recursive){
       if(null == jarFile || StringUtils.isEmpty(packageName)) {
            return Collections.EMPTY_LIST;
       }
       String packageDirName = packageName.replace("/",".");
       List<Class<?>> classes = new ArrayList<>();
        //从此jar包 得到一个枚举类
        Enumeration<JarEntry> entries = jarFile.entries();
        //同样的进行循环迭代
        while (entries.hasMoreElements()) {
            //获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            //如果是以/开头的
            if (name.charAt(0) == '/') {
                //获取后面的字符串
                name = name.substring(1);
            }
            //如果前半部分和定义的包名相同
            if (name.startsWith(packageDirName)) {
                int idx = name.lastIndexOf('/');
                //如果以"/"结尾 是一个包
                if (idx != -1) {
                    //获取包名 把"/"替换成"."
                    packageName = name.substring(0, idx).replace('/', '.');
                }
                //如果是一个包，或者可以迭代下去,或者是当前目录下的类
                if ((idx != -1) || recursive || name.endsWith(".class")){
                    //如果是一个.class文件 而且不是目录
                    if (name.endsWith(".class") && !entry.isDirectory()) {
                        //去掉后面的".class" 获取真正的类名
                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                        try {
                            //添加到classes
                            classes.add(Class.forName(packageName + '.' + className));
                        } catch (ClassNotFoundException e) {
                            logger.info("findClassesByJar",e);
                        }
                    }
                }
            }
        }
        return classes;
    }
    
    /**
     * 以文件的形式来获取包下的所有Class,即指定rootPackagePath下的
     * packageName包下的所有class文件,不支持jar包
     * @param packageName
     * @param rootPackagePath
     * @param recursive 是否递归查找子文件夹
     * @return 如果此路径是一个文件或者没有找到相关的class，则返回空List
     */
    public static List<Class<?>> findClassesByPath(String packageName, String rootPackagePath, final boolean recursive){
        //获取此包的目录 建立一个File
        File dir = new File(rootPackagePath);
        //如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return Collections.emptyList();
        }
        List<Class<?>> classes = new ArrayList<>();
        //如果存在 就获取包下的所有文件 包括目录.
        //自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
        File[] dirFiles = dir.listFiles(file -> (recursive && file.isDirectory()) || (file.getName().endsWith(".class")));
        if(null != dirFiles && dirFiles.length > 0) {
	        //循环所有文件
	        for (File file : dirFiles) {
	            //如果是目录 则继续扫描
	            if (file.isDirectory()) {
                    classes.addAll(findClassesByPath(packageName + "." + file.getName(),file.getAbsolutePath(),recursive));
	            }else {
	                //如果是java类文件 去掉后面的.class 只留下类名
	                String className = file.getName().substring(0, file.getName().length() - 6);
	                try {
	                    //添加到集合中去
	                    classes.add(Class.forName(packageName + '.' + className));
	                } catch (ClassNotFoundException e) {
	                    logger.info("find class,but not find file",e);
	                }
	            }
	        }
        }
        return classes;
    }
}