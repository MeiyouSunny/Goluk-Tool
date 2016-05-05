package com.mobnote.golukmain.userbase.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 关注的用户
 * @author leege100
 *
 */
public class SimpleUserItemBean extends BaseUserBean{

	/** 连接类型 **/
	@JSONField(name = "link")
	public int link;

	/** 分享视频数 **/
	@JSONField(name = "share")
	public int share;

	/** 关注数 **/
	@JSONField(name = "following")
	public int following;

	/** 粉丝数 **/
	@JSONField(name = "fans")
	public int fans;

	/** 索引 **/
	@JSONField(name = "index")
	public String index;
}
