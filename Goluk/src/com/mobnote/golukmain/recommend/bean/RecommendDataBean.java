package com.mobnote.golukmain.recommend.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.mobnote.golukmain.following.bean.FollowingItemBean;

public class RecommendDataBean {

	@JSONField(name = "userlist")
	List<FollowingItemBean> userlist;
	@JSONField(name = "usercount")
	int usercount;
}
