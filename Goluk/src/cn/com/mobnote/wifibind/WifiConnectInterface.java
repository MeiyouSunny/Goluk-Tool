package cn.com.mobnote.wifibind;

public interface WifiConnectInterface {

	/**
	 * 启动软件后自动管理wifi
	 */
	public void autoWifiManage();

	/**
	 * 通过用户名密码创建wifi热点
	 * 
	 * @param ph_ssid
	 * @param ph_password
	 * @param ipc_ssid
	 * @param ipc_mac
	 */
	public void createWifiAP(String ph_ssid, String ph_password, String ipc_ssid, String ipc_mac);

	/**
	 * 通过关键字查询列表信息
	 * 
	 * @param matching
	 *            关键字
	 */
	public void scanWifiList(String matching, boolean reset);

	/**
	 * 保存配置信息
	 * 
	 * @param beans
	 */
	public void saveConfiguration(WifiRsBean beans);

	/**
	 * 当前是否连接配置的ipc
	 */
	public void isConnectIPC();
}
