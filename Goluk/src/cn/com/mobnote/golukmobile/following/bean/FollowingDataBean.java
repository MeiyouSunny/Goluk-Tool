package cn.com.mobnote.golukmobile.following.bean;

import java.util.ArrayList;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowingDataBean {
	
	// 用户列表
	@JSONField(name="userlist")
	public ArrayList<FollowingItemBean> userlist;

}
