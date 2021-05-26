package com.mobnote.golukmain.userlogin;

import com.alibaba.fastjson.annotation.JSONField;

public class UpDescResult {
	/**请求是否成功  **/
	@JSONField(name="success")
	public boolean success;
	
	/**结果代码 **/
	@JSONField(name="data")
	public UpNameData data;
	
	/**返回调试信息 **/
	@JSONField(name="msg")
	public String msg;
}
