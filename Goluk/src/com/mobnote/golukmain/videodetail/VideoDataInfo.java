package com.mobnote.golukmain.videodetail;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoDataInfo {
	@JSONField(name="aid")
	public String aid;
	@JSONField(name="mid")
	public String mid;
	@JSONField(name="activie")
	public String activie;
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
