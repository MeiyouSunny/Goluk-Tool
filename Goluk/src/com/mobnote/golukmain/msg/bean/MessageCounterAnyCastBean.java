package com.mobnote.golukmain.msg.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class MessageCounterAnyCastBean {
	/**通知类型  0：组播；1：广播 **/
	@JSONField(name="type")
	public String type;
	
	/**通知内容 **/
	@JSONField(name="text")
	public String text;
	
}
