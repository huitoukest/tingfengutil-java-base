package com.tingfeng.util.java.base.common.bean;


public class WechatServiceUserInfo {
    /**
     * 用户是否订阅该公众号标识，值为0时，代表此用户没有关注该公众号，拉取不到其余信息。
     * 1 = 已经订阅
     */
    private Integer subscribe;
    private String openid;
    private String nickname;
    /**
     * 只有在用户将公众号绑定到微信开放平台帐号后，才会出现该字段。
     */
    private String unionid;
    /**
     * 用户的性别，值为1时是男性，值为2时是女性，值为0时是未知
     */
    private Byte sex;
    private String language;
    private String city;
    private String province;
    private String country;
    /**
     * 用户头像，最后一个数值代表正方形头像大小（有0、46、64、96、132数值可选，0代表640*640正方形头像）
     * ，用户没有头像时该项为空。若用户更换头像，原有头像URL将失效。
     * 如：http://thirdwx.qlogo.cn/mmopen/xbIQx1GRqdvyqkMMhEaGOX802l1CyqMJNgUzKP8MeAeHFicRDSnZH7FY4XB7p8XHXIf6uJA2SCunTPicGKezDC4saKISzRj3nz/0
     */
    private String headimgurl;
    /**
     * 用户关注时间，为时间戳。如果用户曾多次关注，则取最后关注时间
     */
    private Long subscribe_time;
    /**
     * 公众号运营者对粉丝的备注，公众号运营者可在微信公众平台用户管理界面对粉丝添加备注
     */
    private String remark;
    /**
     * 用户所在的分组ID（兼容旧的用户分组接口）
     */
    private Integer groupid;
    /**
     * 用户被打上的标签ID列表,json数组
     */
    private String tagid_list;
    /**
     * 返回用户关注的渠道来源，ADD_SCENE_SEARCH 公众号搜索，ADD_SCENE_ACCOUNT_MIGRATION 公众号迁移，
     * ADD_SCENE_PROFILE_CARD 名片分享，ADD_SCENE_QR_CODE 扫描二维码，ADD_SCENEPROFILE LINK 图文页内名称点击，
     * ADD_SCENE_PROFILE_ITEM 图文页右上角菜单，ADD_SCENE_PAID 支付后关注，ADD_SCENE_OTHERS 其他
     */
    private String subscribe_scene;
    /**
     * 维码扫码场景（开发者自定义）
     */
    private String qr_scene;
    /**
     * 二维码扫码场景描述（开发者自定义）
     */
    private String qr_scene_str;

    public Integer getSubscribe() {
        return subscribe;
    }

    public WechatServiceUserInfo setSubscribe(Integer subscribe) {
        this.subscribe = subscribe;
        return this;
    }

    public String getOpenid() {
        return openid;
    }

    public WechatServiceUserInfo setOpenid(String openid) {
        this.openid = openid;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public WechatServiceUserInfo setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public String getUnionid() {
        return unionid;
    }

    public WechatServiceUserInfo setUnionid(String unionid) {
        this.unionid = unionid;
        return this;
    }

    public Byte getSex() {
        return sex;
    }

    public WechatServiceUserInfo setSex(Byte sex) {
        this.sex = sex;
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public WechatServiceUserInfo setLanguage(String language) {
        this.language = language;
        return this;
    }

    public String getCity() {
        return city;
    }

    public WechatServiceUserInfo setCity(String city) {
        this.city = city;
        return this;
    }

    public String getProvince() {
        return province;
    }

    public WechatServiceUserInfo setProvince(String province) {
        this.province = province;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public WechatServiceUserInfo setCountry(String country) {
        this.country = country;
        return this;
    }

    public String getHeadimgurl() {
        return headimgurl;
    }

    public WechatServiceUserInfo setHeadimgurl(String headimgurl) {
        this.headimgurl = headimgurl;
        return this;
    }

    public Long getSubscribe_time() {
        return subscribe_time;
    }

    public WechatServiceUserInfo setSubscribe_time(Long subscribe_time) {
        this.subscribe_time = subscribe_time;
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public WechatServiceUserInfo setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    public Integer getGroupid() {
        return groupid;
    }

    public WechatServiceUserInfo setGroupid(Integer groupid) {
        this.groupid = groupid;
        return this;
    }

    public String getTagid_list() {
        return tagid_list;
    }

    public WechatServiceUserInfo setTagid_list(String tagid_list) {
        this.tagid_list = tagid_list;
        return this;
    }

    public String getSubscribe_scene() {
        return subscribe_scene;
    }

    public WechatServiceUserInfo setSubscribe_scene(String subscribe_scene) {
        this.subscribe_scene = subscribe_scene;
        return this;
    }

    public String getQr_scene() {
        return qr_scene;
    }

    public WechatServiceUserInfo setQr_scene(String qr_scene) {
        this.qr_scene = qr_scene;
        return this;
    }

    public String getQr_scene_str() {
        return qr_scene_str;
    }

    public WechatServiceUserInfo setQr_scene_str(String qr_scene_str) {
        this.qr_scene_str = qr_scene_str;
        return this;
    }
}
