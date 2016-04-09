package com.mobnote.golukmain.profit;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class ProfitDetailData {

	/**数组列表**/
	@JSONField(name="incomelist")
	public List<ProfitDetailResult> incomelist;
	
}
