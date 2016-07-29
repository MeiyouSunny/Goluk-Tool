package com.mobnote.golukmain.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by DELL-PC on 2016/7/29.
 */
public class TagDataBean {
    /* 标签类型 0: 普通; 1: 活动标签 */
    @JSONField(name="type")
    public int type;
    /* 标签信息 普通标签 */
    @JSONField(name="tag")
    public TagTagBean tag;
    /* 活动信息 活动标签 */
    @JSONField(name="activity")
    public TagActivityBean activity;
}
