package cn.com.mobnote.module.page;

import cn.com.mobnote.logic.IGolukCommFn;

public interface IPageNotifyFn extends IGolukCommFn {

	public static final int PAGE_RESULT_SUCESS = 1;

	/**
	 * 页面访问类事件声明,主要用于处理各种页面数据的访问接口及其回调通知等
	 * */

	/** 主页param1:0(主页数据)/1(图片数据), param2:(主页json数据/图片json数据) */
	public static int PageType_Main = 0;
	/** 上传视频param1:(上传接口json串信息)，param2:0 */
	public static int PageType_UploadVideo = 1;
	/** 分享param1:（分析成功结果json串信息），param2:0 */
	public static int PageType_Share = 2;
	/** 视频广场更多页面数据 */
	public static int PageType_MoreList = 3;
	/** 点赞/取消点赞 */
	public static int PageType_Like = 4;
	/** 获取评论 */
	public static int PageType_GetComments = 5;
	/** 评论 */
	public static int PageType_Comment = 6;
	/** 请求大头针数据 */
	public static int PageType_GetPinData = 7;
	/** 下载图片 */
	public static int PageType_GetPictureByURL = 8;
	/** 获取视频详情 */
	public static int PageType_GetVideoDetail = 9;
	/** 检测升级 */
	public static int PageType_CheckUpgrade = 10;
	/** 登录 */
	public static int PageType_Login = 11;
	/** 自动登录 */
	public static int PageType_AutoLogin = 12;
	/** 获取用户信息 */
	public static int PageType_GetUserInfo = 13;
	/** 注销 */
	public static int PageType_SignOut = 14;
	public static int PageType_GetVCode = 15;
	public static int PageType_Register = 16;
	/** 修改密码 */
	public static int PageType_ModifyPwd = 17;
	/** 主动开启直播 */
	public static int PageType_LiveStart = 18;
	/** 直播结束 */
	public static int PageType_LiveStop = 19;
	/** 看别人直播 */
	public static int PageType_PlayStart = 20;
	/** 停止观看别人直播 */
	public static int PageType_PlayStop = 21;
	/** 直播分享 */
	public static int PageType_LiveShare = 22;
	/** 直播点赞 */
	public static int PageType_LiveLike = 23;
	public static int PageType_ModifyUserInfo = 24;
	/** 上传视频第一帧图片 */
	public static final int PageType_LiveUploadPic = 26;

	/**
	 * 
	 * 以下为同步获取信息标识
	 * */

	/** 同步获取登录用户信息命令 */
	public static final int PageType_GetUserInfo_Get = 0;

	public void pageNotifyCallBack(int type, int success, Object param1, Object param2);

}
