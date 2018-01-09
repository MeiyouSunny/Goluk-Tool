package com.mobnote.golukmain.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by DELL-PC on 2016/7/25.
 */
public class TagGeneralVideoBean {
    /* 视频唯一id */
    @JSONField(name="videoid")
    public String videoid;
    /* 视频类型：1.直播2.点播 */
    @JSONField(name="type")
    public String type;
    /* 视频分享时间 */
    @JSONField(name="sharingtime")
    public String sharingtime;
    @JSONField(name="sharingts")
    public long sharingts;
    /* 视频描述 */
    @JSONField(name="describe")
    public String describe;
    /* 视频图片 */
    @JSONField(name="picture")
    public String picture;
    /* 点击次数 */
    @JSONField(name="clicknumber")
    public String clicknumber;
    /* 点赞次数 */
    @JSONField(name="praisenumber")
    public String praisenumber;
    /* 直播开始时间 */
    @JSONField(name="starttime")
    public String starttime;
    /* 直播时间 */
    @JSONField(name="livetime")
    public String livetime;
    /* 直播web地址 */
    @JSONField(name="livewebaddress")
    public String livewebaddress;
    /* 直播sdk地址 */
    @JSONField(name="livesdkaddress")
    public String livesdkaddress;
    /* 点播web地址 */
    @JSONField(name="ondemandwebaddress")
    public String ondemandwebaddress;
    /* 点播sdk地址 */
    @JSONField(name="ondemandsdkaddress")
    public String ondemandsdkaddress;
    /* 是否点过赞: 0. 否 1. 是 */
    @JSONField(name="ispraise")
    public String ispraise;
    /* 直播数据 */
    @JSONField(name="videodata")
    public TagGeneralVideoDataBean videodata;
    /* 推荐理由 */
    @JSONField(name="reason")
    public String reason;
    /* 	视频评论 */
    @JSONField(name="comment")
    public TagGeneralCommentBean comment;
    /* 是否为精华 0.否 1.是 */
    @JSONField(name="isessence")
    public String isessence;
    /* 是否公开到广场 0.否 1.是 */
    @JSONField(name="isopen")
    public String isopen;
    /* 用户当前位置 */
    @JSONField(name="location")
    public String location;
    /* 参加活动信息 */
    @JSONField(name="gen")
    public TagGeneralGenBean gen;
    /* GPS信息是否分享 0.否; 1.是 */
    @JSONField(name="isgpsshare")
    public int isgpsshare;
    /* 设备型号: G1,G2,T1 [2.9] */
    @JSONField(name="devicetag")
    public String devicetag;
    /* 操作系统: ios, android, windows （小写）[2.9] */
    @JSONField(name="ostag")
    public String ostag;
    /* 视频清晰度 480p, 1080p */
    @JSONField(name="resolution")
    public String resolution;
    /* 视频分类: 参考 视频分类 */
    @JSONField(name="category")
    public String category;
    /* 标签 */
    @JSONField(name="tags")
    public List<TagTagsBean> tags;
}
