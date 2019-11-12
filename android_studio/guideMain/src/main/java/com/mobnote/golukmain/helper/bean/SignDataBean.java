package com.mobnote.golukmain.helper.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class SignDataBean {
	@JSONField(name="result")
	public String result;
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
	@JSONField(name="videoBucket")
	public String videoBucket;
	@JSONField(name="photoBucket")
	public String photoBucket;
	@JSONField(name="region")
	public String region;
//	@JSONField(name="wonderfulpath")
//	public String wonderfulpath;
}
