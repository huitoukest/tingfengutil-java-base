package com.tingfeng.util.java.base.common.utils.version;

/**
 * 版本号解析器接口。
 * 将版本字符串解析为 {@link VersionParsedResult}，用于后续比较和校验。
 */
@FunctionalInterface
public interface VersionParser {

    /**
     * 解析版本字符串。
     *
     * @param version 版本字符串，不能为 null 或空
     * @return 解析结果
     * @throws IllegalArgumentException 若 version 为 null/空，或解析过程中类型推断失败
     */
    VersionParsedResult parse(String version);
}
