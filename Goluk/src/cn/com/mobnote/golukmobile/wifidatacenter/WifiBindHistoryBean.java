package cn.com.mobnote.golukmobile.wifidatacenter;

import com.alibaba.fastjson.annotation.JSONField;

public class WifiBindHistoryBean {

	public static final int CONN_NOT_USE = 0;
	public static final int CONN_USE = 1;

	/** 使用状态，1 表示正在使用中 , 0: 表示未使用 */
	@JSONField(name = "state")
	public int state;
	/** 设备标识,标识G1, G2, T1 */
	@JSONField(name = "ipcSign")
	public String ipcSign;
	/** 最后一次连接的时间 */
	@JSONField(name = "lasttime")
	public String lasttime;

	/**
	 * IPC信息相关
	 * */
	@JSONField(name = "ipc_ssid")
	public String ipc_ssid;
	@JSONField(name = "ipc_pwd")
	public String ipc_pwd;
	@JSONField(name = "ipc_mac")
	public String ipc_mac;
	@JSONField(name = "ipc_ip")
	public String ipc_ip;
	/** SN号 */
	@JSONField(name = "serial")
	public String serial;
	/** 固件版本 */
	@JSONField(name = "version")
	public String version;

	/**
	 * 手机热点名称相关
	 * */
	@JSONField(name = "mobile_ssid")
	public String mobile_ssid;
	@JSONField(name = "mobile_pwd")
	public String mobile_pwd;

	@Override
	public String toString() {
		return "ipc_ssid: " + ipc_ssid + "  ipc_pwd:" + ipc_pwd + "  mobile_ssid:" + mobile_ssid + " mobile_pwd:"
				+ mobile_pwd + " state:" + state;
	}

}
