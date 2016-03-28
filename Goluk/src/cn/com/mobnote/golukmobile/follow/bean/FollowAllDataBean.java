package cn.com.mobnote.golukmobile.follow.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowAllDataBean {
	// 用户id
	@JSONField(name="uid")
	public String uid;
	@JSONField(name="linklist")
	public List<FollowAllLinkListBean> linklist;
}
