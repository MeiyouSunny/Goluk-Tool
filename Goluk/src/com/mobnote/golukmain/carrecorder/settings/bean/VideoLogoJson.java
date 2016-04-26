package com.mobnote.golukmain.carrecorder.settings.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoLogoJson {

	@JSONField(name = "data")
	public VideoLogo data;

	@JSONField(name = "msg")
	public String msg;

	@JSONField(name = "result")
	public int result;
}
