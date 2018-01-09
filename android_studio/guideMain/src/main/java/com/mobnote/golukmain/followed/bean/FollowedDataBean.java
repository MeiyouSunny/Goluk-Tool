package com.mobnote.golukmain.followed.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;

public class FollowedDataBean {
	// 0:成功；1:参数错误；2:未知异常
	@JSONField(name="result")
	public String result;
	@JSONField(name="operation")
	public String operation;
	// 混合列表
	@JSONField(name="list")
	public ArrayList<FollowedListBean> list;
	@JSONField(name="count")
	public int count;
}
