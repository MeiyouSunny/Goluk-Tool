package com.mobnote.golukmain.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class RankingListData {
	
	/**结果代码**/
	@JSONField(name="result")
	public String result;
	
	/**操作**/
	@JSONField(name="operation")
	public String operation;
	
	/**视频总数**/
	@JSONField(name="videocount")
	public String videocount;
	
	/**视频列表**/
	@JSONField(name="videolist")
	public List<RankingListVideo> videolist;
	

}
