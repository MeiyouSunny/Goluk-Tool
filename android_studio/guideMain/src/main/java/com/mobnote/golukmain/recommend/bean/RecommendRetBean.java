package com.mobnote.golukmain.recommend.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class RecommendRetBean {

	@JSONField(name = "code")
	public int code;
	@JSONField(name = "msg")
	public String msg;
	@JSONField(name = "xieyi")
	public int xieyi;
	@JSONField(name = "data")
	public RecommendDataBean data;
}
