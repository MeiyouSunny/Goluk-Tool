package com.mobnote.golukmain.followed.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowedVideoObjectBean {
	@JSONField(name="video")
	public FollowedVideoBean video;
	@JSONField(name="user")
	public FollowedUserBean user;
}
