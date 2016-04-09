package com.mobnote.golukmain.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class ActivityBean {

	/** 活动开始时间 **/
	@JSONField(name = "starttime")
	public String starttime;

	/** 活动结束时间 **/
	@JSONField(name = "endtime")
	public String endtime;

	/** 封面图片 **/
	@JSONField(name = "picture")
	public String picture;

	/** 描述 **/
	@JSONField(name = "activitycontent")
	public String activitycontent;

	/** 参与数量 **/
	@JSONField(name = "participantcount")
	public String participantcount;

	/** 活动规则 **/
	@JSONField(name = "activityrule")
	public String activityrule;

	/** 话题唯一id **/
	@JSONField(name = "activityid")
	public String activityid;

	/** 活动（话题）名称 **/
	@JSONField(name = "activityname")
	public String activityname;

	/** 活动channel id **/
	@JSONField(name = "channelid")
	public String channelid;

	/** 聚合评论条数 */
	@JSONField(name = "commentcount")
	public String commentcount;

	/** 是否可以评论 */
	@JSONField(name = "iscomment")
	public String iscomment;
	
	/** 活动是否过期 **/
	@JSONField(name = "expiration")
	public String expiration;

	/** 投票url地址 **/
	@JSONField(name = "voteaddress")
	public String voteaddress;

	/** 投票id */
	@JSONField(name = "voteid")
	public String voteid;
}
