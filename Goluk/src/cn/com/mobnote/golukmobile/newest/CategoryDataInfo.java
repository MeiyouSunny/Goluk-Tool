package cn.com.mobnote.golukmobile.newest;

import org.json.JSONObject;

public class CategoryDataInfo {
	public String id;
	public String name;
	public String coverurl;
	public String time;

	public CategoryDataInfo() {
		
	}
	
	public CategoryDataInfo(JSONObject json) {
		this.id = json.optString("id");
		this.name = json.optString("name");
		this.coverurl = json.optString("coverurl");
		this.time = json.optString("time");
	}
	
}
