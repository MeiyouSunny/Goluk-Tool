package cn.com.mobnote.golukmobile.msg.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class MessageCounterBean {
	/** 请求是否成功 **/
	@JSONField(name = "success")
	public boolean success;

	/** 请求返回数据 **/
	@JSONField(name = "data")
	public MessageCounterDataBean data;

	/** 返回调试信息 **/
	@JSONField(name = "msg")
	public String msg;
}
