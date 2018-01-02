package com.mobnote.golukmain.videodetail;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoListInfo {
	@JSONField(name="avatar")
	public String avatar;
	@JSONField(name="name")
	public String name;
	@JSONField(name="text")
	public String text;
	@JSONField(name="time")
	public String time;
	@JSONField(name="ts")
	public long ts;
	@JSONField(name="authorid")
	public String authorid;
	@JSONField(name="commentid")
	public String commentid;
	@JSONField(name="replyname")
	public String replyname;
	@JSONField(name="replyid")
	public String replyid;
	@JSONField(name="customavatar")
	public String customavatar;
}
