package com.mobnote.golukmain.praised.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class MyPraisedVideoBean {
	/**视频标识*/
	@JSONField(name="videoid")
	public String videoid;

	/**视频图片*/
	@JSONField(name="picture")
	public String picture;

	/**视频描述*/
	@JSONField(name="describe")
	public String describe;

	/**视频作者*/
	@JSONField(name="uid")
	public String uid;

	/**用户昵称*/
	@JSONField(name="nickname")
	public String nickname;

	/**点赞日期*/
	@JSONField(name="time")
	public String time;

	@JSONField(name="ts")
	public long ts;
}
