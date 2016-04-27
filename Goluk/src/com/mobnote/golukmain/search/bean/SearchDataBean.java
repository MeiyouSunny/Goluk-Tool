package com.mobnote.golukmain.search.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.mobnote.golukmain.userbase.bean.SimpleUserItemBean;

public class SearchDataBean {

	@JSONField(name="userlist")
	public List<SimpleUserItemBean> userlist;
	@JSONField(name="usercount")
	public int usercount;
	@JSONField(name="recomlist")
	public List<SimpleUserItemBean> recomlist;
}
