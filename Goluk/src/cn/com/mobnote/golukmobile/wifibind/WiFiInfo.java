package cn.com.mobnote.golukmobile.wifibind;

/**
 * @ 功能描述:WiFi绑定信息全局变量
 * 
 * @author 陈宣宇
 * 
 */
public class WiFiInfo {
	/** 连接的Wifi的用户名与密码(手机连接IPC Wifi的信息, 在Wifi列表中的名称) */
	public static String IPC_SSID;
	public static String IPC_PWD;
	public static String IPC_MAC;

	/** 手机热点的信息 (创建手机Wifi热点使用) */
	public static String MOBILE_SSID;
	public static String MOBILE_PWD;
}
