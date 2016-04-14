package com.mobnote.golukmain.newest;

import org.json.JSONObject;

public class LiveInfo {
	public String pic;
	public String number;
	
	public LiveInfo() {
	}
	
	public LiveInfo(JSONObject json) {
		this.pic = json.optString("pic");
		this.number = json.optString("number");
	}

}
