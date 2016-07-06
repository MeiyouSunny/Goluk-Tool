package com.mobnote.golukmain.msg.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class MessageCertificateBean {

	/**认证结果  0：失败；1：成功**/
	@JSONField(name="result")
	public String result;

	/**0：系统（达人）；1：人工（黄V、蓝V）**/
	@JSONField(name="type")
	public String type;
	
}
