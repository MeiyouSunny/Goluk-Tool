package com.mobnote.golukmain.followed.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowedCommentBean {
	//是否显示评论 0:否; 1:是
	@JSONField(name="iscomment")
	public String iscomment;
	@JSONField(name="comcount")
	public String comcount;
	@JSONField(name="comlist")
	public List<FollowedComListBean> comlist;
}
