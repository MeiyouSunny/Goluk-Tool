package com.mobnote.golukmain.carrecorder.settings.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class TSettingsData {

	@JSONField(name = "total")
	public int total;

	@JSONField(name = "list")
	public String[] list;
}
