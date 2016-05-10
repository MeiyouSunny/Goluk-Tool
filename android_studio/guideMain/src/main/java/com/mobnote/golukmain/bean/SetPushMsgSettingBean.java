package com.mobnote.golukmain.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class SetPushMsgSettingBean {
	@JSONField(name="msg")
	public String msg;
	@JSONField(name="success")
	public boolean success;
	@JSONField(name="data")
	public PushMsgSettingData data;
}
