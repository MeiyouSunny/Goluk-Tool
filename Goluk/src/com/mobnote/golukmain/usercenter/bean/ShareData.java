package com.mobnote.golukmain.usercenter.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class ShareData {

	/** 结果代码 0:成功；1:参数错误；2:未知异常 **/
	@JSONField(name = "result")
	public String result;

	/** 短网址 **/
	@JSONField(name = "shorturl")
	public String shorturl;

	/** 分享内容 ：这里一共有XX个精彩视频等你来围观哦！ **/
	@JSONField(name = "describe")
	public String describe;

	/** 标题 这是我的极路客个人主页，快来看看吧。我分享了【用户昵称】的个人主页，快来看看吧。 **/
	@JSONField(name = "title")
	public String title;

	/** 自定义头像URL **/
	@JSONField(name = "customavatar")
	public String customavatar;

	/** 用户头像 **/
	@JSONField(name = "headportrait")
	public String headportrait;

}
