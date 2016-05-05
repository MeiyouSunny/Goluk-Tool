package com.mobnote.golukmain.userinfohome.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class UserinfohomeRetBean {
	
	/** 是否成功 **/
	@JSONField(name = "success")
	public boolean success;
	
	/** msg **/
	@JSONField(name = "msg")
	public String msg;
	
	/** data **/
	@JSONField(name = "data")
	public UserinfohomeDataBean data;
}
