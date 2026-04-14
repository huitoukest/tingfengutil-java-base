package com.tingfeng.util.java.base.text;

import com.tingfeng.util.java.base.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * HTML工具类，提供HTML字符串处理相关功能
 * 包括HTML转义、URL处理、标签移除、字符串截取等功能
 *
 * @author tw 2009-06-05
 */
public class HtmlUtils {

    private HtmlUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 将字符串中的特殊字符转义为HTML实体
     * 转换规则：
     * - 空格 -> &nbsp;
     * - 换行符 -> <br>
     * - 回车符 -> 忽略
     * - 单引号 -> &#39;
     * - 小于号 -> &lt;
     * - 大于号 -> &gt;
     * - 和号 -> &amp;
     * - 双引号 -> &#34;
     * - 反斜杠 -> &#92;
     *
     * @param str 需要转义的字符串，可为null
     * @return 转义后的HTML字符串，如果输入为null则返回null
     */
    public static String escapeHtml(String str) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return str;
        }
        return StringUtils.doAppend(sb -> {
            int len = str.length();
            for (int i = 0; i < len; i++) {
                char c = str.charAt(i);
                switch (c) {
                    case ' ':
                        sb.append("&nbsp;");
                        break;
                    case '\n':
                        sb.append("<br>");
                        break;
                    case '\r':
                        break;
                    case '\'':
                        sb.append("&#39;");
                        break;
                    case '<':
                        sb.append("&lt;");
                        break;
                    case '>':
                        sb.append("&gt;");
                        break;
                    case '&':
                        sb.append("&amp;");
                        break;
                    case '"':
                        sb.append("&#34;");
                        break;
                    case '\\':
                        sb.append("&#92;");
                        break;
                    default:
                        sb.append(c);
                }
            }
            return sb.toString();
        });
    }

    /**
     * 过滤用户输入的URL地址（防治用户广告）
     * 目前只针对以http(s)或www开头的URL地址
     * 本方法调用的正则表达式，不建议用在对性能严格的地方例如:循环及list页面等
     *
     * @param str 需要处理的字符串，可为null
     * @return 处理后的字符串，移除了URL相关内容，如果输入为null则返回null
     */
    public static String removeURL(String str) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return str;
        }
        return StringUtils.replaceByReg(str, "(?i)(http|www|com|cn|org|https|\\.)+", "");
    }

    /**
     * Wap页面的非法字符检查和替换
     * 处理规则：
     * - 移除span标签（包括class="keyword"属性）
     * - 移除strong标签（包括class="keyword"属性）
     * - 将$替换为全角＄
     * - 将&替换为全角＆
     * - 将<替换为全角＜
     * - 将>替换为全角＞
     *
     * @param str 需要处理的字符串，可为null
     * @return 处理后的字符串，如果输入为null则返回null
     */
    public static String replaceWapStr(String str) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return str;
        }
        str = StringUtils.replaceByReg(str, "<span class=\"keyword\">", "");
        str = StringUtils.replaceByReg(str, "</span>", "");
        str = StringUtils.replaceByReg(str, "<strong class=\"keyword\">", "");
        str = StringUtils.replaceByReg(str, "<strong>", "");
        str = StringUtils.replaceByReg(str, "</strong>", "");
        str = str.replace('$', '＄');
        str = StringUtils.replaceByReg(str, "&amp;", "＆");
        str = str.replace('&', '＆');
        str = str.replace('<', '＜');
        str = str.replace('>', '＞');
        return str;
    }

    /**
     * 移除HTML标签，将HTML内容转换为纯文本
     * 处理步骤：
     * 1. 移除标签间的空白字符
     * 2. 将&nbsp;替换为空格
     * 3. 将<br>标签替换为换行符
     * 4. 移除所有HTML标签
     * 5. 压缩多个空格为单个空格
     * 6. 移除首尾空白
     *
     * @param str 需要处理的HTML字符串，可为null
     * @return 移除HTML标签后的纯文本，如果输入为null则返回null
     */
    public static String getStringByRemoveHtmlLabel(String str) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return str;
        }
        str = StringUtils.replaceByReg(str, ">\\s*<", "><");
        str = StringUtils.replaceByReg(str, "&nbsp;", " ");
        str = StringUtils.replaceByReg(str, "<br ?/?>", "\n");
        str = StringUtils.replaceByReg(str, "<([^<>]+)>", "");
        str = StringUtils.replaceByReg(str, "\\s\\s\\s*", " ");
        str = StringUtils.replaceByReg(str, "^\\s*", "");
        str = StringUtils.replaceByReg(str, "\\s*$", "");
        str = StringUtils.replaceByReg(str, " +", " ");
        return str;
    }


    /**
     * 去掉HTML标签之外的字符串，只保留html标签以及其内部的字符串
     * 例如：输入"Hello<div>World</div>Test"，输出"<div>World</div>"
     *
     * @param str 源字符串，可为null
     * @return 只包含HTML标签及其内部内容的字符串，如果输入为null则返回null
     */
    public static String getInnerHTMLLabelAndString(String str) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return str;
        }
        str = StringUtils.replaceByReg(str, "^([^<>]+)<", "<");
        str = StringUtils.replaceByReg(str, ">([^<>]+)$", ">");
        return str;
    }

    /**
     * 将HTML的省略写法替换成非省略写法
     * 例如：<A/>替换为<A></A>
     *
     * @param str HTML字符串，可为null
     * @param pt  标签名，如"table"
     * @return 替换后的完整HTML标签字符串，如果输入为null则返回null
     */
    public static String formatToFullHtmlLabel(String str, String pt) {
        if (str == null || pt == null) {
            return str;
        }
        if (str.isEmpty() || pt.isEmpty()) {
            return str;
        }
        String regEx = "<" + pt + "\\s+([\\S&&[^<>]]*)/>";
        Pattern p = RegExpUtils.getPattern(regEx, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        String[] sa = null;
        String sf = "";
        String sf2 = "";
        String sf3 = "";
        for (; m.find(); ) {
            sa = p.split(str);
            if (sa == null || sa.length == 0) {
                break;
            }
            sf = str.substring(sa[0].length(), str.indexOf("/>", sa[0].length()));
            sf2 = sf + "></" + pt + ">";
            sf3 = str.substring(sa[0].length() + sf.length() + 2);
            str = sa[0] + sf2 + sf3;
            sa = null;
        }
        return str;
    }

    /**
     * 智能截取HTML字符串，保留完整的HTML标签结构
     * 处理逻辑：
     * 1. 遍历字符串，识别HTML标签和普通文本
     * 2. HTML标签不计入截取长度
     * 3. 遇到开始标签时入栈，遇到结束标签时出栈
     * 4. 截取完成后，自动补全未闭合的标签
     * 5. 如果字符串被截断，添加指定的后缀
     *
     * @param str  原始HTML字符串，可为null
     * @param len  要截取的文本长度（不包括HTML标签）
     * @param tail 截断后添加的后缀，如"..."
     * @return 截取后的HTML字符串，保持标签结构完整
     */
    public static String getHtmlSubString(String str, int len, String tail) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty() || len <= 0) {
            return str;
        }
        if (str.length() <= len) {
            return str;
        }
        int length = str.length();
        char c = ' ';
        String tag = null;
        String name = null;
        int size = 0;
        String result = "";
        boolean isTag = false;
        List<String> tags = new ArrayList<String>();
        int i = 0;
        for (int end = 0, spanEnd = 0; i < length && len > 0; i++) {
            c = str.charAt(i);
            if (c == '<') {
                end = str.indexOf('>', i);
            }

            if (end > 0) {
                tag = str.substring(i, end + 1);
                int n = tag.length();
                if (tag.endsWith("/>")) {
                    isTag = true;
                } else if (tag.startsWith("</")) {
                    name = tag.substring(2, end - i);
                    size = tags.size() - 1;
                    if (size >= 0 && name.equals(tags.get(size))) {
                        isTag = true;
                        tags.remove(size);
                    }
                } else {
                    spanEnd = tag.indexOf(' ', 0);
                    spanEnd = spanEnd > 0 ? spanEnd : n;
                    name = tag.substring(1, spanEnd);
                    if (name.trim().length() > 0) {
                        isTag = true;
                        tags.add(name);
                    }
                }
                if (!isTag) {
                    if (n >= len) {
                        result += tag.substring(0, len);
                        break;
                    } else {
                        len -= n;
                    }
                }

                result += tag;
                isTag = false;
                i = end;
                end = 0;
            } else {
                len--;
                result += c;
            }
        }
        for (String endTag : tags) {
            result += "</" + endTag + ">";
        }
        if (i < length) {
            result += tail;
        }
        return result;
    }


    /**
     * 将HTML字符串中的URL从相对路径替换为绝对路径
     * 支持的属性：src、href、url
     * 例如：将"/images/test.jpg"替换为"http://example.com/images/test.jpg"
     *
     * @param prefix URL前缀，如"http://example.com"
     * @param s      HTML字符串，可为null
     * @return 转换后的HTML字符串，如果输入为null则返回null
     */
    public static String transUrl(String prefix, String s) {
        if (s == null || prefix == null) {
            return s;
        }
        if (s.isEmpty() || prefix.isEmpty()) {
            return s;
        }
        try {
            String h = s;
            Pattern pattern = RegExpUtils.getPattern(".*src=\\\\*\\\"(?!http://)([^\\\\][^\\s]+[^\\\\=])\\\\*\\\".*");
            Matcher matcher = pattern.matcher(s);
            boolean has = false;
            while (matcher.find()) {
                String tmp = matcher.group(1);
                h = h.replace(tmp, prefix + tmp);
                has = true;
            }

            pattern = RegExpUtils.getPattern(".*href=\\\\*\\\"(?!http://)([^\\\\][^\\s]+[^\\\\=])\\\\*\\\".*");
            matcher = pattern.matcher(s);
            while (matcher.find()) {
                String tmp = matcher.group(1);
                h = h.replace(tmp, prefix + tmp);
                has = true;
            }

            pattern = RegExpUtils.getPattern(".*url=\\\\*\\\"(?!http://)([^\\\\][^\\s]+[^\\\\=])\\\\*\\\".*");
            matcher = pattern.matcher(s);
            while (matcher.find()) {
                String tmp = matcher.group(1);
                h = h.replace(tmp, prefix + tmp);
                has = true;
            }
            if (!has) {
                return h;
            }
            return transUrl(prefix, h);

        } catch (Exception e) {
            System.out.println("HtmlUtils:url相对地址转换绝对地址失败!" + e.toString());
            return s;
        }
    }

    /**
     * 删除HTML中的script标签、style标签以及所有HTML标签
     * 只保留标签/容器内部的文本内容
     * 处理步骤：
     * 1. 移除<script>标签及其内容
     * 2. 移除<style>标签及其内容
     * 3. 移除所有HTML标签
     * 4. 返回纯文本内容
     *
     * @param htmlStr HTML字符串，可为null
     * @return 移除所有标签后的纯文本，如果输入为null则返回null
     */
    public static String delHTMLTag(String htmlStr) {
        if (htmlStr == null) {
            return null;
        }
        if (htmlStr.isEmpty()) {
            return htmlStr;
        }
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>";
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>";
        String regEx_html = "<[^>]+>";

        Pattern p_script = RegExpUtils.getPattern(regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll("");

        Pattern p_style = RegExpUtils.getPattern(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll("");

        Pattern p_html = RegExpUtils.getPattern(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll("");

        return htmlStr.trim();
    }
}