package com.mobnote.golukmain.userlogin;

import com.alibaba.fastjson.annotation.JSONField;

public class UserResult {
	
	/**请求返回码**/
	@JSONField(name="code")
	public String code;
	
	/**请求是否成功**/
	@JSONField(name="state")
	public String state;
	
	/**请求返回数据 **/
	@JSONField(name="data")
	public UserData data;
	
	/**请求返回数据**/
	@JSONField(name="info")
	public UserInfo info;
	
	/**返回调试信息**/
	@JSONField(name="msg")
	public String msg;

}
