package com.mobnote.golukmain.videosuqare;

import com.mobnote.golukmain.userinfohome.bean.UserLabelBean;

/**
 * 
 * 视频广场用户属性信息
 * 
 * 2015年4月14日
 * 
 * @author xuhw
 */
public class UserEntity {
	/** 用户唯一id */
	public String uid;
	/** 用户昵称 */
	public String nickname;
	/** 用户头像 */
	public String headportrait;
	/** 性别 */
	public String sex;
	/** 关注关系*/
	public int link;
	/** 用户自定义头像,(头像服务器地址) */
	public String mCustomAvatar;
	/**用户认证信息 **/
	public UserLabelBean label;
}
