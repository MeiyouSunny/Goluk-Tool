package cn.com.mobnote.golukmobile.msg.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class MessageCounterDetailBean {
	/** 消息计数id **/
	@JSONField(name = "messagecountid")
	public String messagecountid;

	/** uid **/
	@JSONField(name = "uid")
	public String uid;

	/** 消息总计 **/
	@JSONField(name = "total")
	public int total;

	/** 用户消息计数 **/
	@JSONField(name = "user")
	public MessageCounterUserBean user;

	/** 系统消息计数 **/
	@JSONField(name = "system")
	public MessageCounterSystemBean system;

	/** 通知消息计数 **/
	@JSONField(name = "anycast")
	public MessageCounterAnyCastBean anycast;
}
