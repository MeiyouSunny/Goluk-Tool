package com.mobnote.golukmain.newest.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by DELL-PC on 2016/7/25.
 */
public class NewestVideoListBean {
    /* 视频属性 */
    @JSONField(name="video")
    public NewestVideoBean video;
    /* 用户属性 */
    @JSONField(name="user")
    public NewestUserBean user;
}
