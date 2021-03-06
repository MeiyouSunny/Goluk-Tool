package com.mobnote.golukmain.usercenter.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class AttentionJson {

	/** 请求返回码 **/
	@JSONField(name = "code")
	public int code;

	/** 请求返回数据 **/
	@JSONField(name = "data")
	public AttentionData data;

	/** 返回调试信息 **/
	@JSONField(name = "msg")
	public String msg;

	/** 协议版本 V1：100；V2：200 **/
	@JSONField(name = "xieyi")
	public int xieyi;

}
