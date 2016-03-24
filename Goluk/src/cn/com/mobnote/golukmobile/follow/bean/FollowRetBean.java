package cn.com.mobnote.golukmobile.follow.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowRetBean {
	@JSONField(name="success")
	public boolean success;

	@JSONField(name="data")
	public FollowDataBean data;

	@JSONField(name="msg")
	public String msg;
}