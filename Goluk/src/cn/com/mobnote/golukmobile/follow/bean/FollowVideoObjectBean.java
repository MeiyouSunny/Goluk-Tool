package cn.com.mobnote.golukmobile.follow.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowVideoObjectBean {
	@JSONField(name="video")
	public FollowVideoBean video;
	@JSONField(name="user")
	public FollowUserBean user;
}
