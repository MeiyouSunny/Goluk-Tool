package com.mobnote.golukmain.search.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.mobnote.golukmain.following.bean.FollowingItemBean;

public class SearchDataBean {

	@JSONField(name="userlist")
	public List<FollowingItemBean> userlist;
	@JSONField(name="usercount")
	public int usercount;
	@JSONField(name="recomlist")
	public List<FollowingItemBean> recomlist;
}
