package com.mobnote.golukmain.videodetail;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoDetailRetBean {
	@JSONField(name="success")
	public boolean success;
	@JSONField(name="data")
	public VideoDetailDataBean data;
	@JSONField(name="msg")
	public String msg;
}
