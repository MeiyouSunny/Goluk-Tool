package cn.com.mobnote.golukmobile.promotion;

import com.alibaba.fastjson.annotation.JSONField;

public class PromotionModel {
	@JSONField(name="msg")
	public String msg;
	@JSONField(name="success")
	public boolean success;
	@JSONField(name="data")
	public Promotions data;
}
