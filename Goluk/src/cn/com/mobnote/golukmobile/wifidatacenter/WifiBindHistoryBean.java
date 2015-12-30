package cn.com.mobnote.golukmobile.wifidatacenter;

public class WifiBindHistoryBean {

	public static final int CONN_NOT_USE = 0;
	public static final int CONN_USE = 1;

	/** 使用状态，1 表示正在使用中 , 0: 表示未使用 */
	public int state;
	/** 设备标识,标识G1, G2, T1 */
	public String ipcSign;

	/**
	 * IPC信息相关
	 * */
	public String ipc_ssid;
	public String ipc_pwd;
	public String ipc_mac;
	public String ipc_ip;

	/**
	 * 手机热点名称相关
	 * */
	public String mobile_ssid;
	public String mobile_pwd;

	@Override
	public String toString() {
		return "ipc_ssid: " + ipc_ssid + "  ipc_pwd:" + ipc_pwd + "  mobile_ssid:" + mobile_ssid + " mobile_pwd:"
				+ mobile_pwd + " state:" + state;
	}

}
