package cn.com.mobnote.util;

import org.json.JSONObject;

public class JsonUtil {

	/**
	 * 组织IPC连接方式的json串
	 * 
	 * @param mode
	 *            连接方式，取值为IPCManagerFn类中的IPCMgrMode_IPCDirect/IPCMgrMode_Mobnote
	 * @return json串
	 * @author jiayf
	 * @date Mar 27, 2015
	 */
	public static String getIPCConnModeJson(int mode) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("mode", mode);

			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 组织当前连接wifi的状态json串
	 * 
	 * @param state
	 *            连接wifi状态 0/1　未连接/已连接
	 * @param ip
	 *            连接的IP地址
	 * @return JSON串
	 * @author jiayf
	 * @date Mar 27, 2015
	 */
	public static String getWifiChangeJson(int state, String ip) {
		try {
			if (null == ip) {
				ip = "";
			}
			JSONObject obj = new JSONObject();
			obj.put("state", state);
			obj.put("domain", ip);

			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 组织下载文件的json串
	 * 
	 * @param filename
	 *            文件名称
	 * @param tag
	 *            此文件的唯一标识
	 * @param savepath
	 *            文件的保存路径
	 * @return JSON串
	 * @author jiayf
	 * @date Mar 27, 2015
	 */
	public static String getDownFileJson(String filename, String tag, String savepath) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("filename", filename);
			obj.put("tag", tag);
			obj.put("savepath", savepath);

			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
