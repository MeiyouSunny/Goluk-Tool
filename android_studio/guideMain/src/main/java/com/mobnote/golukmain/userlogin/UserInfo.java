package com.mobnote.golukmain.userlogin;

import com.alibaba.fastjson.annotation.JSONField;

public class UserInfo {
	 
	@JSONField(name="mid")
	private String mid;
	
	@JSONField(name="aid")
	private String aid;
	
	@JSONField(name="key")
	private String key;	
	
	@JSONField(name="city")
	private int city;
	
	@JSONField(name="cfg_sp")
	private String cfg_sp;	
	
	@JSONField(name="cfg_sp_port")
	private String cfg_sp_port;	
	
	@JSONField(name="cfg_mdsr")
	private String cfg_mdsr;
	
	@JSONField(name="cfg_mdsr_port")
	private String cfg_mdsr_port;
	
	@JSONField(name="cfg_sp_lport")
	private String cfg_sp_lport;
	
	@JSONField(name="success")
	private boolean success;
	
}
