package cn.com.mobnote.golukmobile.wifidatacenter;

import java.util.List;

public interface IWifiBindDataFn {

	// 查询所有的绑定信息
	public List<WifiBindHistoryBean> getAllBindData();

	// 保存一条绑定记录
	public void saveBindData(WifiBindHistoryBean bean);

	// 删除一条绑定记录
	public void deleteBindData(String ipc_ssid);

	// 修改绑定状态
	public void editBindStatus(String ipc_ssid, int state);

}
