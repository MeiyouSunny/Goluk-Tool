package com.mobnote.golukmain.userlogin;

import com.alibaba.fastjson.annotation.JSONField;

public class CancelResult {
	/**请求是否成功**/
	@JSONField(name="success")
	public boolean success;
	
	/**返回数据**/
	@JSONField(name="data")
	public CancelData data;
	
	/**返回调试信息  **/
	@JSONField(name="msg")
	public String msg;	
}
