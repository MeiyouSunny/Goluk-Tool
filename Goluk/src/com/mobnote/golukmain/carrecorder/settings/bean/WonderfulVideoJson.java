package com.mobnote.golukmain.carrecorder.settings.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class WonderfulVideoJson {

	@JSONField(name = "data")
	public WonderfulVideoType data;

	@JSONField(name = "msg")
	public String msg;

	@JSONField(name = "result")
	public int result;

}
