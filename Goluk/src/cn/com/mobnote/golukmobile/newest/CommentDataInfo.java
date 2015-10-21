package cn.com.mobnote.golukmobile.newest;

import org.json.JSONObject;

public class CommentDataInfo {
	public String commentid;
	public String authorid;
	public String name;
	public String avatar;
	public String time;
	public String text;
	public String replyid;
	public String replyname;
	
	public CommentDataInfo() {
		
	}
	
	public CommentDataInfo(JSONObject json) {
		this.commentid = json.optString("commentid");
		this.authorid = json.optString("authorid");
		this.name = json.optString("name");
		this.avatar = json.optString("avatar");
		this.time = json.optString("time");
		this.text = json.optString("text");
		this.replyid = json.optString("replyid");
		this.replyname = json.optString("replyname");
	}

}
