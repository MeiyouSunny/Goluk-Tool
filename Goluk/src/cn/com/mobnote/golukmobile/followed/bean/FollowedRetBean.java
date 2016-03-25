package cn.com.mobnote.golukmobile.followed.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowedRetBean {
	@JSONField(name="success")
	public boolean success;

	@JSONField(name="data")
	public FollowedDataBean data;

	@JSONField(name="msg")
	public String msg;
}