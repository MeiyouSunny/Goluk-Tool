package com.mobnote.golukmain.msg.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class MessageRewardBean {

	/**获奖类型**/
	@JSONField(name="type")
	public String type ;
	
	/**获奖数量**/
	@JSONField(name="count")
	public String count;
	
	/**获奖理由**/
	@JSONField(name="reason")
	public String reason;

}
