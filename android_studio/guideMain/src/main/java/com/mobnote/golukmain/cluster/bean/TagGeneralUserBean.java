package com.mobnote.golukmain.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by DELL-PC on 2016/7/25.
 */
public class TagGeneralUserBean {
    /* 用户唯一id */
    @JSONField(name="uid")
    public String uid;
    /* 用户昵称 */
    @JSONField(name="nickname")
    public String nickname;
    /* 用户头像 */
    @JSONField(name="headportrait")
    public String headportrait;
    /* 自定义头像URL 如果为空的话取"headportrait"参数值 */
    @JSONField(name="customavatar")
    public String customavatar;
    /* 性别 0: 男; 1: 女 */
    @JSONField(name="sex")
    public String sex;
    /* 认证标签 */
    @JSONField(name="label")
    public TagGeneralLabelBean label;
}
