package com.tingfeng.util.java.base.lang.base;

import lombok.Getter;
import lombok.Setter;

/**
 * JVM系统属性信息
 */
@Getter
@Setter
public class JvmSystemInfo {
    private String version;
    private String home;
    private String classPath;
    private String classVersion;
    private String vmName;
    private String vmVersion;
    private String vmVendor;
    private String runtimeName;
    private String runtimeVersion;
    private String libraryPath;
}
