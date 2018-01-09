package com.mobnote.golukmain.followed.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.mobnote.golukmain.cluster.bean.TagTagsBean;

import java.util.List;

public class FollowedVideoBean {

	@JSONField(name="category")
	public String category;

	@JSONField(name="videoid")
	public String videoid;
	// 视频类型: 1.直播 2.点播
	@JSONField(name="type")
	public String type;
	@JSONField(name="sharingtime")
	public String sharingtime;
	@JSONField(name="sharingts")
	public long sharingts;
	@JSONField(name="describe")
	public String describe;
	@JSONField(name="picture")
	public String picture;
	@JSONField(name="clicknumber")
	public String clicknumber;
	@JSONField(name="praisenumber")
	public String praisenumber;
	@JSONField(name="starttime")
	public String starttime;
	@JSONField(name="livetime")
	public String livetime;
	@JSONField(name="livewebaddress")
	public String livewebaddress;
	@JSONField(name="livesdkaddress")
	public String livesdkaddress;
	@JSONField(name="ondemandwebaddress")
	public String ondemandwebaddress;
	@JSONField(name="ondemandsdkaddress")
	public String ondemandsdkaddress;
	// 是否点过赞: 0.否1.是
	@JSONField(name="ispraise")
	public String ispraise;
	@JSONField(name="videodata")
	public FollowedVideoDataBean videodata;
	@JSONField(name="reason")
	public String reason;
	@JSONField(name="comment")
	public FollowedCommentBean comment;
	// 是否为精华 0.否1.是
	@JSONField(name="isessence")
	public String isessence;
	// 是否公开到广场 0.否1.是
	@JSONField(name="isopen")
	public String isopen;
	@JSONField(name="location")
	public String location;
	@JSONField(name="gen")
	public FollowedGenBean gen;
	// GPS信息是否分享 0.否; 1.是
	@JSONField(name="isgpsshare")
	public int isgpsshare;
	// 设备型号：G1,G2,T1 [2.9]
	@JSONField(name="devicetag")
	public String devicetag;
	// 操作系统: ios, android, windows(小写)[2.9]
	@JSONField(name="ostag")
	public String ostag;
	// 视频清晰度 480p, 1080p
	@JSONField(name="resolution")
	public String resolution;
	@JSONField(name="tags")
	public List<TagTagsBean> tags;
}
