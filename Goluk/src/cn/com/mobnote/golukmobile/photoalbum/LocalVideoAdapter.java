package cn.com.mobnote.golukmobile.photoalbum;

import java.util.List;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class LocalVideoAdapter extends PagerAdapter{
	private Context mContext=null;
	private WonderfulVideoListView mWonderfulVideoLiseView = null;
	private WonderfulVideoListView mEmergencyVideoLiseView = null;
	private WonderfulVideoListView mLoopVideoLiseView = null;
	private String from = null;
	
	public LocalVideoAdapter(Context c, String from) {
		this.mContext=c;
		this.from = from;
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		if(0 == position){
			mWonderfulVideoLiseView = new WonderfulVideoListView(mContext, IPCManagerFn.TYPE_SHORTCUT, from);
			container.addView(mWonderfulVideoLiseView.getRootView());
			return mWonderfulVideoLiseView.getRootView();
		}else if(1 == position){
			mEmergencyVideoLiseView = new WonderfulVideoListView(mContext, IPCManagerFn.TYPE_URGENT, from);
			container.addView(mEmergencyVideoLiseView.getRootView());
			return mEmergencyVideoLiseView.getRootView();
		}else {		
			mLoopVideoLiseView = new WonderfulVideoListView(mContext, IPCManagerFn.TYPE_CIRCULATE, from);
			container.addView(mLoopVideoLiseView.getRootView());
			return mLoopVideoLiseView.getRootView();
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
	
	public void flush(int type) {
		if(0 == type) {
			mWonderfulVideoLiseView.flushList();
		}else if(1 == type) {
			mEmergencyVideoLiseView.flushList();
		}else {
			mLoopVideoLiseView.flushList();
		}
	}
	
	public void deleteDataFlush(int type, List<String> deleteData) {
		if(0 == type) {
			mWonderfulVideoLiseView.deleteListData(deleteData);
		}else if(1 == type) {
			mEmergencyVideoLiseView.deleteListData(deleteData);
		}else {
			mLoopVideoLiseView.deleteListData(deleteData);
		}
	}
	
	public void updateData(int type) {
		if(0 == type) {
			mWonderfulVideoLiseView.updateData();
		}else if(1 == type) {
			mEmergencyVideoLiseView.updateData();
		}else {
			mLoopVideoLiseView.updateData();
		}
	}
	
	public void onResume() {
		if(null != mWonderfulVideoLiseView) {
			mWonderfulVideoLiseView.onResume();
		}
		
		if(null != mEmergencyVideoLiseView) {
			mEmergencyVideoLiseView.onResume();
		}
		
		if(null != mLoopVideoLiseView) {
			mLoopVideoLiseView.onResume();
		}
		
	}
	
	public void onDestroy() {
		
	}

}
