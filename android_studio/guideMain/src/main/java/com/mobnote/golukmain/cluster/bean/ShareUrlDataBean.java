package com.mobnote.golukmain.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class ShareUrlDataBean {
    /**
     * 短网址
     **/
    @JSONField(name = "shorturl")
    public String shorturl;

    /**
     * 封面地址
     **/
    @JSONField(name = "coverurl")
    public String coverurl;

    /**
     * 活动名称
     **/
    @JSONField(name = "name")
    public String name;

    /**
     * 活动描述
     **/
    @JSONField(name = "description")
    public String description;
}
