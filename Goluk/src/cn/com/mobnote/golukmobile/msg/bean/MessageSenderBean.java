package cn.com.mobnote.golukmobile.msg.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class MessageSenderBean {

	/**发送方id **/
	@JSONField(name="uid")
	public String uid ;
	
	/**发送方呢称**/
	@JSONField(name="name")
	public String name;
	
	/**系统头像**/
	@JSONField(name="avatar")
	public String avatar;
	
	/**自定义头像**/
	@JSONField(name="customavatar")
	public String customavatar;
	
	/**认证信息**/
	@JSONField(name="label")
	public MessageLabelBean label ;
	

}
