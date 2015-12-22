package cn.com.mobnote.golukmobile.wifidatacenter;

import java.util.List;

public interface IWifiBindDataFn {

	/**
	 * 查询所有的绑定历史记录
	 * 
	 * @return 所有绑定记录
	 * @author jyf
	 */
	public List<WifiBindHistoryBean> getAllBindData();

	/**
	 * 保存一条绑定记录
	 * 
	 * @param bean
	 *            wifi绑定的一条记录
	 * @author jyf
	 */
	public void saveBindData(WifiBindHistoryBean bean);

	/**
	 * 删除一条绑定记录
	 * 
	 * @param ipc_ssid
	 * @author jyf
	 */
	public void deleteBindData(String ipc_ssid);

	/**
	 * 修改绑定状态, 主要是修改连接状态
	 * 
	 * @param ipc_ssid
	 *            ipc名称标识
	 * @param state
	 *            1/0 开/关
	 * @author jyf
	 */
	public void editBindStatus(String ipc_ssid, int state);

	/**
	 * 获取当前正在使用的IPC信息
	 * 
	 * @return
	 * @author jyf
	 */
	public WifiBindHistoryBean getCurrentUseIpc();

	/**
	 * 销毁模块
	 * 
	 * @author jyf
	 */
	public void destroy();

}
