package com.mobnote.golukmain.usercenter.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoList {

	/** 视频唯一id **/
	@JSONField(name = "videoid")
	public String videoid;

	/** 视频描述 **/
	@JSONField(name = "description")
	public String description;

	/** 视频封面 **/
	@JSONField(name = "pictureurl")
	public String pictureurl;

	/** 视频文件 **/
	@JSONField(name = "videourl")
	public String videourl;

	/** 视频添加时间 **/
	@JSONField(name = "addtime")
	public String addtime;


	@JSONField(name = "addts")
	public long addts;

	/** 评论次数 **/
	@JSONField(name = "commentcount")
	public int commentcount;

	/** 点击次数 **/
	@JSONField(name = "clickcount")
	public int clickcount;

	/** 点赞次数 **/
	@JSONField(name = "likecount")
	public String likecount;

	/** 视频清晰度 **/
	@JSONField(name = "resolution")
	public String resolution;

	/** 索引 **/
	@JSONField(name = "index")
	public String index;
	
	/** 索引 **/
	@JSONField(name = "isopen")
	public int isopen;

}
