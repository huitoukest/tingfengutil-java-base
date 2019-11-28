package com.tingfeng.util.java.base.common.helper;

import com.tingfeng.util.java.base.common.bean.tuple.Tuple2;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;

import java.util.*;

/**
 * 字符串模板替换处理的Helper
 * 模板参数key值不能为空串
 * @author huitoukest
 */
public class StringTemplateHelper {
    /**
     * 替换开始的标记
     */
    private String startFlag = "${";
    /**
     * 替换结束的标记
     */
    private String endFlag = "}";
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
     * List[Tuple3[是否是模板,模板的key值]]
     * 若不是模板，则是原始字符串的值
     */
    private List<Tuple2<Boolean,String>> paramsMeta = null;

    /**
     * 模板参数key值不能为空串
     * @param startFlag 替换开始的标记,empty String = 使用默认值
     * @param endFlag 替换结束的标记,empty String = 使用默认值
     * @param content 模板内容
     * @param params  模板中的参数有哪些，参数无需包含开始结束标记
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
     * 模板参数key值不能为空串
     * @param content
     * @param params
     */
    public StringTemplateHelper(String content, Set<String> params){
        this(null,null,content,params);
    }

    /**
     * 初始化参数元信息
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
            startIndex = this.content.indexOf(this.startFlag,endIndex);
            if(startIndex < 0){
                break;
            }
            endIndex = this.content.indexOf(this.endFlag,startIndex + startFlagLength);
            if(endIndex < 0){
                break;
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
     * 生成格式化之后的字符串，支持多线程并发;
     * 对于content中存在的模板参数，但是paramsData中没有对应数据或者为null，则不会替换
     * @param paramsData
     * @return
     */
    public String generate(Map<String,String> paramsData){
        return StringUtils.doAppend(sb -> {
            paramsMeta.stream().map(it -> {
                if(it.get_1()){
                    String data = paramsData.getOrDefault(it.get_2(),null);
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
}
