package com.mobnote.golukmain.msg.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class MessageDataBean {
	
	/** 结果代码  0:成功 1:参数错误 2:未知异常 **/
	@JSONField(name = "result")
	public String result;
	
	/** 消息列表  **/
	@JSONField(name = "messages")
	public List<MessageMsgsBean> messages;

	/** pull down, pull up, normal  **/
	public String operation;
}
