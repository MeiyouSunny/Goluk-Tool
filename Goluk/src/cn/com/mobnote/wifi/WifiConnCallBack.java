package cn.com.mobnote.wifi;

public interface WifiConnCallBack {
	/**
	 * 回调接口。获取文件列表 和wifi管理
	 * @param state
	 * @param message
	 * @param arrays
	 */
	public void wifiCallBack(int state, String message,WifiRsBean[] arrays);
}
