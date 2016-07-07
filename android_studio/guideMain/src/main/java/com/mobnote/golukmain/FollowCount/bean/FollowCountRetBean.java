package com.mobnote.golukmain.FollowCount.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.mobnote.golukmain.follow.bean.FollowAllDataBean;

/**
 * Created by leege100 on 16/7/7.
 */
public class FollowCountRetBean {
    @JSONField(name="code")
    public int code;
    @JSONField(name="data")
    public FollowCountDataBean data;
    @JSONField(name="msg")
    public String msg;
    // V1: 100; V2: 200
    @JSONField(name="xieyi")
    public int xieyi;
}
