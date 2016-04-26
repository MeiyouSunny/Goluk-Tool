package com.mobnote.golukmain.recommend.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class RecommendRetBean {

	@JSONField(name = "code")
	int code;
	@JSONField(name = "msg")
	String msg;
	@JSONField(name = "xieyi")
	int xieyi;
	@JSONField(name = "data")
	RecommendDataBean data;
}
