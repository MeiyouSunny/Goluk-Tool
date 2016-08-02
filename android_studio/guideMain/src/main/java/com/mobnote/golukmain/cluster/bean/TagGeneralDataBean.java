package com.mobnote.golukmain.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by DELL-PC on 2016/7/25.
 */
public class TagGeneralDataBean {
    /* 操作 0: 首次进入; 1: 下拉; 2: 上拉 */
    @JSONField(name="operation")
    public String operation;
    /* 视频总数 */
    @JSONField(name="videocount")
    public String videocount;
    /* 视频列表 */
    @JSONField(name="videolist")
    public List<TagGeneralVideoListBean> videolist;
}

