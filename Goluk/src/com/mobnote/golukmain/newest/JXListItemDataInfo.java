package com.mobnote.golukmain.newest;

import org.json.JSONObject;

public class JXListItemDataInfo {
	public String jtype;
	public String ztype;
	public String ztid;
	public String ztitle;
	public String jximg;
	public String jtypeimg;
	public String ztag;
	public String videonumber;
	public String clicknumber;
	public String praisenumber;
	public String commentnumber;
	public String adverturl;
	
	public String jxdate;
	public String jxid;
	
	public JXListItemDataInfo(){
		
	}
	
	public JXListItemDataInfo(JSONObject json, String jxdate, String jxid) {
		this.jxdate = jxdate;
		this.jxid = jxid;
		
		this.jtype = json.optString("jtype");
		this.ztype = json.optString("ztype");
		this.ztid = json.optString("ztid");
		this.ztitle = json.optString("ztitle");
		this.jximg = json.optString("jximg");
		this.jtypeimg = json.optString("jtypeimg");
		this.ztag = json.optString("ztag");
		this.videonumber = json.optString("videonumber");
		this.clicknumber = json.optString("clicknumber");
		this.praisenumber = json.optString("praisenumber");
		this.commentnumber = json.optString("commentnumber");
		this.adverturl = json.optString("adverturl");
	}
	
}
