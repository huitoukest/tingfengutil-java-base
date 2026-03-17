package com.tingfeng.util.java.base.common.helper;

import com.tingfeng.util.java.base.common.bean.tuple.Tuple2;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;

import java.util.*;

/**
 * 字符串模板替换处理的Helper
 * 支持自定义模板标签，预编译模板，多线程并发安全
 * 模板参数key值不能为空串
 * 
 * <p>使用示例：
 * <pre>
 * String content = "Hello ${name}, your age is ${age}";
 * StringTemplateHelper helper = new StringTemplateHelper(content);
 * Map<String, Object> params = new HashMap<>();
 * params.put("name", "John");
 * params.put("age", 30);
 * String result = helper.generate(params); // 输出: "Hello John, your age is 30"
 * </pre>
 * </p>
 * 
 * @author huitoukest
 */
public class StringTemplateHelper {
    /**
     * 默认的替换开始标记
     */
    private static final String DEFAULT_START_FLAG = "${";
    /**
     * 默认的替换结束标记
     */
    private static final String DEFAULT_END_FLAG = "}";
    /**
     * 替换开始的标记
     */
    private String startFlag = DEFAULT_START_FLAG;
    /**
     * 替换结束的标记
     */
    private String endFlag = DEFAULT_END_FLAG;
    /**
     * 模板内容
     */
    private String content;
    /**
     * 模板中的参数有哪些，参数无需包含开始结束标记
     */
    private Set<String> params;
    /**
     * 参数在content中的元信息：
     * List[Tuple2[是否是模板,模板的key值]]
     * 若不是模板，则是原始字符串的值
     */
    private List<Tuple2<Boolean,String>> paramsMeta = null;

    /**
     * 构造函数，使用自定义的开始和结束标记
     * 模板参数key值不能为空串
     * 
     * @param startFlag 替换开始的标记,empty String = 使用默认值 "${"
     * @param endFlag 替换结束的标记,empty String = 使用默认值 "}"
     * @param content 模板内容
     */
    public StringTemplateHelper(String startFlag, String endFlag, String content){
        this(startFlag, endFlag, content, StringTemplateHelper.parseParam(startFlag, endFlag, content));
    }

    /**
     * 构造函数，使用自定义的开始和结束标记，并指定参数列表
     * 模板参数key值不能为空串
     * 
     * @param startFlag 替换开始的标记,empty String = 使用默认值 "${"
     * @param endFlag 替换结束的标记,empty String = 使用默认值 "}"
     * @param content 模板内容
     * @param params 模板中的参数有哪些，参数无需包含开始结束标记
     */
    public StringTemplateHelper(String startFlag, String endFlag, String content, Set<String> params){
        if (StringUtils.isNotEmpty(startFlag)) {
            this.startFlag = startFlag;
        }
        if (StringUtils.isNotEmpty(endFlag)) {
            this.endFlag = endFlag;
        }
        this.content = content;
        this.params = params;
        if (this.params == null) {
            this.params = Collections.EMPTY_SET;
        }
        initParamsMeta();
    }

    /**
     * 构造函数，使用默认的开始和结束标记，并指定参数列表
     * 模板参数key值不能为空串
     * 
     * @param content 模板内容
     * @param params 模板中的参数有哪些，参数无需包含开始结束标记
     */
    public StringTemplateHelper(String content, Set<String> params){
        this(null,null,content,params);
    }

    /**
     * 构造函数，使用默认的开始和结束标记，自动解析参数列表
     * 模板参数key值不能为空串
     * 
     * @param content 模板内容
     */
    public StringTemplateHelper(String content){
        this(null,null,content);
    }

