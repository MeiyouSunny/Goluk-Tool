package com.mobnote.golukmain.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by DELL-PC on 2016/7/29.
 */
public class TagTagBean {
    /* 标签id */
    @JSONField(name="tagid")
    public String tagid;
    /* 标签类型 0: 普通; 1: 活动标签 */
    @JSONField(name="type")
    public int type;
    /* 活动(话题)名称 */
    @JSONField(name="name")
    public String name;
    /* 活动描述 */
    @JSONField(name="description")
    public String description;
    /* 活动封面 */
    @JSONField(name="picture")
    public String picture;
    /* 参与人次 */
    @JSONField(name="participantcount")
    public int participantcount;
    /* 是否允许评论 0: 否; 1: 是 */
    @JSONField(name="iscomment")
    public int iscomment;
    /* 评论数 */
    @JSONField(name="commentcount")
    public int commentcount;
}
