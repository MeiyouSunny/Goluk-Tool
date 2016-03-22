package cn.com.mobnote.golukmobile.photoalbum;

import java.util.List;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class CloudPagerAdapter extends PagerAdapter {
	private Context mContext = null;
	private CloudWonderfulVideoListView mCloudWonderfulVideoListView = null;
	private CloudWonderfulVideoListView mCloudEmergencyVideoListView = null;
	private CloudWonderfulVideoListView mCloudLoopVideoListView = null;

	public CloudPagerAdapter(Context c, CloudVideoManager cloudVideoListView) {
		//CK Start
//		this.mContext = c;
//		this.mCloudWonderfulVideoListView = new CloudWonderfulVideoListView(mContext, cloudVideoListView,
//				IPCManagerFn.TYPE_SHORTCUT);
//		this.mCloudEmergencyVideoListView = new CloudWonderfulVideoListView(mContext, cloudVideoListView,
//				IPCManagerFn.TYPE_URGENT);
//		this.mCloudLoopVideoListView = new CloudWonderfulVideoListView(mContext, cloudVideoListView,
//				IPCManagerFn.TYPE_CIRCULATE);
		//CK End
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		if (0 == position) {
			container.addView(mCloudWonderfulVideoListView.getRootView());
			return mCloudWonderfulVideoListView.getRootView();
		} else if (1 == position) {
			container.addView(mCloudEmergencyVideoListView.getRootView());
			return mCloudEmergencyVideoListView.getRootView();
		} else {
			container.addView(mCloudLoopVideoListView.getRootView());
			return mCloudLoopVideoListView.getRootView();
		}
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		if (1 == position) {
			container.removeView(mCloudEmergencyVideoListView.getRootView());
		} else if (0 == position) {
			container.removeView(mCloudWonderfulVideoListView.getRootView());
		} else {
			container.removeView(mCloudLoopVideoListView.getRootView());
		}
	}

	@Override
	public int getCount() {
		return 3;
	}

	public boolean isWonderfulHasData() {
		if (null == mCloudWonderfulVideoListView) {
			return false;
		}
		return mCloudWonderfulVideoListView.isHasData();
	}

	public boolean isEmergencyHasData() {
		if (null == mCloudEmergencyVideoListView) {
			return false;
		}
		return mCloudEmergencyVideoListView.isHasData();
	}

	public boolean isLoopHasData() {
		if (null == mCloudLoopVideoListView) {
			return false;
		}
		return mCloudLoopVideoListView.isHasData();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	public void loadData(int type) {
		if (IPCManagerFn.TYPE_SHORTCUT == type) {
			mCloudWonderfulVideoListView.loadData(true);
		} else if (IPCManagerFn.TYPE_URGENT == type) {
			mCloudEmergencyVideoListView.loadData(true);
		} else if (IPCManagerFn.TYPE_CIRCULATE == type) {
			mCloudLoopVideoListView.loadData(true);
		}
	}

	public void deleteDataFlush(int type, List<String> deleteData) {
		if (IPCManagerFn.TYPE_SHORTCUT == type) {
			mCloudWonderfulVideoListView.deleteListData(deleteData);
		} else if (IPCManagerFn.TYPE_URGENT == type) {
			mCloudEmergencyVideoListView.deleteListData(deleteData);
		} else {
			mCloudLoopVideoListView.deleteListData(deleteData);
		}
	}

	public void downloadVideoFlush(int type, List<String> deleteData) {
		if (IPCManagerFn.TYPE_SHORTCUT == type) {
			mCloudWonderfulVideoListView.downloadVideoFlush(deleteData);
		} else if (IPCManagerFn.TYPE_URGENT == type) {
			mCloudEmergencyVideoListView.downloadVideoFlush(deleteData);
		} else {
			mCloudLoopVideoListView.downloadVideoFlush(deleteData);
		}
	}

//	public void updateAsncFlag(int type, String filename) {
//		if (IPCManagerFn.TYPE_SHORTCUT == type) {
//			mCloudWonderfulVideoListView.updateAsyncFlag(filename, false);
//		} else if (IPCManagerFn.TYPE_URGENT == type) {
//			mCloudEmergencyVideoListView.updateAsyncFlag(filename, false);
//		} else {
//			mCloudLoopVideoListView.updateAsyncFlag(filename, false);
//		}
//	}

	public void onResume() {
		mCloudWonderfulVideoListView.onResume();
		mCloudEmergencyVideoListView.onResume();
		mCloudLoopVideoListView.onResume();
	}

	public void onDestroy() {
		mCloudWonderfulVideoListView.onDestory();
		mCloudEmergencyVideoListView.onDestory();
		mCloudLoopVideoListView.onDestory();
	}

}
