package com.mobnote.golukmain.FollowCount.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by leege100 on 16/7/7.
 */
public class FollowCountDataBean {
    @JSONField(name="uid")
    public String uid;
    @JSONField(name="following")
    public int following;
    @JSONField(name="fans")
    public int fans;
    @JSONField(name="friend")
    public int friend;
    @JSONField(name="newfans")
    public int newfans;
    @JSONField(name="newvideo")
    public int newvideo;
}
