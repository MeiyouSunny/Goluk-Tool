package com.mobnote.golukmain.msg.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class MessageMsgsBean {
	
	/**消息id**/
	@JSONField(name="messageid")
	public String messageid;
	
	/**消息类型**/
	@JSONField(name="type")
	public int type;
	
	/**消息状态**/
	@JSONField(name="status")
	public String status;
	
	/**添加时间**/
	@JSONField(name="addtime")
	public String addtime;
	
	/**更新时间**/
	@JSONField(name="edittime")
	public String edittime ;
	
	
	/**发送方**/
	@JSONField(name="sender")
	public MessageSenderBean sender;
	
	/**接收方**/
	@JSONField(name="receiver")
	public MessageReceiverBean receiver;
	
	/**消息内容**/
	@JSONField(name="content")
	public MessageContentBean content ;
	
	
}
