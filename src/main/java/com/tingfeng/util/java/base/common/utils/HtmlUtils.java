package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.utils.string.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * &lt;p&gt;对HTML中的保留字符和一些特殊字符进行转
 *
 * @author tw 2009-06-05
 */
public class HtmlUtils {

    /**
     * 通过转义字符串中相关符号为html中的标签，来
     * 获得html页面
     *
     * @param str
     * @return
     */
    public static String escapeHtml(String str) {
        if (str == null) {
            return null;
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
     * 过滤用户输入的URL地址（防治用户广告） 目前只针对以http(s)或www开头的URL地址 本方法调用的正则表达式，
     * 不建议用在对性能严格的地方例如:循环及list页面等
     *
     * @param str 需要处理的字符串
     * @return 返回处理后的字符串
     */
    public static String removeURL(String str) {
        if (str != null) {
            str = str.toLowerCase().replaceAll("(http|www|com|cn|org|https|\\.)+", "");
        }
        return str;
    }

    /**
     * Wap页面的非法字符检查
     *
     * @param str
     * @return
     */
    public static String replaceWapStr(String str) {
        if (str != null) {
            str = str.replaceAll("<span class=\"keyword\">", "");
            str = str.replaceAll("</span>", "");
            str = str.replaceAll("<strong class=\"keyword\">", "");
            str = str.replaceAll("<strong>", "");
            str = str.replaceAll("</strong>", "");
            str = str.replace('$', '＄');
            str = str.replaceAll("&amp;", "＆");
            str = str.replace('&', '＆');
            str = str.replace('<', '＜');
            str = str.replace('>', '＞');
        }
        return str;
    }

    /**
     * 通过将html中的一些标签替换为String中的一些转意符号，来得到String
     *
     * @param str
     * @return ************************************************************************
     */
    public static String getStringByRemoveHtmlLabel(String str) {
        str = StringUtils.replaceByReg(str, ">\\s*<", "><");
        str = StringUtils.replaceByReg(str, "&nbsp;", " ");// 替换空格
        str = StringUtils.replaceByReg(str, "<br ?/?>", "\n");// 去<br><br />
        str = StringUtils.replaceByReg(str, "<([^<>]+)>", "");// 去掉<>内的字符
        str = StringUtils.replaceByReg(str, "\\s\\s\\s*", " ");// 将多个空白变成一个空格
        str = StringUtils.replaceByReg(str, "^\\s*", "");// 去掉头的空白
        str = StringUtils.replaceByReg(str, "\\s*$", "");// 去掉尾的空白
        str = StringUtils.replaceByReg(str, " +", " ");
        return str;
    }


    /**
     * 去掉HTML标签之外的字符串，只保留html标签以及其内部的字符串
     *
     * @param str 源字符串
     * @return 目标字符串
     */
    public static String getInnerHTMLLabelAndString(String str) {
        str = StringUtils.replaceByReg(str, ">([^<>]+)<", "><");
        str = StringUtils.replaceByReg(str, "^([^<>]+)<", "<");
        str = StringUtils.replaceByReg(str, ">([^<>]+)$", ">");
        return str;
    }

    /**
     * 将html的省略写法替换成非省略写法
     * &lt;A/&gt;替换为&lt;A&gt;&lt;/A&gt;
     *
     * @param str html字符串
     * @param pt  标签如table
     * @return 结果串
     */
    public static String formatToFullHtmlLabel(String str, String pt) {
        String regEx = "<" + pt + "\\s+([\\S&&[^<>]]*)/>";
        Pattern p = RegExpUtils.getPattern(regEx, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        String[] sa = null;
        String sf = "";
        String sf2 = "";
        String sf3 = "";
        for (; m.find(); ) {
            sa = p.split(str);
            if (sa == null) {
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
     * 截取字符串
     *
     * @param str  原始字符串
     * @param len  要截取的长度
     * @param tail 结束加上的后缀
     * @return 截取后的字符串
     */
    public static String getHtmlSubString(String str, int len, String tail) {
        if (str == null || str.length() <= len) {
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
                // 截取标签
                tag = str.substring(i, end + 1);
                int n = tag.length();
                if (tag.endsWith("/>")) {
                    isTag = true;
                } else if (tag.startsWith("</")) { // 结束符
                    name = tag.substring(2, end - i);
                    size = tags.size() - 1;
                    // 堆栈取出html开始标签
                    if (size >= 0 && name.equals(tags.get(size))) {
                        isTag = true;
                        tags.remove(size);
                    }
                } else { // 开始符
                    spanEnd = tag.indexOf(' ', 0);
                    spanEnd = spanEnd > 0 ? spanEnd : n;
                    name = tag.substring(1, spanEnd);
                    if (name.trim().length() > 0) {
                        // 如果有结束符则为html标签
                        spanEnd = str.indexOf("</" + name + ">", end);
                        if (spanEnd > 0) {
                            isTag = true;
                            tags.add(name);
                        }
                    }
                }
                // 非html标签字符
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
            } else { // 非html标签字符
                len--;
                result += c;
            }
        }
        // 添加未结束的html标签
        for (String endTag : tags) {
            result += "</" + endTag + ">";
        }
        if (i < length) {
            result += tail;
        }
        return result;
    }


    public static void main(String arg[]) {
        String str = "<tt>sdfdf<''s''d\\s";
        str = "img src=\\\\\\\"/ASPPV1/ueditor/jsp/upload/image/20151013/1444726497211031188.png\\\\\\\" ";
        //str = toHtml(str);  
        str = transUrl("http://192.168.1.76", str);
        System.out.println("-----------str:" + str);


    }

    /**
     * 将html段落的字符串中的url从相对路径替换为绝对路径
     * 比如http://...替换为指定的prefex
     *
     * @param prefix
     * @param s
     * @return
     */
    public static String transUrl(String prefix, String s) {
        try {
            String h = s;//\\表示字符串中的一个反斜杠,\\\\表示正则表达式中的一个反斜杠
            Pattern pattern = RegExpUtils.getPattern(".*src=\\\\*\\\"(?!http://)([^\\\\][^\\s]+[^\\\\=])\\\\*\\\".*");
            Matcher matcher = pattern.matcher(s);
            boolean has = false;
            while (matcher.find()) {
                String tmp = matcher.group(1);
                //System.out.println(tmp);
                h = h.replace(tmp, prefix + tmp);
                has = true;
            }

            pattern = RegExpUtils.getPattern(".*href=\\\\*\\\"(?!http://)([^\\\\][^\\s]+[^\\\\=])\\\\*\\\".*");
            matcher = pattern.matcher(s);
            while (matcher.find()) {
                String tmp = matcher.group(1);
                //System.out.println(tmp);
                h = h.replace(tmp, prefix + tmp);
                has = true;
            }

            pattern = RegExpUtils.getPattern(".*url=\\\\*\\\"(?!http://)([^\\\\][^\\s]+[^\\\\=])\\\\*\\\".*");
            matcher = pattern.matcher(s);
            while (matcher.find()) {
                String tmp = matcher.group(1);
                //System.out.println(tmp);
                h = h.replace(tmp, prefix + tmp);
                has = true;
            }
            if (!has)
                return h;
            return transUrl(prefix, h);

        } catch (Exception e) {
            System.out.println("HtmlUtils:url相对地址转换绝对地址失败!" + e.toString());
            return s;
        }
    }

    /**
     * 删除包括script 和style以及html中的标签内部的(属性)内容,只保留标签/容器内部的内容
     * @param htmlStr
     * @return
     */
    public static String delHTMLTag(String htmlStr) {
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式
        String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式

        Pattern p_script = RegExpUtils.getPattern(regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll(""); //过滤script标签

        Pattern p_style = RegExpUtils.getPattern(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); //过滤style标签

        Pattern p_html = RegExpUtils.getPattern(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); //过滤html标签

        return htmlStr.trim(); //返回文本字符串
    }
} 
