package cn.com.mobnote.golukmobile.followed.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowVideoObjectBean {
	@JSONField(name="video")
	public FollowVideoBean video;
	@JSONField(name="user")
	public FollowUserBean user;
}
