package com.mobnote.golukmain.videodetail;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoSquareDetailInfo {
	@JSONField(name="video")
	public VideoInfo video;
	@JSONField(name="user")
	public VideoUserInfo user;

}