    /**
     * 初始化参数元信息
     * 将模板内容解析为参数和非参数部分的列表
     * 每个元素是一个 Tuple2<Boolean, String>，其中：
     * - 第一个元素表示是否是模板参数
     * - 第二个元素表示参数名（如果是模板参数）或原始字符串（如果不是模板参数）
     */
    private void initParamsMeta(){
        //下一次搜索的索引位置
        int startIndex = 0;
        int endIndex = 0;
        //上次搜索完毕时的尾部的索引位置
        int lastIndex = 0;
        int startFlagLength = this.startFlag.length();
        this.paramsMeta = new ArrayList<>(this.params.size() + 1);
        //从前往后搜索，提供效率
        while(true) {
            //处理上一次（continue）的内容;未找到匹配前缀的情况下，直接加入内容，并开始下次查找
            if(endIndex > lastIndex) {
                this.paramsMeta.add(new Tuple2<>(false, this.content.substring(lastIndex, endIndex + endFlag.length())));
                lastIndex = endIndex + this.endFlag.length();
            }
            //先搜索endIndex，避免startFlag 如${${的嵌套情况。
            endIndex = this.content.indexOf(this.endFlag,lastIndex + startFlagLength);
            if(endIndex < 0){
                break;
            }
            startIndex = StringUtils.lastIndexOf(this.content,this.startFlag,lastIndex,endIndex);
            if(startIndex < 0){
                continue;
            }

            String param = this.content.substring(startIndex + startFlagLength, endIndex);
            if(param.length() == 0){
                continue;
            }
            Boolean isParam = this.params.contains(param);
            if (!isParam) {
                continue;
            }
            //当前模板字符串之前的原始内容加入
            if (lastIndex < startIndex) {
                this.paramsMeta.add(new Tuple2<>(false, this.content.substring(lastIndex, startIndex)));
            }
            lastIndex = endIndex + this.endFlag.length();
            this.paramsMeta.add(new Tuple2<>(true, param));
        }
        //对于最后一个模板字符串之后后尾部的字符串加入
        if (lastIndex < content.length()) {
            this.paramsMeta.add(new Tuple2<>(false, this.content.substring(lastIndex)));
        }
    }

    /**
     * 生成格式化之后的字符串，支持多线程并发
     * 
     * <p>特点：
     * - 对于content中存在的模板参数，但是paramsData中没有对应数据或者为null，则不会替换，保留原始模板标签
     * - 对于map中value的值会自动转为String使用
     * - 线程安全，可在多线程环境下使用
     * </p>
     * 
     * @param paramsData 参数字典，键为参数名，值为参数值
     * @param <T> 参数值的类型
     * @return 格式化后的字符串
     */
    public <T> String generate(Map<String,T> paramsData){
        return StringUtils.doAppend(sb -> {
            paramsMeta.stream().map(it -> {
                if(it.get_1()){
                    Object data = paramsData.getOrDefault(it.get_2(),null);
                    if(data != null){
                        return data;
                    }else{
                        return startFlag + it.get_2() + endFlag;
                    }
                }else{
                    return it.get_2();
                }
            }).forEach(it -> sb.append(it) );
            return sb.toString();
        });
    }

    /**
     * 根据模板内容自动解析参数
     * 有开始结束标记的嵌套时,优先使用从左到右的第一个匹配到的数据
     * 
     * @param startFlag 替换开始的标记,empty String = 使用默认值 "${"
     * @param endFlag 替换结束的标记,empty String = 使用默认值 "}"
     * @param content 模板内容
     * @return 参数名称集合,若存在则返回对应参数值否则返回空集合
     */
    public static Set<String> parseParam(String startFlag, String endFlag, String content) {
        if(StringUtils.isEmpty(startFlag)) {
            startFlag = DEFAULT_START_FLAG;
        }
        if(StringUtils.isEmpty(endFlag)){
            endFlag = DEFAULT_END_FLAG;
        }
        int length = content.length();
        Set<String> params = new HashSet<>(16);
        for (int i = 0; i < length;) {
            int startIndex = StringUtils.indexOf(content, startFlag, i, length);
            if(startIndex < 0){
                break;
            }
            int paramStartIndex = startIndex + startFlag.length();
            int endIndex = StringUtils.indexOf(content, endFlag, paramStartIndex, length);
            if(endIndex < 0){
                break;
            }
            //即参数内容不为空的部分
            if(paramStartIndex < endIndex) {
                String param = content.substring(paramStartIndex, endIndex);
                params.add(param);
            }
            i = endIndex + endFlag.length();
        }
        return params;
    }
}