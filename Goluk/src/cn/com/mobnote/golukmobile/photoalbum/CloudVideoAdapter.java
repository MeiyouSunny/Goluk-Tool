package cn.com.mobnote.golukmobile.photoalbum;

import java.util.List;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class CloudVideoAdapter extends PagerAdapter{
	private Context mContext=null;
	private CloudWonderfulVideoListView mCloudWonderfulVideoListView = null;
	private CloudWonderfulVideoListView mCloudEmergencyVideoListView = null;
	private CloudWonderfulVideoListView mCloudLoopVideoListView = null;
	
	public CloudVideoAdapter(Context c) {
		this.mContext=c;
		this.mCloudWonderfulVideoListView = new CloudWonderfulVideoListView(mContext, IPCManagerFn.TYPE_SHORTCUT);
		this.mCloudEmergencyVideoListView = new CloudWonderfulVideoListView(mContext, IPCManagerFn.TYPE_URGENT);
		this.mCloudLoopVideoListView = new CloudWonderfulVideoListView(mContext, IPCManagerFn.TYPE_CIRCULATE);
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		if(0 == position){
			container.addView(mCloudWonderfulVideoListView.getRootView());
			return mCloudWonderfulVideoListView.getRootView();
		}else if(1 == position){
			container.addView(mCloudEmergencyVideoListView.getRootView());
			return mCloudEmergencyVideoListView.getRootView();
		}else {		
			container.addView(mCloudLoopVideoListView.getRootView());
			return mCloudLoopVideoListView.getRootView();
		}
	}

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if(1 == position) {
            container.removeView(mCloudEmergencyVideoListView.getRootView());
        } else if(0 == position) {
            container.removeView(mCloudWonderfulVideoListView.getRootView());
        } else {
            container.removeView(mCloudLoopVideoListView.getRootView());
        }
    }

	@Override
	public int getCount() {
		return 3;
	}
	
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}
	
	public void loadData(int type) {
		if (IPCManagerFn.TYPE_SHORTCUT == type) {
			mCloudWonderfulVideoListView.loadData(true);
		}else if (IPCManagerFn.TYPE_URGENT == type) {
			mCloudEmergencyVideoListView.loadData(true);
		}else if (IPCManagerFn.TYPE_CIRCULATE == type) {
			mCloudLoopVideoListView.loadData(true);
		}
	}
	
	public void deleteDataFlush(int type, List<String> deleteData) {
		if(0 == type) {
			mCloudWonderfulVideoListView.deleteListData(deleteData);
		}else if(1 == type) {
			mCloudEmergencyVideoListView.deleteListData(deleteData);
		}else {
			mCloudLoopVideoListView.deleteListData(deleteData);
		}
	}
	
	public void downloadVideoFlush(int type, List<String> deleteData) {
		if(0 == type) {
			mCloudWonderfulVideoListView.downloadVideoFlush(deleteData);
		}else if(1 == type) {
			mCloudEmergencyVideoListView.downloadVideoFlush(deleteData);
		}else {
			mCloudLoopVideoListView.downloadVideoFlush(deleteData);
		}
	}
	
	public void onResume() {
		mCloudWonderfulVideoListView.onResume();
		mCloudEmergencyVideoListView.onResume();
		mCloudLoopVideoListView.onResume();
	}
	
	public void onDestroy() {
		
	}

}

