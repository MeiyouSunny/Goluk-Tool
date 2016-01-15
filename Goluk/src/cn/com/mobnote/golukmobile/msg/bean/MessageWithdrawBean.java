package cn.com.mobnote.golukmobile.msg.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class MessageWithdrawBean {
	
	/**提现结果  0：失败；1：成功**/
	@JSONField(name="result")
	public String result;
	
	/**体现类型 0：审核；1：打款  **/
	@JSONField(name="type")
	public String type;
	
}
