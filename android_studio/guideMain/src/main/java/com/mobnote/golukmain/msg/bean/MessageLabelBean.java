package com.mobnote.golukmain.msg.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class MessageLabelBean {
	
	/**蓝V认证标识  **/
	@JSONField(name="approvelabel")
	public String approvelabel ;
	
	/**蓝V认证 **/
	@JSONField(name="approve")
	public String approve;
	
	/**达人**/
	@JSONField(name="tarento")
	public String tarento;
	
	/**黄V认证标识 **/
	@JSONField(name="headplusv")
	public String headplusv;
	
	/**黄V认证**/
	@JSONField(name="headplusvdes")
	public String headplusvdes ;

}
