package cn.com.mobnote.module.page;

import cn.com.mobnote.logic.IGolukCommFn;

public interface IPageNotifyFn extends IGolukCommFn {

	public static final int PAGE_RESULT_SUCESS = 1;

	/**
	 * 页面访问类事件声明,主要用于处理各种页面数据的访问接口及其回调通知等
	 * */

	/** 上传视频param1:(上传接口json串信息)，param2:0 */
	public static int PageType_UploadVideo = 1;
	/** 分享param1:（分析成功结果json串信息），param2:0 */
	public static int PageType_Share = 2;
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

	/** 上传视频第一帧图片 */
	public static final int PageType_LiveUploadPic = 26;
	/** 下载ipc文件 **/
	public static final int PageType_CommDownloadFile = 27;
	/** 意见反馈 **/
	public static final int PageType_FeedBack = 28;
	/** 推送注册 */
	public static final int PageType_PushReg = 29;
	/** 获取推送配置 (是否允许点赞) */
	public static final int PageType_GetPushCfg = 30;
	
	public static final int PageType_SetPushCfg = 31;
	/**下载IPC文件**/
	public static final int PageType_DownloadIPCFile = 32;
	
	/** 上传用户头像 **/
	public static final int PageType_ModifyHeadPic = 35;

	public static final int PageType_ModifyNickName = 33;
	
	public static final int PageType_ModifySignature = 34;
	
	public static final int PageType_GetPromotion = 36;

	public static final int PageType_ClusterMain =  37;
	
	/**我的收益**/
	public static final int PageType_MyProfit = 38;
	/**收益明细**/
	public static final int PageType_ProfitDetail = 39;

	/**活动聚合 推荐 **/
	public static final int PageType_ClusterRecommend = 40;
	/**活动聚合 最新 **/
	public static final int PageType_ClusterNews = 41;
	/**聚合分享地址**/
	public static final int PageType_ClusterShareUrl = 42;
	/* Banner */
	public static final int PageType_BannerGet = 43;
	/**最新视频点击次数上报**/
	public static final int PageType_VideoClick = 44;

	/**
	 * 
	 * 以下为同步获取信息标识
	 * */

	/** 同步获取登录用户信息命令 */
	public static final int PageType_GetUserInfo_Get = 0;
	public static final int PageType_GetVersion = 1;
	/**查询IPC升级文件的存放位置**/
	public static final int PageType_GetIPCFile = 2;
	
	

	public void pageNotifyCallBack(int type, int success, Object param1, Object param2);

}
