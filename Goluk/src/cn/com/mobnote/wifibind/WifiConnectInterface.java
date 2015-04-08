package cn.com.mobnote.wifibind;

import cn.com.mobnote.wifibind.WifiConnectManagerSupport.WifiCipherType;

public interface WifiConnectInterface {
	/**
	 * 通过用户名，密码连接ipc
	 * 
	 * @param ssid
	 * @param password
	 * @param type
	 */
	public void connectWifi(String ssid, String password, WifiCipherType type);

	/**
	 * 启动软件后自动管理wifi
	 */
	public void autoWifiManage() ;

	/**
	 * 通过用户名密码创建wifi热点
	 * 
	 * @param ssid
	 * @param password
	 */
	public void createWifiAP(String ssid, String password) ;

	/**
	 * 通过关键字查询列表信息
	 * 
	 * @param matching
	 *            关键字
	 */
	public void scanWifiList(String matching) ;
	
	/**保存配置信息
	 * @param beans
	 */
	public void saveConfiguration(  WifiRsBean beans);
	
	/**
	 * 当前是否连接配置的ipc
	 */
	public void isConnectIPC();
}
