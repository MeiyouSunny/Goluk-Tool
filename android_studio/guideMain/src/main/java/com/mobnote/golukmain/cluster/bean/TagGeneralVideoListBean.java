package com.mobnote.golukmain.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by DELL-PC on 2016/7/25.
 */
public class TagGeneralVideoListBean {
    /* 视频属性 */
    @JSONField(name="video")
    public TagGeneralVideoBean video;
    /* 用户属性 */
    @JSONField(name="user")
    public TagGeneralUserBean user;
}
