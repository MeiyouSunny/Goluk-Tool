package com.mobnote.golukmain.promotion;

import java.io.Serializable;
import java.util.ArrayList;

import com.alibaba.fastjson.annotation.JSONField;

public class PromotionData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JSONField(name = "channelid")
	public String channelid;
	@JSONField(name = "channelname")
	public String channelname;
	@JSONField(name = "activities")
	public ArrayList<PromotionItem> activities;

	/**
	 * 获取Item内容
	 * 
	 * @param pPosition
	 * @return
	 */
	public PromotionItem getItem(int pPosition) {
		// Category排在第一位
		if (pPosition == 0) {
			return null;
		} else {
			return activities.get(pPosition - 1);
		}
	}

	/**
	 * 当前类别Item总数。Category也需要占用一个Item
	 * 
	 * @return
	 */
	public int getItemCount() {
		if (activities == null) {
			return 1;
		}
		return activities.size() + 1;
	}
}
