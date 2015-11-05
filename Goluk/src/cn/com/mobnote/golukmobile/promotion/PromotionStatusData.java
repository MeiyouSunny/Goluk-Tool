package cn.com.mobnote.golukmobile.promotion;

import com.alibaba.fastjson.annotation.JSONField;

public class PromotionStatusData {
	@JSONField(name="result")
	public String result;
	@JSONField(name="new")
	public boolean hasNew;
}
