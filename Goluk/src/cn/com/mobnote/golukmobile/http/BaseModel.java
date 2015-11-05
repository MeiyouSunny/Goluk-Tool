package cn.com.mobnote.golukmobile.http;

import com.alibaba.fastjson.annotation.JSONField;

public class BaseModel {
	@JSONField(name="msg")
	public String msg;
	@JSONField(name="success")
	public boolean success;
}
