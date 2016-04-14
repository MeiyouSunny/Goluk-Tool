package com.mobnote.golukmain.reportlog;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mobnote.util.JsonUtil;

public class ReportLog {
	/** 绑定成功 */
	public static final String TYPE_SUCESS = "1";
	/** 绑定失败 */
	public static final String TYPE_FAILED = "2";

	private String mKey = null;
	private String mType = "1";
	/** 绑定设备类型, G1, G2, T1 */
	private String mHdType = "";
	private JSONArray valueArray = new JSONArray();

	public ReportLog(String key) {
		mKey = key;
	}

	public void setType(String type) {
		mType = type;
	}

	/**
	 * 设置设备类型
	 * 
	 * @param hdtype
	 *            G1/ G2 /T1
	 * @author jyf
	 */
	public void setHdType(String hdtype) {
		mHdType = hdtype;
	}

	// 添加日志体
	public void addLogData(JSONObject obj) {
		if (null == valueArray) {
			valueArray = new JSONArray();
		}
		valueArray.put(obj);
	}

	// 上报
	public void push() {

	}

	public String getReportData() {
		try {
			JSONObject dataObj = new JSONObject();
			dataObj.put("type", mType);
			dataObj.put("value", valueArray);

			return JsonUtil.getReportJson(mKey, dataObj, mHdType);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	// 清空数据
	public void clear() {
		mType = "1";
		valueArray = null;
		mHdType = "";
	}

}
