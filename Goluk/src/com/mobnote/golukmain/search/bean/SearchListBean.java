package com.mobnote.golukmain.search.bean;

import com.mobnote.golukmain.userbase.bean.SimpleUserItemBean;

public class SearchListBean {

	public static final int ITEM_NO_RESULT = 1;
	public static final int ITEM_RECOMMEND = 2;
	public static final int ITEM_USER = 3;
	public static final int ITEM_NO_MORE = 4;

	private int type;
	private SimpleUserItemBean userItemBean;

	public SearchListBean(int t,SimpleUserItemBean bean){
		this.type = t;
		this.userItemBean = bean;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public SimpleUserItemBean getUserItemBean() {
		return userItemBean;
	}

	public void setUserItemBean(SimpleUserItemBean userItemBean) {
		this.userItemBean = userItemBean;
	}

}
