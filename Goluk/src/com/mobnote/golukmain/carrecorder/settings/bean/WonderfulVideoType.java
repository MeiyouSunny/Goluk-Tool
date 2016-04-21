package com.mobnote.golukmain.carrecorder.settings.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class WonderfulVideoType {

	/** 紧急视频（前） **/
	@JSONField(name = "urgent_history_time")
	public int urgent_history_time;

	/** 紧急视频（后） **/
	@JSONField(name = "urgent_future_time")
	public int urgent_future_time;

	/** 精彩视频（前） **/
	@JSONField(name = "wonder_history_time")
	public int wonder_history_time;

	/** 精彩视频（后） **/
	@JSONField(name = "wonder_future_time")
	public int wonder_future_time;

}
