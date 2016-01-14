package cn.com.mobnote.golukmobile.msg.bean;


import com.alibaba.fastjson.annotation.JSONField;

public class MessageCounterDataBean {
	/** 返回调试信息 **/
	@JSONField(name = "result")
	public String result;

	/** 请求返回数据 **/
	@JSONField(name = "messagecount")
	public MessageCounterDetailBean messagecount;
}
