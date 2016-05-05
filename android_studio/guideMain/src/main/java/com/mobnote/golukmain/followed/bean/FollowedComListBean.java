package com.mobnote.golukmain.followed.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowedComListBean {
	@JSONField(name="commentid")
	public String commentid;
	@JSONField(name="authorid")
	public String authorid;
	@JSONField(name="name")
	public String name;
	@JSONField(name="avatar")
	public String avatar;
	@JSONField(name="time")
	public String time;
	@JSONField(name="text")
	public String text;
	@JSONField(name="replyid")
	public String replyid;
	@JSONField(name="replyname")
	public String replyname;
	@JSONField(name="customavatar")
	public String customavatar;
}
