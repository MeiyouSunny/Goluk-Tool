package cn.com.mobnote.golukmobile.msg.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class MessageReceiverBean {

	/**接收方id  **/
	@JSONField(name="uid")
	public String uid ;
	
	/**接收方呢称 **/
	@JSONField(name="name")
	public String name;
	
}
