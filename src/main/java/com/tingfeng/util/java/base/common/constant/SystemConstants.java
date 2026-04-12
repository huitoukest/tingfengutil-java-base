package com.tingfeng.util.java.base.common.constant;

/**
 * 系统相关常量
 * @author huitoukest
 */
public interface SystemConstants {

    // ========== JVM 系统属性 ==========
    interface JVMSystem {
        String VERSION         = "java.version";
        String HOME            = "java.home";
        String CLASS_PATH      = "java.class.path";
        String CLASS_VERSION   = "java.class.version";
        String VM_NAME         = "java.vm.name";
        String VM_VERSION      = "java.vm.version";
        String VM_VENDOR       = "java.vm.vendor";
        String RUNTIME_NAME    = "java.runtime.name";
        String RUNTIME_VERSION = "java.runtime.version";
        String LIBRARY_PATH    = "java.library.path";
    }

    // ========== OS 系统属性 ==========
    interface OSSystem {
        String NAME    = "os.name";
        String VERSION = "os.version";
        String ARCH    = "os.arch";
    }

    // ========== User 系统属性 ==========
    interface UserSystem {
        String NAME = "user.name";
        String HOME = "user.home";
        String DIR  = "user.dir";
    }

    // ========== File/IO 系统属性 ==========
    interface FileSystem {
        String SEPARATOR      = "file.separator";
        String PATH_SEPARATOR = "path.separator";
        String LINE_SEPARATOR = "line.separator";
        String TMP_DIR        = "java.io.tmpdir";
    }
}
