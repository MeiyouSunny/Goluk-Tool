package cn.com.mobnote.golukmobile.wifidatacenter;

import java.util.List;

public class WifiBindDataCenter implements IWifiBindDataFn {

	private IWifiBindDataFn mDataDeal;

	private static WifiBindDataCenter mInstance = new WifiBindDataCenter();

	public synchronized static WifiBindDataCenter getInstance() {
		return mInstance;
	}

	public void setAdatper(IWifiBindDataFn instance) {
		mDataDeal = instance;
	}

	@Override
	public List<WifiBindHistoryBean> getAllBindData() {
		if (null == mDataDeal) {
			return null;
		}
		return mDataDeal.getAllBindData();
	}

	@Override
	public void saveBindData(WifiBindHistoryBean bean) {
		if (null == mDataDeal) {
			return;
		}
		mDataDeal.saveBindData(bean);
	}

	@Override
	public void deleteBindData(String ipc_ssid) {
		if (null == mDataDeal) {
			return;
		}
		mDataDeal.deleteBindData(ipc_ssid);
	}

	@Override
	public void editBindStatus(String ipc_ssid, int state) {
		if (null == mDataDeal) {
			return;
		}
		mDataDeal.editBindStatus(ipc_ssid, state);
	}

	@Override
	public void destroy() {
		if (null == mDataDeal) {
			return;
		}
		mDataDeal.destroy();
		mDataDeal = null;
	}

	@Override
	public WifiBindHistoryBean getCurrentUseIpc() {
		if (null == mDataDeal) {
			return null;
		}
		return mDataDeal.getCurrentUseIpc();
	}

	@Override
	public boolean isHasDataHistory() {
		if (null == mDataDeal) {
			return false;
		}
		return mDataDeal.isHasDataHistory();
	}

}
