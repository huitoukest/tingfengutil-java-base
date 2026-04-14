package com.tingfeng.util.java.base.lang.base;

import lombok.Getter;
import lombok.Setter;

/**
 * User和File/IO系统属性信息
 */
@Getter
@Setter
public class UserSystemInfo {
    // User
    private String userName;
    private String userHome;
    private String userDir;

    // File/IO
    private String fileSeparator;
    private String pathSeparator;
    private String lineSeparator;
    private String javaIoTmpdir;
}
