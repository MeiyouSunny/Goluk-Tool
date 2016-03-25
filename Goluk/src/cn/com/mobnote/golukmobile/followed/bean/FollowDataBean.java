package cn.com.mobnote.golukmobile.followed.bean;

import java.util.ArrayList;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowDataBean {
	// 0:成功；1:参数错误；2:未知异常
	@JSONField(name="result")
	public String result;
	@JSONField(name="operation")
	public String operation;
	// 混合列表
	@JSONField(name="list")
	public ArrayList<FollowListBean> list;
	@JSONField(name="count")
	public String count;
}
