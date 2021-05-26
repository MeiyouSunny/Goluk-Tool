package com.mobnote.golukmain.userlogin;

import com.alibaba.fastjson.annotation.JSONField;

public class UpHeadData {
	/**	结果代码  0:成功；1:参数错误；2:未知异常 **/
	@JSONField(name="result")
	public String result;
}
