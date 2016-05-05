package com.mobnote.golukmain.following.bean;

import java.util.ArrayList;

import com.alibaba.fastjson.annotation.JSONField;
import com.mobnote.golukmain.userbase.bean.SimpleUserItemBean;

public class FollowingDataBean {
	
	// 用户列表
	@JSONField(name="userlist")
	public ArrayList<SimpleUserItemBean> userlist;

}
