package com.mobnote.golukmain.videoclick;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoClickInfo {

	@JSONField(name="data")
	public VideoClickData data;
	
	@JSONField(name="msg")
	public String msg;
	
	@JSONField(name="success")
	public boolean success; 
	
}
