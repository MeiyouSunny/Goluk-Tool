package cn.com.mobnote.golukmobile.wifidatacenter;

import java.util.List;

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
		final String jsonStr = getHistoryJson();
		List<WifiBindHistoryBean> dataList = JSON.parseArray(jsonStr, WifiBindHistoryBean.class);
		for (int i = 0; i < dataList.size(); i++) {
			dataList.get(i).state = 0;
			if (dataList.get(i).ipc_ssid.equals(bean.ipc_ssid)) {
				dataList.remove(i);
			}
		}
		bean.state = 1;
		dataList.add(0, bean);
		String ss = JSON.toJSONString(dataList);
		saveHistoryJson(ss);
	}

	@Override
	public void deleteBindData(String ipc_ssid) {
		final String jsonStr = getHistoryJson();
		List<WifiBindHistoryBean> dataList = JSON.parseArray(jsonStr, WifiBindHistoryBean.class);
		for (int i = 0; i < dataList.size(); i++) {
			if (dataList.get(i).ipc_ssid.equals(ipc_ssid)) {
				dataList.remove(i);
			}
		}

		String ss = JSON.toJSONString(dataList);
		saveHistoryJson(ss);
	}

	@Override
	public void editBindStatus(String ipc_ssid, int state) {
		final String jsonStr = getHistoryJson();
		List<WifiBindHistoryBean> dataList = JSON.parseArray(jsonStr, WifiBindHistoryBean.class);
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

	// 获取数据
	private String getHistoryJson() {
		return "";
	}

	// 保存数据到数据数据库
	private void saveHistoryJson(String json) {

	}

}
