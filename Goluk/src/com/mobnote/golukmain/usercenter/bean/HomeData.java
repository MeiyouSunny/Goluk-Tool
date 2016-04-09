package com.mobnote.golukmain.usercenter.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class HomeData {

	/** 操作 **/
	@JSONField(name = "operation")
	public String operation;

	/** 视频计数 **/
	@JSONField(name = "videocount")
	public int videocount;

	/** 分享视频计数 **/
	@JSONField(name = "sharecount")
	public int sharecount;

	/** 入选精选视频计数 **/
	@JSONField(name = "selectcount")
	public int selectcount;

	/** 入选推荐视频计数 **/
	@JSONField(name = "recommendcount")
	public int recommendcount;

	/** 入选头条视频计数 **/
	@JSONField(name = "headlinecount")
	public int headlinecount;

	/** 用户信息 **/
	@JSONField(name = "user")
	public HomeUser user;

	/** 视频列表 **/
	@JSONField(name = "videolist")
	public List<HomeVideoList> videolist;

}
