package cn.com.mobnote.golukmobile.live;

import java.io.Serializable;

public class UserInfo implements Serializable {

	private static final long serialVersionUID = -8534844170998963067L;

	/** 用户登录 uid */
	public String uid;
	/** 用户爱滔客aid */
	public String aid;
	/** 用户昵称 */
	public String nickName;
	/** 图片地址 */
	public String picurl;
	/** 性别 */
	public String sex;

	public String lon;
	public String lat;
	public String speed;

	/** 主动/被动直播 1/2 */
	public String active;
	/** 平台类型 pad/android/ios */
	public String tag;
	/** 爱滔客群组ID,如无群组则为空 */
	public String groupId;
	/** 观看人数 */
	public String persons;
	/** 赞的个数 */
	public String zanCount;
	/** 直播开启时间 */
	public int liveDuration;
	/** 视频描述 */
	public String desc;
}
