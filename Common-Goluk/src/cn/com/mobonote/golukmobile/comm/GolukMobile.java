package cn.com.mobonote.golukmobile.comm;

public class GolukMobile {
	/** 网络传输事件声明 */
	//服务器连接状态事件
	public static int ENetTransEvent_ConnectionState = 0;
	//数据传输状态事件
	public static int ENetTransEvent_TransmissionState = 1;
	
	/** 服务器连接状态事件下消息ID */
	//空闲
	public static int ConnectionStateMsg_Idle = 0;
	//连接中
	public static int ConnectionStateMsg_Connecting = 1;
	//连接成功
	public static int ConnectionStateMsg_Connected = 2;
	//连接断开
	public static int ConnectionStateMsg_DisConnected = 3;
	
	/** 文件数据传输事件下消息ID */
	//空闲
	public static int TransmissionStateMsg_Idle = 0;
	//校验文件列表消息
	public static int TransmissionStateMsg_CheckList = 1;
	//文件传输消息
	public static int TransmissionStateMsg_File = 2;
	
	/** 页面访问类事件声明,主要用于处理各种页面数据的访问接口及其回调通知等*/
	//主页param1:0(主页数据)/1(图片数据), param2:(主页json数据/图片json数据)
	public static int PageType_Main = 0;
	//上传视频param1:(上传接口json串信息)，param2:0
	public static int PageType_UploadVideo = 1;
	//分享param1:（分析成功结果json串信息），param2:0
	public static int PageType_Share = 2;
	//请求大头针数据
	public static int PageType_GetPinData = 7;
	//下载图片
	public static int PageType_GetPictureByURL = 8;
	//获取视频详情
	public static int PageType_GetVideoDetail = 9;
	//登录
	public static int PageType_Login = 11;
	private int pGoluk;
	
	/**
	 * 个人中心部分
	 * 
	 */
	//获取验证码
	public static int PageType_GetVCode = 15;
	//注册
	public static int PageType_Register = 16;
	//修改用户密码
	public static int PageType_ModifyPwd = 17;
	//登陆——11
	
	//自动登陆
	public static int PageType_AutoLogin = 12;
	
	/**
	 * 创建指定宽高 缓存大小 的地图
	 * 
	 * @param screenWidth
	 * @param screenHeight
	 * @return
	 */
	public void GolukMobile_Create() {
		pGoluk = GolukMobileJni.GoLuk_Create();
	}

	/**
	 * 释放
	 * 
	 * @param pMapNaviCtrl
	 */
	public void GolukMobile_Destroy() {
		GolukMobileJni.GoLuk_Destroy(pGoluk);
	}

	// 网络传输类对外接口
	// wifi热点状态变更：true热点连接成功/false热点断开，默认断开
	public void GoLuk_WifiStateChanged(boolean isSucess) {
		GolukMobileJni.GoLuk_WifiStateChanged(pGoluk, isSucess);
	}

	// 注册网络传输回调接口
	public void GoLuk_RegistNetTransNotify(INetTransNotifyFn notify) {
		NetTransNotifyAdapter.setNetTransNotify(notify);
		GolukMobileJni.GoLuk_RegistNetTransNotify(pGoluk, notify);
	}

	// 页面类访问接口
	// 注册页面访问类回调接口
	public void GoLuk_RegistPageNotify(IPageNotifyFn notify) {
		PageNotifyAdapter.setNotify(notify);
		GolukMobileJni.GoLuk_RegistPageNotify(pGoluk, notify);

	}

	// 通用访问页面函数
	public boolean GoLuk_CommonGetPage(int type, String param) {
		return GolukMobileJni.GoLuk_CommonGetPage(pGoluk, type, param);
	}

}
