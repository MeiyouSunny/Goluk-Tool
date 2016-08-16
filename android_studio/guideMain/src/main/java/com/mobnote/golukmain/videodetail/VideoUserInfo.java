package com.mobnote.golukmain.videodetail;

import com.alibaba.fastjson.annotation.JSONField;
import com.mobnote.golukmain.cluster.bean.UserLabelBean;
import com.mobnote.golukmain.userbase.bean.CertificationBean;

public class VideoUserInfo {
    @JSONField(name = "uid")
    public String uid;

    @JSONField(name = "nickname")
    public String nickname;

    @JSONField(name = "headportrait")
    public String headportrait;
    /**
     * 头像网络地址
     */
    @JSONField(name = "customavatar")
    public String customavatar;
    @JSONField(name = "sex")
    public String sex;

    /**
     * 个性签名
     **/
    @JSONField(name = "desc")
    public String desc;
    /**
     * 认证信息
     **/
    @JSONField(name = "certification")
    public CertificationBean certification;

    @JSONField(name = "label")
    public UserLabelBean label;

    /**
     * 连接类型
     **/
    @JSONField(name = "link")
    public int link;
}
