package com.mobnote.golukmain.msg.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class MessageContentBean {

	/** g币 **/
	@JSONField(name="gaward")
	public MesaageGawardBean gaward;
	/**跳转内容类型   **/
	@JSONField(name="type")
	public String type;

	/**跳转访问地址 **/
	@JSONField(name="access")
	public String access;

	/**封面**/
	@JSONField(name="picture")
	public String picture;

	/**时间 **/
	@JSONField(name="time")
	public String time;

	@JSONField(name="ts")
	public long ts;

	/**评论**/
	@JSONField(name="comment")
	public MessageCommentBean comment;

//	/**点赞**/
//	@JSONField(name="like")
//	public object like ;
//
//	/**聊天**/
//	@JSONField(name="chat")
//	public object chat ;

	/**获奖**/
	@JSONField(name="reward")
	public MessageRewardBean reward;

	/**推荐**/
	@JSONField(name="recommend")
	public MessageRecommendBean recommend;

	/**认证**/
	@JSONField(name="certificate")
	public MessageCertificateBean certificate;

	/**提现**/
	@JSONField(name="withdraw")
	public MessageWithdrawBean withdraw;

	/**通知**/
	@JSONField(name="anycast")
	public MessageAnycastBean anycast;
	
}
