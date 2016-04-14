package com.mobnote.golukmain.msg.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class MessageCounterSystemBean {
	/** 用户消息总数 **/
	@JSONField(name = "total")
	public int total;

	/** 系统获奖消息数 **/
	@JSONField(name = "rewardsystem")
	public int rewardsystem;

	/** 手动获奖消息数 **/
	@JSONField(name = "rewardmanual")
	public int rewardmanual;

	/** 推荐消息数 **/
	@JSONField(name = "recommend")
	public int recommend;

	/** 认证消息数 **/
	@JSONField(name = "certificate")
	public int certificate;

	/** 提现消息数 **/
	@JSONField(name = "withdraw")
	public int withdraw;
}
