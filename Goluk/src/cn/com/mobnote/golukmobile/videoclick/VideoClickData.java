package cn.com.mobnote.golukmobile.videoclick;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoClickData {

	@JSONField(name="result")
	public String result;
	
	@JSONField(name="goluk")
	public VideoClickGoluk goluk;
	
}
