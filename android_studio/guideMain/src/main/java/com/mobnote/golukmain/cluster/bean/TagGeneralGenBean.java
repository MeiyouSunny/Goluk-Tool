package com.mobnote.golukmain.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by DELL-PC on 2016/7/25.
 */
public class TagGeneralGenBean {
    /* 频道id */
    @JSONField(name="channelid")
    public String channelid;
    /* 频道名称 */
    @JSONField(name="chaname")
    public String chaname;
    /* 活动Id */
    @JSONField(name="topicid")
    public String topicid;
    /* 活动名称 */
    @JSONField(name="topicname")
    public String topicname;
    /* 推荐时间 格式：yyyyMMddHHmmssSSS */
    @JSONField(name="tjtime")
    public String tjtime;
    /* 是否推荐 "0":否; "1":是; */
    @JSONField(name="isrecommend")
    public String isrecommend;
    /* 推荐理由 */
    @JSONField(name="reason")
    public String reason;
    /* 是否获奖 "0": 否; "1": 是; */
    @JSONField(name="isreward")
    public String isreward;
    /* 是否分享获奖 "0": 否; "1": 是; */
    @JSONField(name="atflag")
    public String atflag;
    /* 分享获奖理由 */
    @JSONField(name="atreason")
    public String atreason;
    /* 分享获奖金额 */
    @JSONField(name="atgold")
    public String atgold;
    /* 分享获奖时间 */
    @JSONField(name="attime")
    public String attime;
    /* 是否系统获奖 0: 否; 1: 是; */
    @JSONField(name="sysflag")
    public String sysflag;
    /* 系统获奖理由 */
    @JSONField(name="sysreason")
    public String sysreason;
    /* 系统获奖金额 */
    @JSONField(name="sysgold")
    public String sysgold;
    /* 系统获奖时间 */
    @JSONField(name="systime")
    public String systime;
    /* 获奖总额 */
    @JSONField(name="total")
    public String total;
    /* 最后一次获奖时间 格式：yyyyMMddHHmmssSSS */
    @JSONField(name="lasttime")
    public String lasttime;
}
