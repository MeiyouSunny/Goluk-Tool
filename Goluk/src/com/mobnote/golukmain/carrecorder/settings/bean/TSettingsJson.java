package com.mobnote.golukmain.carrecorder.settings.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class TSettingsJson {

	@JSONField(name = "data")
	public TSettingsData data;

	@JSONField(name = "msg")
	public String msg;

	@JSONField(name = "result")
	public int result;
}
