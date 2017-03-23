package com.mobnote.wifibind;

public interface WifiConnCallBack {

	/**
	 * 回调接口。获取文件列表 和wifi管理
	 * 
	 * @param type
	 *            指令
	 * @param state
	 *            状态 0正常 -1 错误
	 * @param process
	 *            进程号
	 * @param message
	 *            文字描述
	 * @param arrays
	 *            参数
	 */
	public void wifiCallBack(int type, int state, int process, String message, Object arrays);
}
