package com.mobnote.golukmain.videodetail;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoDetailDataBean {
	@JSONField(name="result")
	public String result;
	@JSONField(name="avideo")
	public VideoDetailAvideoBean avideo;
	@JSONField(name="link")
	public VideoLink link;
	@JSONField(name="head")
	public ZTHead head;
}
