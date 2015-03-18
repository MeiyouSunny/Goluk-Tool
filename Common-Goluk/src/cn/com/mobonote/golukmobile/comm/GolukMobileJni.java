package cn.com.mobonote.golukmobile.comm;

public class GolukMobileJni {

	public static native int GoLuk_Create();

	public static native void GoLuk_Destroy(int pGoluk);

	// 网络传输类对外接口
	// wifi热点状态变更：true热点连接成功/false热点断开，默认断开
	public static native void GoLuk_WifiStateChanged(int pGoluk, boolean isSucess);

	// 注册网络传输回调接口
	public static native void GoLuk_RegistNetTransNotify(int pGoluk, INetTransNotifyFn notify);

	// 页面类访问接口
	// 注册页面访问类回调接口
	public static native void GoLuk_RegistPageNotify(int pGoluk, IPageNotifyFn notify);

	// 通用访问页面函数
	public static native boolean GoLuk_CommonGetPage(int pGoluk, int type, String param);

}
