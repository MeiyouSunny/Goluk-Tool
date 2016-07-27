package com.mobnote.golukmain.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by DELL-PC on 2016/7/26.
 */
public class TagsBean {
    /* 标签id */
    @JSONField(name="tagid")
    public String tagid;
    /* 标签名称 */
    @JSONField(name="name")
    public String name;
    /* 标签类型 0: 普通; 1: 活动 */
    @JSONField(name="type")
    public int type;
}
