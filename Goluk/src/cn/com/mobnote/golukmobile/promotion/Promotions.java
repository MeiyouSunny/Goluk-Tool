package cn.com.mobnote.golukmobile.promotion;

import java.util.ArrayList;

import com.alibaba.fastjson.annotation.JSONField;

public class Promotions {
	@JSONField(name="channels")
	public ArrayList<PromotionData> PromotionList;
}
