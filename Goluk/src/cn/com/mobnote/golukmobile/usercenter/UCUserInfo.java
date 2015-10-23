package cn.com.mobnote.golukmobile.usercenter;

import java.io.Serializable;

public class UCUserInfo implements Serializable {

	/** */
	private static final long serialVersionUID = 1L;
	/**
	 * 自定义头像URL
	 */
	public String customavatar;
	/**
	 * 用户头像
	 */
	public String headportrait;
	/**
	 * 性别
	 */
	public String sex;
	/**
	 * 介绍
	 */
	public String introduce;
	/**
	 * uid
	 */
	public String uid;
	/**
	 * 用户昵称
	 */
	public String nickname;
	/**
	 * 分享视频总数
	 */
	public String sharevideonumber;

	/**
	 * 赞你的人总数
	 */
	public String praisemenumber;

}
