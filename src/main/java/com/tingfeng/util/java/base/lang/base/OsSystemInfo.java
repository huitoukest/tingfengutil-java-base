package com.tingfeng.util.java.base.common.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * OS系统属性信息
 */
@Getter
@Setter
public class OsSystemInfo {
    private String name;
    private String version;
    private String osArch;
}
