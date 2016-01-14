package cn.com.mobnote.golukmobile.msg.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class MessageCounterUserBean {
	/** 用户消息总数 **/
	@JSONField(name = "total")
	public int total;

	/** 评论消息数 **/
	@JSONField(name = "comment")
	public int comment;

	/** 点赞消息数 **/
	@JSONField(name = "like")
	public int like;

	/** 聊天消息数 **/
	@JSONField(name = "chat")
	public int chat;
}
