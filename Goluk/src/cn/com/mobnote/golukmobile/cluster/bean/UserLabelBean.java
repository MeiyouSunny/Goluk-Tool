package cn.com.mobnote.golukmobile.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class UserLabelBean {
	
	/**蓝V描述**/
	@JSONField(name="approve")
	public String approve;
	
	/**蓝V标识**/
	@JSONField(name="approvelabel")
	public String approvelabel;
	
	/**达人认证**/
	@JSONField(name="tarento")
	public String tarento;
	
	/**黄v标识**/
	@JSONField(name="headplusv")
	public String headplusv;
	
	/**黄V描述**/
	@JSONField(name="headplusvdes")
	public String headplusvdes;
	
	/**达人描述**/
	@JSONField(name="tarentodes")
	public String tarentodes;
	
	
}
