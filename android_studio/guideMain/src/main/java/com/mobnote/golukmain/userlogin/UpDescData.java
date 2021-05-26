package com.mobnote.golukmain.userlogin;

import com.alibaba.fastjson.annotation.JSONField;

public class UpDescData {
	/**	结果代码  **/
	@JSONField(name="result")
	public String result;
	
	/**个性签名 **/
	@JSONField(name="desc")
	public String desc;
	
}
