package cn.com.mobnote.golukmobile.promotion;

import com.alibaba.fastjson.annotation.JSONField;

import cn.com.mobnote.golukmobile.http.BaseModel;

public class PromotionStatusModel extends BaseModel{
	@JSONField(name="data")
	public PromotionStatusData data;
}
