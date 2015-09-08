package cn.com.mobnote.golukmobile.wifibind;

/**
 * <pre>
 * 1.类命名首字母大写
 * 2.公共函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处
 * 8.所有代码必须使用TAB键缩进
 * 9.函数使用块注释,代码逻辑使用行注释
 * 10.文件头部必须写功能说明
 * 11.后续人员开发保证代码格式一致
 * </pre>
 * 
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
