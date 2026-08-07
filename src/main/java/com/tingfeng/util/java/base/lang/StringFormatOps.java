package com.tingfeng.util.java.base.lang;

import java.text.DecimalFormat;

/**
 * 字符串格式化、宽度计算、全半角转换、裁剪与填充操作实现。
 *
 * 包级私有，不对外暴露。通过 {@link StringUtils} 对外提供统一 API。
 */
class StringFormatOps {

    private StringFormatOps() {

    }

    /**
     * 全角字符变半角字符
     *
     * @param str
     * @return
     */
    static String toSbcCaseByDbcCase(String str) {
        if (str == null || "".equals(str)) {
            return "";
        }
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            // 根据Unicode East Asian Width规范进行转换
            if (c >= 0xFF01 && c <= 0xFF5E) {
                // 全角ASCII字符变体转换为半角：减0xFEE0
                sb.append((char) (c - 0xFEE0));
            } else if (c == 0x3000) {
                // 全角空格(U+3000)转换为半角空格(U+0020)
                sb.append((char) 0x0020);
            } else {
                // 其他字符保持不变（汉字、日文假名、韩文等全角字符本身不转换）
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * 半角字符变全角字符
     * 根据Unicode East Asian Width规范进行转换
     */
    static String toDbcCaseBySbcCase(String str) {
        if (str == null || "".equals(str)) {
            return "";
        }
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            // 根据Unicode East Asian Width规范进行转换
            if (c >= 0x0021 && c <= 0x007E) {
                // 半角ASCII字符转换为全角：加0xFEE0
                sb.append((char) (c + 0xFEE0));
            } else if (c == 0x0020) {
                // 半角空格(U+0020)转换为全角空格(U+3000)
                sb.append((char) 0x3000);
            } else {
                // 其他字符保持不变（汉字、日文假名、韩文等全角字符本身不转换）
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * 获取subString，自动判空
     *
     * @param src
     * @param size
     * @return
     */
    static String getSubString(String src, int size) {
        if (src != null && src.length() > size) {
            src = src.substring(0, size);
        }
        return src;
    }

    /**
     * 截取字符串　超出的字符用symbol代替
     *
     * @param len     　字符串长度　长度计量单位为一个GBK汉字　　两个英文字母计算为一个单位长度
     * @param str
     * @param symbol  超出的字符用symbol代替
     * @param charset 字符编码
     * @return
     */
    static String getStringByLimitLength(String str, int len, String symbol, String charset) {
        int iLen = len * 2;
        int counterOfDoubleByte = 0;
        String strRet = "";
        try {
            if (str != null) {
                byte[] b = str.getBytes(charset);
                if (b.length <= iLen) {
                    return str;
                }
                for (int i = 0; i < iLen; i++) {
                    if (b[i] < 0) {
                        counterOfDoubleByte++;
                    }
                }
                if (counterOfDoubleByte % 2 == 0) {
                    strRet = new String(b, 0, iLen, charset) + symbol;
                    return strRet;
                } else {
                    strRet = new String(b, 0, iLen - 1, charset) + symbol;
                    return strRet;
                }
            } else {
                return "";
            }
        } catch (Exception ex) {
            return str.substring(0, len);
        } finally {
            strRet = null;
        }
    }

    /**
     * 截取字符串　超出的字符用...代替
     *
     * @param len 　字符串长度　长度计量单位为一个GBK汉字　　两个英文字母计算为一个单位长度
     * @param str
     * @return
     */
    static String getStringByLimitLength(String str, int len) {
        return getStringByLimitLength(str, len, "...", "UTF-8");
    }

    /**
     * 取得字符串的实际长度（考虑了汉字的情况）
     *
     * @param srcStr 源字符串
     * @return 字符串的实际长度
     */
    static int getStringLength(String srcStr) {
        int return_value = 0;
        if (srcStr != null) {
            int i = 0;
            while (i < srcStr.length()) {
                int codePoint = srcStr.codePointAt(i);

                // 处理代理对（如emoji表情符号）
                if (Character.isSupplementaryCodePoint(codePoint)) {
                    // 补充平面字符（如emoji）通常算作2个宽度（全角）
                    return_value += 2;
                    i += 2; // 跳过整个代理对
                } else {
                    char c = (char) codePoint;
                    return_value += getEastAsianWidth(c);
                    i++;
                }
            }
        }
        return return_value;
    }

    /**
     * 根据Unicode East Asian Width (UAX #11) 标准计算字符宽度
     * 遵循国际编码规范，正确处理各种Unicode字符的显示宽度
     *
     * @param c 字符
     * @return 字符宽度（半角为1，全角为2）
     */
    private static int getEastAsianWidth(char c) {
        // ASCII控制字符和基本拉丁字母：半角（1）
        if (c <= 0x007F) {
            return 1;
        }

        // 拉丁文补充：半角（1）
        if (c >= 0x0080 && c <= 0x00FF) {
            return 1;
        }

        // CJK统一表意符号（中日韩统一表意文字）：全角（2）
        if (c >= 0x4E00 && c <= 0x9FFF) {
            return 2;
        }

        // CJK扩展A区：全角（2）
        if (c >= 0x3400 && c <= 0x4DBF) {
            return 2;
        }

        // 半角和全角形式：根据具体范围判断
        if (c >= 0xFF00 && c <= 0xFFEF) {
            // 全角ASCII变体：全角（2）
            if (c >= 0xFF01 && c <= 0xFF5E) {
                return 2;
            }
            // 半角片假名：半角（1）
            if (c >= 0xFF65 && c <= 0xFF9F) {
                return 1;
            }
            // 全角片假名：全角（2）
            if (c >= 0xFF61 && c <= 0xFF64) {
                return 2;
            }
        }

        // 韩文音节：全角（2）
        if (c >= 0xAC00 && c <= 0xD7AF) {
            return 2;
        }

        // 韩文字母：全角（2）
        if (c >= 0x1100 && c <= 0x11FF) {
            return 2;
        }

        // 日文平假名：全角（2）
        if (c >= 0x3040 && c <= 0x309F) {
            return 2;
        }

        // 日文片假名：全角（2）
        if (c >= 0x30A0 && c <= 0x30FF) {
            return 2;
        }

        // 中日韩符号和标点：全角（2）
        if (c >= 0x3000 && c <= 0x303F) {
            return 2;
        }

        // 中日韩部首补充：全角（2）
        if (c >= 0x2E80 && c <= 0x2EFF) {
            return 2;
        }

        // 中日韩笔画：全角（2）
        if (c >= 0x31C0 && c <= 0x31EF) {
            return 2;
        }

        // 中日韩兼容字符：全角（2）
        if (c >= 0xF900 && c <= 0xFAFF) {
            return 2;
        }

        // 中日韩兼容表意文字补充：全角（2）
        if (c >= 0x2F800 && c <= 0x2FA1F) {
            return 2;
        }

        // 默认：半角（1）
        return 1;
    }

    /***************************************************************************
     * toHideEmailPrefix - 获得隐藏邮件地址前缀的邮箱地址。
     *
     * @param email
     *            - EMail邮箱地址 例如: linwenguo@koubei.com 等等...
     * @return 返回已隐藏前缀邮件地址, 如 *********@koubei.com.
     **************************************************************************/
    static String toHideEmailPrefix(String email) {
        if (null != email) {
            int index = email.lastIndexOf('@');
            if (index > 0) {
                email = StringUtils.repeat("*", index).concat(email.substring(index));
            }
        }
        return email;
    }

    /**
     * 格式化一个float
     *
     * @param format 要格式化成的格式 such as #.00, #.#
     */
    static String formatFloat(float f, String format) {
        DecimalFormat df = new DecimalFormat(format);
        return df.format(f);
    }

    static String leftPad(Object value, int length, Object c) {
        return padString(value, length, c, 0);
    }

    static String rightPad(Object value, int length, Object c) {
        return padString(value, length, c, 1);
    }

    /**
     * 填充字符串到指定长度
     *
     * @param value
     * @param length
     * @param c
     * @param direction 0 = left,1 = right
     * @return
     */
    private static String padString(Object value, int length, Object c, int direction) {
        String str = value.toString();
        if (str.length() > length) {
            return str;
        }
        int len = length - str.length();
        return StringUtils.doAppend(sb -> {
            if (1 == direction) {
                sb.append(value);
            }
            for (int i = 0; i < len; i++) {
                sb.append(c);
            }
            if (direction == 0) {
                sb.append(value);
            }
            return sb.toString();
        });
    }
}
