package cn.com.mobnote.golukmobile.wifidatacenter;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
import cn.com.mobnote.util.GolukFileUtils;
import cn.com.tiros.debug.GolukDebugUtils;

import com.alibaba.fastjson.JSON;

public class JsonWifiBindManager implements IWifiBindDataFn {

	@Override
	public List<WifiBindHistoryBean> getAllBindData() {
		try {
			String jsonStr = getHistoryJson();
			List<WifiBindHistoryBean> data = JSON.parseArray(jsonStr, WifiBindHistoryBean.class);
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void saveBindData(WifiBindHistoryBean bean) {
		if (!isValid(bean)) {
			return;
		}
		List<WifiBindHistoryBean> dataList = getAllBindData();

		if (null != dataList) {
			for (int i = 0; i < dataList.size(); i++) {
				dataList.get(i).state = 0;
				if (dataList.get(i).ipc_ssid.equals(bean.ipc_ssid)) {
					dataList.remove(i);
				}
			}
		} else {
			dataList = new ArrayList<WifiBindHistoryBean>();
		}

		bean.state = 1;
		dataList.add(0, bean);
		String ss = JSON.toJSONString(dataList);
		saveHistoryJson(ss);
	}

	@Override
	public void deleteBindData(String ipc_ssid) {
		List<WifiBindHistoryBean> dataList = getAllBindData();
		if (null != dataList) {
			for (int i = 0; i < dataList.size(); i++) {
				if (dataList.get(i).ipc_ssid.equals(ipc_ssid)) {
					dataList.remove(i);
				}
			}
		}

		if (null == dataList || dataList.size() <= 0) {
			// 目前没数据
			saveHistoryJson("");
		} else {
			String ss = JSON.toJSONString(dataList);
			saveHistoryJson(ss);
		}
	}

	@Override
	public void editBindStatus(String ipc_ssid, int state) {
		List<WifiBindHistoryBean> dataList = getAllBindData();
		if (null == dataList) {
			return;
		}
		for (int i = 0; i < dataList.size(); i++) {
			if (dataList.get(i).ipc_ssid.equals(ipc_ssid)) {
				dataList.get(i).state = 1;
			} else {
				dataList.get(i).state = 0;
			}
		}
		String ss = JSON.toJSONString(dataList);
		saveHistoryJson(ss);
	}

	@Override
	public WifiBindHistoryBean getCurrentUseIpc() {
		try {
			List<WifiBindHistoryBean> dataList = getAllBindData();
			if (null == dataList) {
				return null;
			}
			for (int i = 0; i < dataList.size(); i++) {
				if (WifiBindHistoryBean.CONN_USE == dataList.get(i).state) {
					return dataList.get(i);
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public boolean isHasDataHistory() {
		List<WifiBindHistoryBean> dataList = getAllBindData();
		if (null != dataList && dataList.size() > 0) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isHasIpc(String ipcssid) {
		if (null == ipcssid || "".equals(ipcssid) || ipcssid.length() <= 0) {
			return false;
		}
		List<WifiBindHistoryBean> dataList = getAllBindData();
		if (null == dataList || dataList.size() <= 0) {
			return false;
		}
		final int size = dataList.size();
		for (int i = 0; i < size; i++) {
			if (ipcssid.equals(dataList.get(i))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void destroy() {

	}

	/**
	 * 判断一个wifi热点参数是否合法
	 * 
	 * @param bean
	 *            true/false 合法/不合法
	 * @return
	 * @author jyf
	 */
	private boolean isValid(WifiBindHistoryBean bean) {
		if (null == bean) {
			return false;
		}
		if (TextUtils.isEmpty(bean.ipc_ssid)) {
			return false;
		}
		if (TextUtils.isEmpty(bean.mobile_ssid)) {
			return false;
		}
		return true;
	}

	/**
	 * 获取绑定历史数据(json)
	 * 
	 * @return
	 * @author jyf
	 */
	private String getHistoryJson() {
		return GolukFileUtils.loadString(GolukFileUtils.KEY_BIND_HISTORY_LIST, null);
	}

	/**
	 * 保存绑定历史数据到本地
	 * 
	 * @param json
	 * @author jyf
	 */
	private void saveHistoryJson(String json) {
		GolukDebugUtils.e("", "BindSaveData-------------saveHistoryJson: " + json);
		GolukFileUtils.saveString(GolukFileUtils.KEY_BIND_HISTORY_LIST, json);
	}

}
