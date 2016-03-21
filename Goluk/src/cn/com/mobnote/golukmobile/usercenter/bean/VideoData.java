package cn.com.mobnote.golukmobile.usercenter.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoData {

	/** 操作 **/
	@JSONField(name = "operation")
	public String operation;

	/** 视频列表 **/
	@JSONField(name = "videolist")
	public List<VideoList> videolist;

	/** 视频计数 **/
	@JSONField(name = "videocount")
	public int videocount;

}
