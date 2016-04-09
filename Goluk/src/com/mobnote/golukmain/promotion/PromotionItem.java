package com.mobnote.golukmain.promotion;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

public class PromotionItem implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JSONField(name="activityid")
	public String id;
	@JSONField(name="activityname")
	public String name;
}
