package cn.com.mobnote.golukmobile.videoclick;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoClickGoluk {

	@JSONField(name="name")
	public String name;
	
	@JSONField(name="version")
	public String version;
	
	@JSONField(name="path")
	public String path;
	
	@JSONField(name="url")
	public String url;
	
	@JSONField(name="md5")
	public String md5;
	
	@JSONField(name="isupdate")
	public String isupdate;
	
	@JSONField(name="filesize")
	public String filesize;
	
	@JSONField(name="releasetime")
	public String releasetime;
	
	@JSONField(name="appcontent")
	public String appcontent;
	
}
