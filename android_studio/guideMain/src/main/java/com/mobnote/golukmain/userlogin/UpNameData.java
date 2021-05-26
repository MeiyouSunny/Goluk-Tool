package com.mobnote.golukmain.userlogin;

import com.alibaba.fastjson.annotation.JSONField;

public class UpNameData {
	/**	结果代码  **/
	@JSONField(name="result")
	public String result;
	
	/**用户昵称 **/
	@JSONField(name="nickname")
	public String nickname;
	
}
