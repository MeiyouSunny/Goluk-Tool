package cn.com.mobnote.golukmobile.videodetail;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoJson {
	@JSONField(name="success")
	public boolean success;
	@JSONField(name="data")
	public VideoAllData data;
	@JSONField(name="msg")
	public String msg;
}
