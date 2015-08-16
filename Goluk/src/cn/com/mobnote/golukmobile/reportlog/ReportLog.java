package cn.com.mobnote.golukmobile.reportlog;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.com.mobnote.util.JsonUtil;

public class ReportLog {
	/** 绑定成功 */
	public static final String TYPE_SUCESS = "1";
	/** 绑定失败 */
	public static final String TYPE_FAILED = "2";

	private String mKey = null;
	private String mType = "1";
	private JSONArray valueArray = new JSONArray();

	public ReportLog(String key) {
		mKey = key;
	}

	public void setType(String type) {
		mType = type;
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

			return JsonUtil.getReportJson(mKey, dataObj);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	// 清空数据
	public void clear() {
		mType = "1";
		valueArray = null;
	}

}
