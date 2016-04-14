package com.mobnote.golukmain.follow.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowRetBean {
	@JSONField(name="code")
	public int code;
	@JSONField(name="data")
	public FollowDataBean data;
	@JSONField(name="msg")
	public String msg;
	// V1: 100; V2: 200
	@JSONField(name="xieyi")
	public String xieyi;
}
