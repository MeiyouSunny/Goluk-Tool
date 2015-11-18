package cn.com.mobnote.golukmobile.startshare.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class SignDataBean {
	@JSONField(name="videoid")
	public String videoid;
	@JSONField(name="videosign")
	public String videosign;
	@JSONField(name="videopath")
	public String videopath;
	@JSONField(name="coversign")
	public String coversign;
	@JSONField(name="coverpath")
	public String coverpath;
	@JSONField(name="signtime")
	public String signtime;
	@JSONField(name="envsync")
	public String envsync;
}
