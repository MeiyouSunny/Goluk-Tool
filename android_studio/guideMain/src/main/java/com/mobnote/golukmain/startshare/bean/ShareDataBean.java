package com.mobnote.golukmain.startshare.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class ShareDataBean {
	@JSONField(name="result")
	public String result;
	@JSONField(name="shorturl")
	public String shorturl;
	@JSONField(name="coverurl")
	public String coverurl;
	@JSONField(name="describe")
	public String describe;
}
