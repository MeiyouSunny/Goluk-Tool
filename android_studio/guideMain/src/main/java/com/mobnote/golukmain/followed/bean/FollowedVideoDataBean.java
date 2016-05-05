package com.mobnote.golukmain.followed.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowedVideoDataBean {
	@JSONField(name="aid")
	public String aid;
	// 设备id
	@JSONField(name="mid")
	public String mid;
	@JSONField(name="active")
	public String active;
	@JSONField(name="tag")
	public String tag;
	@JSONField(name="open")
	public String open;
	@JSONField(name="lon")
	public String lon;
	@JSONField(name="lat")
	public String lat;
	@JSONField(name="speed")
	public String speed;
	@JSONField(name="talk")
	public String talk;
	@JSONField(name="voice")
	public String voice;
	@JSONField(name="vtype")
	public String vtype;
	@JSONField(name="restime")
	public String restime;
	@JSONField(name="flux")
	public String flux;
}
