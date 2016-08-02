package com.mobnote.golukmain.live;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class LiveDataInfo implements Serializable {

    private static final long serialVersionUID = -8534844170998963067L;
    /**
     * 访问状态 200表示成功，其它表示结束
     */
    @JSONField(name="code")
    public int code;
    /**
     * 目前不做处理
     */
    @JSONField(name="state")
    public boolean state;
    /**
     * 主动直播标识 1: 表示主动直播 2：表示被动直播
     */
    @JSONField(name="active")
    public int active;
    /**
     * 视频播放地址
     */
    public String vurl;
    /**
     * 视频ID
     */
    public String vid;
    /**
     * 是否可以声音
     */
    public String voice;

    /**
     * 群组Id
     */
    public String groupId;
    public String groupnumber;
    public String groupType;
    public int membercount;
    public String title;
    /**
     * 视频剩余时间
     */
    public int restime;
    /**
     * 视频描述
     */
    public String desc;

}
