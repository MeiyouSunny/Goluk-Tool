package com.mobnote.golukmain.profit;

import com.alibaba.fastjson.annotation.JSONField;

public class ProfitData {

	/**标识**/
	@JSONField(name="result")
	public String result;

	/**上一次收入金币**/
	@JSONField(name="lgold")
	public String lgold;
	
	/**累计获得的金币**/
	@JSONField(name="hgold")
	public String hgold;
	
	/**活动剩余金币**/
	@JSONField(name="agold")
	public String agold;
}
