package com.mobnote.golukmain.usercenter.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class ShareJson {

	/** 请求是否成功 true：成功；false：失败 **/
	@JSONField(name = "success")
	public boolean success;

	/** 请求返回数据 **/
	@JSONField(name = "data")
	public ShareData data;

	/** 返回调试信息 **/
	@JSONField(name = "msg")
	public String msg;

}
