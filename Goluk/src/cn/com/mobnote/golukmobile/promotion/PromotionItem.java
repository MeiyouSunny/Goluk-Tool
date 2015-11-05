package cn.com.mobnote.golukmobile.promotion;

import com.alibaba.fastjson.annotation.JSONField;

public class PromotionItem {
	@JSONField(name="activityid")
	public String id;
	@JSONField(name="activityname")
	public String name;
}
