package cn.com.mobnote.golukmobile.usercenter.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoJson {

	/** 请求返回码 **/
	@JSONField(name = "code")
	public int code;

	/** 请求返回数据 **/
	@JSONField(name = "msg")
	public String msg;

	/** 返回调试信息 **/
	@JSONField(name = "data")
	public VideoData data;
}
