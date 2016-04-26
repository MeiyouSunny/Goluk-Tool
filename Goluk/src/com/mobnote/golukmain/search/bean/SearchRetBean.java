package com.mobnote.golukmain.search.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class SearchRetBean {

	@JSONField(name="code")
	public int code;
	@JSONField(name="data")
	public SearchDataBean data;
	@JSONField(name="msg")
	public String msg;
	@JSONField(name="xieyi")
	public int xieyi;
}
