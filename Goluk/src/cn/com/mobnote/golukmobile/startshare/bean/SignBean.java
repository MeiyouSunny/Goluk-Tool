package cn.com.mobnote.golukmobile.startshare.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class SignBean {
	@JSONField(name="msg")
	public String msg;
	@JSONField(name="success")
	public boolean success;
	@JSONField(name="data")
	public SignDataBean data;
}
