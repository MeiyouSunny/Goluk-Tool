package com.mobnote.golukmain.promotion;

import java.io.Serializable;
import java.util.ArrayList;

import com.alibaba.fastjson.annotation.JSONField;

public class Promotions implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JSONField(name="channels")
	public ArrayList<PromotionData> PromotionList;
	@JSONField(name="priorityacts")
	public ArrayList<PromotionItem> priorityacts;
}
