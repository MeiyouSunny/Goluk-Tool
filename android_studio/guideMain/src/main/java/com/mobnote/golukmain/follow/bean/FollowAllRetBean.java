package com.mobnote.golukmain.follow.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowAllRetBean {
	@JSONField(name="code")
	public int code;
	@JSONField(name="data")
	public FollowAllDataBean data;
	@JSONField(name="msg")
	public String msg;
	// V1: 100; V2: 200
	@JSONField(name="xieyi")
	public int xieyi;
}
