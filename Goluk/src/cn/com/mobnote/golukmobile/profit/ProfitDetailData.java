package cn.com.mobnote.golukmobile.profit;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class ProfitDetailData {

	/**数组列表**/
	@JSONField(name="result")
	public List<ProfitDetailResult> result;
	
}
