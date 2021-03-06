package com.mobnote.golukmain.followed.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowedUserBean {
	@JSONField(name="uid")
	public String uid;
	@JSONField(name="nickname")
	public String nickname;
	@JSONField(name="headportrait")
	public String headportrait;
	// 自定义头像URL 如果为空的话取“headportrait”参数值
	@JSONField(name="customavatar")
	public String customavatar;
	//性别 0：男；1：女
	@JSONField(name="sex")
	public String sex;
	@JSONField(name="label")
	public FollowedLabelBean label;
}
