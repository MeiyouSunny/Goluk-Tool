package com.mobnote.golukmain.usercenter.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class HomeUser {

	/** 用户唯一id **/
	@JSONField(name = "uid")
	public String uid;

	/** 用户昵称 **/
	@JSONField(name = "nickname")
	public String nickname;

	/** 用户头像 **/
	@JSONField(name = "avatar")
	public String avatar;

	/** 自定义头像URL **/
	@JSONField(name = "customavatar")
	public String customavatar;

	/** 性别 **/
	@JSONField(name = "sex")
	public String sex;

	/** 个性签名 **/
	@JSONField(name = "introduction")
	public String introduction;

	/** 连接类型 **/
	@JSONField(name = "link")
	public int link;

	/** 认证信息 **/
	@JSONField(name = "certification")
	public HomeCertification certification;

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

	/** 自定义头像URL（原图） **/
	@JSONField(name = "rawavatar")
	public String rawavatar;

}
