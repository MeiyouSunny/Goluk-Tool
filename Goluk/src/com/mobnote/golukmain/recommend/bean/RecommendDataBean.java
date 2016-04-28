package com.mobnote.golukmain.recommend.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.mobnote.golukmain.userbase.bean.SimpleUserItemBean;

public class RecommendDataBean {

	@JSONField(name = "userlist")
	public List<SimpleUserItemBean> userlist;
	@JSONField(name = "usercount")
	public int usercount;
}
