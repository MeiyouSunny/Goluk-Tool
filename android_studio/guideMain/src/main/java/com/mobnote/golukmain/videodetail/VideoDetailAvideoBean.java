package com.mobnote.golukmain.videodetail;

import com.alibaba.fastjson.annotation.JSONField;
import com.mobnote.golukmain.startshare.GpsInfo;

import java.util.List;

public class VideoDetailAvideoBean {
	@JSONField(name="video")
	public VideoInfo video;
	@JSONField(name="user")
	public VideoUserInfo user;
	@JSONField(name="locations")
	public List<GpsInfo> locations;

}
