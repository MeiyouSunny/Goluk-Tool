package com.mobnote.golukmain.wifibind;

/**
 * @author 陈宣宇
 * @ 功能描述:WiFi绑定信息全局变量
 */
public class WiFiInfo {
    /**
     * 连接的Wifi的用户名与密码(手机连接IPC Wifi的信息, 在Wifi列表中的名称)
     */
    public static String IPC_SSID;
    public static String IPC_PWD;
    public static String IPC_MAC;
    public static String IPC_MODEL;

    /**
     * 手机热点的信息 (创建手机Wifi热点使用)
     */
    public static String MOBILE_SSID;
    public static String MOBILE_PWD;

    //清空全局变量
    public static void clear() {
        IPC_SSID = "";
        IPC_PWD = "";
        IPC_MAC = "";
        IPC_MODEL = "";
        MOBILE_SSID = "";
        MOBILE_PWD = "";
    }
}
