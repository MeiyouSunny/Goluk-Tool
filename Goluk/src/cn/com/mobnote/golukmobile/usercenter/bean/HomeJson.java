package cn.com.mobnote.golukmobile.usercenter.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class HomeJson {

	/** 请求返回码 **/
	@JSONField(name = "code")
	public String code;
	
	/** 请求返回数据 **/
	@JSONField(name = "msg")
	public String msg;
	
	/** 返回调试信息 **/
	@JSONField(name = "data")
	public HomeData data;
}
