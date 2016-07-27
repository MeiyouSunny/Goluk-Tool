package com.mobnote.golukmain.newest.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by DELL-PC on 2016/7/25.
 */
public class NewestCommentBean {
    /* 是否显示评论 0: 否; 1: 是 */
    @JSONField(name="iscomment")
    public String iscomment;
    /* 评论数 */
    @JSONField(name="comcount")
    public String comcount;
    /* 评论列表 */
    @JSONField(name="comlist")
    public List<NewestComListBean> comlist;
}
